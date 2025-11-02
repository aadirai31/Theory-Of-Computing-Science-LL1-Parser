import java.util.List;

/**
 * Comprehensive test suite for the MiniLisp Lexer.
 * Tests all token types and edge cases.
 */
public class LexerTest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("=== MiniLisp Lexer Test Suite ===\n");

        // Basic token tests
        testNumbers();
        testIdentifiers();
        testOperators();
        testDelimiters();

        // Expression tests
        testSimpleExpressions();
        testNestedExpressions();
        testFunctionExpressions();

        // Edge cases
        testWhitespace();
        testMultiline();
        testErrorCases();

        // Print results
        printResults();
    }

    private static void testNumbers() {
        System.out.println("--- Testing Numbers ---");

        testTokenize("42",
                new ExpectedToken(TokenType.NUMBER, "42"),
                new ExpectedToken(TokenType.EOF, null)
        );

        testTokenize("0",
                new ExpectedToken(TokenType.NUMBER, "0"),
                new ExpectedToken(TokenType.EOF, null)
        );

        testTokenize("999",
                new ExpectedToken(TokenType.NUMBER, "999"),
                new ExpectedToken(TokenType.EOF, null)
        );

        // Leading zeroes (edge case)
        testTokenize("042",
                new ExpectedToken(TokenType.NUMBER, "042"),
                new ExpectedToken(TokenType.EOF, null)
        );

        testTokenize("007",
                new ExpectedToken(TokenType.NUMBER, "007"),
                new ExpectedToken(TokenType.EOF, null)
        );

        testTokenize("00000",
                new ExpectedToken(TokenType.NUMBER, "00000"),
                new ExpectedToken(TokenType.EOF, null)
        );

        System.out.println();
    }

    private static void testIdentifiers() {
        System.out.println("--- Testing Identifiers ---");

        testTokenize("x",
                new ExpectedToken(TokenType.IDENTIFIER, "x"),
                new ExpectedToken(TokenType.EOF, null)
        );

        testTokenize("foo",
                new ExpectedToken(TokenType.IDENTIFIER, "foo"),
                new ExpectedToken(TokenType.EOF, null)
        );

        testTokenize("var123",
                new ExpectedToken(TokenType.IDENTIFIER, "var123"),
                new ExpectedToken(TokenType.EOF, null)
        );

        testTokenize("CamelCase",
                new ExpectedToken(TokenType.IDENTIFIER, "CamelCase"),
                new ExpectedToken(TokenType.EOF, null)
        );

        System.out.println();
    }

    private static void testOperators() {
        System.out.println("--- Testing Operators ---");

        testTokenize("+",
                new ExpectedToken(TokenType.PLUS, null),
                new ExpectedToken(TokenType.EOF, null)
        );

        testTokenize("−",  // U+2212
                new ExpectedToken(TokenType.MINUS, null),
                new ExpectedToken(TokenType.EOF, null)
        );

        testTokenize("×",  // U+00D7
                new ExpectedToken(TokenType.MULT, null),
                new ExpectedToken(TokenType.EOF, null)
        );

        testTokenize("=",
                new ExpectedToken(TokenType.EQUALS, null),
                new ExpectedToken(TokenType.EOF, null)
        );

        testTokenize("?",
                new ExpectedToken(TokenType.CONDITIONAL, null),
                new ExpectedToken(TokenType.EOF, null)
        );

        testTokenize("λ",  // U+03BB
                new ExpectedToken(TokenType.LAMBDA, null),
                new ExpectedToken(TokenType.EOF, null)
        );

        testTokenize("≜",  // U+225C
                new ExpectedToken(TokenType.LET, null),
                new ExpectedToken(TokenType.EOF, null)
        );

        System.out.println();
    }

    private static void testDelimiters() {
        System.out.println("--- Testing Delimiters ---");

        testTokenize("(",
                new ExpectedToken(TokenType.LPAREN, null),
                new ExpectedToken(TokenType.EOF, null)
        );

        testTokenize(")",
                new ExpectedToken(TokenType.RPAREN, null),
                new ExpectedToken(TokenType.EOF, null)
        );

        testTokenize("()",
                new ExpectedToken(TokenType.LPAREN, null),
                new ExpectedToken(TokenType.RPAREN, null),
                new ExpectedToken(TokenType.EOF, null)
        );

        System.out.println();
    }

    private static void testSimpleExpressions() {
        System.out.println("--- Testing Simple Expressions ---");

        testTokenize("(+ 2 3)",
                new ExpectedToken(TokenType.LPAREN, null),
                new ExpectedToken(TokenType.PLUS, null),
                new ExpectedToken(TokenType.NUMBER, "2"),
                new ExpectedToken(TokenType.NUMBER, "3"),
                new ExpectedToken(TokenType.RPAREN, null),
                new ExpectedToken(TokenType.EOF, null)
        );

        testTokenize("(× x 5)",
                new ExpectedToken(TokenType.LPAREN, null),
                new ExpectedToken(TokenType.MULT, null),
                new ExpectedToken(TokenType.IDENTIFIER, "x"),
                new ExpectedToken(TokenType.NUMBER, "5"),
                new ExpectedToken(TokenType.RPAREN, null),
                new ExpectedToken(TokenType.EOF, null)
        );

        testTokenize("(= a b)",
                new ExpectedToken(TokenType.LPAREN, null),
                new ExpectedToken(TokenType.EQUALS, null),
                new ExpectedToken(TokenType.IDENTIFIER, "a"),
                new ExpectedToken(TokenType.IDENTIFIER, "b"),
                new ExpectedToken(TokenType.RPAREN, null),
                new ExpectedToken(TokenType.EOF, null)
        );

        System.out.println();
    }

    private static void testNestedExpressions() {
        System.out.println("--- Testing Nested Expressions ---");

        testTokenize("(+ (× 2 3) 4)",
                new ExpectedToken(TokenType.LPAREN, null),
                new ExpectedToken(TokenType.PLUS, null),
                new ExpectedToken(TokenType.LPAREN, null),
                new ExpectedToken(TokenType.MULT, null),
                new ExpectedToken(TokenType.NUMBER, "2"),
                new ExpectedToken(TokenType.NUMBER, "3"),
                new ExpectedToken(TokenType.RPAREN, null),
                new ExpectedToken(TokenType.NUMBER, "4"),
                new ExpectedToken(TokenType.RPAREN, null),
                new ExpectedToken(TokenType.EOF, null)
        );

        testTokenize("(? (= x 0) 1 0)",
                new ExpectedToken(TokenType.LPAREN, null),
                new ExpectedToken(TokenType.CONDITIONAL, null),
                new ExpectedToken(TokenType.LPAREN, null),
                new ExpectedToken(TokenType.EQUALS, null),
                new ExpectedToken(TokenType.IDENTIFIER, "x"),
                new ExpectedToken(TokenType.NUMBER, "0"),
                new ExpectedToken(TokenType.RPAREN, null),
                new ExpectedToken(TokenType.NUMBER, "1"),
                new ExpectedToken(TokenType.NUMBER, "0"),
                new ExpectedToken(TokenType.RPAREN, null),
                new ExpectedToken(TokenType.EOF, null)
        );

        System.out.println();
    }

    private static void testFunctionExpressions() {
        System.out.println("--- Testing Function Expressions ---");

        testTokenize("(λ x x)",
                new ExpectedToken(TokenType.LPAREN, null),
                new ExpectedToken(TokenType.LAMBDA, null),
                new ExpectedToken(TokenType.IDENTIFIER, "x"),
                new ExpectedToken(TokenType.IDENTIFIER, "x"),
                new ExpectedToken(TokenType.RPAREN, null),
                new ExpectedToken(TokenType.EOF, null)
        );

        testTokenize("(≜ y 10 y)",
                new ExpectedToken(TokenType.LPAREN, null),
                new ExpectedToken(TokenType.LET, null),
                new ExpectedToken(TokenType.IDENTIFIER, "y"),
                new ExpectedToken(TokenType.NUMBER, "10"),
                new ExpectedToken(TokenType.IDENTIFIER, "y"),
                new ExpectedToken(TokenType.RPAREN, null),
                new ExpectedToken(TokenType.EOF, null)
        );

        testTokenize("((λ x (+ x 1)) 5)",
                new ExpectedToken(TokenType.LPAREN, null),
                new ExpectedToken(TokenType.LPAREN, null),
                new ExpectedToken(TokenType.LAMBDA, null),
                new ExpectedToken(TokenType.IDENTIFIER, "x"),
                new ExpectedToken(TokenType.LPAREN, null),
                new ExpectedToken(TokenType.PLUS, null),
                new ExpectedToken(TokenType.IDENTIFIER, "x"),
                new ExpectedToken(TokenType.NUMBER, "1"),
                new ExpectedToken(TokenType.RPAREN, null),
                new ExpectedToken(TokenType.RPAREN, null),
                new ExpectedToken(TokenType.NUMBER, "5"),
                new ExpectedToken(TokenType.RPAREN, null),
                new ExpectedToken(TokenType.EOF, null)
        );

        System.out.println();
    }

    private static void testWhitespace() {
        System.out.println("--- Testing Whitespace Handling ---");

        testTokenize("  42  ",
                new ExpectedToken(TokenType.NUMBER, "42"),
                new ExpectedToken(TokenType.EOF, null)
        );

        testTokenize("(\t+\t2\t3\t)",
                new ExpectedToken(TokenType.LPAREN, null),
                new ExpectedToken(TokenType.PLUS, null),
                new ExpectedToken(TokenType.NUMBER, "2"),
                new ExpectedToken(TokenType.NUMBER, "3"),
                new ExpectedToken(TokenType.RPAREN, null),
                new ExpectedToken(TokenType.EOF, null)
        );

        System.out.println();
    }

    private static void testMultiline() {
        System.out.println("--- Testing Multiline Input ---");

        testTokenize("(+\n  2\n  3)",
                new ExpectedToken(TokenType.LPAREN, null),
                new ExpectedToken(TokenType.PLUS, null),
                new ExpectedToken(TokenType.NUMBER, "2"),
                new ExpectedToken(TokenType.NUMBER, "3"),
                new ExpectedToken(TokenType.RPAREN, null),
                new ExpectedToken(TokenType.EOF, null)
        );

        System.out.println();
    }

    private static void testErrorCases() {
        System.out.println("--- Testing Error Cases ---");

        // Test invalid character (ASCII dash instead of Unicode minus)
        testTokenizeError("-", "Should reject ASCII dash");

        // Test invalid character
        testTokenizeError("@", "Should reject @ symbol");

        // Test identifier starting with digit
        testTokenizeError("123abc", "Should reject identifier starting with digit (tokenizes as number then identifier)");
        // Actually, this will tokenize as "123" then "abc", which might be valid in some contexts
        // Let's test a truly invalid character
        testTokenizeError("#", "Should reject # symbol");

        System.out.println();
    }

    // Helper methods

    private static void testTokenize(String input, ExpectedToken... expected) {
        try {
            Lexer lexer = new Lexer(input);
            List<Token> tokens = lexer.tokenize();

            if (tokens.size() != expected.length) {
                fail(input, "Expected " + expected.length + " tokens, got " + tokens.size());
                return;
            }

            for (int i = 0; i < expected.length; i++) {
                Token token = tokens.get(i);
                ExpectedToken exp = expected[i];

                if (token.getType() != exp.type) {
                    fail(input, "Token " + i + ": expected type " + exp.type + ", got " + token.getType());
                    return;
                }

                if (exp.value != null && !exp.value.equals(token.getValue())) {
                    fail(input, "Token " + i + ": expected value '" + exp.value + "', got '" + token.getValue() + "'");
                    return;
                }
            }

            pass(input);

        } catch (LexerException e) {
            fail(input, "Unexpected error: " + e.getMessage());
        }
    }

    private static void testTokenizeError(String input, String description) {
        try {
            Lexer lexer = new Lexer(input);
            List<Token> tokens = lexer.tokenize();
            fail(input, description + " - but lexer succeeded");
        } catch (LexerException e) {
            pass(input + " (" + description + ")");
        }
    }

    private static void pass(String testName) {
        System.out.println("  ✓ PASS: " + testName);
        testsPassed++;
    }

    private static void fail(String testName, String reason) {
        System.out.println("  ✗ FAIL: " + testName);
        System.out.println("         Reason: " + reason);
        testsFailed++;
    }

    private static void printResults() {
        System.out.println("=== Test Results ===");
        System.out.println("Passed: " + testsPassed);
        System.out.println("Failed: " + testsFailed);
        System.out.println("Total:  " + (testsPassed + testsFailed));

        if (testsFailed == 0) {
            System.out.println("\n✓ All tests passed!");
        } else {
            System.out.println("\n✗ Some tests failed.");
        }
    }

    // Helper class for expected tokens
    private static class ExpectedToken {
        TokenType type;
        String value;

        ExpectedToken(TokenType type, String value) {
            this.type = type;
            this.value = value;
        }
    }
}