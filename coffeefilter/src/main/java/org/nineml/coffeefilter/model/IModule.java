package org.nineml.coffeefilter.model;

import net.sf.saxon.s9api.*;
import org.nineml.coffeefilter.InvisibleXml;
import org.nineml.coffeefilter.InvisibleXmlDocument;
import org.nineml.coffeefilter.InvisibleXmlParser;
import org.nineml.coffeefilter.ParserOptions;
import org.nineml.coffeefilter.util.AttributeBuilder;
import org.nineml.coffeefilter.util.IxmlInputBuilder;
import org.nineml.coffeegrinder.tokens.Token;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URI;
import java.net.URLConnection;
import java.util.*;

public class IModule extends XNonterminal {
    private static final QName _source = new QName("source");
    private static final QName _uses = new QName("uses");
    private static final QName _shares = new QName("shares");
    private static final QName _ixml = new QName("ixml");
    private static final QName _rule = new QName("rule");
    private static final QName _name = new QName("name");
    private static final QName _alias = new QName("alias");
    private static final QName _nonterminal = new QName("nonterminal");

    private final InvisibleXml invisibleXml;
    private final URI baseUri;
    protected final ParserOptions options;
    private final Set<String> sharedSymbols = new HashSet<>();
    private Ixml ixml = null;
    private boolean firstRule = true;

    protected IModule(ParserOptions options, InvisibleXml invisibleXml, URI baseUri) {
        super(null, "module", "$$_module");
        this.invisibleXml = invisibleXml;
        this.baseUri = baseUri;
        this.options = options;
    }

    @Override
    protected XNode copy() {
        IModule module = new IModule(options, invisibleXml, baseUri);
        module.copyChildren(getChildren());
        return module;
    }

    public Ixml getIxml() {
        if (ixml != null) {
            return ixml;
        }

        XNode ixmlChild = null;

        boolean modular = false;
        for (XNode child : children) {
            if ("uses".equals(child.nodeName) || "shares".equals(child.nodeName)) {
                modular = true;
            } else if ("ixml".equals(child.nodeName)) {
                ixmlChild = child;
            }
        }

        if (ixmlChild == null) {
            // Assume we aren't using the modular grammar...
            ixml = new Ixml(options);
            for (XNode child : children) {
                child.parent = ixml;
                ixml.children.add(child);
            }
            ixml.simplifyGrammar(options);
            return ixml;
        }

        if (!modular) {
            ixml = new Ixml(options);
            List<XNode> rules = new ArrayList<>();
            for (XNode child : children) {
                if ("ixml".equals(child.nodeName)) {
                    rules.addAll(ixmlChild.children);
                } else {
                    rules.add(child);
                }
            }
            for (XNode rule : rules) {
                rule.parent = ixml;
                ixml.children.add(rule);
            }

            ixml.simplifyGrammar(options);
            return ixml;
        }

        return composeGrammar();
    }

    private Ixml composeGrammar() {
        // Only used if we are expanding modular grammars
        Processor processor = new Processor(false);

        try {
            Map<URI, ResolvedGrammar> grammarsParsed = new HashMap<>();
            List<URI> grammarsToParse = new ArrayList<>();
            grammarsToParse.add(baseUri);
            while (!grammarsToParse.isEmpty()) {
                URI uri = grammarsToParse.remove(0);
                XdmNode doc = parseGrammar(processor, uri);
                grammarsParsed.put(uri, new ResolvedGrammar(doc));

                XPathCompiler compiler = processor.newXPathCompiler();
                XPathExecutable exec = compiler.compile("/module/uses/from[@source]");
                XPathSelector selector = exec.load();
                selector.setContextItem(doc);
                for (XdmItem item : selector) {
                    XdmNode node = (XdmNode) item;
                    URI guri = node.getBaseURI().resolve(node.getAttributeValue(_source));
                    if (!grammarsParsed.containsKey(guri) && !grammarsToParse.contains(guri)) {
                        grammarsToParse.add(guri);
                    }
                }
            }

            for (ResolvedGrammar grammar : grammarsParsed.values()) {
                grammar.makeTree(grammarsParsed);
            }

            grammarsParsed.get(baseUri).walkTree(grammarsParsed);

            // This is (probably) temporary
            DocumentBuilder docBuilder = processor.newDocumentBuilder();
            BuildingContentHandler bch = docBuilder.newBuildingContentHandler();
            makeNewGrammar(bch, grammarsParsed);
            XdmNode composedGrammar = bch.getDocumentNode();
            options.getLogger().debug("modularity", "Composed grammar:\n%s", composedGrammar);

            IxmlContentHandler builder = new IxmlContentHandler(invisibleXml, baseUri.toString());
            makeNewGrammar(builder, grammarsParsed);

            return builder.getModule().getIxml();
        } catch (SaxonApiException | SAXException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void makeNewGrammar(ContentHandler builder, Map<URI, ResolvedGrammar> grammarsParsed) throws SaxonApiException, SAXException {
        builder.startDocument();
        builder.startElement("", "module", "module", new AttributeBuilder(invisibleXml.getOptions()));
        firstRule = true;

        grammarsParsed.get(baseUri).compose(builder, grammarsParsed, true);
        for (URI uri : grammarsParsed.keySet()) {
            if (!uri.equals(baseUri)) {
                grammarsParsed.get(uri).compose(builder, grammarsParsed, false);
            }
        }

        builder.endElement("", "ixml", "ixml");
        builder.endElement("", "module", "module");
        builder.endDocument();
    }

    private XdmNode parseGrammar(Processor processor, URI grammarUri) {
        try {
            InvisibleXmlParser parser = invisibleXml.getParser();
            URLConnection conn = grammarUri.toURL().openConnection();

            String encoding = conn.getContentEncoding();
            if (encoding == null) {
                encoding = "UTF-8";
            }

            Token[] input = IxmlInputBuilder.fromString(parser.readInputStream(conn.getInputStream(), encoding));
            InvisibleXmlDocument doc = parser.parse(input);
            if (doc.getNumberOfParses() == 0) {
                throw new RuntimeException("Failed to parse " + grammarUri);
            }

            DocumentBuilder builder = processor.newDocumentBuilder();
            builder.setBaseURI(grammarUri);
            BuildingContentHandler handler = builder.newBuildingContentHandler();
            doc.getTree(handler, invisibleXml.getOptions());
            return handler.getDocumentNode();
        } catch (IOException | SaxonApiException ex) {
            throw new RuntimeException(ex);
        }
    }

    private class ResolvedGrammar {
        final XdmNode doc;
        List<ModularRule> rules = new ArrayList<>();
        Set<String> exports = new HashSet<>();
        Map<URI, Set<String>> requirements = new HashMap<>();
        Map<String, ModularRule> tree = new HashMap<>();

        public ResolvedGrammar(XdmNode doc) {
            this.doc = doc;

            try {
                XdmNode root = childNodes(doc).remove(0);
                for (XdmNode node : childNodes(root)) {
                    if (_ixml.equals(node.getNodeName())) {
                        for (XdmNode gchild : childNodes(node)) {
                            rules.add(new ModularRule(gchild));
                        }
                    } else if (!_uses.equals(node.getNodeName()) && !_shares.equals(node.getNodeName())) {
                        rules.add(new ModularRule(node));
                    }
                }

                XPathCompiler compiler = doc.getProcessor().newXPathCompiler();
                XPathExecutable exec = compiler.compile("/module/uses/from[@source]");
                XPathSelector selector = exec.load();
                selector.setContextItem(doc);
                for (XdmItem item : selector) {
                    XdmNode node = (XdmNode) item;
                    URI guri = node.getBaseURI().resolve(node.getAttributeValue(_source));
                    if (!requirements.containsKey(guri)) {
                        requirements.put(guri, new HashSet<>());
                    }
                    Set<String> symbols = requirements.get(guri);

                    exec = compiler.compile("share/@name");
                    XPathSelector shares = exec.load();
                    shares.setContextItem(node);
                    for (XdmItem share : shares) {
                        symbols.add(share.getStringValue());
                    }
                }

                compiler = doc.getProcessor().newXPathCompiler();
                exec = compiler.compile("/module/shares/share/@name");
                selector = exec.load();
                selector.setContextItem(doc);
                for (XdmItem item : selector) {
                    exports.add(item.getStringValue());
                }
            } catch (SaxonApiException ex) {
                throw new RuntimeException(ex);
            }
        }

        public void makeTree(Map<URI, ResolvedGrammar> grammarsParsed) throws SaxonApiException {
            for (ModularRule rule : rules) {
                tree.put(rule.name, rule);
            }

            for (ModularRule rule : rules) {
                for (String nt : nonterminals(rule.node)) {
                    tree.put(nt, findRule(grammarsParsed, nt));
                }
            }
        }

        public void walkTree(Map<URI, ResolvedGrammar> grammarsParsed) {
            walkTree(grammarsParsed, new HashMap<>(), new HashSet<>());
        }

        private void walkTree(Map<URI, ResolvedGrammar> grammarsParsed, HashMap<ModularRule, String> seen, HashSet<String> names) {
            boolean more = false;
            for (String symbol : tree.keySet()) {
                ModularRule rule = tree.get(symbol);
                if (!seen.containsKey(rule)) {
                    String newName = symbol;
                    int count = 0;
                    while (names.contains(newName)) {
                        newName = "_" + (++count) + "_" + newName;
                    }
                    rule.name = newName;

                    seen.put(rule, newName);
                    names.add(newName);
                    more = true;
                }
            }
            if (more) {
                for (URI uri : requirements.keySet()) {
                    ResolvedGrammar grammar = grammarsParsed.get(uri);
                    grammar.walkTree(grammarsParsed, seen, names);
                }
            }
        }

        private ModularRule findRule(Map<URI, ResolvedGrammar> grammarsParsed, String symbol) {
            for (URI uri : requirements.keySet()) {
                if (requirements.get(uri).contains(symbol)) {
                    ResolvedGrammar grammar = grammarsParsed.get(uri);
                    ModularRule rule  = grammar.findRule(grammarsParsed, symbol);
                    if (grammar.exports.isEmpty() || grammar.exports.contains(symbol)) {
                        return rule;
                    }
                    throw new RuntimeException(String.format("Symbol '%s' is not shared by %s", symbol, baseUri));
                }
            }

            for (ModularRule rule : rules) {
                if (rule.node.getAttributeValue(_name).equals(symbol)) {
                    return rule;
                }
            }

            throw new RuntimeException("Did not find symbol: " + symbol);
        }

        public void compose(ContentHandler handler, Map<URI, ResolvedGrammar> grammarsParsed, boolean prolog) throws SaxonApiException, SAXException {
            for (ModularRule rule : rules) {
                if (rule.node.getNodeName().equals(_rule)) {
                    if (firstRule) {
                        handler.startElement("", "ixml", "ixml", new AttributeBuilder(invisibleXml.getOptions()));
                        firstRule = false;
                    }
                    AttributeBuilder attr =  new AttributeBuilder(invisibleXml.getOptions());
                    attr.addAttribute("name", rule.name);
                    if (rule.node.getAttributeValue(_alias) == null) {
                        if (!rule.name.equals(rule.node.getAttributeValue(_name))) {
                            attr.addAttribute("alias", rule.node.getAttributeValue(_name));
                        }
                    }
                    XdmSequenceIterator<XdmNode> attrIter = rule.node.axisIterator(Axis.ATTRIBUTE);
                    while (attrIter.hasNext()) {
                        XdmNode att = attrIter.next();
                        if (!att.getNodeName().equals(_name)) {
                            attr.addAttribute(att.getNodeName().getLocalName(), att.getStringValue());
                        }
                    }
                    handler.startElement("", "rule", "rule", attr);
                    for (XdmNode child : childNodes(rule.node)) {
                        publish(handler, grammarsParsed, child);
                    }
                    handler.endElement("", "rule", "rule");
                } else {
                    if (prolog) {
                        publish(handler, grammarsParsed, rule.node);
                    }
                }
            }
        }

        private void publish(ContentHandler handler, Map<URI, ResolvedGrammar> grammarsParsed, XdmNode node) throws SaxonApiException, SAXException {
            switch (node.getNodeKind()) {
                case ELEMENT:
                    if (node.getNodeName().equals(_nonterminal)) {
                        ModularRule rule = findRule(grammarsParsed, node.getAttributeValue(_name));
                        AttributeBuilder attr = new AttributeBuilder(invisibleXml.getOptions());
                        attr.addAttribute("name", rule.name);

                        XdmSequenceIterator<XdmNode> attrIter = node.axisIterator(Axis.ATTRIBUTE);
                        while (attrIter.hasNext()) {
                            XdmNode att = attrIter.next();
                            if (!_name.equals(att.getNodeName())) {
                                attr.addAttribute(att.getNodeName().getLocalName(), att.getStringValue());
                            }
                        }

                        handler.startElement("", "nonterminal", "nonterminal", attr);
                        handler.endElement("", "nonterminal", "nonterminal");
                    } else {
                        AttributeBuilder attr = new AttributeBuilder(invisibleXml.getOptions());
                        XdmSequenceIterator<XdmNode> attrIter = node.axisIterator(Axis.ATTRIBUTE);
                        while (attrIter.hasNext()) {
                            XdmNode att = attrIter.next();
                            attr.addAttribute(att.getNodeName().getLocalName(), att.getStringValue());
                        }
                        handler.startElement(node.getNodeName().getNamespaceURI(), node.getNodeName().getLocalName(), node.getNodeName().toString(), attr);
                        XdmSequenceIterator<XdmNode> childIter = node.axisIterator(Axis.CHILD);
                        while (childIter.hasNext()) {
                            XdmNode child = childIter.next();
                            publish(handler, grammarsParsed, child);
                        }
                        handler.endElement(node.getNodeName().getNamespaceURI(), node.getNodeName().getLocalName(), node.getNodeName().toString());
                    }
                    break;
                case TEXT:
                    handler.characters(node.getStringValue().toCharArray(), 0, node.getStringValue().length());
                    break;
                default:
                    System.err.println("Unexpected node kind: " + node);
                    break;
            }
        }

        private List<XdmNode> childNodes(XdmNode parent) throws SaxonApiException {
            XPathCompiler compiler = parent.getProcessor().newXPathCompiler();
            XPathExecutable exec = compiler.compile("*");
            XPathSelector selector = exec.load();
            selector.setContextItem(parent);
            List<XdmNode> nodes = new ArrayList<>();
            for (XdmItem item : selector) {
                nodes.add((XdmNode) item);
            }
            return nodes;
        }

        private Set<String> nonterminals(XdmNode parent) throws SaxonApiException {
            Set<String> nonterminals = new HashSet<>();
            XPathCompiler compiler = parent.getProcessor().newXPathCompiler();
            XPathExecutable exec = compiler.compile(".//nonterminal/@name");
            XPathSelector selector = exec.load();
            selector.setContextItem(parent);
            for (XdmItem item : selector) {
                nonterminals.add(item.getStringValue());
            }
            return nonterminals;
        }
    }

    private static class ModularRule {
        final XdmNode node;
        String name;
        public ModularRule(XdmNode node) {
            this.node = node;
            name = node.getAttributeValue(_name);
        }
        @Override
        public String toString() {
            return node.getAttributeValue(_name);
        }
    }
}
