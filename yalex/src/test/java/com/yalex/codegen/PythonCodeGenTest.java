package com.yalex.codegen;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.yalex.yal.YalFile;
import com.yalex.yal.YalParser;

class PythonCodeGenTest {

    @Test
    void generatesLexerFromSimpleYal() {
        YalParser parser = new YalParser();
        YalFile yal = parser.parse(Path.of("src/test/resources/samples/simple.yal"));
        String py = PythonCodeGen.generateString(yal);
        assertTrue(py.contains("def _longest_match"));
        assertTrue(py.contains("def next_token"));
        assertTrue(py.contains("def tokenize_all"));
        assertTrue(py.contains("_RULE_DFAS"));
        assertTrue(py.contains("def _action_"));
        assertTrue(py.contains("from tokens import PLUS, NUM"));
        assertTrue(py.contains("True") && py.contains("False")); // literales Python, no true/false de Java
        assertFalse(py.isBlank());
    }
}
