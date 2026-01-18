package org.nineml.coffeefilter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.nineml.coffeefilter.trees.CsvColumn;
import org.nineml.coffeefilter.trees.DataTree;

import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

public class CsvTest extends CommonBuilder {
    @Test
    public void csvSerializer() {
        try {
            DataTree tree = buildRecordDataTree(new ParserOptions());

            List<CsvColumn> columns = tree.prepareCsv();
            String csv = tree.asCSV(columns);

            Assertions.assertEquals("\"name\",\"age\",\"height\",\"bool\"\n" +
                    "\"John Doe\",25,1.7,true\n" +
                    "\"Mary Smith\",22,,false\n" +
                    "\"Jane Doe\",33,1.4,true\n", csv);
        } catch (Exception ex) {
            fail();
        }
    }
}
