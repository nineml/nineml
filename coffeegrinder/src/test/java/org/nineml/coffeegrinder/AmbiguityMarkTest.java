package org.nineml.coffeegrinder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.trees.Arborist;
import org.nineml.coffeegrinder.trees.StringTreeBuilder;
import org.nineml.coffeegrinder.util.Iterators;

import java.util.Arrays;

public class AmbiguityMarkTest extends CoffeeGrinderTest {
    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void verticalMarks(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);
        options.setMarkAmbiguities(true);

        SourceGrammar grammar = new SourceGrammar();

        /*
        S = A | B .
        A = "a" .
        B = "a" .
        */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B = grammar.getNonterminal("B");

        grammar.addRule(_S, _A);
        grammar.addRule(_S, _B);
        grammar.addRule(_A, TerminalSymbol.ch('a'));
        grammar.addRule(_B, TerminalSymbol.ch('a'));

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(Iterators.characterIterator("a"));

        //result.getForest().serialize("simple.xml");

        Assertions.assertTrue(result.succeeded());
        Assertions.assertTrue(result.getForest().isAmbiguous());
        Assertions.assertFalse(result.getForest().isInfinitelyAmbiguous());
        Assertions.assertEquals(2, result.getForest().getParseTreeCount());

        expectTrees(Arborist.getArborist(result.getForest()), Arrays.asList(
                "<S https://coffeegrinder.nineml.org/attr/ambiguous='true'><A>a</A></S>",
                "<S https://coffeegrinder.nineml.org/attr/ambiguous='true'><B>a</B></S>"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void horizontalMarks(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);
        options.setMarkAmbiguities(true);

        SourceGrammar grammar = new SourceGrammar();

        /*
        S = X, sep, Y
        X = "x", sep, Xstar
        Y = "y", sep, Ystar
        sep = ","
        sep = ()
        Xstar = Xoption
        Ystar = Yoption
        Xoption =
        Xoption = "x", sep, Xstar
        Yoption =
        Yoption = "y", sep, Ystar
        */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _X = grammar.getNonterminal("X");
        NonterminalSymbol _Xstar = grammar.getNonterminal("Xstar");
        NonterminalSymbol _Xoption = grammar.getNonterminal("Xoption");
        NonterminalSymbol _Y = grammar.getNonterminal("Y");
        NonterminalSymbol _Ystar = grammar.getNonterminal("Ystar");
        NonterminalSymbol _Yoption = grammar.getNonterminal("Yoption");
        NonterminalSymbol _sep = grammar.getNonterminal("sep");

        TerminalSymbol _x = TerminalSymbol.ch('x');
        TerminalSymbol _y = TerminalSymbol.ch('y');
        TerminalSymbol _comma = TerminalSymbol.ch(',');

        grammar.addRule(_S, _X, _sep, _Y);
        grammar.addRule(_X, _x, _sep, _Xstar);
        grammar.addRule(_Y, _y, _sep, _Ystar);
        grammar.addRule(_sep, _comma);
        grammar.addRule(_sep);
        grammar.addRule(_Xstar, _Xoption);
        grammar.addRule(_Ystar, _Yoption);
        grammar.addRule(_Xoption);
        grammar.addRule(_Xoption, _x, _sep, _Xstar);
        grammar.addRule(_Yoption);
        grammar.addRule(_Yoption, _y, _sep, _Ystar);

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse("x,x,y,y");

        //result.getForest().serialize("simple.xml");

        Assertions.assertTrue(result.succeeded());
        Assertions.assertTrue(result.getForest().isAmbiguous());
        Assertions.assertEquals(2, result.getForest().getParseTreeCount());

        Arborist walker = Arborist.getArborist(result.getForest());
        StringTreeBuilder builder = new StringTreeBuilder(true);
        walker.getTree(builder);
        String tree = builder.getTree();
        Assertions.assertTrue(tree.contains("<?start-ambiguity") && tree.contains("<?end-ambiguity"));

        builder = new StringTreeBuilder(false);
        walker.getTree(builder);
        tree = builder.getTree();
        Assertions.assertFalse(tree.contains("<?start-ambiguity") || tree.contains("<?end-ambiguity"));
    }
}
