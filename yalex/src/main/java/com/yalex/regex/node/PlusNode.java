package com.yalex.regex.node;

import java.util.Objects;

public class PlusNode implements RegexNode {

    private final RegexNode child;

    public PlusNode(RegexNode child) {
        this.child = Objects.requireNonNull(child, "child no puede ser null");
    }

    public RegexNode getChild() {
        return child;
    }
}
