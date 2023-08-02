package org.nineml.coffeefilter;

public class CoffeeFilterTest {
    protected final ParserOptions globalOptions;
    protected final InvisibleXml globalInvisibleXml;

    public CoffeeFilterTest() {
        globalOptions = new ParserOptions();
        globalInvisibleXml = new InvisibleXml(globalOptions);
    }
}
