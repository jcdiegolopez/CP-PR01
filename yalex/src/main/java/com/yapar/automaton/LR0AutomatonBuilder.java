package com.yapar.automaton;

import com.yapar.model.Grammar;
import com.yapar.model.Production;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Construye la colección canónica de estados LR(0) y la tabla de transiciones
 * (función {@code goto}) a partir de una gramática libre de contexto.
 *
 * <h2>Proceso</h2>
 * <ol>
 *   <li>Aumentar la gramática: agregar producción {@code S' → startSymbol}.</li>
 *   <li>Estado 0 = {@code closure({ [S' → • startSymbol] })}.</li>
 *   <li>BFS: para cada estado y cada símbolo del vocabulario, calcular
 *       {@code goto(I, X) = closure({ [A → αX • β] | [A → α • Xβ] ∈ I })}.</li>
 *   <li>Registrar estados nuevos; guardar las transiciones.</li>
 * </ol>
 *
 * <h2>Uso</h2>
 * <pre>{@code
 *   LR0Automaton automaton = new LR0AutomatonBuilder(grammar).build();
 * }</pre>
 */
public final class LR0AutomatonBuilder {

    /** Nombre reservado para el símbolo inicial de la gramática aumentada. */
    public static final String AUGMENTED_START = "S'";

    private final Grammar grammar;

    /** Producciones de la gramática aumentada (S' → start al índice 0). */
    private final List<Production> augmentedProductions;

    /** La producción S' → startSymbol (id=0). */
    private final Production startProduction;

    public LR0AutomatonBuilder(Grammar grammar) {
        this.grammar = Objects.requireNonNull(grammar, "grammar no puede ser null");
        this.augmentedProductions = buildAugmentedProductions(grammar);
        this.startProduction = augmentedProductions.get(0);
    }

    // ── API pública ───────────────────────────────────────────────────────────

    /**
     * Construye y devuelve el {@link LR0Automaton} completo.
     */
    public LR0Automaton build() {
        List<LR0ItemSet>                   states      = new ArrayList<>();
        Map<Integer, Map<String, Integer>> transitions = new HashMap<>();

        // Estado 0: closure({ [S' → • startSymbol] })
        LR0Item seed    = new LR0Item(startProduction, 0);
        LR0ItemSet s0   = buildState(0, Set.of(seed));
        states.add(s0);

        // BFS sobre los estados por descubrir
        int cursor = 0;
        while (cursor < states.size()) {
            LR0ItemSet current = states.get(cursor++);
            Map<String, Integer> row = new HashMap<>();

            for (String symbol : allVocabulary()) {
                Set<LR0Item> gotoKernel = computeGotoKernel(current, symbol);
                if (gotoKernel.isEmpty()) continue;

                int targetId = findOrCreate(states, gotoKernel);
                row.put(symbol, targetId);
            }

            if (!row.isEmpty()) {
                transitions.put(current.id(), Collections.unmodifiableMap(row));
            }
        }

        return new LR0Automaton(
                Collections.unmodifiableList(states),
                Collections.unmodifiableMap(transitions),
                augmentedProductions,
                0);
    }

    /**
     * La producción aumentada {@code S' → startSymbol}.
     */
    public Production getStartProduction() {
        return startProduction;
    }

    /**
     * Lista completa de producciones augmentadas (S' → S al frente, ids renumerados).
     */
    public List<Production> getAugmentedProductions() {
        return augmentedProductions;
    }

    // ── Algoritmos internos ───────────────────────────────────────────────────

    /**
     * Kernel de {@code goto(I, X)}: ítems con el punto avanzado sobre X.
     */
    private Set<LR0Item> computeGotoKernel(LR0ItemSet state, String symbol) {
        Set<LR0Item> kernel = new LinkedHashSet<>();
        for (LR0Item item : state.items()) {
            if (symbol.equals(item.symbolAfterDot())) {
                kernel.add(item.advance());
            }
        }
        return kernel;
    }

    /**
     * Busca un estado con el mismo cierre que {@code kernel}.
     * Si no existe, lo crea y lo añade a {@code states}.
     *
     * @return id del estado encontrado o creado
     */
    private int findOrCreate(List<LR0ItemSet> states, Set<LR0Item> kernel) {
        // Calcular el cierre del nuevo kernel para comparar
        Set<LR0Item> closed = closure(kernel);
        for (LR0ItemSet existing : states) {
            if (existing.items().equals(closed)) {
                return existing.id();
            }
        }
        int newId = states.size();
        states.add(new LR0ItemSet(newId, closed));
        return newId;
    }

    /**
     * Cierre de un conjunto de ítems usando la gramática aumentada.
     */
    private Set<LR0Item> closure(Set<LR0Item> kernel) {
        Set<LR0Item> result = new LinkedHashSet<>(kernel);
        boolean changed = true;
        while (changed) {
            changed = false;
            for (LR0Item item : new LinkedHashSet<>(result)) {
                String sym = item.symbolAfterDot();
                if (sym == null) continue;
                for (Production prod : productionsForSym(sym)) {
                    changed |= result.add(new LR0Item(prod, 0));
                }
            }
        }
        return result;
    }

    /** Crea un {@link LR0ItemSet} a partir de un kernel (aplica closure). */
    private LR0ItemSet buildState(int id, Set<LR0Item> kernel) {
        return new LR0ItemSet(id, closure(kernel));
    }

    /**
     * Producciones de la gramática augmentada cuyo lhs es {@code sym}.
     * Incluye la producción S' → startSymbol.
     */
    private List<Production> productionsForSym(String sym) {
        List<Production> result = new ArrayList<>();
        for (Production p : augmentedProductions) {
            if (p.lhs().equals(sym)) result.add(p);
        }
        return result;
    }

    /**
     * Vocabulario completo: S' + no-terminales + terminales.
     */
    private Set<String> allVocabulary() {
        Set<String> syms = new LinkedHashSet<>();
        syms.add(AUGMENTED_START);
        syms.addAll(grammar.nonTerminals());
        syms.addAll(grammar.terminals());
        return syms;
    }

    // ── Gramática aumentada ───────────────────────────────────────────────────

    private static List<Production> buildAugmentedProductions(Grammar g) {
        List<Production> list = new ArrayList<>();
        // Producción 0: S' → startSymbol
        list.add(new Production(0, AUGMENTED_START, List.of(g.startSymbol()), ""));
        // Renumerar producciones originales desde 1
        int id = 1;
        for (Production orig : g.productions()) {
            list.add(new Production(id++, orig.lhs(), orig.rhs(), orig.action()));
        }
        return Collections.unmodifiableList(list);
    }
}
