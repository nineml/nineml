package org.nineml.coffeegrinder.trees;

import org.nineml.coffeegrinder.exceptions.TreeWalkerException;
import org.nineml.coffeegrinder.parser.Family;
import org.nineml.coffeegrinder.parser.ForestNode;
import org.nineml.coffeegrinder.parser.ParseForest;
import org.nineml.coffeegrinder.util.ParserAttribute;

import java.util.*;

/**
 * An "unspecialist" Arborist.
 * <p>The lumberjac extracts trees sequentially from a forest.</p>
 */

// FIXME: should DeterministicLumberJack extend NondeterministicLumberjack?

public class Lumberjack extends Arborist {
    private final ParseForest forest;
    private final Axe selector;
    private final HashMap<ForestNode,ArrayList<Vertex>> selected = new HashMap<>();
    private final HashMap<ForestNode,Integer> seenCount = new HashMap<>();
    private final ArrayList<TreeNumber> seeds;
    private TreeNumber treeNumber = null;
    private int numberPosition = 0;
    private boolean moreTrees = true;
    private Vertex nextVertex = null;

    protected Lumberjack(ParseForest forest, Axe axe) {
        super(forest);
        this.forest = forest;
        this.selector = axe;
        seeds = new ArrayList<>();
        treeNumber = new TreeNumber();

        int base = 0;
        for (ForestNode node : forest.getAmbiguousNodes()) {
            if (node.getFamilies().size() > base) {
                base = node.getFamilies().size();
            }
        }
    }

    @Override
    public void reset() {
        absolutelyAmbiguous = false;
        ambiguous = false;
        seeds.clear();
        treeNumber = new TreeNumber();
        numberPosition = 0;
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
            throw TreeWalkerException.noTrees();
        }
        getNextTree(builder);
    }

    private void getNextTree(TreeBuilder builder) {
        if (!hasMoreTrees()) {
            throw TreeWalkerException.noMoreTrees();
        }

        ForestNode node = forest.getRoot();
        List<ParserAttribute> parserAttributes = forest.getRoot().getSymbol().getAttributes();

        ParseTree tree = null;

        boolean ok = false;
        while (!ok) {
            moreTrees = false;
            seenCount.clear();
            selectedNodes.clear();
            numberPosition = 0;
            nextVertex = null;

            tree = new ParseTree();
            ok = seek(node, parserAttributes, tree);

            //System.err.printf("%s %s%n", treeNumber, ok);

            if (ok) {
                if (treeNumber.isMax()) {
                    if (!seeds.isEmpty()) {
                        treeNumber = seeds.remove(0);
                        moreTrees = true;
                    }
                } else {
                    treeNumber.advance();
                    moreTrees = true;
                }
            } else {
                // The parse failed. There must be more trees.
                seeds.add(new TreeNumber(treeNumber, nextVertex.choices.size()));
                moreTrees = true;
                if (treeNumber.isMax()) {
                    treeNumber = seeds.remove(0);
                } else {
                    treeNumber.advance();
                }
            }
        }

        /*
        StringTreeBuilder test = new StringTreeBuilder();
        build(test, tree.left);
        String xml = test.getTree();
         */

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
        selectedNodes.add(node.id);

        if (!selected.containsKey(node)) {
            selected.put(node, new ArrayList<>());
        }
        ArrayList<Vertex> vertices = selected.get(node);

        final Vertex vertex;
        if (scount < vertices.size()) {
            vertex = vertices.get(scount);
        } else {
            assert scount == vertices.size();
            vertex = new Vertex(node, parserAttributes);
            vertices.add(vertex);
            if (vertex.isAmbiguous) {
                absolutelyAmbiguous = true;
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
                vertex.choices.clear();
                vertex.choices.addAll(remainingChoices);

                ambiguous = ambiguous || selector.wasAmbiguousSelection();
            }
        }

        final ParseTree tree = root.addChild(vertex);

        if (node.getFamilies().isEmpty()) {
            return true;
        }

        final Family choice;
        if (vertex.isAmbiguous) {
            if (numberPosition == treeNumber.length) {
                nextVertex = vertex;
                return false;
            }
            int choiceIndex = 0;
            choiceIndex = treeNumber.digit(numberPosition);
            if (choiceIndex >= vertex.choices.size()) {
                throw new IllegalStateException("Choice exceeds number of choices");
            }
            numberPosition++;

            choice = vertex.choices.get(choiceIndex);
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
