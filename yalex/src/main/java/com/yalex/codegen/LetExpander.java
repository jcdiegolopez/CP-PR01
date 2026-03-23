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

        // 1. Resolver dependencias cruzadas entre los propios 'let'
        List<LetDefinition> sortedByDescLength = new ArrayList<>(lets);
        sortedByDescLength.sort(Comparator.comparingInt((LetDefinition ld) -> ld.getName().length()).reversed());

        List<LetDefinition> resolvedLets = new ArrayList<>();
        for (LetDefinition ld : lets) {
            String def = ld.getRegex().trim();
            boolean changed = true;
            int pass = 0;
            // Evaluamos hasta que ya no haya variables no-primitivas dentro del 'let'
            while (changed && pass < 100) {
                changed = false;
                for (LetDefinition other : sortedByDescLength) {
                    if (other.getName().equals(ld.getName())) {
                        continue; // Evitar el auto-reemplazo recursivo
                    }
                    String current = replaceIdentifier(def, other.getName(), "(" + other.getRegex().trim() + ")");
                    if (!current.equals(def)) {
                        def = current;
                        changed = true;
                    }
                }
                pass++;
            }
            if (pass >= 100) {
                throw new IllegalArgumentException("Lazo infinito detectado en variables let (dependencias circulares): " + ld.getName());
            }
            resolvedLets.add(new LetDefinition(ld.getName(), def));
        }

        // 2. Ahora que están resueltas al 100%, aplicamos al patrón principal
        resolvedLets.sort(Comparator.comparingInt((LetDefinition ld) -> ld.getName().length()).reversed());
        String result = pattern;
        for (LetDefinition ld : resolvedLets) {
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
