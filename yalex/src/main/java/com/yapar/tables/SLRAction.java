package com.yapar.tables;

import com.yapar.model.Production;

import java.util.Objects;

/**
 * Acción en la tabla ACTION de un parser SLR(1).
 *
 * Variantes:
 *   Shift(state)      — desplazar y pasar al estado indicado
 *   Reduce(prod)      — reducir usando la producción indicada
 *   Accept            — entrada aceptada (ítem [S' → S •] con $)
 *   Error             — celda vacía (error de sintaxis)
 */
public sealed interface SLRAction
        permits SLRAction.Shift, SLRAction.Reduce, SLRAction.Accept, SLRAction.Error {

    // ── Variantes ─────────────────────────────────────────────────────────────

    record Shift(int state) implements SLRAction {
        public Shift {
            if (state < 0) throw new IllegalArgumentException("state debe ser >= 0");
        }

        @Override
        public String toString() {
            return "s" + state;
        }
    }

    record Reduce(Production production) implements SLRAction {
        public Reduce {
            Objects.requireNonNull(production, "production no puede ser null");
        }

        @Override
        public String toString() {
            return "r" + production.id() + " (" + production.lhs() + " → " +
                   (production.rhs().isEmpty() ? "ε" : String.join(" ", production.rhs())) + ")";
        }
    }

    record Accept() implements SLRAction {
        @Override
        public String toString() {
            return "acc";
        }
    }

    record Error() implements SLRAction {
        @Override
        public String toString() {
            return "";
        }
    }
}
