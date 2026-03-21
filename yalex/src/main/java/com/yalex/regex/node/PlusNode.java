package com.yalex.regex.node;

import java.util.Objects;

/**
 * Nodo que representa la cerradura positiva (regexp+), equivalente a rr*.
 */
public class PlusNode extends RegexNode {

    private final RegexNode child;

    public PlusNode(RegexNode child) {
        this.child = Objects.requireNonNull(child, "child no puede ser null");
    }

    public RegexNode getChild() {
        return child;
    }

    @Override
    public String toString() {
        return "PlusNode(" + child + "+)";
    }
}
