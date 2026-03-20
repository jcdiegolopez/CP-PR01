package com.yalex.yal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import com.yalex.model.RuleSet;

public class YalParserTest {

    @Test
    void parsesSampleWithHeaderLetsRulesAndTrailer() {
        YalParser parser = new YalParser();
        YalFile yalFile = parser.parse(Path.of("src/test/resources/samples/simple.yal"));

        assertTrue(yalFile.getHeader().contains("from tokens import PLUS, NUM"));
        assertEquals(1, yalFile.getLetDefinitions().size());
        assertEquals("digit", yalFile.getLetDefinitions().get(0).getName());
        assertEquals(1, yalFile.getRuleSets().size());

        RuleSet ruleSet = yalFile.getRuleSets().get(0);
        assertEquals("gettoken", ruleSet.getName());
        assertEquals(4, ruleSet.getRules().size());
        assertEquals("digit+", ruleSet.getRules().get(1).getPattern());
        assertEquals("return NUM", ruleSet.getRules().get(1).getAction());
        assertTrue(yalFile.getTrailer().contains("def helper():"));
    }

    @Test
    void stripsCommentsBeforeParsing() {
        String source = """
            (* comentario *)
            let digit = ['0'-'9']
            rule gettoken =
              digit+ { return NUM }
            """;

        YalFile yalFile = new YalParser().parse(source);
        assertEquals(1, yalFile.getLetDefinitions().size());
        assertEquals(1, yalFile.getRuleSets().size());
    }

    @Test
    void failsOnUnclosedComment() {
        String source = "(* comentario sin cierre";
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new YalParser().parse(source));
        assertTrue(ex.getMessage().contains("comentario sin cierre"));
    }

    @Test
    void parsesRuleWithMultipleAlternatives() {
        YalFile yalFile = new YalParser().parse(Path.of("src/test/resources/samples/full_example.yal"));
        RuleSet ruleSet = yalFile.getRuleSets().get(0);

        assertEquals("gettoken", ruleSet.getName());
        assertTrue(ruleSet.getRules().size() >= 8);
        assertEquals("'+'", ruleSet.getRules().get(3).getPattern());
        assertFalse(yalFile.getHeader().isBlank());
    }
}
