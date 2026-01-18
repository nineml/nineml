package org.nineml.coffeegrinder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.trees.Arborist;
import org.nineml.coffeegrinder.trees.StringTreeBuilder;
import org.nineml.coffeegrinder.util.Iterators;
import org.nineml.coffeegrinder.util.ParserAttribute;

public class TreeTest {
    private final ParserOptions options = new ParserOptions();

    @Test
    public void testEmpty() {
        SourceGrammar grammar = new SourceGrammar();

        // S: a, b, c. a:. b:. c:.

        NonterminalSymbol S = grammar.getNonterminal("S");
        //NonterminalSymbol T = grammar.getNonterminal("T");
        NonterminalSymbol a = grammar.getNonterminal("a");
        NonterminalSymbol b = grammar.getNonterminal("b");
        NonterminalSymbol c = grammar.getNonterminal("c");

        //grammar.addRule(S, T);
        grammar.addRule(S, a, b, c);
        grammar.addRule(S);
        grammar.addRule(a);
        grammar.addRule(b);
        grammar.addRule(c);

        GearleyParser parser = grammar.getParser(options, S);
        GearleyResult result = parser.parse(Iterators.characterIterator(""));

        //result.getForest().serialize("/tmp/graph.xml");
        //result.getForest().parse().serialize("tree.xml");

        Assertions.assertTrue(result.succeeded());

        Assertions.assertEquals(2, result.getForest().getParseTreeCount());
    }

    @Test
    public void testAmbiguous() {
        SourceGrammar grammar = new SourceGrammar();

        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol A = grammar.getNonterminal("A");
        NonterminalSymbol d1 = grammar.getNonterminal("$1"); //, true);
        NonterminalSymbol d2 = grammar.getNonterminal("$2"); //, true);
        NonterminalSymbol d3 = grammar.getNonterminal("$3"); //, true);
        NonterminalSymbol d4 = grammar.getNonterminal("$4"); //, true);

        grammar.addRule(S, A);
        grammar.addRule(A, d1);
        grammar.addRule(A, d3);
        grammar.addRule(d1, d2);
        grammar.addRule(d1);
        grammar.addRule(d2, TerminalSymbol.ch('a'), d2);
        grammar.addRule(d3, d4);
        grammar.addRule(d3);
        grammar.addRule(d4, TerminalSymbol.ch('b'), d4);

        GearleyParser parser = grammar.getParser(options, S);
        GearleyResult result = parser.parse(Iterators.characterIterator(""));
        Assertions.assertTrue(result.succeeded());

        Assertions.assertEquals(2, result.getForest().getParseTreeCount());
    }

    @Test
    public void testSymbols() {
        SourceGrammar grammar = new SourceGrammar();

        /*
        S = "a", B1, "c", B2, D, "e".
        B1 = "b".
        B2 = "b".
        D = .
         */

        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol B1 = grammar.getNonterminal("B");
        NonterminalSymbol B2 = grammar.getNonterminal("B", new ParserAttribute("name", "value"));
        NonterminalSymbol D = grammar.getNonterminal("D");

        TerminalSymbol _a = TerminalSymbol.ch('a');
        TerminalSymbol _b = TerminalSymbol.ch('b');
        TerminalSymbol _c = TerminalSymbol.ch('c');
        TerminalSymbol _e = TerminalSymbol.ch('e');

        grammar.addRule(S, _a, B1, _c, B2, D, _e);
        grammar.addRule(B1, _b);
        grammar.addRule(B2, _b);
        grammar.addRule(D);

        GearleyParser parser = grammar.getParser(options, S);
        GearleyResult result = parser.parse(Iterators.characterIterator("abcbe"));
        Assertions.assertTrue(result.succeeded());

        //result.getForest().serialize("testsymbols.xml");

        Assertions.assertEquals(1, result.getForest().getParseTreeCount());

        StringTreeBuilder builder = new StringTreeBuilder();
        Arborist xxx = Arborist.getArborist(result.getForest());
        xxx.getTree(builder);
        //System.err.println(builder.getTree());
    }

}
