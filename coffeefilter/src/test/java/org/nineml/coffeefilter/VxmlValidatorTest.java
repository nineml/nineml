package org.nineml.coffeefilter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.nineml.coffeefilter.exceptions.IxmlException;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.fail;

public class VxmlValidatorTest {
    @Test
    public void validVxml() {
        ParserOptions options = new ParserOptions();
        InvisibleXml ixml = new InvisibleXml(options);

        try {
            InvisibleXmlParser parser = ixml.getParser(new File("src/test/resources/numbers.xml"));
            InvisibleXmlDocument document = parser.parse("3.14");
            String xml = document.getTree();
            Assertions.assertEquals("<number><float><digits>3</digits><point/><digits>14</digits></float></number>", xml);
        } catch (IOException err) {
            fail();
        }
    }

    @Test
    public void invalidVxml() {
        ParserOptions options = new ParserOptions();
        options.setValidateVxml(true);
        InvisibleXml ixml = new InvisibleXml(options);

        try {
            InvisibleXmlParser parser = ixml.getParser(new File("src/test/resources/numbers-invalid.xml"));
            fail();
        } catch (IxmlException ex) {
            Assertions.assertEquals("E018", ex.getCode());
        } catch (IOException ex) {
            fail();
        }
    }

    @Test
    public void uncheckedVxml() {
        ParserOptions options = new ParserOptions();
        options.setValidateVxml(false);
        InvisibleXml ixml = new InvisibleXml(options);

        try {
            InvisibleXmlParser parser = ixml.getParser(new File("src/test/resources/numbers-invalid.xml"));
            InvisibleXmlDocument document = parser.parse("3.14");
            String xml = document.getTree();
            Assertions.assertEquals("<number><float><digits>3</digits><point/><digits>14</digits></float></number>", xml);
        } catch (IOException err) {
            fail();
        }
    }


}
