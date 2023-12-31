package org.nineml.coffeefilter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.nineml.coffeefilter.exceptions.IxmlException;
import org.nineml.coffeegrinder.parser.Grammar;
import org.nineml.coffeegrinder.util.DefaultProgressMonitor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.fail;

public class IxmlParserTest {
    private static InvisibleXml invisibleXml = new InvisibleXml();

    @Test
    public void testParseIxml() {
        try {
            //invisibleXml.getOptions().getLogger().setDefaultLogLevel(Logger.DEBUG);
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/main/resources/org/nineml/coffeefilter/ixml.ixml"));
            Grammar grammar = parser.getGrammar();
            Assertions.assertNotNull(grammar);
            InvisibleXmlDocument doc = parser.parse(new File("src/main/resources/org/nineml/coffeefilter/ixml.ixml"));
            Assertions.assertTrue(doc.succeeded());
            //System.err.println(doc.getTree());
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }

    @Test
    public void testParsePragmasIxml() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/main/resources/org/nineml/coffeefilter/pragmas.ixml"));
            Grammar grammar = parser.getGrammar();
            Assertions.assertNotNull(grammar);
            InvisibleXmlDocument doc = parser.parse(new File("src/main/resources/org/nineml/coffeefilter/pragmas.ixml"));
            //doc.getResult().getForest().serialize("/tmp/out.xml");
            //System.err.println(parser.getCompiledParser());
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }

    @Test
    public void testParseExceptions() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/exceptions.ixml"));
            Grammar grammar = parser.getGrammar();
            Assertions.assertNotNull(grammar);
            //System.err.println(parser.getCompiledParser());
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }

    @Test
    public void sets() {
        String ixml = "S: NotAtoF .\n" +
                "NotAtoF: -~[#41-#46] .\n";
        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(ixml);
        InvisibleXmlDocument doc; // = invisibleXml.getParser().parse(ixml);
        //System.out.println(doc.getTree());
        String input = "m";
        doc = parser.parse(input);
        Assertions.assertTrue(doc.succeeded());
    }

    @Test
    public void ambig() {
        String ixml = "expr: e. e: e, \"+\", e; e, \"-\", e; \"i\".";
        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(ixml);
        InvisibleXmlDocument doc; // = invisibleXml.getParser().parse(ixml);
        //System.out.println(doc.getTree());
        String input = "i+i+i";
        doc = parser.parse(input);
        Assertions.assertTrue(doc.succeeded());
    }

    @Test
    public void ambig4() {
        invisibleXml.getOptions().setParserType("GLL");
        String ixml = "properties: property+.\n" +
                "property: name, \"=\", value++\",\", eol.\n" +
                "name: [L]+.\n" +
                "value: ~[\",\"]+.\n" +
                "eol: -#a | -#d, -#a .\n";
        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(ixml);
        InvisibleXmlDocument doc; //  = invisibleXml.getParser().parse(ixml);
        //System.out.println(doc.getTree());
        String input = "red=#f00\n" +
                "green=#0f0\n";
        doc = parser.parse(input);
        //System.out.println(doc.getTree());
        Assertions.assertTrue(doc.succeeded());
    }

    @Test
    public void ambig6() {
        String ixml = "block: \"{\", rule**\";\", \"}\".\n" +
                "rule: name, \"=\", value; .\n" +
                "name: [L]+.\n" +
                "value: [Nd]+.\n";
        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(ixml);
        InvisibleXmlDocument doc; // = invisibleXml.getParser().parse(ixml);
        //System.out.println(doc.getTree());
        String input = "{}";
        doc = parser.parse(input);
        Assertions.assertTrue(doc.succeeded());
    }

    @Test
    public void ambig7() {
        String ixml = "block: \"{\", rule?, \"}\".\n" +
                "rule: 'x' .\n";
        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(ixml);
        InvisibleXmlDocument doc; // = invisibleXml.getParser().parse(ixml);
        //System.out.println(doc.getTree());
        String input = "{}";
        doc = parser.parse(input);
        Assertions.assertTrue(doc.succeeded());
    }

    @Test
    public void attributeRootsPlural() {
        try {
            String ixml = "      -S: a, b, c, d.\n" +
                    "      @a: 'able'.\n" +
                    "      @b: 'baker'.\n" +
                    "      @c: 'charlie'.\n" +
                    "      d: 'dog'.\n";
            InvisibleXmlParser parser = invisibleXml.getParserFromIxml(ixml);
            Grammar grammar = parser.getGrammar();
            Assertions.assertNotNull(grammar);
            String input = "ablebakercharliedog";
            InvisibleXmlDocument doc = parser.parse(input);
            Assertions.assertTrue(doc.succeeded());
            String xml = doc.getTree();
            fail();
        } catch (Exception ex) {
            Assertions.assertTrue(ex instanceof IxmlException);
            Assertions.assertEquals("D05", ((IxmlException) ex).getCode());
        }
    }

    @Test
    public void bnf() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/bnf.ixml"));
            Grammar grammar = parser.getGrammar();
            Assertions.assertNotNull(grammar);
            InvisibleXmlDocument doc = parser.parse(new File("src/test/resources/bnf.inp"), "UTF-8");
            Assertions.assertTrue(doc.succeeded());
            Assertions.assertNotNull(doc.getTree());
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }

    @Disabled
    public void unicodeLL() {
        // Java 11 test this test wrong
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/unicode-ll.ixml"));
            Grammar grammar = parser.getGrammar();
            Assertions.assertNotNull(grammar);
            InvisibleXmlDocument doc = parser.parse(new File("src/test/resources/unicode-ll.inp"), "UTF-8");
            Assertions.assertTrue(doc.succeeded());
            Assertions.assertNotNull(doc.getTree());
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }


    // I know I fail this test :-(
    @Test
    public void imult6() {
        String ixml = "S: 'a', b, b. b: +\"xml\".";
        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(ixml);
        InvisibleXmlDocument doc; // = invisibleXml.getParser().parse(ixml);
        //System.out.println(doc.getTree());
        String input = "a";
        doc = parser.parse(input);
        String xml = doc.getTree();
        Assertions.assertTrue(doc.succeeded());
        Assertions.assertEquals("<S>a<b>xml</b><b>xml</b></S>", xml);
    }

    @Test
    public void versionDeclaration() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParserFromIxml(Files.newInputStream(Paths.get("src/test/resources/version-decl.ixml")), "UTF-8");
            Assertions.assertEquals("1.0", parser.getIxmlVersion());
            String input = "abc\uD83D\uDE3A";
            InvisibleXmlDocument doc = parser.parse(input);
            Assertions.assertTrue(doc.succeeded());
        } catch (IOException ex) {
            fail();
        }
    }

    @Test
    public void unknownVersion() {
        String grammar = "ixml version '13.3'. S='a'.";
        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(grammar);
        Assertions.assertEquals("13.3", parser.getIxmlVersion());
        String input = "a";
        InvisibleXmlDocument doc = parser.parse(input);
        String xml = doc.getTree();
        Assertions.assertTrue(xml.contains("version-mismatch"));
    }

    @Test
    public void testParseOberon() {
        try {
            invisibleXml.getOptions().setParserType("GLL");
            //invisibleXml.getOptions().getLogger().setDefaultLogLevel("debug");
            invisibleXml.getOptions().setProgressMonitor(new DefaultProgressMonitor());
            InvisibleXmlParser parser = invisibleXml.getParser(new File("ixml/samples/Oberon/Grammars/Oberon.ixml"));
            Grammar grammar = parser.getGrammar();
            Assertions.assertNotNull(grammar);
            InvisibleXmlDocument doc = parser.parse(new File("ixml/samples/Oberon/Project-Oberon-2013-materials/ORB.Mod.txt"));
            Assertions.assertTrue(doc.succeeded());
            //System.err.println(doc.parseTime());
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }

    @Test
    public void testAmbig() {
        try {
            invisibleXml.getOptions().setParserType("GLL");
            //invisibleXml.getOptions().getLogger().setDefaultLogLevel("debug");
            invisibleXml.getOptions().setProgressMonitor(new DefaultProgressMonitor());

            String input = "A: ; B. B: A.";
            input = "A: A, A; {nil} .";
            input = "S: X, X. X: 'a'; .";

            InvisibleXmlParser parser = invisibleXml.getParserFromIxml(input);
            Grammar grammar = parser.getGrammar();
            Assertions.assertNotNull(grammar);
            InvisibleXmlDocument doc = parser.parse("aa");
            //doc.getResult().getForest().serialize("/tmp/ba.xml");
            Assertions.assertTrue(doc.succeeded());
            String xml = doc.getTree();
            //System.err.println(xml);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }

    @Test
    public void testGrammar() {
        try {
            //invisibleXml.getOptions().getLogger().setDefaultLogLevel("debug");
            invisibleXml.getOptions().setProgressMonitor(new DefaultProgressMonitor());
            //invisibleXml.getOptions().setParserType("GLL");

            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/lines2.ixml"));
            Grammar grammar = parser.getGrammar();
            Assertions.assertNotNull(grammar);
            InvisibleXmlDocument doc = parser.parse(new File("src/test/resources/lines.txt"));
            Assertions.assertTrue(doc.succeeded());
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }

}
