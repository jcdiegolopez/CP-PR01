package com.yapar.tables;

import java.util.Objects;

/**
 * Un conflicto en la tabla ACTION: dos acciones distintas para el mismo par (estado, símbolo).
 */
public record Conflict(
        int state,
        String symbol,
        SLRAction existing,
        SLRAction incoming,
        ConflictType type) {

    public enum ConflictType {
        SHIFT_REDUCE,
        REDUCE_REDUCE
    }

    public Conflict {
        Objects.requireNonNull(symbol,   "symbol no puede ser null");
        Objects.requireNonNull(existing, "existing no puede ser null");
        Objects.requireNonNull(incoming, "incoming no puede ser null");
        Objects.requireNonNull(type,     "type no puede ser null");
    }

    @Override
    public String toString() {
        return "[YAPAR] ADVERTENCIA " + type + " en estado " + state +
               ", símbolo '" + symbol + "': " + existing + " vs " + incoming;
    }
}
