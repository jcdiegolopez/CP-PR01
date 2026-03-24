package com.yalex.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.yalex.model.LetDefinition;

class LetExpanderTest {

    @Test
    void replaceIdentifierOnlyWholeNames() {
        assertEquals("(D)+", LetExpander.replaceIdentifier("digito+", "digito", "(D)"));
        assertEquals("xdigito+", LetExpander.replaceIdentifier("xdigito+", "digito", "(D)"));
        assertEquals("mydigito", LetExpander.replaceIdentifier("mydigito", "digito", "(D)"));
        // Tras "digito" sigue '_' (carácter de identificador): no es sustitución de palabra completa.
        assertEquals("digito_x", LetExpander.replaceIdentifier("digito_x", "digito", "(D)"));
    }

    @Test
    void applyExpandsLetsInOrder() {
        List<LetDefinition> lets = List.of(
                new LetDefinition("digito", "['0'-'9']"),
                new LetDefinition("numero", "digito+"));
        assertEquals("((['0'-'9'])+)", LetExpander.apply("numero", lets));
    }
}
