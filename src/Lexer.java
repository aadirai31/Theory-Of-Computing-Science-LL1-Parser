import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private final String input;
    private int position;
    private int line;
    private int column;

    private static final char UNICODE_MINUS = '\u2212'; // − (U+2212)
    private static final char UNICODE_MULT = '\u00D7'; // × (U+00D7)
    private static final char UNICODE_LAMBDA = '\u03BB'; // λ (U+03BB)
    private static final char UNICODE_LET = '\u225C'; // ≜ (U+225C)

    public Lexer(String input) {
        this.input = input;
        this.position = 0;
        this.line = 1;
        this.column = 1;
    }

    public List<Token> tokenize() throws LexerException {
        List<Token> tokens = new ArrayList<>();

        while (!isAtEnd()) {
            skipWhitespace();
            if (isAtEnd())
                break;

            Token token = nextToken();
            tokens.add(token);
        }

        tokens.add(new Token(TokenType.EOF, line, column));

        return tokens;
    }

    private Token nextToken() throws LexerException {
        char current = peek();
        int tokenLine = line;
        int tokenColumn = column;

        switch (current) {
            case '+':
                advance();
                return new Token(TokenType.PLUS, tokenLine, tokenColumn);

            case UNICODE_MINUS: // − (U+2212)
                advance();
                return new Token(TokenType.MINUS, tokenLine, tokenColumn);

            case UNICODE_MULT: // × (U+00D7)
                advance();
                return new Token(TokenType.MULT, tokenLine, tokenColumn);

            case '=':
                advance();
                return new Token(TokenType.EQUALS, tokenLine, tokenColumn);

            case '?':
                advance();
                return new Token(TokenType.CONDITIONAL, tokenLine, tokenColumn);

            case UNICODE_LAMBDA: // λ (U+03BB)
                advance();
                return new Token(TokenType.LAMBDA, tokenLine, tokenColumn);

            case UNICODE_LET: // ≜ (U+225C)
                advance();
                return new Token(TokenType.LET, tokenLine, tokenColumn);

            case '(':
                advance();
                return new Token(TokenType.LPAREN, tokenLine, tokenColumn);

            case ')':
                advance();
                return new Token(TokenType.RPAREN, tokenLine, tokenColumn);
        }

        if (isDigit(current)) {
            return scanNumber(tokenLine, tokenColumn);
        }

        if (isAlpha(current)) {
            return scanIdentifier(tokenLine, tokenColumn);
        }

        if (current == '-') { // ASCII dash U+002D
            throw new LexerException(
                    String.format("Invalid character '-' (ASCII dash U+002D) at line %d, column %d. " +
                            "Did you mean '−' (Unicode minus U+2212)?",
                            line, column));
        }

        throw new LexerException(
                String.format("Unexpected character '%c' at line %d, column %d",
                        current, line, column));
    }

    private Token scanNumber(int tokenLine, int tokenColumn) throws LexerException {
        StringBuilder number = new StringBuilder();

        while (!isAtEnd() && isDigit(peek())) {
            number.append(advance());
        }

        if (isAlpha(peek())) {
            throw new LexerException(
                    String.format("Invalid character '%c' in number at line %d, column %d",
                            peek(), line, column));
        }

        return new Token(TokenType.NUMBER, number.toString(), tokenLine, tokenColumn);
    }

    private Token scanIdentifier(int tokenLine, int tokenColumn) {
        StringBuilder identifier = new StringBuilder();

        identifier.append(advance());

        while (!isAtEnd() && isAlphanumeric(peek())) {
            identifier.append(advance());
        }

        return new Token(TokenType.IDENTIFIER, identifier.toString(), tokenLine, tokenColumn);
    }

    private void skipWhitespace() {
        while (!isAtEnd()) {
            char c = peek();
            switch (c) {
                case ' ':
                case '\r':
                case '\t':
                    advance();
                    break;
                case '\n':
                    advance();
                    line++;
                    column = 1;
                    break;
                default:
                    return;
            }
        }
    }

    private char peek() {
        if (isAtEnd())
            return '\0';
        return input.charAt(position);
    }

    private char advance() {
        char c = input.charAt(position);
        position++;
        column++;
        return c;
    }

    private boolean isAtEnd() {
        return position >= input.length();
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private boolean isAlphanumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
}