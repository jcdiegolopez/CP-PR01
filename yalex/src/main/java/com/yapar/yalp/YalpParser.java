package com.yapar.yalp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Parser de descenso recursivo para archivos .yalp.
 *
 * <h2>Gramática del formato .yalp</h2>
 * <pre>
 *   file            → token_section PERCENT_PERCENT production_section EOF
 *   token_section   → (%token IDENTIFIER+)*
 *   production_section → production*
 *   production      → IDENTIFIER COLON alternative (PIPE alternative)* SEMICOLON
 *   alternative     → IDENTIFIER* ACTION?
 * </pre>
 *
 * Los comentarios {@code (* ... *)} son eliminados por {@link YalpLexer} antes del parseo.
 */
public final class YalpParser {

    public YalpFile parse(Path path) {
        Objects.requireNonNull(path, "path no puede ser null");
        try {
            return parse(Files.readString(path, StandardCharsets.UTF_8));
        } catch (IOException ex) {
            throw new YalpParseException(
                "No se pudo leer el archivo .yalp: " + path + " — " + ex.getMessage());
        }
    }

    public YalpFile parse(String rawSource) {
        Objects.requireNonNull(rawSource, "rawSource no puede ser null");
        List<YalpToken> tokens = new YalpLexer(rawSource).tokenize();
        return new InternalParser(tokens).parse();
    }

    // -------------------------------------------------------------------------

    private static final class InternalParser {

        private final List<YalpToken> tokens;
        private int pos = 0;

        InternalParser(List<YalpToken> tokens) {
            this.tokens = tokens;
        }

        YalpFile parse() {
            List<String> declaredTokens = parseTokenSection();
            expectPercentPercent();
            List<RawProduction> productions = parseProductionSection();
            expectEof();

            if (productions.isEmpty()) {
                throw new YalpParseException(
                    "La sección de producciones (después de %%) está vacía; se requiere al menos una producción");
            }

            return new YalpFile(declaredTokens, productions);
        }

        // ── Sección de tokens (%token ... %token ...) ─────────────────────────

        private List<String> parseTokenSection() {
            List<String> tokens = new ArrayList<>();
            while (peek().type() == YalpTokenType.PERCENT_TOKEN) {
                consume(); // %token
                if (peek().type() != YalpTokenType.IDENTIFIER) {
                    throw new YalpParseException(
                        "%token en línea " + peek().line() + " no tiene ningún identificador");
                }
                while (peek().type() == YalpTokenType.IDENTIFIER) {
                    tokens.add(consume().lexeme());
                }
            }
            return tokens;
        }

        // ── Separador %% ──────────────────────────────────────────────────────

        private void expectPercentPercent() {
            YalpToken t = consume();
            if (t.type() != YalpTokenType.PERCENT_PERCENT) {
                throw new YalpParseException(
                    "Se esperaba '%%' para separar tokens de producciones, " +
                    "pero se encontró '" + t.lexeme() + "' en línea " + t.line());
            }
        }

        // ── Sección de producciones ───────────────────────────────────────────

        private List<RawProduction> parseProductionSection() {
            List<RawProduction> prods = new ArrayList<>();
            while (peek().type() == YalpTokenType.IDENTIFIER) {
                prods.add(parseProduction());
            }
            return prods;
        }

        private RawProduction parseProduction() {
            String lhs = consume(YalpTokenType.IDENTIFIER, "nombre del no-terminal").lexeme();
            consume(YalpTokenType.COLON, "':'");

            List<RawAlternative> alternatives = new ArrayList<>();
            alternatives.add(parseAlternative());

            while (peek().type() == YalpTokenType.PIPE) {
                consume(); // |
                alternatives.add(parseAlternative());
            }

            consume(YalpTokenType.SEMICOLON, "';' al final de la producción '" + lhs + "'");
            return new RawProduction(lhs, alternatives);
        }

        private RawAlternative parseAlternative() {
            List<String> symbols = new ArrayList<>();
            while (peek().type() == YalpTokenType.IDENTIFIER) {
                symbols.add(consume().lexeme());
            }
            String action = "";
            if (peek().type() == YalpTokenType.ACTION) {
                action = consume().lexeme();
            }
            return new RawAlternative(symbols, action);
        }

        // ── Utilidades ────────────────────────────────────────────────────────

        private YalpToken peek() {
            return tokens.get(pos);
        }

        private YalpToken consume() {
            return tokens.get(pos++);
        }

        private YalpToken consume(YalpTokenType expected, String description) {
            YalpToken t = consume();
            if (t.type() != expected) {
                throw new YalpParseException(
                    "Se esperaba " + description + " pero se encontró '" +
                    t.lexeme() + "' en línea " + t.line());
            }
            return t;
        }

        private void expectEof() {
            YalpToken t = peek();
            if (t.type() != YalpTokenType.EOF) {
                throw new YalpParseException(
                    "Texto inesperado después de las producciones: '" +
                    t.lexeme() + "' en línea " + t.line());
            }
        }
    }
}
