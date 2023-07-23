package org.nineml.coffeegrinder.gll;

import org.nineml.coffeegrinder.parser.State;

/**
 * A CRF node.
 */
public class CrfNode {
    public final State slot;
    public final int i;

    protected CrfNode(State slot, int i) {
        this.slot = slot;
        this.i = i;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CrfNode) {
            CrfNode other = (CrfNode) obj;
            return i == other.i && slot.equals(other.slot);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return slot.hashCode() + (31 * i);
    }

    @Override
    public String toString() {
        return slot.toString() + ", " + i;
    }
}
