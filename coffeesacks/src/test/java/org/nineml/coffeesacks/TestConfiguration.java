package org.nineml.coffeesacks;

import net.sf.saxon.s9api.*;
import org.nineml.coffeefilter.InvisibleXml;

import javax.xml.transform.TransformerException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

public class TestConfiguration {
    protected final Processor processor;

    public TestConfiguration() {
        processor = new Processor(false);
        RegisterCoffeeSacks register = new RegisterCoffeeSacks();
        try {
            register.initialize(processor.getUnderlyingConfiguration());
        } catch (TransformerException ex) {
            throw new RuntimeException(ex);
        }
    }

    public XdmNode loadStylesheet(String name) {
        DocumentBuilder builder = processor.newDocumentBuilder();
        try {
            return builder.build(new File(name));
        } catch (SaxonApiException ex) {
            throw new RuntimeException(ex);
        }
    }

    public XdmNode transform(XdmNode stylesheet, XdmNode input) {
        XdmDestination destination = new XdmDestination();

        try {
            XsltCompiler compiler = processor.newXsltCompiler();
            compiler.setSchemaAware(false);
            XsltExecutable exec = compiler.compile(stylesheet.asSource());
            XsltTransformer transformer = exec.load();
            transformer.setInitialContextNode(input);
            transformer.setDestination(destination);
            transformer.transform();
        } catch (SaxonApiException ex) {
            throw new RuntimeException(ex);
        }

        return destination.getXdmNode();
    }

    public String serialize(XdmNode node) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Serializer serializer = processor.newSerializer(baos);
            serializer.setOutputProperty(Serializer.Property.OMIT_XML_DECLARATION, "yes");
            serializer.serialize(node.asSource());
            return baos.toString();
        } catch (SaxonApiException ex) {
            throw new RuntimeException(ex);
        }
    }

}
