package org.nineml.coffeegrinder.gll;

import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.util.StopWatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * The GLL parser constructs BinarySubtrees during parsing.
 */
public class BinarySubtree {
    public static final String logcategory = "GllParser";

    private final BsrPrefixInfo[] bsrPrefixes;
    private final BsrSlotInfo[] bsrSlots;

    private final ParserOptions options;
    private ArrayList<BinarySubtreeSlot> roots = null;
    public final NonterminalSymbol seed;
    private int rightExtent = 0;
    private boolean ambiguous = false;

    public BinarySubtree(NonterminalSymbol seed, int inputLength, ParserOptions options) {
        bsrPrefixes = new BsrPrefixInfo[inputLength];
        bsrSlots = new BsrSlotInfo[inputLength];
        for (int pos = 0; pos < inputLength; pos++) {
            bsrPrefixes[pos] = new BsrPrefixInfo();
            bsrSlots[pos] = new BsrSlotInfo();
        }

        this.options = options;
        this.seed = seed;
        rightExtent = 0;
    }

    protected void addEpsilon(State slot, int k) {
        BinarySubtreeSlot bsrnode = new BinarySubtreeSlot(slot, k, k, k);
        addSlot(bsrnode);
    }

    public void add(State L, int left, int pivot, int right) {
        if (right > rightExtent) {
            rightExtent = right;
        }

        if (L.nextSymbol() == null) {
            BinarySubtreeSlot bsrentry = new BinarySubtreeSlot(L, left, pivot, right);
            addSlot(bsrentry);
        } else if (L.position > 1) {
            BinarySubtreePrefix bsrentry = new BinarySubtreePrefix(L, left, pivot, right);
            addPrefix(bsrentry);
        }
    }

    private void addSlot(BinarySubtreeSlot node) {
        if (!ambiguous) {
            assert node.slot.symbol != null;
            for (BinarySubtreeSlot slot : bsrSlots[node.leftExtent].slots) {
                if (node.slot.symbol.equals(slot.slot.symbol) && node.rightExtent == slot.rightExtent) {
                    ambiguous = true;
                    break;
                }
            }
        }
        bsrSlots[node.leftExtent].slots.add(node);
    }

    private void addPrefix(BinarySubtreePrefix node) {
        bsrPrefixes[node.leftExtent].prefixes.add(node);
    }

    public int getRightExtent() {
        return rightExtent;
    }

    protected boolean succeeded(boolean moreInput) {
        if (roots != null) {
            return !roots.isEmpty();
        }

        if (moreInput) {
            roots = new ArrayList<>();
            return false;
        }

        return !getRoots().isEmpty();
    }

    private List<BinarySubtreeSlot> getRoots() {
        if (roots != null) {
            return roots;
        }

        roots = new ArrayList<>();

        for (BinarySubtreeSlot node : bsrSlots[0].slots) {
            assert node.slot.symbol != null;
            if (node.slot.symbol.equals(seed) && node.rightExtent == rightExtent) {
                roots.add(node);
            }
        }

        return roots;
    }

    protected ParseForest extractSPPF(ParserGrammar grammar, Token[] inputTokens) {
        ParseForestGLL G = new ParseForestGLL(options, grammar, rightExtent, inputTokens);
        int n = rightExtent;

        if (getRoots().isEmpty()) {
            return G;
        }

        StopWatch timer = new StopWatch();
        options.getLogger().debug(logcategory, "Constructing parse forest");

        BinarySubtreeNode success = getRoots().get(0);

        G.findOrCreate(success.slot, success.slot.symbol, 0, n);
        ForestNodeGLL w = G.extendableLeaf();
        while (w != null) {
            if (w.symbol != null) {
                for (BinarySubtreeNode node : bsrSlots[w.leftExtent].slots) {
                    if (node.rightExtent == w.rightExtent
                            && w.symbol.equals(node.slot.symbol)) {
                        w.addEdge(G.mkPN(node.slot, node.leftExtent, node.pivot, node.rightExtent));
                    }
                }
            } else {
                State u = w.state;
                assert u != null;
                if (u.position == 1) {
                    w.addEdge(G.mkPN(u, w.leftExtent, w.leftExtent, w.rightExtent));
                } else {
                    for (BinarySubtreePrefix pnode : bsrPrefixes[w.leftExtent].prefixes) {
                        if (pnode.rightExtent == w.rightExtent && pnode.matches(w)) {
                            w.addEdge(G.mkPN(u, pnode.leftExtent, pnode.pivot, pnode.rightExtent));
                        }
                    }
/*
                    for (BinarySubtreePrefix pnode : bsrPrefixes[w.leftExtent].prefixes) {
                        if (pnode.rightExtent == w.rightExtent && pnode.matches(w)) {
                            w.addEdge(G.mkPN(u, pnode.leftExtent, pnode.pivot, pnode.rightExtent));
                        }
                    }
 */
                }
            }

            w = G.extendableLeaf();
        }

        timer.stop();
        options.getLogger().debug(logcategory, "Constructed forest in %dms", timer.duration());

        G.prune();
        return G;
    }

    private static class BsrPrefixInfo {
        public final HashSet<BinarySubtreePrefix> prefixes;
        public BsrPrefixInfo() {
            prefixes = new HashSet<>();
        }
    }

    private static class BsrSlotInfo {
        public final HashSet<BinarySubtreeSlot> slots;
        public BsrSlotInfo() {
            slots = new HashSet<>();
        }
    }

    /*
    // N.B. This does not work. I need to extract just the gamma-core BSRs.
    private void getTree(TreeBuilder builder, Token[] tokens) {
        if (getRoots().isEmpty()) {
            return;
        }

        TreeWalker walker = new TreeWalker(tokens, this, options);
        List<BinarySubtreeSlot> alternatives = getRoots();
        BinarySubtreeSlot root = alternatives.get(0);
        if (alternatives.size() > 1) {
            ArrayList<RuleChoice> choices = new ArrayList<>(alternatives);
            int pos = builder.startAlternative(choices);
            if (pos < 0 || pos >= alternatives.size()) {
                throw new IllegalStateException("Invalid alternative selected");
            }
            root = alternatives.get(pos);
        }

        walker.buildTree(builder, root);

        if (alternatives.size() > 1) {

        }

    }

    private class TreeWalker {
        private static final String logcategory = "TreeBuilder";
        private final Token[] input;
        private final HashSet<BinarySubtreeNode> seen;
        private final BinarySubtree bsr;
        private final ParserOptions options;
        private TreeBuilder builder = null;

        public TreeWalker(Token[] input, BinarySubtree bsr, ParserOptions options) {
            this.input = input;
            this.bsr = bsr;
            this.options = options;
            seen = new HashSet<>();
        }

        public void buildTree(TreeBuilder builder, BinarySubtreeSlot root) {
            if (options.getLogger().getLogLevel(logcategory) >= Logger.TRACE) {
                options.getLogger().trace(logcategory, "Tree construction from BSR.");
                options.getLogger().trace(logcategory, "Prefixes:");
                SortedSet<Integer> keys = new TreeSet<>(bsr.bsrPrefixes.keySet());
                for (int idx : keys) {
                    options.getLogger().trace(logcategory, "%d", idx);
                    for (BinarySubtreePrefix prefix : bsr.bsrPrefixes.get(idx)) {
                        options.getLogger().trace(logcategory, "\t%s", prefix);
                    }
                }
                options.getLogger().trace(logcategory, "Slots:");
                keys = new TreeSet<>(bsr.bsrSlots.keySet());
                for (int idx : keys) {
                    options.getLogger().trace(logcategory, "%d", idx);
                    for (BinarySubtreeSlot slot : bsr.bsrSlots.get(idx)) {
                        options.getLogger().trace(logcategory, "\t%s", slot);
                    }
                }
            }

            this.builder = builder;
            builder.startTree();
            processSlot(root, 0, root.rightExtent, Collections.emptyList());
            builder.endTree();
            seen.clear();
        }

        private void processSlot(BinarySubtreeSlot node, int left, int right, Collection<ParserAttribute> inherited) {
            assert node.slot.symbol != null;

            final Collection<ParserAttribute> atts;
            if (inherited.isEmpty()) {
                atts = node.slot.symbol.getAttributes();
            } else {
                atts = new ArrayList<>(inherited);
                atts.addAll(node.slot.symbol.getAttributes());
            }

            builder.startNonterminal(node.slot.symbol, atts, node.leftExtent, node.rightExtent);
            recurse(node, left, right);
            builder.endNonterminal(node.slot.symbol, atts, node.leftExtent, node.rightExtent);
        }

        private void recurse(BinarySubtreeNode node, int left, int right) {
            if (node.slot.position > 0) {             // 0 = epsilon
                if (node.slot.position <= 2) {
                    if (node.slot.position == 2) {
                        recurse(node.slot.rhs.symbols[node.slot.position-2], left, node.pivot,
                                node.slot.rhs.symbols[node.slot.position-2].getAttributes());
                    }
                    recurse(node.slot.rhs.symbols[node.slot.position-1], node.pivot, right,
                            node.slot.rhs.symbols[node.slot.position-1].getAttributes());
                } else {
                    recursePrefix(node, left, node.pivot);
                    recurse(node.slot.rhs.symbols[node.slot.position-1], node.pivot, right,
                            node.slot.rhs.symbols[node.slot.position-1].getAttributes());
                }
            }
        }

        private void recurse(Symbol symbol, int left, int right, Collection<ParserAttribute> inherited) {
            final Collection<ParserAttribute> atts;
            if (inherited.isEmpty()) {
                atts = symbol.getAttributes();
            } else {
                atts = new ArrayList<>(inherited);
                atts.addAll(symbol.getAttributes());
            }

            if (symbol instanceof TerminalSymbol) {
                if (input[left].getAttributes().isEmpty()) {
                    builder.token(input[left], atts);
                } else {
                    ArrayList<ParserAttribute> xatts = new ArrayList<>(input[left].getAttributes());
                    xatts.addAll(atts);
                    builder.token(input[left], xatts);
                }
                return;
            }

            if (bsr.bsrSlots.containsKey(left)) {
                ArrayList<RuleChoice> found = new ArrayList<>();
                for (BinarySubtreeSlot slot : bsr.bsrSlots.get(left)) {
                    if (symbol.equals(slot.getSymbol()) && slot.rightExtent == right) {
                        if (seen.contains(slot)) {
                            if (left != right) {
                                builder.loop(slot);
                            }
                        } else {
                            found.add(slot);
                        }
                    }
                }

                if (found.isEmpty()) {
                    if (left == right) {
                        // If there are multiple paths to epsilon, we may have already seen one of the
                        // slots. Consider:
                        //
                        // alt-plus = alt, alt-star .
                        // alt-star = alt-option .
                        // alt-option = .
                        // alt-option = alt, alt-star .
                        //
                        // If alt can go to epsilon, then there's a kind of harmless ambiguity here.
                        // Just ignore it.
                        options.getLogger().trace(logcategory, "Ignoring ε loop for %s at %d", symbol, left);
                        return;
                    }
                    throw new IllegalStateException("BSR has no slot for " + symbol + " from " + left + " to " + right);
                }

                int index = 0;
                if (found.size() > 1) {
                    index = builder.startAlternative(found);
                    if (index < 0 || index >= found.size()) {
                        throw new IllegalStateException("Invalid alternative selected");
                    }
                }

                BinarySubtreeSlot selected = (BinarySubtreeSlot) found.get(index);

                seen.add(selected);
                options.getLogger().trace(logcategory, "Found slot  : %s", selected);
                processSlot(selected, left, right, inherited);
            }
        }

        private void recursePrefix(BinarySubtreeNode node, int left, int right) {
            if (bsr.bsrPrefixes.containsKey(left)) {
                BinarySubtreePrefix found = null;
                for (BinarySubtreePrefix prefix : bsr.bsrPrefixes.get(left)) {
                    if (!seen.contains(prefix) && prefix.rightExtent == right && prefixMatches(node, prefix)) {
                        if (found == null || prefix.pivot > found.pivot) {
                        if (found != null) {
                            System.err.println("*P* " + found);
                            System.err.println("*** " + prefix);
                        }
                            found = prefix;
                        }
                    }
                }

                if (found == null) {
                    throw new IllegalStateException("BSR has no prefix for " + node + " from " + left + " to " + right);
                }

                seen.add(found);
                options.getLogger().trace(logcategory, "Found prefix: %s", found);
                recurse(found, left, right);
            }
        }

        private boolean prefixMatches(BinarySubtreeNode node, BinarySubtreePrefix prefix) {
            assert node.slot.symbol != null;
            if (!node.slot.symbol.equals(prefix.slot.symbol)) {
                return false;
            }
            if (node.slot.rhs.length != prefix.slot.rhs.length) {
                return false;
            }
            for (int pos = 0; pos < node.slot.rhs.length; pos++) {
                if (!node.slot.rhs.symbols[pos].equals(prefix.slot.rhs.symbols[pos])) {
                    return false;
                }
            }
            return true;
        }
    }
     */
}
