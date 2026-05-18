package com.yapar.automaton;

import com.yapar.model.Grammar;
import com.yapar.model.Production;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Un estado del autómata LR(0) canónico: un conjunto cerrado de ítems LR(0).
 *
 * <h2>Creación</h2>
 * <ul>
 *   <li>{@link #of(int, Collection, Grammar)} — para uso externo con gramática original.</li>
 *   <li>Constructor package-private {@code LR0ItemSet(int, Set)} — para {@link LR0AutomatonBuilder},
 *       que ya tiene el conjunto cerrado calculado internamente.</li>
 * </ul>
 *
 * <h2>Algoritmo closure</h2>
 * Para cada ítem {@code [A → α • B β]} en el conjunto:
 * agregar {@code [B → • γ]} para toda producción {@code B → γ} de la gramática;
 * repetir hasta punto fijo.
 */
public final class LR0ItemSet {

    private final int id;
    private final Set<LR0Item> items;

    // ── Constructores ─────────────────────────────────────────────────────────

    /**
     * Constructor package-private: recibe el conjunto ya cerrado.
     * Usado por {@link LR0AutomatonBuilder} que maneja su propia lógica de closure.
     */
    LR0ItemSet(int id, Set<LR0Item> closedItems) {
        this.id    = id;
        this.items = Collections.unmodifiableSet(new LinkedHashSet<>(
                Objects.requireNonNull(closedItems, "closedItems no puede ser null")));
    }

    // ── Fábrica pública ───────────────────────────────────────────────────────

    /**
     * Crea un nuevo estado aplicando closure al kernel {@code kernelItems}.
     *
     * @param id          identificador único del estado (0-based)
     * @param kernelItems ítems base (antes de closure)
     * @param grammar     gramática (sin aumentar) para obtener producciones
     */
    public static LR0ItemSet of(int id, Collection<LR0Item> kernelItems, Grammar grammar) {
        Objects.requireNonNull(kernelItems, "kernelItems no puede ser null");
        Objects.requireNonNull(grammar,     "grammar no puede ser null");
        Set<LR0Item> closed = closure(kernelItems, grammar);
        return new LR0ItemSet(id, closed);
    }

    // ── Closure ───────────────────────────────────────────────────────────────

    /**
     * Calcula el cierre de un conjunto de ítems usando la gramática dada.
     * No modifica la colección recibida; devuelve un conjunto nuevo.
     */
    public static Set<LR0Item> closure(Collection<LR0Item> kernel, Grammar grammar) {
        Set<LR0Item> result = new LinkedHashSet<>(kernel);
        boolean changed = true;
        while (changed) {
            changed = false;
            for (LR0Item item : new LinkedHashSet<>(result)) {
                String sym = item.symbolAfterDot();
                if (sym == null || !grammar.isNonTerminal(sym)) continue;
                for (Production prod : grammar.productionsFor(sym)) {
                    changed |= result.add(new LR0Item(prod, 0));
                }
            }
        }
        return result;
    }

    // ── Consultas ─────────────────────────────────────────────────────────────

    public int id() {
        return id;
    }

    public Set<LR0Item> items() {
        return items;
    }

    // ── Igualdad basada en contenido, no en id ────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LR0ItemSet other)) return false;
        return items.equals(other.items);
    }

    @Override
    public int hashCode() {
        return items.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("State ").append(id).append(" {\n");
        for (LR0Item item : items) {
            sb.append("  ").append(item).append("\n");
        }
        sb.append("}");
        return sb.toString();
    }
}
