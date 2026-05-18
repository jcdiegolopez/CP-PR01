package com.yapar.analysis;

import com.yapar.model.Grammar;
import com.yapar.yalp.YalpParser;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests del {@link FirstFollowCalculator}.
 *
 * <p>Los valores esperados de FIRST y FOLLOW se verificaron a mano
 * usando la gramática clásica de expresiones aritméticas del Dragon Book (Sección 4.4).
 */
class FirstFollowCalculatorTest {

    private static final YalpParser PARSER = new YalpParser();

    private static Grammar grammar(String yalp) {
        return Grammar.build(PARSER.parse(yalp));
    }

    // ── Gramática de expresiones simples (Dragon Book §4.4) ───────────────────
    // E  → E PLUS T | T
    // T  → T TIMES F | F
    // F  → LPAREN E RPAREN | id

    private static final String EXPR_GRAMMAR = """
            %token PLUS TIMES LPAREN RPAREN id
            %%
            E : E PLUS T | T ;
            T : T TIMES F | F ;
            F : LPAREN E RPAREN | id ;
            """;

    @Test
    void first_de_terminal_es_el_propio_terminal() {
        FirstFollowCalculator calc = new FirstFollowCalculator(grammar(EXPR_GRAMMAR));
        assertEquals(Set.of("PLUS"), calc.first("PLUS"));
        assertEquals(Set.of("id"),   calc.first("id"));
    }

    @Test
    void first_de_F_es_LPAREN_id() {
        FirstFollowCalculator calc = new FirstFollowCalculator(grammar(EXPR_GRAMMAR));
        assertEquals(Set.of("LPAREN", "id"), calc.first("F"));
    }

    @Test
    void first_de_T_es_LPAREN_id() {
        FirstFollowCalculator calc = new FirstFollowCalculator(grammar(EXPR_GRAMMAR));
        assertEquals(Set.of("LPAREN", "id"), calc.first("T"));
    }

    @Test
    void first_de_E_es_LPAREN_id() {
        FirstFollowCalculator calc = new FirstFollowCalculator(grammar(EXPR_GRAMMAR));
        assertEquals(Set.of("LPAREN", "id"), calc.first("E"));
    }

    @Test
    void follow_de_E_es_PLUS_RPAREN_dolar() {
        FirstFollowCalculator calc = new FirstFollowCalculator(grammar(EXPR_GRAMMAR));
        Set<String> followE = calc.follow("E");
        assertTrue(followE.containsAll(Set.of("PLUS", "RPAREN", "$")),
            "FOLLOW(E) debe contener PLUS, RPAREN y $; obtenido: " + followE);
    }

    @Test
    void follow_de_T_es_PLUS_TIMES_RPAREN_dolar() {
        FirstFollowCalculator calc = new FirstFollowCalculator(grammar(EXPR_GRAMMAR));
        Set<String> followT = calc.follow("T");
        assertTrue(followT.containsAll(Set.of("PLUS", "TIMES", "RPAREN", "$")),
            "FOLLOW(T) debe contener PLUS, TIMES, RPAREN y $; obtenido: " + followT);
    }

    @Test
    void follow_de_F_es_PLUS_TIMES_RPAREN_dolar() {
        FirstFollowCalculator calc = new FirstFollowCalculator(grammar(EXPR_GRAMMAR));
        Set<String> followF = calc.follow("F");
        assertTrue(followF.containsAll(Set.of("PLUS", "TIMES", "RPAREN", "$")),
            "FOLLOW(F) debe contener PLUS, TIMES, RPAREN y $; obtenido: " + followF);
    }

    // ── Gramática con producción ε ────────────────────────────────────────────
    // S → A B
    // A → a | ε
    // B → b

    private static final String NULLABLE_GRAMMAR = """
            %token a b
            %%
            S : A B ;
            A : a | ;
            B : b ;
            """;

    @Test
    void first_de_S_incluye_a_y_b_por_A_nullable() {
        FirstFollowCalculator calc = new FirstFollowCalculator(grammar(NULLABLE_GRAMMAR));
        Set<String> firstS = calc.first("S");
        assertTrue(firstS.contains("a"), "FIRST(S) debe contener 'a'");
        assertTrue(firstS.contains("b"), "FIRST(S) debe contener 'b' porque A puede ser ε");
    }

    @Test
    void first_de_A_contiene_epsilon() {
        FirstFollowCalculator calc = new FirstFollowCalculator(grammar(NULLABLE_GRAMMAR));
        assertTrue(calc.first("A").contains(FirstFollowCalculator.EPSILON),
            "FIRST(A) debe contener ε porque A → ε existe");
    }

    @Test
    void follow_de_A_contiene_b() {
        FirstFollowCalculator calc = new FirstFollowCalculator(grammar(NULLABLE_GRAMMAR));
        assertTrue(calc.follow("A").contains("b"),
            "FOLLOW(A) debe contener 'b' porque S → A B y FIRST(B) = {b}");
    }

    // ── firstOfString ─────────────────────────────────────────────────────────

    @Test
    void firstOfString_lista_vacia_devuelve_epsilon() {
        FirstFollowCalculator calc = new FirstFollowCalculator(grammar(EXPR_GRAMMAR));
        Set<String> result = calc.firstOfString(List.of());
        assertTrue(result.contains(FirstFollowCalculator.EPSILON));
    }

    @Test
    void firstOfString_con_terminal_al_frente() {
        FirstFollowCalculator calc = new FirstFollowCalculator(grammar(EXPR_GRAMMAR));
        Set<String> result = calc.firstOfString(List.of("PLUS", "id"));
        assertEquals(Set.of("PLUS"), result);
        assertFalse(result.contains(FirstFollowCalculator.EPSILON));
    }

    @Test
    void firstOfString_todos_nullable_agrega_epsilon() {
        FirstFollowCalculator calc = new FirstFollowCalculator(grammar(NULLABLE_GRAMMAR));
        // A puede derivar ε. firstOfString([A, A]) debería incluir ε y 'a'
        Set<String> result = calc.firstOfString(List.of("A", "A"));
        assertTrue(result.contains(FirstFollowCalculator.EPSILON));
        assertTrue(result.contains("a"));
    }

    // ── Gramática mínima: símbolo inicial en FOLLOW ───────────────────────────

    @Test
    void follow_del_simbolo_inicial_siempre_contiene_dolar() {
        Grammar g = grammar("""
                %token X
                %%
                S : X ;
                """);
        FirstFollowCalculator calc = new FirstFollowCalculator(g);
        assertTrue(calc.follow(g.startSymbol()).contains("$"),
            "FOLLOW del símbolo inicial siempre debe contener '$'");
    }
}
