package org.nineml.coffeepot;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.nineml.coffeepot.managers.OutputManager;

import static org.junit.jupiter.api.Assertions.fail;

public class XPathChoiceTest extends CoffeePotTest {
    @Test
    public void selChildren() {
        WrappedPrintStream stdout = new WrappedPrintStream();
        WrappedPrintStream stderr = new WrappedPrintStream();
        Main main = new Main(stdout.stream, stderr.stream);
        try {
            OutputManager manager = main.commandLine(new String[] {"-g:src/test/resources/ambig2.ixml", "--describe-ambiguity",
                    "--choose:children[symbol[@name='B']]", "x" });
            Assertions.assertEquals(1, manager.stringRecords.size());
            Assertions.assertEquals("<S><B>x</B></S>", manager.stringRecords.get(0));
            Assertions.assertTrue(stderr.contains("Found 3 possible parses"));

            String output = stderr.toString();
            Assertions.assertTrue(output.contains("✔ B«1-1»"));
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void selAttribute() {
        WrappedPrintStream stdout = new WrappedPrintStream();
        WrappedPrintStream stderr = new WrappedPrintStream();
        Main main = new Main(stdout.stream, stderr.stream);
        try {
            OutputManager manager = main.commandLine(new String[] {"-g:src/test/resources/ambig2.ixml", "--describe-ambiguity",
                    "--choose:children[symbol[@name='B']]/@id", "x" });
            Assertions.assertEquals(1, manager.stringRecords.size());
            Assertions.assertEquals("<S><B>x</B></S>", manager.stringRecords.get(0));
            Assertions.assertTrue(stderr.contains("Found 3 possible parses"));

            String output = stderr.toString();
            Assertions.assertTrue(output.contains("✔ B«1-1»"));
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void selectNothing() {
        WrappedPrintStream stdout = new WrappedPrintStream();
        WrappedPrintStream stderr = new WrappedPrintStream();
        Main main = new Main(stdout.stream, stderr.stream);
        try {
            OutputManager manager = main.commandLine(new String[] {"-g:src/test/resources/ambig2.ixml",
                    "--choose:children[frizzle]", "x" });
            Assertions.assertEquals(1, manager.stringRecords.size());
            Assertions.assertTrue(manager.stringRecords.get(0).contains("ambiguous"));
            Assertions.assertTrue(stderr.contains("Found 3 possible parses"));
        } catch (Exception ex) {
            fail();
        }
    }
}
