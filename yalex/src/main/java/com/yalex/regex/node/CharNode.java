package com.yalex.regex.node;

import java.util.Objects;

public class CharNode implements RegexNode {

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
}
