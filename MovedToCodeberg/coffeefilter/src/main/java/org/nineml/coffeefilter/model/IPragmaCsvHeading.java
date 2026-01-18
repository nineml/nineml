package org.nineml.coffeefilter.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IPragmaCsvHeading extends IPragma {
    public final String heading;

    public IPragmaCsvHeading(XNode parent, String name, String data) {
        super(parent, name);
        ptype = PragmaType.CSV_HEADING;
        inherit = false;

        if (data == null) {
            heading = null;
            parent.getRoot().getOptions().getLogger().error(XNode.logcategory, "No heading specified for csv-heading");
        } else {
            data = data.trim();
            if ((data.startsWith("\"") && data.endsWith("\"")
                    || (data.startsWith("'") && data.endsWith("'")))) {
                heading = data.substring(1, data.length()-1);
            } else {
                heading = null;
                parent.getRoot().getOptions().getLogger().error(XNode.logcategory, "Failed to parse csv-heading: %s", data);
            }
        }
    }
}
