package org.nineml.coffeefilter;

import net.sf.saxon.s9api.*;
import net.sf.saxon.trans.XPathException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.nineml.coffeefilter.exceptions.IxmlException;
import org.xml.sax.InputSource;

import javax.xml.transform.sax.SAXSource;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.fail;

public class ModularityTest {
    private ParserOptions options;
    private InvisibleXml invisibleXml;
    private XsltTransformer transpiler = null;

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
            Assertions.assertTrue(xml.startsWith("<ixml>"));
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
    public void modular() {
        try {
            InvisibleXmlParser parser = loadModularGrammarTest("modular");
            InvisibleXmlDocument doc = parser.parse("123");
            String xml = doc.getTree();
            Assertions.assertEquals("<partno>123</partno>", xml);
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void overrides() {
        try {
            InvisibleXmlParser parser = loadModularGrammarTest("override");
            InvisibleXmlDocument doc = parser.parse("123ABC");
            String xml = doc.getTree();
            Assertions.assertEquals("<partno>123ABC</partno>", xml);
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void invalidModularity() {
        try {
            InvisibleXmlParser parser = loadModularGrammarTest("moderror");
            Assertions.assertFalse(parser.constructed());

            Exception ex = parser.getException();
            if (ex instanceof IxmlException) {
                IxmlException ie = (IxmlException) ex;
                Assertions.assertEquals("E022", ie.getCode());
            } else {
                fail(ex.getMessage());
            }
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void okayModularity() {
        try {
            InvisibleXmlParser parser = loadModularGrammarTest("modokay");
            InvisibleXmlDocument doc = parser.parse("123");
            String xml = doc.getTree();
            System.err.println("OUTPUT: " + xml);
            Assertions.assertTrue(xml.contains(">123</partno>"));
            Assertions.assertTrue(xml.contains("ambiguous"));
        } catch (Exception ex) {
            System.err.println("ERROR: " + ex.getMessage());
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

    private InvisibleXmlParser loadModularGrammarTest(String basename) {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/" + basename + ".ixml"));
            if (!parser.constructed()) {
                return parser;
            }

            if (parser.modularGrammar == null) {
                throw new RuntimeException("Failed to return modular grammar; maybe enable modularity?");
            }

            Processor processor = parser.modularGrammar.getProcessor();
            DocumentBuilder builder = processor.newDocumentBuilder();
            XdmNode schematron = builder.build(new File("src/test/resources/" + basename + ".sch"));

            checkAssertions(parser.modularGrammar, schematron);

            return parser;
        } catch (SaxonApiException | XPathException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void checkAssertions(XdmNode modularGrammar, XdmNode schematron) throws SaxonApiException, XPathException {
        Processor processor = schematron.getProcessor();
        XsltCompiler compiler = processor.newXsltCompiler();

        if (transpiler == null) {
            SAXSource source = new SAXSource(new InputSource("src/test/resources/schxslt2-1.4.4/transpile.xsl"));
            XsltExecutable exec = compiler.compile(source);
            transpiler = exec.load();
        }

        XdmDestination destination = new XdmDestination();

        transpiler.setInitialContextNode(schematron);
        transpiler.setDestination(destination);
        transpiler.transform();

        XdmNode compiledSchema = destination.getXdmNode();
        XsltExecutable exec = compiler.compile(compiledSchema.asSource());
        Xslt30Transformer transformer = exec.load30();

        destination = new XdmDestination();
        transformer.applyTemplates(modularGrammar.asSource(), destination);

        XdmNode result = destination.getXdmNode();

        String error = evaluateXPath(result, "//svrl:failed-assert");
        if (!"".equals(error)) {
            fail(error);
        }
    }

    private String evaluateXPath(XdmNode doc, String xpath) throws SaxonApiException, XPathException {
        XPathExecutable exec = getCompiler(doc).compile(xpath);
        XPathSelector selector = exec.load();
        selector.setContextItem(doc);
        return selector.evaluate().getUnderlyingValue().getStringValue();
    }

    private XdmNode getNode(XdmNode doc, String xpath) throws SaxonApiException {
        XPathExecutable exec = getCompiler(doc).compile(xpath);
        XPathSelector selector = exec.load();
        selector.setContextItem(doc);
        XdmValue result = selector.evaluate();
        return (XdmNode) result;
    }

    private XPathCompiler getCompiler(XdmNode doc) {
        XPathCompiler compiler = doc.getProcessor().newXPathCompiler();
        compiler.declareNamespace("s", "http://purl.oclc.org/dsdl/schematron");
        compiler.declareNamespace("svrl", "http://purl.oclc.org/dsdl/svrl");
        return compiler;
    }

}
