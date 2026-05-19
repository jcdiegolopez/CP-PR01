package com.yapar.tables;

import java.util.List;
import java.util.Objects;

/**
 * Resultado completo de la construcción de tablas SLR(1):
 * tablas ACTION/GOTO + lista de conflictos detectados.
 */
public record ParseResult(
        ParseTable table,
        List<Conflict> conflicts,
        boolean hasConflicts) {

    public ParseResult {
        Objects.requireNonNull(table,     "table no puede ser null");
        Objects.requireNonNull(conflicts, "conflicts no puede ser null");
        conflicts = List.copyOf(conflicts);
    }
}
