package org.nineml.coffeegrinder.trees;

import org.nineml.coffeegrinder.parser.Family;
import org.nineml.coffeegrinder.parser.ForestNode;

import java.util.ArrayList;
import java.util.List;

/**
 * An Axe that returns ever tree in the forest.
 */
public class SequentialAxe implements Axe {
    private final boolean avoidLoops;
    private Arborist arborist = null;

    public SequentialAxe() {
        this(false);
    }

    public SequentialAxe(boolean avoidLoops) {
        this.avoidLoops = avoidLoops;
    }

    @Override
    public boolean isSpecialist() {
        return false;
    }

    @Override
    public List<Family> select(ParseTree tree, ForestNode forestNode, int count, List<Family> choices) {
        if (avoidLoops && arborist != null) {
            List<Family> available = new ArrayList<>();
            for (Family family : choices) {
                if (!arborist.closesLoop(family.getLeftNode()) && !arborist.closesLoop(family.getRightNode())) {
                    available.add(family);
                }
            }
            return available;
        }
        return choices;
    }

    @Override
    public boolean wasAmbiguousSelection() {
        return true;
    }

    @Override
    public void forArborist(Arborist arborist) {
        this.arborist = arborist;
    }
}
