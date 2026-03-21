package com.yalex.automata.syntax;

import java.util.List;
import java.util.Objects;

import com.yalex.regex.node.RegexNode;

/**
 * Árbol sintáctico aumentado listo para ser analizado por el método directo.
 *
 * <p>El árbol aumentado es siempre de la forma {@code r · #} donde:
 * <ul>
 *   <li>{@code r} es la expresión regular original (ya con Plus/Optional/Wildcard expandidos)
 *   <li>{@code #} es el marcador final: un nodo hoja especial cuya posición tiene {@code isEnd = true}
 * </ul>
 *
 * <p>Todas las hojas del árbol están numeradas (1-based, left-to-right) y accesibles
 * a través de {@link #getPositions()}.
 */
public final class SyntaxTree {

    private final RegexNode root;
    private final List<Position> positions;
    private final Position endPosition;

    /**
     * @param root        raíz del árbol aumentado (siempre un ConcatNode con el marcador final)
     * @param positions   todas las hojas en orden de numeración (incluye el marcador final al final)
     * @param endPosition la posición del marcador final (último elemento de positions)
     */
    public SyntaxTree(RegexNode root, List<Position> positions, Position endPosition) {
        this.root = Objects.requireNonNull(root, "root no puede ser null");
        Objects.requireNonNull(positions, "positions no puede ser null");
        if (positions.isEmpty()) {
            throw new IllegalArgumentException("positions no puede estar vacía");
        }
        this.endPosition = Objects.requireNonNull(endPosition, "endPosition no puede ser null");
        this.positions = List.copyOf(positions);  // inmutable
    }

    /** Raíz del árbol aumentado. Siempre es un {@code ConcatNode(r, marcadorFinal)}. */
    public RegexNode getRoot() {
        return root;
    }

    /**
     * Lista inmutable de todas las posiciones (hojas) en orden left-to-right.
     * La última siempre es {@code endPosition}.
     */
    public List<Position> getPositions() {
        return positions;
    }

    /** Posición del marcador final ({@code isEnd = true}). */
    public Position getEndPosition() {
        return endPosition;
    }

    /** Número total de posiciones (incluyendo el marcador final). */
    public int size() {
        return positions.size();
    }

    @Override
    public String toString() {
        return "SyntaxTree{positions=" + positions.size() + ", end=" + endPosition + "}";
    }
}
