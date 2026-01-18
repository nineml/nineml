package org.nineml.coffeefilter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.fail;

public class GllTest {

    @Test
    public void suppressSeparator() {
        ParserOptions options = new ParserOptions();
        options.setParserType("GLL");
        InvisibleXml ixml = new InvisibleXml(options);

        try {
            InvisibleXmlParser parser = ixml.getParser(new File("src/test/resources/list2.ixml"));
            InvisibleXmlDocument document = parser.parse("a,1");
            //document.getResult().getForest().serialize("list2.xml");
            String xml = document.getTree();
            Assertions.assertEquals("<list xmlns:ixml='http://invisiblexml.org/NS' ixml:state='ambiguous'><list-of-letter><letter>a</letter></list-of-letter><list-of-number><number>1</number></list-of-number></list>", xml);
        } catch (IOException err) {
            fail();
        }
    }

}
