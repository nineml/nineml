package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.tokens.TokenString;
import org.nineml.coffeegrinder.util.ParserAttribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * An SPPF is a shared packed parse forest.
 * <p>The SPPF is a graph representation of all the (possibly infinite) parses that can be used
 * to recognize the input sequence as a sentence in the grammar. This forest is created by the GLL
 * parser.</p>
 */
public class ParseForestGLL extends ParseForest {
    private final HashSet<ForestNodeGLL> extendedLeaves;
    private final ArrayList<ForestNodeGLL> candidateLeaves;
    private final HashMap<Symbol, PrefixTrie> intermediate;
    private final HashMap<Symbol, PrefixTrie> slotPrefixes;
    private final HashMap<Symbol, HashMap<Integer, HashMap<Integer, ArrayList<ForestNodeGLL>>>> nodes;
    private final HashMap<PrefixTrie, HashMap<Integer, HashMap<Integer, ArrayList<ForestNodeGLL>>>> slots;
    private final ParserGrammar grammar;
    private final int rightExtent;
    private final Token[] inputTokens;

    public ParseForestGLL(ParserOptions options, ParserGrammar grammar, int rightExtent, Token[] inputTokens) {
        super(options);
        this.grammar = grammar;
        this.rightExtent = rightExtent;
        this.inputTokens = inputTokens;
        intermediate = new HashMap<>();
        slotPrefixes = new HashMap<>();
        nodes = new HashMap<>();
        slots = new HashMap<>();
        extendedLeaves = new HashSet<>();
        candidateLeaves = new ArrayList<>();
    }

    public ForestNodeGLL findOrCreate(State state, Symbol symbol, int leftExtent, int rightExtent) {
        // Make sure the symbol in the parse tree is the actual character from the input.
        // (And not, for example, the character class from the rule.)
        // The EarleyParser seems to build the states that way, but the GLL parser doesn't.
        // (I wonder if it could be made to?)
        // Sometimes the symbol in the grammar has attributes (e.g., tmarks in a grammar)
        // and sometimes the symbol in the input has attributes. Combine them.
        // Also deal with regex matches
        if (symbol instanceof TerminalSymbol) {
            ArrayList<ParserAttribute> attr = new ArrayList<>(symbol.getAttributes());
            attr.addAll(inputTokens[leftExtent].getAttributes());
            if (leftExtent + 1 == rightExtent) {
                symbol = new TerminalSymbol(inputTokens[leftExtent], attr);
            } else {
                StringBuilder sb = new StringBuilder();
                for (int pos = leftExtent; pos < rightExtent; pos++) {
                    sb.append(inputTokens[pos].getValue());
                }
                if (symbol != TerminalSymbol.EPSILON) {
                    symbol = new TerminalSymbol(TokenString.get(sb.toString()), attr);
                }
            }
        }

        if (!nodes.containsKey(symbol)) {
            nodes.put(symbol, new HashMap<>());
        }
        if (!nodes.get(symbol).containsKey(leftExtent)) {
            nodes.get(symbol).put(leftExtent, new HashMap<>());
        }
        if (!nodes.get(symbol).get(leftExtent).containsKey(rightExtent)) {
            ForestNodeGLL node = new ForestNodeGLL(this, symbol, state, leftExtent, rightExtent);
            if (!(symbol instanceof TerminalSymbol) && !extendedLeaves.contains(node)) {
                candidateLeaves.add(node);
            }
            graph.add(node);
            graphIds.add(node.id);
            ArrayList<ForestNodeGLL> list = new ArrayList<>();
            list.add(node);
            nodes.get(symbol).get(leftExtent).put(rightExtent, list);
            return node;
        }
        return nodes.get(symbol).get(leftExtent).get(rightExtent).get(0);
    }

    protected ForestNodeGLL findOrCreate(State slot, int leftExtent, int rightExtent) {
        PrefixTrie trie = getPrefix(slot, slotPrefixes);

        if (!slots.containsKey(trie)) {
            slots.put(trie, new HashMap<>());
        }
        if (!slots.get(trie).containsKey(leftExtent)) {
            slots.get(trie).put(leftExtent, new HashMap<>());
        }
        if (!slots.get(trie).get(leftExtent).containsKey(rightExtent)) {
            ForestNodeGLL node = new ForestNodeGLL(this, slot, leftExtent, rightExtent);
            if (!extendedLeaves.contains(node)) {
                candidateLeaves.add(node);
            }
            graph.add(node);
            graphIds.add(node.id);
            ArrayList<ForestNodeGLL> list = new ArrayList<>();
            list.add(node);
            slots.get(trie).get(leftExtent).put(rightExtent, list);
            return node;
        }

        return slots.get(trie).get(leftExtent).get(rightExtent).get(0);
    }

    public ForestNodeGLL extendableLeaf() {
        if (candidateLeaves.isEmpty()) {
            return null;
        }

        ForestNodeGLL leaf = candidateLeaves.remove(0);
        extendedLeaves.add(leaf);
        return leaf;
    }

    protected ForestNodeGLL create(State slot, int pivot) {
        PrefixTrie trie = getPrefix(slot, intermediate);
        if (!trie.nodes.containsKey(pivot)) {
            trie.nodes.put(pivot, new ArrayList<>());
        }

        ForestNodeGLL node = new ForestNodeGLL(this, slot, pivot);
        trie.nodes.get(pivot).add(node);
        return node;
    }

    private PrefixTrie getPrefix(State slot, HashMap<Symbol, PrefixTrie> root) {
        final Symbol start;
        if (slot.position == 0) {
            start = TerminalSymbol.EPSILON;
        } else {
            start = slot.rhs.get(0);
        }
        if (!root.containsKey(start)) {
            root.put(start, new PrefixTrie(start));
        }
        PrefixTrie trie = root.get(start);
        for (int pos = 1; pos < slot.position; pos++) {
            trie = trie.child(slot.rhs.get(pos));
        }
        return trie;
    }

    public ForestNodeGLL mkPN(State slot, int leftExtent, int pivot, int rightExtent) {
        ForestNodeGLL y = create(slot, pivot);
        if (slot.position == 0) {
            mkN(slot, TerminalSymbol.EPSILON, leftExtent, leftExtent, y);
        }

        if (slot.position > 0) {
            Symbol x = slot.prevSymbol();
            mkN(slot, x, pivot, rightExtent, y);
            if (slot.position == 2) {
                mkN(slot, slot.rhs.get(0), leftExtent, pivot, y);
            } else if (slot.position > 2) {
                assert slot.rule != null;
                State newSlot = new State(slot.rule, slot.position-1);
                mkN(newSlot, leftExtent, pivot, y);
            }
        }

        return y;
    }

    protected void mkN(State state, Symbol symbol, int leftExtent, int rightExtent, ForestNodeGLL parent) {
        ForestNodeGLL node = findOrCreate(state, symbol, leftExtent, rightExtent);
        parent.addEdge(node);
    }

    protected void mkN(State slot, int leftExtent, int rightExtent, ForestNodeGLL parent) {
        ForestNodeGLL node = findOrCreate(slot, leftExtent, rightExtent);
        parent.addEdge(node);
    }

    @Override
    public void prune() {
        // Step 0. Unlink the pointers to a single epsilon terminal and remove it.
        ForestNode epsilon = null;
        for (ForestNode fnode : graph) {
            for (Family family : fnode.families) {
                if (family.w == null && family.v.symbol == TerminalSymbol.EPSILON) {
                    family.v = null;
                }
            }
            if (fnode.symbol == TerminalSymbol.EPSILON) {
                epsilon = fnode;
            }

            if (grammar.getSeed().equals(fnode.symbol) && fnode.leftExtent == 0 && fnode.rightExtent == rightExtent) {
                roots.add(fnode);
                rootIds.add(fnode.id);
            }
        }

        if (epsilon != null) {
            graph.remove(epsilon);
        }

        super.prune();
    }

    private static class PrefixTrie {
        public final Symbol symbol;
        public final HashMap<Symbol, PrefixTrie> children;
        public final HashMap<Integer, ArrayList<ForestNodeGLL>> nodes;
        public PrefixTrie(Symbol symbol) {
            this.symbol = symbol;
            children = new HashMap<>();
            nodes = new HashMap<>();
        }
        public PrefixTrie child(Symbol symbol) {
            if (children.containsKey(symbol)) {
                return children.get(symbol);
            }
            PrefixTrie newchild = new PrefixTrie(symbol);
            children.put(symbol, newchild);
            return newchild;
        }
    }
}
