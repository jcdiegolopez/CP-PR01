package com.yapar.model;

import com.yapar.yalp.RawAlternative;
import com.yapar.yalp.RawProduction;
import com.yapar.yalp.YalpFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Gramática libre de contexto construida a partir de un {@link YalpFile}.
 *
 * <p>El símbolo inicial es el lhs de la <em>primera</em> producción en el .yalp.
 * El terminal {@code "$"} (fin de entrada) se agrega automáticamente al conjunto de terminales.
 *
 * <p>Esta gramática <b>no</b> está aumentada; el Workstream B agrega la producción
 * {@code S' → startSymbol} cuando construye el autómata LR(0).
 */
public record Grammar(
        String startSymbol,
        List<Production> productions,
        Set<String> terminals,
        Set<String> nonTerminals) {

    public Grammar {
        Objects.requireNonNull(startSymbol, "startSymbol no puede ser null");
        Objects.requireNonNull(productions, "productions no puede ser null");
        Objects.requireNonNull(terminals, "terminals no puede ser null");
        Objects.requireNonNull(nonTerminals, "nonTerminals no puede ser null");
        if (startSymbol.isBlank()) throw new IllegalArgumentException("startSymbol no puede estar vacío");
        productions  = List.copyOf(productions);
        terminals    = Collections.unmodifiableSet(new LinkedHashSet<>(terminals));
        nonTerminals = Collections.unmodifiableSet(new LinkedHashSet<>(nonTerminals));
    }

    // ── Factory ──────────────────────────────────────────────────────────────

    public static Grammar build(YalpFile yalpFile) {
        Objects.requireNonNull(yalpFile, "yalpFile no puede ser null");

        if (yalpFile.getProductions().isEmpty()) {
            throw new IllegalArgumentException("El archivo .yalp no tiene producciones");
        }

        // Terminales declarados con %token + el marcador de fin "$"
        Set<String> terminals = new LinkedHashSet<>(yalpFile.getDeclaredTokens());
        terminals.add("$");

        // No-terminales: todos los lhs de producciones
        Set<String> nonTerminals = new LinkedHashSet<>();
        for (RawProduction raw : yalpFile.getProductions()) {
            nonTerminals.add(raw.lhs());
        }

        // Un símbolo no puede ser terminal y no-terminal al mismo tiempo
        for (String nt : nonTerminals) {
            if (terminals.contains(nt)) {
                throw new IllegalArgumentException(
                    "El símbolo '" + nt + "' aparece en %token y también como lhs de una producción");
            }
        }

        // Construir producciones y validar cada símbolo del rhs
        List<Production> productions = new ArrayList<>();
        int id = 0;
        for (RawProduction raw : yalpFile.getProductions()) {
            for (RawAlternative alt : raw.alternatives()) {
                for (String sym : alt.symbols()) {
                    if (!terminals.contains(sym) && !nonTerminals.contains(sym)) {
                        throw new IllegalArgumentException(
                            "Símbolo '" + sym + "' en la producción '" + raw.lhs() +
                            " → ...' no está declarado en %token ni aparece como lhs");
                    }
                }
                productions.add(new Production(id++, raw.lhs(), alt.symbols(), alt.action()));
            }
        }

        String startSymbol = yalpFile.getProductions().get(0).lhs();

        return new Grammar(startSymbol, productions,
                           Collections.unmodifiableSet(terminals),
                           Collections.unmodifiableSet(nonTerminals));
    }

    // ── Consultas de conveniencia ─────────────────────────────────────────────

    /** Todas las producciones cuyo lhs es {@code nonTerminal}. */
    public List<Production> productionsFor(String nonTerminal) {
        List<Production> result = new ArrayList<>();
        for (Production p : productions) {
            if (p.lhs().equals(nonTerminal)) result.add(p);
        }
        return result;
    }

    public boolean isTerminal(String symbol)    { return terminals.contains(symbol); }
    public boolean isNonTerminal(String symbol) { return nonTerminals.contains(symbol); }

    /** Todos los símbolos de la gramática (terminales ∪ no-terminales). */
    public Set<String> allSymbols() {
        Set<String> all = new LinkedHashSet<>(nonTerminals);
        all.addAll(terminals);
        return Collections.unmodifiableSet(all);
    }
}
