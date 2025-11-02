import java.util.List;
import java.util.Scanner;

/**
 * Main entry point for the MiniLisp LL(1) Parser.
 *
 * This implementation includes:
 * - Lexer: Tokenizes MiniLisp source code according to specification
 * - Parser: Table-driven LL(1) parser (to be implemented)
 * - Parse Tree Generator: Outputs parse trees in nested list format
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=== MiniLisp LL(1) Parser ===\n");

        if (args.length > 0) {
            if (args[0].equals("--test")) {
                // Run test suite
                runLexerTests();
            } else {
                // Parse from command-line argument
                String input = args[0];
                processInput(input);
            }
        } else {
            // Interactive mode
            runInteractiveMode();
        }
    }

    /**
     * Runs the parser in interactive mode, allowing multiple inputs.
     */
    private static void runInteractiveMode() {
        System.out.println("Interactive Mode - Enter MiniLisp expressions");
        System.out.println("Commands: 'exit' to quit, 'help' for examples\n");

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine().trim();

                if (input.equalsIgnoreCase("exit")) {
                    System.out.println("Goodbye!");
                    break;
                }

                if (input.equalsIgnoreCase("help")) {
                    printHelp();
                    continue;
                }

                if (input.isEmpty()) {
                    continue;
                }

                processInput(input);
                System.out.println();
            }
        }
    }

    /**
     * Processes a single input string through the lexer and parser.
     */
    private static void processInput(String input) {
        try {
            // Step 1: Lexical Analysis
            System.out.println("Input: " + input);
            Lexer lexer = new Lexer(input);
            List<Token> tokens = lexer.tokenize();

            System.out.println("\n--- Tokens ---");
            for (Token token : tokens) {
                if (token.getType() != TokenType.EOF) {
                    System.out.println(token);
                }
            }

            // Step 2: Parsing
            Parser parser = new Parser(tokens);
            Object parseTree = parser.parse();

            System.out.println("\n--- Parse Tree (JSON) ---");
            System.out.println(JsonFormatter.toPrettyJson(parseTree));

        } catch (LexerException e) {
            System.err.println("Lexer Error: " + e.getMessage());
        } catch (ParseException e) {
            System.err.println("Parse Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Prints help information with example expressions.
     */
    private static void printHelp() {
        System.out.println("\n=== MiniLisp Examples ===\n");

        System.out.println("Basic expressions:");
        System.out.println("  42              - number literal");
        System.out.println("  x               - identifier");
        System.out.println("  (+ 2 3)         - addition");
        System.out.println("  (× x 5)         - multiplication");
        System.out.println();

        System.out.println("Nested expressions:");
        System.out.println("  (+ (× 2 3) 4)   - nested arithmetic");
        System.out.println("  (? (= x 0) 1 0) - conditional");
        System.out.println();

        System.out.println("Function expressions:");
        System.out.println("  (λ x x)         - lambda (identity function)");
        System.out.println("  (≜ y 10 y)      - let binding");
        System.out.println("  ((λ x (+ x 1)) 5) - function application");
        System.out.println();

        System.out.println("IMPORTANT: Unicode symbols");
        System.out.println("  Use × (U+00D7) for multiplication, not 'x'");
        System.out.println("  Use − (U+2212) for minus, not '-' (dash)");
        System.out.println("  Use λ (U+03BB) for lambda");
        System.out.println("  Use ≜ (U+225C) for let");
        System.out.println();
    }

    /**
     * Simple test suite for the lexer.
     */
    public static void runLexerTests() {
        System.out.println("=== Running Lexer Tests ===\n");

        String[] testCases = {
                "42",
                "x",
                "(+ 2 3)",
                "(× x 5)",
                "(+ (× 2 3) 4)",
                "(? (= x 0) 1 0)",
                "(λ x x)",
                "(≜ y 10 y)",
                "((λ x (+ x 1)) 5)"
        };

        int passed = 0;
        int failed = 0;

        for (String testCase : testCases) {
            try {
                System.out.println("Test: " + testCase);
                Lexer lexer = new Lexer(testCase);
                List<Token> tokens = lexer.tokenize();
                System.out.println("  Tokens: " + tokens.size() + " (including EOF)");
                passed++;
            } catch (Exception e) {
                System.out.println("  FAILED: " + e.getMessage());
                failed++;
            }
            System.out.println();
        }

        System.out.println("=== Test Results ===");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
    }
}