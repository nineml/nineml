package org.nineml.coffeefilter.model;

import net.sf.saxon.s9api.*;
import org.nineml.coffeefilter.InvisibleXml;
import org.nineml.coffeefilter.InvisibleXmlDocument;
import org.nineml.coffeefilter.InvisibleXmlParser;
import org.nineml.coffeefilter.ParserOptions;
import org.nineml.coffeefilter.exceptions.IxmlException;
import org.nineml.coffeefilter.util.AttributeBuilder;
import org.nineml.coffeefilter.util.IxmlInputBuilder;
import org.nineml.coffeegrinder.tokens.Token;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URI;
import java.net.URLConnection;
import java.util.*;

public class ModularGrammar {
    private static final QName _source = new QName("source");
    private static final QName _uses = new QName("uses");
    private static final QName _shares = new QName("shares");
    private static final QName _ixml = new QName("ixml");
    private static final QName _rule = new QName("rule");
    private static final QName _name = new QName("name");
    private static final QName _alias = new QName("alias");
    private static final QName _nonterminal = new QName("nonterminal");

    private final ParserOptions options;
    private final InvisibleXml invisibleXml;
    private final URI baseUri;
    private final List<ResolvedGrammar> grammarList = new ArrayList<>();
    private final Map<URI, ResolvedGrammar> grammarsParsed = new HashMap<>();
    private final Set<ModularRule> referenced = new HashSet<>();
    private Ixml ixml = null;
    private XdmNode modularGrammar = null;

    public ModularGrammar(ParserOptions options, InvisibleXml invisibleXml, URI baseUri) {
        this.options = options;
        this.invisibleXml = invisibleXml;
        this.baseUri = baseUri;
    }

    public Ixml getIxml() {
        if (ixml != null) {
            return ixml;
        }

        Processor processor = new Processor(options.getSaxonConfiguration());

        try {
            List<URI> grammarsToParse = new ArrayList<>();
            grammarsToParse.add(baseUri);
            while (!grammarsToParse.isEmpty()) {
                URI uri = grammarsToParse.remove(0);
                XdmNode doc = parseGrammar(processor, uri);
                ResolvedGrammar grammar = new ResolvedGrammar(doc);
                grammarsParsed.put(uri, grammar);
                grammarList.add(grammar);

                XPathCompiler compiler = processor.newXPathCompiler();
                XPathExecutable exec = compiler.compile("/ixml/uses/from[@source]");
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

            grammarList.get(0).makeTree(new HashMap<>());

            // Update all the tree rules with the correct underlying ModularRule
            for (ResolvedGrammar grammar : grammarList) {
                for (TreeRule trule : grammar.tree.values()) {
                    if (trule.rule == null) {
                        trule.rule = grammar.findRule(trule.symbol);
                    }
                }
            }

            grammarList.get(0).walkTree();

            if (options.getModularity()) {
                // This is (probably) temporary
                DocumentBuilder docBuilder = processor.newDocumentBuilder();
                BuildingContentHandler bch = docBuilder.newBuildingContentHandler();
                makeNewGrammar(bch);
                modularGrammar = bch.getDocumentNode();
                options.getLogger().debug("modularity", "Composed grammar:\n%s", modularGrammar);
            }

            IxmlContentHandler builder = new IxmlContentHandler(invisibleXml, baseUri.toString());
            makeNewGrammar(builder);

            ixml = builder.getIxml();
            return ixml;
        } catch (SaxonApiException | SAXException ex) {
            throw new RuntimeException(ex);
        }
    }

    public XdmNode getModularGrammar() {
        return modularGrammar;
    }

    private void makeNewGrammar(ContentHandler builder) throws SaxonApiException, SAXException {
        builder.startDocument();
        builder.startElement("", "ixml", "ixml", new AttributeBuilder(invisibleXml.getOptions()));

        boolean prolog = true;
        for (ResolvedGrammar grammar : grammarList) {
            grammar.compose(builder, prolog);
            prolog = false;
        }

        builder.endElement("", "ixml", "ixml");
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
        final URI grammarUri;
        List<ModularRule> rules = new ArrayList<>();
        List<URI> modlist = new ArrayList<>();
        Map<URI, Set<String>> requirements = new HashMap<>();
        Map<URI, Set<String>> overrides = new HashMap<>();
        Map<String, TreeRule> tree = new HashMap<>();
        boolean resolved = false;

        public ResolvedGrammar(XdmNode doc) {
            this.doc = doc;
            this.grammarUri = doc.getBaseURI();

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
                XPathExecutable exec = compiler.compile("/ixml/uses/from[@source]");
                XPathSelector selector = exec.load();
                selector.setContextItem(doc);
                for (XdmItem item : selector) {
                    XdmNode node = (XdmNode) item;
                    URI guri = node.getBaseURI().resolve(node.getAttributeValue(_source));
                    if (!requirements.containsKey(guri)) {
                        modlist.add(guri);
                        requirements.put(guri, new HashSet<>());
                        overrides.put(guri, new HashSet<>());
                    }

                    Set<String> symbols = requirements.get(guri);
                    exec = compiler.compile("share/@name");
                    XPathSelector shares = exec.load();
                    shares.setContextItem(node);
                    for (XdmItem share : shares) {
                        String name = share.getUnderlyingValue().getStringValue();
                        if (hasRule(doc, name)) {
                            throw IxmlException.usedSymbolDeclaredLocally(name);
                        }
                        symbols.add(name);
                    }

                    symbols = overrides.get(guri);
                    exec = compiler.compile("overrides/share/@name");
                    shares = exec.load();
                    shares.setContextItem(node);
                    for (XdmItem share : shares) {
                        String name = share.getUnderlyingValue().getStringValue();
                        if (!hasRule(doc, name)) {
                            throw IxmlException.overrideUndeclared(name);
                        }
                        symbols.add(name);
                    }
                }

                compiler = doc.getProcessor().newXPathCompiler();
                exec = compiler.compile("/ixml/shares/share/@name");
                selector = exec.load();
                selector.setContextItem(doc);
                for (XdmItem item : selector) {
                    String name = item.getUnderlyingValue().getStringValue();
                    if (!hasRule(doc, name)) {
                        throw IxmlException.sharedSymbolUndeclared(name);
                    }
                }
            } catch (SaxonApiException ex) {
                throw new RuntimeException(ex);
            }
        }

        private boolean hasRule(XdmNode doc, String symbol) throws SaxonApiException {
            XPathCompiler compiler = doc.getProcessor().newXPathCompiler();
            XPathExecutable exec = compiler.compile("/ixml/rule[@name='" + symbol + "']");
            XPathSelector selector = exec.load();
            selector.setContextItem(doc);
            return selector.effectiveBooleanValue();
        }

        private ModularRule findRule(String nt) {
            TreeRule trule = tree.get(nt);
            if (trule == null) {
                throw new RuntimeException("Failed to find tree rule for " + nt + " in " + grammarUri);
            }
            if (trule.grammar.equals(grammarUri)) {
                for (ModularRule rule : rules) {
                    if (nt.equals(rule.name)) {
                        return rule;
                    }
                }
                throw new RuntimeException("Failed to find rule for " + nt + " in " + grammarUri);
            } else {
                ResolvedGrammar nextGrammar = grammarsParsed.get(trule.grammar);
                return nextGrammar.findRule(nt);
            }
        }

        public void makeTree(Map<String,TreeRule> localOverrides) throws SaxonApiException {
            for (ModularRule rule : rules) {
                if (rule.name != null) {
                    if (localOverrides.containsKey(rule.name)) {
                        tree.put(rule.name, localOverrides.get(rule.name));
                    } else {
                        tree.put(rule.name, new TreeRule(grammarUri, rule.name, rule));
                    }
                }
            }

            for (URI uses : modlist) {
                for (String symbol : requirements.get(uses)) {
                    if (localOverrides.containsKey(symbol)) {
                        tree.put(symbol, localOverrides.get(symbol));
                    } else {
                        tree.put(symbol, new TreeRule(uses, symbol));
                    }
                }
            }

            for (ModularRule rule : rules) {
                if (rule.name != null) {
                    for (String nt : nonterminals(rule.node)) {
                        TreeRule foundRule = tree.get(nt);
                        if (foundRule == null) {
                            throw new IllegalStateException("Rule " + rule.name + " not found in " + grammarUri);
                        }
                        tree.put(nt, foundRule);
                    }
                }
            }

            resolved = true;

            for (URI uses : modlist) {
                Set<String> overideSymbols = this.overrides.containsKey(uses) ? this.overrides.get(uses) : new HashSet<>();
                Map<String,TreeRule> overrideTrees = new HashMap<>();
                for (String symbol : overideSymbols) {
                    overrideTrees.put(symbol, tree.get(symbol));
                }

                ResolvedGrammar subgrammar = grammarsParsed.get(uses);
                if (subgrammar.resolved) {
                    if (!overideSymbols.isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        String sep = "";
                        for (String symbol : overideSymbols) {
                            sb.append(sep);
                            sb.append(symbol);
                            sep = ", ";
                        }
                        throw IxmlException.multipleOverrides(subgrammar.grammarUri.toString(), sb.toString());
                    }
                } else {
                    subgrammar.makeTree(overrideTrees);
                }
            }
        }

        public void walkTree() throws SaxonApiException {
            walkTree(new HashMap<>(), new HashSet<>());

            ResolvedGrammar rg = grammarList.get(0);
            for (ModularRule rule : rg.rules) {
                if (rule.name == null) {
                    referenced.add(rule);
                } else {
                    findReferences(rule);
                    break;
                }
            }
        }

        private void findReferences(ModularRule rule) throws SaxonApiException {
            if (referenced.contains(rule)) {
                return;
            }
            referenced.add(rule);
            for (String symbol : nonterminals(rule.node)) {
                TreeRule trule = tree.get(symbol);
                if (trule != null) {
                    ResolvedGrammar nextgrammar = grammarsParsed.get(trule.grammar);
                    ModularRule foundRule = nextgrammar.tree.get(symbol).rule;
                    if (foundRule == null) {
                        throw new RuntimeException("WAT");
                    }
                    nextgrammar.findReferences(foundRule);
                }
            }
        }

        private void walkTree(HashMap<ModularRule, String> seen, HashSet<String> names) {
            boolean more = false;
            for (String symbol : tree.keySet()) {
                ModularRule rule = tree.get(symbol).rule;
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
                for (URI uri : modlist) {
                    ResolvedGrammar grammar = grammarsParsed.get(uri);
                    grammar.walkTree(seen, names);
                }
            }
        }

        public void compose(ContentHandler handler, boolean prolog) throws SaxonApiException, SAXException {
            for (ModularRule rule : rules) {
                if (rule.node.getNodeName().equals(_rule)) {
                    if (referenced.contains(rule)) {
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
                            publish(handler, child);
                        }
                        handler.endElement("", "rule", "rule");
                    }
                } else {
                    if (prolog) {
                        publish(handler, rule.node);
                    }
                }
            }
        }

        private void publish(ContentHandler handler, XdmNode node) throws SaxonApiException, SAXException {
            switch (node.getNodeKind()) {
                case ELEMENT:
                    if (node.getNodeName().equals(_nonterminal)) {
                        String symbol = node.getAttributeValue(_name);
                        ModularRule rule = tree.get(symbol).rule;

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
                            publish(handler, child);
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

        @Override
        public String toString() {
            return grammarUri.toString();
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

    private static class TreeRule {
        final URI grammar;
        final String symbol;
        ModularRule rule = null;
        public TreeRule(URI grammar, String symbol, ModularRule rule) {
            this.grammar = grammar;
            this.symbol = symbol;
            this.rule = rule;
        }
        public TreeRule(URI grammar, String symbol) {
            this.grammar = grammar;
            this.symbol = symbol;
        }
        @Override
        public String toString() {
            if  (rule == null) {
                return grammar.toString() + " ?? " + symbol;
            }
            return grammar.toString() + " :: " + symbol;
        }
    }
}
