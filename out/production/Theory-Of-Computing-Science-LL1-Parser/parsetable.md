## Complete LL(1) Parse Table

### Table Layout

The parse table M[Non-terminal, Terminal] maps each (non-terminal, lookahead token) pair to the production to apply.

| Non-Terminal | NUMBER | IDENTIFIER | + | × | = | − | ? | λ | ≜ | ( | ) | $ |
|-------------|--------|-----------|---|---|---|---|---|---|---|---|---|---|
| **`<program>`** | 1 | 1 | — | — | — | — | — | — | — | 1 | — | — |
| **`<expr>`** | 2 | 3 | — | — | — | — | — | — | — | 4 | — | — |
| **`<paren-expr>`** | 12 | 12 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12 | — | — |

**Legend:**
- Numbers refer to production rules (listed below)
- "—" indicates an error (no valid production)

### Production Rules Reference

1. `<program> → <expr>`
2. `<expr> → NUMBER`
3. `<expr> → IDENTIFIER`
4. `<expr> → '(' <paren-expr> ')'`
5. `<paren-expr> → '+' <expr> <expr>`
6. `<paren-expr> → '×' <expr> <expr>`
7. `<paren-expr> → '=' <expr> <expr>`
8. `<paren-expr> → '−' <expr> <expr>`
9. `<paren-expr> → '?' <expr> <expr> <expr>`
10. `<paren-expr> → 'λ' IDENTIFIER <expr>`
11. `<paren-expr> → '≜' IDENTIFIER <expr> <expr>`
12. `<paren-expr> → <expr> <expr>*`