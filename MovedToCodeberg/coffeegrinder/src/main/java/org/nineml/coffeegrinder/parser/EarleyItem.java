package org.nineml.coffeegrinder.parser;

/**
 * An Earley item.
 *
 * <p>Earley items are used internally by the parser to keep track of the current state.
 * The current state is an internal {@link State} that tracks progress through a production
 * and an indication of where (in the input) the item began. There's also a {@link ForestNode}
 * to track the parse forest.</p>
 */
public class EarleyItem {
    public final State state;
    public final int j;
    public final ForestNode w;

    protected EarleyItem(State state, int j, ForestNode w) {
        this.state = state;
        this.j = j;
        this.w = w;
    }

    protected EarleyItem(State state, int j) {
        this.state = state;
        this.j = j;
        this.w = null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EarleyItem) {
            EarleyItem other = (EarleyItem) obj;
            if (w == null) {
                if (other.w != null) {
                    return false;
                }
                return state.equals(other.state) && j == other.j;
            } else {
                return state.equals(other.state) && j == other.j && w.equals(other.w);
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (w == null) {
            return (3 * state.hashCode()) + (7 * j);
        }

        return (3 * state.hashCode()) + (7 * j) + (13 * w.hashCode());
    }

    @Override
    public String toString() {
        if (w == null) {
            return state + " / " + j;
        }
        return state + " / " + j + " / " + w;
    }
}
