package org.nineml.coffeegrinder.trees;

import org.nineml.coffeegrinder.parser.NonterminalSymbol;
import org.nineml.coffeegrinder.tokens.Token;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

/**
 * A tree builder that returns the serialized tree as a string.
 */
public class StringTreeBuilder implements TreeBuilder {
    protected ByteArrayOutputStream buffer = null;
    protected PrintStreamTreeBuilder builder = null;
    private boolean ran = false;
    private String tree = null;
    private final boolean debug;

    public StringTreeBuilder() {
        this(false);
    }

    public StringTreeBuilder(boolean debugAttributes) {
        debug = debugAttributes;
    }

    public String getTree() {
        if (tree == null && buffer != null) {
            try {
                tree = ran ? buffer.toString("UTF-8") : null;
            } catch (UnsupportedEncodingException ex) {
                // This can't happen
            }
        }

        return tree;
    }

    @Override
    public void startTree() {
        buffer = new ByteArrayOutputStream();
        builder = new PrintStreamTreeBuilder(new PrintStream(buffer), false);
        builder.startTree();
        tree = null;
        ran = false;
    }

    @Override
    public void endTree(boolean ambiguous, boolean absolutelyAmbiguous, boolean infinitelyAmbiguous) {
        // ignore the trailing newline from the supertype
        ran = true;
        builder.endTree(ambiguous, absolutelyAmbiguous, infinitelyAmbiguous);
    }

    @Override
    public void startNonterminal(NonterminalSymbol symbol, Map<String,String> attributes, int leftExtent, int rightExtent) {
        if (!symbol.getName().startsWith("$")) {
            if (!debug || attributes.isEmpty()) {
                builder.startNonterminal(symbol, attributes, leftExtent, rightExtent);
            } else {
                ArrayList<String> names = new ArrayList<>(attributes.keySet());
                Collections.sort(names);
                builder.stream.printf("<%s", symbol.getName());
                for (String name : names) {
                    if (!"name".equals(name) || !attributes.get(name).equals(symbol.getName())) {
                        builder.stream.printf(" %s='%s'", name, attributes.get(name));
                    }
                }
                builder.stream.print(">");
            }
        }
    }

    @Override
    public void endNonterminal(NonterminalSymbol symbol, Map<String,String> attributes, int leftExtent, int rightExtent) {
        if (!symbol.getName().startsWith("$")) {
            builder.endNonterminal(symbol, attributes, leftExtent, rightExtent);
        }
    }

    @Override
    public void token(Token token, Map<String,String> attributes, int leftExtent, int rightExtent) {
        builder.token(token, attributes, leftExtent, rightExtent);
    }

    @Override
    public void startAmbiguity(int id, int leftExtent, int rightExtent) {
        if (debug) {
            builder.stream.printf("<?start-ambiguity id='%d' start='%d' end='%d'?>", id, leftExtent, rightExtent);
        }
    }

    @Override
    public void endAmbiguity(int id, int leftExtent, int rightExtent) {
        if (debug) {
            builder.stream.printf("<?end-ambiguity id='%d'>", id);
        }
    }
}
