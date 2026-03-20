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
import com.yalex.regex.node.ReferenceNode;
import com.yalex.regex.node.RegexNode;
import com.yalex.regex.node.WildcardNode;

public class RegexParserTest {

    @Test
    void parsesAlternationAndConcatenationPrecedence() {
        RegexNode root = new RegexParser().parse("'a'|'b''c'");
        AlternNode altern = assertInstanceOf(AlternNode.class, root);

        CharNode left = assertInstanceOf(CharNode.class, altern.getLeft());
        ConcatNode right = assertInstanceOf(ConcatNode.class, altern.getRight());

        assertEquals("a", left.getValue());
        assertEquals("b", assertInstanceOf(CharNode.class, right.getLeft()).getValue());
        assertEquals("c", assertInstanceOf(CharNode.class, right.getRight()).getValue());
    }

    @Test
    void parsesUnaryOperators() {
        RegexParser parser = new RegexParser();
        assertInstanceOf(KleeneNode.class, parser.parse("'a'*"));
        assertInstanceOf(PlusNode.class, parser.parse("'a'+"));
        assertInstanceOf(OptionalNode.class, parser.parse("'a'?"));
    }

    @Test
    void parsesDifferenceBeforeConcatAndUnion() {
        RegexNode root = new RegexParser().parse("'a'#'b'|'c'");
        AlternNode altern = assertInstanceOf(AlternNode.class, root);
        assertInstanceOf(DiffNode.class, altern.getLeft());
        assertEquals("c", assertInstanceOf(CharNode.class, altern.getRight()).getValue());
    }

    @Test
    void parsesStringAndWildcardAndIdentifier() {
        RegexParser parser = new RegexParser();

        RegexNode stringNode = parser.parse("\"ab\"");
        ConcatNode concat = assertInstanceOf(ConcatNode.class, stringNode);
        assertEquals("a", assertInstanceOf(CharNode.class, concat.getLeft()).getValue());
        assertEquals("b", assertInstanceOf(CharNode.class, concat.getRight()).getValue());

        assertInstanceOf(WildcardNode.class, parser.parse("_"));
        ReferenceNode referenceNode = assertInstanceOf(ReferenceNode.class, parser.parse("digit"));
        assertEquals("digit", referenceNode.getName());
    }

    @Test
    void parsesCharClassIncludingNegationAndRange() {
        RegexNode node = new RegexParser().parse("[^a-z\\n]");
        CharClassNode clazz = assertInstanceOf(CharClassNode.class, node);

        assertTrue(clazz.isNegated());
        assertEquals(2, clazz.getEntries().size());
        assertEquals("a-z", clazz.getEntries().get(0));
    }

    @Test
    void failsOnMalformedRegex() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new RegexParser().parse("('a'"));
        assertTrue(ex.getMessage().contains("se esperaba ')'"));
    }
}
