package com.yalex.yal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.yalex.model.LetDefinition;
import com.yalex.model.RuleSet;

public class YalFile {

    private final String header;
    private final List<LetDefinition> letDefinitions;
    private final List<RuleSet> ruleSets;
    private final String trailer;

    public YalFile(String header, List<LetDefinition> letDefinitions, List<RuleSet> ruleSets, String trailer) {
        this.header = Objects.requireNonNull(header, "header no puede ser null");
        Objects.requireNonNull(letDefinitions, "letDefinitions no puede ser null");
        Objects.requireNonNull(ruleSets, "ruleSets no puede ser null");
        this.trailer = Objects.requireNonNull(trailer, "trailer no puede ser null");
        this.letDefinitions = List.copyOf(letDefinitions);
        this.ruleSets = List.copyOf(ruleSets);
    }

    public String getHeader() {
        return header;
    }

    public List<LetDefinition> getLetDefinitions() {
        return new ArrayList<>(letDefinitions);
    }

    public List<RuleSet> getRuleSets() {
        return new ArrayList<>(ruleSets);
    }

    public String getTrailer() {
        return trailer;
    }
}
