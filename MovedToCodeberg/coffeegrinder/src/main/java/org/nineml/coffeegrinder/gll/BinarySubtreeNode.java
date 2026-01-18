package org.nineml.coffeegrinder.gll;

import org.nineml.coffeegrinder.parser.State;
import org.nineml.coffeegrinder.util.Instrumentation;

/**
 * A node in a {@link BinarySubtree}.
 */
public abstract class BinarySubtreeNode {
    public final State slot;
    public final int leftExtent;
    public final int rightExtent;
    public final int pivot;

    protected BinarySubtreeNode(State slot, int left, int pivot, int right) {
        //Instrumentation.count(String.format("bsrNode %s (%d, %d, %d)", slot, left, pivot, right));
        //Instrumentation.count("bsrNode");
        this.slot = slot;
        this.leftExtent = left;
        this.rightExtent = right;
        this.pivot = pivot;
    }

    @Override
    public String toString() {
        return String.format("%s, %d, %d, %d", slot, leftExtent, pivot, rightExtent);
    }
}
