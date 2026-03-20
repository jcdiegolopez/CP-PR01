package com.yalex.model;

import java.util.Objects;

public class LetDefinition {

    private final String name;
    private final String regex;

    public LetDefinition(String name, String regex) {
        this.name = Objects.requireNonNull(name, "name no puede ser null").trim();
        this.regex = Objects.requireNonNull(regex, "regex no puede ser null").trim();
        if (this.name.isEmpty()) {
            throw new IllegalArgumentException("name no puede estar vacio");
        }
        if (this.regex.isEmpty()) {
            throw new IllegalArgumentException("regex no puede estar vacio");
        }
    }

    public String getName() {
        return name;
    }

    public String getRegex() {
        return regex;
    }
}
