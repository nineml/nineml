package org.nineml.coffeefilter;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.nineml.coffeefilter.exceptions.IxmlException;
import org.nineml.coffeegrinder.parser.Grammar;
import org.nineml.coffeegrinder.util.DefaultProgressMonitor;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

public class PragmasTest {
    private ParserOptions options;
    private InvisibleXml invisibleXml;

    @BeforeEach
    public void setup() {
        options = new ParserOptions();
        //options.getLogger().setDefaultLogLevel("debug");
        invisibleXml = new InvisibleXml(options);
    }

    @Test
    public void renameNonterminal() {
        try {
            //invisibleXml.getOptions().getLogger().setDefaultLogLevel("debug");
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/two-dates.ixml"));
            Map<String, List<String>> meta = parser.getMetadata();
            Assertions.assertEquals(meta.get("http://purl.org/dc/elements/1.1/creator").get(0), "Norman Walsh");
            InvisibleXmlDocument doc = parser.parse("1999-12-31");
            String xml = doc.getTree();
            Assertions.assertEquals("<input><year>1999</year><month>12</month><day>31</day></input>", xml);
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void renameNonterminalXml() {
        try {
            //invisibleXml.getOptions().getLogger().setDefaultLogLevel("debug");
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/two-dates.xml"));
            Map<String, List<String>> meta = parser.getMetadata();
            Assertions.assertEquals(meta.get("http://purl.org/dc/elements/1.1/creator").get(0), "Norman Walsh");
            InvisibleXmlDocument doc = parser.parse("1999-12-31");
            String xml = doc.getTree();
            Assertions.assertEquals("<input><year>1999</year><month>12</month><day>31</day></input>", xml);
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void renameTerminal() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/two-dates.ixml"));
            InvisibleXmlDocument doc = parser.parse("12 February 2022");
            String xml = doc.getTree();
            Assertions.assertEquals("<input month='Febtacular'><day>12</day><year>2022</year></input>", xml);
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void defaultNamespace() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/xmlns.ixml"));
            InvisibleXmlDocument doc = parser.parse("2022-03-01");
            String xml = doc.getTree();
            Assertions.assertEquals("<date xmlns='http://example.com/'><year>2022</year><month>03</month><day>01</day></date>", xml);
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void anotherTest() {
        try {
            // FIXME: test that the syntax error is reported
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/malformed-test.ixml"));
            InvisibleXmlDocument doc;
            doc = parser.parse("b");
            String xml = doc.getTree();
            Assertions.assertEquals("<S><A>a'c</A></S>", xml);
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void discardEmpty() {
        try {
            String grammar1 = "S = A, B, C. A = 'a'. B='b'?. C='c'.";
            String grammar2 = "{[+pragma n 'https://nineml.org/ns/pragma/']} S = A, {[n discard empty]} B, C. A = 'a'. B='b'?. C='c'.";
            String input = "ac";

            InvisibleXmlParser parser = invisibleXml.getParserFromIxml(grammar1);
            InvisibleXmlDocument doc = parser.parse(input);
            String xml = doc.getTree();
            Assertions.assertTrue(xml.contains("<B"));

            parser = invisibleXml.getParserFromIxml(grammar2);
            doc = parser.parse(input);
            xml = doc.getTree();
            Assertions.assertFalse(xml.contains("<B"));
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void discardNone() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/discard-empty.ixml"));
            InvisibleXmlDocument doc = parser.parse("abcde");
            String xml = doc.getTree();
            Assertions.assertEquals("<S D='d' E='e'><A>a</A><B>b</B><C>c</C></S>", xml);
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void discardA() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/discard-empty.ixml"));
            InvisibleXmlDocument doc = parser.parse("cde");
            String xml = doc.getTree();
            Assertions.assertEquals("<S D='d' E='e'><B/><C>c</C></S>", xml);
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void discardACD() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/discard-empty.ixml"));
            InvisibleXmlDocument doc = parser.parse("");
            String xml = doc.getTree();
            Assertions.assertEquals("<S E=''><B/></S>", xml);
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void ignoreDiscardACD() {
        try {
            ParserOptions localOptions = new ParserOptions();
            localOptions.disablePragma("discard-empty");
            InvisibleXml localInvisibleXml = new InvisibleXml(localOptions);

            InvisibleXmlParser parser = localInvisibleXml.getParser(new File("src/test/resources/discard-empty.ixml"));
            InvisibleXmlDocument doc = parser.parse("");
            String xml = doc.getTree();
            Assertions.assertEquals("<S D='' E=''><A/><B/><C/></S>", xml);
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void ignoreAllDiscardACD() {
        try {
            ParserOptions localOptions = new ParserOptions();
            localOptions.disablePragma("#all");
            InvisibleXml localInvisibleXml = new InvisibleXml(localOptions);

            InvisibleXmlParser parser = localInvisibleXml.getParser(new File("src/test/resources/discard-empty.ixml"));
            InvisibleXmlDocument doc = parser.parse("");
            String xml = doc.getTree();
            Assertions.assertEquals("<S D='' E=''><A/><B/><C/></S>", xml);
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void ignoreAllExceptDiscardACD() {
        try {
            ParserOptions localOptions = new ParserOptions();
            localOptions.disablePragma("#all");
            localOptions.enablePragma("discard-empty");
            InvisibleXml localInvisibleXml = new InvisibleXml(localOptions);

            InvisibleXmlParser parser = localInvisibleXml.getParser(new File("src/test/resources/discard-empty.ixml"));
            InvisibleXmlDocument doc = parser.parse("");
            String xml = doc.getTree();
            Assertions.assertEquals("<S E=''><B/></S>", xml);
        } catch (Exception ex) {
            fail();
        }
    }

    @Disabled
    public void greedy() {
        try {
            //invisibleXml.getOptions().getLogger().setDefaultLogLevel("debug");
            invisibleXml.getOptions().setParserType("GLL");
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/greedy.ixml"));
            InvisibleXmlDocument doc = parser.parse("abbbc");
            String xml = doc.getTree();
            Assertions.assertEquals("<S><A>a</A><B>bbb</B><C>c</C></S>", xml);
        } catch (Exception ex) {
            fail();
        }
    }

    /*
    @Test
    public void unicodeData() {
        try {
            ParserOptions options = new ParserOptions();
            options.setPrettyPrint(true);
            invisibleXml = new InvisibleXml(options);
            InvisibleXmlParser parser = invisibleXml.getParser(new File("../pot/scraps/chars/unicode.ixml"));
            InvisibleXmlDocument doc = parser.parse(new File("../pot/scraps/chars/MediumData.txt"));
            //String xml = doc.getTree();
            System.out.println(doc.succeeded());
        } catch (Exception ex) {
            fail();
        }
    }
     */

    @Test
    public void testRegex() {
        try {
            //invisibleXml.getOptions().getLogger().setDefaultLogLevel("trace");
            invisibleXml.getOptions().setProgressMonitor(new DefaultProgressMonitor());

            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/regex.ixml"));
            Grammar grammar = parser.getGrammar();
            Assertions.assertNotNull(grammar);
            InvisibleXmlDocument doc = parser.parse("abzcdz");
            Assertions.assertTrue(doc.succeeded());
            //System.err.println(doc.getTree());
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }


    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void testLines(String parserType) {
        ParserOptions options = new ParserOptions();
        options.setParserType(parserType);

        try {
            InvisibleXml ixml = new InvisibleXml(options);
            InvisibleXmlParser parser = ixml.getParser(new File("src/test/resources/lines1.ixml"));
            InvisibleXmlDocument doc = parser.parse(new File("src/test/resources/lines.txt"));
            Assertions.assertTrue(doc.succeeded());
            String slow = doc.getTree();
            long slowms = doc.parseTime();

            parser = ixml.getParser(new File("src/test/resources/lines2.ixml"));
            doc = parser.parse(new File("src/test/resources/lines.txt"));
            Assertions.assertTrue(doc.succeeded());
            String fast = doc.getTree();
            long fastms = doc.parseTime();

            Assertions.assertTrue(fastms < slowms);
            Assertions.assertEquals(slow, fast);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }
}
