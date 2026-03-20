package com.yalex.regex.node;

import java.util.Objects;

public class DiffNode implements RegexNode {

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
}
