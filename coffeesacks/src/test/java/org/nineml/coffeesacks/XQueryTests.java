package org.nineml.coffeesacks;

import net.sf.saxon.s9api.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.nineml.coffeefilter.InvisibleXml;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

public class XQueryTests extends TestConfiguration {
    public static final QName ixml_state = new QName(InvisibleXml.ixml_prefix, InvisibleXml.ixml_ns, "state");

    @Test
    public void xquerySmokeTest() {
        try {
            XQueryCompiler compiler = processor.newXQueryCompiler();
            XQueryExecutable exec = compiler.compile("3 + 4");
            XQueryEvaluator eval = exec.load();
            int count = 0;
            for (XdmItem item : eval) {
                count++;
                Assertions.assertEquals(1, count);
                Assertions.assertTrue(item instanceof XdmAtomicValue);
                XdmAtomicValue value = (XdmAtomicValue) item;
                Assertions.assertEquals(7L, value.getLongValue());
            }
        } catch (SaxonApiException ex) {
            fail();
        }
    }

    @Test
    public void xqueryFunction() {
        // This tests two things: calling CoffeeSacks from XQuery, but also passing a curried function
        // as the choose-alternatives function.
        try {
            XQueryCompiler compiler = processor.newXQueryCompiler();
            XQueryExecutable exec = compiler.compile(new File("src/test/resources/simpler.xqy"));
            XQueryEvaluator eval = exec.load();
            int count = 0;
            for (XdmItem item : eval) {
                count++;
                if (count == 1) {
                    Assertions.assertEquals("FX+XF-X+XFF+X-XF+X+XFFF+X+XF-X+XFF+X+XF+X-XFFFF+X+XF-X-XFF+X-XF-X+XFFF+X+XF+X+XFF-X+XF+X+XFFFFF+FX+XF-X-XFF-X+XF+X+XFFF-X+XF-X+XFF-X+XF-X-XFFFF-X+XF-X-XFF+X+XF+X+XFFF-X-XF+X+XFF+X-XF+X-XFFFFF+",
                            item.getStringValue());
                } else if (count == 2) {
                    Assertions.assertEquals("FX-XF+X-XFF+X+XF+X-XFFF+X+XF-X+XFF-X+XF+X-XFFFF-X+XF-X+XFF-X+XF+X+XFFF+X-XF-X+XFF-X+XF+X-XFFFFF+FX+XF+X+XFF+X+XF+X-XFFF+X+XF-X-XFF-X+XF+X-XFFFF+X-XF-X-XFF-X+XF-X-XFFF-X+XF+X+XFF-X-XF+X+XFFFFF+",
                            item.getStringValue());
                } else {
                    fail();
                }
            }
        } catch (SaxonApiException | IOException ex) {
            ex.printStackTrace();
            fail();
        }
    }


}
