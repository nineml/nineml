package org.nineml.coffeegrinder.trees;

import org.nineml.coffeegrinder.parser.Family;
import org.nineml.coffeegrinder.parser.ForestNode;
import org.nineml.coffeegrinder.util.ParserAttribute;
import org.nineml.logging.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Vertex {
    public final ForestNode node;
    /* package */ final List<Family> choices;
    public final boolean isAmbiguous;
    public final List<ParserAttribute> parserAttributes;

    public Vertex(ForestNode node, List<ParserAttribute> parserAttributes) {
        this.node = node;
        this.parserAttributes = Collections.unmodifiableList(parserAttributes);
        choices = new ArrayList<>(node.getFamilies());
        isAmbiguous = choices.size() > 1;
    }

    public List<Family> getChoices() {
        return new ArrayList<>(choices);
    }

    @Override
    public String toString() {
        return String.format("%s / %d", node, choices.size());
    }
}
