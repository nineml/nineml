package org.nineml.coffeegrinder.trees;

import org.nineml.coffeegrinder.parser.Family;
import org.nineml.coffeegrinder.parser.ForestNode;

import java.util.*;

/**
 * An Axe that returns the highest priority trees first.
 */
public class PriorityAxe implements Axe {
    private final boolean avoidLoops;
    private boolean ambiguous = false;
    private Arborist arborist = null;

    public PriorityAxe() {
        this(false);
    }

    public PriorityAxe(boolean avoidLoops) {
        this.avoidLoops = avoidLoops;
    }

    @Override
    public boolean isSpecialist() {
        return false;
    }

    @Override
    /**
     * Return the nodes in priority order.
     * <p>Note that this method always returns all of the possible choices. This avoids an infinite
     * loop if the highest priority choice selects a loop.</p>
     */
    public List<Family> select(ParseTree tree, ForestNode node, int count, List<Family> choices) {
        final ArrayList<Family> selected;
        if (avoidLoops && arborist != null) {
            selected = new ArrayList<>();
            for (Family family : choices) {
                if (!arborist.closesLoop(family.getLeftNode()) && !arborist.closesLoop(family.getRightNode())) {
                    selected.add(family);
                }
            }
        } else {
            selected = new ArrayList<>(choices);
        }

        selected.sort(Comparator.comparing(Family::getPriority).reversed());

        // This selection is ambiguous if the highest priority isn't unique.
        ambiguous = selected.size() > 1 && (selected.get(0).getPriority() == selected.get(1).getPriority());

        return selected;
    }

    @Override
    public boolean wasAmbiguousSelection() {
        return ambiguous;
    }

    @Override
    public void forArborist(Arborist arborist) {
        this.arborist = arborist;
    }
}
