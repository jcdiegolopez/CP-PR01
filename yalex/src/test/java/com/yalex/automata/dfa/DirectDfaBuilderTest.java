package com.yalex.automata.dfa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.yalex.automata.DfaTestSupport;
import com.yalex.automata.syntax.SyntaxTree;
import com.yalex.automata.syntax.SyntaxTreeBuilder;
import com.yalex.regex.RegexParser;
import com.yalex.regex.node.RegexNode;

class DirectDfaBuilderTest {

    private static SyntaxTree tree(String pattern) {
        RegexNode ast = RegexParser.parse(pattern);
        return SyntaxTreeBuilder.build(ast);
    }

    @Test
    void singleChar_hasTwoStatesAndAcceptsA() {
        DFA dfa = DirectDfaBuilder.build(tree("'a'"));
        assertEquals(2, dfa.getStateCount());
        assertFalse(dfa.getState(dfa.getInitialStateId()).isAccepting());
        assertTrue(DfaTestSupport.accepts(dfa, "a"));
        assertFalse(DfaTestSupport.accepts(dfa, ""));
        assertFalse(DfaTestSupport.accepts(dfa, "b"));
    }

    @Test
    void alternation_acceptsEitherBranch() {
        DFA dfa = DirectDfaBuilder.build(tree("'a'|'b'"));
        assertTrue(DfaTestSupport.accepts(dfa, "a"));
        assertTrue(DfaTestSupport.accepts(dfa, "b"));
        assertFalse(DfaTestSupport.accepts(dfa, "c"));
    }
}
