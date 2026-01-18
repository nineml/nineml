package org.nineml.coffeegrinder.trees;

import org.nineml.coffeegrinder.parser.Family;
import org.nineml.coffeegrinder.parser.NonterminalSymbol;
import org.nineml.coffeegrinder.util.ParserAttribute;

import java.util.List;
import java.util.Map;

/**
 * The tree selector is responsible for choosing among ambiguous parses.
 */
public interface TreeSelector {
    /**
     * Did the tree selector make an arbitrary choice?
     * <p>A non-arbitrary choice, resolved by priority or some other mechanism does not necessarily
     * count as an ambiguous choice.</p>
     * @return True if the selector made an arbitrary choice.
     */
    boolean getMadeAmbiguousChoice();

    /**
     * Start a nonterminal.
     * <p>The start and end nonterminal methods allow the tree selector to track what
     * choices have already been made.</p>
     * @param symbol The symbol.
     * @param attributes Its attributes.
     * @param leftExtent The starting position in the input.
     * @param rightExtent The ending position in the input.
     */
    void startNonterminal(NonterminalSymbol symbol, Map<String,String> attributes, int leftExtent, int rightExtent);

    /**
     * Enda a nonterminal.
     * <p>The start and end nonterminal methods allow the tree selector to track what
     * choices have already been made.</p>
     * @param symbol The symbol.
     * @param attributes Its attributes.
     * @param leftExtent The starting position in the input.
     * @param rightExtent The ending position in the input.
     */
    void endNonterminal(NonterminalSymbol symbol, Map<String,String> attributes, int leftExtent, int rightExtent);

    /**
     * Make a selection.
     * <p>The list of choices will always contain at least one choice. The list of otherChoices will
     * include any choices previously made. The method must return a choice.</p>
     * @param choices The available, unused choices.
     * @param otherChoices The available, but previously selected choices.
     * @return The selected choice.
     */
    Family select(List<Family> choices, List<Family> otherChoices);

    /**
     * Reset the state of the tree selector.
     */
    void reset();
}
