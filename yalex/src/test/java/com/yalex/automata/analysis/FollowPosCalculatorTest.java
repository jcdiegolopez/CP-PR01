package com.yalex.automata.analysis;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.yalex.automata.syntax.SyntaxTree;
import com.yalex.automata.syntax.SyntaxTreeBuilder;
import com.yalex.regex.RegexParser;
import com.yalex.regex.node.RegexNode;

class FollowPosCalculatorTest {

    private static SyntaxTree tree(String pattern) {
        RegexNode ast = RegexParser.parse(pattern);
        return SyntaxTreeBuilder.build(ast);
    }

    @Test
    void singleChar_concatEnd_followsToEndMarker() {
        SyntaxTree t = tree("'a'");
        FollowPosTable table = FollowPosCalculator.compute(t);
        // pos1 = 'a', pos2 = END; concat a · END  ⇒  follow(1) ⊇ {2}
        assertEquals(Set.of(2), table.getFollowSet(1));
        assertTrue(table.getFollowSet(2).isEmpty());
    }

    @Test
    void alternation_bothBranchesLeadToEnd() {
        SyntaxTree t = tree("'a'|'b'");
        FollowPosTable table = FollowPosCalculator.compute(t);
        assertEquals(Set.of(3), table.getFollowSet(1));
        assertEquals(Set.of(3), table.getFollowSet(2));
        assertTrue(table.getFollowSet(3).isEmpty());
    }

    @Test
    void kleeneStar_selfLoopAndExitToEnd() {
        SyntaxTree t = tree("'a'*");
        FollowPosTable table = FollowPosCalculator.compute(t);
        // pos1 = 'a' dentro del *, pos2 = END
        assertEquals(Set.of(1, 2), table.getFollowSet(1));
        assertTrue(table.getFollowSet(2).isEmpty());
    }

    @Test
    void nullableFirstLast_consistentWithAnnotatedRoot() {
        SyntaxTree t = tree("'a'|'b'");
        Map<RegexNode, NodeAnnotation> ann = NullableFirstLast.annotate(t);
        NodeAnnotation root = ann.get(t.getRoot());
        assertFalse(root.nullable());
        assertEquals(Set.of(1, 2), root.firstpos());
        assertEquals(Set.of(3), root.lastpos());
    }

    @Test
    void diff_nullableOnlyWhenLeftEmptyAndRightNot() {
        SyntaxTree t = tree("'a'#'a'");
        Map<RegexNode, NodeAnnotation> ann = NullableFirstLast.annotate(t);
        NodeAnnotation root = ann.get(t.getRoot());
        assertFalse(root.nullable());
    }

    @Test
    void epsilon_topLevel_concatWithEnd_doesNotThrow() {
        SyntaxTree t = tree("epsilon");
        Map<RegexNode, NodeAnnotation> ann = NullableFirstLast.annotate(t);
        NodeAnnotation root = ann.get(t.getRoot());
        assertFalse(root.nullable());
        assertEquals(Set.of(1), root.firstpos());
        assertEquals(Set.of(1), root.lastpos());
    }

    @Test
    void epsilon_concat_char_annotatesWithoutException() {
        SyntaxTree t = tree("epsilon'a'");
        assertDoesNotThrow(() -> NullableFirstLast.annotate(t));
        assertDoesNotThrow(() -> FollowPosCalculator.compute(t));
    }
}
