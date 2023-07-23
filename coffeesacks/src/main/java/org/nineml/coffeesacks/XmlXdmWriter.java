package org.nineml.coffeesacks;

import net.sf.saxon.s9api.*;
import org.nineml.coffeefilter.util.XmlWriter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.Map;

/**
 * An {@link XmlWriter} that produces Saxon {@link XdmNode}s.
 */
public class XmlXdmWriter extends XmlWriter {
    private final BuildingContentHandler handler;

    public XmlXdmWriter(Processor processor) {
        DocumentBuilder builder = processor.newDocumentBuilder();
        try {
            this.handler = builder.newBuildingContentHandler();
        } catch (SaxonApiException ex) {
            throw new RuntimeException(ex);
        }
    }

    public XdmNode getDocument() {
        try {
            return handler.getDocumentNode();
        } catch (SaxonApiException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void addNode(XdmNode node) {
        if (node.getNodeKind() == XdmNodeKind.DOCUMENT) {
            XdmSequenceIterator<XdmNode> iter = node.axisIterator(Axis.CHILD);
            while (iter.hasNext()) {
                addNode(iter.next());
            }
            return;
        }

        if (node.getNodeKind() == XdmNodeKind.ELEMENT) {
            Map<String,String> inscope = this.getInScopeNamespaces();
            XdmSequenceIterator<XdmNode> iter = node.axisIterator(Axis.NAMESPACE);
            while (iter.hasNext()) {
                XdmNode ns = iter.next();
                String prefix = ns.getNodeName().getLocalName();
                String uri = ns.getStringValue();
                if (!uri.equals(inscope.getOrDefault(prefix, null))) {
                    declareNamespace(prefix, uri);
                }
            }
            startElement(node.getNodeName().toString());
            iter = node.axisIterator(Axis.ATTRIBUTE);
            while (iter.hasNext()) {
                XdmNode attr = iter.next();
                addAttribute(attr.getNodeName().toString(), attr.getStringValue());
            }
            iter = node.axisIterator(Axis.CHILD);
            while (iter.hasNext()) {
                addNode(iter.next());
            }
            endElement();
            return;
        }

        if (node.getNodeKind() == XdmNodeKind.TEXT) {
            text(node.getStringValue());
            return;
        }

        if (node.getNodeKind() == XdmNodeKind.COMMENT) {
            comment(node.getStringValue());
            return;
        }

        if (node.getNodeKind() == XdmNodeKind.PROCESSING_INSTRUCTION) {
            processingInstruction(node.getNodeName().getLocalName(), node.getStringValue());
            return;
        }

        throw new IllegalStateException("Unexpected element kind"); // This can't happen
    }

    @Override
    protected void writeStartDocument() {
        try {
            handler.startDocument();
        } catch (SAXException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    protected void writeEndDocument() {
        try {
            handler.endDocument();
        } catch (SAXException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected void startPrefixMapping(String prefix, String uri) {
        try {
            handler.startPrefixMapping(prefix, uri);
        } catch (SAXException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected void endPrefixMapping(String prefix, String uri) {
        try {
            handler.endPrefixMapping(prefix);
        } catch (SAXException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    protected void writeStartElement(XmlQName tag, Map<XmlQName, String> attributes) {
        try {
            handler.startElement(tag.namespaceURI, tag.localName, tag.toString(), new SaxAttributes(attributes));
        } catch (SAXException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    protected void writeEndElement(XmlQName tag) {
        try {
            handler.endElement(tag.namespaceURI, tag.localName, tag.toString());
        } catch (SAXException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    protected void writeText(String text) {
        try {
            handler.characters(text.toCharArray(), 0, text.length());
        } catch (SAXException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    protected void writeComment(String comment) {
        /// handler doesn't have a comment method? WAT?
    }

    @Override
    protected void writeProcessingInstruction(String name, String data) {
        try {
            handler.processingInstruction(name, data);
        } catch (SAXException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static class SaxAttributes implements Attributes {
        private final XmlQName[] names;
        private final String[] values;

        public SaxAttributes(Map<XmlQName,String> attributes) {
            names = new XmlQName[attributes.size()];
            values = new String[attributes.size()];
            int pos = 0;
            for (XmlQName name : attributes.keySet()) {
                names[pos] = name;
                values[pos] = attributes.get(name);
                pos++;
            }
        }

        @Override
        public int getLength() {
            return names.length;
        }

        @Override
        public String getURI(int index) {
            if (index <= names.length) {
                return names[index].namespaceURI;
            }
            return null;
        }

        @Override
        public String getLocalName(int index) {
            if (index <= names.length) {
                return names[index].localName;
            }
            return null;
        }

        @Override
        public String getQName(int index) {
            if (index <= names.length) {
                return names[index].toString();
            }
            return null;
        }

        @Override
        public String getType(int index) {
            if (index <= names.length) {
                return "CDATA";
            }
            return null;
        }

        @Override
        public String getValue(int index) {
            if (index <= values.length) {
                return values[index];
            }
            return null;
        }

        @Override
        public int getIndex(String uri, String localName) {
            for (int pos = 0; pos < names.length; pos++) {
                if (names[pos].namespaceURI.equals(uri) && names[pos].localName.equals(localName)) {
                    return pos;
                }
            }
            return -1;
        }

        @Override
        public int getIndex(String qName) {
            for (int pos = 0; pos < names.length; pos++) {
                if (names[pos].toString().equals(qName)) {
                    return pos;
                }
            }
            return -1;
        }

        @Override
        public String getType(String uri, String localName) {
            if (getIndex(uri, localName) >= 0) {
                return "CDATA";
            }
            return null;
        }

        @Override
        public String getType(String qName) {
            if (getIndex(qName) >= 0) {
                return "CDATA";
            }
            return null;
        }

        @Override
        public String getValue(String uri, String localName) {
            int pos = getIndex(uri, localName);
            if (pos >= 0) {
                return values[pos];
            }
            return null;
        }

        @Override
        public String getValue(String qName) {
            int pos = getIndex(qName);
            if (pos >= 0) {
                return values[pos];
            }
            return null;
        }
    }
}
