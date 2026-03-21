package com.yalex.regex.node;

import java.util.Objects;

/**
 * Nodo que representa la cerradura de Kleene (regexp*).
 */
public class KleeneNode extends RegexNode {

    private final RegexNode child;

    public KleeneNode(RegexNode child) {
        this.child = Objects.requireNonNull(child, "child no puede ser null");
    }

    public RegexNode getChild() {
        return child;
    }

    @Override
    public String toString() {
        return "KleeneNode(" + child + "*)";
    }
}
