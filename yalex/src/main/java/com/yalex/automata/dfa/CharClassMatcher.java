package com.yalex.automata.dfa;

import java.util.Set;
import java.util.TreeSet;

import com.yalex.regex.node.CharClassNode;

/**
 * Coincidencia de un carácter contra una clase (incluye negación en ASCII imprimible 32–126).
 */
public final class CharClassMatcher {

    private static final int ASCII_PRINT_MIN = 32;
    private static final int ASCII_PRINT_MAX = 126;

    private CharClassMatcher() {
    }

    public static boolean matches(CharClassNode node, char c) {
        boolean in = positiveContains(node, c);
        return node.isNegated() ? !in : in;
    }

    /** Conjunto finito de caracteres que pueden hacer match (para alfabeto del DFA). */
    public static Set<Character> matchingAlphabet(CharClassNode node) {
        if (!node.isNegated()) {
            return expandPositive(node);
        }
        TreeSet<Character> out = new TreeSet<>();
        for (int cp = ASCII_PRINT_MIN; cp <= ASCII_PRINT_MAX; cp++) {
            char ch = (char) cp;
            if (!positiveContains(node, ch)) {
                out.add(ch);
            }
        }
        return out;
    }

    private static boolean positiveContains(CharClassNode node, char c) {
        for (String e : node.getEntries()) {
            if (e.length() == 3 && e.charAt(1) == '-') {
                char lo = e.charAt(0);
                char hi = e.charAt(2);
                if (rangeContains(lo, hi, c)) {
                    return true;
                }
            } else if (!e.isEmpty() && e.charAt(0) == c && e.length() == 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Rango inclusivo {@code lo-hi}. Si {@code lo > hi}, el bucle clásico no añade nada;
     * en ese caso: (1) error típico {@code 'a'-'Z'} en vez de {@code 'a'-'z''A'-'Z'} → letras
     * minúsculas y mayúsculas; (2) en otro caso → rango continuo entre los dos códigos (min–max).
     */
    static boolean rangeContains(char lo, char hi, char c) {
        if (lo <= hi) {
            return c >= lo && c <= hi;
        }
        if (lo >= 'a' && lo <= 'z' && hi >= 'A' && hi <= 'Z') {
            return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
        }
        int a = Math.min(lo, hi);
        int b = Math.max(lo, hi);
        return c >= a && c <= b;
    }

    private static TreeSet<Character> expandPositive(CharClassNode node) {
        TreeSet<Character> out = new TreeSet<>();
        for (String e : node.getEntries()) {
            if (e.length() == 3 && e.charAt(1) == '-') {
                char lo = e.charAt(0);
                char hi = e.charAt(2);
                addRange(out, lo, hi);
            } else if (!e.isEmpty()) {
                out.add(e.charAt(0));
            }
        }
        return out;
    }

    private static void addRange(TreeSet<Character> out, char lo, char hi) {
        if (lo <= hi) {
            for (char ch = lo; ch <= hi; ch++) {
                out.add(ch);
            }
            return;
        }
        if (lo >= 'a' && lo <= 'z' && hi >= 'A' && hi <= 'Z') {
            for (char ch = 'a'; ch <= 'z'; ch++) {
                out.add(ch);
            }
            for (char ch = 'A'; ch <= 'Z'; ch++) {
                out.add(ch);
            }
            return;
        }
        int a = Math.min(lo, hi);
        int b = Math.max(lo, hi);
        for (int cp = a; cp <= b; cp++) {
            out.add((char) cp);
        }
    }
}
