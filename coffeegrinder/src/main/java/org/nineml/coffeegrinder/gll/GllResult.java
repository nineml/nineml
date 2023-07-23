package org.nineml.coffeegrinder.gll;

import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.trees.*;

import java.util.Collections;
import java.util.Set;

/**
 * The results of a GLL parse.
 */public class GllResult implements GearleyResult {
    private final GllParser parser;
    private final ParseForest graph;
    public final boolean success;
    private final int tokenCount;
    private final Token lastToken;
    private final int offset;
    private final int lineNumber;
    private final int columnNumber;
    private Arborist walker = null;
    private long parseTime = -1;

    public GllResult(GllParser parser, BinarySubtree bsr) {
        this.parser = parser;
        this.graph = bsr.extractSPPF(parser.getGrammar(), parser.getTokens());
        tokenCount = parser.tokenCount;
        lastToken = parser.lastToken;
        offset = parser.getOffset();
        lineNumber = parser.getLineNumber();
        columnNumber = parser.getColumnNumber();
        success = parser.succeeded();
    }

    @Override
    public GearleyResult continueParsing() {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public GearleyResult continueParsing(GearleyParser parser) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public ParseForest getForest() {
        return graph;
    }

    @Override
    public Arborist getArborist() {
        return Arborist.getArborist(graph);
    }

    @Override
    public Arborist getArborist(Axe axe) {
        return Arborist.getArborist(graph, axe);
    }

    @Override
    public boolean isAmbiguous() {
        return graph != null && graph.isAmbiguous();
    }

    @Override
    public boolean isInfinitelyAmbiguous() {
        return graph != null && graph.isInfinitelyAmbiguous();
    }

    @Override
    public int getTokenCount() {
        return tokenCount;
    }

    @Override
    public Token getLastToken() {
        return lastToken;
    }

    public int getOffset() {
        return offset;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public Set<TerminalSymbol> getPredictedTerminals() {
        return Collections.emptySet();
    }

    @Override
    public GearleyParser getParser() {
        return parser;
    }

    @Override
    public boolean prefixSucceeded() {
        return false;
    }

    @Override
    public long getParseTime() {
        return parseTime;
    }

    protected void setParseTime(long time) {
        parseTime = time;
    }

    @Override
    public boolean succeeded() {
        return success;
    }
}
