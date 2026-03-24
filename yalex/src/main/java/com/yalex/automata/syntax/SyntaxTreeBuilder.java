package com.yalex.automata.syntax;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.yalex.regex.node.AlternNode;
import com.yalex.regex.node.CharClassNode;
import com.yalex.regex.node.CharNode;
import com.yalex.regex.node.ConcatNode;
import com.yalex.regex.node.DiffNode;
import com.yalex.regex.node.EpsilonRegexNode;
import com.yalex.regex.node.KleeneNode;
import com.yalex.regex.node.OptionalNode;
import com.yalex.regex.node.PlusNode;
import com.yalex.regex.node.RegexNode;
import com.yalex.regex.node.WildcardNode;

/**
 * Construye el árbol sintáctico aumentado ({@link SyntaxTree}) a partir de un AST de regexp.
 *
 * <h2>Transformaciones aplicadas</h2>
 * <ol>
 *   <li><b>PlusNode</b> {@code r+} → {@code r · r*} (ConcatNode + KleeneNode)</li>
 *   <li><b>OptionalNode</b> {@code r?} → {@code r | ε}. La cadena vacía se representa
 *       dejando el AlternNode con un solo hijo (el hijo nulo queda como null y es tratado
 *       como nullable sin posiciones). Se usa un nodo sentinel {@link EpsilonNode} interno.</li>
 *   <li><b>WildcardNode</b> {@code _} → {@link CharClassNode} con todos los caracteres
 *       imprimibles ASCII (32–126), 95 entradas, no negado.</li>
 *   <li><b>Marcador final</b>: el árbol transformado {@code r'} se envuelve en {@code r' · #}
 *       donde {@code #} es un {@link CharNode} especial con símbolo {@code '\0'} y
 *       {@code isEnd = true}.</li>
 * </ol>
 *
 * <h2>Numeración de posiciones</h2>
 * <p>Se recorre el árbol transformado de izquierda a derecha. A cada hoja ({@link CharNode}
 * o {@link CharClassNode}) se le asigna un ID único incremental (1-based). El marcador final
 * siempre recibe el ID más alto.
 *
 * <h2>Uso</h2>
 * <pre>{@code
 *   RegexNode ast = RegexParser.parse("'a'|'b'");
 *   SyntaxTree tree = SyntaxTreeBuilder.build(ast);
 * }</pre>
 */
public class SyntaxTreeBuilder {

    // Símbolo reservado para el marcador final
    static final char END_SYMBOL = '\0';

    // Nodo sentinel interno para representar ε (cadena vacía)
    // Solo existe dentro de este builder, nunca se expone al exterior
    private static final class EpsilonNode extends RegexNode {
        static final EpsilonNode INSTANCE = new EpsilonNode();
        private EpsilonNode() {}
    }

    /**
     * Indica si el nodo es el marcador interno de ε usado en {@code r? → r | ε}.
     * Expuesto para análisis ({@code NullableFirstLast}) fuera de este builder.
     */
    public static boolean isEpsilonRegex(RegexNode node) {
        return node instanceof EpsilonNode;
    }

    /**
     * Expande un AST de regex, reemplazando nodos derivados (Plus, Optional,
     * Wildcard, EpsilonRegexNode) por sus equivalentes primitivos.
     *
     * <p>Útil para construir árboles combinados fuera de {@link #build(RegexNode)}.
     */
    public static RegexNode expandAst(RegexNode node) {
        return new SyntaxTreeBuilder().expand(node);
    }

    // =========================================================================
    // Punto de entrada estático
    // =========================================================================

    /**
     * Construye el árbol aumentado para la expresión regular dada.
     *
     * @param ast nodo raíz producido por {@code RegexParser.parse()} (no null)
     * @return árbol aumentado con posiciones asignadas y marcador final
     * @throws IllegalArgumentException si el AST es null o contiene nodos desconocidos
     */
    public static SyntaxTree build(RegexNode ast) {
        Objects.requireNonNull(ast, "ast no puede ser null");

        SyntaxTreeBuilder builder = new SyntaxTreeBuilder();

        // 1. Expandir nodos derivados (Plus, Optional, Wildcard)
        RegexNode expanded = builder.expand(ast);

        // 2. Crear el nodo marcador final (#) y envolver: expanded · #
        CharNode endMarkerNode = new CharNode(String.valueOf(END_SYMBOL));
        RegexNode augmented = new ConcatNode(expanded, endMarkerNode);

        // 3. Numerar hojas de izquierda a derecha
        List<Position> positions = new ArrayList<>();
        builder.assignPositions(augmented, positions, endMarkerNode);

        // La posición final es siempre la última asignada
        Position endPosition = positions.get(positions.size() - 1);
        if (!endPosition.isEnd()) {
            throw new IllegalStateException("La última posición debería ser el marcador final");
        }

        return new SyntaxTree(augmented, positions, endPosition);
    }

    // =========================================================================
    // Expansión de nodos derivados
    // =========================================================================

    /**
     * Recorre el AST recursivamente y reemplaza nodos derivados por sus equivalentes primitivos.
     */
    private RegexNode expand(RegexNode node) {
        if (node instanceof CharNode) {
            return node; // hoja: sin cambios

        } else if (node instanceof CharClassNode) {
            return node; // hoja: sin cambios

        } else if (node instanceof WildcardNode) {
            return expandWildcard();

        } else if (node instanceof KleeneNode k) {
            return new KleeneNode(expand(k.getChild()));

        } else if (node instanceof PlusNode p) {
            // r+ → r · r*
            // IMPORTANTE: expand() reutiliza hojas idénticas (p.ej. CharClassNode); NullableFirstLast
            // anota por identidad de nodo, así que la segunda copia debe ser un clon profundo
            // para que digito+ tenga followpos correcto (varios dígitos seguidos, p.ej. -11).
            RegexNode leftChild = expand(p.getChild());
            RegexNode rightChild = deepCopyRegex(leftChild);
            return new ConcatNode(leftChild, new KleeneNode(rightChild));

        } else if (node instanceof OptionalNode o) {
            // r? → r | ε
            RegexNode child = expand(o.getChild());
            return new AlternNode(child, EpsilonNode.INSTANCE);

        } else if (node instanceof ConcatNode c) {
            return new ConcatNode(expand(c.getLeft()), expand(c.getRight()));

        } else if (node instanceof AlternNode a) {
            return new AlternNode(expand(a.getLeft()), expand(a.getRight()));

        } else if (node instanceof DiffNode d) {
            // DiffNode se preserva; la diferencia se resuelve en el análisis posterior
            return new DiffNode(expand(d.getLeft()), expand(d.getRight()));

        } else if (node instanceof EpsilonRegexNode) {
            return EpsilonNode.INSTANCE;

        } else if (node instanceof EpsilonNode) {
            return node;

        } else {
            throw new IllegalArgumentException(
                "Nodo de AST desconocido: " + node.getClass().getSimpleName()
                + ". Las referencias 'let' deben expandirse antes de llamar a SyntaxTreeBuilder.");
        }
    }

    /**
     * Copia profunda del AST ya expandido (sin Plus/Optional/Wildcard sin expandir).
     * Necesaria para {@code r+} → {@code r·r*} con dos subárboles independientes.
     */
    private static RegexNode deepCopyRegex(RegexNode node) {
        Objects.requireNonNull(node, "node");
        if (node instanceof CharNode cn) {
            return new CharNode(cn.getValue());
        }
        if (node instanceof CharClassNode cc) {
            return new CharClassNode(new ArrayList<>(cc.getEntries()), cc.isNegated());
        }
        if (node instanceof ConcatNode c) {
            return new ConcatNode(deepCopyRegex(c.getLeft()), deepCopyRegex(c.getRight()));
        }
        if (node instanceof AlternNode a) {
            if (isEpsilonRegex(a.getRight())) {
                return new AlternNode(deepCopyRegex(a.getLeft()), EpsilonNode.INSTANCE);
            }
            return new AlternNode(deepCopyRegex(a.getLeft()), deepCopyRegex(a.getRight()));
        }
        if (node instanceof KleeneNode k) {
            return new KleeneNode(deepCopyRegex(k.getChild()));
        }
        if (node instanceof OptionalNode o) {
            return new OptionalNode(deepCopyRegex(o.getChild()));
        }
        if (node instanceof DiffNode d) {
            return new DiffNode(deepCopyRegex(d.getLeft()), deepCopyRegex(d.getRight()));
        }
        if (node instanceof WildcardNode) {
            return expandWildcard();
        }
        if (isEpsilonRegex(node)) {
            return EpsilonNode.INSTANCE;
        }
        if (node instanceof EpsilonRegexNode) {
            return EpsilonRegexNode.INSTANCE;
        }
        throw new IllegalArgumentException("deepCopyRegex: nodo no soportado: " + node.getClass().getName());
    }

    /**
     * Expande {@code _} (wildcard) como un {@link CharClassNode} con todos los
     * caracteres imprimibles ASCII (code points 32–126 inclusive, 95 caracteres).
     * El nodo no es negado.
     */
    private static CharClassNode expandWildcard() {
        List<String> entries = new ArrayList<>(95);
        for (int cp = 32; cp <= 126; cp++) {
            entries.add(String.valueOf((char) cp));
        }
        return new CharClassNode(entries, false);
    }

    // =========================================================================
    // Numeración de posiciones (recorrido left-to-right)
    // =========================================================================

    private int nextId = 1;

    /**
     * Recorre el árbol en orden left-to-right y registra una {@link Position}
     * por cada hoja ({@link CharNode} o {@link CharClassNode}).
     *
     * @param node          nodo actual
     * @param positions     lista donde se acumulan las posiciones
     * @param endMarkerNode referencia al nodo marcador final para detectar {@code isEnd}
     */
    private void assignPositions(RegexNode node, List<Position> positions, CharNode endMarkerNode) {
        if (node instanceof CharNode cn) {
            boolean isEnd = (cn == endMarkerNode); // identidad de objeto, no valor
            char symbol = cn.getValue().charAt(0);
            positions.add(new Position(nextId++, symbol, isEnd));

        } else if (node instanceof CharClassNode) {
            // Una clase de caracteres ocupa UNA sola posición.
            // El símbolo se deja como '\0' porque el símbolo real depende del
            // input en tiempo de ejecución. El análisis posterior usa el nodo
            // completo para determinar qué símbolos habilita la clase.
            positions.add(new Position(nextId++, END_SYMBOL, false));   // símbolo placeholder

        } else if (node instanceof ConcatNode c) {
            assignPositions(c.getLeft(), positions, endMarkerNode);
            assignPositions(c.getRight(), positions, endMarkerNode);

        } else if (node instanceof AlternNode a) {
            assignPositions(a.getLeft(), positions, endMarkerNode);
            if (!(a.getRight() instanceof EpsilonNode)) {
                assignPositions(a.getRight(), positions, endMarkerNode);
            }

        } else if (node instanceof KleeneNode k) {
            assignPositions(k.getChild(), positions, endMarkerNode);

        } else if (node instanceof DiffNode d) {
            assignPositions(d.getLeft(), positions, endMarkerNode);
            assignPositions(d.getRight(), positions, endMarkerNode);

        } else if (node instanceof EpsilonNode) {
            // ε no tiene hoja — no asigna posición

        } else {
            throw new IllegalArgumentException(
                "Nodo inesperado durante asignación de posiciones: "
                + node.getClass().getSimpleName());
        }
    }
}
