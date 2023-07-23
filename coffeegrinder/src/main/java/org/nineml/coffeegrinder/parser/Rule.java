package org.nineml.coffeegrinder.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * A grammar rule.
 *
 * <p>A grammar rule maps a single {@link NonterminalSymbol} to zero or more {@link Symbol symbols} (either {@link TerminalSymbol Terminals}
 * or further {@link NonterminalSymbol Nonterminals}).</p>
 */
public class Rule {
    public final NonterminalSymbol symbol;
    public final RightHandSide rhs;

    /**
     * Construct a new rule mapping the nonterminal to a sequence of symbols.
     * <p>If the sequence of symbols is empty or null, then the nonterminal maps to "ε", that is to say,
     * it's allowed to be absent.</p>
     * @param symbol The nonterminal.
     * @param rhs The sequence of symbols.
     */
    public Rule(NonterminalSymbol symbol, Symbol... rhs) {
        if (symbol == null) {
            throw new NullPointerException("Rule name cannot be null");
        }

        this.symbol = symbol;
        this.rhs = new RightHandSide(rhs);
    }

    /**
     * Construct a new rule mapping the nonterminal to a sequence of symbols.
     * <p>If the sequence of symbols is empty or null, then the nonterminal maps to "ε", that is to say,
     * it's allowed to be absent.</p>
     * @param symbol The nonterminal.
     * @param rhs The list of symbols.
     */
    public Rule(NonterminalSymbol symbol, List<Symbol> rhs) {
        if (symbol == null) {
            throw new NullPointerException("Rule name cannot be null");
        }

        this.symbol = symbol;
        this.rhs = new RightHandSide(rhs);
    }

    /**
     * The nonterminal symbol defined by this rule.
     * @return The nonterminal symbol.
     */
    public NonterminalSymbol getSymbol() {
        return symbol;
    }

    /**
     * The sequence of symbols that comprise the definition of the rule's nonterminal.
     * <p>Note: although a rule may be defined with a null "right hand side", this method
     * always returns an empty list in such cases.</p>
     * @return The sequence of symbols.
     */
    public RightHandSide getRhs() {
        return rhs;
    }

    public boolean epsilonRule() {
        return rhs.isEmpty();
    }

    public List<State> getSlots() {
        // You want a copy every time.
        ArrayList<State> ruleSlots = new ArrayList<>();
        for (int pos = 0; pos <= rhs.length; pos++) {
            ruleSlots.add(new State(this, pos));
        }
        return ruleSlots;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Rule) {
            Rule other = (Rule) obj;
            if (symbol != other.symbol) {
                return false;
            }
            return rhs.equals(other.rhs);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return symbol.hashCode() + rhs.hashCode();
    }

    /**
     * Pretty print a node.
     * @return a string representation of the node.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(symbol);
        sb.append(" ::= ");
        int count = 0;
        for (Symbol symbol : rhs.symbols) {
            if (count > 0) {
                sb.append(", ");
            }
            if (symbol == null) {
                sb.append("<null>");
            } else {
                sb.append(symbol.toString());
            }
            count += 1;
        }
        return sb.toString();
    }
}
