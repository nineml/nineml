package org.nineml.coffeegrinder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.trees.Arborist;
import org.nineml.coffeegrinder.trees.RandomAxe;
import org.nineml.coffeegrinder.trees.StringTreeBuilder;
import org.nineml.coffeegrinder.util.Iterators;

import java.util.Arrays;
import java.util.HashSet;

public class RandomAxeTest extends CoffeeGrinderTest {
    @Test
    public void fourparsesnoloop() {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType("GLL");

        SourceGrammar grammar = new SourceGrammar();

        /*
        S = 'x', (A | B1), 'y' .
        A = 'a' | B2 .
        B1 = 'b' | A .
        B2 = 'b' | 'a' .
        */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B1 = grammar.getNonterminal("B1");
        NonterminalSymbol _B2 = grammar.getNonterminal("B2");

        grammar.addRule(_S, TerminalSymbol.ch('x'), _A, TerminalSymbol.ch('y'));
        grammar.addRule(_S, TerminalSymbol.ch('x'), _B1, TerminalSymbol.ch('y'));
        grammar.addRule(_A, TerminalSymbol.ch('a'));
        grammar.addRule(_A, _B2);
        grammar.addRule(_B1, TerminalSymbol.ch('b'));
        grammar.addRule(_B1, _A);
        grammar.addRule(_B2, TerminalSymbol.ch('b'));
        grammar.addRule(_B2, TerminalSymbol.ch('a'));

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(Iterators.characterIterator("xay"));

        Assertions.assertTrue(result.succeeded());
        Assertions.assertEquals(4, result.getForest().getParseTreeCount());

        //result.getForest().serialize("noloops.xml");

        HashSet<String> validParses = new HashSet<>();
        validParses.add("<S>x<A>a</A>y</S>");
        validParses.add("<S>x<A><B2>a</B2></A>y</S>");
        validParses.add("<S>x<B1><A>a</A></B1>y</S>");
        validParses.add("<S>x<B1><A><B2>a</B2></A></B1>y</S>");

        HashSet<String> foundParses = new HashSet<>();

        for (int i = 0; i < 100; i++) {
            Arborist walker = Arborist.getArborist(result.getForest(), new RandomAxe());
            StringTreeBuilder builder = new StringTreeBuilder();
            walker.getTree(builder);
            String xml = builder.getTree();
            Assertions.assertTrue(validParses.contains(xml));
            foundParses.add(xml);
        }

        // This is only highly probably, not guaranteed.
        Assertions.assertEquals(4, foundParses.size());
    }
}
