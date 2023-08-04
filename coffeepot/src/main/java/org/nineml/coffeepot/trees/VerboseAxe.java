package org.nineml.coffeepot.trees;

import net.sf.saxon.PreparedStylesheet;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.functions.FunctionLibrary;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.query.QueryModule;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.s9api.*;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.FunctionItemType;
import org.nineml.coffeefilter.InvisibleXmlDocument;
import org.nineml.coffeefilter.InvisibleXmlParser;
import org.nineml.coffeegrinder.parser.Family;
import org.nineml.coffeegrinder.parser.ForestNode;
import org.nineml.coffeegrinder.trees.Arborist;
import org.nineml.coffeegrinder.trees.Axe;
import org.nineml.coffeegrinder.trees.ParseTree;
import org.nineml.coffeegrinder.trees.PriorityAxe;
import org.nineml.coffeepot.managers.Configuration;
import org.nineml.coffeepot.utils.NodeUtils;
import org.nineml.coffeepot.utils.ParserOptions;
import org.nineml.coffeesacks.XmlForest;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class VerboseAxe extends PriorityAxe {
    public static final String logcategory = "CoffeePot";

    private static final QName _id = new QName("id");
    private static final QName _children = new QName("children");

    public static final XdmAtomicValue _input = new XdmAtomicValue("input");
    public static final XdmAtomicValue _forest = new XdmAtomicValue("forest");
    public static final XdmAtomicValue _available_choices = new XdmAtomicValue("available-choices");
    public static final XdmAtomicValue _other_choices = new XdmAtomicValue("other-choices");
    public static final XdmAtomicValue _selection = new XdmAtomicValue("selection");
    public static final XdmAtomicValue _ambiguous_choice = new XdmAtomicValue("ambiguous-choice");
    private static final QName _context = new QName("context");
    private static final QName _options = new QName("options");
    public static final String CPNS = "https://coffeepot.nineml.org/ns/functions";
    public static final StructuredQName CP_CHOOSE = new StructuredQName("cp", CPNS, "choose-alternative");
    private final Processor processor;
    private final XmlForest forest;
    private final ParserOptions options;
    private final XdmAtomicValue input;
    private final List<String> expressions = new ArrayList<>();
    private String functionLibrary = null;
    private XdmMap accumulator = new XdmMap();
    private boolean lastChoiceWasAmbiguous = false;
    private boolean madeAmbiguousChoice = false;

    public VerboseAxe(Configuration config, InvisibleXmlParser parser, XmlForest forest, InvisibleXmlDocument document, String input) {
        this.processor = config.processor;
        this.options = config.options;
        this.input = new XdmAtomicValue(input);
        this.forest = forest;
    }

    public void addExpression(String expr) {
        expressions.add(expr);
    }

    public void addFunctionLibrary(String uri) {
        String sed = processor.getUnderlyingConfiguration().getEditionCode();
        if ("HE".equals(sed)) {
            throw new IllegalStateException("Function library support requires Saxon PE or Saxon EE");
        }
        functionLibrary = uri;

        try {
            XPathCompiler compiler = processor.newXPathCompiler();
            StaticContext staticContext = compiler.getUnderlyingStaticContext();

            String cwd = System.getProperty("user.dir").replace('\\', '/');
            cwd += cwd.endsWith("/") ? "" : "/";
            URI baseURI;
            if (cwd.startsWith("/")) {
                baseURI = new URI("file:" + cwd);
            } else {
                baseURI = new URI("file:///" + cwd);
            }
            String staticBaseURI = staticContext.getStaticBaseURI();
            if (staticBaseURI != null && !"".equals(staticBaseURI)) {
                baseURI = baseURI.resolve(staticBaseURI);
            }

            URL url = baseURI.resolve(functionLibrary).toURL();

            options.getLogger().debug(logcategory, "Loading function library: %s", url);
            URLConnection connection = url.openConnection();
            final FunctionLibrary fl;
            if (functionLibrary.contains("xsl")) {
                Source xslt = new SAXSource(new InputSource(connection.getInputStream()));
                XsltCompiler xsltCompiler = processor.newXsltCompiler();
                XsltExecutable xsltExec = xsltCompiler.compile(xslt);
                PreparedStylesheet ps = xsltExec.getUnderlyingCompiledStylesheet();
                fl = ps.getFunctionLibrary();
            } else {
                XQueryCompiler xqcomp = processor.newXQueryCompiler();
                StaticQueryContext sqc = xqcomp.getUnderlyingStaticContext();
                sqc.compileLibrary(connection.getInputStream(), "utf-8");
                XQueryExpression xqe = sqc.compileQuery("import module namespace cp='" + CPNS + "'; .");
                QueryModule qm = xqe.getMainModule();
                fl = qm.getGlobalFunctionLibrary();
            }

            // Set the function library with reflection so that this code will compile with HE
            Class<?> pc = Class.forName("com.saxonica.config.ProfessionalConfiguration");
            Class<?> fc = Class.forName("net.sf.saxon.functions.FunctionLibrary");
            Method setBinder = pc.getMethod("setExtensionBinder", String.class, fc);
            setBinder.invoke(processor.getUnderlyingConfiguration(), "coffeepot", fl);

            // Did you actually provide the function we need?
            SymbolicName.F fname = new SymbolicName.F(CP_CHOOSE, 2);

            // Object so it works with either Saxon 11 or Saxon 12. See below.
            Object chooseAlternative = fl.getFunctionItem(fname, staticContext);

            if (chooseAlternative == null) {
                throw new IllegalArgumentException("Function library does not provide suitable function: " + functionLibrary);
            }

            // Make this code work for either Saxon 11 or Saxon 12
            Class<?> klass = null;
            try {
                // Saxon 11
                klass = Class.forName("net.sf.saxon.om.Function");
            } catch (ClassNotFoundException ex11) {
                try {
                    // Saxon 12
                    klass = Class.forName("net.sf.saxon.om.FunctionItem");
                } catch (ClassNotFoundException ex12) {
                    throw new RuntimeException(ex12);
                }
            }

            final FunctionItemType ctype;
            try {
                Method getFit = klass.getMethod("getFunctionItemType");
                ctype = (FunctionItemType) getFit.invoke(chooseAlternative);
            } catch (IllegalAccessException | IllegalArgumentException ex) {
                throw new RuntimeException(ex);
            }

            if (ctype.getResultType().getPrimaryType() != MapType.ANY_MAP_TYPE) {
                throw new IllegalArgumentException("The choose-alternative function must return a map");
            }
            if (ctype.getArgumentTypes()[0].getPrimaryType() != NodeKindTest.ELEMENT) {
                throw new IllegalArgumentException("The first argument to the choose-alternative function must be an element");
            }
            if (ctype.getArgumentTypes()[1].getPrimaryType() != MapType.ANY_MAP_TYPE) {
                throw new IllegalArgumentException("The second argument to the choose-alternative function must be a map");
            }
        } catch (SaxonApiException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                 InvocationTargetException | XPathException | URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Family> select(ParseTree tree, ForestNode forestNode, int count, List<Family> choices) {
        lastChoiceWasAmbiguous = false;
        List<Family> selected = new ArrayList<>();

        HashMap<String,Family> choiceMap = new HashMap<>();
        for (Family choice : choices) {
            choiceMap.put("C" + choice.id, choice);
        }

        final XdmNode node = NodeUtils.getAmbiguityContext(processor, forest, choices.get(0));
        try {
            XdmMap map = new XdmMap();
            map = map.put(_forest, forest.getXml());
            map = map.put(_input, input);

            XdmValue seq = XdmEmptySequence.getInstance();
            for (Family choice : choices) {
                seq = seq.append(new XdmAtomicValue("C" + choice.id));
            }
            map = map.put(_available_choices, seq);

            for (XdmAtomicValue key : accumulator.keySet()) {
                map = map.put(key, accumulator.get(key));
            }

            if (functionLibrary == null) {
                for (String expr : expressions) {
                    XPathCompiler compiler = processor.newXPathCompiler();
                    XPathExecutable exec = compiler.compile(expr);
                    XPathSelector selector = exec.load();

                    selector.setContextItem(node);
                    XdmValue selection = selector.evaluate();

                    if (selection.size() == 1 && selection instanceof XdmNode) {
                        XdmNode selNode = (XdmNode) selection;
                        // If the expression selects the ID attribute, make that work
                        if (_id.equals(selNode.getNodeName()) && selNode.getNodeKind() == XdmNodeKind.ATTRIBUTE) {
                            selNode = selNode.getParent();
                        }
                        if (_children.equals(selNode.getNodeName())) {
                            String id = selNode.getAttributeValue(_id);
                            if (choiceMap.containsKey(id)) { // it has to, right?
                                options.getLogger().debug("Expression %s selected %s", expr, selection);
                                selected.add(choiceMap.get(id));
                            }
                        } else {
                            options.getLogger().debug("Expression %s did not select an element named 'children'.", expr);
                        }
                    } else {
                        if (selection.size() > 1) {
                            options.getLogger().debug("Expression %s selected %d nodes", expr, selection.size());
                        } else {
                            options.getLogger().debug("Expression %s did not select an element named 'children'.", expr);
                        }
                    }

                    if (!selected.isEmpty()) {
                        break;
                    }

                    options.getLogger().debug("Expression %s did not make a selection", expr);
                }
            } else  {
                XPathCompiler compiler = processor.newXPathCompiler();
                compiler.declareNamespace("f", "https://coffeepot.nineml.org/ns/functions");
                compiler.declareVariable(_context);
                compiler.declareVariable(_options);
                XPathExecutable exec = compiler.compile("f:choose-alternative($context, $options)");
                XPathSelector selector = exec.load();
                selector.setVariable(_context, node);
                selector.setVariable(_options, map);
                Map<XdmAtomicValue,XdmValue> newMap = selector.evaluateSingle().asMap();

                String selection = null;
                map = new XdmMap();
                for (XdmAtomicValue key : newMap.keySet()) {
                    if (!_forest.equals(key) && !_selection.equals(key)
                            && !_available_choices.equals(key) && !_other_choices.equals(key)
                            && !_ambiguous_choice.equals(key)) {
                        map = map.put(key, newMap.get(key));
                    }
                    if (_selection.equals(key)) {
                        selection = newMap.get(key).getUnderlyingValue().getStringValue();
                    }
                    if (_ambiguous_choice.equals(key)) {
                        lastChoiceWasAmbiguous = newMap.get(key).getUnderlyingValue().effectiveBooleanValue();
                        madeAmbiguousChoice = madeAmbiguousChoice || lastChoiceWasAmbiguous;
                    }
                }
                accumulator = map;

                if (selection == null) {
                    throw new IllegalArgumentException("choose-alternatives function must return a selection");
                }

                if (choiceMap.containsKey(selection)) {
                    selected.add(choiceMap.get(selection));
                } else {
                    throw new IllegalArgumentException("choose-alternatives function returned an invalid selection: " + selection);
                }
            }
        } catch (SaxonApiException | XPathException e) {
            throw new RuntimeException(e);
        }

        if (selected.isEmpty()) {
            selected = super.select(tree, forestNode, count, choices);
            lastChoiceWasAmbiguous = super.wasAmbiguousSelection();
            madeAmbiguousChoice = madeAmbiguousChoice || lastChoiceWasAmbiguous;
        }

        return selected;
    }

    @Override
    public boolean wasAmbiguousSelection() {
        return lastChoiceWasAmbiguous;
    }

    @Override
    public void forArborist(Arborist arborist) {
        // nop
    }
}
