package org.nineml.coffeefilter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.nineml.coffeefilter.exceptions.IxmlException;

import static org.junit.jupiter.api.Assertions.fail;

public class StartSymbolTest {
    @Test
    public void testStartSymbolUnspecified() {
        ParserOptions options = new ParserOptions();
        InvisibleXml invisibleXml = new InvisibleXml(options);
        InvisibleXmlParser parser = invisibleXml.getParserFromIxml("S = 'a' | B. B = 'a'.");
        InvisibleXmlDocument doc = parser.parse("a");
        String xml = doc.getTree();
        Assertions.assertTrue(xml.startsWith("<S "));
        Assertions.assertTrue(xml.contains("ambiguous"));
    }

    @Test
    public void testStartSymbolSpecified() {
        ParserOptions options = new ParserOptions();
        options.setStartSymbol("B");
        InvisibleXml invisibleXml = new InvisibleXml(options);
        InvisibleXmlParser parser = invisibleXml.getParserFromIxml("S = 'a' | B. B = 'a'.");
        InvisibleXmlDocument doc = parser.parse("a");
        String xml = doc.getTree();
        Assertions.assertEquals("<B>a</B>", xml);
    }

    @Test
    public void testNoSuchStartSymbol() {
        ParserOptions options = new ParserOptions();
        options.setStartSymbol("X");
        InvisibleXml invisibleXml = new InvisibleXml(options);
        InvisibleXmlParser parser = invisibleXml.getParserFromIxml("S = 'a' | B. B = 'a'.");
        Assertions.assertFalse(parser.constructed());
        Assertions.assertNotNull(parser.getException());
        Assertions.assertEquals("E017", ((IxmlException) parser.getException()).getCode());
    }
}
