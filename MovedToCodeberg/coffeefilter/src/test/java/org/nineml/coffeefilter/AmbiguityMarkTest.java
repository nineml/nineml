package org.nineml.coffeefilter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class AmbiguityMarkTest extends CoffeeFilterTest {
    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void verticalMarksOn(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);
        options.setMarkAmbiguities(true);

        String ixml = "S = A | B . A = 'a' . B = 'a' .";

        InvisibleXml invisibleXml = new InvisibleXml(options);
        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(ixml);
        InvisibleXmlDocument doc = parser.parse("a");

        String ex_a = "<S xmlns:n='https://nineml.org/ns/' xmlns:ixml='http://invisiblexml.org/NS' n:ambiguous='true' ixml:state='ambiguous'><A>a</A></S>";
        String ex_b = "<S xmlns:n='https://nineml.org/ns/' xmlns:ixml='http://invisiblexml.org/NS' n:ambiguous='true' ixml:state='ambiguous'><B>a</B></S>";

        Assertions.assertEquals(2, doc.result.getForest().getParseTreeCount());

        String a = doc.getTree();
        String b = doc.getTree();

        Assertions.assertTrue((ex_a.equals(a) && ex_b.equals(b)) || (ex_a.equals(b) && ex_b.equals(a)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void verticalMarksOff(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);
        options.setMarkAmbiguities(false);

        String ixml = "S = A | B . A = 'a' . B = 'a' .";

        InvisibleXml invisibleXml = new InvisibleXml(options);
        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(ixml);
        InvisibleXmlDocument doc = parser.parse("a");

        String ex_a = "<S xmlns:ixml='http://invisiblexml.org/NS' ixml:state='ambiguous'><A>a</A></S>";
        String ex_b = "<S xmlns:ixml='http://invisiblexml.org/NS' ixml:state='ambiguous'><B>a</B></S>";

        Assertions.assertEquals(2, doc.result.getForest().getParseTreeCount());

        String a = doc.getTree();
        String b = doc.getTree();

        Assertions.assertTrue((ex_a.equals(a) && ex_b.equals(b)) || (ex_a.equals(b) && ex_b.equals(a)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void horizontalMarksOn(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);
        options.setMarkAmbiguities(true);

        String ixml = "S = X, sep, Y .\n" +
                "-X = (x, sep)+ .\n" +
                "-Y = (y, sep)+ .\n" +
                "x = 'x' .\n" +
                "y = 'y' .\n" +
                "-sep = -',' | () .";

        InvisibleXml invisibleXml = new InvisibleXml(options);
        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(ixml);
        InvisibleXmlDocument doc = parser.parse("x,y");

        String tree = doc.getTree();
        Assertions.assertTrue(tree.contains("<?start-ambiguity "));
        Assertions.assertTrue(tree.contains("<?end-ambiguity "));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void horizontalMarksOff(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);
        options.setMarkAmbiguities(false);

        String ixml = "S = X, sep, Y .\n" +
                "-X = (x, sep)+ .\n" +
                "-Y = (y, sep)+ .\n" +
                "x = 'x' .\n" +
                "y = 'y' .\n" +
                "-sep = -',' | () .";

        InvisibleXml invisibleXml = new InvisibleXml(options);
        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(ixml);
        InvisibleXmlDocument doc = parser.parse("x,y");

        String tree = doc.getTree();
        Assertions.assertFalse(tree.contains("<?start-ambiguity "));
        Assertions.assertFalse(tree.contains("<?end-ambiguity "));
    }
}
