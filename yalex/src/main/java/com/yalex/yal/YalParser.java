package com.yalex.yal;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.yalex.model.LetDefinition;
import com.yalex.model.Rule;
import com.yalex.model.RuleSet;

public class YalParser {

    public YalFile parse(Path path) {
        Objects.requireNonNull(path, "path no puede ser null");
        try {
            return parse(Files.readString(path, StandardCharsets.UTF_8));
        } catch (IOException ex) {
            throw new IllegalArgumentException("no se pudo leer archivo .yal: " + path, ex);
        }
    }

    public YalFile parse(String rawSource) {
        Objects.requireNonNull(rawSource, "rawSource no puede ser null");

        String source = new YalLexer(rawSource).stripComments();
        int index = 0;
        StringBuilder header = new StringBuilder();
        StringBuilder trailer = new StringBuilder();
        List<LetDefinition> lets = new ArrayList<>();
        List<RuleSet> ruleSets = new ArrayList<>();

        // Lee bloques iniciales como header.
        while (true) {
            index = skipWhitespace(source, index);
            if (index >= source.length() || source.charAt(index) != '{') {
                break;
            }
            int blockEnd = findMatchingBrace(source, index);
            if (!header.isEmpty()) {
                header.append(System.lineSeparator());
            }
            header.append(source, index + 1, blockEnd).append(System.lineSeparator());
            index = blockEnd + 1;
        }

        while (index < source.length()) {
            index = skipWhitespace(source, index);
            if (index >= source.length()) {
                break;
            }

            if (startsWithWord(source, index, "let")) {
                int lineEnd = findLineEnd(source, index);
                String line = source.substring(index, lineEnd).trim();
                lets.add(parseLet(line));
                index = lineEnd;
                continue;
            }

            if (startsWithWord(source, index, "rule")) {
                ParsedRuleSet parsed = parseRuleSet(source, index);
                ruleSets.add(parsed.ruleSet());
                index = parsed.nextIndex();
                continue;
            }

            if (source.charAt(index) == '{') {
                // Lo que queda en bloques es trailer.
                while (index < source.length()) {
                    index = skipWhitespace(source, index);
                    if (index >= source.length()) {
                        break;
                    }
                    if (source.charAt(index) != '{') {
                        throw new IllegalArgumentException("se esperaba bloque { ... } en trailer");
                    }
                    int blockEnd = findMatchingBrace(source, index);
                    if (!trailer.isEmpty()) {
                        trailer.append(System.lineSeparator());
                    }
                    trailer.append(source, index + 1, blockEnd).append(System.lineSeparator());
                    index = blockEnd + 1;
                }
                break;
            }

            throw new IllegalArgumentException("token inesperado en .yal cerca de: " + preview(source, index));
        }

        return new YalFile(header.toString().trim(), lets, ruleSets, trailer.toString().trim());
    }

    private LetDefinition parseLet(String line) {
        int eqIndex = line.indexOf('=');
        if (eqIndex < 0) {
            throw new IllegalArgumentException("let invalido, falta '=': " + line);
        }
        String left = line.substring(0, eqIndex).trim();
        String right = line.substring(eqIndex + 1).trim();
        if (!left.startsWith("let ")) {
            throw new IllegalArgumentException("let invalido: " + line);
        }
        String name = left.substring(4).trim();
        return new LetDefinition(name, right);
    }

    private ParsedRuleSet parseRuleSet(String source, int start) {
        int equals = source.indexOf('=', start);
        if (equals < 0) {
            throw new IllegalArgumentException("rule invalida, falta '='");
        }

        String header = source.substring(start, equals).trim();
        String[] parts = header.split("\\s+");
        if (parts.length < 2 || !"rule".equals(parts[0])) {
            throw new IllegalArgumentException("encabezado de rule invalido: " + header);
        }
        String ruleName = parts[1].trim();
        if (ruleName.isEmpty()) {
            throw new IllegalArgumentException("nombre de rule vacio");
        }

        int index = equals + 1;
        List<Rule> rules = new ArrayList<>();

        while (index < source.length()) {
            index = skipWhitespace(source, index);
            if (index >= source.length()) {
                break;
            }
            if (startsWithWord(source, index, "rule") || source.charAt(index) == '{') {
                break;
            }
            if (source.charAt(index) == '|') {
                index++;
                index = skipWhitespace(source, index);
            }

            ParsedRule parsedRule = parseRuleAlternative(source, index);
            rules.add(parsedRule.rule());
            index = parsedRule.nextIndex();
        }

        if (rules.isEmpty()) {
            throw new IllegalArgumentException("rule sin alternativas: " + ruleName);
        }

        return new ParsedRuleSet(new RuleSet(ruleName, rules), index);
    }

    private ParsedRule parseRuleAlternative(String source, int start) {
        int actionOpen = source.indexOf('{', start);
        if (actionOpen < 0) {
            throw new IllegalArgumentException("alternativa sin accion { ... }");
        }
        String pattern = source.substring(start, actionOpen).trim();
        if (pattern.isEmpty()) {
            throw new IllegalArgumentException("alternativa sin patron");
        }
        int actionClose = findMatchingBrace(source, actionOpen);
        String action = source.substring(actionOpen + 1, actionClose).trim();
        if (action.isEmpty()) {
            throw new IllegalArgumentException("accion vacia en alternativa");
        }
        return new ParsedRule(new Rule(pattern, action), actionClose + 1);
    }

    private int skipWhitespace(String value, int index) {
        int i = index;
        while (i < value.length() && Character.isWhitespace(value.charAt(i))) {
            i++;
        }
        return i;
    }

    private boolean startsWithWord(String source, int index, String word) {
        if (!source.startsWith(word, index)) {
            return false;
        }
        int before = index - 1;
        int after = index + word.length();
        boolean validBefore = before < 0 || !Character.isLetterOrDigit(source.charAt(before));
        boolean validAfter = after >= source.length() || !Character.isLetterOrDigit(source.charAt(after));
        return validBefore && validAfter;
    }

    private int findLineEnd(String source, int index) {
        int i = index;
        while (i < source.length() && source.charAt(i) != '\n' && source.charAt(i) != '\r') {
            i++;
        }
        return i;
    }

    private int findMatchingBrace(String source, int open) {
        int depth = 0;
        for (int i = open; i < source.length(); i++) {
            char c = source.charAt(i);
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        throw new IllegalArgumentException("bloque { ... } sin cierre");
    }

    private String preview(String source, int index) {
        int end = Math.min(source.length(), index + 20);
        return source.substring(index, end).replace('\n', ' ').replace('\r', ' ');
    }

    private record ParsedRuleSet(RuleSet ruleSet, int nextIndex) {
    }

    private record ParsedRule(Rule rule, int nextIndex) {
    }
}
