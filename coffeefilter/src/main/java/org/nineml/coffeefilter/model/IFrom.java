package org.nineml.coffeefilter.model;

public class IFrom extends XNonterminal {
    public final String href;

    /**
     * Create an IUses.
     *
     * @param parent The parent node.
     */
    public IFrom(XNode parent, String href) {
        super(parent, "from");
        this.href = href;
    }

    @Override
    protected XNode copy() {
        return null;
    }
}
