package com.yapar.yalp;

import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class YalpParserTest {

    private final YalpParser parser = new YalpParser();

    // ── Casos felices ─────────────────────────────────────────────────────────

    @Test
    void parsea_baja_desde_cadena() {
        String src = """
                %token NUM PLUS MINUS
                %%
                E : E PLUS T { "suma" }
                  | T         { "pasar" }
                  ;
                T : NUM { "numero" }
                  ;
                """;

        YalpFile f = parser.parse(src);

        assertEquals(List.of("NUM", "PLUS", "MINUS"), f.getDeclaredTokens());
        assertEquals(2, f.getProductions().size());

        RawProduction e = f.getProductions().get(0);
        assertEquals("E", e.lhs());
        assertEquals(2, e.alternatives().size());
        assertEquals(List.of("E", "PLUS", "T"), e.alternatives().get(0).symbols());
        assertEquals("\"suma\"", e.alternatives().get(0).action());
        assertEquals(List.of("T"), e.alternatives().get(1).symbols());
    }

    @Test
    void parsea_baja_yalp_desde_archivo() {
        Path path = samplePath("baja.yalp");
        YalpFile f = parser.parse(path);
        assertEquals(List.of("NUM", "PLUS", "MINUS"), f.getDeclaredTokens());
        assertEquals(2, f.getProductions().size());
    }

    @Test
    void parsea_media_yalp() {
        Path path = samplePath("media.yalp");
        YalpFile f = parser.parse(path);
        assertEquals(7, f.getDeclaredTokens().size());
        assertEquals(3, f.getProductions().size()); // E, T, F
    }

    @Test
    void parsea_alta_yalp() {
        Path path = samplePath("alta.yalp");
        YalpFile f = parser.parse(path);
        // La gramática alta tiene múltiples líneas %token
        assertFalse(f.getDeclaredTokens().isEmpty());
        assertFalse(f.getProductions().isEmpty());
    }

    @Test
    void admite_multiples_lineas_percent_token() {
        String src = """
                %token A B
                %token C D
                %%
                S : A B C D { "ok" }
                  ;
                """;
        YalpFile f = parser.parse(src);
        assertEquals(List.of("A", "B", "C", "D"), f.getDeclaredTokens());
    }

    @Test
    void admite_produccion_epsilon() {
        String src = """
                %token A
                %%
                S : A { "con_a" }
                  |   { "epsilon" }
                  ;
                """;
        YalpFile f = parser.parse(src);
        RawAlternative eps = f.getProductions().get(0).alternatives().get(1);
        assertTrue(eps.isEpsilon());
        assertEquals("\"epsilon\"", eps.action());
    }

    @Test
    void admite_alternativa_sin_accion() {
        String src = """
                %token A B
                %%
                S : A B
                  | A
                  ;
                """;
        YalpFile f = parser.parse(src);
        assertEquals("", f.getProductions().get(0).alternatives().get(0).action());
    }

    @Test
    void admite_sin_seccion_token() {
        // Sin %token es válido; la lista quedará vacía
        String src = """
                %%
                S : S { "ok" }
                  ;
                """;
        YalpFile f = parser.parse(src);
        assertTrue(f.getDeclaredTokens().isEmpty());
        assertEquals(1, f.getProductions().size());
    }

    @Test
    void elimina_comentarios() {
        String src = """
                (* declaracion de tokens *)
                %token NUM
                (* separador *)
                %%
                (* produccion principal *)
                E : NUM { (* accion *) "numero" }
                  ;
                """;
        YalpFile f = parser.parse(src);
        assertEquals(List.of("NUM"), f.getDeclaredTokens());
    }

    // ── Casos de error ────────────────────────────────────────────────────────

    @Test
    void error_sin_separador_percent_percent() {
        String src = """
                %token A
                S : A ;
                """;
        assertThrows(YalpParseException.class, () -> parser.parse(src));
    }

    @Test
    void error_produccion_sin_punto_y_coma() {
        String src = """
                %token A
                %%
                S : A { "ok" }
                """;
        assertThrows(YalpParseException.class, () -> parser.parse(src));
    }

    @Test
    void error_percent_token_sin_identificadores() {
        String src = """
                %token
                %%
                S : S ;
                """;
        assertThrows(YalpParseException.class, () -> parser.parse(src));
    }

    @Test
    void error_secciones_vacias() {
        String src = "%%";
        assertThrows(YalpParseException.class, () -> parser.parse(src));
    }

    @Test
    void error_comentario_sin_cerrar() {
        String src = """
                (* sin cierre
                %%
                S : S ;
                """;
        assertThrows(YalpParseException.class, () -> parser.parse(src));
    }

    @Test
    void error_accion_sin_cerrar() {
        String src = """
                %token A
                %%
                S : A { accion sin cierre
                  ;
                """;
        assertThrows(YalpParseException.class, () -> parser.parse(src));
    }

    // ── Utilidad ──────────────────────────────────────────────────────────────

    private Path samplePath(String filename) {
        URL url = getClass().getClassLoader().getResource("samples/" + filename);
        assertNotNull(url, "Archivo de muestra no encontrado: " + filename);
        try {
            return Paths.get(url.toURI());
        } catch (java.net.URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }
}
