package org.nineml.coffeefilter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.fail;

public class AmbiguityTest {
    private InvisibleXml invisibleXml;
    private ParserOptions options;

    @BeforeEach
    public void setup() {
        options = new ParserOptions();
        options.setPedantic(false);
        invisibleXml = new InvisibleXml(options);
    }

    @Test
    public void ambiguity1() {
        // This test is for the bug where a terminal marked as optional was losing its optionality
        String input = "{[+pragma n \"https://nineml.org/ns/pragma/\"]} S = 'x', (A | {[n priority 2]} B), 'y'.  {[n priority 1]} A = 'a' | B. B = 'b' | A.";

        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(input);
        input = "xay";
        InvisibleXmlDocument doc = parser.parse(input);

        try {
            String xml = doc.getTree();
            Assertions.assertTrue(xml.contains("x<B><A>a</A></B>y"));
            Assertions.assertTrue(xml.contains("ixml:state"));
            Assertions.assertTrue(xml.contains("ambiguous"));
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void ambiguity2() {
        // This test is for the case where no ambiguous choices are made (taking priority into account)
        String input = "{[+pragma n \"https://nineml.org/ns/pragma/\"]} S = 'x', (A | {[n priority 2]} B), 'y'. -X = 'a' . {[n priority 1]} A = {[n priority 1]} X | B. B = 'b' | A.";

        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(input);
        input = "xay";
        InvisibleXmlDocument doc = parser.parse(input);

        try {
            String xml = doc.getTree();
            Assertions.assertTrue(xml.contains("x<B><A>a</A></B>y"));
            Assertions.assertFalse(xml.contains("ixml:state"));
            Assertions.assertFalse(xml.contains("ambiguous"));
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void ambigmarks() {
        String input = "S = A, B, C | A, @B, C . A = 'a' . B = 'b' . C = 'c' .";

        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(input);
        input = "abc";
        InvisibleXmlDocument doc = parser.parse(input);

        try {
            String xml = doc.getTree();
            Assertions.assertTrue(xml.contains("<A>a</A><B>b</B><C>c</C>") || xml.contains("a</A><C>c</C>"));
        } catch (Exception ex) {
            fail();
        }

    }

    @Test
    public void horiz1() {
        try {
            FileInputStream fis = new FileInputStream(new File("src/test/resources/horiz1.ixml"));
            InvisibleXmlParser parser = invisibleXml.getParserFromIxml(fis, "utf-8");
            String input = "xay";
            InvisibleXmlDocument doc = parser.parse(input);
            String xml = doc.getTree();
            Assertions.assertTrue(xml.contains("<A>a</A>") || xml.contains("<B>ay</B>"));
        } catch (Exception ex) {
            fail();
        }

    }

    @Test
    public void aplus() {
        String input = "S = s, alts. alts = alt++(';', s). alt = term**(',', s). term = ('a' ; 'b'), s. -s = (-' '|comment)*. comment = -'{', ~[{}]*, -'}'.";


        InvisibleXmlParser parser = invisibleXml.getParserFromIxml(input);
        input = " a;{comment} b";

        InvisibleXmlDocument doc = parser.parse(input);

        try {
            String xml = doc.getTree();
            //System.out.println(xml);
        } catch (Exception ex) {
            fail();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void phone2(String parserType) {
        try {
            ParserOptions opts = new ParserOptions(options);
            opts.setParserType(parserType);
            InvisibleXml ixml = new InvisibleXml(opts);
            InvisibleXmlParser parser = ixml.getParser(new File("src/test/resources/phone2.ixml"));
            InvisibleXmlDocument doc = parser.parse("123-4567");
            String xml = doc.getTree();

            // <prefix> must occur exactly once
            int pos = xml.indexOf("<prefix");
            Assertions.assertTrue(pos > 0);
            xml = xml.substring(pos+6);
            pos = xml.indexOf("<prefix");
            Assertions.assertEquals(-1, pos);
        } catch (IOException ex) {
            fail();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void phone7_1(String parserType) {
        try {
            ParserOptions opts = new ParserOptions(options);
            opts.setParserType(parserType);
            //opts.getLogger().setDefaultLogLevel("trace");
            InvisibleXml ixml = new InvisibleXml(opts);
            InvisibleXmlParser parser = ixml.getParser(new File("src/test/resources/seven.ixml"));
            InvisibleXmlDocument doc = parser.parse("7");
            String xml = doc.getTree();
            Assertions.assertEquals("<phone-number><cc>+1</cc>7</phone-number>", xml);
            System.err.println(xml);
        } catch (IOException ex) {
            fail();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void phone7_2(String parserType) {
        try {
            ParserOptions opts = new ParserOptions(options);
            opts.setParserType(parserType);
            //opts.getLogger().setDefaultLogLevel("trace");
            InvisibleXml ixml = new InvisibleXml(opts);
            InvisibleXmlParser parser = ixml.getParser(new File("src/test/resources/seven.ixml"));
            InvisibleXmlDocument doc = parser.parse("+1-7");
            String xml = doc.getTree();
            Assertions.assertEquals("<phone-number><cc>+1</cc>7</phone-number>", xml);
            System.err.println(xml);
        } catch (IOException ex) {
            fail();
        }
    }
}
