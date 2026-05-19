package com.yapar.yalp;

import java.util.List;

/**
 * Una producción cruda: el no-terminal de la izquierda (lhs) y sus alternativas.
 * Ejemplo: {@code E : E PLUS T | T ;} → lhs="E", alternatives=[alt1, alt2].
 */
public record RawProduction(String lhs, List<RawAlternative> alternatives) {

    public RawProduction {
        alternatives = List.copyOf(alternatives);
    }
}
