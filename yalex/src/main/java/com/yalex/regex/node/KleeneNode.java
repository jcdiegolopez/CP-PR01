package com.yalex.regex.node;

import java.util.Objects;

public class KleeneNode implements RegexNode {

    private final RegexNode child;

    public KleeneNode(RegexNode child) {
        this.child = Objects.requireNonNull(child, "child no puede ser null");
    }

    public RegexNode getChild() {
        return child;
    }
}
