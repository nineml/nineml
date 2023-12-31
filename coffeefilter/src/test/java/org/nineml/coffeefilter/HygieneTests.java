package org.nineml.coffeefilter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.nineml.coffeefilter.exceptions.IxmlException;

import static org.junit.jupiter.api.Assertions.fail;

public class HygieneTests {

    @Test
    public void unreachableForbidden() {
        // This test is for the bug where a terminal marked as optional was losing its optionality
        String input = "S: 'a'. B: 'b'.";

        ParserOptions options = new ParserOptions();
        options.setAllowUnreachableSymbols(false);
        InvisibleXml invisibleXml = new InvisibleXml(options);

        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(input);
        Assertions.assertFalse(parser.constructed());
    }

    @Test
    public void unreachableOk() {
        // This test is for the bug where a terminal marked as optional was losing its optionality
        String input = "S: 'a'. B: 'b'.";

        ParserOptions options = new ParserOptions();
        options.setAllowUnreachableSymbols(true);
        InvisibleXml invisibleXml = new InvisibleXml(options);

        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(input);

        input = "a";
        InvisibleXmlDocument doc = parser.parse(input);

        Assertions.assertTrue(doc.succeeded());
    }

    @Test
    public void multipleForbidden() {
        // This test is for the bug where a terminal marked as optional was losing its optionality
        String input = "S: 'a'. S: 'b'.";

        ParserOptions options = new ParserOptions();
        options.setAllowMultipleDefinitions(false);
        InvisibleXml invisibleXml = new InvisibleXml(options);

        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(input);
        Assertions.assertFalse(parser.constructed());
    }

    @Test
    public void multipleOk() {
        // This test is for the bug where a terminal marked as optional was losing its optionality
        String input = "S: 'a'. S: 'b'.";

        ParserOptions options = new ParserOptions();
        options.setAllowMultipleDefinitions(true);
        InvisibleXml invisibleXml = new InvisibleXml(options);

        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(input);

        input = "b";
        InvisibleXmlDocument doc = parser.parse(input);

        Assertions.assertTrue(doc.succeeded());
    }

    @Test
    public void undefinedOk() {
        // This test is for the bug where a terminal marked as optional was losing its optionality
        String input = "S = A; B; '(', S, ')'.\n" +
                "A = 'a'; X, A.\n" +
                "B = 'b'; B, X*.\n";

        ParserOptions options = new ParserOptions();
        options.setAllowUndefinedSymbols(true);
        InvisibleXml invisibleXml = new InvisibleXml(options);

        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(input);

        input = "b";
        try {
            InvisibleXmlDocument doc = parser.parse(input);
            Assertions.assertTrue(doc.succeeded());
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void undefined_1_4() {
        String input = "S = A; B; '(', S, ')'.\n" +
                "A = 'a'; X, A.\n" +
                "B = 'b'; B, X*.\n";

        ParserOptions options = new ParserOptions();
        InvisibleXml invisibleXml = new InvisibleXml(options);

        try {
            InvisibleXmlParser parser = invisibleXml.getParserFromIxml(input);
            if (parser.getException() != null) {
                throw parser.getException();
            }
            fail();
        } catch (Exception ex) {
            Assertions.assertTrue(ex instanceof IxmlException);
            Assertions.assertEquals("S02", ((IxmlException) ex).getCode());
        }
    }

}
