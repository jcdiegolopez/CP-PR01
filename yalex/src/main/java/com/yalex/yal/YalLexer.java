package com.yalex.yal;

import java.util.Objects;

public class YalLexer {

    private final String source;

    public YalLexer(String source) {
        this.source = Objects.requireNonNull(source, "source no puede ser null");
    }

    public String stripComments() {
        // Quita comentarios (* ... *).
        StringBuilder output = new StringBuilder();
        int depth = 0;
        for (int i = 0; i < source.length(); i++) {
            char current = source.charAt(i);
            char next = i + 1 < source.length() ? source.charAt(i + 1) : '\0';

            if (current == '(' && next == '*') {
                depth++;
                i++;
                continue;
            }
            if (current == '*' && next == ')' && depth > 0) {
                depth--;
                i++;
                continue;
            }
            if (depth == 0) {
                output.append(current);
            }
        }
        if (depth != 0) {
            throw new IllegalArgumentException("comentario sin cierre en archivo .yal");
        }
        return output.toString();
    }
}
