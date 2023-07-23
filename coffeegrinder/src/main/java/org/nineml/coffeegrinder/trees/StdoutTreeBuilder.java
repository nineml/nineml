package org.nineml.coffeegrinder.trees;

/**
 * A tree builder that sends serialized output to {@link System#out}.
 */
public class StdoutTreeBuilder extends PrintStreamTreeBuilder {
    public StdoutTreeBuilder() {
        super(System.out);
    }
}
