package com.yapar.tables;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Tablas ACTION y GOTO de un parser SLR(1).
 *
 * <ul>
 *   <li>{@code ACTION[state][terminal]} → qué hacer al ver ese terminal.</li>
 *   <li>{@code GOTO[state][nonTerminal]} → estado al que ir tras reducir.</li>
 * </ul>
 *
 * Las celdas vacías de ACTION devuelven {@link SLRAction.Error};
 * las celdas vacías de GOTO devuelven {@link Optional#empty()}.
 */
public final class ParseTable {

    /** ACTION[stateId][terminal] */
    private final Map<Integer, Map<String, SLRAction>> actionTable;

    /** GOTO[stateId][nonTerminal] */
    private final Map<Integer, Map<String, Integer>> gotoTable;

    ParseTable(Map<Integer, Map<String, SLRAction>> actionTable,
               Map<Integer, Map<String, Integer>> gotoTable) {
        this.actionTable = Objects.requireNonNull(actionTable);
        this.gotoTable   = Objects.requireNonNull(gotoTable);
    }

    // ── Consultas ─────────────────────────────────────────────────────────────

    /**
     * Acción para el par (estado, terminal). Nunca devuelve null;
     * devuelve {@link SLRAction.Error} si la celda está vacía.
     */
    public SLRAction action(int state, String terminal) {
        Objects.requireNonNull(terminal, "terminal no puede ser null");
        Map<String, SLRAction> row = actionTable.getOrDefault(state, Map.of());
        return row.getOrDefault(terminal, new SLRAction.Error());
    }

    /**
     * Estado destino para (estado, no-terminal) tras una reducción.
     * Devuelve vacío si no hay entrada en GOTO.
     */
    public Optional<Integer> gotoState(int state, String nonTerminal) {
        Objects.requireNonNull(nonTerminal, "nonTerminal no puede ser null");
        Map<String, Integer> row = gotoTable.getOrDefault(state, Map.of());
        return Optional.ofNullable(row.get(nonTerminal));
    }

    // ── Acceso a las tablas completas (para GUI y serialización) ──────────────

    public Map<Integer, Map<String, SLRAction>> actionTable() {
        return Collections.unmodifiableMap(actionTable);
    }

    public Map<Integer, Map<String, Integer>> gotoTable() {
        return Collections.unmodifiableMap(gotoTable);
    }
}
