package com.yalex.automata.analysis;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Tabla {@code followpos}: para cada ID de posición, el conjunto de posiciones que pueden seguirle.
 */
public final class FollowPosTable {

    private final Map<Integer, Set<Integer>> follow;

    public FollowPosTable(Map<Integer, Set<Integer>> follow) {
        Map<Integer, Set<Integer>> copy = new TreeMap<>();
        for (var e : follow.entrySet()) {
            copy.put(e.getKey(), Collections.unmodifiableSet(new TreeSet<>(e.getValue())));
        }
        this.follow = Collections.unmodifiableMap(copy);
    }

    /** Conjunto followpos para {@code positionId}, o vacío si no hay entradas. */
    public Set<Integer> getFollowSet(int positionId) {
        return follow.getOrDefault(positionId, Set.of());
    }

    /** Vista inmutable de toda la tabla (IDs de posición ordenados). */
    public Map<Integer, Set<Integer>> asMap() {
        return follow;
    }
}
