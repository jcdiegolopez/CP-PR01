package com.yalex.regex;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.yalex.automata.syntax.SyntaxTree;
import com.yalex.automata.syntax.SyntaxTreeBuilder;
import com.yalex.regex.node.AlternNode;
import com.yalex.regex.node.CharClassNode;
import com.yalex.regex.node.CharNode;
import com.yalex.regex.node.ConcatNode;
import com.yalex.regex.node.DiffNode;
import com.yalex.regex.node.KleeneNode;
import com.yalex.regex.node.RegexNode;

/**
 * Regresión contra comentarios de laboratorio (UVG): ε, escapes, espacios en regexp,
 * y el caso {@code \?(((.|ε)?!?)*)+} con cadena {@code ?!.}.
 *
 * <p><b>Nota (entrada por consola / Windows):</b> si se lee la regexp con
 * {@code new Scanner(System.in)} sin charset, ε (U+03B5) puede corromperse; usar
 * {@link java.io.InputStreamReader#InputStreamReader(java.io.InputStream, java.nio.charset.Charset)
 * InputStreamReader(System.in, StandardCharsets.UTF_8)} o equivalente.
 *
 * <p><b>Dónde aplican escapes y espacios:</b> en la <em>especificación</em> de la regexp
 * ({@link RegexLexer}: escapes como {@code \\?}, espacios ignorados entre tokens). Eso ocurre
 * al parsear el patrón, antes del DFA. El DFA solo ve transiciones por símbolos de entrada;
 * los escapes ya quedaron resueltos en el AST.
 */
class RegexLabRegressionTest {

    /**
     * Patrón del laboratorio. El carácter ε debe ser el Unicode U+03B5 (en Java: {@code "\u03B5"},
     * no la secuencia literal {@code \\u03B5} dentro del string, que no es un escape Unicode).
     */
    static final String PROFESSOR_PATTERN = "\\?(((.|" + "\u03B5" + ")?!?)*)+";

    static final String PROFESSOR_INPUT = "?!.";

    @Test
    @DisplayName("Patrón del lab parsea (ε como identificador resuelto)")
    void professorPattern_parses() {
        assertDoesNotThrow(() -> RegexParser.parse(PROFESSOR_PATTERN));
    }

    @Test
    @DisplayName("Espacios en la regexp se ignoran (RegexLexer.skipWhitespace)")
    void professorPattern_withSpaces_parsesSameStructure() {
        String spaced = " \\? ( ( ( . | " + "\u03B5" + " ) ? ! ? ) * ) + ";
        RegexNode a = RegexParser.parse(PROFESSOR_PATTERN);
        RegexNode b = RegexParser.parse(spaced);
        assertTrue(structurallySimilarForLab(a, b),
                "El lexer ignora whitespace entre tokens; el AST debería coincidir en forma");
    }

    @Test
    @DisplayName("Palabra 'epsilon' equivale a ε en la especificación")
    void epsilonAsciiIdentifier_sameAsUnicodeEpsilon() {
        String withWord = "\\?(((.|epsilon)?!?)*)+";
        // Misma forma que PROFESSOR_PATTERN pero con el identificador epsilon (ASCII)
        assertDoesNotThrow(() -> RegexParser.parse(withWord));
        RegexNode u = RegexParser.parse(PROFESSOR_PATTERN);
        RegexNode e = RegexParser.parse(withWord);
        assertTrue(structurallySimilarForLab(u, e));
    }

    @Test
    @DisplayName("Árbol aumentado y análisis followpos se construyen para el patrón del lab")
    void professorPattern_buildsSyntaxTreeAndAnalysis() {
        RegexNode ast = RegexParser.parse(PROFESSOR_PATTERN);
        SyntaxTree tree = assertDoesNotThrow(() -> SyntaxTreeBuilder.build(ast));
        assertTrue(tree.size() >= 2);
    }

    @Test
    @DisplayName("Cadena ?!. debe ser aceptada por la lenguaje del patrón (simulación de prueba)")
    void professorInput_acceptedBySimulationMatcher() {
        RegexNode ast = RegexParser.parse(PROFESSOR_PATTERN);
        SyntaxTree tree = SyntaxTreeBuilder.build(ast);
        RegexNode body = ((ConcatNode) tree.getRoot()).getLeft();
        assertTrue(
                RegexSimulationMatcher.matchesEntireInput(body, PROFESSOR_INPUT),
                "La cadena ?!. debería ser aceptada; si falla, revisar DFA/simulación del producto");
    }

    @Nested
    @DisplayName("Escape \\? fuera de comillas")
    class EscapedQuestionMark {
        @Test
        void backslashQuestion_isLiteralQuestionToken() {
            RegexLexer lexer = new RegexLexer("\\?");
            assertTrue(lexer.nextToken().type() == RegexLexer.TokenType.CHAR);
            assertTrue(lexer.nextToken().type() == RegexLexer.TokenType.EOF);
        }
    }

    // -------------------------------------------------------------------------
    // Comparación estructural simple (solo para espacios / epsilon vs ε)
    // -------------------------------------------------------------------------

    private static boolean structurallySimilarForLab(RegexNode a, RegexNode b) {
        if (a == null || b == null) {
            return a == b;
        }
        if (a.getClass() != b.getClass()) {
            return false;
        }
        if (a instanceof CharNode ca && b instanceof CharNode cb) {
            return ca.getValue().equals(cb.getValue());
        }
        if (a instanceof ConcatNode xa && b instanceof ConcatNode xb) {
            return structurallySimilarForLab(xa.getLeft(), xb.getLeft())
                    && structurallySimilarForLab(xa.getRight(), xb.getRight());
        }
        if (a instanceof AlternNode xa && b instanceof AlternNode xb) {
            return structurallySimilarForLab(xa.getLeft(), xb.getLeft())
                    && structurallySimilarForLab(xa.getRight(), xb.getRight());
        }
        if (a instanceof KleeneNode xa && b instanceof KleeneNode xb) {
            return structurallySimilarForLab(xa.getChild(), xb.getChild());
        }
        if (a instanceof com.yalex.regex.node.OptionalNode xa
                && b instanceof com.yalex.regex.node.OptionalNode xb) {
            return structurallySimilarForLab(xa.getChild(), xb.getChild());
        }
        if (a instanceof com.yalex.regex.node.PlusNode xa
                && b instanceof com.yalex.regex.node.PlusNode xb) {
            return structurallySimilarForLab(xa.getChild(), xb.getChild());
        }
        if (a instanceof DiffNode xa && b instanceof DiffNode xb) {
            return structurallySimilarForLab(xa.getLeft(), xb.getLeft())
                    && structurallySimilarForLab(xa.getRight(), xb.getRight());
        }
        if (a instanceof CharClassNode xa && b instanceof CharClassNode xb) {
            return xa.isNegated() == xb.isNegated() && xa.getEntries().equals(xb.getEntries());
        }
        if (a instanceof com.yalex.regex.node.WildcardNode && b instanceof com.yalex.regex.node.WildcardNode) {
            return true;
        }
        if (a instanceof com.yalex.regex.node.EpsilonRegexNode
                && b instanceof com.yalex.regex.node.EpsilonRegexNode) {
            return true;
        }
        return false;
    }

    /**
     * Simulador solo para tests: conjuntos de posiciones alcanzables (sin {@code java.util.regex}).
     * No cubre {@link DiffNode} (lanza {@link UnsupportedOperationException}).
     */
    static final class RegexSimulationMatcher {

        private RegexSimulationMatcher() {
        }

        static boolean matchesEntireInput(RegexNode body, String input) {
            Set<Integer> endPositions = reachable(body, input, 0);
            return endPositions.contains(input.length());
        }

        private static Set<Integer> reachable(RegexNode n, String s, int from) {
            if (n instanceof ConcatNode c) {
                Set<Integer> cur = new HashSet<>();
                cur.add(from);
                cur = concatStep(c.getLeft(), s, cur);
                cur = concatStep(c.getRight(), s, cur);
                return cur;
            }
            if (n instanceof AlternNode a) {
                Set<Integer> out = new HashSet<>(reachable(a.getLeft(), s, from));
                if (SyntaxTreeBuilder.isEpsilonRegex(a.getRight())) {
                    out.add(from);
                } else {
                    out.addAll(reachable(a.getRight(), s, from));
                }
                return out;
            }
            if (n instanceof KleeneNode k) {
                Set<Integer> closure = new HashSet<>();
                closure.add(from);
                boolean changed = true;
                while (changed) {
                    changed = false;
                    for (int p : new HashSet<>(closure)) {
                        for (int q : reachable(k.getChild(), s, p)) {
                            if (closure.add(q)) {
                                changed = true;
                            }
                        }
                    }
                }
                return closure;
            }
            if (n instanceof CharNode cn) {
                char ch = cn.getValue().charAt(0);
                if (from >= s.length()) {
                    return Set.of();
                }
                if (s.charAt(from) == ch) {
                    return Set.of(from + 1);
                }
                return Set.of();
            }
            if (n instanceof CharClassNode cc) {
                if (from >= s.length()) {
                    return Set.of();
                }
                char ch = s.charAt(from);
                return charClassMatches(cc, ch) ? Set.of(from + 1) : Set.of();
            }
            if (SyntaxTreeBuilder.isEpsilonRegex(n)) {
                return Set.of(from);
            }
            if (n instanceof DiffNode) {
                throw new UnsupportedOperationException("DiffNode no soportado en simulador de prueba");
            }
            throw new UnsupportedOperationException("Nodo no soportado: " + n.getClass().getSimpleName());
        }

        private static Set<Integer> concatStep(RegexNode n, String s, Set<Integer> starts) {
            Set<Integer> out = new HashSet<>();
            for (int p : starts) {
                out.addAll(reachable(n, s, p));
            }
            return out;
        }

        private static boolean charClassMatches(CharClassNode node, char c) {
            List<String> entries = node.getEntries();
            boolean in = false;
            for (String e : entries) {
                if (e.length() == 3 && e.charAt(1) == '-') {
                    char lo = e.charAt(0);
                    char hi = e.charAt(2);
                    if (c >= lo && c <= hi) {
                        in = true;
                        break;
                    }
                } else if (e.length() == 1 && e.charAt(0) == c) {
                    in = true;
                    break;
                } else if (e.codePointCount(0, e.length()) == 1 && e.charAt(0) == c) {
                    in = true;
                    break;
                }
            }
            if (node.isNegated()) {
                return !in;
            }
            return in;
        }
    }
}
