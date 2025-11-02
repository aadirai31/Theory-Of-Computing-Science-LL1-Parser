# MiniLisp LL(1) Parser - Complete Implementation

## Overview

This is a complete LL(1) parser implementation for the MiniLisp language as specified in the Theory of Computing Science assignment. It includes both lexer (B.1) and parser (B.2) phases, with parse tree output (B.3).

## Project Structure

```
src/
├── Token.java           - Token class representing a single token
├── TokenType.java       - Enumeration of all token types
├── Lexer.java           - Lexer implementation (tokenizer)
├── LexerException.java  - Exception class for lexer errors
├── Parser.java          - LL(1) parser implementation
├── ParseException.java  - Exception class for parser errors
├── Main.java            - Main entry point with interactive mode
├── LexerTest.java       - Comprehensive lexer test suite
└── ParserTest.java      - Comprehensive parser test suite
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

## Running the Parser

### Interactive Mode
```bash
java Main
```

This starts an interactive REPL where you can enter MiniLisp expressions:
```
> (+ 2 3)
Input: (+ 2 3)

--- Tokens ---
Token(LPAREN, 1:1)
Token(PLUS, 1:2)
Token(NUMBER, '2', 1:4)
Token(NUMBER, '3', 1:6)
Token(RPAREN, 1:7)

--- Parse Tree ---
['PLUS', 2, 3]

> exit
```

### Single Expression Mode
```bash
java Main "(+ 2 3)"
```

### Running Tests
```bash
java LexerTest    # Run lexer tests
java ParserTest   # Run parser tests
```

The parser test suite includes 24+ test cases covering all grammar productions.

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

## Features

### Lexer Features

1. **Unicode Support**: Correctly handles all required Unicode characters
2. **Whitespace Handling**: Skips spaces, tabs, carriage returns, and newlines
3. **Error Reporting**: Reports unrecognized characters with line and column numbers
4. **Position Tracking**: Each token records its line and column position

### Parser Features

1. **LL(1) Parsing**: Implements table-driven LL(1) parsing algorithm
2. **Parse Table**: Directly follows the computed parse table from `parsetable.md`
3. **Parse Tree Generation**: Builds nested list representation (Part B.3)
4. **Error Handling**: Clear error messages with line and column information

## Implementation Details

### Lexer Algorithm

The lexer uses a simple character-by-character scanning approach:

1. Skip whitespace characters
2. Peek at the next character without consuming it
3. Determine token type (single-character vs multi-character)
4. Scan and consume characters to form the token
5. Create token object with type, value, and position
6. Repeat until end of input
7. Add EOF token to signal end

### Parser Algorithm

The parser implements the standard **table-driven LL(1) parsing algorithm**:

1. **Parse Table Initialization**:
   - Explicit `Map<String, Production>` mapping `(NonTerminal, Terminal)` → `Production`
   - Contains all 12 productions from `parsetable.md`

2. **Stack-Based Parsing Loop**:
   ```
   Initialize: push EOF and start symbol onto parse stack
   While parse stack is not empty:
     Pop top symbol from parse stack
     If symbol is a terminal:
       Match with current input token
       Consume token and add value to semantic stack
     Else if symbol is a non-terminal:
       Look up production in parse table using (non-terminal, lookahead)
       Push production RHS symbols onto parse stack in reverse order
     Else if symbol is a semantic action:
       Execute action (build parse tree node from semantic stack)
   ```

3. **Parse Tree Construction**:
   - Uses a **semantic stack** alongside the parse stack
   - Terminals (NUMBER, IDENTIFIER) push values to semantic stack
   - Semantic actions pop values and build tree nodes:
     - Binary ops: `['PLUS', left, right]`
     - Lambda: `['LAMBDA', param, body]`
     - Function app: `[func, arg1, arg2, ...]`

### Parse Table Usage

The parser directly implements the LL(1) parse table from `parsetable.md`:

| Non-Terminal | Lookahead | Production |
|--------------|-----------|------------|
| `<program>` | NUMBER, IDENTIFIER, LPAREN | 1: `<program> → <expr>` |
| `<expr>` | NUMBER | 2: `<expr> → NUMBER` |
| `<expr>` | IDENTIFIER | 3: `<expr> → IDENTIFIER` |
| `<expr>` | LPAREN | 4: `<expr> → '(' <paren-expr> ')'` |
| `<paren-expr>` | PLUS | 5: `<paren-expr> → '+' <expr> <expr>` |
| `<paren-expr>` | MULT | 6: `<paren-expr> → '×' <expr> <expr>` |
| `<paren-expr>` | EQUALS | 7: `<paren-expr> → '=' <expr> <expr>` |
| `<paren-expr>` | MINUS | 8: `<paren-expr> → '−' <expr> <expr>` |
| `<paren-expr>` | CONDITIONAL | 9: `<paren-expr> → '?' <expr> <expr> <expr>` |
| `<paren-expr>` | LAMBDA | 10: `<paren-expr> → 'λ' IDENTIFIER <expr>` |
| `<paren-expr>` | LET | 11: `<paren-expr> → '≜' IDENTIFIER <expr> <expr>` |
| `<paren-expr>` | NUMBER, IDENTIFIER, LPAREN | 12: `<paren-expr> → <expr> <expr>*` |

## Testing

### Lexer Tests (`LexerTest.java`)

Comprehensive tests for the lexer:
- Basic tokens (numbers, identifiers, operators)
- Simple expressions
- Nested expressions
- Function expressions (lambda, let)
- Whitespace handling
- Multiline expressions
- Error cases

Run with:
```bash
java LexerTest
```

### Parser Tests (`ParserTest.java`)

Comprehensive tests for the parser (24+ test cases):

1. **Basic Expressions**:
   - `42` → `42`
   - `x` → `'x'`
   - `(+ 2 3)` → `['PLUS', 2, 3]`
   - `(× x 5)` → `['MULT', 'x', 5]`

2. **Nested Expressions**:
   - `(+ (× 2 3) 4)` → `['PLUS', ['MULT', 2, 3], 4]`
   - `(? (= x 0) 1 0)` → `['CONDITIONAL', ['EQUALS', 'x', 0], 1, 0]`

3. **Function Expressions**:
   - `(λ x x)` → `['LAMBDA', 'x', 'x']`
   - `(≜ y 10 y)` → `['LET', 'y', 10, 'y']`
   - `((λ x (+ x 1)) 5)` → `[['LAMBDA', 'x', ['PLUS', 'x', 1]], 5]`
   - `(f 1 2 3)` → `['f', 1, 2, 3]` (function application)

4. **Error Cases**:
   - Missing closing parenthesis
   - Unmatched parenthesis
   - Invalid expressions
   - Missing arguments

Run with:
```bash
java ParserTest
```

Expected output:
```
=== MiniLisp Parser Test Suite ===

--- Testing Basic Expressions ---
  ✓ PASS: 42
  ✓ PASS: x
  ✓ PASS: (+ 2 3)
...

=== Test Results ===
Passed: 24
Failed: 0
Total:  24

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

## Implementation Status

### Completed

✅ **Part B.1: Lexer** - Complete lexical analyzer with Unicode support
✅ **Part B.2: Parser** - LL(1) parser following the computed parse table
✅ **Part B.3: Parse Tree Output** - Nested list representation
✅ **Part C.1: Test Cases** - Comprehensive test suites for lexer and parser
✅ **Part C.2: Error Handling** - Clear error messages with position information

### Assignment Deliverables

This implementation satisfies the following requirements:

1. **Lexer (15 points)**: Tokenizes all required tokens with mandatory Unicode support
2. **Parser (30 points)**: Table-driven LL(1) algorithm using computed parse table
3. **Testing (15 points)**: Comprehensive test design with coverage of all productions
4. **Parse Tree Output (B.3)**: Simple nested structure format as specified

### What's Included

- ✅ Full lexer implementation with Unicode support
- ✅ LL(1) parser based on parse table from `parsetable.md`
- ✅ Parse tree generation in nested list format
- ✅ Interactive REPL mode
- ✅ Comprehensive test suites (24+ parser tests, 20+ lexer tests)
- ✅ Error handling with line/column information
- ✅ Clean, readable code with documentation

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

This implementation:
- ✅ Follows the specification exactly
- ✅ Supports all required Unicode characters
- ✅ Implements LL(1) parsing following the computed parse table
- ✅ Generates parse trees in the specified nested list format
- ✅ Includes comprehensive error handling
- ✅ Has detailed position tracking
- ✅ Comes with extensive test suites (44+ total tests)
- ✅ Is well-documented and maintainable

The code is designed to be clear and educational, prioritizing readability and correctness over performance optimization (as suggested in the assignment requirements).

### Parser Design Decisions

1. **Table-Driven Approach**: Uses explicit parse table with stack-based algorithm (as required by the specification)
2. **Explicit Parse Table**: `Map<String, Production>` stores all (NonTerminal, Terminal) → Production mappings
3. **Two-Stack Architecture**:
   - **Parse Stack**: Contains symbols to match/expand (terminals, non-terminals, semantic actions)
   - **Semantic Stack**: Accumulates parse tree fragments during parsing
4. **Parse Tree Format**: Returns nested `List<Object>` structures matching the specification format
5. **Error Messages**: Provides line and column information for all errors
6. **Semantic Actions**: Embedded in production RHS to build tree nodes at the right time
7. **Function Application**: Special handling for `<expr>*` (zero or more expressions) using a helper method