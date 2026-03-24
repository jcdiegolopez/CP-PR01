package com.yalex.codegen;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
     * Equivalente a {@code (?<![A-Za-z0-9_])name(?![A-Za-z0-9_])} sin usar {@code java.util.regex}.
     */
    static String replaceIdentifier(String text, String name, String replacement) {
        if (name.isEmpty()) {
            return text;
        }
        int n = name.length();
        if (text.length() < n) {
            return text;
        }
        StringBuilder sb = new StringBuilder(text.length() + replacement.length() * 4);
        int i = 0;
        while (i <= text.length() - n) {
            int idx = text.indexOf(name, i);
            if (idx < 0) {
                sb.append(text, i, text.length());
                return sb.toString();
            }
            if (isBoundariedOccurrence(text, idx, n)) {
                sb.append(text, i, idx);
                sb.append(replacement);
                i = idx + n;
            } else {
                sb.append(text, i, idx + 1);
                i = idx + 1;
            }
        }
        sb.append(text, i, text.length());
        return sb.toString();
    }

    /** Mismo criterio que [A-Za-z0-9_] en los lookaround del patrón anterior. */
    private static boolean isAsciiIdentChar(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_';
    }

    private static boolean isBoundariedOccurrence(String text, int start, int len) {
        if (start > 0 && isAsciiIdentChar(text.charAt(start - 1))) {
            return false;
        }
        int after = start + len;
        return after >= text.length() || !isAsciiIdentChar(text.charAt(after));
    }
}
