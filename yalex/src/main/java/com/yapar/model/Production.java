package com.yapar.model;

import java.util.List;
import java.util.Objects;

/**
 * Una producción de la gramática: {@code lhs → rhs} con acción semántica opcional.
 * El campo {@code id} es el índice 0-based dentro de la gramática construida por A.
 * El Workstream B inserta la producción aumentada {@code S' → startSymbol} como id=0
 * y renumera las demás.
 */
public record Production(int id, String lhs, List<String> rhs, String action) {

    public Production {
        Objects.requireNonNull(lhs, "lhs no puede ser null");
        Objects.requireNonNull(rhs, "rhs no puede ser null");
        Objects.requireNonNull(action, "action no puede ser null");
        if (lhs.isBlank()) throw new IllegalArgumentException("lhs no puede estar vacío");
        rhs = List.copyOf(rhs);
    }

    /** Una producción es ε si su rhs está vacío. */
    public boolean isEpsilon() {
        return rhs.isEmpty();
    }

    @Override
    public String toString() {
        String body = rhs.isEmpty() ? "ε" : String.join(" ", rhs);
        return id + ": " + lhs + " → " + body;
    }
}
