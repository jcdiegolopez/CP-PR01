package com.yapar.automaton;

import com.yapar.model.Production;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Resultado final del Workstream B: autómata LR(0) canónico.
 *
 * <p>Contiene:
 * <ul>
 *   <li>La lista de estados ({@link LR0ItemSet}), indexada por su id.</li>
 *   <li>La tabla de transiciones: {@code goto(stateId, symbol) → stateId}.</li>
 *   <li>Las producciones de la gramática <em>aumentada</em> (S' → startSymbol al frente).</li>
 *   <li>El id del estado inicial (siempre 0).</li>
 * </ul>
 *
 * <h2>Contrato hacia Workstream C</h2>
 * <pre>{@code
 *   LR0Automaton automaton = new LR0AutomatonBuilder(grammar).build();
 *
 *   // Consultar goto
 *   Optional<Integer> target = automaton.gotoTarget(0, "E");
 *
 *   // Producciones augmentadas (id=0 es S' → startSymbol)
 *   List<Production> prods = automaton.augmentedProductions();
 * }</pre>
 */
public record LR0Automaton(
        List<LR0ItemSet>                   states,
        Map<Integer, Map<String, Integer>> transitions,
        List<Production>                   augmentedProductions,
        int                                initialState) {

    public LR0Automaton {
        Objects.requireNonNull(states,               "states no puede ser null");
        Objects.requireNonNull(transitions,           "transitions no puede ser null");
        Objects.requireNonNull(augmentedProductions,  "augmentedProductions no puede ser null");
        if (states.isEmpty()) throw new IllegalArgumentException("El autómata no tiene estados");
        states               = List.copyOf(states);
        augmentedProductions = List.copyOf(augmentedProductions);
        // transitions ya debe ser unmodifiable (lo garantiza el builder)
    }

    // ── Consultas ─────────────────────────────────────────────────────────────

    /**
     * El estado con el id dado.
     *
     * @throws IndexOutOfBoundsException si {@code id} no es válido
     */
    public LR0ItemSet state(int id) {
        return states.get(id);
    }

    /**
     * {@code goto(stateId, symbol)} → id del estado destino, o vacío si no existe
     * la transición.
     */
    public Optional<Integer> gotoTarget(int stateId, String symbol) {
        Map<String, Integer> row = transitions.getOrDefault(stateId, Map.of());
        return Optional.ofNullable(row.get(symbol));
    }

    /**
     * La producción augmentada {@code S' → startSymbol} (siempre id=0).
     */
    public Production startProduction() {
        return augmentedProductions.get(0);
    }

    // ── Estadísticas ──────────────────────────────────────────────────────────

    /** Número total de estados en la colección canónica. */
    public int stateCount() {
        return states.size();
    }

    /** Número total de transiciones (aristas en el autómata). */
    public int transitionCount() {
        int total = 0;
        for (Map<String, Integer> row : transitions.values()) {
            total += row.size();
        }
        return total;
    }

    // ── toString ──────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LR(0) Automaton [")
          .append(stateCount()).append(" estados, ")
          .append(transitionCount()).append(" transiciones]\n");
        for (LR0ItemSet s : states) {
            sb.append(s).append("\n");
            Map<String, Integer> row = transitions.getOrDefault(s.id(), Map.of());
            for (Map.Entry<String, Integer> e : row.entrySet()) {
                sb.append("  goto(").append(s.id()).append(", ").append(e.getKey())
                  .append(") = ").append(e.getValue()).append("\n");
            }
        }
        return sb.toString();
    }
}
