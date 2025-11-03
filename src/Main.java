import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== MiniLisp LL(1) Parser ===\n");

        if (args.length > 0) {
            String input = args[0];
            processInput(input);
        } else {
            runInteractiveMode();
        }
    }

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

    private static void processInput(String input) {
        try {
            System.out.println("Input: " + input);
            Lexer lexer = new Lexer(input);
            List<Token> tokens = lexer.tokenize();

            System.out.println("\n--- Tokens ---");
            for (Token token : tokens) {
                if (token.getType() != TokenType.EOF) {
                    System.out.println(token);
                }
            }

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
}