package com.yalex.automata.analysis;

import java.util.Set;

/**
 * Resultado de anotar un nodo del árbol aumentado con {@link NullableFirstLast}.
 *
 * @param nullable {@code true} si el subárbol puede generar la cadena vacía
 * @param firstpos   conjunto de IDs de posición que pueden coincidir con el primer símbolo
 * @param lastpos    conjunto de IDs de posición que pueden coincidir con el último símbolo
 */
public record NodeAnnotation(boolean nullable, Set<Integer> firstpos, Set<Integer> lastpos) {
}
