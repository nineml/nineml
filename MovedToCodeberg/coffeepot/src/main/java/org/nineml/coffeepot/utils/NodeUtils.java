package org.nineml.coffeepot.utils;

import net.sf.saxon.s9api.*;
import org.nineml.coffeegrinder.parser.Family;
import org.nineml.coffeesacks.XmlForest;

public class NodeUtils {
    public static XdmNode getAmbiguityContext(Processor processor, XmlForest forest, Family choice) {
        try {
            XPathCompiler compiler = processor.newXPathCompiler();
            XPathExecutable exec = compiler.compile(String.format("//children[@id='C%d']/parent::*", choice.id));
            XPathSelector selector = exec.load();
            selector.setContextItem(forest.getXml());
            XdmNode parentNode = (XdmNode) selector.evaluateSingle();
            if (parentNode == null) {
                exec = compiler.compile(String.format("//*[@id='N%d']", choice.id));
                selector = exec.load();
                selector.setContextItem(forest.getXml());
                parentNode = (XdmNode) selector.evaluateSingle();
            }
            return parentNode;
        } catch (SaxonApiException ex) {
            throw new RuntimeException(ex);
        }
    }

}
