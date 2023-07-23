package org.nineml.coffeesacks;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.Initializer;

import javax.xml.transform.TransformerException;

/**
 * An initializer class for registering the CoffeeSacks extension functions.
 */
public class RegisterCoffeeSacks implements Initializer {
    @Override
    public void initialize(Configuration config) throws TransformerException {
        config.registerExtensionFunction(new LoadGrammar(config));
        config.registerExtensionFunction(new MakeParser(config));
        config.registerExtensionFunction(new HygieneReportFunction(config));
    }
}
