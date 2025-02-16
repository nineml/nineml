package org.nineml.coffeesacks;

import net.sf.saxon.s9api.*;
import net.sf.saxon.trans.XPathException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.nineml.coffeefilter.InvisibleXml;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

public class StylesheetTests extends TestConfiguration {
    public static final QName ixml_state = new QName(InvisibleXml.ixml_prefix, InvisibleXml.ixml_ns, "state");

    @Test
    public void stringInputXmlOutput() {
        XdmNode stylesheet = loadStylesheet("src/test/resources/date-xml-string.xsl");
        XdmNode result = transform(stylesheet, stylesheet);
        Assertions.assertEquals("<doc><date><day>15</day><month>February</month><year>2022</year></date></doc>", serialize(result));
    }

    @Test
    public void stringInputError() {
        XdmNode stylesheet = loadStylesheet("src/test/resources/date-input-error.xsl");
        try {
            transform(stylesheet, stylesheet);
            fail();
        } catch (RuntimeException ex) {
            Assertions.assertTrue(ex.getMessage() != null && ex.getMessage().contains("<failed"));
        }
    }


    @Test
    public void uriInputXmlOutput() {
        XdmNode stylesheet = loadStylesheet("src/test/resources/date-xml-uri.xsl");
        XdmNode result = transform(stylesheet, stylesheet);
        Assertions.assertEquals("<doc><date><day>20</day><month>February</month><year>2022</year></date></doc>", serialize(result));
    }

    @Test
    public void uriInputXmlOutputExample() {
        XdmNode stylesheet = loadStylesheet("src/test/resources/date-xml-example.xsl");
        XdmNode result = transform(stylesheet, stylesheet);
        Assertions.assertEquals("<doc><date><day>15</day><month>February</month><year>2022</year></date></doc>", serialize(result));
    }

    @Test
    public void xmlInputXmlOutput() {
        XdmNode stylesheet = loadStylesheet("src/test/resources/date-xml-xml.xsl");
        XdmNode result = transform(stylesheet, stylesheet);
        Assertions.assertEquals("<doc><date><day>15</day><month>February</month><year>2022</year></date></doc>", serialize(result));
    }

    @Test
    public void textInputXmlOutput() {
        XdmNode stylesheet = loadStylesheet("src/test/resources/date-xml-text.xsl");
        XdmNode result = transform(stylesheet, stylesheet);
        Assertions.assertEquals("<doc><date><day>15</day><month>February</month><year>2022</year></date></doc>", serialize(result));
    }

    @Test
    public void stringInputMapOutput() {
        XdmNode stylesheet = loadStylesheet("src/test/resources/date-map-string.xsl");
        XdmNode result = transform(stylesheet, stylesheet);
        Assertions.assertEquals("<doc>year: 2022, month: February, day: 15</doc>", serialize(result));
    }

    @Test
    public void uriInputMapOutput() {
        XdmNode stylesheet = loadStylesheet("src/test/resources/date-map-uri.xsl");
        XdmNode result = transform(stylesheet, stylesheet);
        Assertions.assertEquals("<doc>year: 2022, month: February, day: 20</doc>", serialize(result));
    }

    @Test
    public void hygieneOutputTransformOk() {
        XdmNode stylesheet = loadStylesheet("src/test/resources/hygieneok.xsl");
        XdmNode result = transform(stylesheet, stylesheet);
        Assertions.assertEquals("<doc><S><A>a<X>x</X></A></S></doc>", serialize(result));
    }

    @Test
    public void hygieneOutputTransformFail() {
        XdmNode stylesheet = loadStylesheet("src/test/resources/hygienefail.xsl");
        XdmNode result = transform(stylesheet, stylesheet);
        String xml = serialize(result);
        Assertions.assertEquals("<doc>FAILED</doc>", serialize(result));
    }

    @Test
    public void hygieneOutputReportXml() {
        XdmNode stylesheet = loadStylesheet("src/test/resources/hygieneinfo.xsl");
        XdmNode result = transform(stylesheet, stylesheet);
        // Cheap and cheerful
        String report = serialize(result);
        Assertions.assertTrue(report.contains("clean=\"false\"") || report.contains("clean='false'"));
        Assertions.assertTrue(report.contains("<symbol>Y</symbol>"));
        Assertions.assertTrue(report.contains("<symbol>Z</symbol>"));
        Assertions.assertTrue(report.contains("<rule>B"));
        Assertions.assertTrue(report.contains("<rule>S"));
    }

    @Test
    public void hygieneOutputReportJson() {
        XdmNode stylesheet = loadStylesheet("src/test/resources/hygieneinfo-json.xsl");
        XdmNode result = transform(stylesheet, stylesheet);
        // Cheap and cheerful
        String report = serialize(result);
        Assertions.assertTrue(report.contains("\"clean\":false"));
        Assertions.assertTrue(report.contains("[\"Y\"]"));
        Assertions.assertTrue(report.contains("[\"Z\"]"));
        Assertions.assertTrue(report.contains("\"unproductive\":"));
    }

    @Test
    public void resolveAmbiguityFunctionXml() {
        XdmNode stylesheet = loadStylesheet("src/test/resources/ambiguity-xml.xsl");
        XdmNode result = transform(stylesheet, stylesheet);
        Assertions.assertEquals("<doc><s><n>123</n></s></doc>", serialize(result));
    }

    @Test
    public void resolveAmbiguityFunctionRootXml() {
        XdmNode stylesheet = loadStylesheet("src/test/resources/root-ambiguity-xml.xsl");
        XdmNode result = transform(stylesheet, stylesheet);
        Assertions.assertEquals("<doc><s><b>a</b></s></doc>", serialize(result));
    }

    @Test
    public void resolveAmbiguityStrictFunctionRootXml() {
        XdmNode stylesheet = loadStylesheet("src/test/resources/root-ambiguity-strict-xml.xsl");
        XdmNode result = transform(stylesheet, stylesheet);
        Assertions.assertEquals("<doc><s xmlns:ixml=\"http://invisiblexml.org/NS\" ixml:state=\"ambiguous\"><b>a</b></s></doc>", serialize(result));
    }

    @Test
    public void resolveAmbiguityFunctionNumbers() {
        XdmNode stylesheet = loadStylesheet("src/test/resources/numbers.xsl");
        XdmNode result = transform(stylesheet, stylesheet);
        Assertions.assertTrue(serialize(result).contains("<decimal>"));
    }

    @Test
    public void resolveAmbiguityFunctionJson() {
        XdmNode stylesheet = loadStylesheet("src/test/resources/ambiguity-json.xsl");
        XdmNode result = transform(stylesheet, stylesheet);
        Assertions. assertEquals("<doc>{ \"s\":{ \"n\":123 } }</doc>", serialize(result));
    }

    @Test
    public void resolveAmbiguityStrictFunctionJson() {
        XdmNode stylesheet = loadStylesheet("src/test/resources/ambiguity-strict-json.xsl");
        XdmNode result = transform(stylesheet, stylesheet);
        Assertions.assertEquals("<doc>{ \"s\":{ \"ixml:state\":\"ambiguous\", \"n\":123 } }</doc>", serialize(result));
    }

    @Test
    public void alternativeChoice01() {
        XdmNode stylesheet = loadStylesheet("src/test/resources/alt-01.xsl");
        XdmNode result = transform(stylesheet, stylesheet);
        Assertions.assertTrue(serialize(result).contains("<B>"));
        Assertions.assertFalse(serialize(result).contains("ambiguous"));
    }

    @Test
    public void horiz1() {
        XdmNode stylesheet = loadStylesheet("src/test/resources/horiz1.xsl");
        XdmNode result = transform(stylesheet, stylesheet);
        String xml = serialize(result);
        Assertions.assertTrue(xml.contains("<B>"));
        Assertions.assertFalse(xml.contains("ambiguous"));
    }

    @Test
    public void horiz2() {
        XdmNode stylesheet = loadStylesheet("src/test/resources/horiz2.xsl");
        XdmNode result = transform(stylesheet, stylesheet);
        String xml = serialize(result);
        Assertions.assertTrue(xml.contains("<A>"));
        Assertions.assertTrue(xml.contains("ambiguous"));
    }

    @Test
    public void numbers() {
        XdmNode stylesheet = loadStylesheet("src/test/resources/decimal.xsl");
        XdmNode result = transform(stylesheet, stylesheet);
        String xml = serialize(result);
        Assertions.assertTrue(xml.contains("<decimal>42"));
    }

    @Test
    public void startSymbolUndefined() {
        XdmNode stylesheet = loadStylesheet("src/test/resources/start-symbol.xsl");

        XdmNode result = transform(stylesheet, stylesheet);
        String xml = serialize(result);
        Assertions.assertTrue(xml.contains("<S "));
        Assertions.assertTrue(xml.contains("ambiguous"));
    }

    @Test
    public void startSymbolB() {
        XdmNode stylesheet = loadStylesheet("src/test/resources/start-symbol.xsl");

        Map<String,String> params = new HashMap<>();
        params.put("start-symbol", "B");

        XdmNode result = transform(stylesheet, stylesheet, params);
        String xml = serialize(result);
        Assertions.assertEquals("<doc><B>a</B></doc>", xml);
    }

    @Test
    public void startSymbolX() {
        XdmNode stylesheet = loadStylesheet("src/test/resources/start-symbol.xsl");

        Map<String,String> params = new HashMap<>();
        params.put("start-symbol", "X");

        try {
            XdmNode result = transform(stylesheet, stylesheet, params);
            String xml = serialize(result);
            fail();
        } catch (Exception ex) {
            ex = (Exception) ex.getCause();
            Assertions.assertTrue(ex.getMessage().contains("Failed to parse grammar"));
        }
    }

    @Test
    public void logLevelDefault() {
        XdmNode stylesheet = loadStylesheet("src/test/resources/log-level.xsl");

        Map<String,String> params = new HashMap<>();

        XdmNode result = transform(stylesheet, stylesheet, params);
        String xml = serialize(result);
        Assertions.assertEquals("<doc><S>a</S></doc>", xml);
    }

    @Test
    public void logLevelDebug() {
        XdmNode stylesheet = loadStylesheet("src/test/resources/log-level.xsl");

        Map<String,String> params = new HashMap<>();
        params.put("log-level", "debug");

        XdmNode result = transform(stylesheet, stylesheet, params);
        String xml = serialize(result);
        Assertions.assertEquals("<doc><S>a</S></doc>", xml);
    }

}
