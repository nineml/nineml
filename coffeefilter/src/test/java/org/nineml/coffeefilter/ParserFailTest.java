package org.nineml.coffeefilter;

import net.sf.saxon.s9api.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.nineml.coffeegrinder.parser.ParserType;

public class ParserFailTest {
    private static InvisibleXml invisibleXml = new InvisibleXml();

    @Test
    public void parseDate() {
        String input = "date: s?, day, -s, month, (-s, year)? .\n" +
                "-s: -\" \"+ .\n" +
                "day: digit, digit? .\n" +
                "-digit: \"0\"; \"1\"; \"2\"; \"3\"; \"4\"; \"5\"; \"6\"; \"7\"; \"8\"; \"9\".\n" +
                "month: \"January\"; \"February\"; \"March\"; \"April\";\n" +
                "       \"May\"; \"June\"; \"July\"; \"August\";\n" +
                "       \"September\"; \"October\"; \"November\"; \"December\".\n" +
                "year: ((digit, digit); -\"'\")?, digit, digit .";

        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(input);

        input = "16 Jinglebells 1992";
        InvisibleXmlDocument doc = parser.parse(input);

        Processor processor = new Processor(false);
        DocumentBuilder builder = processor.newDocumentBuilder();

        try {
            BuildingContentHandler bch = builder.newBuildingContentHandler();
            doc.getTree(bch);
            XdmNode node = bch.getDocumentNode();

            String str = node.toString();
            if (doc.getParserType() == ParserType.Earley) {
                Assertions.assertTrue(str.contains("<line>1</line>"));
                Assertions.assertTrue(str.contains("<column>4</column>"));
                Assertions.assertTrue(str.contains("<pos>4</pos>"));
                Assertions.assertTrue(str.contains("<permitted>' ', 'A', 'D', 'F', 'J', 'M', 'N', 'O', 'S'</permitted>"));
            }
            if (doc.getParserType() == ParserType.GLL) {
                Assertions.assertTrue(str.contains("<line>1</line>"));
                Assertions.assertTrue(str.contains("<column>5</column>"));
                Assertions.assertTrue(str.contains("<pos>5</pos>"));
                Assertions.assertTrue(str.contains("<unexpected>i</unexpected>"));
            }
        } catch (SaxonApiException ex) {
            System.err.println(ex.getMessage());
        }
    }

    @Test
    public void nonXmlChars() {
        String input = "date: s?, day, -s, month, (-s, year)? .\n" +
                "-s: -\" \"+ .\n" +
                "day: digit, digit? .\n" +
                "-digit: \"0\"; \"1\"; \"2\"; \"3\"; \"4\"; \"5\"; \"6\"; \"7\"; \"8\"; \"9\".\n" +
                "month: \"January\"; \"February\"; \"March\"; \"April\";\n" +
                "       \"May\"; \"June\"; \"July\"; \"August\";\n" +
                "       \"September\"; \"October\"; \"November\"; \"December\".\n" +
                "year: ((digit, digit); -\"'\")?, digit, digit .";

        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(input);

        input = "16 \u0001 1992";
        InvisibleXmlDocument doc = parser.parse(input);

        Processor processor = new Processor(false);
        DocumentBuilder builder = processor.newDocumentBuilder();

        try {
            BuildingContentHandler bch = builder.newBuildingContentHandler();
            doc.getTree(bch);
            XdmNode node = bch.getDocumentNode();

            String str = node.toString();
            if (doc.getParserType() == ParserType.Earley) {
                Assertions.assertTrue(str.contains("<line>1</line>"));
                Assertions.assertTrue(str.contains("<column>4</column>"));
                Assertions.assertTrue(str.contains("<pos>4</pos>"));
                Assertions.assertTrue(str.contains(" <unexpected codepoint=\"#0001\">U+0001</unexpected>"));
                Assertions.assertTrue(str.contains("<permitted>' ', 'A', 'D', 'F', 'J', 'M', 'N', 'O', 'S'</permitted>"));
            }
            if (doc.getParserType() == ParserType.GLL) {
                Assertions.assertTrue(str.contains("<line>1</line>"));
                Assertions.assertTrue(str.contains("<column>4</column>"));
                Assertions.assertTrue(str.contains("<pos>4</pos>"));
                Assertions.assertTrue(str.contains(" <unexpected codepoint=\"#0001\">U+0001</unexpected>"));
            }
        } catch (SaxonApiException ex) {
            System.err.println(ex.getMessage());
        }
    }

}
