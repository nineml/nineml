package org.nineml.coffeefilter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.fail;

public class CommentTest {
    private InvisibleXml invisibleXml;
    private ParserOptions options;

    @BeforeEach
    public void setup() {
        options = new ParserOptions();
        options.setPedantic(true);
        invisibleXml = new InvisibleXml(options);
    }

    @Test
    public void comments() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/comments.ixml"));
            String input = "hello";
            InvisibleXmlDocument doc = parser.parse(input);
            Assertions.assertTrue(doc.getNumberOfParses() > 0);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }

    @Disabled
    public void longComments1() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/long-comments1.ixml"));
            System.err.println(parser.getParseTime());
            String input = "ab";
            InvisibleXmlDocument doc = parser.parse(input);
            Assertions.assertTrue(doc.getNumberOfParses() > 0);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }

    @Disabled
    public void longComments2() {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/long-comments2.ixml"));
            System.err.println(parser.getParseTime());
            String input = "ab";
            InvisibleXmlDocument doc = parser.parse(input);
            Assertions.assertTrue(doc.getNumberOfParses() > 0);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }
}
