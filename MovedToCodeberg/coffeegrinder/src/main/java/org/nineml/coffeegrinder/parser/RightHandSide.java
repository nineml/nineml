package org.nineml.coffeegrinder.parser;

import java.util.Arrays;
import java.util.List;

/**
 * A utility class for what appears on the "right hand side" of a production in a grammar.
 */
public class RightHandSide {
    public final Symbol[] symbols;
    private final int hcode;
    public final int length;

    public RightHandSide(Symbol[] symbols) {
        this.symbols = symbols;
        this.length = this.symbols.length;
        hcode = Arrays.hashCode(this.symbols);
    }

    public RightHandSide(List<Symbol> symbols) {
        this.symbols = new Symbol[symbols.size()];
        symbols.toArray(this.symbols);
        this.length = this.symbols.length;
        hcode = Arrays.hashCode(this.symbols);
    }

    public Symbol get(int pos) {
        if (pos < 0 || pos >= symbols.length) {
            throw new IndexOutOfBoundsException("No " + pos + " item in symbol list");
        }
        return symbols[pos];
    }

    public Symbol getFirst() {
        if (symbols.length == 0) {
            return null;
        }
        return symbols[0];
    }

    public boolean isEmpty() {
        return symbols.length == 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RightHandSide) {
            RightHandSide other = (RightHandSide) obj;
            if (symbols.length != other.symbols.length) {
                return false;
            }
            for (int pos = 0; pos < symbols.length; pos++) {
                if (!symbols[pos].equals(other.symbols[pos])) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return hcode;
    }
}
