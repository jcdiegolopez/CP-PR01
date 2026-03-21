package com.yalex.regex.node;

/**
 * Nodo que representa el wildcard "_" — acepta cualquier carácter del alfabeto.
 */
public class WildcardNode extends RegexNode {

    public WildcardNode() {
    }

    @Override
    public String toString() {
        return "WildcardNode(_)";
    }
}
