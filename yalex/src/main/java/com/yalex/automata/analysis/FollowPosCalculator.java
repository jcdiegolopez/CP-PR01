package com.yalex.automata.analysis;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.yalex.automata.syntax.SyntaxTree;
import com.yalex.automata.syntax.SyntaxTreeBuilder;
import com.yalex.regex.node.AlternNode;
import com.yalex.regex.node.ConcatNode;
import com.yalex.regex.node.DiffNode;
import com.yalex.regex.node.KleeneNode;
import com.yalex.regex.node.RegexNode;

/**
 * Calcula {@code followpos} a partir del árbol aumentado y las anotaciones
 * {@link NodeAnnotation} (nullable / firstpos / lastpos).
 */
public final class FollowPosCalculator {

    private FollowPosCalculator() {
    }

    /**
     * Anota el árbol con {@link NullableFirstLast} y construye la tabla followpos.
     */
    public static FollowPosTable compute(SyntaxTree tree) {
        Map<RegexNode, NodeAnnotation> ann = NullableFirstLast.annotate(tree);
        Map<Integer, TreeSet<Integer>> raw = new HashMap<>();
        applyFollowRules(tree.getRoot(), ann, raw);
        Map<Integer, Set<Integer>> frozen = new HashMap<>();
        for (var e : raw.entrySet()) {
            frozen.put(e.getKey(), e.getValue());
        }
        return new FollowPosTable(frozen);
    }

    /**
     * Usa anotaciones ya calculadas (útil para tests que comparan paso a paso).
     */
    public static FollowPosTable compute(SyntaxTree tree, Map<RegexNode, NodeAnnotation> ann) {
        Map<Integer, TreeSet<Integer>> raw = new HashMap<>();
        applyFollowRules(tree.getRoot(), ann, raw);
        Map<Integer, Set<Integer>> frozen = new HashMap<>();
        for (var e : raw.entrySet()) {
            frozen.put(e.getKey(), e.getValue());
        }
        return new FollowPosTable(frozen);
    }

    private static void applyFollowRules(
            RegexNode node,
            Map<RegexNode, NodeAnnotation> ann,
            Map<Integer, TreeSet<Integer>> raw) {

        if (node instanceof ConcatNode c) {
            NodeAnnotation left = ann.get(c.getLeft());
            NodeAnnotation right = ann.get(c.getRight());
            for (int i : left.lastpos()) {
                addAll(raw, i, right.firstpos());
            }
            applyFollowRules(c.getLeft(), ann, raw);
            applyFollowRules(c.getRight(), ann, raw);

        } else if (node instanceof KleeneNode k) {
            NodeAnnotation child = ann.get(k.getChild());
            for (int i : child.lastpos()) {
                addAll(raw, i, child.firstpos());
            }
            applyFollowRules(k.getChild(), ann, raw);

        } else if (node instanceof AlternNode a) {
            applyFollowRules(a.getLeft(), ann, raw);
            if (!SyntaxTreeBuilder.isEpsilonRegex(a.getRight())) {
                applyFollowRules(a.getRight(), ann, raw);
            }

        } else if (node instanceof DiffNode d) {
            // R1 # R2: no hay enlace follow entre subárboles; solo reglas internas
            applyFollowRules(d.getLeft(), ann, raw);
            applyFollowRules(d.getRight(), ann, raw);
        }
        // hojas: sin reglas propias
    }

    private static void addAll(Map<Integer, TreeSet<Integer>> raw, int from, Set<Integer> targets) {
        if (targets.isEmpty()) {
            return;
        }
        raw.computeIfAbsent(from, k -> new TreeSet<>()).addAll(targets);
    }
}
