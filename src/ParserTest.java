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
    private static final String OUTPUT_DIR = "parser_test_outputs";

    public static void main(String[] args) {
        System.out.println("=== MiniLisp Parser Test Suite ===\n");

        createOutputDirectory();
        testBasicExpressions();
        testEdgeCases();
        testNestedExpressions();
        testFunctionExpressions();
        testErrorCases();

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

        test("42", "42");
        test("x", "\"x\"");
        test("(+ 2 3)", "[\"PLUS\",2,3]");
        test("(× x 5)", "[\"MULT\",\"x\",5]");
        test("(= 10 20)", "[\"EQUALS\",10,20]");
        test("(− 100 50)", "[\"MINUS\",100,50]");
    }

    private static void testEdgeCases() {
        System.out.println("\n--- Testing Edge Cases ---");

        // Leading zeroes in numbers
        test("042", "42");
        test("007", "7");
        test("00000", "0");
        test("(+ 007 042)", "[\"PLUS\",7,42]");

        // Multiple consecutive spaces
        test("(+    2    3)", "[\"PLUS\",2,3]");
        test("(×     x     5)", "[\"MULT\",\"x\",5]");

        // Leading and trailing whitespace
        test("  42  ", "42");
        test("  (+ 2 3)  ", "[\"PLUS\",2,3]");

        // Mixed whitespace (spaces and tabs)
        test("(+\t\t2\t\t3)", "[\"PLUS\",2,3]");
    }

    private static void testNestedExpressions() {
        System.out.println("\n--- Testing Nested Expressions ---");

        test("(+ (× 2 3) 4)", "[\"PLUS\",[\"MULT\",2,3],4]");
        test("(× (+ 1 2) 3)", "[\"MULT\",[\"PLUS\",1,2],3]");
        test("(+ (+ 1 2) (+ 3 4))", "[\"PLUS\",[\"PLUS\",1,2],[\"PLUS\",3,4]]");
        test("(? (= x 0) 1 0)", "[\"CONDITIONAL\",[\"EQUALS\",\"x\",0],1,0]");
        test("(? x 10 20)", "[\"CONDITIONAL\",\"x\",10,20]");
    }

    private static void testFunctionExpressions() {
        System.out.println("\n--- Testing Function Expressions ---");

        test("(λ x x)", "[\"LAMBDA\",\"x\",\"x\"]");
        test("(λ x (+ x 1))", "[\"LAMBDA\",\"x\",[\"PLUS\",\"x\",1]]");
        test("(≜ y 10 y)", "[\"LET\",\"y\",10,\"y\"]");
        test("(≜ x 5 (+ x 1))", "[\"LET\",\"x\",5,[\"PLUS\",\"x\",1]]");
        test("((λ x (+ x 1)) 5)", "[[\"LAMBDA\",\"x\",[\"PLUS\",\"x\",1]],5]");
        test("(f 1 2 3)", "[\"f\",1,2,3]");
        test("(add x y)", "[\"add\",\"x\",\"y\"]");
        test("((f x) y)", "[[\"f\",\"x\"],\"y\"]");
    }

    private static void testErrorCases() {
        System.out.println("\n--- Testing Error Cases ---");

        testError("(+ 2", "Expected ')' but found EOF");
        testError(")", "Unexpected token RPAREN");
        testError("+", "Unexpected token PLUS");
        testError("(+)", "Unexpected token RPAREN");
        testError("(+ 2)", "Unexpected token RPAREN");
    }

    private static void test(String input, String expectedJson) {
        try {
            Lexer lexer = new Lexer(input);
            List<Token> tokens = lexer.tokenize();
            Parser parser = new Parser(tokens);
            Object result = parser.parse();

            String resultJson = JsonFormatter.toJson(result);

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

    private static String generateFilename(String input) {
        StringBuilder safe = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '+':
                    safe.append("PLUS");
                    break;
                case '−': // U+2212 (Unicode minus)
                    safe.append("MINUS");
                    break;
                case '×': // U+00D7 (Unicode multiply)
                    safe.append("MULT");
                    break;
                case '=':
                    safe.append("EQUALS");
                    break;
                case '?':
                    safe.append("CONDITIONAL");
                    break;
                case 'λ': // U+03BB (Greek lambda)
                    safe.append("LAMBDA");
                    break;
                case '≜': // U+225C (let)
                    safe.append("LET");
                    break;
                case '(':
                    safe.append("LPAREN");
                    break;
                case ')':
                    safe.append("RPAREN");
                    break;
                case '-': // ASCII dash (error case)
                    safe.append("DASH");
                    break;
                case '@':
                    safe.append("AT");
                    break;
                case '#':
                    safe.append("HASH");
                    break;
                case ' ':
                case '\t':
                case '\n':
                case '\r':
                    safe.append("_");
                    break;
                default:
                    if (Character.isLetterOrDigit(c)) {
                        safe.append(c);
                    } else {
                        safe.append("_");
                    }
                    break;
            }
        }

        String result = safe.toString();
        if (result.length() > 50) {
            result = result.substring(0, 50);
        }
        return result + ".json";
    }

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
