package com.yapar.model;

import com.yapar.yalp.YalpFile;
import com.yapar.yalp.YalpParser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GrammarTest {

    private final YalpParser parser = new YalpParser();

    private Grammar buildFrom(String yalpSrc) {
        return Grammar.build(parser.parse(yalpSrc));
    }

    // ── Construcción básica ───────────────────────────────────────────────────

    @Test
    void simbolo_inicial_es_lhs_de_primera_produccion() {
        Grammar g = buildFrom("""
                %token NUM PLUS
                %%
                E : E PLUS T { "suma" } | T { "p" } ;
                T : NUM { "num" } ;
                """);
        assertEquals("E", g.startSymbol());
    }

    @Test
    void terminales_incluyen_dolar() {
        Grammar g = buildFrom("""
                %token A B
                %%
                S : A B ;
                """);
        assertTrue(g.terminals().contains("$"),
            "El marcador de fin '$' debe estar en los terminales");
        assertTrue(g.terminals().contains("A"));
        assertTrue(g.terminals().contains("B"));
    }

    @Test
    void no_terminales_son_los_lhs() {
        Grammar g = buildFrom("""
                %token NUM PLUS
                %%
                E : E PLUS T | T ;
                T : NUM ;
                """);
        assertEquals(java.util.Set.of("E", "T"), g.nonTerminals());
    }

    @Test
    void producciones_se_numeran_desde_cero() {
        Grammar g = buildFrom("""
                %token A B
                %%
                S : A | B ;
                """);
        assertEquals(0, g.productions().get(0).id());
        assertEquals(1, g.productions().get(1).id());
    }

    @Test
    void produccion_epsilon_es_valida() {
        Grammar g = buildFrom("""
                %token A
                %%
                S : A | ;
                """);
        Production eps = g.productions().get(1);
        assertTrue(eps.isEpsilon());
        assertEquals("S", eps.lhs());
    }

    @Test
    void productions_for_filtra_correctamente() {
        Grammar g = buildFrom("""
                %token NUM PLUS
                %%
                E : E PLUS T | T ;
                T : NUM ;
                """);
        List<Production> eProds = g.productionsFor("E");
        assertEquals(2, eProds.size());
        assertTrue(eProds.stream().allMatch(p -> p.lhs().equals("E")));
    }

    @Test
    void toString_de_produccion() {
        Grammar g = buildFrom("""
                %token A B
                %%
                S : A B ;
                """);
        assertEquals("0: S → A B", g.productions().get(0).toString());
    }

    // ── Errores de validación ─────────────────────────────────────────────────

    @Test
    void error_simbolo_en_rhs_no_declarado() {
        assertThrows(IllegalArgumentException.class, () -> buildFrom("""
                %token A
                %%
                S : A UNDECLARED ;
                """));
    }

    @Test
    void error_simbolo_terminal_y_no_terminal_a_la_vez() {
        assertThrows(IllegalArgumentException.class, () -> buildFrom("""
                %token A S
                %%
                S : A ;
                """));
    }

    @Test
    void consultas_de_clasificacion() {
        Grammar g = buildFrom("""
                %token NUM
                %%
                E : NUM ;
                """);
        assertTrue(g.isTerminal("NUM"));
        assertFalse(g.isTerminal("E"));
        assertTrue(g.isNonTerminal("E"));
        assertFalse(g.isNonTerminal("NUM"));
    }
}
