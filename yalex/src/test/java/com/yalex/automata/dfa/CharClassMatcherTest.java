package com.yalex.automata.dfa;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CharClassMatcherTest {

    @Test
    void aUpperZTypoExpandsToAllLetters() {
        assertTrue(CharClassMatcher.rangeContains('a', 'Z', 'v'));
        assertTrue(CharClassMatcher.rangeContains('a', 'Z', 'M'));
        assertFalse(CharClassMatcher.rangeContains('a', 'Z', '0'));
    }

    @Test
    void reversedRangeUsesContiguousCodepointsWhenNotLetterTypo() {
        assertTrue(CharClassMatcher.rangeContains('z', 'a', 'm'));
        assertTrue(CharClassMatcher.rangeContains('9', '0', '5'));
    }
}
