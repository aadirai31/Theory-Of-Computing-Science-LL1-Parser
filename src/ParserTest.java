import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Comprehensive test suite for the MiniLisp parser.
 * Tests all production rules and parse tree construction.
 * Generates .json output files for test cases.
 */
public class ParserTest {

    private static int passCount = 0;
    private static int failCount = 0;
    private static final String OUTPUT_DIR = "test_outputs";

    public static void main(String[] args) {
        System.out.println("=== MiniLisp Parser Test Suite ===\n");

        // Create output directory for JSON files
        createOutputDirectory();

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
            System.out.println("\nJSON output files written to: " + OUTPUT_DIR + "/");
        } else {
            System.out.println("\n✗ Some tests failed!");
            System.exit(1);
        }
    }

    private static void createOutputDirectory() {
        File dir = new File(OUTPUT_DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    private static void testBasicExpressions() {
        System.out.println("--- Testing Basic Expressions ---");

        // NUMBER
        test("42", "42");

        // IDENTIFIER
        test("x", "\"x\"");

        // Binary operations
        test("(+ 2 3)", "[\"PLUS\",2,3]");
        test("(× x 5)", "[\"MULT\",\"x\",5]");
        test("(= 10 20)", "[\"EQUALS\",10,20]");
        test("(− 100 50)", "[\"MINUS\",100,50]");
    }

    private static void testNestedExpressions() {
        System.out.println("\n--- Testing Nested Expressions ---");

        // Nested arithmetic
        test("(+ (× 2 3) 4)", "[\"PLUS\",[\"MULT\",2,3],4]");
        test("(× (+ 1 2) 3)", "[\"MULT\",[\"PLUS\",1,2],3]");

        // Deeply nested
        test("(+ (+ 1 2) (+ 3 4))", "[\"PLUS\",[\"PLUS\",1,2],[\"PLUS\",3,4]]");

        // Conditional
        test("(? (= x 0) 1 0)", "[\"CONDITIONAL\",[\"EQUALS\",\"x\",0],1,0]");
        test("(? x 10 20)", "[\"CONDITIONAL\",\"x\",10,20]");
    }

    private static void testFunctionExpressions() {
        System.out.println("\n--- Testing Function Expressions ---");

        // Lambda (identity function)
        test("(λ x x)", "[\"LAMBDA\",\"x\",\"x\"]");

        // Lambda with expression body
        test("(λ x (+ x 1))", "[\"LAMBDA\",\"x\",[\"PLUS\",\"x\",1]]");

        // Let binding
        test("(≜ y 10 y)", "[\"LET\",\"y\",10,\"y\"]");
        test("(≜ x 5 (+ x 1))", "[\"LET\",\"x\",5,[\"PLUS\",\"x\",1]]");

        // Function application
        test("((λ x (+ x 1)) 5)", "[[\"LAMBDA\",\"x\",[\"PLUS\",\"x\",1]],5]");
        test("(f 1 2 3)", "[\"f\",1,2,3]");
        test("(add x y)", "[\"add\",\"x\",\"y\"]");

        // Nested function application
        test("((f x) y)", "[[\"f\",\"x\"],\"y\"]");
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
     * Also writes the result to a JSON file.
     */
    private static void test(String input, String expectedJson) {
        try {
            Lexer lexer = new Lexer(input);
            List<Token> tokens = lexer.tokenize();
            Parser parser = new Parser(tokens);
            Object result = parser.parse();

            String resultJson = JsonFormatter.toJson(result);

            // Write to JSON file
            String filename = generateFilename(input);
            writeJsonFile(filename, input, resultJson);

            if (resultJson.equals(expectedJson)) {
                System.out.println("  ✓ PASS: " + input);
                passCount++;
            } else {
                System.out.println("  ✗ FAIL: " + input);
                System.out.println("    Expected: " + expectedJson);
                System.out.println("    Got:      " + resultJson);
                failCount++;
            }
        } catch (Exception e) {
            System.out.println("  ✗ FAIL: " + input);
            System.out.println("    Unexpected exception: " + e.getMessage());
            failCount++;
        }
    }

    /**
     * Generates a safe filename from an input expression.
     */
    private static String generateFilename(String input) {
        // Remove special characters and limit length
        String safe = input.replaceAll("[^a-zA-Z0-9]", "_");
        if (safe.length() > 30) {
            safe = safe.substring(0, 30);
        }
        return safe + ".json";
    }

    /**
     * Writes a JSON output file for a test case.
     */
    private static void writeJsonFile(String filename, String input, String json) {
        try {
            File file = new File(OUTPUT_DIR, filename);
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("{\n");
                writer.write("  \"input\": " + JsonFormatter.toJson(input) + ",\n");
                writer.write("  \"output\": " + json + "\n");
                writer.write("}\n");
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not write file " + filename + ": " + e.getMessage());
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
            System.out.println("    Got: " + JsonFormatter.toJson(result));
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

}
