package org.nineml.coffeegrinder.util;

import org.nineml.coffeegrinder.exceptions.AttributeException;
import org.nineml.coffeegrinder.exceptions.GrammarException;

import java.util.*;

public abstract class Decoratable {
    // On the one hand, maintaining two maps is a waste of space.
    // On the other hand, it means we can quickly return either the original
    // attributes or a map of the attributes without allocating new objects
    // on the heap. There are usually relatively few attributes, so I'm gambling
    // that two maps is better...
    private HashMap<String, ParserAttribute> attributes = null;
    private HashMap<String, String> attrmap = null;

    /**
     * A decoratable item with no attributes.
     */
    public Decoratable() {
    }

    /**
     * A decoratable item with attributes.
     *
     * @param attributes the attributes
     * @throws GrammarException if the attribute names are not unique
     * @throws AttributeException if an attribute has an invalid value
     */
    public Decoratable(Collection<ParserAttribute> attributes) {
        addAttributes(attributes);
    }

    /**
     * Check if a specific attribute is specified.
     * @param name the name of the attribute.
     * @return true if the attribute is associated with this token.
     */
    public final boolean hasAttribute(String name) {
        return attributes != null && attributes.containsKey(name);
    }

    /**
     * Get a specific token attribute.
     * @param name the name of the attribute.
     * @return the associated attribute, or null if there is no attribute with that name.
     */
    public final ParserAttribute getAttribute(String name) {
        if (attributes != null) {
            return attributes.getOrDefault(name, null);
        }
        return null;
    }

    /**
     * Get a specific token attribute value.
     * @param name the name of the attribute.
     * @param defaultValue the default value.
     * @return the associated attribute value, or the default if there is no attribute with that name.
     */
    public final String getAttributeValue(String name, String defaultValue) {
        if (attributes != null && attributes.containsKey(name)) {
            return attributes.get(name).getValue();
        }
        return defaultValue;
    }

    /**
     * Get all the token's attributes.
     *
     * @return the attributes.
     */
    public final List<ParserAttribute> getAttributes() {
        if (attributes == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(attributes.values());
    }

    /**
     * Get all the token's attributes as a map.
     *
     * @return the associated attribute, or null if there is no attribute with that name.
     */
    public final Map<String,String> getAttributesMap() {
        if (attrmap == null) {
            return Collections.emptyMap();
        }
        return attrmap;
    }

    /**
     * Add the specified attribute to the attributes collection.
     * <p>Once added, an attribute cannot be removed, nor can its value be changed.</p>
     * @param attribute the attribute
     * @throws AttributeException if you attempt to change the value of an attribute
     * @throws AttributeException if you pass an illegal attribute
     * @throws NullPointerException if the attribute is null
     */
    public final void addAttribute(ParserAttribute attribute) {
        if (attribute == null) {
            throw new NullPointerException("Attribute must not be null");
        }
        addAttributes(Collections.singletonList(attribute));
    }

    /**
     * Add the specified attributes to the attributes collection.
     * <p>Once added, an attribute cannot be removed, nor can its value be changed.</p>
     * @param attrcoll the attributes
     * @throws AttributeException if you attempt to change the value of an attribute
     * @throws AttributeException if you pass an illegal attribute
     */
    public final void addAttributes(Collection<ParserAttribute> attrcoll) {
        if (attrcoll == null) {
            return;
        }

        if (attributes == null) {
            attributes = new HashMap<>();
            attrmap = new HashMap<>();
        }

        for (ParserAttribute attr : attrcoll) {
            if (this.attributes.containsKey(attr.getName())) {
                if (!this.attributes.get(attr.getName()).getValue().equals(attr.getValue())) {
                    throw AttributeException.immutable(attr.getName(), attr.getValue());
                }
            } else {
                this.attributes.put(attr.getName(), attr);
                this.attrmap.put(attr.getName(), attr.getValue());
            }
        }
    }
}
