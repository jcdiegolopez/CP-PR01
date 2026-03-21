package com.yalex.regex;

import java.util.Objects;

/**
 * Tokenizador para cadenas de expresiones regulares en formato YALex.
 *
 * <p>Tokens reconocidos:
 * <ul>
 *   <li>{@code PIPE}        — {@code |}
 *   <li>{@code HASH}        — {@code #}  (diferencia)
 *   <li>{@code STAR}        — {@code *}
 *   <li>{@code PLUS}        — {@code +}
 *   <li>{@code QUESTION}    — {@code ?}
 *   <li>{@code LPAREN}      — {@code (}
 *   <li>{@code RPAREN}      — {@code )}
 *   <li>{@code LBRACKET}    — {@code [}  (inicio de clase)
 *   <li>{@code RBRACKET}    — {@code ]}  (fin de clase)
 *   <li>{@code WILDCARD}    — {@code _}
 *   <li>{@code CHAR}        — carácter entre comillas simples {@code 'c'} o escape {@code \n}
 *   <li>{@code STRING}      — literal entre comillas dobles {@code "abc"}
 *   <li>{@code CHAR_CLASS}  — todo el bloque {@code [...]}
 *   <li>{@code IDENTIFIER}  — nombre de let sin comillas (letras/dígitos/_)
 *   <li>{@code EOF}         — fin de la cadena
 * </ul>
 *
 * <p>Secuencias de escape soportadas dentro de {@code '...'} y {@code "..."}:
 * {@code \n}, {@code \t}, {@code \r}, {@code \\}, {@code \'}, {@code \"}.
 */
public class RegexLexer {

    private final String source;
    private int index;

    public RegexLexer(String source) {
        this.source = Objects.requireNonNull(source, "source no puede ser null");
        this.index = 0;
    }

    // -------------------------------------------------------------------------
    // API pública
    // -------------------------------------------------------------------------

    public Token nextToken() {
        skipWhitespace();
        if (index >= source.length()) {
            return tok(TokenType.EOF, "", index);
        }

        char c = source.charAt(index);
        int start = index;

        switch (c) {
            case '|': index++; return tok(TokenType.PIPE, "|", start);
            case '#': index++; return tok(TokenType.HASH, "#", start);
            case '*': index++; return tok(TokenType.STAR, "*", start);
            case '+': index++; return tok(TokenType.PLUS, "+", start);
            case '?': index++; return tok(TokenType.QUESTION, "?", start);
            case '(': index++; return tok(TokenType.LPAREN, "(", start);
            case ')': index++; return tok(TokenType.RPAREN, ")", start);
            case '_': index++; return tok(TokenType.WILDCARD, "_", start);
            case '[': {
                // Consume el bloque completo [...] tal cual para que el parser lo analice
                String raw = consumeCharClassRaw();
                return tok(TokenType.CHAR_CLASS, raw, start);
            }
            case '\'': {
                String value = parseQuotedChar();
                return tok(TokenType.CHAR, value, start);
            }
            case '"': {
                String value = parseString();
                return tok(TokenType.STRING, value, start);
            }
            case '\\': {
                String value = parseEscapeOutside();
                return tok(TokenType.CHAR, value, start);
            }
            default: {
                if (Character.isLetter(c)) {
                    return tok(TokenType.IDENTIFIER, parseIdentifier(), start);
                }
                // Cualquier otro carácter se trata como literal
                index++;
                return tok(TokenType.CHAR, String.valueOf(c), start);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Helpers internos
    // -------------------------------------------------------------------------

    private void skipWhitespace() {
        while (index < source.length() && Character.isWhitespace(source.charAt(index))) {
            index++;
        }
    }

    private String parseIdentifier() {
        int start = index;
        index++; // primera letra ya verificada
        while (index < source.length()) {
            char c = source.charAt(index);
            if (!Character.isLetterOrDigit(c) && c != '_') break;
            index++;
        }
        return source.substring(start, index);
    }

    /** Parsea una secuencia de escape fuera de literales: {@code \n}, {@code \t}, etc. */
    private String parseEscapeOutside() {
        index++; // consume '\'
        if (index >= source.length()) {
            throw new RegexParseException("escape incompleto al final de la regexp");
        }
        char escaped = source.charAt(index++);
        return mapEscape(escaped);
    }

    /** Parsea un carácter entre comillas simples: {@code 'c'} o {@code '\n'}. */
    private String parseQuotedChar() {
        index++; // consume '\''
        if (index >= source.length()) {
            throw new RegexParseException("char literal sin contenido ni cierre");
        }
        String value;
        char c = source.charAt(index);
        if (c == '\\') {
            index++;
            if (index >= source.length()) {
                throw new RegexParseException("escape incompleto dentro de char literal");
            }
            value = mapEscape(source.charAt(index++));
        } else {
            value = String.valueOf(c);
            index++;
        }
        if (index >= source.length() || source.charAt(index) != '\'') {
            throw new RegexParseException("char literal sin comilla de cierre después de: " + value);
        }
        index++; // consume '\''
        return value;
    }

    /** Parsea el contenido de un string entre comillas dobles; retorna los chars ya resueltos. */
    private String parseString() {
        index++; // consume '"'
        StringBuilder sb = new StringBuilder();
        while (index < source.length()) {
            char c = source.charAt(index);
            if (c == '"') {
                index++;
                return sb.toString();
            }
            if (c == '\\') {
                index++;
                if (index >= source.length()) throw new RegexParseException("escape incompleto en string");
                sb.append(mapEscape(source.charAt(index++)));
            } else {
                sb.append(c);
                index++;
            }
        }
        throw new RegexParseException("string sin comilla de cierre");
    }

    /**
     * Consume el bloque {@code [...]}, incluyendo los corchetes y manejando
     * escapes dentro para no confundir un {@code \]} con el cierre real.
     */
    private String consumeCharClassRaw() {
        int start = index;
        index++; // consume '['
        while (index < source.length()) {
            char c = source.charAt(index);
            if (c == '\\') {
                index += 2; // consume escape + siguiente
                continue;
            }
            if (c == ']') {
                index++;
                return source.substring(start, index);
            }
            index++;
        }
        throw new RegexParseException("clase de caracteres '[...]' sin corchete de cierre");
    }

    private String mapEscape(char escaped) {
        return switch (escaped) {
            case 'n'  -> "\n";
            case 't'  -> "\t";
            case 'r'  -> "\r";
            case '\\' -> "\\";
            case '\'' -> "'";
            case '"'  -> "\"";
            default   -> String.valueOf(escaped);
        };
    }

    private Token tok(TokenType type, String lexeme, int position) {
        return new Token(type, lexeme, position);
    }

    // -------------------------------------------------------------------------
    // Tipos públicos
    // -------------------------------------------------------------------------

    public enum TokenType {
        PIPE,         // |
        HASH,         // #
        STAR,         // *
        PLUS,         // +
        QUESTION,     // ?
        LPAREN,       // (
        RPAREN,       // )
        WILDCARD,     // _
        CHAR,         // carácter literal
        STRING,       // "abc"
        CHAR_CLASS,   // [...]
        IDENTIFIER,   // nombre de let
        EOF
    }

    public record Token(TokenType type, String lexeme, int position) {}
}
