package com.yalex.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RuleSet {

    private final String name;
    private final List<Rule> rules;

    public RuleSet(String name, List<Rule> rules) {
        this.name = Objects.requireNonNull(name, "name no puede ser null").trim();
        Objects.requireNonNull(rules, "rules no puede ser null");
        if (this.name.isEmpty()) {
            throw new IllegalArgumentException("name no puede estar vacio");
        }
        if (rules.isEmpty()) {
            throw new IllegalArgumentException("rules no puede estar vacia");
        }
        this.rules = List.copyOf(rules);
    }

    public String getName() {
        return name;
    }

    public List<Rule> getRules() {
        return new ArrayList<>(rules);
    }
}
