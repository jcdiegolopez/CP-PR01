package com.yapar.bridge;

import com.yalex.model.LetDefinition;
import com.yalex.model.Rule;
import com.yalex.model.RuleSet;
import com.yalex.yal.YalFile;
import com.yalex.yal.YalParser;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Conecta YAPar con YALex: parsea un .yal usando la API existente de YALex
 * y extrae información útil para validar los tokens declarados en el .yalp.
 *
 * <p>La validación es <em>advistoria</em>: los conflictos se reportan como advertencias,
 * nunca como errores fatales. La fuente de verdad para los terminales es siempre
 * el {@code %token} del .yalp.
 */
public final class YalexBridge {

    private YalexBridge() {}

    /**
     * Resultado del análisis del .yal.
     *
     * @param letNames       nombres de las definiciones {@code let} en el .yal
     * @param rulePatterns   patrones simples (identificadores únicos) usados en las reglas
     * @param warnings       advertencias de posibles inconsistencias con {@code declaredTokens}
     */
    public record BridgeResult(
            Set<String> letNames,
            Set<String> rulePatterns,
            List<String> warnings) {

        public BridgeResult {
            letNames      = Collections.unmodifiableSet(new LinkedHashSet<>(letNames));
            rulePatterns  = Collections.unmodifiableSet(new LinkedHashSet<>(rulePatterns));
            warnings      = List.copyOf(warnings);
        }
    }

    /**
     * Analiza el archivo {@code yalPath} y compara sus definiciones con {@code declaredTokens}.
     */
    public static BridgeResult analyze(Path yalPath, List<String> declaredTokens) {
        Objects.requireNonNull(yalPath, "yalPath no puede ser null");
        Objects.requireNonNull(declaredTokens, "declaredTokens no puede ser null");

        YalFile yal = new YalParser().parse(yalPath);
        return analyze(yal, declaredTokens);
    }

    static BridgeResult analyze(YalFile yal, List<String> declaredTokens) {
        // 1. Nombres de let definitions
        Set<String> letNames = new LinkedHashSet<>();
        for (LetDefinition ld : yal.getLetDefinitions()) {
            letNames.add(ld.getName());
        }

        // 2. Patrones simples usados en reglas (solo identificadores únicos = referencias a lets)
        Set<String> rulePatterns = new LinkedHashSet<>();
        for (RuleSet rs : yal.getRuleSets()) {
            for (Rule r : rs.getRules()) {
                String pattern = r.getPattern().trim();
                if (pattern.matches("[A-Za-z_][A-Za-z0-9_]*")) {
                    rulePatterns.add(pattern);
                }
            }
        }

        // 3. Advertencias: tokens en %token que no tienen correspondencia obvia
        Set<String> known = new LinkedHashSet<>(letNames);
        known.addAll(rulePatterns);

        List<String> warnings = new ArrayList<>();
        for (String token : declaredTokens) {
            boolean found = known.stream().anyMatch(n -> n.equalsIgnoreCase(token));
            if (!found) {
                warnings.add(
                    "[YAPAR] ADVERTENCIA: el token '" + token +
                    "' declarado en %token no tiene correspondencia obvia en el .yal " +
                    "(esto puede ser normal si el token es una palabra clave literal)");
            }
        }

        return new BridgeResult(letNames, rulePatterns, warnings);
    }
}
