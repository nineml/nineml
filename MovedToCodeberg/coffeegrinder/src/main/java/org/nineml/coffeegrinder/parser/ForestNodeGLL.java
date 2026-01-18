package org.nineml.coffeegrinder.parser;

import java.util.Objects;

/**
 * A node in the SPPF constructed by the GLL parser.
 *
 * <p>When walking the graph, for example to extract parses, these nodes represent what's
 * available in the graph.</p>
 */
public class ForestNodeGLL extends ForestNode {
    public final boolean intermediate;
    public final int pivot;
    protected ForestNodeGLL parent = null;

    protected ForestNodeGLL(ParseForest graph, Symbol symbol, int j, int i) {
        super(graph, symbol, j, i);
        intermediate = false;
        pivot = -1;
    }

    protected ForestNodeGLL(ParseForest graph, State state, int j, int i) {
        super(graph, state, j, i);
        intermediate = false;
        pivot = -1;
    }

    protected ForestNodeGLL(ParseForest graph, Symbol symbol, State state, int j, int i) {
        super(graph, symbol, state, j, i);
        intermediate = false;
        pivot = -1;
    }

    protected ForestNodeGLL(ParseForest graph, State state, int pivot) {
        super(graph, state, -1, -1);
        intermediate = true;
        this.pivot = pivot;
    }

    public void addEdge(ForestNodeGLL node) {
        node.parent = this;

        if (intermediate) {
            if (families.isEmpty()) {
                families.add(new Family(graph, node, node.state));
            } else {
                assert families.size() == 1;
                assert families.get(0).w == null;
                families.get(0).w = node;
            }
            return;
        }

        final Family fam;
        if (node.intermediate) {
            assert node.families.size() == 1;
            fam = node.families.get(0);
        } else {
            fam = new Family(graph, node, node.state);
        }

        // Don't add a duplicate...
        for (Family cfam : families) {
            if (cfam.v == fam.v && cfam.w == fam.w) {
                return;
            }
        }

        families.add(fam);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ForestNodeGLL) {
            ForestNodeGLL other = (ForestNodeGLL) obj;
            if (state == null) {
                assert symbol != null;
                return symbol.equals(other.symbol) && rightExtent == other.rightExtent && leftExtent == other.leftExtent;
            }
            return Objects.equals(symbol, other.symbol) && state.equals(other.state) && rightExtent == other.rightExtent && leftExtent == other.leftExtent;
        }
        return false;
    }
}
