package com.yalex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class MainTest {

    @Test
    void parseCli_inputThenOutput() {
        Main.ParsedCli c = Main.parseCli(new String[] { "src/test/resources/samples/simple.yal", "-o", "out_lexer" });
        assertTrue(c.input().toString().endsWith("simple.yal"));
        assertEquals(Path.of("out_lexer.py"), c.output());
    }

    @Test
    void parseCli_outputThenInput() {
        Main.ParsedCli c = Main.parseCli(new String[] { "-o", "foo.py", "src/test/resources/samples/simple.yal" });
        assertEquals(Path.of("foo.py"), c.output());
    }

    @Test
    void parseCli_rejectsUnknownFlag() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> Main.parseCli(new String[] { "-x", "a", "-o", "o" }));
        assertTrue(ex.getMessage().contains("desconocida"));
    }
}
