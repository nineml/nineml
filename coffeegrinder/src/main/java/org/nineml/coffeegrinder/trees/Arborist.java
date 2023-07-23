package org.nineml.coffeegrinder.trees;

import org.nineml.coffeegrinder.parser.ForestNode;
import org.nineml.coffeegrinder.parser.NonterminalSymbol;
import org.nineml.coffeegrinder.parser.ParseForest;
import org.nineml.coffeegrinder.parser.TerminalSymbol;
import org.nineml.logging.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * One who might be employed to extract trees from a forest.
 */
abstract public class Arborist {
    protected final String logcategory = "Lumberjack";

    protected final Set<Integer> selectedNodes = new HashSet<>();
    public final ParseForest forest;
    protected final Logger logger;
    protected boolean ambiguous = false;
    protected boolean absolutelyAmbiguous = false;

    //public abstract ParseTree getTree();
    public abstract void getTree(TreeBuilder builder);
    public abstract boolean isAmbiguous();
    public abstract boolean isAbsolutelyAmbiguous();
    public abstract boolean hasMoreTrees();
    public abstract void reset();

    protected Arborist(ParseForest forest) {
        this.forest = forest;
        this.logger = forest.getOptions().getLogger();
    }

    public static Arborist getArborist(ParseForest forest) {
        return getArborist(forest, new SequentialAxe());
    }

    public static Arborist getArborist(ParseForest forest, Axe axe) {
        final Arborist treeCutter;
        if (axe.isSpecialist()) {
            treeCutter = new TreeSurgeon(forest, axe);
        } else {
            treeCutter = new Lumberjack(forest, axe);
        }
        axe.forArborist(treeCutter);
        return treeCutter;
    }

    public boolean closesLoop(ForestNode node) {
        if (node == null) {
            return false;
        }
        return selectedNodes.contains(node.id);
    };

    public Set<Integer> getSelectedNodes() {
        return new HashSet<>(selectedNodes);
    }

    protected void build(TreeBuilder builder, ParseTree tree) {
        assert tree.vertex != null;
        builder.startTree();
        tree.build(builder);
        builder.endTree(ambiguous, absolutelyAmbiguous, forest.isInfinitelyAmbiguous());
    }

    protected String showTree(ParseTree tree) {
        if (tree == null) {
            return "";
        }
        String xml = "";
        assert tree.vertex != null;
        ForestNode node = tree.vertex.node;
        if (node.symbol != null) {
            if (node.symbol instanceof NonterminalSymbol) {
                xml += String.format("<%s>", ((NonterminalSymbol) node.symbol).getName());
            } else {
                xml += String.format("%s", ((TerminalSymbol) node.symbol).getToken().getValue());
            }
        }
        xml += showTree(tree.left);
        xml += showTree(tree.right);
        if (node.symbol instanceof NonterminalSymbol) {
            xml += String.format("</%s>", ((NonterminalSymbol) node.symbol).getName());
        }
        return xml;
    }

    protected void debug(String format, Object... params) {
        logger.trace(logcategory, format, params);
    }

    protected void trace(String format, Object... params) {
        logger.trace(logcategory, format, params);
    }
}
