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
import com.yalex.regex.node.EpsilonRegexNode;
import com.yalex.regex.node.KleeneNode;
import com.yalex.regex.node.OptionalNode;
import com.yalex.regex.node.PlusNode;
import com.yalex.regex.node.RegexNode;
import com.yalex.regex.node.WildcardNode;

/**
 * Parser recursivo descendente para expresiones regulares en formato YALex.
 *
 * <p>
 * Gramática con precedencia (de menor a mayor):
 * 
 * <pre>
 *   expr    → altern
 *   altern  → diff ("|" diff)*
 *   diff    → concat ("#" concat)*
 *   concat  → unary unary*
 *   unary   → atom ("*" | "+" | "?")*
 *   atom    → CHAR
 *           | STRING          (expandido como concat de CharNodes)
 *           | WILDCARD
 *           | IDENTIFIER      (referencia a let → ReferenceNode, "eof" especial)
 *           | "(" expr ")"
 *           | CHAR_CLASS      (p.ej. [a-z0-9])
 * </pre>
 *
 * <p>
 * Punto de entrada estático:
 * 
 * <pre>{@code
 * RegexNode ast = RegexParser.parse("digit+");
 * }</pre>
 */
public class RegexParser {

    private RegexLexer lexer;
    private Token current;
    private Token previous;

    // Constructor privado — uso solo a través de parse()
    private RegexParser() {
    }

    // =========================================================================
    // Punto de entrada estático
    // =========================================================================

    /**
     * Parsea la regexp {@code pattern} y devuelve el AST raíz.
     *
     * @param pattern expresión regular en formato YALex (no null, no vacío)
     * @return nodo raíz del AST
     * @throws RegexParseException si la expresión tiene errores de sintaxis
     */
    public static RegexNode parse(String pattern) {
        Objects.requireNonNull(pattern, "pattern no puede ser null");
        if (pattern.isBlank()) {
            throw new RegexParseException("pattern no puede estar vacio");
        }
        RegexParser p = new RegexParser();
        p.lexer = new RegexLexer(pattern);
        p.current = p.lexer.nextToken();
        p.previous = null;

        RegexNode root = p.parseAlternation();
        p.expect(TokenType.EOF, "se esperaba fin de expresion regular");
        return root;
    }

    // =========================================================================
    // Reglas gramaticales
    // =========================================================================

    /** altern → diff ("|" diff)* */
    private RegexNode parseAlternation() {
        RegexNode left = parseDiff();
        while (match(TokenType.PIPE)) {
            RegexNode right = parseDiff();
            left = new AlternNode(left, right);
        }
        return left;
    }

    /** diff → concat ("#" concat)* */
    private RegexNode parseDiff() {
        RegexNode left = parseConcatenation();
        while (match(TokenType.HASH)) {
            RegexNode right = parseConcatenation();
            left = new DiffNode(left, right);
        }
        return left;
    }

    /** concat → unary unary* (concatenación implícita) */
    private RegexNode parseConcatenation() {
        RegexNode first = parseUnary();
        if (!canStartAtom(current.type())) {
            return first;
        }
        List<RegexNode> parts = new ArrayList<>();
        parts.add(first);
        while (canStartAtom(current.type())) {
            parts.add(parseUnary());
        }
        // Construir árbol de ConcatNode asociado a la izquierda
        RegexNode result = parts.get(0);
        for (int i = 1; i < parts.size(); i++) {
            result = new ConcatNode(result, parts.get(i));
        }
        return result;
    }

    /** unary → atom ("*" | "+" | "?")* */
    private RegexNode parseUnary() {
        RegexNode node = parseAtom();
        while (true) {
            if (match(TokenType.STAR)) {
                node = new KleeneNode(node);
            } else if (match(TokenType.PLUS)) {
                node = new PlusNode(node);
            } else if (match(TokenType.QUESTION)) {
                node = new OptionalNode(node);
            } else {
                break;
            }
        }
        return node;
    }

    /**
     * atom → CHAR | STRING | WILDCARD | IDENTIFIER | "(" expr ")" | CHAR_CLASS
     */
    private RegexNode parseAtom() {
        if (match(TokenType.LPAREN)) {
            RegexNode inner = parseAlternation();
            expect(TokenType.RPAREN, "se esperaba ')' para cerrar grupo");
            return inner;
        }

        if (match(TokenType.CHAR)) {
            return new CharNode(previous.lexeme());
        }

        if (match(TokenType.STRING)) {
            return expandString(previous.lexeme());
        }

        if (match(TokenType.WILDCARD)) {
            return new WildcardNode();
        }

        if (match(TokenType.IDENTIFIER)) {
            String name = previous.lexeme();
            if ("eof".equals(name)) {
                // El token EOF del lexer fuente se trata como un marcador especial
                return new CharNode("EOF");
            }
            // ε (U+03B5) o "epsilon": cadena vacía en la especificación (no es un let)
            if ("ε".equals(name) || "epsilon".equalsIgnoreCase(name)) {
                return EpsilonRegexNode.INSTANCE;
            }
            // Las referencias a 'let' deben expandirse por sustitución de texto
            // ANTES de invocar RegexParser.parse(). Ver SyntaxTreeBuilder.
            throw error("referencia no resuelta a let '" + name + "': "
                    + "expande las definiciones let antes de parsear");
        }

        if (match(TokenType.CHAR_CLASS)) {
            return parseCharClass(previous.lexeme());
        }

        throw error("token inesperado: '" + current.lexeme() + "' (tipo " + current.type() + ")"
                + " en posición " + current.position());
    }

    // =========================================================================
    // Parseo de clases de caracteres [...]
    // =========================================================================

    /**
     * Parsea el contenido del bloque {@code [...]} ya tokenizado como CHAR_CLASS.
     * El {@code raw} incluye los corchetes; p.ej. {@code "[a-z0-9]"}.
     */
    private CharClassNode parseCharClass(String raw) {
        // Quita corchetes
        String body = raw.substring(1, raw.length() - 1);
        boolean negated = false;
        int i = 0;

        if (!body.isEmpty() && body.charAt(0) == '^') {
            negated = true;
            i = 1;
        }

        List<String> entries = new ArrayList<>();
        while (i < body.length()) {
            String first = readClassSymbol(body, i);
            i += classSymbolLength(body, i);

            // Si el siguiente (sin salirse del string) es '-' seguido de otro símbolo →
            // rango
            if (i < body.length() && body.charAt(i) == '-' && i + 1 < body.length()) {
                i++; // consume '-'
                String second = readClassSymbol(body, i);
                i += classSymbolLength(body, i);
                entries.add(first + "-" + second);
            } else {
                entries.add(first);
            }
        }

        if (entries.isEmpty()) {
            throw error("clase de caracteres vacía: " + raw);
        }
        return new CharClassNode(entries, negated);
    }

    /**
     * Lee el símbolo en la posición {@code i} dentro del cuerpo de una clase.
     * Soporta tres formas:
     * <ul>
     * <li>{@code 'c'} — char entre comillas simples (formato YALex normal)
     * <li>{@code '\n'} — char escapado entre comillas simples
     * <li>{@code \n} — escape sin comillas
     * <li>{@code c} — carácter plano
     * </ul>
     */
    private String readClassSymbol(String body, int i) {
        if (i >= body.length()) {
            throw error("símbolo inesperado al final de clase de caracteres");
        }
        char c = body.charAt(i);

        // Forma 'c' o '\n'
        if (c == '\'') {
            int j = i + 1;
            if (j >= body.length())
                throw error("char literal sin cierre en clase de caracteres");
            String value;
            if (body.charAt(j) == '\\') {
                j++;
                if (j >= body.length())
                    throw error("escape incompleto en char literal de clase");
                value = mapEscapeInClass(body.charAt(j));
                j++;
            } else {
                value = String.valueOf(body.charAt(j));
                j++;
            }
            if (j >= body.length() || body.charAt(j) != '\'') {
                throw error("char literal sin comilla de cierre en clase de caracteres");
            }
            return value;
        }

        // Forma \n fuera de comillas
        if (c == '\\') {
            if (i + 1 >= body.length())
                throw error("escape incompleto en clase de caracteres");
            return mapEscapeInClass(body.charAt(i + 1));
        }

        // Char plano
        return String.valueOf(c);
    }

    /**
     * Devuelve cuántas posiciones ocupa el símbolo en la posición {@code i}
     * dentro del cuerpo de una clase de caracteres.
     */
    private int classSymbolLength(String body, int i) {
        if (i >= body.length())
            return 0;
        char c = body.charAt(i);
        if (c == '\'') {
            // 'x' → 3 chars, '\n' → 4 chars
            int j = i + 1;
            if (j < body.length() && body.charAt(j) == '\\')
                j += 2;
            else
                j++;
            j++; // cierre '
            return j - i;
        }
        return c == '\\' ? 2 : 1;
    }

    private String mapEscapeInClass(char escaped) {
        return switch (escaped) {
            case 'n' -> "\n";
            case 't' -> "\t";
            case 'r' -> "\r";
            case '\\' -> "\\";
            case '\'' -> "'";
            case '"' -> "\"";
            case ']' -> "]";
            case '-' -> "-";
            case '^' -> "^";
            default -> String.valueOf(escaped);
        };
    }

    // =========================================================================
    // Expansión de string literal "abc" → concat de CharNodes
    // =========================================================================

    /**
     * Expande la cadena {@code value} (ya sin comillas, con escapes resueltos)
     * como una concatenación de {@link CharNode}s.
     */
    private RegexNode expandString(String value) {
        if (value.isEmpty()) {
            throw error("string literal vacío no está soportado");
        }
        // Itera sobre los code points por si hay caracteres Unicode compuestos
        List<String> chars = new ArrayList<>();
        for (int i = 0; i < value.length();) {
            int cp = value.codePointAt(i);
            chars.add(new String(Character.toChars(cp)));
            i += Character.charCount(cp);
        }
        RegexNode result = new CharNode(chars.get(0));
        for (int i = 1; i < chars.size(); i++) {
            result = new ConcatNode(result, new CharNode(chars.get(i)));
        }
        return result;
    }

    // =========================================================================
    // Helpers del parser
    // =========================================================================

    /** Verifica si el tipo de token actual puede iniciar un átomo. */
    private boolean canStartAtom(TokenType type) {
        return type == TokenType.LPAREN
                || type == TokenType.CHAR
                || type == TokenType.STRING
                || type == TokenType.WILDCARD
                || type == TokenType.IDENTIFIER
                || type == TokenType.CHAR_CLASS;
    }

    /**
     * Consume el token actual si coincide con {@code type}; retorna true si lo
     * hizo.
     */
    private boolean match(TokenType type) {
        if (current.type() != type)
            return false;
        previous = current;
        current = lexer.nextToken();
        return true;
    }

    /** Consume el token o lanza excepción. */
    private void expect(TokenType type, String message) {
        if (!match(type)) {
            throw error(message + "; se encontró '" + current.lexeme()
                    + "' (tipo " + current.type() + ") en posición " + current.position());
        }
    }

    private RegexParseException error(String message) {
        return new RegexParseException("Error de parseo en regexp: " + message);
    }
}
