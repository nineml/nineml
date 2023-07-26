package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.util.ParserAttribute;

import java.util.*;

/**
 * A node in the SPPF.
 *
 * <p>When walking the graph, for example to extract parses, these nodes represent what's
 * available in the graph.</p>
 */
public class ForestNode {
    public static final String logcategory = "ForestNode";
    /**
     * The internal name of the attribute that holds priority.
     */
    public static final String PRIORITY_ATTRIBUTE = "https://coffeegrinder.nineml.org/attr/priority";

    private final TreeInfo emptyTree = new TreeInfo(0, 1);

    protected final ParseForest graph;
    public final Symbol symbol;
    public final State state;
    /**
     * The last position in the input covered by this node.
     */
    public final int rightExtent;
    /**
     * The first position in the input covered by this node.
     */
    public final int leftExtent;
    /**
     * This node's unique identifier.
     */
    public final int id;
    protected final ArrayList<Family> families = new ArrayList<>();
    protected int priority = 0;

    protected int reachable = 0;

    protected ForestNode(ParseForest graph, Symbol symbol, int leftExtent, int rightExtent) {
        this.graph = graph;
        this.symbol = symbol;
        this.state = null;
        this.rightExtent = rightExtent;
        this.leftExtent = leftExtent;
        id = graph.nextForestNodeId++;
    }

    protected ForestNode(ParseForest graph, State state, int leftExtent, int rightExtent) {
        this.graph = graph;
        this.symbol = null;
        this.state = state;
        this.rightExtent = rightExtent;
        this.leftExtent = leftExtent;
        id = graph.nextForestNodeId++;
    }

    // N.B. This is a *symbol* node, the state is just being carried along so that we can tell
    // what rule defined this symbol. That can be useful when analysing the parse tree.
    protected ForestNode(ParseForest graph, Symbol symbol, State state, int leftExtent, int rightExtent) {
        this.graph = graph;
        this.symbol = symbol;
        this.state = state;
        this.rightExtent = rightExtent;
        this.leftExtent = leftExtent;
        id = graph.nextForestNodeId++;
    }

    /**
     * Returns the priority style.
     */
    protected String getPriorityStyle() {
        return graph.getOptions().getPriorityStyle();
    }

    /**
     * What symbol does this node represent?
     * <p>There are nodes for terminal and nonterminal symbols as well as intermediate nodes
     * representing partial parses of a rule.</p>
     *
     * @return The symbol, or null if this is a state node
     */
    public Symbol getSymbol() {
        return symbol;
    }

    /**
     * What state does this node represent?
     * <p>There are nodes for terminal and nonterminal symbols as well as intermediate nodes
     * representing partial parses of a rule. In order to expose as much information as possible,
     * the states associated with nonterminal symbols are included in the graph.</p>
     *
     * @return The state associated with this node, or null if this is a terminal symbol node
     */
    public State getState() {
        return state;
    }

    public List<Family> getFamilies() {
        return families;
    }

    public void addFamily(ForestNode v, State state) {
        for (Family family : families) {
            if (family.w == null) {
                if ((v == null && family.v == null) || (v != null && v.equals(family.v))) {
                    return;
                }
            }
        }

        families.add(new Family(graph, v, state));
    }

    public void addFamily(ForestNode w, ForestNode v, State state) {
        for (Family family : families) {
            if (((v == null && family.v == null) || (v != null && v.equals(family.v)))
                    && ((w == null && family.w == null) || (w != null && w.equals(family.w)))) {
                return;
            }
        }

        families.add(new Family(graph, w, v, state));
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ForestNode) {
            ForestNode other = (ForestNode) obj;
            if (state == null) {
                assert symbol != null;
                return symbol.equals(other.symbol) && rightExtent == other.rightExtent && leftExtent == other.leftExtent;
            }
            return Objects.equals(symbol, other.symbol) && state.equals(other.state) && rightExtent == other.rightExtent && leftExtent == other.leftExtent;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int code = (17 * rightExtent) + (31 * leftExtent);
        if (symbol != null) {
            code += 11 * symbol.hashCode();
        } else {
            assert state != null;
            code += 13 * state.hashCode();
        }
        return code;
    }

    @Override
    public String toString() {
        if (symbol == null) {
            return state + ", " + leftExtent + ", " + rightExtent;
        } else {
            return symbol + ", " + leftExtent + ", " + rightExtent;
        }
    }

    private static class TreeInfo {
        public final int priority;
        public final int trees;

        public TreeInfo(int priority, int trees) {
            this.priority = priority;
            this.trees = trees;
        }

        @Override
        public String toString() {
            return String.format("%d / %d", priority, trees);
        }
    }
}
