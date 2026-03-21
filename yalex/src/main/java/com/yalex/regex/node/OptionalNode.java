package com.yalex.regex.node;

import java.util.Objects;

/**
 * Nodo que representa el operador opcional (regexp?), equivalente a (r | ε).
 */
public class OptionalNode extends RegexNode {

    private final RegexNode child;

    public OptionalNode(RegexNode child) {
        this.child = Objects.requireNonNull(child, "child no puede ser null");
    }

    public RegexNode getChild() {
        return child;
    }

    @Override
    public String toString() {
        return "OptionalNode(" + child + "?)";
    }
}
