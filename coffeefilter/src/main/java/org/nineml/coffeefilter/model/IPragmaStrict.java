package org.nineml.coffeefilter.model;

import java.util.*;

public class IPragmaStrict extends IPragma {
    public static final Set<String> constraints;
    static {
        constraints = new HashSet<>();
        constraints.add("multiple-definitions");
        constraints.add("undefined");
        constraints.add("unreachable");
        constraints.add("unproductive");
        constraints.add("empty-alt");
    }

    public final Set<String> flags;

    public IPragmaStrict(XNode parent, String data) {
        super(parent, "strict");
        ptype = PragmaType.STRICT;
        inherit = false;

        HashSet<String> allowed = new HashSet<>();

        for (String flag : data.split("\\s+")) {
            if (flag.startsWith("allow-")) {
                flag = flag.substring(6);
                if (constraints.contains(flag)) {
                    allowed.add(flag);
                } else {
                    parent.getRoot().getOptions().getLogger().error(XNode.logcategory, "Unknown strict constraint: %s", flag);
                }
            } else {
                parent.getRoot().getOptions().getLogger().error(XNode.logcategory, "Unknown strict option: %s", flag);
            }
        }

        HashSet<String> forbiddenFlags = new HashSet<>();
        for (String flag : constraints) {
            if (!allowed.contains(flag)) {
                forbiddenFlags.add(flag);
            }
        }

        flags = Collections.unmodifiableSet(forbiddenFlags);
    }

    public boolean allowMultipleDefinitions() {
        return !flags.contains("multiple-definitions");
    }

    public boolean allowUndefined() {
        return !flags.contains("undefined");
    }

    public boolean allowUnreachable() {
        return !flags.contains("unreachable");
    }

    public boolean allowUnproductive() {
        return !flags.contains("unproductive");
    }

    public boolean allowEmptyAlt() {
        return !flags.contains("empty-alt");
    }

}
