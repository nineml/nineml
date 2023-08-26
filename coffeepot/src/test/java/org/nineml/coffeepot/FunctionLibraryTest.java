package org.nineml.coffeepot;

import net.sf.saxon.s9api.Processor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.nineml.coffeepot.managers.OutputManager;

public class FunctionLibraryTest extends CoffeePotTest {
    private static boolean saxonEE = false;

    @BeforeAll
    public static void setup() {
        Processor processor = new Processor(true);
        saxonEE = "EE".equals(processor.getSaxonEdition());
    }

    @Test
    public void ambig1SelectAB() {
        Assumptions.assumeTrue(saxonEE, "Saxon-EE is not available; skipping function library test");

        WrappedPrintStream stdout = new WrappedPrintStream();
        WrappedPrintStream stderr = new WrappedPrintStream();
        Main main = new Main(stdout.stream, stderr.stream);
        try {
            OutputManager manager = main.commandLine(new String[] {"-g:src/test/resources/ambig1.ixml", "--function-library:src/test/resources/ambig1-a-b.xsl", "x" });
            Assertions.assertEquals(1, manager.stringRecords.size());
            Assertions.assertEquals("<S><A><B><C>x</C></B></A></S>", manager.stringRecords.get(0));
            Assertions.assertTrue(stderr.contains("Found 4 possible parses"));
        } catch (Exception ex) {
            Assertions.fail();
        }
    }

    @Test
    public void ambig1SelectAC() {
        Assumptions.assumeTrue(saxonEE, "Saxon-EE is not available; skipping function library test");

        WrappedPrintStream stdout = new WrappedPrintStream();
        WrappedPrintStream stderr = new WrappedPrintStream();
        Main main = new Main(stdout.stream, stderr.stream);
        try {
            OutputManager manager = main.commandLine(new String[] {"-g:src/test/resources/ambig1.ixml", "--function-library:src/test/resources/ambig1-a-c.xsl", "x" });
            Assertions.assertEquals(1, manager.stringRecords.size());
            Assertions.assertEquals("<S><A><C>x</C></A></S>", manager.stringRecords.get(0));
            Assertions.assertTrue(stderr.contains("Found 4 possible parses"));
        } catch (Exception ex) {
            Assertions.fail();
        }
    }

    @Test
    public void ambig1SelectBC() {
        Assumptions.assumeTrue(saxonEE, "Saxon-EE is not available; skipping function library test");

        WrappedPrintStream stdout = new WrappedPrintStream();
        WrappedPrintStream stderr = new WrappedPrintStream();
        Main main = new Main(stdout.stream, stderr.stream);
        try {
            OutputManager manager = main.commandLine(new String[] {"-g:src/test/resources/ambig1.ixml", "--function-library:src/test/resources/ambig1-b-c.xsl", "x" });
            Assertions.assertEquals(1, manager.stringRecords.size());
            Assertions.assertEquals("<S><B><C>x</C></B></S>", manager.stringRecords.get(0));
            Assertions.assertTrue(stderr.contains("Found 4 possible parses"));
        } catch (Exception ex) {
            Assertions.fail();
        }
    }

    @Test
    public void ambig1SelectD() {
        Assumptions.assumeTrue(saxonEE, "Saxon-EE is not available; skipping function library test");

        WrappedPrintStream stdout = new WrappedPrintStream();
        WrappedPrintStream stderr = new WrappedPrintStream();
        Main main = new Main(stdout.stream, stderr.stream);
        try {
            OutputManager manager = main.commandLine(new String[] {"-g:src/test/resources/ambig1.ixml", "--function-library:src/test/resources/ambig1-d.xsl", "x" });
            Assertions.assertEquals(1, manager.stringRecords.size());
            Assertions.assertTrue(manager.stringRecords.get(0).contains("ambiguous"));
            Assertions.assertTrue(stderr.contains("Found 4 possible parses"));
        } catch (Exception ex) {
            Assertions.fail();
        }
    }

    @Test
    public void ambig3loop10() {
        Assumptions.assumeTrue(saxonEE, "Saxon-EE is not available; skipping function library test");

        WrappedPrintStream stdout = new WrappedPrintStream();
        WrappedPrintStream stderr = new WrappedPrintStream();
        Main main = new Main(stdout.stream, stderr.stream);
        try {
            OutputManager manager = main.commandLine(new String[] {"-g:src/test/resources/loop.ixml", "--function-library:src/test/resources/loop10.xsl", "xyz" });
            Assertions.assertEquals(1, manager.stringRecords.size());
            Assertions.assertEquals("<S><X>x</X><A><A><A><A><A><A><A><A><A><A>y</A></A></A></A></A></A></A></A></A></A><Z>z</Z></S>", manager.stringRecords.get(0));
            Assertions.assertTrue(stderr.contains("Found 2 possible parses (of infinitely many)"));
        } catch (Exception ex) {
            System.err.println(stderr);
            Assertions.fail();
        }
    }
}
