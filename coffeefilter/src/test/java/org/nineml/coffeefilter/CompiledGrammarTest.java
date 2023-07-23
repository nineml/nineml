package org.nineml.coffeefilter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.fail;

public class CompiledGrammarTest {
    private static final InvisibleXml invisibleXml = new InvisibleXml();

    @Test
    public void parseIxmlGrammar() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/date.ixml"));
            //System.err.println(parser.getCompiledParser());
            InvisibleXmlDocument doc = parser.parse("1 January 2022");
            Assertions.assertEquals("<date><day>1</day><month>January</month><year>2022</year></date>", doc.getTree());
        } catch (Exception ex) {
            fail();
        }
    }
}
