package com.yapar.yalp;

import java.util.List;
import java.util.Objects;

/**
 * Resultado del parseo de un archivo .yalp.
 * Contiene los tokens declarados con {@code %token} y las producciones crudas.
 */
public final class YalpFile {

    private final List<String> declaredTokens;
    private final List<RawProduction> productions;

    public YalpFile(List<String> declaredTokens, List<RawProduction> productions) {
        Objects.requireNonNull(declaredTokens, "declaredTokens no puede ser null");
        Objects.requireNonNull(productions, "productions no puede ser null");
        this.declaredTokens = List.copyOf(declaredTokens);
        this.productions = List.copyOf(productions);
    }

    /** Terminales declarados explícitamente con {@code %token}. */
    public List<String> getDeclaredTokens() {
        return declaredTokens;
    }

    /** Producciones en el orden en que aparecen; el lhs del primero es el símbolo inicial. */
    public List<RawProduction> getProductions() {
        return productions;
    }
}
