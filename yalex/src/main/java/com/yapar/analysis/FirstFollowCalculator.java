package com.yapar.analysis;

import com.yapar.model.Grammar;
import com.yapar.model.Production;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Calcula los conjuntos {@code FIRST} y {@code FOLLOW} para una gramática libre de contexto.
 *
 * <h2>Convenciones</h2>
 * <ul>
 *   <li>La cadena vacía (ε) se representa con {@code ""}  (String vacío).</li>
 *   <li>{@code "$"} es el marcador de fin de entrada; siempre está en {@code FOLLOW(startSymbol)}.</li>
 * </ul>
 *
 * <h2>Uso</h2>
 * <pre>{@code
 *   FirstFollowCalculator calc = new FirstFollowCalculator(grammar);
 *   Map<String, Set<String>> first  = calc.getFirstSets();
 *   Map<String, Set<String>> follow = calc.getFollowSets();
 * }</pre>
 *
 * <p>La gramática que se pasa al constructor puede ser la gramática original (sin aumentar);
 * el Workstream B agrega la producción aumentada {@code S' → startSymbol} en
 * {@link com.yapar.automaton.LR0AutomatonBuilder}, no aquí.
 * Sin embargo, si la gramática ya está aumentada (tiene una producción cuyo lhs termina en "'"),
 * el cálculo sigue siendo correcto.
 */
public final class FirstFollowCalculator {

    /** Sentinel que representa ε dentro de los conjuntos FIRST. */
    public static final String EPSILON = "";

    private final Grammar grammar;

    /** {@code FIRST(X)} para cada símbolo X del vocabulario (terminales + no-terminales). */
    private final Map<String, Set<String>> firstSets = new HashMap<>();

    /** {@code FOLLOW(A)} para cada no-terminal A. */
    private final Map<String, Set<String>> followSets = new HashMap<>();

    private boolean computed = false;

    public FirstFollowCalculator(Grammar grammar) {
        this.grammar = Objects.requireNonNull(grammar, "grammar no puede ser null");
    }

    // ── API pública ───────────────────────────────────────────────────────────

    /**
     * Devuelve los conjuntos FIRST de todos los símbolos.
     * Dispara el cómputo si aún no se ha hecho.
     */
    public Map<String, Set<String>> getFirstSets() {
        ensureComputed();
        return Collections.unmodifiableMap(firstSets);
    }

    /**
     * Devuelve los conjuntos FOLLOW de todos los no-terminales.
     * Dispara el cómputo si aún no se ha hecho.
     */
    public Map<String, Set<String>> getFollowSets() {
        ensureComputed();
        return Collections.unmodifiableMap(followSets);
    }

    /**
     * FIRST de un símbolo individual.
     *
     * @param symbol nombre del símbolo
     * @return conjunto inmutable; contiene {@value #EPSILON} si el símbolo puede derivar ε
     */
    public Set<String> first(String symbol) {
        ensureComputed();
        return Collections.unmodifiableSet(firstSets.getOrDefault(symbol, Set.of()));
    }

    /**
     * FIRST de una cadena de símbolos {@code X₁ X₂ … Xₙ}.
     *
     * <ul>
     *   <li>Acumula {@code FIRST(Xᵢ) \ {ε}}.</li>
     *   <li>Incluye {@code ε} solo si todos los Xᵢ pueden derivar ε.</li>
     *   <li>Si la lista está vacía devuelve {@code {ε}}.</li>
     * </ul>
     *
     * <p>Dispara el cómputo completo si aún no se ha hecho.
     */
    public Set<String> firstOfString(List<String> symbols) {
        ensureComputed();
        return firstOfStringInternal(symbols);
    }

    /**
     * Versión interna de {@link #firstOfString} que opera directamente sobre
     * {@link #firstSets} sin pasar por {@link #ensureComputed}.
     *
     * <p>Se usa durante el cómputo de FOLLOW para evitar recursión mutua:
     * {@code computeFollow → firstOfStringInternal} (no hay ciclo).
     */
    private Set<String> firstOfStringInternal(List<String> symbols) {
        Set<String> result = new LinkedHashSet<>();

        if (symbols.isEmpty()) {
            result.add(EPSILON);
            return result;
        }

        for (String sym : symbols) {
            Set<String> fi = firstSets.getOrDefault(sym, Set.of());
            for (String t : fi) {
                if (!t.equals(EPSILON)) result.add(t);
            }
            if (!fi.contains(EPSILON)) {
                return result;   // Xᵢ no puede derivar ε → parar
            }
        }

        // Todos los símbolos pueden derivar ε
        result.add(EPSILON);
        return result;
    }

    /**
     * FOLLOW de un no-terminal.
     *
     * @param nonTerminal nombre del no-terminal
     * @return conjunto inmutable (nunca contiene ε)
     */
    public Set<String> follow(String nonTerminal) {
        ensureComputed();
        return Collections.unmodifiableSet(followSets.getOrDefault(nonTerminal, Set.of()));
    }

    // ── Cómputo interno ───────────────────────────────────────────────────────

    private void ensureComputed() {
        if (!computed) {
            computeFirst();
            computeFollow();
            computed = true;
        }
    }

    // ── FIRST ────────────────────────────────────────────────────────────────

    /**
     * Calcula FIRST para todos los terminales y no-terminales de la gramática
     * mediante iteración hasta punto fijo.
     *
     * <p>Reglas:
     * <ul>
     *   <li>Si X es terminal: {@code FIRST(X) = {X}}</li>
     *   <li>Si {@code A → ε}: {@code ε ∈ FIRST(A)}</li>
     *   <li>Si {@code A → X₁ X₂ … Xₙ}: acumular según la regla de la cadena.</li>
     * </ul>
     */
    private void computeFirst() {
        // Inicializar terminales: FIRST(t) = {t}
        for (String t : grammar.terminals()) {
            firstSets.put(t, new LinkedHashSet<>(Set.of(t)));
        }

        // Inicializar no-terminales con conjunto vacío
        for (String nt : grammar.nonTerminals()) {
            firstSets.put(nt, new LinkedHashSet<>());
        }

        // Iterar hasta punto fijo
        boolean changed = true;
        while (changed) {
            changed = false;
            for (Production p : grammar.productions()) {
                Set<String> lhsFirst = firstSets.get(p.lhs());

                if (p.isEpsilon()) {
                    // A → ε
                    changed |= lhsFirst.add(EPSILON);
                    continue;
                }

                // A → X₁ X₂ … Xₙ
                boolean allHaveEpsilon = true;
                for (String sym : p.rhs()) {
                    Set<String> symFirst = firstSets.getOrDefault(sym, Set.of());
                    for (String t : symFirst) {
                        if (!t.equals(EPSILON)) {
                            changed |= lhsFirst.add(t);
                        }
                    }
                    if (!symFirst.contains(EPSILON)) {
                        allHaveEpsilon = false;
                        break;
                    }
                }
                if (allHaveEpsilon) {
                    changed |= lhsFirst.add(EPSILON);
                }
            }
        }
    }

    // ── FOLLOW ───────────────────────────────────────────────────────────────

    /**
     * Calcula FOLLOW para todos los no-terminales hasta punto fijo.
     *
     * <p>Reglas:
     * <ul>
     *   <li>{@code $ ∈ FOLLOW(startSymbol)}</li>
     *   <li>Para cada {@code B → α A β}: {@code FOLLOW(A) ⊇ FIRST(β) \ {ε}}</li>
     *   <li>Para cada {@code B → α A β} con {@code β ⟹* ε} (o β vacío):
     *       {@code FOLLOW(A) ⊇ FOLLOW(B)}</li>
     * </ul>
     */
    private void computeFollow() {
        // Inicializar todos los conjuntos FOLLOW vacíos
        for (String nt : grammar.nonTerminals()) {
            followSets.put(nt, new LinkedHashSet<>());
        }

        // $ ∈ FOLLOW(startSymbol)
        followSets.get(grammar.startSymbol()).add("$");

        // Iterar hasta punto fijo
        boolean changed = true;
        while (changed) {
            changed = false;
            for (Production p : grammar.productions()) {
                List<String> rhs = p.rhs();
                for (int i = 0; i < rhs.size(); i++) {
                    String sym = rhs.get(i);
                    if (!grammar.isNonTerminal(sym)) continue;

                    Set<String> followA = followSets.get(sym);

                    // β = rhs[i+1 .. end]
                    List<String> beta = rhs.subList(i + 1, rhs.size());
                    // Usar la versión interna para evitar recursión mutua con ensureComputed
                    Set<String> firstBeta = firstOfStringInternal(beta);

                    // FOLLOW(A) ⊇ FIRST(β) \ {ε}
                    for (String t : firstBeta) {
                        if (!t.equals(EPSILON)) {
                            changed |= followA.add(t);
                        }
                    }

                    // Si β ⟹* ε (o β está vacío) → FOLLOW(A) ⊇ FOLLOW(B)
                    if (firstBeta.contains(EPSILON)) {
                        Set<String> followB = followSets.getOrDefault(p.lhs(), Set.of());
                        changed |= followA.addAll(followB);
                    }
                }
            }
        }
    }
}
