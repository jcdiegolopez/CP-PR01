package com.yapar.model;

import java.util.Objects;

/**
 * Un símbolo de la gramática: terminal o no-terminal.
 * Los terminales vienen de las declaraciones {@code %token}.
 * Los no-terminales son los lhs de las producciones.
 */
public record GrammarSymbol(String name, boolean terminal) {

    public GrammarSymbol {
        Objects.requireNonNull(name, "name no puede ser null");
        if (name.isBlank()) throw new IllegalArgumentException("name no puede estar vacío");
    }

    public static GrammarSymbol terminal(String name) {
        return new GrammarSymbol(name, true);
    }

    public static GrammarSymbol nonTerminal(String name) {
        return new GrammarSymbol(name, false);
    }

    public boolean isTerminal()    { return terminal; }
    public boolean isNonTerminal() { return !terminal; }

    @Override
    public String toString() {
        return name;
    }
}
