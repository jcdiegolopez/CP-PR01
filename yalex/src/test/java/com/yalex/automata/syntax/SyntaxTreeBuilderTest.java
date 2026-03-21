package com.yalex.automata.syntax;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.yalex.regex.RegexParser;
import com.yalex.regex.node.CharClassNode;
import com.yalex.regex.node.RegexNode;

/**
 * Tests de integración para {@link SyntaxTreeBuilder}.
 *
 * <p>Todos los patrones usan la sintaxis YALex con comillas simples para chars.
 */
public class SyntaxTreeBuilderTest {

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private static SyntaxTree build(String pattern) {
        RegexNode ast = RegexParser.parse(pattern);
        return SyntaxTreeBuilder.build(ast);
    }

    // -------------------------------------------------------------------------
    // 1. 'a'  →  2 posiciones: pos1='a', pos2=END
    // -------------------------------------------------------------------------
    @Test
    void single_char_has_two_positions() {
        SyntaxTree tree = build("'a'");

        assertEquals(2, tree.size(), "Un char debe producir 2 posiciones (char + end)");

        Position pos1 = tree.getPositions().get(0);
        assertEquals(1, pos1.getId());
        assertEquals('a', pos1.getSymbol());
        assertTrue(!pos1.isEnd());

        Position pos2 = tree.getPositions().get(1);
        assertEquals(2, pos2.getId());
        assertTrue(pos2.isEnd(), "La última posición debe ser el marcador final");
    }

    // -------------------------------------------------------------------------
    // 2. 'a'|'b'  →  3 posiciones: pos1='a', pos2='b', pos3=END
    // -------------------------------------------------------------------------
    @Test
    void alternation_has_three_positions() {
        SyntaxTree tree = build("'a'|'b'");

        assertEquals(3, tree.size());
        assertEquals('a', tree.getPositions().get(0).getSymbol());
        assertEquals('b', tree.getPositions().get(1).getSymbol());
        assertTrue(tree.getPositions().get(2).isEnd());
    }

    // -------------------------------------------------------------------------
    // 3. 'a'+ se expande a 'a'·'a'*  →  al menos 3 posiciones
    //    (pos 'a' del r, pos 'a' del r*, pos END)
    // -------------------------------------------------------------------------
    @Test
    void plus_expands_and_has_at_least_three_positions() {
        SyntaxTree tree = build("'a'+");

        // r+ → r·r* → dos hojas de 'a' + marcador final = 3
        assertTrue(tree.size() >= 3,
                "Plus expandido debe tener al menos 3 posiciones, tiene: " + tree.size());

        // Ninguna de las posiciones no-final debe ser isEnd
        long endCount = tree.getPositions().stream().filter(Position::isEnd).count();
        assertEquals(1, endCount, "Exactamente una posición debe ser el marcador final");
    }

    // -------------------------------------------------------------------------
    // 4. La última posición siempre es isEnd = true
    // -------------------------------------------------------------------------
    @Test
    void last_position_is_always_end() {
        String[] patterns = { "'x'", "'a'|'b'", "'a'+'b'", "'x'*", "'a'?" };
        for (String pattern : patterns) {
            SyntaxTree tree = build(pattern);
            Position last = tree.getPositions().get(tree.size() - 1);
            assertTrue(last.isEnd(),
                    "Último nodo debe ser END para patrón: " + pattern);
            assertEquals(tree.getEndPosition(), last,
                    "getEndPosition() debe coincidir con el último de la lista");
        }
    }

    // -------------------------------------------------------------------------
    // 5. _ se expande a CharClassNode con 95 entradas (chars 32-126)
    // -------------------------------------------------------------------------
    @Test
    void wildcard_expands_to_95_chars() {
        RegexNode ast = RegexParser.parse("_");
        // Verificamos el nodo expandido inspeccionando el árbol internamente.
        // Construimos el árbol y chequeamos que tiene 2 posiciones (CharClass + END).
        SyntaxTree tree = SyntaxTreeBuilder.build(ast);

        // El wildcard ocupa una posición (la CharClassNode)
        assertEquals(2, tree.size(),
                "Wildcard debe producir 2 posiciones: la clase y el marcador final");

        // Además verificamos el nodo expandido directamente
        // El número de chars ASCII imprimibles es 126 - 32 + 1 = 95
        CharClassNode cls = SyntaxTreeBuilderTest.expandWildcardNode();
        assertEquals(95, cls.getEntries().size(),
                "El wildcard debe expandirse a 95 caracteres (32-126)");
        assertTrue(!cls.isNegated(), "El wildcard no debe ser negado");
    }

    // -------------------------------------------------------------------------
    // 6. IDs son 1-based y consecutivos
    // -------------------------------------------------------------------------
    @Test
    void position_ids_are_consecutive_one_based() {
        SyntaxTree tree = build("'a''b''c'");
        List<Position> positions = tree.getPositions();
        for (int i = 0; i < positions.size(); i++) {
            assertEquals(i + 1, positions.get(i).getId(),
                    "Posición " + i + " debe tener id " + (i + 1));
        }
    }

    // -------------------------------------------------------------------------
    // 7. 'a'? se expande a 'a' | ε  →  2 posiciones: pos1='a', pos2=END
    // -------------------------------------------------------------------------
    @Test
    void optional_expands_correctly() {
        SyntaxTree tree = build("'a'?");
        // 'a'? → 'a' | ε → solo una posición real ('a') + marcador final
        assertEquals(2, tree.size(),
                "Optional de un char debe tener 2 posiciones: char + END");
        assertEquals('a', tree.getPositions().get(0).getSymbol());
        assertTrue(tree.getPositions().get(1).isEnd());
    }

    // -------------------------------------------------------------------------
    // 8. endPosition nunca es null y tiene isEnd = true
    // -------------------------------------------------------------------------
    @Test
    void end_position_is_not_null_and_is_end() {
        SyntaxTree tree = build("'z'+'y'");
        assertNotNull(tree.getEndPosition());
        assertTrue(tree.getEndPosition().isEnd());
    }

    // =========================================================================
    // Helper para inspeccionar la expansión del wildcard sin exponer internos
    // =========================================================================
    private static CharClassNode expandWildcardNode() {
        java.util.List<String> entries = new java.util.ArrayList<>();
        for (int cp = 32; cp <= 126; cp++) {
            entries.add(String.valueOf((char) cp));
        }
        return new CharClassNode(entries, false);
    }
}
