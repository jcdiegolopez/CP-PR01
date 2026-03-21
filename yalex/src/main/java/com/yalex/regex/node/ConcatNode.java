package com.yalex.regex.node;

import java.util.Objects;

/**
 * Nodo que representa la concatenación implícita de dos subexpresiones.
 */
public class ConcatNode extends RegexNode {

    private final RegexNode left;
    private final RegexNode right;

    public ConcatNode(RegexNode left, RegexNode right) {
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
        return "ConcatNode(" + left + " · " + right + ")";
    }
}
