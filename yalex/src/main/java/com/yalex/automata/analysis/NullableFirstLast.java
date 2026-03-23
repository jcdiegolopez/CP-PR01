package com.yalex.automata.analysis;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.yalex.automata.syntax.Position;
import com.yalex.automata.syntax.SyntaxTree;
import com.yalex.automata.syntax.SyntaxTreeBuilder;
import com.yalex.regex.node.AlternNode;
import com.yalex.regex.node.CharClassNode;
import com.yalex.regex.node.CharNode;
import com.yalex.regex.node.ConcatNode;
import com.yalex.regex.node.DiffNode;
import com.yalex.regex.node.KleeneNode;
import com.yalex.regex.node.RegexNode;

/**
 * Calcula {@code nullable}, {@code firstpos} y {@code lastpos} para cada nodo del árbol
 * sintáctico aumentado (método directo de construcción de DFA).
 */
public final class NullableFirstLast {

    private NullableFirstLast() {
    }

    /**
     * Anota todos los nodos del árbol y devuelve un mapa nodo → anotación.
     * El recorrido de hojas coincide con {@link SyntaxTree#getPositions()}.
     */
    public static Map<RegexNode, NodeAnnotation> annotate(SyntaxTree tree) {
        List<Position> positions = tree.getPositions();
        int[] index = {0};
        Map<RegexNode, NodeAnnotation> map = new HashMap<>();
        annotateNode(tree.getRoot(), positions, index, map);
        if (index[0] != positions.size()) {
            throw new IllegalStateException(
                    "Desajuste entre nodos hoja y posiciones: esperadas " + positions.size()
                            + " hojas, se visitaron " + index[0]);
        }
        return Collections.unmodifiableMap(map);
    }

    private static NodeAnnotation annotateNode(
            RegexNode node,
            List<Position> positions,
            int[] index,
            Map<RegexNode, NodeAnnotation> map) {

        NodeAnnotation ann = compute(node, positions, index, map);
        map.put(node, ann);
        return ann;
    }

    private static NodeAnnotation compute(
            RegexNode node,
            List<Position> positions,
            int[] index,
            Map<RegexNode, NodeAnnotation> map) {

        // Sentinel interno (r?→r|ε, o EpsilonRegexNode tras expand): sin posiciones
        if (SyntaxTreeBuilder.isEpsilonRegex(node)) {
            return new NodeAnnotation(true, Set.of(), Set.of());
        }

        if (node instanceof CharNode || node instanceof CharClassNode) {
            Position p = positions.get(index[0]++);
            int id = p.getId();
            Set<Integer> singleton = Set.of(id);
            return new NodeAnnotation(false, singleton, singleton);
        }

        if (node instanceof ConcatNode c) {
            NodeAnnotation left = annotateNode(c.getLeft(), positions, index, map);
            NodeAnnotation right = annotateNode(c.getRight(), positions, index, map);
            boolean nullable = left.nullable() && right.nullable();
            Set<Integer> first = left.nullable()
                    ? union(left.firstpos(), right.firstpos())
                    : copy(left.firstpos());
            Set<Integer> last = right.nullable()
                    ? union(left.lastpos(), right.lastpos())
                    : copy(right.lastpos());
            return new NodeAnnotation(nullable, first, last);
        }

        if (node instanceof AlternNode a) {
            NodeAnnotation left = annotateNode(a.getLeft(), positions, index, map);
            if (SyntaxTreeBuilder.isEpsilonRegex(a.getRight())) {
                // r | ε  →  nullable siempre true; solo posiciones del lado izquierdo
                return new NodeAnnotation(
                        true,
                        copy(left.firstpos()),
                        copy(left.lastpos()));
            }
            NodeAnnotation right = annotateNode(a.getRight(), positions, index, map);
            boolean nullable = left.nullable() || right.nullable();
            return new NodeAnnotation(
                    nullable,
                    union(left.firstpos(), right.firstpos()),
                    union(left.lastpos(), right.lastpos()));
        }

        if (node instanceof KleeneNode k) {
            NodeAnnotation child = annotateNode(k.getChild(), positions, index, map);
            return new NodeAnnotation(
                    true,
                    copy(child.firstpos()),
                    copy(child.lastpos()));
        }

        if (node instanceof DiffNode d) {
            NodeAnnotation left = annotateNode(d.getLeft(), positions, index, map);
            NodeAnnotation right = annotateNode(d.getRight(), positions, index, map);
            // λ ∈ L(R1 \ R2)  ⇔  λ ∈ L(R1) y λ ∉ L(R2)
            boolean nullable = left.nullable() && !right.nullable();
            return new NodeAnnotation(
                    nullable,
                    copy(left.firstpos()),
                    copy(left.lastpos()));
        }

        throw new IllegalArgumentException(
                "Nodo no soportado en NullableFirstLast: " + node.getClass().getSimpleName());
    }

    private static Set<Integer> union(Set<Integer> a, Set<Integer> b) {
        if (a.isEmpty()) {
            return copy(b);
        }
        if (b.isEmpty()) {
            return copy(a);
        }
        TreeSet<Integer> out = new TreeSet<>(a);
        out.addAll(b);
        return Collections.unmodifiableSet(out);
    }

    private static Set<Integer> copy(Set<Integer> s) {
        if (s.isEmpty()) {
            return Set.of();
        }
        return Collections.unmodifiableSet(new TreeSet<>(s));
    }
}
