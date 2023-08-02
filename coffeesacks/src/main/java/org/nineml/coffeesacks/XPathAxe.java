package org.nineml.coffeesacks;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.hof.UserFunctionReference;
import net.sf.saxon.ma.map.KeyValuePair;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.s9api.*;
import net.sf.saxon.trans.UncheckedXPathException;
import net.sf.saxon.trans.XPathException;
import org.nineml.coffeefilter.InvisibleXmlParser;
import org.nineml.coffeegrinder.parser.Family;
import org.nineml.coffeegrinder.parser.ForestNode;
import org.nineml.coffeegrinder.trees.Arborist;
import org.nineml.coffeegrinder.trees.Axe;
import org.nineml.coffeegrinder.trees.ParseTree;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@link Axe} used by CoffeeSacks to choose between ambiguous alternatives.
 * <p>At the moment, this is <em>not</em> a specialist axe. To implement more general
 * selections (random ones, for example), some provision would have to be made for creating
 * either a "normal" or a specialist axe. TBD.</p>
 */
public class XPathAxe implements Axe {
    public static final XdmAtomicValue _input = new XdmAtomicValue("input");
    public static final XdmAtomicValue _forest = new XdmAtomicValue("forest");
    public static final XdmAtomicValue _available_choices = new XdmAtomicValue("available-choices");
    public static final XdmAtomicValue _other_choices = new XdmAtomicValue("other-choices");
    public static final XdmAtomicValue _selection = new XdmAtomicValue("selection");
    public static final XdmAtomicValue _ambiguous_choice = new XdmAtomicValue("ambiguous-choice");

    protected final Processor processor;
    protected final InvisibleXmlParser parser;
    protected final XmlForest forest;
    protected final XdmAtomicValue input;

    protected XPathContext context = null;
    private XdmMap accumulator = new XdmMap();
    protected UserFunctionReference.BoundUserFunction chooseAlternatives = null;
    private boolean madeAmbiguousChoice = false;
    private boolean lastChoiceWasAmbiguous = false;

    public XPathAxe(Processor processor, InvisibleXmlParser parser, XmlForest forest, String input) {
        this.processor = processor;
        this.parser = parser;
        this.forest = forest;
        this.input = new XdmAtomicValue(input);
    }

    public void setChooseFunction(XPathContext context, UserFunctionReference.BoundUserFunction chooseFunction) {
        this.context = context;
        this.chooseAlternatives = chooseFunction;
    }

    @Override
    public boolean isSpecialist() {
        // Fix JavaDoc if you change this.
        return false;
    }

    @Override
    public List<Family> select(ParseTree tree, ForestNode forestNode, int count, List<Family> choices) {
        if (chooseAlternatives == null) {
            lastChoiceWasAmbiguous = true;
            madeAmbiguousChoice = true;
            return choices;
        }

        XdmNode node = forest.choiceIndex.get("C" + choices.get(0).id);
        XdmMap map = new XdmMap();
        map = map.put(_input, input);

        XdmValue seq = XdmEmptySequence.getInstance();
        for (Family choice : choices) {
            seq = seq.append(new XdmAtomicValue("C" + choice.id));
        }
        map = map.put(_available_choices, seq);

        for (XdmAtomicValue key : accumulator.keySet()) {
            map = map.put(key, accumulator.get(key));
        }

        lastChoiceWasAmbiguous = false;
        String selection = null;
        try {
            Sequence result = chooseAlternatives.call(context, new Sequence[] { node.getUnderlyingNode(), map.getUnderlyingValue() });
            MapItem newMap = (MapItem) result.head();
            map = new XdmMap();
            for (KeyValuePair pair : newMap.keyValuePairs()) {
                XdmAtomicValue key = new XdmAtomicValue(pair.key);
                if (!_forest.equals(key) && !_selection.equals(key)
                        && !_available_choices.equals(key) && !_other_choices.equals(key)
                        && !_ambiguous_choice.equals(key)) {
                    map = map.put(key, XdmValue.wrap(pair.value));
                }
                if (_selection.equals(key)) {
                    selection = pair.value.getStringValue();
                }
                if (_ambiguous_choice.equals(key)) {
                    lastChoiceWasAmbiguous = pair.value.effectiveBooleanValue();
                    madeAmbiguousChoice = madeAmbiguousChoice || lastChoiceWasAmbiguous;
                }
            }
            accumulator = map;

            if (selection == null) {
                throw new CoffeeSacksException(CoffeeSacksException.ERR_INVALID_CHOICE, "choose-alternative function must return a selection");
            }

            ArrayList<Family> selected = new ArrayList<>();
            for (Family choice : choices) {
                if (selection.equals("C" + choice.id)) {
                    selected.add(choice);
                }
            }
            if (!selected.isEmpty()) {
                return selected;
            }

            throw new CoffeeSacksException(CoffeeSacksException.ERR_INVALID_CHOICE, "choose-alternative function returned invalid selection: " + selection);
        } catch (XPathException ex) {
            throw new UncheckedXPathException(ex);
        }
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