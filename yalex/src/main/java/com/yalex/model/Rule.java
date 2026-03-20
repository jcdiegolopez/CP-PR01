package com.yalex.model;

import java.util.Objects;

public class Rule {

    private final String pattern;
    private final String action;

    public Rule(String pattern, String action) {
        this.pattern = Objects.requireNonNull(pattern, "pattern no puede ser null").trim();
        this.action = Objects.requireNonNull(action, "action no puede ser null").trim();
        if (this.pattern.isEmpty()) {
            throw new IllegalArgumentException("pattern no puede estar vacio");
        }
        if (this.action.isEmpty()) {
            throw new IllegalArgumentException("action no puede estar vacia");
        }
    }

    public String getPattern() {
        return pattern;
    }

    public String getAction() {
        return action;
    }
}
