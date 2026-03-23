package com.yalex.automata.dfa;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeSet;

/**
 * Autómata finito determinista: estados, estado inicial, transiciones y alfabeto.
 */
public final class DFA {

    private final List<DFAState> states;
    private final int initialStateId;
    private final Map<Integer, Map<Character, Integer>> transitions;
    private final NavigableSet<Character> alphabet;

    public DFA(
            List<DFAState> states,
            int initialStateId,
            Map<Integer, Map<Character, Integer>> transitions,
            NavigableSet<Character> alphabet) {
        this.states = List.copyOf(Objects.requireNonNull(states, "states"));
        this.initialStateId = initialStateId;
        this.transitions = Map.copyOf(deepCopyTrans(transitions));
        this.alphabet = Collections.unmodifiableNavigableSet(
                new TreeSet<>(Objects.requireNonNull(alphabet, "alphabet")));
    }

    private static Map<Integer, Map<Character, Integer>> deepCopyTrans(
            Map<Integer, Map<Character, Integer>> t) {
        Map<Integer, Map<Character, Integer>> out = new java.util.HashMap<>();
        for (var e : t.entrySet()) {
            out.put(e.getKey(), Map.copyOf(e.getValue()));
        }
        return out;
    }

    public List<DFAState> getStates() {
        return states;
    }

    public DFAState getState(int id) {
        return states.get(id);
    }

    public int getInitialStateId() {
        return initialStateId;
    }

    /** Transición definida; si no hay arista, devuelve {@code -1}. */
    public int transition(int stateId, char symbol) {
        Map<Character, Integer> row = transitions.get(stateId);
        if (row == null) {
            return -1;
        }
        return row.getOrDefault(symbol, -1);
    }

    public Map<Integer, Map<Character, Integer>> getTransitionTable() {
        return transitions;
    }

    public NavigableSet<Character> getAlphabet() {
        return alphabet;
    }

    public int getStateCount() {
        return states.size();
    }
}
