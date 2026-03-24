package com.yalex.automata.dfa;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.yalex.automata.minimization.DFAMinimizer;
import com.yalex.automata.syntax.SyntaxTreeBuilder;
import com.yalex.model.LetDefinition;
import com.yalex.codegen.LetExpander;
import com.yalex.regex.RegexParser;
import com.yalex.regex.node.RegexNode;

/**
 * Regresión: {@code digito+} debe permitir más de un dígito (p. ej. {@code -11} es un solo número).
 */
class NumeroPlusDfaTest {

    @Test
    void directDfaHasDigitLoopAfterMinusOne() {
        List<LetDefinition> lets = List.of(
                new LetDefinition("digito", "['0'-'9']"),
                new LetDefinition("numero", "'-'?digito+"));
        String expanded = LetExpander.apply("numero", lets);
        RegexNode ast = RegexParser.parse(expanded);
        DFA dfa = DirectDfaBuilder.build(SyntaxTreeBuilder.build(ast));

        int s = dfa.getInitialStateId();
        s = dfa.transition(s, '-');
        assertNotEquals(-1, s, "debe existir transición con '-'");
        s = dfa.transition(s, '1');
        assertNotEquals(-1, s, "tras '-' y un dígito debe haber estado");
        assertTrue(dfa.getState(s).isAccepting(), "debe aceptar prefijo -1");
        int s2 = dfa.transition(s, '1');
        assertNotEquals(-1, s2, "debe poder leer un segundo dígito (11) sin cortar en -1");
        assertTrue(dfa.getState(s2).isAccepting(), "debe aceptar -11");
    }

    @Test
    void minimizedDfaPreservesDigitContinuation() {
        List<LetDefinition> lets = List.of(
                new LetDefinition("digito", "['0'-'9']"),
                new LetDefinition("numero", "'-'?digito+"));
        String expanded = LetExpander.apply("numero", lets);
        RegexNode ast = RegexParser.parse(expanded);
        DFA raw = DirectDfaBuilder.build(SyntaxTreeBuilder.build(ast));
        DFA min = DFAMinimizer.minimize(raw);

        int s = min.getInitialStateId();
        s = min.transition(s, '-');
        s = min.transition(s, '1');
        assertNotEquals(-1, min.transition(s, '1'), "DFA minimizado no debe perder el bucle de dígitos");
    }
}
