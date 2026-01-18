package org.nineml.coffeegrinder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.tokens.TokenCharacter;
import org.nineml.coffeegrinder.trees.*;
import org.nineml.coffeegrinder.util.ParserAttribute;

public class PriorityTest extends CoffeeGrinderTest {
    @Test
    public void infiniteLoop() {
        ParserOptions options = new ParserOptions(globalOptions);
        SourceGrammar grammar = new SourceGrammar(new ParserOptions());

        /*
        S: A .
        A: 'a', B ; 'x' .
        B: 'b', A ; LDOE, A .
        LDOE: M; 'l' .
        M: 'm'; LDOE .
        */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B = grammar.getNonterminal("B");
        NonterminalSymbol _LDOE = grammar.getNonterminal("LDOE");
        NonterminalSymbol _M = grammar.getNonterminal("M", new ParserAttribute(ForestNode.PRIORITY_ATTRIBUTE, "5"));

        TerminalSymbol _a = TerminalSymbol.ch('a');
        TerminalSymbol _b = TerminalSymbol.ch('b');
        TerminalSymbol _x = TerminalSymbol.ch('x');
        TerminalSymbol _l = TerminalSymbol.ch('l');
        TerminalSymbol _m = new TerminalSymbol(TokenCharacter.get('m', new ParserAttribute(ForestNode.PRIORITY_ATTRIBUTE, "10")));

        grammar.addRule(_S, _A);
        grammar.addRule(_A, _a, _B);
        grammar.addRule(_A, _x);
        grammar.addRule(_B, _b, _A);
        grammar.addRule(_B, _LDOE, _A);
        grammar.addRule(_LDOE, _l);
        grammar.addRule(_LDOE, _M);
        grammar.addRule(_M, _LDOE);
        grammar.addRule(_M, _m);

        options.setPriorityStyle("sum");
        options.setParserType("GLL");

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse("amalx");

        //result.getForest().serialize("infiniteLoop.xml");

        Assertions.assertTrue(result.getForest().isAmbiguous());
        Assertions.assertTrue(result.getForest().isInfinitelyAmbiguous());

        TreeBuilder builder = new NopTreeBuilder();
        Arborist walker = Arborist.getArborist(result.getForest());
        walker.getTree(builder);
        Assertions.assertTrue(walker.isAmbiguous());

        Axe axe = new PriorityAxe();
        walker = Arborist.getArborist(result.getForest(), axe);
        walker.getTree(builder);

        // N.B. Prior to 3.0.0e, this was not an ambiguous choice because "forced choices"
        // to avoid a loop were considered unambiguous. But that's not true. You *could*
        // go back around the loop, you're just choosing not to. That's now captured in
        // isAbsolutelyAmbiguous
        Assertions.assertFalse(walker.isAmbiguous());
        Assertions.assertTrue(walker.isAbsolutelyAmbiguous());
    }

    @Test
    public void infiniteLoopNoLoops() {
        ParserOptions options = new ParserOptions(globalOptions);
        SourceGrammar grammar = new SourceGrammar(new ParserOptions());

        /*
        S: A .
        A: 'a', B ; 'x' .
        B: 'b', A ; LDOE, A .
        LDOE: M; 'l' .
        M: 'm'; LDOE .
        */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B = grammar.getNonterminal("B");
        NonterminalSymbol _LDOE = grammar.getNonterminal("LDOE");
        NonterminalSymbol _M = grammar.getNonterminal("M", new ParserAttribute(ForestNode.PRIORITY_ATTRIBUTE, "5"));

        TerminalSymbol _a = TerminalSymbol.ch('a');
        TerminalSymbol _b = TerminalSymbol.ch('b');
        TerminalSymbol _x = TerminalSymbol.ch('x');
        TerminalSymbol _l = TerminalSymbol.ch('l');
        TerminalSymbol _m = new TerminalSymbol(TokenCharacter.get('m', new ParserAttribute(ForestNode.PRIORITY_ATTRIBUTE, "10")));

        grammar.addRule(_S, _A);
        grammar.addRule(_A, _a, _B);
        grammar.addRule(_A, _x);
        grammar.addRule(_B, _b, _A);
        grammar.addRule(_B, _LDOE, _A);
        grammar.addRule(_LDOE, _l);
        grammar.addRule(_LDOE, _M);
        grammar.addRule(_M, _LDOE);
        grammar.addRule(_M, _m);

        options.setPriorityStyle("sum");
        options.setParserType("GLL");

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse("amalx");

        //result.getForest().serialize("infiniteLoop1noloop.xml");

        Assertions.assertTrue(result.getForest().isAmbiguous());
        Assertions.assertTrue(result.getForest().isInfinitelyAmbiguous());

        StringTreeBuilder builder = new StringTreeBuilder();
        Axe axe = new PriorityAxe(true);
        Arborist walker = Arborist.getArborist(result.getForest(), axe);
        walker.getTree(builder);
        Assertions.assertFalse(walker.isAmbiguous());
        Assertions.assertTrue(walker.isAbsolutelyAmbiguous());

        Assertions.assertEquals("<S><A>a<B><LDOE><M>m</M></LDOE><A>a<B><LDOE>l</LDOE><A>x</A></B></A></B></A></S>", builder.getTree());
        Assertions.assertTrue(walker.hasMoreTrees());
    }

    @Test
    public void ambiguousPriority() {
        ParserOptions options = new ParserOptions(globalOptions);
        SourceGrammar grammar = new SourceGrammar(new ParserOptions());

        /*
        S = A, B .
        A = X | Y .
        X = 'a' .
        Y = 'a' .
        B = 'b'
        */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _X = grammar.getNonterminal("X", new ParserAttribute(ForestNode.PRIORITY_ATTRIBUTE, "5"));
        NonterminalSymbol _Y = grammar.getNonterminal("Y", new ParserAttribute(ForestNode.PRIORITY_ATTRIBUTE, "5"));
        NonterminalSymbol _B = grammar.getNonterminal("B");

        TerminalSymbol _a = TerminalSymbol.ch('a');
        TerminalSymbol _b = TerminalSymbol.ch('b');

        grammar.addRule(_S, _A, _B);
        grammar.addRule(_A, _X);
        grammar.addRule(_A, _Y);
        grammar.addRule(_X, _a);
        grammar.addRule(_Y, _a);
        grammar.addRule(_B, _b);

        options.setPriorityStyle("max");
        options.setParserType("Earley");

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse("ab");

        //result.getForest().serialize("ambiguouspriority.xml");

        Assertions.assertTrue(result.getForest().isAmbiguous());
        Assertions.assertFalse(result.getForest().isInfinitelyAmbiguous());

        Axe axe = new PriorityAxe();
        Arborist walker = Arborist.getArborist(result.getForest(), axe);

        TreeBuilder builder = new NopTreeBuilder();
        walker.getTree(builder);

        // Here the choice is ambiguous even with priority
        Assertions.assertTrue(walker.isAmbiguous());
        Assertions.assertTrue(walker.isAbsolutelyAmbiguous());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void terminalPriority(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);
        SourceGrammar grammar = new SourceGrammar(new ParserOptions());

        /*
        S = A, B .
        A = 'a' .
        B = 'a' .
        */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B = grammar.getNonterminal("B");

        TerminalSymbol _a1 = new TerminalSymbol(TokenCharacter.get('a'), new ParserAttribute(ForestNode.PRIORITY_ATTRIBUTE, "5"));
        TerminalSymbol _a2 = TerminalSymbol.ch('a');

        // a1 first...
        grammar.addRule(_S, _A);
        grammar.addRule(_S, _B);
        grammar.addRule(_A, _a1);
        grammar.addRule(_B, _a2);

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse("a");

        //result.getForest().serialize("terminalprio.xml");

        Assertions.assertTrue(result.getForest().isAmbiguous());
        Assertions.assertFalse(result.getForest().isInfinitelyAmbiguous());

        Axe axe = new PriorityAxe();
        Arborist walker = Arborist.getArborist(result.getForest(), axe);

        StringTreeBuilder builder = new StringTreeBuilder();
        walker.getTree(builder);

        Assertions.assertEquals("<S><A>a</A></S>", builder.getTree());

        grammar = new SourceGrammar(new ParserOptions());

        // a1 second...
        grammar.addRule(_S, _A);
        grammar.addRule(_S, _B);
        grammar.addRule(_B, _a2);
        grammar.addRule(_A, _a1);

        parser = grammar.getParser(options, _S);
        result = parser.parse("a");

        Assertions.assertTrue(result.getForest().isAmbiguous());
        Assertions.assertFalse(result.getForest().isInfinitelyAmbiguous());

        axe = new PriorityAxe();
        walker = Arborist.getArborist(result.getForest(), axe);

        builder = new StringTreeBuilder();
        walker.getTree(builder);

        Assertions.assertEquals("<S><A>a</A></S>", builder.getTree());
    }
}
