package com.yalex.regex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.yalex.regex.node.AlternNode;
import com.yalex.regex.node.CharClassNode;
import com.yalex.regex.node.CharNode;
import com.yalex.regex.node.ConcatNode;
import com.yalex.regex.node.DiffNode;
import com.yalex.regex.node.KleeneNode;
import com.yalex.regex.node.OptionalNode;
import com.yalex.regex.node.PlusNode;
import com.yalex.regex.node.RegexNode;
import com.yalex.regex.node.WildcardNode;

/**
 * Tests de integración para {@link RegexParser}.
 * Cubre los casos pedidos en la spec: a|b, a*, [0-9]+, (a|b)#c, _, "abc" y error.
 */
public class RegexParserTest {

    // -------------------------------------------------------------------------
    // 1. a | b   →  AlternNode(CharNode(a), CharNode(b))
    // -------------------------------------------------------------------------
    @Test
    void alternation_simple() {
        RegexNode root = RegexParser.parse("'a'|'b'");
        AlternNode altern = assertInstanceOf(AlternNode.class, root);
        assertEquals("a", assertInstanceOf(CharNode.class, altern.getLeft()).getValue());
        assertEquals("b", assertInstanceOf(CharNode.class, altern.getRight()).getValue());
    }

    // -------------------------------------------------------------------------
    // 2. a*  →  KleeneNode(CharNode(a))
    // -------------------------------------------------------------------------
    @Test
    void kleene_star() {
        RegexNode root = RegexParser.parse("'a'*");
        KleeneNode kleene = assertInstanceOf(KleeneNode.class, root);
        assertEquals("a", assertInstanceOf(CharNode.class, kleene.getChild()).getValue());
    }

    // -------------------------------------------------------------------------
    // 3. [0-9]+  →  PlusNode(CharClassNode([0-9]))
    // -------------------------------------------------------------------------
    @Test
    void charClass_plus() {
        // YALex usa comillas simples dentro de clases: ['0'-'9']
        RegexNode root = RegexParser.parse("['0'-'9']+");
        PlusNode plus = assertInstanceOf(PlusNode.class, root);
        CharClassNode cls = assertInstanceOf(CharClassNode.class, plus.getChild());
        assertEquals(1, cls.getEntries().size());
        assertEquals("0-9", cls.getEntries().get(0));
    }

    // -------------------------------------------------------------------------
    // 4. (a|b)#c  →  DiffNode(AlternNode, CharNode(c))
    //    Verifica precedencia: # > |  pero concat > #
    // -------------------------------------------------------------------------
    @Test
    void diff_precedence_over_alternation() {
        // 'a'|'b' se parsea como un grupo, luego # 'c'
        RegexNode root = RegexParser.parse("('a'|'b')#'c'");
        DiffNode diff = assertInstanceOf(DiffNode.class, root);
        assertInstanceOf(AlternNode.class, diff.getLeft());
        assertEquals("c", assertInstanceOf(CharNode.class, diff.getRight()).getValue());
    }

    // -------------------------------------------------------------------------
    // 5. _  →  WildcardNode
    // -------------------------------------------------------------------------
    @Test
    void wildcard() {
        RegexNode root = RegexParser.parse("_");
        assertInstanceOf(WildcardNode.class, root);
    }

    // -------------------------------------------------------------------------
    // 6. "abc"  →  ConcatNode(ConcatNode(CharNode(a), CharNode(b)), CharNode(c))
    // -------------------------------------------------------------------------
    @Test
    void string_expansion() {
        RegexNode root = RegexParser.parse("\"abc\"");
        // El resultado es ((a . b) . c)
        ConcatNode outer = assertInstanceOf(ConcatNode.class, root);
        ConcatNode inner = assertInstanceOf(ConcatNode.class, outer.getLeft());

        assertEquals("a", assertInstanceOf(CharNode.class, inner.getLeft()).getValue());
        assertEquals("b", assertInstanceOf(CharNode.class, inner.getRight()).getValue());
        assertEquals("c", assertInstanceOf(CharNode.class, outer.getRight()).getValue());
    }

    // -------------------------------------------------------------------------
    // 7. Error de parseo
    // -------------------------------------------------------------------------
    @Test
    void malformed_unclosed_paren_throws() {
        RegexParseException ex = assertThrows(RegexParseException.class,
                () -> RegexParser.parse("('a'"));
        assertTrue(ex.getMessage().contains("se esperaba ')'"),
                "Mensaje esperado debe mencionar ')': " + ex.getMessage());
    }

    // -------------------------------------------------------------------------
    // Casos adicionales
    // -------------------------------------------------------------------------

    @Test
    void unary_operators() {
        assertInstanceOf(KleeneNode.class,   RegexParser.parse("'x'*"));
        assertInstanceOf(PlusNode.class,     RegexParser.parse("'x'+"));
        assertInstanceOf(OptionalNode.class, RegexParser.parse("'x'?"));
    }

    @Test
    void unresolved_identifier_throws() {
        // Las referencias a 'let' deben expandirse antes de llamar al parser.
        // Un identificador sin resolver debe lanzar RegexParseException.
        RegexParseException ex = assertThrows(RegexParseException.class,
                () -> RegexParser.parse("digit"));
        assertTrue(ex.getMessage().contains("digit"),
                "El mensaje debe incluir el nombre del identificador");
    }

    @Test
    void negated_char_class() {
        RegexNode root = RegexParser.parse("[^'a'-'z']");
        CharClassNode cls = assertInstanceOf(CharClassNode.class, root);
        assertTrue(cls.isNegated());
        assertEquals(1, cls.getEntries().size());
        assertEquals("a-z", cls.getEntries().get(0));
    }

    @Test
    void alternation_with_concat_on_right() {
        // 'a' | 'b''c'  →  altern(CharNode(a), concat(CharNode(b), CharNode(c)))
        RegexNode root = RegexParser.parse("'a'|'b''c'");
        AlternNode altern = assertInstanceOf(AlternNode.class, root);
        ConcatNode right = assertInstanceOf(ConcatNode.class, altern.getRight());
        assertEquals("b", assertInstanceOf(CharNode.class, right.getLeft()).getValue());
        assertEquals("c", assertInstanceOf(CharNode.class, right.getRight()).getValue());
    }

    @Test
    void diff_binds_tighter_than_pipe_without_parens() {
        // 'a' # 'b' | 'c'  →  altern(diff(a, b), c)
        RegexNode root = RegexParser.parse("'a'#'b'|'c'");
        AlternNode altern = assertInstanceOf(AlternNode.class, root);
        assertInstanceOf(DiffNode.class, altern.getLeft());
        assertEquals("c", assertInstanceOf(CharNode.class, altern.getRight()).getValue());
    }

    @Test
    void nullOrBlank_throws() {
        assertThrows(NullPointerException.class, () -> RegexParser.parse(null));
        assertThrows(RegexParseException.class,  () -> RegexParser.parse("   "));
    }
}
