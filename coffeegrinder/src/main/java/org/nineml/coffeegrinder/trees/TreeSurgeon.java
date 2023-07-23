package org.nineml.coffeegrinder.trees;

import org.nineml.coffeegrinder.exceptions.TreeWalkerException;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.util.ParserAttribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A specialist Arborist.
 * <p>The tree surgeon makes no attempt to return trees in sequential order and treats every
 * request as distinct.</p>
 */
public class TreeSurgeon extends Arborist {
    private final ParseForest forest;
    private final Axe selector;
    private final HashMap<ForestNode,Integer> seenCount = new HashMap<>();
    private boolean ambiguous = false;
    private boolean absolutelyAmbiguous = false;
    private boolean moreTrees = true;

    protected TreeSurgeon(ParseForest forest, Axe axe) {
        super(forest);
        this.forest = forest;
        this.selector = axe;
    }

    @Override
    public void reset() {
        ambiguous = false;
        absolutelyAmbiguous = false;
        moreTrees = true;
    }

    @Override
    public boolean isAmbiguous() {
        return ambiguous;
    }

    @Override
    public boolean isAbsolutelyAmbiguous() {
        return absolutelyAmbiguous;
    }

    @Override
    public boolean hasMoreTrees() {
        return moreTrees;
    }

    @Override
    public void getTree(TreeBuilder builder) {
        if (forest.getRoot() == null || forest.getRoot().symbol == null) {
            throw TreeWalkerException.noMoreTrees();
        }
        getNextTree(builder);
    }

    private void getNextTree(TreeBuilder builder) {
        ForestNode node = forest.getRoot();
        List<ParserAttribute> parserAttributes = forest.getRoot().getSymbol().getAttributes();

        seenCount.clear();
        selectedNodes.clear();
        ParseTree tree = new ParseTree();
        seek(node, parserAttributes, tree);
        build(builder, tree.left);
    }

    private boolean seek(ForestNode node, List<ParserAttribute> parserAttributes, ParseTree root) {
        final int scount;
        if (seenCount.containsKey(node)) {
            scount = seenCount.get(node)+1;
        } else {
            scount = 0;
        }
        seenCount.put(node, scount);

        final Vertex vertex = new Vertex(node, parserAttributes);
        final ParseTree tree = root.addChild(vertex);

        if (node.getFamilies().isEmpty()) {
            return true;
        }

        final Family choice;
        if (vertex.isAmbiguous) {
            absolutelyAmbiguous = true;
            moreTrees = true;

            List<Family> originalChoices = new ArrayList<>(node.getFamilies());
            List<Family> selection = selector.select(root, node, scount, originalChoices);
            if (selection == null) {
                throw new NullPointerException("Select returned null");
            }
            if (selection.isEmpty()) {
                throw TreeWalkerException.noChoiceSelected();
            }

            ArrayList<Family> remainingChoices = new ArrayList<>();
            for (Family family : selection) {
                if (remainingChoices.contains(family) || !vertex.choices.contains(family)) {
                    throw TreeWalkerException.invalidChoiceSelected();
                }
                remainingChoices.add(family);
            }
            choice = remainingChoices.remove(0);

            vertex.choices.clear();
            vertex.choices.addAll(remainingChoices);

            ambiguous = remainingChoices.size() > 1;
        } else {
            choice = node.getFamilies().get(0);
        }

        boolean goLeft = choice.getLeftNode() != null;
        boolean goRight = choice.getRightNode() != null;

        boolean ok = true;
        if (goLeft) {
            ok = seek(choice.getLeftNode(), choice.getLeftAttributes(), tree);
        }
        if (goRight) {
            ok = ok && seek(choice.getRightNode(), choice.getRightAttributes(), tree);
        }

        return ok;
    }
}
