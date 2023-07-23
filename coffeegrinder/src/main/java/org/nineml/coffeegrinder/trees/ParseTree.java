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
    /* package */ final int cost;
    public final Vertex vertex;
    /* package */ ParseTree left = null;
    /* package */ ParseTree right = null;
    public final ParseTree parent;

    /* package */ ParseTree() {
        cost = 0;
        vertex = null;
        parent = null;
    }

    /* package */ ParseTree(ParseTree parent, Vertex vertex, int cost) {
        this.vertex = vertex;
        this.cost = cost;
        this.parent = parent;
        if (parent.right != null) {
            throw new RuntimeException("BANG");
        }
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
        return addChild(vertex, -1);
    }

    /* package */ ParseTree addChild(Vertex vertex, int cost) {
        return new ParseTree(this, vertex, cost);
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
            if (left != null) {
                left.build(builder);
            }
            if (right != null) {
                right.build(builder);
            }
        }
    }

    private Map<String,String> attributeMap(List<ParserAttribute> attributes) {
        if (attributes.isEmpty()) {
            return Collections.emptyMap();
        }

        HashMap<String,String> attmap = new HashMap<>();
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
        return vertex + " :: " + (left == null ? "" : "L") + (right == null ? "" : "R");
    }
}
