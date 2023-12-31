package org.nineml.coffeepot;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.nineml.coffeepot.managers.OutputManager;

import static org.junit.jupiter.api.Assertions.fail;

public class MainTest extends CoffeePotTest {

    @Test
    public void help() {
        WrappedPrintStream stdout = new WrappedPrintStream();
        WrappedPrintStream stderr = new WrappedPrintStream();
        Main main = new Main(stdout.stream, stderr.stream);
        try {
            OutputManager manager = main.commandLine(new String[] {"--help" });
            Assertions.assertEquals(0, manager.stringRecords.size());
            Assertions.assertTrue(stderr.contains("Usage:"));
            Assertions.assertEquals(0, manager.getReturnCode());
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void smokeTestXml() {
        Main main = new Main();
        try {
            OutputManager manager = main.commandLine(new String[] {"-g:src/test/resources/smoke.ixml", "a" });
            Assertions.assertEquals(1, manager.stringRecords.size());
            Assertions.assertEquals("<S><A>a</A></S>", manager.stringRecords.get(0));
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void smokeTestJson() {
        Main main = new Main();
        try {
            OutputManager manager = main.commandLine(new String[] {"-g:src/test/resources/smoke.ixml", "--format:json", "a" });
            Assertions.assertEquals(1, manager.stringRecords.size());
            Assertions.assertEquals("{\"S\":{\"A\":\"a\"}}", manager.stringRecords.get(0));
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void showEarleyGrammar() {
        WrappedPrintStream stdout = new WrappedPrintStream();
        WrappedPrintStream stderr = new WrappedPrintStream();
        Main main = new Main(stdout.stream, stderr.stream);
        try {
            OutputManager manager = main.commandLine(new String[] {"-g:src/test/resources/smoke.ixml", "--show-grammar", "a" });
            Assertions.assertEquals(1, manager.stringRecords.size());
            Assertions.assertEquals("<S><A>a</A></S>", manager.stringRecords.get(0));
            Assertions.assertTrue(stderr.contains("The Earley grammar"));
            Assertions.assertTrue(stderr.contains("2.  S ::= A"));
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void showGllGrammar() {
        WrappedPrintStream stdout = new WrappedPrintStream();
        WrappedPrintStream stderr = new WrappedPrintStream();
        Main main = new Main(stdout.stream, stderr.stream);
        try {
            OutputManager manager = main.commandLine(new String[] {"-g:src/test/resources/smoke.ixml", "--show-grammar", "--gll", "a" });
            Assertions.assertEquals(1, manager.stringRecords.size());
            Assertions.assertEquals("<S><A>a</A></S>", manager.stringRecords.get(0));
            Assertions.assertTrue(stderr.contains("The GLL grammar"));
            Assertions.assertTrue(stderr.contains("2.  S ::= A"));
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void noOutput() {
        Main main = new Main();
        try {
            OutputManager manager = main.commandLine(new String[] {"-g:src/test/resources/smoke.ixml", "a", "--no-output"});
            Assertions.assertEquals(0, manager.stringRecords.size());
            Assertions.assertEquals("", manager.publication());
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void timing() {
        WrappedPrintStream stdout = new WrappedPrintStream();
        WrappedPrintStream stderr = new WrappedPrintStream();
        Main main = new Main(stdout.stream, stderr.stream);
        try {
            OutputManager manager = main.commandLine(new String[] {"-g:src/test/resources/smoke.ixml", "a", "--time"});
            Assertions.assertEquals(1, manager.stringRecords.size());
            Assertions.assertTrue(stderr.toString().contains("Parsed src/"));
            Assertions.assertTrue(stderr.toString().contains("Parsed input in"));
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void ambig2() {
        WrappedPrintStream stdout = new WrappedPrintStream();
        WrappedPrintStream stderr = new WrappedPrintStream();
        Main main = new Main(stdout.stream, stderr.stream);
        try {
            OutputManager manager = main.commandLine(new String[] {"-g:src/test/resources/ambig2.ixml", "x" });
            Assertions.assertEquals(1, manager.stringRecords.size());
            Assertions.assertTrue(manager.stringRecords.get(0).contains("ambiguous"));
            Assertions.assertTrue(stderr.contains("Found 3 possible"));
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void ambig2_suppressed() {
        WrappedPrintStream stdout = new WrappedPrintStream();
        WrappedPrintStream stderr = new WrappedPrintStream();
        Main main = new Main(stdout.stream, stderr.stream);
        try {
            OutputManager manager = main.commandLine(new String[] {"-g:src/test/resources/ambig2.ixml", "--suppress:ambiguous", "x" });
            Assertions.assertEquals(1, manager.stringRecords.size());
            Assertions.assertFalse(manager.stringRecords.get(0).contains("ambiguous"));
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void describeAmbiguity() {
        WrappedPrintStream stdout = new WrappedPrintStream();
        WrappedPrintStream stderr = new WrappedPrintStream();
        Main main = new Main(stdout.stream, stderr.stream);
        try {
            OutputManager manager = main.commandLine(new String[] {"-g:src/test/resources/ambig2.ixml", "--describe-ambiguity", "x" });
            Assertions.assertEquals(1, manager.stringRecords.size());
            Assertions.assertTrue(stderr.contains("✔ 'x'«1-1»"));
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void analyzeAmbiguity() {
        WrappedPrintStream stdout = new WrappedPrintStream();
        WrappedPrintStream stderr = new WrappedPrintStream();
        Main main = new Main(stdout.stream, stderr.stream);
        try {
            OutputManager manager = main.commandLine(new String[] {"-g:src/test/resources/ambig2.ixml", "--analyze-ambiguity", "x" });
            Assertions.assertEquals(1, manager.stringRecords.size());
            Assertions.assertTrue(stderr.contains("The grammar is ambiguous"));
            Assertions.assertTrue(stderr.contains("vertical ambiguity:"));
            Assertions.assertTrue(stderr.contains("horizontal ambiguity:"));
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void analyzeUnicodeClassAmbiguity() {
        WrappedPrintStream stdout = new WrappedPrintStream();
        WrappedPrintStream stderr = new WrappedPrintStream();
        Main main = new Main(stdout.stream, stderr.stream);
        try {
            OutputManager manager = main.commandLine(new String[] {"-g:src/test/resources/list2.ixml", "--analyze-ambiguity", "a,1" });
            Assertions.assertEquals(1, manager.stringRecords.size());
            Assertions.assertTrue(stderr.contains("The grammar is ambiguous"));
            Assertions.assertTrue(stderr.contains("may be unreliable"));
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void showAmbiguities() {
        WrappedPrintStream stdout = new WrappedPrintStream();
        WrappedPrintStream stderr = new WrappedPrintStream();
        Main main = new Main(stdout.stream, stderr.stream);
        try {
            OutputManager manager = main.commandLine(new String[] {"-g:src/test/resources/ambig2.ixml", "x", "--mark-ambiguities" });
            Assertions.assertEquals(1, manager.stringRecords.size());
            Assertions.assertEquals("<S xmlns:n='https://nineml.org/ns/' xmlns:ixml='http://invisiblexml.org/NS' n:ambiguous='true' ixml:state='ambiguous'>x</S>",
                    manager.stringRecords.get(0));
        } catch (Exception ex) {
            fail();
        }
    }


    @Test
    public void showMarks() {
        WrappedPrintStream stdout = new WrappedPrintStream();
        WrappedPrintStream stderr = new WrappedPrintStream();
        Main main = new Main(stdout.stream, stderr.stream);
        try {
            OutputManager manager = main.commandLine(new String[] {"-g:src/test/resources/priority.ixml", "--show-marks", "due 02/07/2023" });
            Assertions.assertEquals(1, manager.stringRecords.size());
            String result = manager.stringRecords.get(0);
            Assertions.assertTrue(result.contains(" ixml:mark"));
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void prettyPrint() {
        WrappedPrintStream stdout = new WrappedPrintStream();
        WrappedPrintStream stderr = new WrappedPrintStream();
        Main main = new Main(stdout.stream, stderr.stream);
        try {
            OutputManager manager = main.commandLine(new String[] {"-g:src/test/resources/priority.ixml", "-pp", "due 02/07/2023" });
            Assertions.assertEquals(1, manager.stringRecords.size());
            String result = manager.stringRecords.get(0);
            Assertions.assertTrue(result.contains("<deadline>\n   <date>\n"));
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void showHiddenNonterminals() {
        WrappedPrintStream stdout = new WrappedPrintStream();
        WrappedPrintStream stderr = new WrappedPrintStream();
        Main main = new Main(stdout.stream, stderr.stream);
        try {
            OutputManager manager = main.commandLine(new String[] {"-g:src/test/resources/priority.ixml", "--show-hidden-nonterminals", "due 02/07/2023" });
            Assertions.assertEquals(1, manager.stringRecords.size());
            String result = manager.stringRecords.get(0);
            Assertions.assertTrue(result.contains("<n:symbol"));
            Assertions.assertTrue(result.contains("name='$$'"));
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void encoding() {
        WrappedPrintStream stdout = new WrappedPrintStream();
        WrappedPrintStream stderr = new WrappedPrintStream();
        Main main = new Main(stdout.stream, stderr.stream);
        try {
            OutputManager manager = main.commandLine(new String[] {"-g:src/test/resources/iso-latin-1.ixml",
                    "--grammar-encoding:iso-8859-1", "--encoding:iso-8859-1", "-i:src/test/resources/iso-latin-1.txt" });
            Assertions.assertEquals(1, manager.stringRecords.size());
            Assertions.assertEquals("<S>© (C)</S>", manager.stringRecords.get(0));
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void bnf() {
        WrappedPrintStream stdout = new WrappedPrintStream();
        WrappedPrintStream stderr = new WrappedPrintStream();
        Main main = new Main(stdout.stream, stderr.stream);
        try {
            OutputManager manager = main.commandLine(new String[] {"-g:src/test/resources/simple-bnf.ixml",
                    "--bnf", "bc" });
            Assertions.assertEquals(1, manager.stringRecords.size());
            Assertions.assertEquals("<S><B>b<C>c</C></B></S>", manager.stringRecords.get(0));
            Assertions.assertEquals("", stderr.toString());
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void notBnf() {
        WrappedPrintStream stdout = new WrappedPrintStream();
        WrappedPrintStream stderr = new WrappedPrintStream();
        Main main = new Main(stdout.stream, stderr.stream);
        try {
            OutputManager manager = main.commandLine(new String[] {"-g:src/test/resources/not-bnf.ixml",
                    "--bnf", "abcbc" });
            Assertions.assertEquals(1, manager.stringRecords.size());
            Assertions.assertEquals("<S><A>a</A><B>b<C>c</C></B><B>b<C>c</C></B></S>", manager.stringRecords.get(0));
            Assertions.assertEquals("Grammar does not conform to plain BNF: S\n", stderr.toString());
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void rewriteTest1() {
        Main main = new Main();
        try {
            OutputManager manager = main.commandLine(new String[] {"-g:src/test/resources/rename.ixml", "c" });
            Assertions.assertEquals(1, manager.stringRecords.size());
            Assertions.assertEquals("<X><C>c</C></X>", manager.stringRecords.get(0));
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void rewriteTest2() {
        Main main = new Main();
        try {
            OutputManager manager = main.commandLine(new String[] {"-g:src/test/resources/rename.ixml", "ab" });
            Assertions.assertEquals(1, manager.stringRecords.size());
            Assertions.assertEquals("<X><Y>a</Y><B>b</B></X>", manager.stringRecords.get(0));
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void normalize_line_endings_1() {
        Main main = new Main();
        try {
            String input = "-i:src/test/resources/unix-lines.txt";
            OutputManager manager = main.commandLine(new String[] {"-g:src/test/resources/lines.ixml", input });
            Assertions.assertEquals(1, manager.stringRecords.size());
            Assertions.assertEquals("<lines><l>1</l><l>2</l><l>3</l></lines>", manager.stringRecords.get(0));
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void normalize_line_endings_2() {
        Main main = new Main();
        try {
            String input = "-i:src/test/resources/pc-lines.bin";
            OutputManager manager = main.commandLine(new String[] {"-g:src/test/resources/lines.ixml", input });
            Assertions.assertEquals(1, manager.stringRecords.size());
            Assertions.assertEquals("<lines><l>1&#xD;</l><l>2&#xD;</l><l>3&#xD;</l></lines>", manager.stringRecords.get(0));
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void normalize_line_endings_3() {
        Main main = new Main();
        try {
            String input = "-i:src/test/resources/pc-lines.bin";
            OutputManager manager = main.commandLine(new String[] {"-g:src/test/resources/lines.ixml", "--normalize", input });
            Assertions.assertEquals(1, manager.stringRecords.size());
            Assertions.assertEquals("<lines><l>1</l><l>2</l><l>3</l></lines>", manager.stringRecords.get(0));
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void normalize_line_endings_4() {
        Main main = new Main();
        try {
            String input = "-i:src/test/resources/mixed-lines1.bin";
            OutputManager manager = main.commandLine(new String[] {"-g:src/test/resources/lines.ixml", input });
            Assertions.assertEquals(1, manager.stringRecords.size());
            Assertions.assertEquals("<lines><l>1&#xD;2&#x85;3&#xD;</l></lines>", manager.stringRecords.get(0));
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void normalize_line_endings_5() {
        Main main = new Main();
        try {
            String input = "-i:src/test/resources/mixed-lines1.bin";
            OutputManager manager = main.commandLine(new String[] {"-g:src/test/resources/lines.ixml", "--normalize", input });
            Assertions.assertEquals(1, manager.stringRecords.size());
            Assertions.assertEquals("<lines><l>1</l><l>2</l><l>3</l></lines>", manager.stringRecords.get(0));
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void normalize_line_endings_6() {
        Main main = new Main();
        try {
            String input = "-i:src/test/resources/mixed-lines2.bin";
            OutputManager manager = main.commandLine(new String[] {"-g:src/test/resources/lines.ixml", input });
            Assertions.assertEquals(1, manager.stringRecords.size());
            Assertions.assertEquals("<lines><l>1&#xD;2&#x2028;3</l></lines>", manager.stringRecords.get(0));
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void normalize_line_endings_7() {
        Main main = new Main();
        try {
            String input = "-i:src/test/resources/mixed-lines2.bin";
            OutputManager manager = main.commandLine(new String[] {"-g:src/test/resources/lines.ixml", "--normalize", input });
            Assertions.assertEquals(1, manager.stringRecords.size());
            Assertions.assertEquals("<lines><l>1</l><l>2</l><l>3</l></lines>", manager.stringRecords.get(0));
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void prettyprint() {
        Main main = new Main();
        try {
            String input = "-i:src/test/resources/unix-lines.txt";
            OutputManager manager = main.commandLine(new String[] {"-g:src/test/resources/lines.ixml", "-pp", input });
            Assertions.assertEquals(1, manager.stringRecords.size());
            Assertions.assertEquals("<lines>\n   <l>1</l>\n   <l>2</l>\n   <l>3</l>\n</lines>", manager.stringRecords.get(0));
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void issue_29() {
        // This test is the only test I have that exercises the weird condition on line 128
        // of Family where the GLL parser has returned an oddly formatted SPPF where
        // w.state has only one symbol on the right hand side
        Main main = new Main();
        try {
            String input = "Y bardd ddwylaw";
            OutputManager manager = main.commandLine(new String[] {"-g:src/test/resources/poem.ixml", "--gll", "-pp", input });
            Assertions.assertEquals(1, manager.stringRecords.size());
            Assertions.assertTrue(manager.stringRecords.get(0).contains("<poem"));
        } catch (Exception ex) {
            fail();
        }
    }
}
