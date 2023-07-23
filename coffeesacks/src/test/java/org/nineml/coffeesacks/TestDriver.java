package org.nineml.coffeesacks;


import net.sf.saxon.s9api.*;
import org.xml.sax.InputSource;

import javax.xml.transform.sax.SAXSource;
import java.io.*;

public class TestDriver extends TestConfiguration {
    public static void main(String[] args) {
        TestDriver driver = new TestDriver();
        driver.run(args);
    }

    private void run(String[] args) {
        try {
            XdmNode stylesheet = loadStylesheet("src/test/resources/date-xml-string.xsl");
            XdmNode result = transform(stylesheet, stylesheet);
            System.out.println(result);
        } catch (Exception ex) {
            System.err.println("Failed: " + ex.getMessage());
        }
    }
}
