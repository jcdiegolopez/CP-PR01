package com.yalex.regex.node;

import java.util.Objects;

public class ReferenceNode implements RegexNode {

    private final String name;

    public ReferenceNode(String name) {
        this.name = Objects.requireNonNull(name, "name no puede ser null").trim();
        if (this.name.isEmpty()) {
            throw new IllegalArgumentException("name no puede estar vacio");
        }
    }

    public String getName() {
        return name;
    }
}
