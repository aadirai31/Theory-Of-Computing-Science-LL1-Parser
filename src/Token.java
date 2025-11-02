/**
 * Represents a token in the MiniLisp language.
 * Each token has a type and an optional value (for NUMBER and IDENTIFIER tokens).
 */
public class Token {
    private final TokenType type;
    private final String value;
    private final int line;
    private final int column;

    /**
     * Constructs a token without a value (for operators and keywords).
     */
    public Token(TokenType type, int line, int column) {
        this(type, null, line, column);
    }

    /**
     * Constructs a token with a value (for NUMBER and IDENTIFIER).
     */
    public Token(TokenType type, String value, int line, int column) {
        this.type = type;
        this.value = value;
        this.line = line;
        this.column = column;
    }

    public TokenType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public String toString() {
        if (value != null) {
            return String.format("Token(%s, '%s', %d:%d)", type, value, line, column);
        }
        return String.format("Token(%s, %d:%d)", type, line, column);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Token)) return false;
        Token other = (Token) obj;
        return type == other.type &&
                (value == null ? other.value == null : value.equals(other.value));
    }

    @Override
    public int hashCode() {
        return type.hashCode() * 31 + (value == null ? 0 : value.hashCode());
    }
}