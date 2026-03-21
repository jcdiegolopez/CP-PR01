package com.yalex.regex;

/**
 * Excepción lanzada cuando el parser encuentra un error de sintaxis en una regexp.
 */
public class RegexParseException extends RuntimeException {

    public RegexParseException(String message) {
        super(message);
    }

    public RegexParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
