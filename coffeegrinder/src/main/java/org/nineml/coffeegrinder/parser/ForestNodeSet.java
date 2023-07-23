package org.nineml.coffeegrinder.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * A utility class for constructing forest nodes.
 */
public class ForestNodeSet {
    // These odd data structures are an attempt to avoid creating new ForestNode
    // objects when they aren't needed. Performance testing says that the API
    // is spending ~20% of it's time creating ForestNodes. Creating one to answer
    // the question, did I need to create one? seems like something to avoid.
    private final HashMap<Symbol, HashMap<Long, ForestNode>> forestSymbolMap = new HashMap<>();
    private final HashMap<State, HashMap<Long, ForestNode>> forestStateMap = new HashMap<>();
    private final ParseForest graph;

    protected ForestNodeSet(ParseForest graph) {
        this.graph = graph;
    }

    protected void clear() {
        forestStateMap.clear();
        forestSymbolMap.clear();
    }

    protected ForestNode get(Symbol symbol, int j, int i) {
        long ji = ((long) j << 32) + i;
        if (forestSymbolMap.containsKey(symbol)) {
            return forestSymbolMap.get(symbol).getOrDefault(ji, null);
        }
        forestSymbolMap.put(symbol, new HashMap<>());
        return null;
    }

    protected ForestNode get(State state, int j, int i) {
        long ji = ((long) j << 32) + i;
        if (forestStateMap.containsKey(state)) {
            return forestStateMap.get(state).getOrDefault(ji, null);
        }
        forestStateMap.put(state, new HashMap<>());
        return null;
    }

    protected boolean contains(Symbol symbol, int j, int i) {
        return get(symbol, j, i) != null;
    }

    protected boolean contains(State state, int j, int i) {
        return get(state, j, i) != null;
    }

    protected ForestNode conditionallyCreateNode(Symbol symbol, State state, int j, int i) {
        ForestNode node = get(symbol, j, i);
        if (node == null) {
            long ji = ((long) j << 32) + i;
            node = graph.createNode(symbol, state, j, i);
            forestSymbolMap.get(symbol).put(ji, node);
        }
        return node;
    }

    protected ForestNode conditionallyCreateNode(State state, int j, int i) {
        ForestNode node = get(state, j, i);
        if (node == null) {
            long ji = ((long) j << 32) + i;
            node = graph.createNode(state, j, i);
            forestStateMap.get(state).put(ji, node);
        }
        return node;
    }

    protected Set<TerminalSymbol> openPredictions() {
        HashSet<TerminalSymbol> set = new HashSet<>();
        for (State state : forestStateMap.keySet()) {
            if (!state.completed()) {
                Symbol s = state.nextSymbol();
                if (s instanceof TerminalSymbol) {
                    set.add((TerminalSymbol) s);
                }
            }
        }
        return set;
    }
}
