package org.nineml.coffeefilter;

import org.nineml.coffeefilter.exceptions.IxmlException;
import org.nineml.coffeefilter.trees.ContentHandlerAdapter;
import org.nineml.coffeefilter.util.AttributeBuilder;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.tokens.TokenCharacter;
import org.nineml.coffeegrinder.tokens.TokenEOF;
import org.nineml.coffeegrinder.trees.TreeBuilder;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import java.io.PrintStream;
import java.util.*;

/**
 * A failure document represents the results of a failed parse.
 *
 * This is a subclass of the {@link InvisibleXmlDocument} that generates the
 * result document for a failed parse.
 */
public class InvisibleXmlFailureDocument extends InvisibleXmlDocument {
    protected InvisibleXmlFailureDocument(GearleyResult result, String parserVersion, ParserOptions options) {
        super(result, parserVersion, options);
    }

    @Override
    public boolean succeeded() {
        return false;
    }

    @Override
    public long getNumberOfParses() {
        return 0;
    }

    @Override
    public String getTree() {
        StringContentHandler handler = new StringContentHandler();
        realizeErrorDocument(handler);
        return handler.getString();
    }

    @Override
    public void getTree(PrintStream output) {
        output.print(getTree());
    }

    @Override
    public void getTree(ContentHandler handler) {
        realizeErrorDocument(handler);
    }

    @Override
    public void getTree(ContentHandler handler, ParserOptions options) {
        getTree(handler);
    }

    @Override
    public void getTree(TreeBuilder builder) {
        if (builder instanceof ContentHandlerAdapter) {
            getTree(((ContentHandlerAdapter) builder).getHandler());
        } else {
            throw IxmlException.cannotParseFailure();
        }
    }

    private void realizeErrorDocument(ContentHandler handler) {
        try {
            handler.startDocument();

            handler.startPrefixMapping(InvisibleXml.ixml_prefix, InvisibleXml.ixml_ns);

            AttributeBuilder attrs = new AttributeBuilder(options);
            attrs.addAttribute(InvisibleXml.ixml_ns, InvisibleXml.ixml_prefix + ":state", "failed");
            handler.startElement("", "fail", "failed", attrs);

            if (getLineNumber() > 0) {
                atomicValue(handler, "line", String.valueOf(getLineNumber()));
            }

            if (getColumnNumber() > 0) {
                atomicValue(handler, "column", String.valueOf(getColumnNumber()));
            }

            atomicValue(handler, "pos", String.valueOf(result.getTokenCount()));

            if (result.getLastToken() == TokenEOF.EOF) {
                // This only happens for the GLL parser.
                atomicValue(handler, "end-of-input", "true");
            } else {
                TokenCharacter tchar = (TokenCharacter) result.getLastToken();
                if (tchar != null) {
                    if (result.getParser().hasMoreInput()) {
                        // Special case so that we can include the code point
                        attrs = new AttributeBuilder(options);
                        if (tchar.getCodepoint() < 32 || tchar.getCodepoint() >= 127) {
                            attrs.addAttribute("codepoint", String.format("#%04X", tchar.getCodepoint()));
                        }
                        handler.startElement("", "unexpected", "unexpected", attrs);
                        String value = tchar.getValue();
                        handler.characters(value.toCharArray(), 0, value.length());
                        handler.endElement("", "unexpected", "unexpected");
                    } else {
                        atomicValue(handler, "end-of-input", "true");
                    }
                }
            }

            if (result instanceof EarleyResult) {
                EarleyResult eresult = (EarleyResult) result;
                NextTokenDetail detail = couldBeNextDetail(eresult.getChart(), result.getParser().getGrammar());

                boolean predictedSome = false;
                List<Token> oknext = couldBeNext(result.getPredictedTerminals());
                if (!oknext.isEmpty()) {
                    predictedSome = true;
                    tokenList(handler, oknext, "permitted");
                }

                oknext = couldBeNext(eresult.getChart(), result.getParser().getGrammar());
                if (!oknext.isEmpty()) {
                    String elemName = "permitted";
                    if (predictedSome) {
                        elemName = "also-predicted";
                    }
                    tokenList(handler, oknext, elemName);
                }

                EarleyPath path = eresult.getPath();
                if (!path.getSegments().isEmpty()) {
                    handler.startElement("", "completions", "completions", AttributeBuilder.EMPTY_ATTRIBUTES);
                    for (EarleyPath.PathSegment segment : path.getSegments()) {
                        attrs = new AttributeBuilder(options);
                        attrs.addAttribute("start", String.valueOf(segment.start));
                        attrs.addAttribute("end", String.valueOf(segment.end));

                        StringBuilder sb = new StringBuilder();
                        boolean first = true;
                        for (NonterminalSymbol symbol : segment.symbols) {
                            if (!first) {
                                sb.append(", ");
                            }
                            sb.append(symbol.getName());
                            first = false;
                        }
                        attrs.addAttribute("rules", sb.toString());

                        handler.startElement("", "completed", "completed", attrs);
                        atomicValue(handler, "input", segment.input);
                        handler.endElement("", "completed", "completed");
                    }
                    handler.endElement("", "completions", "completions");
                }

                if (!detail.getNextTokens().isEmpty()) {
                    handler.startElement("", "could-be-next", "could-be-next", AttributeBuilder.EMPTY_ATTRIBUTES);
                    for (NonterminalSymbol symbol : detail.getNextTokens().keySet()) {
                        attrs = new AttributeBuilder(options);
                        attrs.addAttribute("rule", symbol.getName());
                        handler.startElement("", "in", "in", attrs);
                        List<Token> tokens = detail.getNextTokens().get(symbol);
                        atomicValue(handler, "tokens", tokenList(tokens));
                        handler.endElement("", "in", "in");
                    }
                    handler.endElement("", "could-be-next", "could-be-next");
                }

                if (!path.getRules().isEmpty()) {
                    handler.startElement("", "unfinished", "unfinished", AttributeBuilder.EMPTY_ATTRIBUTES);
                    for (EarleyPath.PathSegment segment : path.getRules()) {
                        attrs = new AttributeBuilder(options);
                        attrs.addAttribute("start", String.valueOf(segment.start));
                        attrs.addAttribute("end", String.valueOf(segment.end));

                        StringBuilder sb = new StringBuilder();
                        boolean first = true;
                        for (NonterminalSymbol symbol : segment.symbols) {
                            if (!first) {
                                sb.append(" ");
                            }
                            sb.append(symbol.getName());
                            first = false;
                        }
                        attrs.addAttribute("rules", sb.toString());

                        handler.startElement("", "open", "open", attrs);
                        atomicValue(handler, "input", segment.input);
                        handler.endElement("", "open", "open");
                    }
                    handler.endElement("", "unfinished", "unfinished");
                }

                if (options.getShowChart()) {
                    handler.startElement("", "chart", "chart", AttributeBuilder.EMPTY_ATTRIBUTES);

                    for (int row = 0; row < eresult.getChart().size(); row++) {
                        if (!eresult.getChart().get(row).isEmpty()) {
                            attrs = new AttributeBuilder(options);
                            attrs.addAttribute("n", ""+row);
                            handler.startElement("", "row", "row", attrs);

                            attrs = new AttributeBuilder(options);
                            for (EarleyItem item : eresult.getChart().get(row)) {
                                writeString(handler,"  ");
                                handler.startElement("", "item", "item", attrs);
                                writeString(handler, item.toString());
                                handler.endElement("", "item", "item");
                            }

                            handler.endElement("", "row", "row");
                        }
                    }
                    handler.endElement("", "chart", "chart");
                }
            }

            handler.endElement("", "fail", "failed");
            handler.endDocument();
        } catch (SAXException ex) {
            throw IxmlException.parseFailed(ex);
        }
    }

    private List<Token> couldBeNext(Set<TerminalSymbol> symbols) {
        ArrayList<Token> next = new ArrayList<>();
        for (TerminalSymbol symbol : symbols) {
            if (symbol.getToken() != null) {
                next.add(symbol.getToken());
            }
        }
        return next;
    }

    private List<Token> couldBeNext(EarleyChart chart, Grammar grammar) {
        ArrayList<Token> next = new ArrayList<>();
        List<TerminalSymbol> symbols = couldBeNextSymbols(chart, grammar);
        for (TerminalSymbol symbol : symbols) {
            if (symbol.getToken() != null) {
                next.add(symbol.getToken());
            }
        }
        return next;
    }

    private List<TerminalSymbol> couldBeNextSymbols(EarleyChart chart, Grammar grammar) {
        ArrayList<TerminalSymbol> nextChars = new ArrayList<>();
        HashSet<TerminalSymbol> nextSet = new HashSet<>();

        int lastrow = chart.size() - 1;
        while (lastrow >= 0 && chart.get(lastrow).isEmpty()) {
            lastrow--;
        }

        if (lastrow < 0 || chart.get(lastrow).isEmpty()) {
            return nextChars;
        }

        HashSet<Symbol> nextSymbols = new HashSet<>();
        for (EarleyItem item : chart.get(lastrow)) {
            State state = item.state;
            if (state != null && !state.completed()) {
                if (state.nextSymbol() instanceof TerminalSymbol) {
                    nextSet.add((TerminalSymbol) state.nextSymbol());
                } else {
                    nextSymbols.add(state.nextSymbol());
                }
            }
        }

        for (Symbol s: nextSymbols) {
            for (Rule rule : grammar.getRules()) {
                if (rule.getSymbol().equals(s) && !rule.getRhs().isEmpty()) {
                    if (rule.getRhs().get(0) instanceof TerminalSymbol) {
                        nextSet.add((TerminalSymbol) rule.getRhs().get(0));
                    }
                }
            }
        }

        nextChars.addAll(nextSet);
        return nextChars;
    }

    private NextTokenDetail couldBeNextDetail(EarleyChart chart, Grammar grammar) {
        NextTokenDetail detail = new NextTokenDetail();

        int lastrow = chart.size() - 1;
        while (lastrow >= 0 && chart.get(lastrow).isEmpty()) {
            lastrow--;
        }

        if (lastrow < 0 || chart.get(lastrow).isEmpty()) {
            return detail;
        }

        HashSet<Symbol> nextSymbols = new HashSet<>();
        for (EarleyItem item : chart.get(lastrow)) {
            State state = item.state;
            if (state != null && !state.completed()) {
                if (state.nextSymbol() instanceof TerminalSymbol) {
                    TerminalSymbol symbol = (TerminalSymbol) state.nextSymbol();
                    if (symbol.getToken() != null) {
                        detail.add(symbol.getToken(), state.symbol);
                    }
                } else {
                    nextSymbols.add(state.nextSymbol());
                }
            }
        }

        for (Symbol s: nextSymbols) {
            for (Rule rule : grammar.getRules()) {
                if (!rule.symbol.symbolName.startsWith("$") && rule.getSymbol().equals(s) && !rule.getRhs().isEmpty()) {
                    Symbol symbol = rule.getRhs().get(0);
                    if (symbol instanceof TerminalSymbol && ((TerminalSymbol) symbol).getToken() != null) {
                        detail.add(((TerminalSymbol) symbol).getToken(), rule.symbol);
                    }
                }
            }
        }

        return detail;
    }

    private void writeString(ContentHandler handler, String str) throws SAXException {
        handler.characters(str.toCharArray(), 0, str.length());
    }

    private void atomicValue(ContentHandler handler, String name, String value) throws SAXException {
        handler.startElement("", name, name, AttributeBuilder.EMPTY_ATTRIBUTES);
        handler.characters(value.toCharArray(), 0, value.length());
        handler.endElement("", name, name);
    }

    private String tokenList(List<Token> oknext) throws SAXException {
        // I don't actually care about the order,
        // but let's not just make it HashMap random for testing if nothing else.
        ArrayList<String> chars = new ArrayList<>();
        for (Token next : oknext) {
            chars.add(next.toString());
        }
        Collections.sort(chars);

        StringBuilder sb = new StringBuilder();
        for (int pos = 0; pos < chars.size(); pos++) {
            if (pos > 0) {
                sb.append(", ");
            }
            sb.append(chars.get(pos));
        }

        return sb.toString();
    }

    private void tokenList(ContentHandler handler, List<Token> oknext, String elemName) throws SAXException {
        atomicValue(handler, elemName, tokenList(oknext));
    }

    private static class NextTokenDetail {
        private final Set<Token> tokenSet = new HashSet<>();
        private final HashMap<NonterminalSymbol,List<Token>> nextTokens = new HashMap<>();

        public Map<NonterminalSymbol,List<Token>> getNextTokens() {
            return nextTokens;
        }

        public Set<Token> getTokenSet() {
            return tokenSet;
        }

        public void add(Token token, NonterminalSymbol rule) {
            tokenSet.add(token);

            if (!nextTokens.containsKey(rule)) {
                nextTokens.put(rule, new ArrayList<>());
            }
            ArrayList<Token> list = (ArrayList<Token>) nextTokens.get(rule);
            if (!list.contains(token)) {
                list.add(token);
            }
        }
    }

    private static class StringContentHandler implements ContentHandler {
        private final StringBuilder sb;
        private final HashMap<String,String> prefixMappings;


        public StringContentHandler() {
            sb = new StringBuilder();
            prefixMappings = new HashMap<>();
        }

        public String getString() {
            return sb.toString();
        }

        @Override
        public void setDocumentLocator(Locator locator) {
            // nop
        }

        @Override
        public void startDocument() throws SAXException {
            // nop
        }

        @Override
        public void endDocument() throws SAXException {
            // nop
        }

        @Override
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            prefixMappings.put(prefix, uri);
        }

        @Override
        public void endPrefixMapping(String prefix) throws SAXException {
            // nop; this handler doesn't really support the API
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            sb.append("<").append(qName);
            for (String prefix : prefixMappings.keySet()) {
                if ("".equals(prefix)) {
                    sb.append(" xmlns=\"");
                } else {
                    sb.append(" xmlns:").append(prefix).append("=\"");
                }
                sb.append(prefixMappings.get(prefix)).append("\"");
            }
            prefixMappings.clear();
            for (int pos = 0; pos < atts.getLength(); pos++) {
                sb.append(" ").append(atts.getQName(pos)).append("=\"");
                sb.append(xmlEscape(atts.getValue(pos))).append("\"");
            }
            sb.append(">");
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            sb.append("</").append(qName).append(">");
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            String text = String.valueOf(ch, start, length);
            sb.append(xmlEscape(text));
        }

        @Override
        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
            // nop
        }

        @Override
        public void processingInstruction(String target, String data) throws SAXException {
            sb.append("<?").append(target);
            if (data != null && !"".equals(data)) {
                sb.append(xmlEscape(data)).append("?>");
            }
            sb.append("?>");
        }

        @Override
        public void skippedEntity(String name) throws SAXException {
            // nop
        }

        private String xmlEscape(String input) {
            return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;");
        }
    }
}
