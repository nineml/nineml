package org.nineml.coffeefilter.model;

import org.nineml.coffeefilter.InvisibleXml;
import org.nineml.coffeefilter.ParserOptions;
import org.nineml.coffeefilter.util.URIUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.net.URI;

public class IxmlContentHandler extends DefaultHandler {
    private boolean finished = false;
    private boolean simplified = false;
    private final InvisibleXml invisibleXml;
    private final ParserOptions options;
    private IModule module = null;
    private XNode current = null;
    private final URI baseUri;

    /**
     * Construct a new content handler.
     * @param invisibleXml The Invisible XML instance
     */
    public IxmlContentHandler(InvisibleXml invisibleXml, String systemId) {
        this .invisibleXml = invisibleXml;
        this.options = invisibleXml.getOptions();
        if (systemId == null) {
            baseUri = URIUtils.cwd();
        } else {
            baseUri = URIUtils.cwd().resolve(systemId);
        }
    }

    /**
     * Get the Ixml for this handler.
     * <p>The underlying grammar is cached. Calling this method with different parser
     * options will have no effect.</p>
     * @return the Ixml grammar
     */
    public IModule getModule() {
        if (!finished) {
            return null;
        }

        if (module == null) {
            throw new NullPointerException("No iXML grammar has been loaded");
        }

        return module;
    }

    @Override
    public void startDocument () throws SAXException {
        finished = false;
        simplified = false;
    }

    @Override
    public void endDocument () throws SAXException {
        finished = true;
    }

    @Override
    public void startElement (String uri, String localName,
                              String qName, Attributes attributes)
            throws SAXException
    {
        /*
        System.err.printf("%s : %d\n", localName, attributes.getLength());
        for (int pos = 0; pos < attributes.getLength(); pos++) {
            System.err.printf("  @%s: %s\n", attributes.getLocalName(pos), attributes.getValue(pos));
        }
         */
        if (current == null) {
            module = new IModule(options, invisibleXml, baseUri);
            current = module;
        } else {
            current = current.createChild(localName, attributes);
        }
    }

    @Override
    public void endElement (String uri, String localName, String qName)
            throws SAXException
    {
        if (current == null) {
            System.err.println("Current is null for " + localName + "?");
        } else {
            current = current.getParent();
        }
    }

    @Override
    public void characters (char ch[], int start, int length) {
        if (current == null) {
            System.err.println("Current is null for characters?");
        } else {
            current.addCharacters(new String(ch, start, length));
        }
    }
}
