package org.nineml.coffeesacks;

import net.sf.saxon.lib.Logger;
import net.sf.saxon.s9api.*;

import javax.xml.transform.TransformerException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TestConfiguration {
    protected final Processor processor;
    protected List<String> messages = new ArrayList<>();

    public TestConfiguration() {
        processor = new Processor(false);
        RegisterCoffeeSacks register = new RegisterCoffeeSacks();
        try {
            register.initialize(processor.getUnderlyingConfiguration());
            processor.getUnderlyingConfiguration().setLogger(new MyLogger());
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
        return transform(stylesheet, input, Collections.emptyMap());
    }

    public XdmNode transform(XdmNode stylesheet, XdmNode input, Map<String,String> parameters) {
        XdmDestination destination = new XdmDestination();

        try {
            XsltCompiler compiler = processor.newXsltCompiler();
            compiler.setSchemaAware(false);
            XsltExecutable exec = compiler.compile(stylesheet.asSource());
            XsltTransformer transformer = exec.load();

            for (String key : parameters.keySet()) {
                transformer.setParameter(new QName("", key), new XdmAtomicValue(parameters.get(key)));
            }

            transformer.setInitialContextNode(input);
            transformer.setDestination(destination);
            transformer.setMessageHandler(message -> {
                messages.add(message.getStringValue());
            });
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

    private class MyLogger extends Logger {
        @Override
        public void println(String message, int severity) {
            messages.add(message);
        }
    }

}
