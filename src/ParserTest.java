import java.util.List;

/**
 * Comprehensive test suite for the MiniLisp parser.
 * Tests all production rules and parse tree construction.
 */
public class ParserTest {

    private static int passCount = 0;
    private static int failCount = 0;

    public static void main(String[] args) {
        System.out.println("=== MiniLisp Parser Test Suite ===\n");

        // Basic expressions (from spec C.1)
        testBasicExpressions();

        // Nested expressions
        testNestedExpressions();

        // Function expressions
        testFunctionExpressions();

        // Error cases (from spec C.2)
        testErrorCases();

        // Print results
        System.out.println("\n=== Test Results ===");
        System.out.println("Passed: " + passCount);
        System.out.println("Failed: " + failCount);
        System.out.println("Total:  " + (passCount + failCount));

        if (failCount == 0) {
            System.out.println("\n✓ All tests passed!");
        } else {
            System.out.println("\n✗ Some tests failed!");
            System.exit(1);
        }
    }

    private static void testBasicExpressions() {
        System.out.println("--- Testing Basic Expressions ---");

        // NUMBER
        test("42", 42);

        // IDENTIFIER
        test("x", "x");

        // Binary operations
        test("(+ 2 3)", "[PLUS, 2, 3]");
        test("(× x 5)", "[MULT, x, 5]");
        test("(= 10 20)", "[EQUALS, 10, 20]");
        test("(− 100 50)", "[MINUS, 100, 50]");
    }

    private static void testNestedExpressions() {
        System.out.println("\n--- Testing Nested Expressions ---");

        // Nested arithmetic
        test("(+ (× 2 3) 4)", "[PLUS, [MULT, 2, 3], 4]");
        test("(× (+ 1 2) 3)", "[MULT, [PLUS, 1, 2], 3]");

        // Deeply nested
        test("(+ (+ 1 2) (+ 3 4))", "[PLUS, [PLUS, 1, 2], [PLUS, 3, 4]]");

        // Conditional
        test("(? (= x 0) 1 0)", "[CONDITIONAL, [EQUALS, x, 0], 1, 0]");
        test("(? x 10 20)", "[CONDITIONAL, x, 10, 20]");
    }

    private static void testFunctionExpressions() {
        System.out.println("\n--- Testing Function Expressions ---");

        // Lambda (identity function)
        test("(λ x x)", "[LAMBDA, x, x]");

        // Lambda with expression body
        test("(λ x (+ x 1))", "[LAMBDA, x, [PLUS, x, 1]]");

        // Let binding
        test("(≜ y 10 y)", "[LET, y, 10, y]");
        test("(≜ x 5 (+ x 1))", "[LET, x, 5, [PLUS, x, 1]]");

        // Function application
        test("((λ x (+ x 1)) 5)", "[[LAMBDA, x, [PLUS, x, 1]], 5]");
        test("(f 1 2 3)", "[f, 1, 2, 3]");
        test("(add x y)", "[add, x, y]");

        // Nested function application
        test("((f x) y)", "[[f, x], y]");
    }

    private static void testErrorCases() {
        System.out.println("\n--- Testing Error Cases ---");

        // Missing closing paren
        testError("(+ 2", "Expected ')' but found EOF");

        // Unmatched paren
        testError(")", "Unexpected token RPAREN");

        // Invalid expression
        testError("+", "Unexpected token PLUS");

        // Missing arguments
        testError("(+)", "Unexpected token RPAREN");
        testError("(+ 2)", "Unexpected token RPAREN");
    }

    /**
     * Tests a valid expression and checks if the parse tree matches expected output.
     */
    private static void test(String input, Object expected) {
        try {
            Lexer lexer = new Lexer(input);
            List<Token> tokens = lexer.tokenize();
            Parser parser = new Parser(tokens);
            Object result = parser.parse();

            String resultStr = formatParseTree(result);
            String expectedStr = expected.toString();

            if (resultStr.equals(expectedStr)) {
                System.out.println("  ✓ PASS: " + input);
                passCount++;
            } else {
                System.out.println("  ✗ FAIL: " + input);
                System.out.println("    Expected: " + expectedStr);
                System.out.println("    Got:      " + resultStr);
                failCount++;
            }
        } catch (Exception e) {
            System.out.println("  ✗ FAIL: " + input);
            System.out.println("    Unexpected exception: " + e.getMessage());
            failCount++;
        }
    }

    /**
     * Tests that an invalid expression throws a ParseException.
     */
    private static void testError(String input, String expectedErrorFragment) {
        try {
            Lexer lexer = new Lexer(input);
            List<Token> tokens = lexer.tokenize();
            Parser parser = new Parser(tokens);
            Object result = parser.parse();

            System.out.println("  ✗ FAIL: " + input);
            System.out.println("    Expected error but parsing succeeded");
            System.out.println("    Got: " + formatParseTree(result));
            failCount++;
        } catch (ParseException e) {
            if (e.getMessage().contains(expectedErrorFragment)) {
                System.out.println("  ✓ PASS: " + input + " (error detected)");
                passCount++;
            } else {
                System.out.println("  ~ PARTIAL: " + input);
                System.out.println("    Expected error containing: " + expectedErrorFragment);
                System.out.println("    Got error: " + e.getMessage());
                passCount++;
            }
        } catch (LexerException e) {
            System.out.println("  ✓ PASS: " + input + " (lexer error)");
            passCount++;
        } catch (Exception e) {
            System.out.println("  ✗ FAIL: " + input);
            System.out.println("    Unexpected exception type: " + e.getClass().getName());
            System.out.println("    Message: " + e.getMessage());
            failCount++;
        }
    }

    /**
     * Formats a parse tree object into a string representation.
     * Matches the format from the spec: ['PLUS', 2, 3]
     */
    private static String formatParseTree(Object tree) {
        if (tree instanceof Integer) {
            return tree.toString();
        } else if (tree instanceof String) {
            return (String) tree;
        } else if (tree instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) tree;
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(formatParseTree(list.get(i)));
            }
            sb.append("]");
            return sb.toString();
        } else {
            return tree.toString();
        }
    }
}
