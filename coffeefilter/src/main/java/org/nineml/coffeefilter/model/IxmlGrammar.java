package org.nineml.coffeefilter.model;

public class IxmlGrammar extends XNonterminal {
    /**
     * Create an IxmlGrammar.
     *
     * @param parent The parent node.
     */
    public IxmlGrammar(XNode parent) {
        super(parent, "ixml");
    }

    @Override
    protected XNode copy() {
        IxmlGrammar copy = new IxmlGrammar(parent);
        copy.copyChildren(getChildren());
        return copy;
    }
}
