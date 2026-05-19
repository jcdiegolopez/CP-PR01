package com.yapar.tables;

import java.util.List;

/**
 * Resuelve conflictos en la tabla ACTION y los registra.
 *
 * Reglas de desempate estándar:
 *   Shift/reduce  → gana Shift
 *   Reduce/reduce → gana la producción de menor id (la que aparece primero en el .yalp)
 */
public final class ConflictDetector {

    private ConflictDetector() {}

    /**
     * Decide qué acción colocar en la tabla cuando ya existe una entrada.
     * Registra el conflicto en {@code collector} y devuelve la acción ganadora.
     *
     * @param state     estado donde ocurre el conflicto
     * @param symbol    símbolo (terminal) que lo provoca
     * @param existing  acción ya presente en la celda
     * @param incoming  acción nueva que quiere entrar
     * @param collector lista donde se acumula el conflicto
     * @return la acción que debe quedar en la celda
     */
    public static SLRAction resolve(int state, String symbol,
                                    SLRAction existing, SLRAction incoming,
                                    List<Conflict> collector) {
        Conflict.ConflictType type = classifyType(existing, incoming);
        collector.add(new Conflict(state, symbol, existing, incoming, type));
        return winner(existing, incoming, type);
    }

    /**
     * Imprime todos los conflictos a stderr en el formato estándar de YAPar.
     */
    public static void report(List<Conflict> conflicts) {
        for (Conflict c : conflicts) {
            System.err.println(c);
        }
    }

    // ── Internos ──────────────────────────────────────────────────────────────

    private static Conflict.ConflictType classifyType(SLRAction a, SLRAction b) {
        boolean oneIsShift = (a instanceof SLRAction.Shift) || (b instanceof SLRAction.Shift);
        return oneIsShift ? Conflict.ConflictType.SHIFT_REDUCE : Conflict.ConflictType.REDUCE_REDUCE;
    }

    private static SLRAction winner(SLRAction existing, SLRAction incoming,
                                    Conflict.ConflictType type) {
        if (type == Conflict.ConflictType.SHIFT_REDUCE) {
            // Shift siempre gana
            return (existing instanceof SLRAction.Shift) ? existing : incoming;
        }
        // Reduce/reduce: gana la producción de menor id
        SLRAction.Reduce r1 = (SLRAction.Reduce) existing;
        SLRAction.Reduce r2 = (SLRAction.Reduce) incoming;
        return r1.production().id() <= r2.production().id() ? r1 : r2;
    }
}
