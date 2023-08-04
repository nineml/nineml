package org.nineml.coffeegrinder.trees;

import org.nineml.coffeegrinder.parser.ForestNode;
import org.nineml.coffeegrinder.parser.NonterminalSymbol;
import org.nineml.coffeegrinder.parser.TerminalSymbol;
import org.nineml.coffeegrinder.util.ParserAttribute;

import java.util.*;

/**
 * A parse tree used when finding trees in the forest.
 * <p>This tree is exposed to the {@link Axe} during tree construction so that it's possible
 * to determine what parts of the tree have already been constructed.</p>
 */
public class ParseTree {
    public final Vertex vertex;
    /* package */ ParseTree left = null;
    /* package */ ParseTree right = null;
    public final ParseTree parent;
    private final boolean markAmbiguities;
    private final boolean ambiguous;

    /* package */ ParseTree(boolean markAmbiguities) {
        vertex = null;
        parent = null;
        ambiguous = false;
        this.markAmbiguities = markAmbiguities;
    }

    /* package */ ParseTree(ParseTree parent, Vertex vertex) {
        this.vertex = vertex;
        this.parent = parent;
        this.markAmbiguities = parent.markAmbiguities;
        this.ambiguous = markAmbiguities && vertex.isAmbiguous;
        assert parent.right == null;
        if (parent.left == null) {
            parent.left = this;
        } else {
            parent.right = this;
        }
    }

    public List<ParseTree> getChildren() {
        ArrayList<ParseTree> children = new ArrayList<>();
        if (left != null) {
            children.add(left);
        }
        if (right != null) {
            children.add(right);
        }
        return children;
    }

    /* package */ ParseTree addChild(Vertex vertex) {
        return new ParseTree(this, vertex);
    }

    /* package */ void build(TreeBuilder builder) {
        assert vertex != null;
        ForestNode node = vertex.node;

        if (node.symbol instanceof NonterminalSymbol) {
            builder.startNonterminal((NonterminalSymbol) node.symbol, attributeMap(vertex.parserAttributes), node.leftExtent, node.rightExtent);
            if (left != null) {
                left.build(builder);
            }
            if (right != null) {
                right.build(builder);
            }
            builder.endNonterminal((NonterminalSymbol) node.symbol, attributeMap(vertex.parserAttributes), node.leftExtent, node.rightExtent);
        } else if (node.symbol instanceof TerminalSymbol) {
            builder.token(((TerminalSymbol) node.getSymbol()).getToken(), attributeMap(vertex.parserAttributes), node.leftExtent, node.rightExtent);
        } else {
            if (vertex.isAmbiguous) {
                builder.startAmbiguity(node.id, node.leftExtent, node.rightExtent);
            }
            if (left != null) {
                left.build(builder);
            }
            if (right != null) {
                right.build(builder);
            }
            if (vertex.isAmbiguous) {
                builder.endAmbiguity(node.id, node.leftExtent, node.rightExtent);
            }
        }
    }

    private Map<String,String> attributeMap(List<ParserAttribute> attributes) {
        if (!ambiguous && attributes.isEmpty()) {
            return Collections.emptyMap();
        }

        HashMap<String,String> attmap = new HashMap<>();
        if (ambiguous) {
            attmap.put(ForestNode.AMBIGUOUS_ATTRIBUTE, "true");
        }

        for (ParserAttribute attr : attributes) {
            if (!attmap.containsKey(attr.getName())) {
                attmap.put(attr.getName(), attr.getValue());
            }
        }
        return attmap;
    }

    @Override
    public String toString() {
        if (vertex == null) {
            return "ROOT";
        }
        return vertex.toString();
    }
}
