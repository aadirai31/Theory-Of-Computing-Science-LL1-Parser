import java.util.*;

/**
 * Table-driven LL(1) parser for MiniLisp.
 * Uses an explicit parse table and stack-based parsing algorithm.
 *
 * The parser implements the standard LL(1) table-driven algorithm:
 * 1. Initialize stack with start symbol and EOF
 * 2. Loop while stack is not empty:
 *    - Pop top of stack
 *    - If terminal: match with current input token
 *    - If non-terminal: look up production in parse table and push RHS
 * 3. Accept when stack is empty and input consumed
 *
 * Parse tree is built using a semantic stack alongside the parse stack.
 */
public class Parser {
    /**
     * Non-terminals in the grammar.
     */
    private enum NonTerminal {
        PROGRAM,      // <program>
        EXPR,         // <expr>
        PAREN_EXPR    // <paren-expr>
    }

    /**
     * Stack symbols: terminals, non-terminals, and semantic actions.
     */
    private static abstract class StackSymbol {
        abstract boolean isTerminal();
        abstract boolean isNonTerminal();
        abstract boolean isAction();
    }

    private static class TerminalSymbol extends StackSymbol {
        final TokenType type;
        TerminalSymbol(TokenType type) { this.type = type; }
        boolean isTerminal() { return true; }
        boolean isNonTerminal() { return false; }
        boolean isAction() { return false; }
        public String toString() { return type.toString(); }
    }

    private static class NonTerminalSymbol extends StackSymbol {
        final NonTerminal nt;
        NonTerminalSymbol(NonTerminal nt) { this.nt = nt; }
        boolean isTerminal() { return false; }
        boolean isNonTerminal() { return true; }
        boolean isAction() { return false; }
        public String toString() { return "<" + nt + ">"; }
    }

    private static abstract class SemanticAction extends StackSymbol {
        boolean isTerminal() { return false; }
        boolean isNonTerminal() { return false; }
        boolean isAction() { return true; }
        abstract void execute(Stack<Object> semanticStack);
    }

    /**
     * Production rule with its RHS symbols.
     */
    private static class Production {
        final int number;
        final List<StackSymbol> rhs;

        Production(int number, StackSymbol... symbols) {
            this.number = number;
            this.rhs = Arrays.asList(symbols);
        }
    }

    private final List<Token> tokens;
    private int currentTokenIndex;
    private final Map<String, Production> parseTable;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.currentTokenIndex = 0;
        this.parseTable = new HashMap<>();
        initializeParseTable();
    }

    /**
     * Initializes the LL(1) parse table based on parsetable.md.
     * Maps (NonTerminal, Terminal) pairs to Production rules.
     */
    private void initializeParseTable() {
        // Production 1: <program> → <expr>
        Production prod1 = new Production(1, new NonTerminalSymbol(NonTerminal.EXPR));
        addToTable(NonTerminal.PROGRAM, TokenType.NUMBER, prod1);
        addToTable(NonTerminal.PROGRAM, TokenType.IDENTIFIER, prod1);
        addToTable(NonTerminal.PROGRAM, TokenType.LPAREN, prod1);

        // Production 2: <expr> → NUMBER
        Production prod2 = new Production(2, new TerminalSymbol(TokenType.NUMBER));
        addToTable(NonTerminal.EXPR, TokenType.NUMBER, prod2);

        // Production 3: <expr> → IDENTIFIER
        Production prod3 = new Production(3, new TerminalSymbol(TokenType.IDENTIFIER));
        addToTable(NonTerminal.EXPR, TokenType.IDENTIFIER, prod3);

        // Production 4: <expr> → '(' <paren-expr> ')'
        Production prod4 = new Production(4,
            new TerminalSymbol(TokenType.LPAREN),
            new NonTerminalSymbol(NonTerminal.PAREN_EXPR),
            new TerminalSymbol(TokenType.RPAREN));
        addToTable(NonTerminal.EXPR, TokenType.LPAREN, prod4);

        // Production 5: <paren-expr> → '+' <expr> <expr>
        Production prod5 = new Production(5,
            new TerminalSymbol(TokenType.PLUS),
            new NonTerminalSymbol(NonTerminal.EXPR),
            new NonTerminalSymbol(NonTerminal.EXPR),
            new BuildBinaryOp("PLUS"));
        addToTable(NonTerminal.PAREN_EXPR, TokenType.PLUS, prod5);

        // Production 6: <paren-expr> → '×' <expr> <expr>
        Production prod6 = new Production(6,
            new TerminalSymbol(TokenType.MULT),
            new NonTerminalSymbol(NonTerminal.EXPR),
            new NonTerminalSymbol(NonTerminal.EXPR),
            new BuildBinaryOp("MULT"));
        addToTable(NonTerminal.PAREN_EXPR, TokenType.MULT, prod6);

        // Production 7: <paren-expr> → '=' <expr> <expr>
        Production prod7 = new Production(7,
            new TerminalSymbol(TokenType.EQUALS),
            new NonTerminalSymbol(NonTerminal.EXPR),
            new NonTerminalSymbol(NonTerminal.EXPR),
            new BuildBinaryOp("EQUALS"));
        addToTable(NonTerminal.PAREN_EXPR, TokenType.EQUALS, prod7);

        // Production 8: <paren-expr> → '−' <expr> <expr>
        Production prod8 = new Production(8,
            new TerminalSymbol(TokenType.MINUS),
            new NonTerminalSymbol(NonTerminal.EXPR),
            new NonTerminalSymbol(NonTerminal.EXPR),
            new BuildBinaryOp("MINUS"));
        addToTable(NonTerminal.PAREN_EXPR, TokenType.MINUS, prod8);

        // Production 9: <paren-expr> → '?' <expr> <expr> <expr>
        Production prod9 = new Production(9,
            new TerminalSymbol(TokenType.CONDITIONAL),
            new NonTerminalSymbol(NonTerminal.EXPR),
            new NonTerminalSymbol(NonTerminal.EXPR),
            new NonTerminalSymbol(NonTerminal.EXPR),
            new BuildConditional());
        addToTable(NonTerminal.PAREN_EXPR, TokenType.CONDITIONAL, prod9);

        // Production 10: <paren-expr> → 'λ' IDENTIFIER <expr>
        Production prod10 = new Production(10,
            new TerminalSymbol(TokenType.LAMBDA),
            new TerminalSymbol(TokenType.IDENTIFIER),
            new NonTerminalSymbol(NonTerminal.EXPR),
            new BuildLambda());
        addToTable(NonTerminal.PAREN_EXPR, TokenType.LAMBDA, prod10);

        // Production 11: <paren-expr> → '≜' IDENTIFIER <expr> <expr>
        Production prod11 = new Production(11,
            new TerminalSymbol(TokenType.LET),
            new TerminalSymbol(TokenType.IDENTIFIER),
            new NonTerminalSymbol(NonTerminal.EXPR),
            new NonTerminalSymbol(NonTerminal.EXPR),
            new BuildLet());
        addToTable(NonTerminal.PAREN_EXPR, TokenType.LET, prod11);

        // Production 12: <paren-expr> → <expr> <expr>*
        // This is handled specially with a marker to collect multiple expressions
        Production prod12 = new Production(12, new CollectFunctionApp());
        addToTable(NonTerminal.PAREN_EXPR, TokenType.NUMBER, prod12);
        addToTable(NonTerminal.PAREN_EXPR, TokenType.IDENTIFIER, prod12);
        addToTable(NonTerminal.PAREN_EXPR, TokenType.LPAREN, prod12);
    }

    private void addToTable(NonTerminal nt, TokenType term, Production prod) {
        parseTable.put(nt + "," + term, prod);
    }

    // Semantic action: Build binary operation node [OP, left, right]
    private static class BuildBinaryOp extends SemanticAction {
        final String opName;
        BuildBinaryOp(String opName) { this.opName = opName; }
        void execute(Stack<Object> semanticStack) {
            Object right = semanticStack.pop();
            Object left = semanticStack.pop();
            List<Object> node = new ArrayList<>();
            node.add(opName);
            node.add(left);
            node.add(right);
            semanticStack.push(node);
        }
    }

    // Semantic action: Build conditional node [CONDITIONAL, cond, then, else]
    private static class BuildConditional extends SemanticAction {
        void execute(Stack<Object> semanticStack) {
            Object elseExpr = semanticStack.pop();
            Object thenExpr = semanticStack.pop();
            Object condition = semanticStack.pop();
            List<Object> node = new ArrayList<>();
            node.add("CONDITIONAL");
            node.add(condition);
            node.add(thenExpr);
            node.add(elseExpr);
            semanticStack.push(node);
        }
    }

    // Semantic action: Build lambda node [LAMBDA, param, body]
    private static class BuildLambda extends SemanticAction {
        void execute(Stack<Object> semanticStack) {
            Object body = semanticStack.pop();
            Object param = semanticStack.pop();
            List<Object> node = new ArrayList<>();
            node.add("LAMBDA");
            node.add(param);
            node.add(body);
            semanticStack.push(node);
        }
    }

    // Semantic action: Build let node [LET, var, value, body]
    private static class BuildLet extends SemanticAction {
        void execute(Stack<Object> semanticStack) {
            Object body = semanticStack.pop();
            Object value = semanticStack.pop();
            Object var = semanticStack.pop();
            List<Object> node = new ArrayList<>();
            node.add("LET");
            node.add(var);
            node.add(value);
            node.add(body);
            semanticStack.push(node);
        }
    }

    // Special action: Collect function application <expr> <expr>*
    private class CollectFunctionApp extends SemanticAction {
        void execute(Stack<Object> semanticStack) {
            // This marker indicates we should parse function + args until ')'
            // We handle this specially in the main parse loop
        }
    }

    /**
     * Main parsing method: implements table-driven LL(1) algorithm.
     */
    public Object parse() throws ParseException {
        Stack<StackSymbol> parseStack = new Stack<>();
        Stack<Object> semanticStack = new Stack<>();

        // Initialize: push EOF and start symbol
        parseStack.push(new TerminalSymbol(TokenType.EOF));
        parseStack.push(new NonTerminalSymbol(NonTerminal.PROGRAM));

        while (!parseStack.isEmpty()) {
            StackSymbol top = parseStack.pop();
            Token currentToken = getCurrentToken();

            if (top.isAction()) {
                // Execute semantic action
                ((SemanticAction) top).execute(semanticStack);
            }
            else if (top.isTerminal()) {
                // Match terminal
                TerminalSymbol termSym = (TerminalSymbol) top;
                if (termSym.type != currentToken.getType()) {
                    throw new ParseException(
                        String.format("Expected %s but found %s at line %d, column %d",
                            termSym.type, currentToken.getType(),
                            currentToken.getLine(), currentToken.getColumn()));
                }

                // Consume token and push to semantic stack if it has a value
                if (termSym.type == TokenType.NUMBER) {
                    semanticStack.push(Integer.parseInt(currentToken.getValue()));
                } else if (termSym.type == TokenType.IDENTIFIER) {
                    semanticStack.push(currentToken.getValue());
                }

                if (termSym.type != TokenType.EOF) {
                    advance();
                }
            }
            else if (top.isNonTerminal()) {
                // Expand non-terminal using parse table
                NonTerminalSymbol ntSym = (NonTerminalSymbol) top;
                Production production = getProduction(ntSym.nt, currentToken.getType());

                if (production == null) {
                    throw new ParseException(
                        String.format("Unexpected %s at line %d, column %d in context of %s",
                            currentToken.getType(), currentToken.getLine(),
                            currentToken.getColumn(), ntSym.nt));
                }

                // Special handling for production 12 (function application)
                if (production.number == 12) {
                    List<Object> funcApp = parseFunctionApplication();
                    semanticStack.push(funcApp);
                } else {
                    // Push production RHS in reverse order
                    for (int i = production.rhs.size() - 1; i >= 0; i--) {
                        parseStack.push(production.rhs.get(i));
                    }
                }
            }
        }

        // Result should be on top of semantic stack
        if (semanticStack.isEmpty()) {
            throw new ParseException("Parse completed but no result generated");
        }

        return semanticStack.pop();
    }

    /**
     * Special handling for production 12: <expr> <expr>*
     * Parses function and zero or more arguments.
     */
    private List<Object> parseFunctionApplication() throws ParseException {
        List<Object> result = new ArrayList<>();

        // Parse function (first expr)
        Object function = parseSingleExpr();
        result.add(function);

        // Parse arguments (expr*)
        while (getCurrentToken().getType() != TokenType.RPAREN &&
               getCurrentToken().getType() != TokenType.EOF) {
            TokenType lookahead = getCurrentToken().getType();

            if (lookahead == TokenType.NUMBER ||
                lookahead == TokenType.IDENTIFIER ||
                lookahead == TokenType.LPAREN) {
                Object arg = parseSingleExpr();
                result.add(arg);
            } else {
                break;
            }
        }

        return result;
    }

    /**
     * Parses a single expression for function application.
     * This is a mini-parser for just <expr> non-terminal.
     */
    private Object parseSingleExpr() throws ParseException {
        Token currentToken = getCurrentToken();
        TokenType lookahead = currentToken.getType();

        if (lookahead == TokenType.NUMBER) {
            int value = Integer.parseInt(currentToken.getValue());
            advance();
            return value;
        } else if (lookahead == TokenType.IDENTIFIER) {
            String value = currentToken.getValue();
            advance();
            return value;
        } else if (lookahead == TokenType.LPAREN) {
            advance(); // consume '('

            // Now parse <paren-expr>
            Object parenExpr = parseParenExprForFuncApp();

            if (getCurrentToken().getType() != TokenType.RPAREN) {
                throw new ParseException(
                    String.format("Expected ')' but found %s at line %d, column %d",
                        getCurrentToken().getType(),
                        getCurrentToken().getLine(),
                        getCurrentToken().getColumn()));
            }
            advance(); // consume ')'

            return parenExpr;
        } else {
            throw new ParseException(
                String.format("Unexpected %s at line %d, column %d in expression",
                    lookahead, currentToken.getLine(), currentToken.getColumn()));
        }
    }

    /**
     * Parses <paren-expr> for function application context.
     */
    private Object parseParenExprForFuncApp() throws ParseException {
        Token currentToken = getCurrentToken();
        TokenType lookahead = currentToken.getType();

        switch (lookahead) {
            case PLUS:
                advance();
                return buildBinaryOp("PLUS");
            case MULT:
                advance();
                return buildBinaryOp("MULT");
            case EQUALS:
                advance();
                return buildBinaryOp("EQUALS");
            case MINUS:
                advance();
                return buildBinaryOp("MINUS");
            case CONDITIONAL:
                advance();
                return buildConditional();
            case LAMBDA:
                advance();
                return buildLambda();
            case LET:
                advance();
                return buildLet();
            case NUMBER:
            case IDENTIFIER:
            case LPAREN:
                return parseFunctionApplication();
            default:
                throw new ParseException(
                    String.format("Unexpected %s at line %d, column %d in parenthesized expression",
                        lookahead, currentToken.getLine(), currentToken.getColumn()));
        }
    }

    private List<Object> buildBinaryOp(String opName) throws ParseException {
        Object left = parseSingleExpr();
        Object right = parseSingleExpr();
        List<Object> result = new ArrayList<>();
        result.add(opName);
        result.add(left);
        result.add(right);
        return result;
    }

    private List<Object> buildConditional() throws ParseException {
        Object condition = parseSingleExpr();
        Object thenExpr = parseSingleExpr();
        Object elseExpr = parseSingleExpr();
        List<Object> result = new ArrayList<>();
        result.add("CONDITIONAL");
        result.add(condition);
        result.add(thenExpr);
        result.add(elseExpr);
        return result;
    }

    private List<Object> buildLambda() throws ParseException {
        Token idToken = getCurrentToken();
        if (idToken.getType() != TokenType.IDENTIFIER) {
            throw new ParseException(
                String.format("Expected identifier after λ at line %d, column %d",
                    idToken.getLine(), idToken.getColumn()));
        }
        String param = idToken.getValue();
        advance();

        Object body = parseSingleExpr();

        List<Object> result = new ArrayList<>();
        result.add("LAMBDA");
        result.add(param);
        result.add(body);
        return result;
    }

    private List<Object> buildLet() throws ParseException {
        Token idToken = getCurrentToken();
        if (idToken.getType() != TokenType.IDENTIFIER) {
            throw new ParseException(
                String.format("Expected identifier after ≜ at line %d, column %d",
                    idToken.getLine(), idToken.getColumn()));
        }
        String var = idToken.getValue();
        advance();

        Object value = parseSingleExpr();
        Object body = parseSingleExpr();

        List<Object> result = new ArrayList<>();
        result.add("LET");
        result.add(var);
        result.add(value);
        result.add(body);
        return result;
    }

    private Production getProduction(NonTerminal nt, TokenType term) {
        return parseTable.get(nt + "," + term);
    }

    private Token getCurrentToken() {
        if (currentTokenIndex < tokens.size()) {
            return tokens.get(currentTokenIndex);
        }
        return tokens.get(tokens.size() - 1);
    }

    private void advance() {
        if (currentTokenIndex < tokens.size() - 1) {
            currentTokenIndex++;
        }
    }
}
