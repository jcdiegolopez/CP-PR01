package com.yalex.regex.node;

import java.util.Objects;

/**
 * Nodo hoja que representa un carácter literal (o secuencia de escape ya resuelta).
 * El valor es el carácter real, p.ej. "\n" para nueva línea.
 */
public class CharNode extends RegexNode {

    private final String value;

    public CharNode(String value) {
        this.value = Objects.requireNonNull(value, "value no puede ser null");
        if (this.value.isEmpty()) {
            throw new IllegalArgumentException("value no puede estar vacio");
        }
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "CharNode(" + value + ")";
    }
}
