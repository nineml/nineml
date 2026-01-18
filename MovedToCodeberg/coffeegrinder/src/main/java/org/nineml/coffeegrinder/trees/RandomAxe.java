package org.nineml.coffeegrinder.trees;

import org.nineml.coffeegrinder.parser.Family;
import org.nineml.coffeegrinder.parser.ForestNode;

import java.util.ArrayList;
import java.util.List;

/**
 * An Axe that returns random trees.
 * <p>Whenever this axe has to make a choice, it picks randomly. There is no guarantee that this
 * will ever find a tree.</p>
 */
public class RandomAxe implements Axe {
    private Arborist arborist = null;

    @Override
    public boolean isSpecialist() {
        return true;
    }

    @Override
    public List<Family> select(ParseTree tree, ForestNode forestNode, int count, List<Family> choices) {
        ArrayList<Family> selected = new ArrayList<>();
        double randomChoice = Math.random() * choices.size();
        int index = (int) Math.floor(randomChoice);

        selected.add(choices.get(index));
        for (int pos = 0; pos < choices.size(); pos++) {
            if (pos != index) {
                selected.add(choices.get(pos));
            }
        }
        return selected;
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
