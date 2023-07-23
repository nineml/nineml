package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.exceptions.ParseException;
import org.nineml.coffeegrinder.gll.Descriptor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * States (or Earley items) in the chart.
 *
 * FIXME: this API is in flux.
 */
public class State {
    public static final State L0 = new State();

    public final Rule rule;
    public final NonterminalSymbol symbol;
    public final int position;
    public final RightHandSide rhs;
    private Integer cachedCode = null;
    private HashSet<Symbol> firstSet = null;

    private State() {
        symbol = null;
        rule = null;
        position = 0;
        rhs = new RightHandSide(new Symbol[0]);
    }

    protected State(Rule rule) {
        this.rule = rule;
        this.symbol = rule.getSymbol();
        this.rhs = rule.getRhs();
        this.position = 0;
    }

    protected State(State other, int position) {
        this.rule = other.rule;
        this.symbol = other.symbol;
        this.rhs = other.rhs;
        this.position = position;
    }

    protected State(Rule rule, int position) {
        this.rule = rule;
        this.symbol = rule.symbol;
        this.rhs = rule.rhs;
        this.position = position;
    }

    protected State(NonterminalSymbol symbol, int pos, List<Symbol> rhs) {
        this.rule = new Rule(symbol, rhs);
        this.symbol = symbol;
        this.rhs = rule.getRhs();
        this.position = pos;
    }

    /**
     * Get the next symbol
     * <p>If the state has more symbols, return the symbol that occurs after the current position.
     * </p>
     * @return the next symbol, or null if position is last
     */

    public Symbol nextSymbol() {
        if (position >= rhs.length) {
            return null;
        }
        return rhs.get(position);
    }

    public Symbol prevSymbol() {
        if (position == 0) {
            return null;
        }
        return rhs.get(position - 1);
    }

    public Descriptor getDescriptor(int k, int i) {
        return new Descriptor(this, k, i);
    }

    /**
     * Get the nonterminal associated with this state
     * @return the symbol
     */
    public NonterminalSymbol getSymbol() {
        return symbol;
    }

    /**
     * Get the rule that originated this state.
     * @return the rule
     */
    public Rule getRule() {
        return rule;
    }

    /**
     * Get the list of symbols that define this state's nonterminal symbol.
     * @return the list of symbols on the "right hand side"
     */
    public RightHandSide getRhs() {
        return rhs;
    }

    /**
     * Get the current position in this state
     * @return the position
     */
    public int getPosition() {
        return position;
    }

    /**
     * Get a new state with the position advanced by one
     * @return a new state with the position advanced
     * @throws ParseException if an attempt is made to advance a completed state
     */
    public State advance() {
        if (position < rhs.length) {
            return new State(this, position+1);
        } else {
            throw ParseException.internalError("Cannot advance a completed state");
        }
    }

    /**
     * Are we finished with this symbol?
     * @return true if the position indicates that we've seen all of the symbols on the "right hand side"
     */
    public boolean completed() {
        return position == rhs.length;
    }

    public Set<Symbol> getFirst(ParserGrammar grammar) {
        if (firstSet == null) {
            firstSet = new HashSet<>();
            int spos = position;
            boolean keepGoing = spos < rhs.length;
            while (keepGoing) {
                Set<Symbol> first = grammar.getFirst(rhs.get(spos));
                firstSet.addAll(first);

                keepGoing = firstSet.contains(TerminalSymbol.EPSILON);
                if (grammar.isNullable(rhs.get(spos))) {
                    first.add(TerminalSymbol.EPSILON);
                    keepGoing = true;
                }

                if (!keepGoing) {
                    return firstSet;
                }

                spos++;
                keepGoing = spos < rhs.length;
            }
            if (spos == 0 && rhs.length == 0) {
                firstSet.add(TerminalSymbol.EPSILON);
            }
        }
        return firstSet;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof State) {
            State other = (State) obj;
            return symbol.equals(other.symbol) && position == other.position
                    && rhs.equals(other.rhs);
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (cachedCode == null) {
            cachedCode = symbol.hashCode() + (13 * position) + rhs.hashCode();
        }
        return cachedCode;
    }

    @Override
    public String toString() {
        if (this == L0) {
            return "L₀";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(symbol);
        sb.append(" ⇒ ");
        int count = 0;
        if (rhs.symbols == null) {
            sb.append("<<null>>");
        } else {
            for (Symbol symbol : rhs.symbols) {
                if (count > 0) {
                    sb.append(" ");
                }
                if (count == position) {
                    sb.append("• ");
                }
                if (symbol == null) {
                    sb.append("<null>");
                } else {
                    sb.append(symbol.toString());
                }
                count += 1;
            }
            if (count == position) {
                if (count > 0) {
                    sb.append(" ");
                }
                sb.append("•");
            }
        }
        return sb.toString();
    }
}
