package com.yalex.regex.node;

import java.util.Objects;

/**
 * Nodo que representa la diferencia de lenguajes (regexp1 # regexp2).
 * Acepta cadenas que hacen match con left pero NO con right.
 */
public class DiffNode extends RegexNode {

    private final RegexNode left;
    private final RegexNode right;

    public DiffNode(RegexNode left, RegexNode right) {
        this.left = Objects.requireNonNull(left, "left no puede ser null");
        this.right = Objects.requireNonNull(right, "right no puede ser null");
    }

    public RegexNode getLeft() {
        return left;
    }

    public RegexNode getRight() {
        return right;
    }

    @Override
    public String toString() {
        return "DiffNode(" + left + " # " + right + ")";
    }
}
