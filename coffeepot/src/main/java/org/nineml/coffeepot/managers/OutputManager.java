package org.nineml.coffeepot.managers;

import net.sf.saxon.s9api.*;
import org.nineml.coffeefilter.InvisibleXml;
import org.nineml.coffeefilter.InvisibleXmlDocument;
import org.nineml.coffeefilter.InvisibleXmlParser;
import org.nineml.coffeefilter.model.IPragma;
import org.nineml.coffeefilter.model.IPragmaCsvColumns;
import org.nineml.coffeefilter.trees.*;
import org.nineml.coffeefilter.trees.StringTreeBuilder;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.trees.*;
import org.nineml.coffeepot.BuildConfig;
import org.nineml.coffeepot.trees.VerboseAxe;
import org.nineml.coffeepot.trees.XdmDataTree;
import org.nineml.coffeepot.trees.XdmSimpleTree;
import org.nineml.coffeepot.utils.NodeUtils;
import org.nineml.coffeepot.utils.ParserOptions;
import org.nineml.coffeesacks.XmlForest;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class OutputManager {
    public static final String logcategory = "CoffeePot";
    private boolean parseError = false;
    public final List<XdmValue> records = new ArrayList<>();
    public final List<String> stringRecords = new ArrayList<>();
    private Configuration config = null;
    public boolean xdmResults = false;
    private int returnCode = 0;
    private Exception thrown = null;
    private int firstParse = -1;
    private int parseCount = -1;
    private long totalParses = -1;
    private boolean infiniteParses = false;
    public Set<Integer> selectedNodes = null;
    public List<TreeSelection> selectedTrees = null;
    private boolean firstResult = true;
    private InputManager inputManager = null;
    private XmlForest forest = null;
    private Axe axe = null;

    public void configure(Configuration config) {
        if (config == null) {
            throw new NullPointerException("OutputManager config must not be null");
        }
        if (this.config != null) {
            throw new IllegalStateException("Cannot configure OutputManager twice");
        }
        this.config = config;
    }

    private void checkConfig() {
        if (config == null) {
            throw new IllegalStateException("Cannot use an unconfigured OutputManager");
        }
    }

    public boolean isConfigured() {
        return config != null;
    }

    public void setReturnCode(int code) {
        returnCode = code;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public void setException(Exception ex) {
        thrown = ex;
    }

    public Exception getException() {
        return thrown;
    }

    public boolean hadParseError() {
        checkConfig();
        return parseError;
    }

    public void setInputManager(InputManager manager) {
        inputManager = manager;
    }

    public void addOutput(InvisibleXmlParser parser, InvisibleXmlDocument doc, String input) {
        checkConfig();

        parseError = parseError || !doc.succeeded();

        if (doc.getNumberOfParses() > 1 || doc.isInfinitelyAmbiguous()) {
            if (!doc.getOptions().isSuppressedState("ambiguous")) {
                if (doc.getNumberOfParses() == 1) {
                    config.stderr.println("Found 1 parse, but the grammar is infinitely ambiguous");
                } else {
                    if (doc.isInfinitelyAmbiguous()) {
                        config.stderr.printf("Found %,d possible parses (of infinitely many).%n", doc.getNumberOfParses());
                    } else {
                        config.stderr.printf("Found %,d possible parses.%n", doc.getNumberOfParses());
                    }
                }
            }
        }

        if (config.parse <= 0) {
            config.options.getLogger().warn(logcategory, "Ignoring absurd parse number: %d", config.parse);
            firstParse = 1;
        } else {
            firstParse = config.parse;
        }

        try {
            forest = new XmlForest(config.processor, doc);
        } catch (SaxonApiException | SAXException ex) {
            throw new RuntimeException(ex);
        }

        if ("random".equals(config.axe)) {
            axe = new RandomAxe();
        } else {
            VerboseAxe vaxe = new VerboseAxe(config, parser, forest, doc, input);
            for (String expr : config.choose) {
                vaxe.addExpression(expr);
            }
            if (config.functionLibrary != null) {
                vaxe.addFunctionLibrary(config.functionLibrary);
            }
            axe = vaxe;
        }


        Arborist walker = doc.getResult().getArborist(axe);
        if (doc.succeeded()) {
            NopTreeBuilder nopBuilder = new NopTreeBuilder();
            for (int pos = 1; pos < firstParse; pos++) {
                if (!walker.hasMoreTrees()) {
                    config.stderr.printf("There are only %d parses.%n", pos - 1);
                    return;
                }
                walker.getTree(nopBuilder);
            }
        }

        parseCount = 0;
        totalParses = doc.getNumberOfParses();
        infiniteParses = doc.isInfinitelyAmbiguous();

        boolean done = false;
        while (!done) {
            if (xdmResults) {
                getXdmResults(parser, doc, walker);
            } else {
                getStringResults(parser, doc, walker);
            }

            if (!config.suppressOutput && config.unbuffered) {
                String record = stringRecords.remove(0);
                System.out.println(record);
            }

            parseCount++;
            if (config.allParses) {
                done = !walker.hasMoreTrees();
            } else {
                done = parseCount == config.parseCount;
            }
        }

        if (parseCount > totalParses) {
            totalParses = parseCount;
        }

        selectedNodes = Collections.unmodifiableSet(walker.getSelectedNodes());
        selectedTrees = Collections.unmodifiableList(walker.getSelectedTrees());
    }

    public void getXdmResults(InvisibleXmlParser parser, InvisibleXmlDocument doc, Arborist walker) {
        checkConfig();

        try {
            ParserOptions opts = new ParserOptions(config.options);
            SimpleTreeBuilder simpleBuilder = null;

            switch (config.outputFormat) {
                case XML:
                    DocumentBuilder builder = config.processor.newDocumentBuilder();
                    BuildingContentHandler handler = builder.newBuildingContentHandler();
                    walker.getTree(doc.getAdapter(handler));
                    records.add(handler.getDocumentNode());
                    break;
                case JSON_DATA:
                    opts.setAssertValidXmlNames(false);
                    opts.setAssertValidXmlCharacters(false);
                    DataTreeBuilder dataBuilder = new DataTreeBuilder(opts);
                    walker.getTree(doc.getAdapter(dataBuilder));
                    XdmDataTree dtree = new XdmDataTree(config, dataBuilder.getTree());
                    records.add(dtree.json());
                    break;
                case JSON_TREE:
                    opts.setAssertValidXmlNames(false);
                    opts.setAssertValidXmlCharacters(false);
                    simpleBuilder = new SimpleTreeBuilder(opts);
                    walker.getTree(doc.getAdapter(simpleBuilder));
                    XdmSimpleTree stree = new XdmSimpleTree(config, simpleBuilder.getTree());
                    records.add(stree.json());
                    break;
                case CSV:
                    opts.setAssertValidXmlNames(false);
                    opts.setAssertValidXmlCharacters(false);
                    DataTreeBuilder csvBuilder = new DataTreeBuilder(opts);
                    walker.getTree(doc.getAdapter(csvBuilder));
                    XdmDataTree csvtree = new XdmDataTree(config, csvBuilder.getTree());
                    records.add(csvtree.csv());
                    break;
                default:
                    throw new RuntimeException("Unexpected output format!?");
            }
        } catch (SaxonApiException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void getStringResults(InvisibleXmlParser parser, InvisibleXmlDocument doc, Arborist walker) {
        checkConfig();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(baos);

        DataTreeBuilder dataBuilder;
        SimpleTreeBuilder simpleBuilder;
        DataTree dataTree;
        SimpleTree simpleTree;

        ParserOptions opts = new ParserOptions(config.options);

        switch (config.outputFormat) {
            case XML:
                StringTreeBuilder handler = new StringTreeBuilder(opts, out);
                if  (firstResult && config.options.getProvenance() && inputManager.records.size() == 1) {
                    out.print(provenance());
                }
                firstResult = false;
                if (doc.succeeded()) {
                    walker.getTree(doc.getAdapter(handler));
                } else {
                    doc.getTree(handler);
                }
                break;
            case JSON_DATA:
                opts.setAssertValidXmlNames(false);
                opts.setAssertValidXmlCharacters(false);
                dataBuilder = new DataTreeBuilder(opts);
                if (doc.succeeded()) {
                    walker.getTree(doc.getAdapter(dataBuilder));
                } else {
                    doc.getTree(dataBuilder);
                }
                dataTree = dataBuilder.getTree();
                out.print(dataTree.asJSON());
                break;
            case JSON_TREE:
                opts.setAssertValidXmlNames(false);
                opts.setAssertValidXmlCharacters(false);
                simpleBuilder = new SimpleTreeBuilder(opts);
                if (doc.succeeded()) {
                    walker.getTree(doc.getAdapter(simpleBuilder));
                } else {
                    doc.getTree(simpleBuilder);
                }
                simpleTree = simpleBuilder.getTree();
                out.print(simpleTree.asJSON());
                break;
            case CSV:
                opts.setAssertValidXmlNames(false);
                opts.setAssertValidXmlCharacters(false);
                dataBuilder = new DataTreeBuilder(opts);
                if (doc.succeeded()) {
                    walker.getTree(doc.getAdapter(dataBuilder));
                    dataTree = dataBuilder.getTree();
                    List<CsvColumn> columns = dataTree.prepareCsv();

                    if (columns == null) {
                        walker.reset();
                        StringTreeBuilder shandler = new StringTreeBuilder(opts, out);
                        walker.getTree(doc.getAdapter(shandler));
                        try {
                            config.stderr.println("Result cannot be serialized as CSV: " + baos.toString("UTF-8"));
                            returnCode = 1;
                        } catch (UnsupportedEncodingException ex) {
                            // This can't happen.
                        }
                        return;
                    }

                    // Are the csv-columns defined in the grammar?
                    List<String> colnames = null;
                    for (IPragma pragma : parser.getPragmas()) {
                        if (pragma instanceof IPragmaCsvColumns) {
                            colnames = ((IPragmaCsvColumns) pragma).columns;
                        }
                    }

                    if (colnames != null) {
                        List<CsvColumn> updatedColumns = new ArrayList<>();
                        for (String name : colnames) {
                            boolean found = false;
                            for (CsvColumn column : columns) {
                                if (column.getName().equals(name)) {
                                    updatedColumns.add(column);
                                    found = true;
                                }
                            }
                            if (!found) {
                                config.stderr.printf("No column named %s in data%n", name);
                            }
                        }
                        if (updatedColumns.isEmpty()) {
                            config.stderr.println("No columns selected");
                            returnCode = 1;
                            return;
                        }
                        columns = updatedColumns;
                    }

                    // Are there headers defined for the columns being output?
                    Grammar grammar = parser.getGrammar();
                    for (CsvColumn column : columns) {
                        for (Rule rule : grammar.getRules()) {
                            String name = rule.symbol.getName();
                            if (rule.getSymbol().hasAttribute(InvisibleXml.NAME_ATTRIBUTE)) {
                                name = rule.getSymbol().getAttributeValue(InvisibleXml.NAME_ATTRIBUTE, name);
                            }

                            if (column.getName().equals(name)) {
                                String heading = rule.getSymbol().getAttributeValue(InvisibleXml.CSV_HEADING_ATTRIBUTE, null);
                                if (heading != null) {
                                    column.setHeader(heading);
                                }
                            }
                        }
                    }

                    out.print(dataTree.asCSV(columns, config.omitCsvHeaders));
                } else {
                    StringTreeBuilder sbuilder = new StringTreeBuilder(doc.getOptions());
                    doc.getTree(sbuilder);
                    config.stderr.print("Result cannot be serialized as CSV: ");
                    config.stderr.println(sbuilder.getXml());
                    return;
                }

                break;
            default:
                throw new RuntimeException("Unexpected output format!?");
        }

        if (config.suppressOutput) {
            return;
        }

        try {
            String output = baos.toString("UTF-8");
            stringRecords.add(output);
        } catch (UnsupportedEncodingException ex) {
            // This can't happen.
        }
    }

    private String provenance() {
        StringBuilder sb = new StringBuilder();
        sb.append("<!-- NineML version ").append(BuildConfig.VERSION);
        sb.append(" at ");
        sb.append(ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS).format( DateTimeFormatter.ISO_INSTANT ));
        int size = inputManager.inputSize;
        String units = "b";
        if (size > 1024) {
            size = size / 1024;
            units = "k";
        }
        if (size > 1000) {
            size = size / 1000;
            units = "m";
        }
        sb.append("\n     Parsed ").append(String.format("%,d", size)).append(units);
        if (config.inputFile != null) {
            sb.append(" from ").append(config.inputFile);
        }
        if (config.grammar != null) {
            sb.append("\n     Using ").append(config.grammar);
        }
        if (inputManager.records.size() != 1) {
            sb.append("\n     (Input processed as ").append(inputManager.records.size()).append(" records)");
        }
        sb.append("\n-->\n");
        return sb.toString();
    }

    public void publish() throws IOException {
        checkConfig();

        if (config.suppressOutput || config.unbuffered) {
            return;
        }

        PrintStream output = config.stdout;
        if (config.outputFile != null) {
            output = new PrintStream(Files.newOutputStream(Paths.get(config.outputFile)));
        }
        output.print(publication());
    }

    public String publication() {
        checkConfig();

        if (config.suppressOutput) {
            return "";
        }

        if (xdmResults) {
            return publishXdm();
        } else {
            return publishStrings();
        }
    }

    private String publishXdm() {
        throw new IllegalStateException("XDM results output not implemented");
    }

    private String publishStrings() {
        StringBuilder sb = new StringBuilder();

        final String root = config.records ? "records" : "ixml";

        boolean outputJSON = config.outputFormat == Configuration.OutputFormat.JSON_DATA
                || config.outputFormat == Configuration.OutputFormat.JSON_TREE;

        if (stringRecords.size() > 1) {
            if (outputJSON) {
                sb.append("{\n");
                if (parseError) {
                    sb.append("  \"ixml:state\": \"failed\",\n");
                }
                if (firstParse != 1) {
                    sb.append("  \"firstParse\": ").append(firstParse).append(",\n");
                }
                if (parseCount > 1 || totalParses > 1) {
                    sb.append("  \"parses\": ").append(parseCount).append(",\n");
                    sb.append("  \"totalParses\": ").append(totalParses).append(",\n");
                }
                if (infiniteParses) {
                    sb.append("  \"infinitelyAmbiguous\": true,");
                }
                sb.append("  \"records\": [\n");
            } else {
                if  (config.options.getProvenance()) {
                    sb.append(provenance());
                }
                sb.append("<").append(root);
                if (firstParse != 1) {
                    sb.append(" firstParse=\"").append(firstParse).append("\"");
                }
                if (parseCount > 1 || totalParses > 1) {
                    sb.append(" parses=\"").append(parseCount).append("\"");
                    sb.append(" totalParses=\"").append(totalParses).append("\"");
                }
                if (infiniteParses) {
                    sb.append(" infinitelyAmbiguous=\"true\"");
                }
                if (parseError) {
                    sb.append(" xmlns:ixml=\"http://invisiblexml.org/NS\" ixml:state=\"failed\"");
                }
                sb.append(">\n");
            }
        }

        for (int pos = 0; pos < stringRecords.size(); pos++) {
            sb.append(stringRecords.get(pos));
            if (pos+1 < stringRecords.size()) {
                if (outputJSON) {
                    sb.append(",\n");
                }
            }
        }

        if (stringRecords.size() > 1) {
            if (outputJSON) {
                sb.append("]\n}\n");
            } else {
                sb.append("</").append(root).append(">\n");
            }
        }

        return sb.toString();
    }

    public void describeAmbiguity(PrintStream stderr) {
        final AmbiguityDescription desc;
        switch (config.describeAmbiguityWith) {
            case "text":
                desc = new TextDescription();
                break;
            case "xml":
                desc = new XmlDescription();
                break;
            default:
                desc = new AmbiguityDescription();
                break;
        }
        desc.describe(stderr, selectedTrees);
    }

    public XdmNode getAmbiguityContext(Family choice) {
        try {
            XPathCompiler compiler = config.processor.newXPathCompiler();
            XPathExecutable exec = compiler.compile(String.format("//children[@id='C%d']/parent::*", choice.id));
            XPathSelector selector = exec.load();
            selector.setContextItem(forest.getXml());
            XdmNode parentNode = (XdmNode) selector.evaluateSingle();
            if (parentNode == null) {
                exec = compiler.compile(String.format("//*[@id='N%d']", choice.id));
                selector = exec.load();
                selector.setContextItem(forest.getXml());
                parentNode = (XdmNode) selector.evaluateSingle();
            }
            return parentNode;
        } catch (SaxonApiException ex) {
            throw new RuntimeException(ex);
        }
    }

    private class AmbiguityDescription {
        final String nocheck = " ";
        final String check;
        final String lquo;
        final String rquo;
        final String epsilon;

        public AmbiguityDescription() {
            if (config.options.getAsciiOnly()) {
                check = "X";
                lquo = "(";
                rquo = ")";
                epsilon = "e";
            } else {
                check = "✔";
                lquo = "«";
                rquo = "»";
                epsilon = "ε";
            }
        }

        protected String path(ParseTree branch) {
            Stack<Symbol> ancestors = new Stack<>();
            while (branch.parent != null) {
                if (branch.vertex.node.symbol != null
                        && !branch.vertex.node.symbol.getAttributeValue(InvisibleXml.MARK_ATTRIBUTE, "^").equals("-")) {
                    ancestors.push(branch.vertex.node.symbol);
                }
                branch = branch.parent;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("At ");
            if (ancestors.isEmpty()) {
                sb.append("/");
            } else {
                while (!ancestors.isEmpty()) {
                    sb.append("/");
                    sb.append(ancestors.pop());
                }
            }
            return sb.toString();
        }

        public void describe(PrintStream stderr, List<TreeSelection> trees) {
            // nop;
        }
    }

    private class TextDescription extends AmbiguityDescription {
        @Override
        public void describe(PrintStream stderr, List<TreeSelection> trees) {
            for (TreeSelection selection : trees) {
                stderr.println(path(selection.parent));

                for (Family choice : selection.node.getFamilies()) {
                    StringBuilder sb = new StringBuilder();
                    if (choice.equals(selection.selection)) {
                        sb.append("    ").append(check).append(" ");
                    } else {
                        sb.append("    ").append(nocheck).append(" ");
                    }
                    textShowChoice(sb, choice);
                    stderr.println(sb);
                }
            }
        }

        private void textShowChoice(StringBuilder sb, Family choice) {
            textShowSide(sb, choice.getLeftNode());
            if (choice.getLeftNode() != null && choice.getRightNode() != null) {
                sb.append(", ");
            }
            textShowSide(sb, choice.getRightNode());
        }

        private void textShowSide(StringBuilder sb, ForestNode node) {
            if (node == null) {
                return;
            }

            if (node.symbol == null) {
                boolean first = true;
                sb.append("[");
                for (Symbol s : node.state.rhs.symbols) {
                    if (!first) {
                        sb.append(", ");
                    }
                    sb.append(s);
                    first = false;
                }
                sb.append("]");
            } else {
                sb.append(node.symbol);
            }

            if (node.leftExtent == node.rightExtent) {
                sb.append(lquo).append(epsilon).append(rquo);
            } else {
                sb.append(lquo).append(node.leftExtent+1).append("-").append(node.rightExtent).append(rquo);
            }
        }
    }

    private class XmlDescription extends AmbiguityDescription {
        private final QName _indent = new QName("indent");
        private final QName _omit = new QName("omit-xml-declaration");
        @Override
        public void describe(PrintStream stderr, List<TreeSelection> trees) {
            for (TreeSelection selection : trees) {
                stderr.println(path(selection.parent));
                XdmNode context = NodeUtils.getAmbiguityContext(config.processor, forest, selection.selection);

                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    Serializer serializer = config.processor.newSerializer(baos);
                    serializer.setOutputProperty(_indent, "yes");
                    serializer.setOutputProperty(_omit, "yes");
                    serializer.serialize(context.getUnderlyingNode());
                    String text = baos.toString();

                    // Crude!
                    String cid = String.format("<children id=\"C%d\"", selection.selection.id);
                    String nid = String.format(" id=\"N%d\"", selection.selection.id);
                    if (text.contains(cid)) {
                        nid = "<<THIS NEVER OCCURS>>";
                    } else {
                        cid = "<<THIS NEVER OCCURS>>";
                    }

                    for (String line : text.split("\\n")) {
                        final String indent;
                        if (line.contains(cid) || line.contains(nid)) {
                            indent = String.format("<!-- %s --> ", check);
                        } else {
                            indent = "           ";
                        }

                        stderr.printf("%s%s%n", indent, line);
                    }
                } catch (SaxonApiException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }
}
