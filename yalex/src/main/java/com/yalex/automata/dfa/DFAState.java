package com.yalex.automata.dfa;

import java.util.Collections;
import java.util.Set;

/**
 * Estado del DFA construido por el método directo: conjunto de posiciones del árbol sintáctico.
 */
public final class DFAState {

    private final int id;
    private final Set<Integer> positions;
    private final boolean accepting;

    public DFAState(int id, Set<Integer> positions, boolean accepting) {
        this.id = id;
        this.positions = Collections.unmodifiableSet(positions);
        this.accepting = accepting;
    }

    public int getId() {
        return id;
    }

    /** Conjunto de IDs de posición (followpos / firstpos) que identifica este estado. */
    public Set<Integer> getPositions() {
        return positions;
    }

    public boolean isAccepting() {
        return accepting;
    }

    @Override
    public String toString() {
        return "S" + id + (accepting ? "*" : "") + positions;
    }
}
