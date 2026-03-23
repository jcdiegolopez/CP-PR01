package com.yalex.regex.node;

/**
 * Representa el símbolo ε de la especificación (cadena vacía), p. ej. el identificador
 * {@code ε} (U+03B5) o la palabra {@code epsilon} en una regexp YALex.
 *
 * <p>Se expande en {@link com.yalex.automata.syntax.SyntaxTreeBuilder} al mismo
 * sentinel interno que {@code r? → r | ε}.
 */
public final class EpsilonRegexNode extends RegexNode {

    public static final EpsilonRegexNode INSTANCE = new EpsilonRegexNode();

    private EpsilonRegexNode() {
    }

    @Override
    public String toString() {
        return "ε";
    }
}
