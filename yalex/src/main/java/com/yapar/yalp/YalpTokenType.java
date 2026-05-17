package com.yapar.yalp;

enum YalpTokenType {
    PERCENT_TOKEN,    // %token
    PERCENT_PERCENT,  // %%
    COLON,            // :
    PIPE,             // |
    SEMICOLON,        // ;
    IDENTIFIER,       // [A-Za-z_][A-Za-z0-9_]*
    ACTION,           // contenido de { ... }
    EOF
}
