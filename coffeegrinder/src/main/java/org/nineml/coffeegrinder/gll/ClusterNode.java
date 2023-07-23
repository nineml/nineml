package org.nineml.coffeegrinder.gll;

import org.nineml.coffeegrinder.parser.NonterminalSymbol;

/**
 * A cluster node.
 */
public class ClusterNode {
    public final NonterminalSymbol symbol;
    public final int k;

    protected ClusterNode(NonterminalSymbol symbol, int k) {
        this.symbol = symbol;
        this.k = k;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ClusterNode) {
            ClusterNode other = (ClusterNode) obj;
            return k == other.k && symbol.equals(other.symbol);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return symbol.hashCode() + (31 * k);
    }

    @Override
    public String toString() {
        return symbol.toString() + ", " + k;
    }
}
