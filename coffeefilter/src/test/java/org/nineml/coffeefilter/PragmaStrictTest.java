package org.nineml.coffeefilter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.nineml.coffeefilter.exceptions.IxmlException;
import org.nineml.coffeegrinder.parser.Grammar;
import org.nineml.coffeegrinder.util.DefaultProgressMonitor;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

public class PragmaStrictTest {
    @Test
    public void testStrictEmptyAltFail() {
        ParserOptions options = new ParserOptions();
        try {
            InvisibleXml ixml = new InvisibleXml(options);
            InvisibleXmlParser parser = ixml.getParser(new File("src/test/resources/strict-empty-alt-fail.ixml"));
            Assertions.assertFalse(parser.constructed());
            Assertions.assertNotNull(parser.getException());
            Assertions.assertTrue(parser.getException() instanceof IxmlException);
            IxmlException ixmlex = (IxmlException) parser.getException();
            Assertions.assertEquals(ixmlex.getCode(), "I004");
            Assertions.assertTrue(ixmlex.getMessage().endsWith("A."));
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }

    @Test
    public void testStrictEmptyAltPass() {
        ParserOptions options = new ParserOptions();
        try {
            InvisibleXml ixml = new InvisibleXml(options);
            InvisibleXmlParser parser = ixml.getParser(new File("src/test/resources/strict-empty-alt-pass.ixml"));
            Assertions.assertTrue(parser.constructed());
            InvisibleXmlDocument document = parser.parse("ab");
            Assertions.assertTrue(document.succeeded());
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }

    @Test
    public void testStrictMultipleDefinitionsFail() {
        ParserOptions options = new ParserOptions();
        try {
            InvisibleXml ixml = new InvisibleXml(options);
            InvisibleXmlParser parser = ixml.getParser(new File("src/test/resources/strict-multiple-defs-fail.ixml"));
            Assertions.assertFalse(parser.constructed());
            Assertions.assertNotNull(parser.getException());
            Assertions.assertTrue(parser.getException() instanceof IxmlException);
            IxmlException ixmlex = (IxmlException) parser.getException();
            Assertions.assertEquals(ixmlex.getCode(), "S03");
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }

    @Test
    public void testStrictMultipleDefinitionsPass() {
        ParserOptions options = new ParserOptions();
        try {
            InvisibleXml ixml = new InvisibleXml(options);
            InvisibleXmlParser parser = ixml.getParser(new File("src/test/resources/strict-multiple-defs-pass.ixml"));
            Assertions.assertTrue(parser.constructed());
            InvisibleXmlDocument document = parser.parse("ab");
            Assertions.assertTrue(document.succeeded());
            document = parser.parse("1b");
            Assertions.assertTrue(document.succeeded());
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }

    @Test
    public void testStrictUndefinedFail() {
        ParserOptions options = new ParserOptions();
        try {
            InvisibleXml ixml = new InvisibleXml(options);
            InvisibleXmlParser parser = ixml.getParser(new File("src/test/resources/strict-undefined-fail.ixml"));
            Assertions.assertFalse(parser.constructed());
            Assertions.assertNotNull(parser.getException());
            Assertions.assertTrue(parser.getException() instanceof IxmlException);
            IxmlException ixmlex = (IxmlException) parser.getException();
            Assertions.assertEquals(ixmlex.getCode(), "S02");
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }

    @Test
    public void testStrictUndefinedPass() {
        ParserOptions options = new ParserOptions();
        try {
            InvisibleXml ixml = new InvisibleXml(options);
            InvisibleXmlParser parser = ixml.getParser(new File("src/test/resources/strict-undefined-pass.ixml"));
            Assertions.assertTrue(parser.constructed());
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }

    @Test
    public void testStrictUnreachableFail() {
        ParserOptions options = new ParserOptions();
        try {
            InvisibleXml ixml = new InvisibleXml(options);
            InvisibleXmlParser parser = ixml.getParser(new File("src/test/resources/strict-unreachable-fail.ixml"));
            Assertions.assertFalse(parser.constructed());
            Assertions.assertNotNull(parser.getException());
            Assertions.assertTrue(parser.getException() instanceof IxmlException);
            IxmlException ixmlex = (IxmlException) parser.getException();
            Assertions.assertEquals(ixmlex.getCode(), "E012");
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }

    @Test
    public void testStrictUnreachablePass() {
        ParserOptions options = new ParserOptions();
        try {
            InvisibleXml ixml = new InvisibleXml(options);
            InvisibleXmlParser parser = ixml.getParser(new File("src/test/resources/strict-unreachable-pass.ixml"));
            Assertions.assertTrue(parser.constructed());
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }

    @Test
    public void testStrictUnproductiveFail() {
        ParserOptions options = new ParserOptions();
        try {
            InvisibleXml ixml = new InvisibleXml(options);
            InvisibleXmlParser parser = ixml.getParser(new File("src/test/resources/strict-unproductive-fail.ixml"));
            Assertions.assertFalse(parser.constructed());
            Assertions.assertNotNull(parser.getException());
            Assertions.assertTrue(parser.getException() instanceof IxmlException);
            IxmlException ixmlex = (IxmlException) parser.getException();
            Assertions.assertEquals(ixmlex.getCode(), "E013");
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }

    @Test
    public void testStrictUnproductivePass() {
        ParserOptions options = new ParserOptions();
        try {
            InvisibleXml ixml = new InvisibleXml(options);
            InvisibleXmlParser parser = ixml.getParser(new File("src/test/resources/strict-unproductive-pass.ixml"));
            Assertions.assertTrue(parser.constructed());
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }

}
