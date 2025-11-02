# MiniLisp LL(1) Parser - Lexer Implementation

## Overview

This is the lexer (tokenizer) implementation for the MiniLisp language as specified in the Theory of Computing Science assignment.

## Project Structure

```
src/
├── Token.java           - Token class representing a single token
├── TokenType.java       - Enumeration of all token types
├── Lexer.java          - Main lexer implementation
├── LexerException.java - Exception class for lexer errors
├── Main.java           - Main entry point with interactive mode
└── LexerTest.java      - Comprehensive test suite
```

## Token Types

The lexer recognizes the following tokens according to the specification:

| Token Name | Pattern | Unicode | Example |
|------------|---------|---------|---------|
| NUMBER | `[0-9]+` | U+0030-U+0039 | `42`, `0`, `999` |
| IDENTIFIER | `[a-zA-Z][a-zA-Z0-9]*` | U+0061-U+007A, U+0041-U+005A | `x`, `foo`, `var123` |
| PLUS | `+` | U+002B | `+` |
| MINUS | `−` | **U+2212** (NOT `-`) | `−` |
| MULT | `×` | U+00D7 | `×` |
| EQUALS | `=` | U+003D | `=` |
| CONDITIONAL | `?` | U+003F | `?` |
| LAMBDA | `λ` | U+03BB | `λ` |
| LET | `≜` | U+225C | `≜` |
| LPAREN | `(` | U+0028 | `(` |
| RPAREN | `)` | U+0029 | `)` |
| EOF | End of input | - | - |

### CRITICAL: Unicode Requirements

**IMPORTANT:** The specification requires specific Unicode characters:

- **MINUS**: Must use `−` (U+2212), NOT the ASCII dash `-` (U+002D)
- **MULT**: Must use `×` (U+00D7), not the letter 'x'
- **LAMBDA**: Must use `λ` (U+03BB)
- **LET**: Must use `≜` (U+225C)

## Compilation

### Windows (Command Prompt)
```cmd
cd src
javac *.java
```

### Linux/Mac
```bash
cd src
javac *.java
```

## Running the Lexer

### Interactive Mode
```bash
java Main
```

This starts an interactive REPL where you can enter MiniLisp expressions:
```
> (+ 2 3)
> (λ x x)
> exit
```

### Single Expression Mode
```bash
java Main "(+ 2 3)"
```

### Running Tests
```bash
java LexerTest
```

This runs the comprehensive test suite with 20+ test cases.

## Example Usage

### Basic Expressions
```lisp
42              # NUMBER: 42
x               # IDENTIFIER: x
(+ 2 3)         # LPAREN, PLUS, NUMBER:2, NUMBER:3, RPAREN
```

### Nested Expressions
```lisp
(+ (× 2 3) 4)   # Addition with nested multiplication
(? (= x 0) 1 0) # Conditional expression
```

### Function Expressions
```lisp
(λ x x)                # Lambda (identity function)
(≜ y 10 y)            # Let binding
((λ x (+ x 1)) 5)     # Function application
```

## Lexer Features

### 1. Unicode Support
- Correctly handles all required Unicode characters
- Distinguishes between Unicode minus (−) and ASCII dash (-)

### 2. Whitespace Handling
- Skips spaces, tabs, carriage returns, and newlines
- Maintains line and column tracking for error reporting

### 3. Error Reporting
- Reports unrecognized characters with line and column numbers
- Clear error messages for debugging

### 4. Position Tracking
- Each token records its line and column position
- Useful for error reporting in later parser stages

## Implementation Details

### Lexer Algorithm

The lexer uses a simple character-by-character scanning approach:

1. **Skip Whitespace**: Consume and ignore whitespace characters
2. **Peek Current Character**: Look at the next character without consuming it
3. **Determine Token Type**:
    - Single-character tokens: operators, delimiters
    - Multi-character tokens: numbers, identifiers
4. **Scan Token**: Consume characters until token is complete
5. **Create Token**: Build token object with type, value, and position
6. **Repeat**: Continue until end of input
7. **Add EOF Token**: Signal end of input

### Token Class

```java
public class Token {
    private final TokenType type;    // Token type (NUMBER, IDENTIFIER, etc.)
    private final String value;      // Optional value (for NUMBER and IDENTIFIER)
    private final int line;          // Line number in source
    private final int column;        // Column number in source
}
```

### Key Methods

- `tokenize()`: Main entry point, returns list of all tokens
- `nextToken()`: Scans and returns the next single token
- `scanNumber()`: Scans a number token
- `scanIdentifier()`: Scans an identifier token
- `skipWhitespace()`: Skips whitespace and updates position

## Testing

The `LexerTest.java` file contains comprehensive tests:

- **Basic Tokens**: Individual token types
- **Simple Expressions**: Basic operator expressions
- **Nested Expressions**: Multi-level nesting
- **Function Expressions**: Lambda and let constructs
- **Whitespace**: Various whitespace patterns
- **Multiline**: Expressions spanning multiple lines
- **Error Cases**: Invalid input handling

Run tests with:
```bash
java LexerTest
```

Expected output:
```
=== MiniLisp Lexer Test Suite ===

--- Testing Numbers ---
  ✓ PASS: 42
  ✓ PASS: 0
  ✓ PASS: 999
...

=== Test Results ===
Passed: 20+
Failed: 0
Total:  20+

✓ All tests passed!
```

## Common Issues and Solutions

### Issue 1: Wrong Minus Character
**Problem**: Using ASCII dash `-` instead of Unicode minus `−`
**Solution**: Copy the correct character from the specification or use Unicode escape `\u2212`

### Issue 2: Compilation Errors on Unicode
**Problem**: Source file encoding doesn't support Unicode
**Solution**:
- Save files as UTF-8
- Use javac with `-encoding UTF-8` flag: `javac -encoding UTF-8 *.java`

### Issue 3: Cannot Type Unicode Characters
**Problem**: Keyboard doesn't have Unicode characters
**Solution**:
- Windows: Use Character Map or Alt codes
- Mac: Use Character Viewer (Ctrl+Cmd+Space)
- Linux: Use Compose key or Unicode input
- Or copy from specification/web

## Next Steps

After implementing the lexer, the next steps in the assignment are:

1. **Parser Implementation** (Part B.2):
    - Implement table-driven LL(1) parser
    - Use the parse table computed in Part A
    - Build parse stack and process tokens

2. **Parse Tree Output** (Part B.3):
    - Generate nested list representation
    - Convert parse tree to JSON format
    - Example: `(+ 2 3)` → `['PLUS', 2, 3]`

3. **Testing and Validation** (Part C):
    - Create comprehensive test cases
    - Implement error handling
    - Generate test output files

## Git Workflow

To work on this in your repository:

```bash
# Navigate to your repository
cd "C:\git\Theory-Of-Computing-Science-LL1-Parser"

# Create and checkout a new branch
git checkout -b lexer-implementation

# Copy the source files to your repository
# (Copy all files from src/ to your repository)

# Add and commit
git add .
git commit -m "Implement lexer for MiniLisp language"

# Push to remote
git push -u origin lexer-implementation
```

## Resources

- **Assignment Specification**: See `spec.md`
- **FIRST/FOLLOW Sets**: See `FIRST_sets_analysis.md` and `FOLLOW_sets_analysis.md`
- **Parse Table**: See `LL1_parse_table.md`
- **Conflict Resolution**: See `conflict_resolution_analysis.md`

## Author Notes

This lexer implementation:
- ✓ Follows the specification exactly
- ✓ Supports all required Unicode characters
- ✓ Includes comprehensive error handling
- ✓ Has detailed position tracking
- ✓ Comes with extensive test suite
- ✓ Is well-documented and maintainable

The code is designed to be clear and educational, prioritizing readability and correctness over performance optimization (as suggested in the assignment requirements).