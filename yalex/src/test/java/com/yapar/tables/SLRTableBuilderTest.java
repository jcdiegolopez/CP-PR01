package com.yapar.tables;

import com.yapar.analysis.FirstFollowCalculator;
import com.yapar.automaton.LR0Automaton;
import com.yapar.automaton.LR0AutomatonBuilder;
import com.yapar.model.Grammar;
import com.yapar.yalp.YalpParser;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de {@link SLRTableBuilder}.
 *
 * La gramática de expresiones del Dragon Book (§4.6) no tiene conflictos SLR(1)
 * y su tabla se puede verificar a mano. Se usa también para probar detección de conflictos
 * con una gramática ambigua.
 */
class SLRTableBuilderTest {

    private static final YalpParser PARSER = new YalpParser();

    // ── Gramática de expresiones (Dragon Book §4.6) ───────────────────────────
    // E → E PLUS T | T
    // T → T TIMES F | F
    // F → LPAREN E RPAREN | id
    // 12 estados, 0 conflictos SLR(1)

    private static final String EXPR_GRAMMAR = """
            %token PLUS TIMES LPAREN RPAREN id
            %%
            E : E PLUS T | T ;
            T : T TIMES F | F ;
            F : LPAREN E RPAREN | id ;
            """;

    private static ParseResult buildResult(String yalp) {
        Grammar g = Grammar.build(PARSER.parse(yalp));
        LR0Automaton automaton = new LR0AutomatonBuilder(g).build();
        Map<String, Set<String>> follow = new FirstFollowCalculator(g).getFollowSets();
        return new SLRTableBuilder(automaton, follow).build();
    }

    // ── Sin conflictos ────────────────────────────────────────────────────────

    @Test
    void expresiones_sin_conflictos() {
        ParseResult result = buildResult(EXPR_GRAMMAR);
        assertFalse(result.hasConflicts(),
            "La gramática de expresiones no debe tener conflictos SLR(1)");
        assertEquals(0, result.conflicts().size());
    }

    @Test
    void estado0_shift_sobre_id_y_LPAREN() {
        ParseResult result = buildResult(EXPR_GRAMMAR);
        ParseTable table = result.table();

        assertInstanceOf(SLRAction.Shift.class, table.action(0, "id"),
            "ACTION[0][id] debe ser Shift");
        assertInstanceOf(SLRAction.Shift.class, table.action(0, "LPAREN"),
            "ACTION[0][LPAREN] debe ser Shift");
    }

    @Test
    void celda_vacia_devuelve_Error() {
        ParseResult result = buildResult(EXPR_GRAMMAR);
        assertInstanceOf(SLRAction.Error.class, result.table().action(0, "$"),
            "Una celda vacía debe devolver Error");
    }

    @Test
    void accept_existe_en_algun_estado() {
        ParseResult result = buildResult(EXPR_GRAMMAR);
        ParseTable table = result.table();

        boolean found = false;
        for (Map.Entry<Integer, Map<String, SLRAction>> stateEntry
                : table.actionTable().entrySet()) {
            if (stateEntry.getValue().get("$") instanceof SLRAction.Accept) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Debe existir exactamente un Accept en ACTION[?][$]");
    }

    @Test
    void goto_de_E_desde_estado0_existe() {
        ParseResult result = buildResult(EXPR_GRAMMAR);
        assertTrue(result.table().gotoState(0, "E").isPresent(),
            "GOTO[0][E] debe existir");
    }

    // ── Gramática mínima S → a ────────────────────────────────────────────────

    private static final String MINIMAL = """
            %token a
            %%
            S : a ;
            """;

    @Test
    void minimal_accept_en_dolar() {
        ParseResult result = buildResult(MINIMAL);
        ParseTable table = result.table();

        boolean hasAccept = table.actionTable().values().stream()
            .anyMatch(row -> row.get("$") instanceof SLRAction.Accept);
        assertTrue(hasAccept);
    }

    @Test
    void minimal_reduce_en_follow_de_S() {
        ParseResult result = buildResult(MINIMAL);
        ParseTable table = result.table();

        boolean hasReduce = table.actionTable().values().stream()
            .anyMatch(row -> row.values().stream()
                .anyMatch(a -> a instanceof SLRAction.Reduce));
        assertTrue(hasReduce, "Debe haber al menos una entrada Reduce");
    }

    // ── Gramática con conflicto shift/reduce ──────────────────────────────────
    // La gramática ambigua "dangling else" produce shift/reduce

    private static final String AMBIGUOUS = """
            %token IF ELSE EXPR
            %%
            S : IF EXPR S ELSE S
              | IF EXPR S
              | EXPR
              ;
            """;

    @Test
    void gramatica_ambigua_detecta_conflicto_shift_reduce() {
        ParseResult result = buildResult(AMBIGUOUS);
        assertTrue(result.hasConflicts(),
            "La gramática ambigua debe producir al menos un conflicto shift/reduce");

        boolean hasShiftReduce = result.conflicts().stream()
            .anyMatch(c -> c.type() == Conflict.ConflictType.SHIFT_REDUCE);
        assertTrue(hasShiftReduce, "El conflicto debe ser de tipo SHIFT_REDUCE");
    }

    @Test
    void conflicto_shift_reduce_gana_shift() {
        ParseResult result = buildResult(AMBIGUOUS);

        for (Conflict c : result.conflicts()) {
            if (c.type() == Conflict.ConflictType.SHIFT_REDUCE) {
                SLRAction winner = result.table().action(c.state(), c.symbol());
                assertInstanceOf(SLRAction.Shift.class, winner,
                    "Shift debe ganar sobre Reduce en " + c);
            }
        }
    }
}
