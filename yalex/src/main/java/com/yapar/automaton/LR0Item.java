package com.yapar.automaton;

import com.yapar.model.Production;

import java.util.Objects;

/**
 * Un ítem LR(0): una producción con un punto (•) en alguna posición del cuerpo.
 *
 * <p>Ejemplo para la producción {@code E → E PLUS T} (id=0):
 * <ul>
 *   <li>dotPosition=0 → {@code [E → • E PLUS T]}</li>
 *   <li>dotPosition=1 → {@code [E → E • PLUS T]}</li>
 *   <li>dotPosition=3 → {@code [E → E PLUS T •]}  (ítem de reducción)</li>
 * </ul>
 *
 * <p>Los ítems son value objects; la igualdad está basada en {@code production.id()}
 * y {@code dotPosition}, por lo que pueden usarse correctamente en {@link java.util.Set}.
 */
public record LR0Item(Production production, int dotPosition) {

    public LR0Item {
        Objects.requireNonNull(production, "production no puede ser null");
        if (dotPosition < 0 || dotPosition > production.rhs().size()) {
            throw new IllegalArgumentException(
                "dotPosition=" + dotPosition + " fuera de rango [0, " + production.rhs().size() + "]");
        }
    }

    // ── Consultas de estado ───────────────────────────────────────────────────

    /**
     * {@code true} si el punto está al final del cuerpo — ítem de reducción.
     * {@code [A → α •]}
     */
    public boolean isComplete() {
        return dotPosition == production.rhs().size();
    }

    /**
     * El símbolo inmediatamente a la derecha del punto, o {@code null} si el ítem es completo.
     * {@code [A → α • X β]}  →  devuelve X
     */
    public String symbolAfterDot() {
        if (isComplete()) return null;
        return production.rhs().get(dotPosition);
    }

    /**
     * Devuelve el ítem que resulta de avanzar el punto un paso a la derecha.
     * Lanza {@link IllegalStateException} si el ítem ya está completo.
     */
    public LR0Item advance() {
        if (isComplete()) {
            throw new IllegalStateException("No se puede avanzar: el ítem ya está completo: " + this);
        }
        return new LR0Item(production, dotPosition + 1);
    }

    // ── Object ────────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        sb.append(production.lhs()).append(" →");
        var rhs = production.rhs();
        for (int i = 0; i <= rhs.size(); i++) {
            if (i == dotPosition) sb.append(" •");
            if (i < rhs.size())  sb.append(" ").append(rhs.get(i));
        }
        sb.append("]");
        return sb.toString();
    }
}
