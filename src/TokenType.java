/**
 *
 * Token specifications from the assignment:
 * - NUMBER: [0-9]+ (U+0030-U+0039)
 * - IDENTIFIER: [a-zA-Z][a-zA-Z0-9]* (U+0061-U+007A, U+0041-U+005A,
 * U+0030-U+0039)
 * - PLUS: + (U+002B)
 * - MINUS: − (U+2212) -- NOT U+002D (dash "-")
 * - MULT: × (U+00D7)
 * - EQUALS: = (U+003D)
 * - CONDITIONAL: ? (U+003F)
 * - LAMBDA: λ (U+03BB)
 * - LET: ≜ (U+225C)
 * - LPAREN: ( (U+0028)
 * - RPAREN: ) (U+0029)
 * - EOF: End of file marker
 */
public enum TokenType {
    // Literals
    NUMBER, // [0-9]+
    IDENTIFIER, // [a-zA-Z][a-zA-Z0-9]*

    // Operators
    PLUS, // + (U+002B)
    MINUS, // − (U+2212)
    MULT, // × (U+00D7)
    EQUALS, // = (U+003D)

    // Special forms
    CONDITIONAL, // ? (U+003F)
    LAMBDA, // λ (U+03BB)
    LET, // ≜ (U+225C)

    // Delimiters
    LPAREN, // ( (U+0028)
    RPAREN, // ) (U+0029)

    // End marker
    EOF // End of input
}