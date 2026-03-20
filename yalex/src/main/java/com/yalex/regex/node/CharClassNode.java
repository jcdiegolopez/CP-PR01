package com.yalex.regex.node;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CharClassNode implements RegexNode {

    private final List<String> entries;
    private final boolean negated;

    public CharClassNode(List<String> entries, boolean negated) {
        Objects.requireNonNull(entries, "entries no puede ser null");
        if (entries.isEmpty()) {
            throw new IllegalArgumentException("entries no puede estar vacia");
        }
        this.entries = List.copyOf(entries);
        this.negated = negated;
    }

    public List<String> getEntries() {
        return new ArrayList<>(entries);
    }

    public boolean isNegated() {
        return negated;
    }
}
