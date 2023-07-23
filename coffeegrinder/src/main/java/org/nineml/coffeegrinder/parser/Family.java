package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.exceptions.ParseException;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.util.ParserAttribute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An internal class representing a family of nodes in the SPPF.
 * <p>This has no public use, but it's shared between two classes so it can't be private to either of them.</p>
 */
public class Family {
    public final int id;
    // if v is null, this family represents epsilon
    protected ForestNode v;
    protected ForestNode w;
    protected final State state;
    private Symbol[] combinedRHS = null;

    protected Family(ParseForest forest, ForestNode v, State state) {
        this.id = forest.nextFamilyId++;
        this.v = v;
        this.w = null;
        this.state = state;
    }

    protected Family(ParseForest forest, ForestNode w, ForestNode v, State state) {
        if (w == null) {
            throw ParseException.internalError("Attempt to create family with null 'w'");
        }
        this.id = forest.nextFamilyId++;
        this.w = w;
        this.v = v;
        this.state = state;
    }

    public Symbol getSymbol() {
        if (v == null) {
            return TerminalSymbol.EPSILON;
        }
        if (w == null) {
            if (v.symbol == null) {
                return v.state.symbol;
            }
            return v.symbol;
        }
        if (w.symbol == null) {
            return w.state.symbol;
        }
        return w.symbol;
    }

    public Symbol[] getRightHandSide() {
        // Work out what the combined right hand side for this family is. If there's only
        // one choice, it's that choice. If there are two choices, it's the concatenation
        // of the left and right.
        if (combinedRHS != null) {
            return combinedRHS;
        }

        if (v == null) {
            combinedRHS = new Symbol[0];
            return combinedRHS;
        }

        if (w == null) {
            if (v.state == null) {
                combinedRHS = new Symbol[0];
            } else {
                combinedRHS = v.state.getRhs().symbols;
            }
            return combinedRHS;
        }

        // The GLL parser sometimes constructs nodes that share the same RHS.
        // I'm not quite sure how or why or if this is a bug in the forest
        // builder. But for now...
        if (v.state != null && w.state != null && v.state.rhs == w.state.rhs) {
            combinedRHS = v.state.getRhs().symbols;
            return combinedRHS;
        }

        int slength = w.state == null ? 0 : w.state.getRhs().symbols.length;
        if (v.state != null) {
            slength += v.state.getRhs().symbols.length;
        }
        combinedRHS = new Symbol[slength];

        int pos = 0;
        if (w.state != null) {
            for (Symbol symbol : w.state.getRhs().symbols) {
                combinedRHS[pos] = symbol;
                pos++;
            }
        }
        if (v.state != null) {
            for (Symbol symbol : v.state.getRhs().symbols) {
                combinedRHS[pos] = symbol;
                pos++;
            }
        }
        return combinedRHS;
    }

    public List<ParserAttribute> getLeftAttributes() {
        if (w == null || state == null) {
            return Collections.emptyList();
        }

        // The GLL parser makes slightly different states. Sometimes there isn't a position - 2,
        // so I'm just guessing for the moment that it's position - 1 and the balance is different.
        if (state.position > 1) {
            return getAttributes(w.getSymbol(), state.rhs.get(state.position - 2));
        }
        return getAttributes(w.getSymbol(), state.rhs.get(state.position - 1));
    }

    public List<ParserAttribute> getRightAttributes() {
        if (v == null || state == null || state.position == 0) {
            return Collections.emptyList();
        }

        return getAttributes(v.getSymbol(), state.rhs.get(state.position - 1));
    }

    private List<ParserAttribute> getAttributes(Symbol symbol, Symbol rhsSymbol) {
        Token token = symbol instanceof TerminalSymbol ? ((TerminalSymbol) symbol).getToken() : null;
        Token rhsToken = rhsSymbol instanceof TerminalSymbol ? ((TerminalSymbol) rhsSymbol).getToken() : null;

        if (rhsSymbol.getAttributes().isEmpty()
                && (token == null || token.getAttributes().isEmpty())
                && (rhsToken == null || rhsToken.getAttributes().isEmpty())) {
            return Collections.emptyList();
        }

        ArrayList<ParserAttribute> list = new ArrayList<>();
        if (token != null) {
            list.addAll(token.getAttributes());
        }
        list.addAll(rhsSymbol.getAttributes());
        if (rhsToken != null) {
            list.addAll(rhsToken.getAttributes());
        }
        return list;
    }

    public ForestNode getLeftNode() {
        return w;
    }

    public ForestNode getRightNode() {
        return v;
    }

    public int getPriority() {
        int left = 0;
        int right = 0;
        boolean max = false;

        if (v != null) {
            left = v.getPriority();
            max = "max".equals(v.getPriorityStyle());
        }
        if (w != null) {
            right = w.getPriority();
            max = "max".equals(w.getPriorityStyle());
        }

        if (max) {
            return Math.max(left, right);
        }

        return left+right;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Family) {
            Family other = (Family) obj;
            if (v == null) {
                return other.v == null;
            }
            if (w == null) {
                return other.w == null && v.equals(other.v);
            }
            return w.equals(other.w) && v.equals(other.v);
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (v == null) {
            return 19;
        }
        if (w == null) {
            return v.hashCode();
        }
        return (31 * w.hashCode()) + v.hashCode();
    }

    @Override
    public String toString() {
        if (v == null) {
            return "ε";
        }
        if (w == null) {
            return v.toString();
        }
        return w + " / " + v;
    }
}
