package com.yalex.automata.syntax;

/**
 * Representa una hoja numerada del árbol sintáctico aumentado.
 *
 * <p>En el método directo para construcción de DFA, cada hoja del árbol
 * corresponde a una ocurrencia de un símbolo en la expresión regular.
 * El árbol aumentado tiene una hoja extra (el <em>marcador final</em>) que
 * se usa para detectar cuándo una cadena ha sido aceptada.
 *
 * <p>Los IDs son 1-based y se asignan en orden de recorrido left-to-right.
 */
public final class Position {

    private final int id;        // número único de posición (1-based)
    private final char symbol;   // símbolo en esa hoja
    private final boolean isEnd; // true si es la posición del marcador final

    /**
     * Crea una posición regular (hoja de símbolo).
     *
     * @param id     identificador único (1-based)
     * @param symbol carácter que representa esta hoja
     */
    public Position(int id, char symbol) {
        this(id, symbol, false);
    }

    /**
     * Crea una posición, opcionalmente marcándola como posición final.
     *
     * @param id     identificador único (1-based)
     * @param symbol carácter que representa esta hoja (convención: {@code '\0'} para el marcador)
     * @param isEnd  {@code true} si esta es la posición del marcador final
     */
    public Position(int id, char symbol, boolean isEnd) {
        if (id < 1) {
            throw new IllegalArgumentException("id debe ser >= 1, se recibió: " + id);
        }
        this.id = id;
        this.symbol = symbol;
        this.isEnd = isEnd;
    }

    /** Identificador único de esta posición (1-based). */
    public int getId() {
        return id;
    }

    /** Símbolo asociado a esta hoja. {@code '\0'} para el marcador final. */
    public char getSymbol() {
        return symbol;
    }

    /** {@code true} si esta posición es el marcador final del árbol aumentado. */
    public boolean isEnd() {
        return isEnd;
    }

    @Override
    public String toString() {
        return isEnd ? "Pos#" + id + "(END)"
                     : "Pos#" + id + "('" + (symbol == '\0' ? "\\0" : symbol) + "')";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Position other)) return false;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
