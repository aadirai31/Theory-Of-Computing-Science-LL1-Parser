import java.util.ArrayList;
import java.util.List;

/**
 * Lexer for the MiniLisp language.
 * Tokenizes input according to the specification:
 * - NUMBER: [0-9]+
 * - IDENTIFIER: [a-zA-Z][a-zA-Z0-9]*
 * - Operators: +, −(U+2212), ×, =, ?, λ, ≜
 * - Delimiters: (, )
 *
 * CRITICAL: The MINUS token is Unicode U+2212 (−), NOT the ASCII dash U+002D
 * (-)
 */
public class Lexer {
    private final String input;
    private int position;
    private int line;
    private int column;

    // Unicode constants for clarity
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

    /**
     * Tokenizes the entire input and returns a list of tokens.
     */
    public List<Token> tokenize() throws LexerException {
        List<Token> tokens = new ArrayList<>();

        while (!isAtEnd()) {
            skipWhitespace();
            if (isAtEnd())
                break;

            Token token = nextToken();
            tokens.add(token);
        }

        // Add EOF token
        tokens.add(new Token(TokenType.EOF, line, column));

        return tokens;
    }

    /**
     * Reads and returns the next token from the input.
     */
    private Token nextToken() throws LexerException {
        char current = peek();
        int tokenLine = line;
        int tokenColumn = column;

        // Check for single-character tokens first
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

        // Check for NUMBER: [0-9]+
        if (isDigit(current)) {
            return scanNumber(tokenLine, tokenColumn);
        }

        // Check for IDENTIFIER: [a-zA-Z][a-zA-Z0-9]*
        if (isAlpha(current)) {
            return scanIdentifier(tokenLine, tokenColumn);
        }

        // Special error message for common Unicode mistake
        if (current == '-') { // ASCII dash U+002D
            throw new LexerException(
                    String.format("Invalid character '-' (ASCII dash U+002D) at line %d, column %d. " +
                            "Did you mean '−' (Unicode minus U+2212)?",
                            line, column));
        }

        // Unrecognized character
        throw new LexerException(
                String.format("Unexpected character '%c' at line %d, column %d",
                        current, line, column));
    }

    /**
     * Scans a NUMBER token: [0-9]+
     */
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

    /**
     * Scans an IDENTIFIER token: [a-zA-Z][a-zA-Z0-9]*
     */
    private Token scanIdentifier(int tokenLine, int tokenColumn) {
        StringBuilder identifier = new StringBuilder();

        // First character must be alphabetic
        identifier.append(advance());

        // Subsequent characters can be alphanumeric
        while (!isAtEnd() && isAlphanumeric(peek())) {
            identifier.append(advance());
        }

        return new Token(TokenType.IDENTIFIER, identifier.toString(), tokenLine, tokenColumn);
    }

    /**
     * Skips whitespace and updates line/column tracking.
     */
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

    /**
     * Returns the current character without consuming it.
     */
    private char peek() {
        if (isAtEnd())
            return '\0';
        return input.charAt(position);
    }

    /**
     * Consumes and returns the current character, updating position and column.
     */
    private char advance() {
        char c = input.charAt(position);
        position++;
        column++;
        return c;
    }

    /**
     * Checks if we've reached the end of input.
     */
    private boolean isAtEnd() {
        return position >= input.length();
    }

    /**
     * Checks if a character is a digit (0-9).
     */
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    /**
     * Checks if a character is alphabetic (a-z, A-Z).
     */
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    /**
     * Checks if a character is alphanumeric (a-z, A-Z, 0-9).
     */
    private boolean isAlphanumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
}