package org.nineml.coffeefilter;

import org.junit.jupiter.api.Disabled;
import org.nineml.coffeegrinder.parser.Grammar;
import org.nineml.coffeegrinder.util.DefaultProgressMonitor;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.fail;

public class RegexTest {

    @Disabled
    public void simple() {
        try {
            InvisibleXml invisibleXml = new InvisibleXml();

            String ixml = "{[+ixmlns:n \"https://nineml.org/ns\"]} S: A,B. A:'a'. {[n:token]} B: 'b'+.";
            ByteArrayInputStream bais = new ByteArrayInputStream(ixml.getBytes(StandardCharsets.UTF_8));

            invisibleXml.getOptions().setParserType("GLL");
            //invisibleXml.getOptions().getLogger().setDefaultLogLevel("debug");
            invisibleXml.getOptions().setProgressMonitor(new DefaultProgressMonitor());

            InvisibleXmlParser parser = invisibleXml.getParser(bais, "UTF-8");
            Grammar grammar = parser.getGrammar();
            System.err.println("bang");
        } catch (Exception ex) {
            fail();
        }
    }

    @Disabled
    public void choice() {
        try {
            InvisibleXml invisibleXml = new InvisibleXml();

            String ixml = "{[+ixmlns:n \"https://nineml.org/ns\"]} S: A,B. A:'b','c'. {[n:token]} B: ('b'|'c')+.";
            ByteArrayInputStream bais = new ByteArrayInputStream(ixml.getBytes(StandardCharsets.UTF_8));

            invisibleXml.getOptions().setParserType("GLL");
            //invisibleXml.getOptions().getLogger().setDefaultLogLevel("debug");
            invisibleXml.getOptions().setProgressMonitor(new DefaultProgressMonitor());

            InvisibleXmlParser parser = invisibleXml.getParser(bais, "UTF-8");
            Grammar grammar = parser.getGrammar();
            System.err.println("bang");
        } catch (Exception ex) {
            fail();
        }
    }

}
