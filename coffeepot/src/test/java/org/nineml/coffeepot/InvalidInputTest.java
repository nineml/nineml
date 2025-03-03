package org.nineml.coffeepot;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.nineml.coffeepot.managers.OutputManager;

import static org.junit.jupiter.api.Assertions.fail;

public class InvalidInputTest extends CoffeePotTest {
    @Test
    public void abc_abx_xml() {
        WrappedPrintStream stdout = new WrappedPrintStream();
        WrappedPrintStream stderr = new WrappedPrintStream();
        Main main = new Main(stdout.stream, stderr.stream);
        try {
            OutputManager manager = main.commandLine(new String[] {"-g:src/test/resources/abc.ixml", "abx" });
            Assertions.assertEquals(1, manager.stringRecords.size());
            String failed = manager.stringRecords.get(0);
            Assertions.assertTrue(failed.startsWith("<fail xmlns:ixml='http://invisiblexml.org/NS' ixml:state='failed'><line>1</line><column>2</column><pos>2</pos><unexpected>b</unexpected><permitted>'c'</permitted><also-predicted>'a'</also-predicted>"));
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void abc_abx_json_data() {
        WrappedPrintStream stdout = new WrappedPrintStream();
        WrappedPrintStream stderr = new WrappedPrintStream();
        Main main = new Main(stdout.stream, stderr.stream);
        try {
            OutputManager manager = main.commandLine(new String[] {"-g:src/test/resources/abc.ixml", "--format:json-data", "abx" });
            Assertions.assertEquals(1, manager.stringRecords.size());
            String failed = manager.stringRecords.get(0);
            Assertions.assertTrue(failed.startsWith("{\"fail\":{\"ixml:state\":\"failed\",\"line\":1,\"column\":2,\"pos\":2,\"unexpected\":\"b\",\"permitted\":\"'c'\",\"also-predicted\":\"'a'\""));
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void abc_abx_json_tree() {
        WrappedPrintStream stdout = new WrappedPrintStream();
        WrappedPrintStream stderr = new WrappedPrintStream();
        Main main = new Main(stdout.stream, stderr.stream);
        try {
            OutputManager manager = main.commandLine(new String[] {"-g:src/test/resources/abc.ixml", "--format:json-tree", "abx" });
            Assertions.assertEquals(1, manager.stringRecords.size());
            String failed = manager.stringRecords.get(0);
            Assertions.assertTrue(failed.startsWith("{\"content\":{\"name\":\"fail\",\"attributes\":{\"ixml:state\":\"failed\"},\"content\":[{\"name\":\"line\",\"content\":1},{\"name\":\"column\",\"content\":2},{\"name\":\"pos\",\"content\":2},{\"name\":\"unexpected\",\"content\":\"b\"},{\"name\":\"permitted\",\"content\":\"'c'\"},{\"name\":\"also-predicted\",\"content\":\"'a'\""));
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void abc_abx_csv() {
        WrappedPrintStream stdout = new WrappedPrintStream();
        WrappedPrintStream stderr = new WrappedPrintStream();
        Main main = new Main(stdout.stream, stderr.stream);
        try {
            OutputManager manager = main.commandLine(new String[] {"-g:src/test/resources/abc.ixml", "--format:csv", "abx" });
            Assertions.assertEquals(0, manager.stringRecords.size());
            Assertions.assertTrue(stderr.contains("Result cannot be serialized as CSV: <fail"));
        } catch (Exception ex) {
            fail();
        }
    }

}
