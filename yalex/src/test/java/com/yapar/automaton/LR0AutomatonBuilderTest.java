package com.yapar.automaton;

import com.yapar.model.Grammar;
import com.yapar.model.Production;
import com.yapar.yalp.YalpParser;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests del {@link LR0AutomatonBuilder} y del {@link LR0Automaton} resultante.
 *
 * <p>Las gramáticas y los números de estados esperados se verifican contra
 * los ejemplos del Dragon Book (Compilers: Principles, Techniques, and Tools, 2nd ed.),
 * sección 4.6.
 */
class LR0AutomatonBuilderTest {

    private static final YalpParser PARSER = new YalpParser();

    private static Grammar grammar(String yalp) {
        return Grammar.build(PARSER.parse(yalp));
    }

    // ── Gramática simple de expresiones (Dragon Book §4.6) ───────────────────
    // E  → E PLUS T | T
    // T  → T TIMES F | F
    // F  → LPAREN E RPAREN | id
    //
    // Con la gramática augmentada E' → E, la colección canónica tiene 12 estados.

    private static final String EXPR_GRAMMAR = """
            %token PLUS TIMES LPAREN RPAREN id
            %%
            E : E PLUS T | T ;
            T : T TIMES F | F ;
            F : LPAREN E RPAREN | id ;
            """;

    @Test
    void automata_expresiones_tiene_doce_estados() {
        Grammar g = grammar(EXPR_GRAMMAR);
        LR0Automaton a = new LR0AutomatonBuilder(g).build();
        assertEquals(12, a.stateCount(),
            "La gramática de expresiones del Dragon Book debe producir 12 estados LR(0)");
    }

    @Test
    void estado_inicial_es_cero() {
        LR0Automaton a = new LR0AutomatonBuilder(grammar(EXPR_GRAMMAR)).build();
        assertEquals(0, a.initialState());
    }

    @Test
    void estado_0_contiene_item_inicial_de_S_prima() {
        Grammar g = grammar(EXPR_GRAMMAR);
        LR0Automaton a = new LR0AutomatonBuilder(g).build();

        LR0ItemSet s0 = a.state(0);
        boolean foundStart = s0.items().stream().anyMatch(item ->
                item.production().lhs().equals(LR0AutomatonBuilder.AUGMENTED_START)
                && item.dotPosition() == 0);
        assertTrue(foundStart,
            "El estado 0 debe contener [S' → • E]");
    }

    @Test
    void goto_de_estado0_sobre_id_existe() {
        LR0Automaton a = new LR0AutomatonBuilder(grammar(EXPR_GRAMMAR)).build();
        Optional<Integer> target = a.gotoTarget(0, "id");
        assertTrue(target.isPresent(), "goto(0, id) debe existir");
    }

    @Test
    void produccion_augmentada_es_S_prima_a_startSymbol() {
        Grammar g = grammar(EXPR_GRAMMAR);
        LR0Automaton a = new LR0AutomatonBuilder(g).build();
        Production start = a.startProduction();
        assertEquals(LR0AutomatonBuilder.AUGMENTED_START, start.lhs());
        assertEquals(1, start.rhs().size());
        assertEquals(g.startSymbol(), start.rhs().get(0));
    }

    // ── Gramática mínima: S → a ───────────────────────────────────────────────
    // S'→ S, S → a
    // Estados esperados: 3
    //   I0: S'→•S, S→•a    --a-->  I2: S→a•
    //                       --S-->  I1: S'→S•

    private static final String MINIMAL_GRAMMAR = """
            %token a
            %%
            S : a ;
            """;

    @Test
    void automata_minimo_tiene_tres_estados() {
        LR0Automaton a = new LR0AutomatonBuilder(grammar(MINIMAL_GRAMMAR)).build();
        assertEquals(3, a.stateCount(),
            "S → a debe producir 3 estados: I0, I1 (S'→S•), I2 (S→a•)");
    }

    @Test
    void goto_minimo_sobre_a_lleva_a_reduccion() {
        LR0Automaton a = new LR0AutomatonBuilder(grammar(MINIMAL_GRAMMAR)).build();
        Optional<Integer> target = a.gotoTarget(0, "a");
        assertTrue(target.isPresent(), "goto(0, a) debe existir");

        // El estado destino debe contener S → a • (ítem completo)
        LR0ItemSet dest = a.state(target.get());
        boolean hasComplete = dest.items().stream().anyMatch(item ->
                item.production().lhs().equals("S") && item.isComplete());
        assertTrue(hasComplete, "El estado goto(0,a) debe contener [S → a •]");
    }

    // ── Gramática con producción ε ────────────────────────────────────────────
    // S → A B
    // A → a | ε
    // B → b

    @Test
    void automata_con_epsilon_no_lanza_excepcion() {
        Grammar g = grammar("""
                %token a b
                %%
                S : A B ;
                A : a | ;
                B : b ;
                """);
        assertDoesNotThrow(() -> new LR0AutomatonBuilder(g).build());
    }

    // ── LR0Item ───────────────────────────────────────────────────────────────

    @Test
    void item_toString_con_punto_al_inicio() {
        Grammar g = grammar(MINIMAL_GRAMMAR);
        Production p = g.productions().get(0); // S → a
        LR0Item item = new LR0Item(p, 0);
        assertTrue(item.toString().contains("• a"), "Punto antes de 'a': " + item);
    }

    @Test
    void item_advance_mueve_el_punto() {
        Grammar g = grammar(MINIMAL_GRAMMAR);
        Production p = g.productions().get(0); // S → a
        LR0Item item = new LR0Item(p, 0);
        LR0Item advanced = item.advance();
        assertTrue(advanced.isComplete());
        assertThrows(IllegalStateException.class, advanced::advance,
            "Avanzar un ítem completo debe lanzar IllegalStateException");
    }

    @Test
    void item_symbolAfterDot_null_cuando_completo() {
        Grammar g = grammar(MINIMAL_GRAMMAR);
        Production p = g.productions().get(0);
        LR0Item complete = new LR0Item(p, p.rhs().size());
        assertNull(complete.symbolAfterDot());
    }

    @Test
    void item_dotPosition_invalido_lanza_excepcion() {
        Grammar g = grammar(MINIMAL_GRAMMAR);
        Production p = g.productions().get(0); // S → a  (rhs.size=1)
        assertThrows(IllegalArgumentException.class, () -> new LR0Item(p, -1));
        assertThrows(IllegalArgumentException.class, () -> new LR0Item(p, 3));
    }

    // ── LR0ItemSet ────────────────────────────────────────────────────────────

    @Test
    void closure_agrega_items_de_no_terminal_tras_punto() {
        Grammar g = grammar(EXPR_GRAMMAR);
        // Crear ítem [E → • E PLUS T] con la gramática original
        Production eProds = g.productionsFor("E").get(0); // E → E PLUS T
        LR0Item seed = new LR0Item(eProds, 0);
        Set<LR0Item> closed = LR0ItemSet.closure(Set.of(seed), g);

        // El cierre debe incluir ítems para F y T también
        boolean hasF = closed.stream().anyMatch(it -> it.production().lhs().equals("F") && it.dotPosition() == 0);
        boolean hasT = closed.stream().anyMatch(it -> it.production().lhs().equals("T") && it.dotPosition() == 0);
        assertTrue(hasF, "closure debe agregar ítems de F");
        assertTrue(hasT, "closure debe agregar ítems de T");
    }

    @Test
    void dos_item_sets_con_mismos_items_son_iguales() {
        Grammar g = grammar(MINIMAL_GRAMMAR);
        Production p = g.productions().get(0);
        Set<LR0Item> items = Set.of(new LR0Item(p, 0));
        LR0ItemSet s1 = LR0ItemSet.of(0, items, g);
        LR0ItemSet s2 = LR0ItemSet.of(5, items, g); // id distinto, mismos ítems
        assertEquals(s1, s2, "La igualdad se basa en los ítems, no en el id");
    }
}
