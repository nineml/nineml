package org.nineml.coffeegrinder.trees;

import org.nineml.coffeegrinder.parser.Family;
import org.nineml.coffeegrinder.parser.ForestNode;

import java.util.List;

/**
 * An axe is a tool an Arborist might use to extract trees from a forest.
 */
public interface Axe {
    /**
     * Is this a specialist axe?
     * <p>A specialist axe can consider every tree in the forest differently every time it
     * is used. A non-specialist axe considers every tree at most once and returns all
     * the trees that are selected.</p>
     * @return true if the axe is specialist
     */
    boolean isSpecialist();

    /**
     * Select a branch from a list of branches.
     * <p>In an ambiguous forest, some nodes will have more than one possible choice. In any given
     * tree, only one choice may be selected. This function is called to make the selection.
     * </p>
     * <p>There will always be at least one element in the choices list when the method is called.
     * The method must return at least one.</p>
     * <p>The first node in the list returned is the choice selected for the tree currently under construction.
     * If only one choice is returned, the node becomes unambiguous on subsequent parses,
     * the same selection will always be used.
     * If additional choices are returned, they will be considered on subsequent parses. Note that if you
     * want the selected choice to be considered on future parses, it must appear in the list twice.
     * It is the only node that may appear twice.</p>
     * @param tree the parent node in the parse tree
     * @param forestNode the forest node
     * @param count the number of times node has occurred in this parse
     * @param choices the possible paths
     * @return the acceptable paths
     * @throws NullPointerException if null is returned
     * @throws org.nineml.coffeegrinder.exceptions.TreeWalkerException if an invalid choice is made
     */
    List<Family> select(ParseTree tree, ForestNode forestNode, int count, List<Family> choices);

    /**
     * Was the previous selection ambiguous?
     * <p>This method asks if the previous selection was ambiguous. If the axe ever indicates that
     * an ambiguous selection was made, the resulting parse is considered ambiguous. For example, the
     * {@link SequentialAxe} always returns true because it treats all choices as equivalent.
     * The {@link PriorityAxe} only considers a selection ambiguous if there wasn't a uniquely highest
     * priority selection.</p>
     * @return true if the choice made was random
     */
    boolean wasAmbiguousSelection();

    /**
     * Who's using this axe?
     * <p>Do not pass the same axe to more than one {@link org.nineml.coffeegrinder.trees.Arborist}.</p>
     * @param arborist the {@link Arborist} using this axe.
     */
    void forArborist(Arborist arborist);
}
