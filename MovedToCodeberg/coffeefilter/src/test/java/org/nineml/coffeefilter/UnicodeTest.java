package org.nineml.coffeefilter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.fail;

public class UnicodeTest {
    private InvisibleXml invisibleXml;

    @BeforeEach
    public void setup() {
        ParserOptions options = new ParserOptions();
        options.setPedantic(true);
        invisibleXml = new InvisibleXml(options);
    }

    @Test
    public void small() {
        try {
            //invisibleXml.getOptions().getLogger().setDefaultLogLevel(Logger.DEBUG);
            //invisibleXml.getOptions().setParserType("GLL");
            //invisibleXml.getOptions().setProgressMonitor(new DefaultProgressMonitor());
            InvisibleXmlParser parser = invisibleXml.getParser(new File("src/test/resources/unicode.ixml"));
            InvisibleXmlDocument doc = parser.parse(new File("src/test/resources/SmallData.txt"));
            Assertions.assertTrue(doc.succeeded());
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }

}
