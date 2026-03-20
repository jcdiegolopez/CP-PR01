package com.yalex.regex;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.yalex.regex.RegexLexer.Token;
import com.yalex.regex.RegexLexer.TokenType;
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

public class RegexParser {

    private RegexLexer lexer;
    private Token current;
    private Token previous;

    public RegexNode parse(String source) {
        Objects.requireNonNull(source, "source no puede ser null");
        this.lexer = new RegexLexer(source);
        this.current = lexer.nextToken();
        this.previous = null;

        RegexNode node = parseAlternation();
        expect(TokenType.EOF, "se esperaba fin de regex");
        return node;
    }

    // Precedencia baja: |
    private RegexNode parseAlternation() {
        RegexNode left = parseConcatenation();
        while (match(TokenType.PIPE)) {
            RegexNode right = parseConcatenation();
            left = new AlternNode(left, right);
        }
        return left;
    }

    // Concatenacion implicita.
    private RegexNode parseConcatenation() {
        List<RegexNode> nodes = new ArrayList<>();
        nodes.add(parseDifference());

        while (canStartPrimary(current.type())) {
            nodes.add(parseDifference());
        }

        RegexNode currentNode = nodes.get(0);
        for (int i = 1; i < nodes.size(); i++) {
            currentNode = new ConcatNode(currentNode, nodes.get(i));
        }
        return currentNode;
    }

    // Precedencia alta: #
    private RegexNode parseDifference() {
        RegexNode left = parseUnary();
        while (match(TokenType.DIFF)) {
            RegexNode right = parseUnary();
            left = new DiffNode(left, right);
        }
        return left;
    }

    // Operadores postfix: *, +, ?
    private RegexNode parseUnary() {
        RegexNode node = parsePrimary();
        while (true) {
            if (match(TokenType.STAR)) {
                node = new KleeneNode(node);
                continue;
            }
            if (match(TokenType.PLUS)) {
                node = new PlusNode(node);
                continue;
            }
            if (match(TokenType.QUESTION)) {
                node = new OptionalNode(node);
                continue;
            }
            break;
        }
        return node;
    }

    private RegexNode parsePrimary() {
        if (match(TokenType.LPAREN)) {
            RegexNode inner = parseAlternation();
            expect(TokenType.RPAREN, "se esperaba ')' ");
            return inner;
        }
        if (match(TokenType.WILDCARD)) {
            return new WildcardNode();
        }
        if (match(TokenType.CHAR)) {
            return new CharNode(previous().lexeme());
        }
        if (match(TokenType.STRING)) {
            return buildStringNode(previous().lexeme());
        }
        if (match(TokenType.CHAR_CLASS)) {
            return parseCharClass(previous().lexeme());
        }
        if (match(TokenType.IDENTIFIER)) {
            String name = previous().lexeme();
            if ("eof".equals(name)) {
                return new CharNode("EOF");
            }
            return new ReferenceNode(name);
        }
        throw error("token inesperado en regex: " + current.lexeme());
    }

    private RegexNode parseCharClass(String raw) {
        String body = raw.substring(1, raw.length() - 1);
        boolean negated = false;
        int index = 0;
        if (!body.isEmpty() && body.charAt(0) == '^') {
            negated = true;
            index = 1;
        }

        List<String> entries = new ArrayList<>();
        while (index < body.length()) {
            String first = readClassSymbol(body, index);
            index += symbolLength(body, index);

            if (index + 1 < body.length() && body.charAt(index) == '-') {
                index++;
                String second = readClassSymbol(body, index);
                index += symbolLength(body, index);
                entries.add(first + "-" + second);
                continue;
            }
            entries.add(first);
        }

        return new CharClassNode(entries, negated);
    }

    private String readClassSymbol(String body, int index) {
        if (index >= body.length()) {
            throw error("simbolo invalido en clase de caracteres");
        }
        char c = body.charAt(index);
        if (c == '\\') {
            if (index + 1 >= body.length()) {
                throw error("escape incompleto en clase de caracteres");
            }
            return mapEscape(body.charAt(index + 1));
        }
        return String.valueOf(c);
    }

    private int symbolLength(String body, int index) {
        return body.charAt(index) == '\\' ? 2 : 1;
    }

    private String mapEscape(char escaped) {
        return switch (escaped) {
            case 'n' -> "\n";
            case 't' -> "\t";
            case 'r' -> "\r";
            case '\\' -> "\\";
            case '\'' -> "'";
            case '"' -> "\"";
            default -> String.valueOf(escaped);
        };
    }

    private RegexNode buildStringNode(String value) {
        if (value.isEmpty()) {
            throw error("string vacio no soportado");
        }
        RegexNode node = new CharNode(String.valueOf(value.charAt(0)));
        for (int i = 1; i < value.length(); i++) {
            node = new ConcatNode(node, new CharNode(String.valueOf(value.charAt(i))));
        }
        return node;
    }

    private boolean canStartPrimary(TokenType type) {
        return type == TokenType.LPAREN
            || type == TokenType.WILDCARD
            || type == TokenType.CHAR
            || type == TokenType.STRING
            || type == TokenType.CHAR_CLASS
            || type == TokenType.IDENTIFIER;
    }

    private boolean match(TokenType type) {
        if (current.type() != type) {
            return false;
        }
        previous = current;
        current = lexer.nextToken();
        return true;
    }

    private Token previous() {
        if (previous == null) {
            throw new IllegalStateException("no hay token previo disponible");
        }
        return previous;
    }

    private void expect(TokenType type, String message) {
        if (!match(type)) {
            throw error(message + " en posicion " + current.position());
        }
    }

    private IllegalArgumentException error(String message) {
        return new IllegalArgumentException(message);
    }
}
