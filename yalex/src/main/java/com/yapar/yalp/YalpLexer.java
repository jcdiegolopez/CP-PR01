package com.yapar.yalp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Tokeniza un archivo .yalp.
 * Reconoce: %token, %%, :, |, ;, identificadores y bloques de acción { }.
 * Los comentarios (* ... *) se eliminan antes de tokenizar.
 */
public final class YalpLexer {

    private final String source;
    private int pos = 0;
    private int line = 1;

    public YalpLexer(String rawSource) {
        Objects.requireNonNull(rawSource, "rawSource no puede ser null");
        this.source = stripComments(rawSource);
    }

    static String stripComments(String src) {
        StringBuilder sb = new StringBuilder(src.length());
        int depth = 0;
        for (int i = 0; i < src.length(); i++) {
            char c = src.charAt(i);
            char next = (i + 1 < src.length()) ? src.charAt(i + 1) : '\0';
            if (c == '(' && next == '*') {
                depth++;
                i++;
            } else if (c == '*' && next == ')' && depth > 0) {
                depth--;
                i++;
            } else if (depth == 0) {
                sb.append(c);
            } else if (c == '\n') {
                sb.append('\n'); // preservar líneas dentro de comentarios para el conteo
            }
        }
        if (depth != 0) {
            throw new YalpParseException("Comentario (* ... *) sin cerrar en el archivo .yalp");
        }
        return sb.toString();
    }

    public List<YalpToken> tokenize() {
        List<YalpToken> tokens = new ArrayList<>();
        while (pos < source.length()) {
            skipWhitespace();
            if (pos >= source.length()) break;

            char c = source.charAt(pos);

            if (c == '%') {
                tokens.add(readPercent());
            } else if (c == ':') {
                tokens.add(tok(YalpTokenType.COLON, ":"));
                pos++;
            } else if (c == '|') {
                tokens.add(tok(YalpTokenType.PIPE, "|"));
                pos++;
            } else if (c == ';') {
                tokens.add(tok(YalpTokenType.SEMICOLON, ";"));
                pos++;
            } else if (c == '{') {
                tokens.add(readAction());
            } else if (Character.isLetter(c) || c == '_') {
                tokens.add(readIdentifier());
            } else {
                throw new YalpParseException(
                    "Carácter inesperado '" + c + "' en línea " + line);
            }
        }
        tokens.add(new YalpToken(YalpTokenType.EOF, "", line));
        return tokens;
    }

    private void skipWhitespace() {
        while (pos < source.length()) {
            char c = source.charAt(pos);
            if (c == '\n') { line++; pos++; }
            else if (Character.isWhitespace(c)) { pos++; }
            else break;
        }
    }

    private YalpToken tok(YalpTokenType type, String lexeme) {
        return new YalpToken(type, lexeme, line);
    }

    private YalpToken readPercent() {
        int startLine = line;
        if (source.startsWith("%%", pos)) {
            pos += 2;
            return new YalpToken(YalpTokenType.PERCENT_PERCENT, "%%", startLine);
        }
        if (source.startsWith("%token", pos)) {
            int after = pos + 6;
            if (after >= source.length() || isWordBoundary(source.charAt(after))) {
                pos += 6;
                return new YalpToken(YalpTokenType.PERCENT_TOKEN, "%token", startLine);
            }
        }
        throw new YalpParseException("Directiva desconocida con '%' en línea " + line);
    }

    private boolean isWordBoundary(char c) {
        return !Character.isLetterOrDigit(c) && c != '_';
    }

    private YalpToken readIdentifier() {
        int start = pos;
        int startLine = line;
        while (pos < source.length()
               && (Character.isLetterOrDigit(source.charAt(pos)) || source.charAt(pos) == '_')) {
            pos++;
        }
        return new YalpToken(YalpTokenType.IDENTIFIER, source.substring(start, pos), startLine);
    }

    private YalpToken readAction() {
        int startLine = line;
        int depth = 0;
        StringBuilder sb = new StringBuilder();
        while (pos < source.length()) {
            char c = source.charAt(pos);
            if (c == '{') {
                depth++;
                if (depth > 1) sb.append(c);
                pos++;
            } else if (c == '}') {
                depth--;
                pos++;
                if (depth == 0) break;
                sb.append(c);
            } else {
                if (c == '\n') line++;
                sb.append(c);
                pos++;
            }
        }
        if (depth != 0) {
            throw new YalpParseException(
                "Bloque de acción { } sin cerrar, abierto en línea " + startLine);
        }
        return new YalpToken(YalpTokenType.ACTION, sb.toString().trim(), startLine);
    }
}
