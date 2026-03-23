package com.yalex.automata.minimization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.yalex.automata.DfaTestSupport;
import com.yalex.automata.dfa.DFA;
import com.yalex.automata.dfa.DirectDfaBuilder;
import com.yalex.automata.syntax.SyntaxTree;
import com.yalex.automata.syntax.SyntaxTreeBuilder;
import com.yalex.regex.RegexParser;
import com.yalex.regex.node.RegexNode;

class DFAMinimizerTest {

    private static SyntaxTree tree(String pattern) {
        RegexNode ast = RegexParser.parse(pattern);
        return SyntaxTreeBuilder.build(ast);
    }

    @Test
    void minimizeDoesNotIncreaseStates() {
        DFA dfa = DirectDfaBuilder.build(tree("'a'|'b'"));
        DFA min = DFAMinimizer.minimize(dfa);
        assertTrue(min.getStateCount() <= dfa.getStateCount());
    }

    @Test
    void minimizedSameLanguage_onShortStrings() {
        DFA dfa = DirectDfaBuilder.build(tree("'a'"));
        DFA min = DFAMinimizer.minimize(dfa);
        for (String s : new String[] {"", "a", "aa", "b"}) {
            assertEquals(
                    DfaTestSupport.accepts(dfa, s),
                    DfaTestSupport.accepts(min, s),
                    "input: " + s);
        }
    }
}
