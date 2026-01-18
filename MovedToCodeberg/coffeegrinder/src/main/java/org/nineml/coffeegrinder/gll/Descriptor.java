package org.nineml.coffeegrinder.gll;

import org.nineml.coffeegrinder.parser.State;

/**
 * A descriptor is a slot (a state) with it's position in the input.
 */
public class Descriptor {
    public final State slot;
    public final int k;
    public final int j;

    public Descriptor(State slot, int k, int j) {
        this.slot = slot;
        this.k = k;
        this.j = j;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Descriptor) {
            Descriptor other = (Descriptor) obj;
            return k == other.k && j == other.j && slot.equals(other.slot);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return slot.hashCode() + (31 * k) + (19 * j);
    }

    @Override
    public String toString() {
        return "(" + slot + " " + k + ", " + j + ")";
    }
}
