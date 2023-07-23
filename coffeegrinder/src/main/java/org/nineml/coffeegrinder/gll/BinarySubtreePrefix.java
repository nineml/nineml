package org.nineml.coffeegrinder.gll;

import org.nineml.coffeegrinder.parser.ForestNodeGLL;
import org.nineml.coffeegrinder.parser.State;
import org.nineml.coffeegrinder.parser.Symbol;

import java.util.Arrays;

/**
 * A "prefix subtree" in a {@link BinarySubtree}.
 */
public class BinarySubtreePrefix extends BinarySubtreeNode {
    protected BinarySubtreePrefix(State slot, int left, int pivot, int right) {
        super(slot, left, pivot, right);
    }

    protected boolean matches(ForestNodeGLL fslot) {
        assert fslot.state != null;
        if (slot.position != fslot.state.position) {
            return false;
        }

        for (int pos = 0; pos < slot.position; pos++) {
            if (!slot.rhs.get(pos).equals(fslot.state.rhs.get(pos))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BinarySubtreePrefix) {
            BinarySubtreePrefix other = (BinarySubtreePrefix) obj;
            if (leftExtent != other.leftExtent || rightExtent != other.rightExtent
                    || pivot != other.pivot || slot.position != other.slot.position) {
                return false;
            }
            return slot.rhs.equals(other.slot.rhs);
        }
        return false;
    }

    @Override
    public int hashCode() {
        Symbol[] prefix = new Symbol[slot.position];
        System.arraycopy(slot.rhs.symbols, 0, prefix, 0, slot.position);
        int code = Arrays.hashCode(prefix);
        return code + (11 * leftExtent) + (13 * pivot) + (31 * rightExtent);
    }
}
