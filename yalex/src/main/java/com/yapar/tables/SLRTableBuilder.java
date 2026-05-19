package com.yapar.tables;

import com.yapar.automaton.LR0Automaton;
import com.yapar.automaton.LR0Item;
import com.yapar.automaton.LR0ItemSet;
import com.yapar.automaton.LR0AutomatonBuilder;
import com.yapar.model.Production;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Construye las tablas ACTION y GOTO del método SLR(1) a partir del
 * autómata LR(0) canónico y los conjuntos FOLLOW.
 *
 * <h2>Reglas aplicadas por estado</h2>
 * <ol>
 *   <li>Ítem [A → α • a β] con a terminal →  ACTION[i][a] = Shift(goto(i,a))</li>
 *   <li>Ítem [A → α •] con A ≠ S' →  para cada t ∈ FOLLOW(A): ACTION[i][t] = Reduce(A→α)</li>
 *   <li>Ítem [S' → S •]  →  ACTION[i][$] = Accept</li>
 *   <li>Transición sobre no-terminal B →  GOTO[i][B] = goto(i,B)</li>
 * </ol>
 *
 * <p>Los conflictos se resuelven con {@link ConflictDetector} y se acumulan en el {@link ParseResult}.
 */
public final class SLRTableBuilder {

    private final LR0Automaton automaton;
    private final Map<String, Set<String>> followSets;

    /**
     * @param automaton  resultado del Workstream B (colección canónica LR(0))
     * @param followSets FOLLOW de cada no-terminal, producidos por {@code FirstFollowCalculator}
     */
    public SLRTableBuilder(LR0Automaton automaton, Map<String, Set<String>> followSets) {
        this.automaton  = Objects.requireNonNull(automaton,  "automaton no puede ser null");
        this.followSets = Objects.requireNonNull(followSets, "followSets no puede ser null");
    }

    // ── API pública ───────────────────────────────────────────────────────────

    /** Construye y devuelve el {@link ParseResult} completo. */
    public ParseResult build() {
        Map<Integer, Map<String, SLRAction>> actionTable = new HashMap<>();
        Map<Integer, Map<String, Integer>>   gotoTable   = new HashMap<>();
        List<Conflict> conflicts = new ArrayList<>();

        for (LR0ItemSet state : automaton.states()) {
            int i = state.id();

            for (LR0Item item : state.items()) {
                Production prod = item.production();

                if (!item.isComplete()) {
                    // ── Regla 1: Shift ────────────────────────────────────────
                    String sym = item.symbolAfterDot();
                    Optional<Integer> target = automaton.gotoTarget(i, sym);
                    if (isTerminal(sym) && target.isPresent()) {
                        putAction(actionTable, conflicts, i, sym,
                                  new SLRAction.Shift(target.get()));
                    }

                } else {
                    // ── Reglas 2 y 3: Reduce / Accept ────────────────────────
                    if (isAugmentedStart(prod)) {
                        // Regla 3: Accept
                        putAction(actionTable, conflicts, i, "$", new SLRAction.Accept());
                    } else {
                        // Regla 2: Reduce para cada t ∈ FOLLOW(lhs)
                        Set<String> follow = followSets.getOrDefault(prod.lhs(), Set.of());
                        for (String t : follow) {
                            putAction(actionTable, conflicts, i, t,
                                      new SLRAction.Reduce(prod));
                        }
                    }
                }
            }

            // ── Regla 4: GOTO para no-terminales ─────────────────────────────
            Map<String, Integer> transRow = automaton.transitions()
                                                     .getOrDefault(i, Map.of());
            for (Map.Entry<String, Integer> e : transRow.entrySet()) {
                String sym = e.getKey();
                if (!isTerminalOrStart(sym)) {
                    gotoTable.computeIfAbsent(i, k -> new HashMap<>())
                             .put(sym, e.getValue());
                }
            }
        }

        ParseTable table = new ParseTable(actionTable, gotoTable);
        return new ParseResult(table, conflicts, !conflicts.isEmpty());
    }

    // ── Internos ──────────────────────────────────────────────────────────────

    /**
     * Intenta insertar {@code action} en ACTION[state][symbol].
     * Si ya hay una entrada diferente, delega en {@link ConflictDetector}.
     */
    private void putAction(Map<Integer, Map<String, SLRAction>> table,
                           List<Conflict> conflicts,
                           int state, String symbol, SLRAction action) {
        Map<String, SLRAction> row = table.computeIfAbsent(state, k -> new HashMap<>());
        SLRAction existing = row.get(symbol);

        if (existing == null) {
            row.put(symbol, action);
        } else if (!actionsEqual(existing, action)) {
            SLRAction winner = ConflictDetector.resolve(state, symbol, existing, action, conflicts);
            row.put(symbol, winner);
        }
        // Si son iguales: no hay conflicto, no hace nada
    }

    /**
     * Compara dos acciones por igualdad semántica (los records ya implementan equals).
     */
    private boolean actionsEqual(SLRAction a, SLRAction b) {
        return a.equals(b);
    }

    /** Un símbolo es terminal si aparece en los FOLLOW sets como lookahead válido,
     *  o más precisamente: si NO es un no-terminal conocido ni el símbolo aumentado. */
    private boolean isTerminal(String sym) {
        return sym != null
            && !LR0AutomatonBuilder.AUGMENTED_START.equals(sym)
            && !followSets.containsKey(sym);
    }

    /** Igual que isTerminal pero también excluye S'. */
    private boolean isTerminalOrStart(String sym) {
        return isTerminal(sym);
    }

    /** True si la producción es la augmentada S' → startSymbol. */
    private boolean isAugmentedStart(Production prod) {
        return LR0AutomatonBuilder.AUGMENTED_START.equals(prod.lhs());
    }
}
