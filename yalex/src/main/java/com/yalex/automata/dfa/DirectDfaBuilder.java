package com.yalex.automata.dfa;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.yalex.automata.analysis.FollowPosCalculator;
import com.yalex.automata.analysis.FollowPosTable;
import com.yalex.automata.analysis.NodeAnnotation;
import com.yalex.automata.analysis.NullableFirstLast;
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
 * Construcción directa del DFA a partir del árbol aumentado y la tabla {@code followpos}.
 */
public final class DirectDfaBuilder {

    private DirectDfaBuilder() {
    }

    public static DFA build(SyntaxTree tree) {
        FollowPosTable follow = FollowPosCalculator.compute(tree);
        Map<RegexNode, NodeAnnotation> ann = NullableFirstLast.annotate(tree);
        return build(tree, follow, ann);
    }

    public static DFA build(
            SyntaxTree tree,
            FollowPosTable follow,
            Map<RegexNode, NodeAnnotation> ann) {
        Objects.requireNonNull(tree, "tree");
        Objects.requireNonNull(follow, "follow");
        Objects.requireNonNull(ann, "ann");

        Map<Integer, Position> posById = new HashMap<>();
        for (Position p : tree.getPositions()) {
            posById.put(p.getId(), p);
        }

        int[] idx = {0};
        Map<Integer, RegexNode> leafAtom = new HashMap<>();
        collectLeafAtoms(tree.getRoot(), tree.getPositions(), idx, leafAtom);
        if (idx[0] != tree.getPositions().size()) {
            throw new IllegalStateException("Leaf walk desincronizado con posiciones");
        }

        int endId = tree.getEndPosition().getId();
        NavigableSet<Character> alphabet = buildAlphabet(tree, leafAtom);

        NodeAnnotation rootAnn = ann.get(tree.getRoot());
        TreeSet<Integer> initial = new TreeSet<>(rootAnn.firstpos());

        Map<TreeSet<Integer>, Integer> setToId = new HashMap<>();
        List<TreeSet<Integer>> stateSets = new ArrayList<>();
        Map<Integer, Map<Character, Integer>> trans = new HashMap<>();

        TreeSet<Integer> initialKey = new TreeSet<>(initial);
        setToId.put(initialKey, 0);
        stateSets.add(initialKey);
        ArrayDeque<TreeSet<Integer>> work = new ArrayDeque<>();
        work.add(initialKey);

        while (!work.isEmpty()) {
            TreeSet<Integer> s = work.poll();
            Integer sidObj = setToId.get(s);
            if (sidObj == null) {
                throw new IllegalStateException("Estado no registrado");
            }
            int sid = sidObj;
            for (char a : alphabet) {
                TreeSet<Integer> dest = move(s, a, follow, leafAtom, posById);
                if (dest.isEmpty()) {
                    continue;
                }
                TreeSet<Integer> destKey = new TreeSet<>(dest);
                if (!setToId.containsKey(destKey)) {
                    int nid = stateSets.size();
                    setToId.put(destKey, nid);
                    stateSets.add(destKey);
                    work.add(destKey);
                }
                int tid = setToId.get(destKey);
                trans.computeIfAbsent(sid, k -> new TreeMap<>()).put(a, tid);
            }
        }

        List<DFAState> states = new ArrayList<>();
        for (int i = 0; i < stateSets.size(); i++) {
            TreeSet<Integer> set = stateSets.get(i);
            boolean acc = set.contains(endId);
            states.add(new DFAState(i, set, acc));
        }

        return new DFA(states, 0, trans, alphabet);
    }

    private static TreeSet<Integer> move(
            Set<Integer> state,
            char a,
            FollowPosTable follow,
            Map<Integer, RegexNode> leafAtom,
            Map<Integer, Position> posById) {

        TreeSet<Integer> next = new TreeSet<>();
        for (int p : state) {
            Position pos = posById.get(p);
            if (pos == null || pos.isEnd()) {
                continue;
            }
            RegexNode leaf = leafAtom.get(p);
            if (leaf == null) {
                continue;
            }
            if (!leafMatches(leaf, pos)) {
                continue;
            }
            if (!matchesLeafInput(leaf, a)) {
                continue;
            }
            next.addAll(follow.getFollowSet(p));
        }
        return next;
    }

    private static boolean leafMatches(RegexNode leaf, Position pos) {
        if (pos.isEnd()) {
            return false;
        }
        if (leaf instanceof CharNode cn) {
            return cn.getValue().length() != 1 || cn.getValue().charAt(0) != '\0';
        }
        return leaf instanceof CharClassNode;
    }

    private static boolean matchesLeafInput(RegexNode leaf, char a) {
        if (leaf instanceof CharNode cn) {
            return cn.getValue().charAt(0) == a;
        }
        if (leaf instanceof CharClassNode cc) {
            return CharClassMatcher.matches(cc, a);
        }
        return false;
    }

    private static NavigableSet<Character> buildAlphabet(
            SyntaxTree tree,
            Map<Integer, RegexNode> leafAtom) {

        TreeSet<Character> sigma = new TreeSet<>();
        for (Position p : tree.getPositions()) {
            if (p.isEnd()) {
                continue;
            }
            RegexNode n = leafAtom.get(p.getId());
            if (n instanceof CharNode cn) {
                if (cn.getValue().length() == 1 && cn.getValue().charAt(0) == '\0') {
                    continue;
                }
                sigma.add(cn.getValue().charAt(0));
            } else if (n instanceof CharClassNode cc) {
                sigma.addAll(CharClassMatcher.matchingAlphabet(cc));
            }
        }
        return sigma;
    }

    private static void collectLeafAtoms(
            RegexNode node,
            List<Position> positions,
            int[] index,
            Map<Integer, RegexNode> atoms) {

        if (node instanceof CharNode cn) {
            Position p = positions.get(index[0]++);
            atoms.put(p.getId(), cn);
            return;
        }
        if (node instanceof CharClassNode cc) {
            Position p = positions.get(index[0]++);
            atoms.put(p.getId(), cc);
            return;
        }
        if (node instanceof ConcatNode c) {
            collectLeafAtoms(c.getLeft(), positions, index, atoms);
            collectLeafAtoms(c.getRight(), positions, index, atoms);
            return;
        }
        if (node instanceof AlternNode a) {
            collectLeafAtoms(a.getLeft(), positions, index, atoms);
            if (!SyntaxTreeBuilder.isEpsilonRegex(a.getRight())) {
                collectLeafAtoms(a.getRight(), positions, index, atoms);
            }
            return;
        }
        if (node instanceof KleeneNode k) {
            collectLeafAtoms(k.getChild(), positions, index, atoms);
            return;
        }
        if (node instanceof DiffNode d) {
            collectLeafAtoms(d.getLeft(), positions, index, atoms);
            collectLeafAtoms(d.getRight(), positions, index, atoms);
            return;
        }
        if (SyntaxTreeBuilder.isEpsilonRegex(node)) {
            return;
        }
        throw new IllegalArgumentException(
                "Nodo hoja inesperado en collectLeafAtoms: " + node.getClass().getSimpleName());
    }
}
