package org.nineml.coffeesacks;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.hof.UserFunctionReference;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AnyURIValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import org.nineml.coffeefilter.InvisibleXmlDocument;
import org.nineml.coffeefilter.InvisibleXmlParser;
import org.nineml.coffeefilter.ParserOptions;
import org.nineml.coffeegrinder.parser.HygieneReport;
import org.nineml.coffeegrinder.parser.NonterminalSymbol;
import org.nineml.coffeegrinder.parser.Rule;
import org.xml.sax.InputSource;

import javax.xml.transform.sax.SAXSource;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A Saxon extension function to obtain the hygiene report for a grammar.
 */
public class HygieneReportFunction extends CommonDefinition {
    private static final StructuredQName qName =
            new StructuredQName("", "http://nineml.com/ns/coffeesacks", "hygiene-report");

    public HygieneReportFunction(Configuration config) {
        super(config);
    }

    @Override
    public StructuredQName getFunctionQName() {
        return qName;
    }

    @Override
    public int getMinimumNumberOfArguments() {
        return 1;
    }

    @Override
    public int getMaximumNumberOfArguments() {
        return 2;
    }

    @Override
    public SequenceType[] getArgumentTypes() {
        return new SequenceType[]{SequenceType.SINGLE_ITEM, SequenceType.OPTIONAL_ITEM};
    }

    @Override
    public SequenceType getResultType(SequenceType[] sequenceTypes) {
        return SequenceType.SINGLE_ITEM;
    }

    @Override
    public ExtensionFunctionCall makeCallExpression() {
        return new HygieneCall();
    }

    private class HygieneCall extends ExtensionFunctionCall {
        @Override
        public void supplyStaticContext(StaticContext context, int locationId, Expression[] arguments) throws XPathException {
            sourceLoc = context.getContainingLocation();
            if (context.getStaticBaseURI() != null && !"".equals(context.getStaticBaseURI())) {
                baseURI = URIUtils.resolve(URIUtils.cwd(), context.getStaticBaseURI());
            }
        }

        @Override
        public Sequence call(XPathContext context, Sequence[] sequences) throws XPathException {
            final ParserOptions popts;
            HashMap<String, String> options = new HashMap<>();
            if (sequences.length > 1) {
                Item item = sequences[1].head();
                if (item instanceof MapItem) {
                    Map<String,Object> parsedMap = parseMap((MapItem) item);
                    for (Map.Entry<String,Object> entry : parsedMap.entrySet()) {
                        if ("choose-alternative".equals(entry.getKey())) {
                            // ignore
                        } else {
                            options.put(entry.getKey(), (String) entry.getValue());
                        }
                    }
                    popts = checkOptions(options);
                } else {
                    throw new CoffeeSacksException(CoffeeSacksException.ERR_BAD_OPTIONS, "Options must be a map", sourceLoc);
                }
            } else {
                popts = new ParserOptions(parserOptions);
            }

            Sequence input = sequences[0].head();
            final InvisibleXmlParser parser;
            if (input instanceof AnyURIValue) {
                String grammarHref = ((AnyURIValue) input).getStringValue();
                URI grammarURI;
                if (baseURI != null) {
                    grammarURI = baseURI.resolve(grammarHref);
                } else {
                    grammarURI = URIUtils.resolve(URIUtils.cwd(), grammarHref);
                }
                parser = parserFromURI(context, grammarURI, popts, options);
            } else if (input instanceof StringValue) {
                parser = parserFromString(context, ((StringValue) input).getStringValue(), popts, options);
            } else {
                throw new CoffeeSacksException(CoffeeSacksException.ERR_BAD_GRAMMAR, "Grammar must be a string or a URI", sourceLoc);
            }

            try {
                if (parser.constructed()) {
                    // Can this ever fail?
                    Processor processor = (Processor) context.getConfiguration().getProcessor();

                    HygieneReport report = parser.getHygieneReport();

                    String format = options.getOrDefault(_format, "xml");
                    if ("xml".equals(format)) {
                        return xmlReport(processor, report);
                    } else {
                        return jsonReport(processor, report);
                    }
                } else {
                    InvisibleXmlDocument failed = parser.getFailedParse();
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

    private NodeInfo xmlReport(Processor processor, org.nineml.coffeegrinder.parser.HygieneReport report) throws SaxonApiException {
        // FIXME: use an actual builder of some sort!
        StringBuilder sb = new StringBuilder();
        sb.append("<report xmlns='http://nineml.com/ns/coffeegrinder' ");
        sb.append("clean='").append(report.isClean()).append("'>");

        if (!report.getUndefinedSymbols().isEmpty()) {
            sb.append("<undefined>");
            reportXmlSymbols(sb, report.getUndefinedSymbols());
            sb.append("</undefined>\n");
        }

        if (!report.getUnreachableSymbols().isEmpty()) {
            sb.append("<unreachable>");
            reportXmlSymbols(sb, report.getUnreachableSymbols());
            sb.append("</unreachable>\n");
        }

        if (!report.getUnproductiveSymbols().isEmpty()) {
            sb.append("<unproductive>");
            reportXmlSymbols(sb, report.getUndefinedSymbols());
            for (Rule rule : report.getUnproductiveRules()) {
                sb.append("<rule>").append(rule).append("</rule>\n");
            }
            sb.append("</unproductive>\n");
        }

        sb.append("</report>");

        DocumentBuilder builder = processor.newDocumentBuilder();
        ByteArrayInputStream bais = new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));
        SAXSource source = new SAXSource(new InputSource(bais));
        XdmNode result = builder.build(source);
        return result.getUnderlyingNode();
    }

    private Item jsonReport(Processor processor, org.nineml.coffeegrinder.parser.HygieneReport report) throws SaxonApiException {
        // FIXME: use an actual builder of some sort!
        StringBuilder sb = new StringBuilder();
        sb.append("{\"report\": {").append("\"clean\":").append(report.isClean());

        if (!report.getUndefinedSymbols().isEmpty()) {
            sb.append(",\n");
            sb.append("\"undefined\":[");
            reportJsonSymbols(sb, report.getUndefinedSymbols());
            sb.append("]");
        }

        if (!report.getUnreachableSymbols().isEmpty()) {
            sb.append(",\n");
            sb.append("\"unreachable\":[");
            reportJsonSymbols(sb, report.getUnreachableSymbols());
            sb.append("]");
        }

        if (!report.getUnproductiveSymbols().isEmpty()) {
            sb.append(",\n");
            sb.append("\"unproductive\":[");
            reportJsonSymbols(sb, report.getUnproductiveSymbols());
            for (Rule rule : report.getUnproductiveRules()) {
                sb.append(",\"").append(rule).append("\"");
            }
            sb.append("]");
        }

        sb.append("}}");
        return jsonToXDM(processor, sb.toString());
    }

    private void reportXmlSymbols(StringBuilder sb, Set<NonterminalSymbol> symbols) {
        for (NonterminalSymbol symbol : symbols) {
            sb.append("<symbol>").append(symbol).append("</symbol>\n");
        }
    }

    private void reportJsonSymbols(StringBuilder sb, Set<NonterminalSymbol> symbols) {
        String sep = "";
        for (NonterminalSymbol symbol : symbols) {
            sb.append(sep).append("\"").append(symbol).append("\"");
            sep = ",";
        }
    }
}
