package org.nineml.coffeesacks;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.CallableFunction;
import net.sf.saxon.functions.hof.UserFunctionReference;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.s9api.*;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AtomicIterator;
import net.sf.saxon.type.FunctionItemType;
import net.sf.saxon.type.SpecificFunctionType;
import net.sf.saxon.value.*;
import net.sf.saxon.value.SequenceType;
import org.nineml.coffeefilter.InvisibleXml;
import org.nineml.coffeefilter.InvisibleXmlDocument;
import org.nineml.coffeefilter.InvisibleXmlParser;
import org.nineml.coffeefilter.ParserOptions;
import org.nineml.coffeefilter.trees.*;
import org.nineml.coffeegrinder.trees.Arborist;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.transform.sax.SAXSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Superclass for the CoffeeSacks functions containing some common definitions.
 */
public abstract class CommonDefinition extends ExtensionFunctionDefinition {
    public static final String logcategory = "CoffeeSacks";

    protected static final String _encoding = "encoding";
    protected static final String _type = "type";
    protected static final String _format = "format";

    private static final QName _json = new QName("", "json");

    public static int FOLLOW_REDIRECT_LIMIT = 100;
    protected final ParserOptions parserOptions;
    protected InvisibleXml invisibleXml = null;
    protected final Configuration config;
    protected URI baseURI = null;
    protected Location sourceLoc = null;

    public CommonDefinition(Configuration config) {
        parserOptions = new ParserOptions();
        parserOptions.setLogger(new SacksLogger(config.getLogger()));
        this.config = config;
    }

    protected InvisibleXmlParser parserFromURI(XPathContext context, URI grammarURI, ParserOptions popts, Map<String,String> options) throws XPathException {
        try {
            invisibleXml = new InvisibleXml(popts);
            final InvisibleXmlParser parser;

            if (options.containsKey(_type)) {
                String grammarType = options.get(_type);
                URLConnection conn = getUrlConnection(grammarURI);
                if ("ixml".equals(grammarType)) {
                    String encoding = options.getOrDefault(_encoding, "UTF-8");
                    parser = invisibleXml.getParserFromIxml(conn.getInputStream(), encoding);
                } else if ("xml".equals(grammarType) || "vxml".equals(grammarType)) {
                    parser = invisibleXml.getParserFromVxml(conn.getInputStream(), grammarURI.toString());
                } else {
                    throw new IllegalArgumentException("Unexpected grammar type: " + grammarType);
                }
            } else {
                parser = invisibleXml.getParser(grammarURI);
            }

            return parser;
        } catch (Exception ex) {
            throw new XPathException(ex);
        }
    }

    private URLConnection getUrlConnection(URI grammarURI) throws IOException, CoffeeSacksException {
        URLConnection conn = grammarURI.toURL().openConnection();
        String location = conn.getHeaderField("location");
        if (location != null) {
            HashSet<String> seenLocations = new HashSet<>();
            while (location != null) {
                if (seenLocations.contains(location)) {
                    throw new CoffeeSacksException(CoffeeSacksException.ERR_HTTP_INF_LOOP, "Redirect loop", sourceLoc, new AnyURIValue(grammarURI.toString()));
                }
                if (seenLocations.size() > FOLLOW_REDIRECT_LIMIT) {
                    throw new CoffeeSacksException(CoffeeSacksException.ERR_HTTP_LOOP, "Redirect limit exceeded", sourceLoc, new AnyURIValue(grammarURI.toString()));
                }
                seenLocations.add(location);
                URL loc = new URL(location);
                conn = loc.openConnection();
                location = conn.getHeaderField("location");
            }
        }
        return conn;
    }

    protected InvisibleXmlParser parserFromString(XPathContext context, String grammarString, ParserOptions popts, Map<String,String> options) throws XPathException {
        invisibleXml = new InvisibleXml(popts);
        final InvisibleXmlParser parser;

        if (options.containsKey(_type)) {
            String grammarType = options.get(_type);
            if ("ixml".equals(grammarType)) {
                parser = invisibleXml.getParserFromIxml(grammarString);
            } else {
                throw new IllegalArgumentException("Only ixml grammars can be parsed from strings: " + grammarType);
            }
        } else {
            parser = invisibleXml.getParserFromIxml(grammarString);
        }

        return parser;
    }

    protected InvisibleXmlParser parserFromXml(XPathContext context, NodeInfo grammar, ParserOptions popts, Map<String,String> options) throws XPathException {
        invisibleXml = new InvisibleXml(popts);
        final InvisibleXmlParser parser;

        try {
            // This can be made more efficient if/when CoffeeFilter accepts a node directly...
            Processor processor = (Processor) context.getConfiguration().getProcessor();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Serializer serializer = processor.newSerializer(baos);
            serializer.serialize(grammar.asActiveSource());
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            parser = invisibleXml.getParserFromVxml(bais, grammar.getBaseURI());
        } catch (IOException | SaxonApiException ex) {
            throw new XPathException(ex);
        }

        return parser;
    }

    protected Sequence functionFromParser(XPathContext context,
                                          InvisibleXmlParser parser,
                                          UserFunctionReference.BoundUserFunction chooseAlternative,
                                          Map<String, String> options) throws XPathException {
        if (parser.constructed()) {
            SequenceType[] argTypes = new SequenceType[]{SequenceType.SINGLE_STRING};

            final FunctionItemType ftype;
            if ("xml".equals(options.getOrDefault(_format, "xml"))) {
                ftype = new SpecificFunctionType(argTypes, SequenceType.SINGLE_NODE);
            } else {
                ftype = new SpecificFunctionType(argTypes, SequenceType.SINGLE_ITEM);
            }

            if (chooseAlternative != null) {
                FunctionItemType ctype = chooseAlternative.getFunctionItemType();
                if (ctype.getResultType().getPrimaryType() != MapType.ANY_MAP_TYPE) {
                    throw new CoffeeSacksException(CoffeeSacksException.ERR_INVALID_CHOOSE_FUNCTION, "The choose-alternative function must return a map");
                }
                if (ctype.getArgumentTypes().length != 2) {
                    throw new CoffeeSacksException(CoffeeSacksException.ERR_INVALID_CHOOSE_FUNCTION, "The choose-alternative function must have two arguments");
                }
                if (ctype.getArgumentTypes()[0].getPrimaryType() != NodeKindTest.ELEMENT) {
                    throw new CoffeeSacksException(CoffeeSacksException.ERR_INVALID_CHOOSE_FUNCTION, "The first argument to the choose-alternative function must be an element");
                }
                if (ctype.getArgumentTypes()[1].getPrimaryType() != MapType.ANY_MAP_TYPE) {
                    throw new CoffeeSacksException(CoffeeSacksException.ERR_INVALID_CHOOSE_FUNCTION, "The second argument to the choose-alternative function must be a map");
                }
            }

            return new CallableFunction(1, new InvisibleXmlParserFunction(parser, chooseAlternative, options), ftype);
        } else {
            try {
                InvisibleXmlDocument failed = parser.getFailedParse();
                if (failed.getResult().succeeded()) {
                    // we never even tried to parse apparently
                    throw new CoffeeSacksException(CoffeeSacksException.ERR_INVALID_GRAMMAR, "Failed to parse grammar",
                            sourceLoc, new XdmAtomicValue(parser.getException().getMessage()).getUnderlyingValue());
                } else {
                    ByteArrayInputStream bais = new ByteArrayInputStream(failed.getTree().getBytes(StandardCharsets.UTF_8));
                    SAXSource source = new SAXSource(new InputSource(bais));
                    Processor processor = (Processor) context.getConfiguration().getProcessor();
                    DocumentBuilder builder = processor.newDocumentBuilder();
                    XdmNode errdoc = builder.build(source);
                    throw new CoffeeSacksException(CoffeeSacksException.ERR_INVALID_GRAMMAR, "Failed to parse grammar",
                            sourceLoc, errdoc.getUnderlyingNode());
                }
            } catch (SaxonApiException ex) {
                throw new XPathException(ex);
            }
        }
    }

    protected HashMap<String,Object> parseMap(MapItem item) throws XPathException {
        HashMap<String,Object> options = new HashMap<>();

        // The implementation of the keyValuePairs() method is incompatible between Saxon 10 and Saxon 11.
        // In order to avoid having to publish two versions of this class, we use reflection to
        // work it out at runtime. (Insert programmer barfing on his shoes emoji here.)
        try {
            Method keys = MapItem.class.getMethod("keys");
            Method get = MapItem.class.getMethod("get", AtomicValue.class);
            AtomicIterator aiter = (AtomicIterator) keys.invoke(item);
            AtomicValue next = aiter.next();
            while (next != null) {
                String key = next.getStringValue();
                if ("choose-alternative".equals(key)) {
                    UserFunctionReference.BoundUserFunction ref = (UserFunctionReference.BoundUserFunction) get.invoke(item, next);
                    options.put(key, ref);
                } else {
                    GroundedValue value = (GroundedValue) get.invoke(item, next);
                    // Ignore options that are set to the empty sequence
                    if (value.getLength() > 0) {
                        options.put(key, ((AtomicValue) value).getStringValue());
                    }
                }
                next = aiter.next();
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new IllegalArgumentException("Failed to resolve MapItem with reflection");
        }

        return options;
    }

    protected ParserOptions checkOptions(Map<String,String> options) throws XPathException {
        ParserOptions parserOptions = new ParserOptions(this.parserOptions);

        Set<String> booleanOptions = new HashSet<>(Arrays.asList(
                "ignore-trailing-whitespace",  "ignoreTrailingWhitespace",
                "allow-undefined-symbols",     "allowUndefinedSymbols",
                "allow-unreachable-symbols",   "allowUnreachableSymbols",
                "allow-unproductive-symbols",  "allowUnproductiveSymobls",
                "allow-multiple-definitions",  "allowMultipleDefinitions",
                "show-marks",                  "showMarks",
                "show-bnf-nonterminals",       "showBnfNonterminals",
                                               "suppressAmbiguousState",
                                               "suppressPrefixState",
                "strict-ambiguity",            "strictAmbiguity",
                "ignore-bom",
                "normalize-line-endings",
                "mark-ambiguities",
                "pedantic"
        ));

        Set<String> stringOptions = new HashSet<>(Arrays.asList(
                "parser-type",                 "parser",
                "priority-style",
                "disable-pragmas",
                "enable-pragmas",
                "disable-states",
                "enable-states",
                "default-log-level",
                "log-levels",
                "start-symbol",
                "format",
                "type"
        ));

        // We want to make sure we have an appropriate logger.
        parserOptions.setLogger(new SacksLogger(config.getLogger()));

        if (options.containsKey("default-log-level")) {
            parserOptions.getLogger().setDefaultLogLevel(options.get("default-log-level"));
        }
        if (options.containsKey("log-levels")) {
            parserOptions.getLogger().setLogLevels(options.get("log-levels"));
        }

        for (String key : options.keySet()) {
            final String value = options.get(key);
            final boolean bool;

            if (booleanOptions.contains(key)) {
                if ("true".equals(value) || "yes".equals(value) || "1".equals(value)) {
                    bool = true;
                } else if ("false".equals(value) || "no".equals(value) || "0".equals(value)) {
                    bool = false;
                } else {
                    parserOptions.getLogger().warn(logcategory, "Ignoring invalid option value: %s=%s", key, value);
                    continue;
                }
            } else {
                bool = false; // irrelevant but makes the compiler happy
                if (!stringOptions.contains(key)) {
                    parserOptions.getLogger().warn(logcategory, "Ignoring invalid option: %s=%s", key, value);
                    continue;
                }
            }

            switch (key) {
                case "ignore-trailing-whitespace":
                case "ignoreTrailingWhitespace":
                    parserOptions.setIgnoreTrailingWhitespace(bool);
                    break;
                case "allow-undefined-symbols":
                case "allowUndefinedSymbols":
                    parserOptions.setAllowUndefinedSymbols(bool);
                    break;
                case "allow-unreachable-symbols":
                case "allowUnreachableSymbols":
                    parserOptions.setAllowUnreachableSymbols(bool);
                    break;
                case "allow-unproductive-symbols":
                case "allowUnproductiveSymbols":
                    parserOptions.setAllowUnproductiveSymbols(bool);
                    break;
                case "allow-multiple-definitions":
                case "allowMultipleDefinitions":
                    parserOptions.setAllowMultipleDefinitions(bool);
                    break;
                case "show-marks":
                case "showMarks":
                    parserOptions.setShowMarks(bool);
                    break;
                case "show-bnf-nonterminals":
                case "showBnfNonterminals":
                    parserOptions.setShowBnfNonterminals(bool);
                    break;
                case "suppressAmbiguousState":
                    if (bool) {
                        parserOptions.suppressState("ambiguous");
                    } else {
                        parserOptions.exposeState("ambiguous");
                    }
                    break;
                case "suppressPrefixState":
                    if (bool) {
                        parserOptions.suppressState("prefix");
                    } else {
                        parserOptions.exposeState("prefix");
                    }
                    break;
                case "strict-ambiguity":
                case "strictAmbiguity":
                    parserOptions.setStrictAmbiguity(bool);
                    break;
                case "ignore-bom":
                    parserOptions.setIgnoreBOM(bool);
                    break;
                case "normalize-line-endings":
                    parserOptions.setNormalizeLineEndings(bool);
                    break;
                case "mark-ambiguities":
                    parserOptions.setMarkAmbiguities(bool);
                    break;
                case "pedantic":
                    parserOptions.setPedantic(bool);
                    break;
                case "parser-type":
                case "parser":
                    parserOptions.setParserType(value);
                    break;
                case "priority-style":
                    parserOptions.setPriorityStyle(value);
                    break;
                case "start-symbol":
                    parserOptions.setStartSymbol(value);
                    break;
                case "disable-pragmas":
                case "disablePragmas":
                    break; // see below
                case "enable-pragmas":
                case "enablePragmas":
                    break; // see below
                case "disable-states":
                case "enable-states":
                    break; // see below
                case "default-log-level":
                    break; // see above
                case "log-levels":
                    break; // see above

                case "format":
                    Set<String> formats = new HashSet<>(Arrays.asList("xml", "json", "json-data", "json-tree", "json-text"));
                    if (!formats.contains(value)) {
                        throw new CoffeeSacksException(CoffeeSacksException.ERR_BAD_OUTPUT_FORMAT, "Invalid output format",
                                sourceLoc, new StringValue(value));
                    }
                    break;
                case "type":
                    Set<String> types = new HashSet<>(Arrays.asList("ixml", "xml", "vxml"));
                    if (!types.contains(value)) {
                        throw new CoffeeSacksException(CoffeeSacksException.ERR_BAD_INPUT_FORMAT, "Invalid input format",
                                sourceLoc, new StringValue(value));
                    }
                    break;
                default:
                    parserOptions.getLogger().warn(logcategory, "Ignoring unexpected option: %s", key);
            }
        }

        // Disable first,
        String value = options.getOrDefault("disable-pragmas", options.getOrDefault("disablePragmas", null));
        if (value != null) {
            for (String name : value.split(",\\s*")) {
                parserOptions.disablePragma(name.trim());
            }
        }
        // then enable
        value = options.getOrDefault("enable-pragmas", options.getOrDefault("enablePragmas", null));
        if (value != null) {
            for (String name : value.split(",\\s*")) {
                parserOptions.enablePragma(name.trim());
            }
        }

        // Disable first,
        value = options.getOrDefault("disable-states", null);
        if (value != null) {
            for (String name : value.split(",\\s*")) {
                parserOptions.suppressState(name.trim());
            }
        }
        // then enable
        value = options.getOrDefault("enable-states",null);
        if (value != null) {
            for (String name : value.split(",\\s*")) {
                parserOptions.exposeState(name.trim());
            }
        }

        return parserOptions;
    }

    protected Item jsonToXDM(Processor processor, String json) throws SaxonApiException {
        // This also really isn't very nice
        XPathCompiler compiler = processor.newXPathCompiler();
        compiler.declareVariable(_json);
        XPathExecutable exec = compiler.compile("parse-json($json)");
        XPathSelector selector = exec.load();
        selector.setVariable(_json, new XdmAtomicValue(json));
        XdmSequenceIterator<XdmItem> iter = selector.iterator();
        XdmItem item = iter.next();
        return item.getUnderlyingValue();
    }

    private class InvisibleXmlParserFunction implements Callable {
        private final InvisibleXmlParser parser;
        private final UserFunctionReference.BoundUserFunction chooseAlternative;
        private final Map<String,String> options;

        public InvisibleXmlParserFunction(InvisibleXmlParser parser, UserFunctionReference.BoundUserFunction chooseAlternative, Map<String, String> options) {
            this.parser = parser;
            this.chooseAlternative = chooseAlternative;
            this.options = options;
        }

        @Override
        public Sequence call(XPathContext context, Sequence[] sequences) throws XPathException {
            String input = sequences[0].head().getStringValue();
            String format = options.getOrDefault(_format, "xml");

            try {
                Processor processor = (Processor) context.getConfiguration().getProcessor();
                InvisibleXmlDocument document = parser.parse(input);

                final XmlForest forest;
                try {
                    forest = new XmlForest(processor, document);
                } catch (SaxonApiException | SAXException ex) {
                    throw new XPathException(ex);
                }

                XPathAxe axe = new XPathAxe(processor, parser, forest, input);
                axe.setChooseFunction(context, chooseAlternative);

                Arborist arborist = document.getResult().getArborist(axe);

                if ("xml".equals(format)) {
                    DocumentBuilder docBuilder = processor.newDocumentBuilder();
                    BuildingContentHandler handler = docBuilder.newBuildingContentHandler();
                    arborist.getTree(document.getAdapter(handler));
                    return handler.getDocumentNode().getUnderlyingNode();
                }

                String json;
                ParserOptions newOptions = new ParserOptions();
                newOptions.setAssertValidXmlNames(false);
                if ("json-tree".equals(format) || "json-text".equals(format)) {
                    SimpleTreeBuilder simpleBuilder = new SimpleTreeBuilder(newOptions);
                    arborist.getTree(document.getAdapter(simpleBuilder));
                    SimpleTree tree = simpleBuilder.getTree();
                    json = tree.asJSON();
                } else {
                    DataTreeBuilder dataBuilder = new DataTreeBuilder(newOptions);
                    arborist.getTree(document.getAdapter(dataBuilder));
                    DataTree tree = dataBuilder.getTree();
                    json = tree.asJSON();
                }

                return jsonToXDM(processor, json);
            } catch (Exception ex) {
                throw new XPathException(ex);
            }
        }
    }
}
