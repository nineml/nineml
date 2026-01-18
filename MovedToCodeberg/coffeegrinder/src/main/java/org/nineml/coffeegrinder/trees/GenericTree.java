package org.nineml.coffeegrinder.trees;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A generic tree representation of a parse.
 */
public abstract class GenericTree {
    public final Map<String,String> attributes;
    public final int leftExtent;
    public final int rightExtent;
    protected GenericBranch parent;

    /* package */ GenericTree(Map<String,String> attributes, int leftExtent, int rightExtent) {
        this.attributes = Collections.unmodifiableMap(attributes);
        this.leftExtent = leftExtent;
        this.rightExtent = rightExtent;
        parent = null;
    }

    public GenericBranch getParent() {
        return parent;
    }

    public List<GenericTree> getChildren() {
        return Collections.emptyList();
    }

    public String getAttribute(String name, String defaultValue) {
        return attributes == null ? defaultValue : attributes.getOrDefault(name, defaultValue);
    }

    public Map<String,String> getAttributes() {
        return attributes;
    }
}
