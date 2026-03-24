package com.yalex.automata.dfa;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
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
 * Construye un DFA combinado a partir de múltiples reglas del lexer.
 *
 * <p>Cada regex rᵢ se concatena con su propio marcador final #ᵢ y todas se unen
 * con alternación: {@code (r₁·#₁) | (r₂·#₂) | … | (rₙ·#ₙ)}.
 * Los estados de aceptación del DFA resultante saben cuál regla matchearon
 * (prioridad por índice mínimo si hay conflicto).
 */
public final class CombinedDfaBuilder {

    /**
     * Resultado de la construcción del DFA combinado.
     *
     * @param dfa            el DFA combinado
     * @param stateRuleIndex mapa stateId → ruleIndex para estados de aceptación (-1 si no acepta)
     * @param rulePatterns   patrones originales de cada regla
     */
    public record Result(
            DFA dfa,
            Map<Integer, Integer> stateRuleIndex,
            List<String> rulePatterns) {

        public Result {
            Objects.requireNonNull(dfa, "dfa");
            stateRuleIndex = Map.copyOf(stateRuleIndex);
            rulePatterns = List.copyOf(rulePatterns);
        }
    }

    private CombinedDfaBuilder() {
    }

    /**
     * Construye el DFA combinado.
     *
     * @param asts     lista de ASTs de regex ya parseados (uno por regla)
     * @param patterns patrones originales (para metadata)
     * @return resultado con DFA y mapeo de estados a reglas
     */
    public static Result build(List<RegexNode> asts, List<String> patterns) {
        Objects.requireNonNull(asts, "asts");
        Objects.requireNonNull(patterns, "patterns");
        if (asts.isEmpty()) {
            throw new IllegalArgumentException("No hay reglas para combinar");
        }
        if (asts.size() != patterns.size()) {
            throw new IllegalArgumentException("asts y patterns deben tener el mismo tamaño");
        }

        // 1. Expandir cada AST y concatenar con su marcador final
        List<CharNode> endMarkers = new ArrayList<>();
        List<RegexNode> augmented = new ArrayList<>();
        for (RegexNode ast : asts) {
            RegexNode expanded = SyntaxTreeBuilder.expandAst(ast);
            CharNode endMarker = new CharNode(String.valueOf('\0'));
            endMarkers.add(endMarker);
            augmented.add(new ConcatNode(expanded, endMarker));
        }

        // 2. Combinar con alternación (right-associative)
        RegexNode combined = augmented.get(augmented.size() - 1);
        for (int i = augmented.size() - 2; i >= 0; i--) {
            combined = new AlternNode(augmented.get(i), combined);
        }

        // 3. Asignar posiciones y construir SyntaxTree
        List<Position> positions = new ArrayList<>();
        int[] nextId = {1};
        assignPositions(combined, positions, endMarkers, nextId);

        // Identificar qué posiciones son end markers y a qué regla pertenecen
        Map<Integer, Integer> endPosToRule = new HashMap<>();
        int posIdx = 0;
        int[] leafCount = {0};
        mapEndPositions(combined, endMarkers, positions, leafCount, endPosToRule);

        // Necesitamos el "último" end position para construir un SyntaxTree válido.
        // Usamos el end marker de la última regla (la que tiene mayor position ID).
        Position lastEndPos = null;
        for (Position p : positions) {
            if (p.isEnd()) {
                lastEndPos = p;
            }
        }
        if (lastEndPos == null) {
            throw new IllegalStateException("No se encontró marcador final");
        }

        SyntaxTree tree = new SyntaxTree(combined, positions, lastEndPos);

        // 4. Calcular nullable/firstpos/lastpos y followpos
        Map<RegexNode, NodeAnnotation> ann = NullableFirstLast.annotate(tree);
        FollowPosTable follow = FollowPosCalculator.compute(tree, ann);

        // 5. Construir DFA con subset construction (reusa lógica de DirectDfaBuilder)
        Map<Integer, Position> posById = new HashMap<>();
        for (Position p : positions) {
            posById.put(p.getId(), p);
        }

        Map<Integer, RegexNode> leafAtom = new HashMap<>();
        int[] idx = {0};
        collectLeafAtoms(combined, positions, idx, leafAtom);

        // Todos los end position IDs
        Set<Integer> allEndIds = new TreeSet<>(endPosToRule.keySet());

        NavigableSet<Character> alphabet = buildAlphabet(positions, leafAtom);

        NodeAnnotation rootAnn = ann.get(combined);
        TreeSet<Integer> initial = new TreeSet<>(rootAnn.firstpos());

        Map<TreeSet<Integer>, Integer> setToId = new HashMap<>();
        List<TreeSet<Integer>> stateSets = new ArrayList<>();
        Map<Integer, Map<Character, Integer>> trans = new HashMap<>();

        setToId.put(initial, 0);
        stateSets.add(initial);
        ArrayDeque<TreeSet<Integer>> work = new ArrayDeque<>();
        work.add(initial);

        while (!work.isEmpty()) {
            TreeSet<Integer> s = work.poll();
            int sid = setToId.get(s);
            for (char a : alphabet) {
                TreeSet<Integer> dest = move(s, a, follow, leafAtom, posById, allEndIds);
                if (dest.isEmpty()) {
                    continue;
                }
                if (!setToId.containsKey(dest)) {
                    int nid = stateSets.size();
                    setToId.put(dest, nid);
                    stateSets.add(dest);
                    work.add(dest);
                }
                int tid = setToId.get(dest);
                trans.computeIfAbsent(sid, k -> new TreeMap<>()).put(a, tid);
            }
        }

        // 6. Crear estados con información de regla aceptada
        List<DFAState> states = new ArrayList<>();
        Map<Integer, Integer> stateRuleIndex = new HashMap<>();

        for (int i = 0; i < stateSets.size(); i++) {
            TreeSet<Integer> set = stateSets.get(i);
            int ruleIdx = -1;
            for (int posId : set) {
                if (endPosToRule.containsKey(posId)) {
                    int ri = endPosToRule.get(posId);
                    if (ruleIdx < 0 || ri < ruleIdx) {
                        ruleIdx = ri;
                    }
                }
            }
            boolean acc = ruleIdx >= 0;
            states.add(new DFAState(i, set, acc));
            stateRuleIndex.put(i, ruleIdx);
        }

        DFA dfa = new DFA(states, 0, trans, alphabet);
        return new Result(dfa, stateRuleIndex, patterns);
    }

    // =========================================================================
    // Helpers — position assignment
    // =========================================================================

    private static void assignPositions(
            RegexNode node,
            List<Position> positions,
            List<CharNode> endMarkers,
            int[] nextId) {

        if (node instanceof CharNode cn) {
            boolean isEnd = endMarkers.contains(cn);
            char symbol = cn.getValue().charAt(0);
            positions.add(new Position(nextId[0]++, symbol, isEnd));

        } else if (node instanceof CharClassNode) {
            positions.add(new Position(nextId[0]++, '\0', false));

        } else if (node instanceof ConcatNode c) {
            assignPositions(c.getLeft(), positions, endMarkers, nextId);
            assignPositions(c.getRight(), positions, endMarkers, nextId);

        } else if (node instanceof AlternNode a) {
            assignPositions(a.getLeft(), positions, endMarkers, nextId);
            if (!SyntaxTreeBuilder.isEpsilonRegex(a.getRight())) {
                assignPositions(a.getRight(), positions, endMarkers, nextId);
            }

        } else if (node instanceof KleeneNode k) {
            assignPositions(k.getChild(), positions, endMarkers, nextId);

        } else if (node instanceof DiffNode d) {
            assignPositions(d.getLeft(), positions, endMarkers, nextId);
            assignPositions(d.getRight(), positions, endMarkers, nextId);

        } else if (SyntaxTreeBuilder.isEpsilonRegex(node)) {
            // ε — no position
        } else {
            throw new IllegalArgumentException(
                    "Nodo inesperado: " + node.getClass().getSimpleName());
        }
    }

    // =========================================================================
    // Helpers — map end positions to rule indices
    // =========================================================================

    private static void mapEndPositions(
            RegexNode node,
            List<CharNode> endMarkers,
            List<Position> positions,
            int[] leafIdx,
            Map<Integer, Integer> endPosToRule) {

        if (node instanceof CharNode cn) {
            Position p = positions.get(leafIdx[0]++);
            int markerIdx = endMarkers.indexOf(cn);
            if (markerIdx >= 0) {
                endPosToRule.put(p.getId(), markerIdx);
            }

        } else if (node instanceof CharClassNode) {
            leafIdx[0]++;

        } else if (node instanceof ConcatNode c) {
            mapEndPositions(c.getLeft(), endMarkers, positions, leafIdx, endPosToRule);
            mapEndPositions(c.getRight(), endMarkers, positions, leafIdx, endPosToRule);

        } else if (node instanceof AlternNode a) {
            mapEndPositions(a.getLeft(), endMarkers, positions, leafIdx, endPosToRule);
            if (!SyntaxTreeBuilder.isEpsilonRegex(a.getRight())) {
                mapEndPositions(a.getRight(), endMarkers, positions, leafIdx, endPosToRule);
            }

        } else if (node instanceof KleeneNode k) {
            mapEndPositions(k.getChild(), endMarkers, positions, leafIdx, endPosToRule);

        } else if (node instanceof DiffNode d) {
            mapEndPositions(d.getLeft(), endMarkers, positions, leafIdx, endPosToRule);
            mapEndPositions(d.getRight(), endMarkers, positions, leafIdx, endPosToRule);

        } else if (SyntaxTreeBuilder.isEpsilonRegex(node)) {
            // ε — skip
        }
    }

    // =========================================================================
    // Helpers — leaf atoms and alphabet (same logic as DirectDfaBuilder)
    // =========================================================================

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
                "Nodo inesperado: " + node.getClass().getSimpleName());
    }

    private static NavigableSet<Character> buildAlphabet(
            List<Position> positions,
            Map<Integer, RegexNode> leafAtom) {

        TreeSet<Character> sigma = new TreeSet<>();
        for (Position p : positions) {
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

    // =========================================================================
    // Helpers — subset-construction move function
    // =========================================================================

    private static TreeSet<Integer> move(
            Set<Integer> state,
            char a,
            FollowPosTable follow,
            Map<Integer, RegexNode> leafAtom,
            Map<Integer, Position> posById,
            Set<Integer> allEndIds) {

        TreeSet<Integer> next = new TreeSet<>();
        for (int p : state) {
            if (allEndIds.contains(p)) {
                continue;
            }
            Position pos = posById.get(p);
            if (pos == null || pos.isEnd()) {
                continue;
            }
            RegexNode leaf = leafAtom.get(p);
            if (leaf == null) {
                continue;
            }
            if (!matchesLeafInput(leaf, a)) {
                continue;
            }
            next.addAll(follow.getFollowSet(p));
        }
        return next;
    }

    private static boolean matchesLeafInput(RegexNode leaf, char a) {
        if (leaf instanceof CharNode cn) {
            char c = cn.getValue().charAt(0);
            return c != '\0' && c == a;
        }
        if (leaf instanceof CharClassNode cc) {
            return CharClassMatcher.matches(cc, a);
        }
        return false;
    }
}
