package org.nineml.examples;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.nineml.coffeefilter.InvisibleXml;
import org.nineml.coffeefilter.InvisibleXmlDocument;
import org.nineml.coffeefilter.InvisibleXmlParser;
import org.nineml.coffeefilter.ParserOptions;
import org.nineml.coffeegrinder.parser.ParseForest;
import org.nineml.coffeegrinder.trees.Arborist;
import org.nineml.coffeegrinder.trees.StringTreeBuilder;

import java.io.File;
import java.io.IOException;

public class CoffeeFilterExamples {
    public String simpleParseNumber(String number) {
        try {
            ParserOptions options = new ParserOptions();
            InvisibleXml invisibleXml = new InvisibleXml(options);

            File grammar = new File("src/test/resources/numbers.ixml");
            InvisibleXmlParser parser = invisibleXml.getParser(grammar);
            InvisibleXmlDocument document = parser.parse(number);

            if (document.succeeded()) {
                String tree = document.getTree();
                return String.format("%s is a number: %s", number, tree);
            } else {
                return String.format("%s is not a number", number);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String parseNumber(String number) {
        try {
            ParserOptions options = new ParserOptions();
            InvisibleXml invisibleXml = new InvisibleXml(options);

            File grammar = new File("src/test/resources/numbers.ixml");
            InvisibleXmlParser parser = invisibleXml.getParser(grammar);
            InvisibleXmlDocument document = parser.parse(number);

            if (document.succeeded()) {
                StringTreeBuilder builder = new StringTreeBuilder();
                ParseForest forest = document.getResult().getForest();
                Arborist.getArborist(forest).getTree(builder);
                String tree = builder.getTree();
                return String.format("%s is a number: %s", number, tree);
            } else {
                return String.format("%s is not a number", number);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void parseInteger() {
        String result = parseNumber("42");
        System.out.println(result);
        Assertions.assertTrue(result.startsWith("42 is a number"));
    }

    @Test
    public void parseFloat() {
        String result = parseNumber("3.14");
        System.out.println(result);
        Assertions.assertTrue(result.startsWith("3.14 is a number"));
    }

    @Test
    public void parseScientific() {
        String result = parseNumber("1.0E6");
        System.out.println(result);
        Assertions.assertTrue(result.startsWith("1.0E6 is a number"));
    }

    @Test
    public void parse1dot2dot3() {
        String result = parseNumber("1.2.3");
        System.out.println(result);
        Assertions.assertTrue(result.startsWith("1.2.3 is not a number"));
    }

    public String parseDate(String date) {
        try {
            ParserOptions options = new ParserOptions();
            InvisibleXml invisibleXml = new InvisibleXml(options);

            File grammar = new File("src/test/resources/useudates.ixml");
            InvisibleXmlParser parser = invisibleXml.getParser(grammar);
            InvisibleXmlDocument document = parser.parse(date);

            if (document.succeeded()) {
                StringTreeBuilder builder = new StringTreeBuilder();
                ParseForest forest = document.getResult().getForest();
                Arborist.getArborist(forest).getTree(builder);
                String tree = builder.getTree();

                long parseCount = document.getNumberOfParses();
                if (parseCount > 1) {
                    if (document.isInfinitelyAmbiguous()) {
                        return String.format("%s is a date (in infinite ways): %s", date, tree);
                    }
                    return String.format("%s is a date (%d ways): %s", date, parseCount, tree);
                }
                return String.format("%s is a date: %s", date, tree);
            } else {
                return String.format("%s is not a number", date);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void parseUsDate() {
        String result = parseDate("01/19/2038");
        System.out.println(result);
        Assertions.assertTrue(result.startsWith("01/19/2038 is a date"));
    }

    @Test
    public void parseEuDate() {
        String result = parseDate("19/01/2038");
        System.out.println(result);
        Assertions.assertTrue(result.startsWith("19/01/2038 is a date"));
    }

    @Test
    public void parseAmbiguousDate() {
        String result = parseDate("08/01/2038");
        System.out.println(result);
        Assertions.assertTrue(result.startsWith("08/01/2038 is a date (2 ways)"));
    }
}
