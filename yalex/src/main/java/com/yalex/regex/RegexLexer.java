package com.yalex.regex;

import java.util.Objects;

public class RegexLexer {

    private final String source;
    private int index;

    public RegexLexer(String source) {
        this.source = Objects.requireNonNull(source, "source no puede ser null");
        this.index = 0;
    }

    public Token nextToken() {
        skipWhitespace();
        if (index >= source.length()) {
            return new Token(TokenType.EOF, "", index);
        }

        char c = source.charAt(index);
        int start = index;

        if (c == '|') {
            index++;
            return new Token(TokenType.PIPE, "|", start);
        }
        if (c == '#') {
            index++;
            return new Token(TokenType.DIFF, "#", start);
        }
        if (c == '*') {
            index++;
            return new Token(TokenType.STAR, "*", start);
        }
        if (c == '+') {
            index++;
            return new Token(TokenType.PLUS, "+", start);
        }
        if (c == '?') {
            index++;
            return new Token(TokenType.QUESTION, "?", start);
        }
        if (c == '(') {
            index++;
            return new Token(TokenType.LPAREN, "(", start);
        }
        if (c == ')') {
            index++;
            return new Token(TokenType.RPAREN, ")", start);
        }
        if (c == '_') {
            index++;
            return new Token(TokenType.WILDCARD, "_", start);
        }
        if (c == '\\') {
            String escaped = parseEscapedOutsideLiteral();
            return new Token(TokenType.CHAR, escaped, start);
        }
        if (c == '\'') {
            String value = parseQuotedChar();
            return new Token(TokenType.CHAR, value, start);
        }
        if (c == '"') {
            String value = parseString();
            return new Token(TokenType.STRING, value, start);
        }
        if (c == '[') {
            String value = parseCharClass();
            return new Token(TokenType.CHAR_CLASS, value, start);
        }
        if (Character.isLetter(c) || c == '_') {
            String ident = parseIdentifier();
            return new Token(TokenType.IDENTIFIER, ident, start);
        }

        index++;
        return new Token(TokenType.CHAR, String.valueOf(c), start);
    }

    private void skipWhitespace() {
        while (index < source.length() && Character.isWhitespace(source.charAt(index))) {
            index++;
        }
    }

    private String parseIdentifier() {
        int start = index;
        index++;
        while (index < source.length()) {
            char c = source.charAt(index);
            if (!Character.isLetterOrDigit(c) && c != '_') {
                break;
            }
            index++;
        }
        return source.substring(start, index);
    }

    private String parseEscapedOutsideLiteral() {
        index++;
        if (index >= source.length()) {
            throw new IllegalArgumentException("escape incompleto en regex");
        }
        char escaped = source.charAt(index++);
        return mapEscape(escaped);
    }

    private String parseQuotedChar() {
        index++;
        if (index >= source.length()) {
            throw new IllegalArgumentException("char sin cierre en regex");
        }

        String value;
        char c = source.charAt(index);
        if (c == '\\') {
            index++;
            if (index >= source.length()) {
                throw new IllegalArgumentException("escape incompleto en char");
            }
            value = mapEscape(source.charAt(index));
            index++;
        } else {
            value = String.valueOf(c);
            index++;
        }

        if (index >= source.length() || source.charAt(index) != '\'') {
            throw new IllegalArgumentException("char sin cierre en regex");
        }
        index++;
        return value;
    }

    private String parseString() {
        index++;
        StringBuilder sb = new StringBuilder();
        while (index < source.length()) {
            char c = source.charAt(index);
            if (c == '"') {
                index++;
                return sb.toString();
            }
            if (c == '\\') {
                index++;
                if (index >= source.length()) {
                    throw new IllegalArgumentException("escape incompleto en string");
                }
                sb.append(mapEscape(source.charAt(index)));
                index++;
                continue;
            }
            sb.append(c);
            index++;
        }
        throw new IllegalArgumentException("string sin cierre en regex");
    }

    private String parseCharClass() {
        int start = index;
        index++;
        boolean escaped = false;
        while (index < source.length()) {
            char c = source.charAt(index);
            if (escaped) {
                escaped = false;
                index++;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                index++;
                continue;
            }
            if (c == ']') {
                index++;
                return source.substring(start, index);
            }
            index++;
        }
        throw new IllegalArgumentException("clase de caracteres sin cierre");
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

    public enum TokenType {
        PIPE,
        DIFF,
        STAR,
        PLUS,
        QUESTION,
        LPAREN,
        RPAREN,
        WILDCARD,
        CHAR,
        STRING,
        CHAR_CLASS,
        IDENTIFIER,
        EOF
    }

    public record Token(TokenType type, String lexeme, int position) {
    }
}
