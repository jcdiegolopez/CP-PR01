package com.yapar.yalp;

import java.util.List;

/**
 * Una alternativa cruda dentro de una producción: secuencia de símbolos + acción semántica.
 * Ejemplo: {@code E PLUS T { "suma" }} → symbols=["E","PLUS","T"], action="\"suma\"".
 * rhs vacío representa una producción épsilon.
 */
public record RawAlternative(List<String> symbols, String action) {

    public RawAlternative {
        symbols = List.copyOf(symbols);
        action = (action == null) ? "" : action;
    }

    public boolean isEpsilon() {
        return symbols.isEmpty();
    }
}
