/**
 * Exception thrown when the lexer encounters an error during tokenization.
 */
public class LexerException extends Exception {
    public LexerException(String message) {
        super(message);
    }

    public LexerException(String message, Throwable cause) {
        super(message, cause);
    }
}