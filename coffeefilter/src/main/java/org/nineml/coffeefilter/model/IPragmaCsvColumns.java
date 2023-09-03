package org.nineml.coffeefilter.model;

import java.util.*;

public class IPragmaCsvColumns extends IPragma {
    public final List<String> columns;

    public IPragmaCsvColumns(XNode parent, String name, String data) {
        super(parent, name);
        ptype = PragmaType.CSV_COLUMNS;
        inherit = false;

        ArrayList<String> colnames = new ArrayList<>();
        if (data != null) {
            colnames.addAll(Arrays.asList(data.split(",\\s*")));
        }
        columns = colnames;

        if (columns.isEmpty()) {
            parent.getRoot().getOptions().getLogger().error(XNode.logcategory, "No columns specified for csv-columns");
        }
    }
}
