package com.yalex.codegen;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.yalex.model.LetDefinition;

/**
 * Sustituye identificadores {@code let} en el texto de una regexp por su definición entre paréntesis.
 */
public final class LetExpander {

    private LetExpander() {
    }

    public static String apply(String pattern, List<LetDefinition> lets) {
        if (lets == null || lets.isEmpty()) {
            return pattern;
        }
        List<LetDefinition> sorted = new ArrayList<>(lets);
        sorted.sort(Comparator.comparingInt((LetDefinition ld) -> ld.getName().length()).reversed());
        String result = pattern;
        for (LetDefinition ld : sorted) {
            String name = ld.getName();
            String def = ld.getRegex().trim();
            String replacement = "(" + def + ")";
            result = replaceIdentifier(result, name, replacement);
        }
        return result;
    }

    /**
     * Reemplaza {@code name} como identificador completo (no parte de otro nombre).
     */
    static String replaceIdentifier(String text, String name, String replacement) {
        if (name.isEmpty()) {
            return text;
        }
        String quoted = Pattern.quote(name);
        Pattern p = Pattern.compile("(?<![A-Za-z0-9_])" + quoted + "(?![A-Za-z0-9_])");
        Matcher m = p.matcher(text);
        return m.replaceAll(Matcher.quoteReplacement(replacement));
    }
}
