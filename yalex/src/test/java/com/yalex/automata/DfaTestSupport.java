package com.yalex.automata;

import com.yalex.automata.dfa.DFA;

/** Utilidades compartidas para pruebas de DFA. */
public final class DfaTestSupport {

    private DfaTestSupport() {
    }

    public static boolean accepts(DFA dfa, String input) {
        int st = dfa.getInitialStateId();
        for (int i = 0; i < input.length(); i++) {
            int n = dfa.transition(st, input.charAt(i));
            if (n < 0) {
                return false;
            }
            st = n;
        }
        return dfa.getState(st).isAccepting();
    }
}
