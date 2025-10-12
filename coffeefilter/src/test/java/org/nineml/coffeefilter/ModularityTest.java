package org.nineml.coffeefilter;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.nineml.coffeegrinder.parser.Grammar;
import org.nineml.coffeegrinder.util.DefaultProgressMonitor;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

public class ModularityTest {
    private ParserOptions options;
    private InvisibleXml invisibleXml;

    @BeforeEach
    public void setup() {
        options = new ParserOptions();
        options.setModularity(true);
        invisibleXml = new InvisibleXml(options);
    }

    @Test
    public void notModular() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/notmodular.ixml"));
            InvisibleXmlDocument doc = parser.parse("123");
            String xml = doc.getTree();
            Assertions.assertEquals("<partno>123</partno>", xml);
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void parts() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/parts.ixml"));
            InvisibleXmlDocument doc = parser.parse("123");
            String xml = doc.getTree();
            Assertions.assertEquals("<partno>123</partno>", xml);
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void partIxmlModule() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser();
            InvisibleXmlDocument doc = parser.parse(new File("src/test/resources/date.ixml"));
            String xml = doc.getTree();
            Assertions.assertTrue(xml.startsWith("<module>"));
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void partIxml() {
        try {
            ParserOptions xoptions = new ParserOptions();
            xoptions.setModularity(false);
            InvisibleXml xinvisibleXml = new InvisibleXml(xoptions);

            InvisibleXmlParser parser = xinvisibleXml.getParser();
            InvisibleXmlDocument doc = parser.parse(new File("src/test/resources/date.ixml"));
            String xml = doc.getTree();
            Assertions.assertTrue(xml.startsWith("<ixml>"));
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void mutuallyrecursive() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/mr1.ixml"));
            InvisibleXmlDocument doc = parser.parse("proceduredeclaration");
            String xml = doc.getTree();
            Assertions.assertEquals("<procedure>procedure<declaration>declaration</declaration></procedure>", xml);
        } catch (Exception ex) {
            fail();
        }
    }

}
