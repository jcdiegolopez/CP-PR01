package com.yalex.regex.node;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Nodo que representa una clase de caracteres [a-z0-9] o su negación [^...].
 *
 * <p>Cada entrada en {@code entries} es uno de:
 * <ul>
 *   <li>Un único carácter, p.ej. {@code "a"}
 *   <li>Un rango con guión, p.ej. {@code "a-z"}
 * </ul>
 * Los caracteres ya están con sus escapes resueltos (e.g. "\n", "\t").
 */
public class CharClassNode extends RegexNode {

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

    @Override
    public String toString() {
        return "CharClassNode(" + (negated ? "^" : "") + entries + ")";
    }
}
