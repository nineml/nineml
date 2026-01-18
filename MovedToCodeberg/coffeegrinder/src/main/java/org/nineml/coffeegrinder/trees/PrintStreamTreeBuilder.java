package org.nineml.coffeegrinder.trees;

import org.nineml.coffeegrinder.parser.NonterminalSymbol;
import org.nineml.coffeegrinder.tokens.Token;

import java.io.PrintStream;
import java.util.Map;

/**
 * A tree builder that sends serialized results to a print stream.
 */
public class PrintStreamTreeBuilder implements TreeBuilder {
    protected final PrintStream stream;
    protected final boolean addTrailingNewline;
    private boolean trailingnl = false;

    public PrintStreamTreeBuilder(PrintStream stream) {
        this(stream, true);
    }

    public PrintStreamTreeBuilder(PrintStream stream, boolean addTrailingNewline) {
        this.stream = stream;
        this.addTrailingNewline = addTrailingNewline;
    }

    @Override
    public void startTree() {
        // nop
    }

    @Override
    public void endTree(boolean ambiguous, boolean absolutelyAmbiguous, boolean infinitelyAmbiguous) {
        if (!trailingnl && addTrailingNewline) {
            stream.println();
        }
    }

    @Override
    public void startNonterminal(NonterminalSymbol symbol, Map<String,String> attributes, int leftExtent, int rightExtent) {
        stream.printf("<%s>", symbol.getName());
    }

    @Override
    public void endNonterminal(NonterminalSymbol symbol, Map<String,String> attributes, int leftExtent, int rightExtent) {
        stream.printf("</%s>", symbol.getName());
    }

    @Override
    public void token(Token token, Map<String,String> attributes, int leftExtent, int rightExtent) {
        String text = token.getValue();
        trailingnl = text.endsWith("\n");
        stream.printf(text);
    }

    @Override
    public void startAmbiguity(int id, int leftExtent, int rightExtent) {
        // nop
    }

    @Override
    public void endAmbiguity(int id, int leftExtent, int rightExtent) {
        // nop
    }
}
