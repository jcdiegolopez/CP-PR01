package com.yalex.regex.node;

import java.util.Objects;

public class OptionalNode implements RegexNode {

    private final RegexNode child;

    public OptionalNode(RegexNode child) {
        this.child = Objects.requireNonNull(child, "child no puede ser null");
    }

    public RegexNode getChild() {
        return child;
    }
}
