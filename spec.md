# Theoretical Background
Before beginning this assignment, familiarise yourself with these key concepts:

## LL(1) Parsing
LL(1) stands for "Left-to-right scan, Leftmost derivation, 1 token lookahead." An LL(1) grammar has the property that at any point during parsing, we can uniquely determine which production rule to apply by looking at only the current input token.

**Formal Definition**: A grammar G is LL(1) if and only if for every non-terminal A with productions A → α₁ | α₂ | ... | αₙ, the following conditions hold:

- FIRST(αᵢ) ∩ FIRST(αⱼ) = ∅ for all i ≠ j

- If ε ∈ FIRST(αᵢ) for some i, then FIRST(αⱼ) ∩ FOLLOW(A) = ∅ for all j ≠ i

## FIRST Sets
FIRST(α) is the set of all terminals that can begin strings derived from α.

Computation Rules:

- If α is a terminal, FIRST(α) = {α}

- If α is a non-terminal and α → ε is a production, then ε ∈ FIRST(α)

- If α is a non-terminal and α → Y₁Y₂...Yₖ is a production:

    - Add FIRST(Y₁) - {ε} to FIRST(α)

    - If ε ∈ FIRST(Y₁), add FIRST(Y₂) - {ε} to FIRST(α)

    - Continue until Yᵢ where ε ∉ FIRST(Yᵢ), or all symbols are processed

    - If ε ∈ FIRST(Yᵢ) for all i, then add ε to FIRST(α)

## FOLLOW Sets
FOLLOW(A) is the set of all terminals that can appear immediately after non-terminal A in some sentential form.

**Computation Rules:**

- Add $ (end-of-input) to FOLLOW(start symbol)

- If A → αBβ is a production, add FIRST(β) - {ε} to FOLLOW(B)

- If A → αBβ is a production and ε ∈ FIRST(β), add FOLLOW(A) to FOLLOW(B)

- If A → αB is a production, add FOLLOW(A) to FOLLOW(B)

## Parse Tables
An **LL(1) parse table** M is a 2D table where M[A,a] contains the production to use when we have non-terminal A on the parse stack and terminal a as the current input token.

**Construction Algorithm:**

- For each production A → α:

    - For each terminal a ∈ FIRST(α), add A → α to M[A,a]

    - If ε ∈ FIRST(α), then for each terminal b ∈ FOLLOW(A), add A → α to M[A,b]

- Mark all undefined entries as "error"

- If any cell contains more than one production, the grammar is not LL(1)

## Suggested Reading
https://craftinginterpreters.com/parsing-expressions.html

https://web.stanford.edu/class/cs143/ -- particularly slides for lecture 7

https://ocw.mit.edu/courses/6-035-computer-language-engineering-spring-2010/pages/lecture-notes/ -- first four lectures are relevant

### Assignment Overview
You will analyse the LL(1) properties of the core MiniLisp grammar and implement a working parser based on your analysis. This assignment connects our study of context-free grammars to practical parsing implementation.

**Learning Objectives:**

- Master LL(1) parsing theory through hands-on analysis

- Implement a parser that directly uses LL(1) parse tables

- Bridge formal language theory with practical implementation

## Part A: Grammar Analysis (40 points)
### A.1: LL(1) Property Verification (25 points)
Analyse this core MiniLisp grammar:
```
<program>    ::= <expr>

<expr>       ::= NUMBER
               | IDENTIFIER  
               | '(' <paren-expr> ')'

<paren-expr> ::= '+' <expr> <expr>
               | '×' <expr> <expr>
               | '=' <expr> <expr>
               | '−' <expr> <expr>
               | '?' <expr> <expr> <expr>
               | 'λ' IDENTIFIER <expr>
               | '≜' IDENTIFIER <expr> <expr>
               | <expr> <expr>*
```
#### Required Analysis:

- Compute FIRST sets for all non-terminals

- Compute FOLLOW sets for all non-terminals

- Construct the LL(1) parse table

- Verify LL(1) property: Show there are no conflicts in your parse table

### A.2: Conflict Resolution (10 points)
The function application rule `<expr> <expr>*` might seem problematic for LL(1). Explain:

- Why this rule doesn't create conflicts

- How the parser decides between <expr> <expr>* and other <paren-expr> alternatives

- What the lookahead tells us in each case

### A.3: Grammar Properties (5 points)
Discuss:

- Why this grammar is unambiguous

- How left-factoring was used in the design

- What would happen if we wrote <expr> ::= NUMBER | IDENTIFIER | '(' <expr> ')' | '(' '+' <expr> <expr> ')' | ...

## Part B: Implementation (45 points)
### B.1: Lexer (15 points)
Implement a lexer that produces tokens as follows.
| Token Name | Regex | Unicode Reference |
| :--------- | :---- | :-----------------|
| NUMBER | [0-9]+ | U+0030-U+0039 |
| IDENTIFIER | [a-zA-Z][a-zA-Z0-9]* | U+0061-U+007A, U+0041-U+005A, U+0030-U+0039 |
| PLUS | + | U+002B |
| MINUS | − | U+2212 -- NOT U+002D, which is dash "-" |
| MULT | × | U+00D7 |
| EQUALS | = | U+003D |
| CONDITIONAL | ? | U+003F |
| LAMBDA | λ | U+03BB |
| LET | ≜ | U+225C |
| LPAREN | ( | U+0028 |
| RPAREN | ) | U+0029 |

### B.2: Parse Table Implementation (20 points)
Implement a table-driven LL(1) parser that uses the parse table you constructed in Part A.

Requirements:

- Use the actual parse table from your analysis

- Implement the standard LL(1) parsing algorithm

- Handle the predictive parsing stack

- Parse Table Structure: Your table should map (non-terminal, terminal) pairs to production rules.

### B.3: Parse Tree Output (10 points)
Generate a simple parse tree representation. Use basic nested structures:

Example:
```
Input: (+ 2 x)
Output: ['PLUS', 2, 'x']

Input: (× (+ 1 2) 3)  
Output: ['MULT', ['PLUS', 1, 2], 3]

Input: (λ x (+ x 1))
Output: ['LAMBDA', 'x', ['PLUS', 'x', 1]]
```
**Keep it simple**: Don't worry about fancy AST classes - basic lists/arrays/tuples are fine.

## Part C: Testing and Validation (15 points)
### C.1: Test Cases (10 points)
Create test cases that validate your parser:

**Basic expressions:**
```
42
x  
(+ 2 3)
(× x 5)
```
**Nested expressions:**
```
(+ (× 2 3) 4)
(? (= x 0) 1 0)
```
**Function expressions:**
```
(λ x x)
(≜ y 10 y)
((λ x (+ x 1)) 5)
```
### C.2: Error Handling (5 points)
Your parser should return useful error messages.
```
(+ 2          // missing closing paren
)             // unmatched paren  
(+ 2 3 4)     // wrong number of arguments
```

## Technical Requirements
### Implementation Notes
**Language Choice**: Any language, but consider:

- Python: easy list/dict structures for parse table

- Java: clear object-oriented design

- C++: explicit memory management practice

**Parser Algorithm**: Must implement the standard LL(1) table-driven algorithm:

- Initialize stack with start symbol

- Loop: match terminals, expand non-terminals using parse table

- Accept when stack is empty and input consumed

**No Parser Generators**: Must implement the algorithm yourself, not use yacc/ANTLR/etc.

### Expected Scope
This is a **learning implementation**, not production code:

- ~200-400 lines total implementation

- Focus on correctness over efficiency

- Clean, readable code over optimization

### Submission Format
**Written Analysis (PDF):**

- FIRST/FOLLOW set computations

- Complete LL(1) parse table

- Conflict analysis and explanations

- 4-6 pages total

**Source Code:**

- Lexer implementation

- Table-driven parser implementation

- Test cases with expected JSON outputs

- README with running instructions

**Sample Output Files:**

- Include .json files showing your parser's output on test cases

- Demonstrate proper JSON formatting

### Grading Rubric
| Component | Points | Criteria |
| :-------- | :----- | :------- |
| FIRST/FOLLOW Sets | 15 | Correct computation with clear step-by-step derivations |
| Parse Table | 15 | Accurate table with construction methodology explained |
| Conflict Analysis | 10 | Deep understanding of LL(1) properties and design decisions |
| Lexer | 15 | Correct tokenization, mandatory Unicode support, handles all required tokens |
| Parser Implementation | 30 | Table-driven algorithm, uses computed parse table correctly |
| Testing & Justification | 15 | Comprehensive test design with clear rationale and coverage analysis |
| Total | 100 | |


### Getting Started
**Development Strategy**
- **Start with grammar analysis** - you need the parse table before you can implement

- **Implement lexer first** - parser needs clean token stream

- **Build parser incrementally** - test with simple expressions first

- **Validate against your analysis** - your parser should behave exactly as your parse table predicts

**Key Insight**
Your written analysis and implementation should match perfectly. If your parser accepts an input, you should be able to trace through your parse table by hand and get the same result. This assignment tests whether you truly understand LL(1) parsing by making you use the theory directly.

### Connection to Course Goals
This assignment bridges formal language theory with practical implementation. The LL(1) analysis you perform is the same process used in real compiler design, while the simple implementation demonstrates how theoretical constructs become working code.