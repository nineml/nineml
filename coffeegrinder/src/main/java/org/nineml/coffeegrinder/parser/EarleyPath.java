package org.nineml.coffeegrinder.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EarleyPath {
    private final ArrayList<PathSegment> segments = new ArrayList<>();
    private final ArrayList<PathSegment> rules = new ArrayList<>();

    public List<PathSegment> getSegments() {
        return segments;
    }

    public List<PathSegment> getRules() {
        return rules;
    }

    public void addSegment(int start, int end, String input, List<NonterminalSymbol> symbols) {
        PathSegment segment = new PathSegment(start, end, input, symbols);
        segments.add(0, segment);
    }

    public void addRule(int start, int end, String input, NonterminalSymbol symbol) {
        PathSegment segment = new PathSegment(start, end, input, Collections.singletonList(symbol));
        rules.add(0, segment);
    }

    public static class PathSegment {
        public final int start;
        public final int end;
        public final String input;
        public final List<NonterminalSymbol> symbols;

        public PathSegment(int start, int end, String input, List<NonterminalSymbol> symbols) {
            this.start = start;
            this.end = end;
            this.input = input;
            this.symbols = symbols;
        }
    }
}
