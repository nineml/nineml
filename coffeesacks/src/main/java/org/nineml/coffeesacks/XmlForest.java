package org.nineml.coffeesacks;

import net.sf.saxon.s9api.*;
import org.nineml.coffeefilter.InvisibleXml;
import org.nineml.coffeefilter.InvisibleXmlDocument;
import org.nineml.coffeefilter.ParserOptions;
import org.nineml.coffeefilter.util.AttributeBuilder;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.util.ParserAttribute;
import org.xml.sax.SAXException;

import java.util.*;

/** Constructs an XML representation of the parser forest.
 * <p>The {@link XPathAxe} gets to see this representation when making choices about ambiguous parses.
 * Because the forest is a graph, not a tree, all of the nodes in this representation are siblings.
 * Parent and child (tree) relationships are expressed with links.</p>
 */
public class XmlForest {
    private static final QName _id = new QName("id");
    private final Processor processor;
    private final boolean maxPriorityStyle;
    private final ParseForest graph;
    private final ParserOptions options;
    private Stack<ForestNode> nodeStack = new Stack<>();
    private HashMap<ForestNode, HashSet<ForestNode>> parents = new HashMap<>();
    private HashMap<ForestNode, ArrayList<ChildList>> children = new HashMap<>();
    private HashMap<ForestNode, Integer> priority = new HashMap<>();
    private HashMap<ForestNode, Map<String,String>> parseAttrMap = new HashMap<>();
    private BuildingContentHandler handler;
    protected final Map<String,XdmNode> choiceIndex = new HashMap<>();
    private XdmNode doc;
    HashSet<ForestNode> processed = null;
    HashSet<ForestNode> queued = null;
    ArrayList<ForestNode> toBeProcessed = null;


    public XmlForest(Processor processor, InvisibleXmlDocument document) throws SaxonApiException, SAXException {
        this.processor = processor;
        this.maxPriorityStyle = "max".equals(document.getOptions().getPriorityStyle());
        this.graph = document.getResult().getForest();
        this.options = document.getOptions();
        this.doc = null;
        if (document.succeeded()) {
            constructXml();
        }
    }

    public XdmNode getXml() {
        return doc;
    }

    private void constructXml() throws SaxonApiException, SAXException {
        DocumentBuilder builder = processor.newDocumentBuilder();
        handler = builder.newBuildingContentHandler();

        ForestNode root = graph.getRoot();

        handler.startDocument();

        if (graph.isAmbiguous()) {
            AttributeBuilder atts = newAttributes("root", ((NonterminalSymbol) root.symbol).getName());
            atts.addAttribute("ref", "N" + root.id);
            handler.startElement("", "graph", "graph", atts);
            buildXmlRepresentation(root);
        } else {
            AttributeBuilder atts = newAttributes();
            handler.startElement("", "graph", "graph", atts);
        }

        handler.endElement("", "graph", "graph");
        handler.endDocument();

        nodeStack = null;
        parents = null;
        children = null;
        priority = null;
        parseAttrMap = null;

        doc = handler.getDocumentNode();

        XPathCompiler compiler = processor.newXPathCompiler();
        XPathExecutable exec = compiler.compile("//children[preceding-sibling::children or following-sibling::children]");
        XPathSelector selector = exec.load();
        selector.setContextItem(doc);
        for (XdmValue value : selector.evaluate()) {
            XdmNode choice = (XdmNode) value;
            choiceIndex.put(choice.getAttributeValue(_id), choice.getParent());
        }
    }

    private synchronized void buildXmlRepresentation(ForestNode root) throws SAXException {
        processed = new HashSet<>();
        queued = new HashSet<>();
        toBeProcessed = new ArrayList<>();

        traverse(root, Collections.emptyList());

        AttributeBuilder atts;
        toBeProcessed.add(root);

        while (!toBeProcessed.isEmpty()) {
            ForestNode node = toBeProcessed.remove(0);
            processed.add(node);
            queued.remove(node);

            //System.err.printf("Node: %s%n", node);

            final String gi;
            if (node.symbol instanceof TerminalSymbol) {
                atts = newAttributes("value", node.symbol.toString());
                atts.addAttribute("id", "N" + node.id);
                gi = "token";
            } else {
                atts = newAttributes("name", ((NonterminalSymbol) node.symbol).getName());
                atts.addAttribute("id", "N" + node.id);
                gi = "symbol";
            }

            String mark = parseAttrMap.get(node).getOrDefault(InvisibleXml.MARK_ATTRIBUTE,
                parseAttrMap.get(node).getOrDefault(InvisibleXml.TMARK_ATTRIBUTE, null));
            if (mark != null) {
                atts.addAttribute("mark", mark);
            }

            atts.addAttribute("", "start", String.valueOf(node.leftExtent+1));
            atts.addAttribute("", "end", String.valueOf(node.rightExtent));
            atts.addAttribute("", "length", String.valueOf(node.rightExtent - node.leftExtent));
            handler.startElement("", gi, gi, atts);

            for (String key : parseAttrMap.get(node).keySet()) {
                if (!InvisibleXml.NAME_ATTRIBUTE.equals(key)
                        && !InvisibleXml.MARK_ATTRIBUTE.equals(key)
                        && !InvisibleXml.TMARK_ATTRIBUTE.equals(key)) {
                    atts = newAttributes("name", key);
                    atts.addAttribute("value", parseAttrMap.get(node).get(key));
                    handler.startElement("", "attribute", "attribute", atts);
                    handler.endElement("", "attribute", "attribute");
                }
            }

            if (parents.containsKey(node)) {
                for (ForestNode parent : parents.get(node)) {
                    atts = newAttributes("ref", "N" + parent.id);
                    handler.startElement("", "parent", "parent", atts);
                    char[] chars = ((NonterminalSymbol) parent.symbol).getName().toCharArray();
                    handler.characters(chars, 0, chars.length);
                    handler.endElement("", "parent", "parent");
                }
            }

            for (Family family : node.getFamilies()) {
                atts = newAttributes("id", "C" + family.id);
                handler.startElement("", "children", "children", atts);

                addChild(family.getLeftNode());
                addChild(family.getRightNode());

                handler.endElement("", "children", "children");
            }

            handler.endElement("", gi, gi);
        }
    }

    private void addChild(ForestNode child) throws SAXException {
        if (child == null) {
            return;
        }

        if (child.symbol != null && !processed.contains(child) && !queued.contains(child)) {
            toBeProcessed.add(child);
            queued.add(child);
        }

        AttributeBuilder atts;
        final String childgi;
        if (child.symbol == null) {
            atts = newAttributes("id", "N" + child.id);
            childgi = "state";
        } else if (child.symbol instanceof TerminalSymbol) {
            atts = newAttributes("value", child.symbol.toString());
            atts.addAttribute("ref", "N" + child.id);
            childgi = "token";
        } else {
            atts = newAttributes("name", ((NonterminalSymbol) child.symbol).getName());
            atts.addAttribute("ref", "N" + child.id);
            childgi = "symbol";
        }
        atts.addAttribute("", "start", String.valueOf(child.leftExtent+1));
        atts.addAttribute("", "end", String.valueOf(child.rightExtent));
        atts.addAttribute("", "length", String.valueOf(child.rightExtent - child.leftExtent));
        handler.startElement("", childgi, childgi, atts);

        if (child.symbol == null) {
            for (Family family : child.getFamilies()) {
                atts = newAttributes("id", "C" + family.id);
                handler.startElement("", "children", "children", atts);
                addChild(family.getLeftNode());
                addChild(family.getRightNode());
                handler.endElement("", "children", "children");
            }
        }

        handler.endElement("", childgi, childgi);
    }

    private void xbuildXmlRepresentation(ForestNode root) throws SAXException {
        traverse(root, Collections.emptyList());

        AttributeBuilder atts;
        HashSet<ForestNode> processed = new HashSet<>();
        HashSet<ForestNode> queued = new HashSet<>();
        ArrayList<ForestNode> toBeProcessed = new ArrayList<>();
        toBeProcessed.add(root);

        while (!toBeProcessed.isEmpty()) {
            ForestNode node = toBeProcessed.remove(0);
            processed.add(node);
            queued.remove(node);

            //System.err.printf("Node: %s%n", node);

            final String gi;
            if (node.symbol instanceof TerminalSymbol) {
                atts = newAttributes("value", node.symbol.toString());
                atts.addAttribute("id", "N" + node.id);
                gi = "token";
            } else {
                atts = newAttributes("name", ((NonterminalSymbol) node.symbol).getName());
                atts.addAttribute("id", "N" + node.id);
                gi = "symbol";
            }

            String mark = parseAttrMap.get(node).getOrDefault(InvisibleXml.MARK_ATTRIBUTE,
                    parseAttrMap.get(node).getOrDefault(InvisibleXml.TMARK_ATTRIBUTE, null));
            if (mark != null) {
                atts.addAttribute("mark", mark);
            }

            atts.addAttribute("", "start", String.valueOf(node.leftExtent+1));
            atts.addAttribute("", "end", String.valueOf(node.rightExtent));
            atts.addAttribute("", "length", String.valueOf(node.rightExtent - node.leftExtent));
            handler.startElement("", gi, gi, atts);

            for (String key : parseAttrMap.get(node).keySet()) {
                if (!InvisibleXml.NAME_ATTRIBUTE.equals(key)
                        && !InvisibleXml.MARK_ATTRIBUTE.equals(key)
                        && !InvisibleXml.TMARK_ATTRIBUTE.equals(key)) {
                    atts = newAttributes("name", key);
                    atts.addAttribute("value", parseAttrMap.get(node).get(key));
                    handler.startElement("", "attribute", "attribute", atts);
                    handler.endElement("", "attribute", "attribute");
                }
            }

            if (parents.containsKey(node)) {
                for (ForestNode parent : parents.get(node)) {
                    atts = newAttributes("ref", "N" + parent.id);
                    handler.startElement("", "parent", "parent", atts);
                    char[] chars = ((NonterminalSymbol) parent.symbol).getName().toCharArray();
                    handler.characters(chars, 0, chars.length);
                    handler.endElement("", "parent", "parent");
                }
            }

            for (ChildList family : children.get(node)) {
                atts = newAttributes("id", "C" + family.id);
                int prio = 0;
                for (ForestNode child : family.children) {
                    if (maxPriorityStyle) {
                        prio = Math.max(prio, priority.get(child));
                    } else {
                        prio += priority.get(child);
                    }
                }
                atts.addAttribute("priority", String.format("%d", prio));

                handler.startElement("", "children", "children", atts);
                for (ForestNode child : family.children) {
                    final String childgi;
                    if (child.symbol instanceof TerminalSymbol) {
                        atts = newAttributes("value", child.symbol.toString());
                        atts.addAttribute("ref", "N" + child.id);
                        childgi = "token";
                    } else {
                        atts = newAttributes("name", ((NonterminalSymbol) child.symbol).getName());
                        atts.addAttribute("ref", "N" + child.id);
                        childgi = "symbol";
                    }
                    atts.addAttribute("", "start", String.valueOf(child.leftExtent+1));
                    atts.addAttribute("", "end", String.valueOf(child.rightExtent));
                    atts.addAttribute("", "length", String.valueOf(child.rightExtent - child.leftExtent));
                    handler.startElement("", childgi, childgi, atts);
                    handler.endElement("", childgi, childgi);
                    if (!processed.contains(child) && !queued.contains(child)) {
                        toBeProcessed.add(child);
                        queued.add(child);
                    }
                }
                handler.endElement("", "children", "children");
            }

            handler.endElement("", gi, gi);
        }
    }

    private void traverse(ForestNode node, List<ParserAttribute> parserAttributes) {
        if (node == null || children.containsKey(node)) {
            return;
        }

        nodeStack.push(node);

        int pos = nodeStack.size() - 1;
        ForestNode parent = nodeStack.get(pos);
        while (parent.symbol == null) {
            pos--;
            parent = nodeStack.get(pos);
        }

        ArrayList<ChildList> childList = new ArrayList<>();
        children.put(node, childList);
        parseAttrMap.put(node, attMap(parserAttributes));

        for (Family family : node.getFamilies()) {
            ChildList childs = new ChildList(family.id);
            if (family.getLeftNode() != null) {
                ForestNode left = family.getLeftNode();
                if (!parents.containsKey(left)) {
                    parents.put(left, new HashSet<>());
                    priority.put(left, left.getPriority());
                }
                parents.get(left).add(parent);
                childs.children.addAll(properChildren(left));
                traverse(left, family.getLeftAttributes());
            }
            if (family.getRightNode() != null) {
                ForestNode right = family.getRightNode();
                if (!parents.containsKey(right)) {
                    parents.put(right, new HashSet<>());
                    priority.put(right, right.getPriority());
                }
                parents.get(right).add(parent);
                childs.children.addAll(properChildren(right));
                traverse(right, family.getRightAttributes());
            }
            childList.add(childs);
        }

        nodeStack.pop();
    }

    private ArrayList<ForestNode> properChildren(ForestNode node) {
        ArrayList<ForestNode> symbols = new ArrayList<>();
        if (node.symbol != null) {
            symbols.add(node);
            return symbols;
        }

        for (Family family : node.getFamilies()) {
            if (family.getLeftNode() != null) {
                symbols.addAll(properChildren(family.getLeftNode()));
            }
            if (family.getRightNode() != null) {
                symbols.addAll(properChildren(family.getRightNode()));
            }
        }

        return symbols;
    }

    private Map<String,String> attMap(List<ParserAttribute> attributes) {
        HashMap<String,String> map = new HashMap<>();
        for (ParserAttribute attr : attributes) {
            if (!map.containsKey(attr.getName())) {
                map.put(attr.getName(), attr.getValue());
            }
        }
        return map;
    }

    private AttributeBuilder newAttributes() {
        return new AttributeBuilder(options);
    }

    private AttributeBuilder newAttributes(String name, String value) {
        AttributeBuilder atts = newAttributes();
        atts.addAttribute("", name, value);
        return atts;
    }

    private static class ChildList {
        public final int id;
        public final ArrayList<ForestNode> children;
        public ChildList(int id) {
            this.id = id;
            this.children = new ArrayList<>();
        }
    }
}
