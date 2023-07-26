package org.nineml.coffeegrinder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.tokens.CharacterSet;
import org.nineml.coffeegrinder.tokens.TokenCharacter;
import org.nineml.coffeegrinder.tokens.TokenCharacterSet;
import org.nineml.coffeegrinder.trees.Arborist;
import org.nineml.coffeegrinder.trees.PriorityAxe;
import org.nineml.coffeegrinder.trees.StringTreeBuilder;
import org.nineml.coffeegrinder.util.Iterators;
import org.nineml.coffeegrinder.util.ParserAttribute;

import java.util.Arrays;
import java.util.Collections;

public class AmbiguityTest extends CoffeeGrinderTest {
    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void testAmbiguity(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);

        SourceGrammar grammar = new SourceGrammar();

        NonterminalSymbol _letter1 = grammar.getNonterminal("letter", new ParserAttribute("L", "1"));
        NonterminalSymbol _letter2 = grammar.getNonterminal("letter", new ParserAttribute("L", "2"));
        NonterminalSymbol _letterOrNumber = grammar.getNonterminal("letterOrNumber");
        NonterminalSymbol _expr = grammar.getNonterminal("expr");
        NonterminalSymbol _number1 = grammar.getNonterminal("number", new ParserAttribute("N", "1"));
        NonterminalSymbol _number2 = grammar.getNonterminal("number", new ParserAttribute("N", "2"));
        NonterminalSymbol _number3 = grammar.getNonterminal("number", new ParserAttribute("N", "3"));

        grammar.addRule(_expr, TerminalSymbol.ch('x'), _letter1, _letterOrNumber, _letter1);
        grammar.addRule(_letter1, TerminalSymbol.regex("[a-z]"));
        grammar.addRule(_letter2, TerminalSymbol.regex("[a-z]"));
        grammar.addRule(_number1, TerminalSymbol.regex("[0-9]"));
        grammar.addRule(_number2, TerminalSymbol.regex("[0-9]"));
        grammar.addRule(_letterOrNumber, _letter2);
        grammar.addRule(_letterOrNumber, _number2);

        grammar.addRule(_number3, TerminalSymbol.ch('b'));

        ParserOptions newOptions = new ParserOptions(options);

        GearleyParser parser = grammar.getParser(newOptions, _expr);
        GearleyResult result = parser.parse(Iterators.characterIterator("xabb"));

        //result.getForest().serialize("ambiguity.xml");

        Assertions.assertTrue(result.succeeded());
        Assertions.assertEquals(2, result.getForest().getParseTreeCount());

        expectTrees(Arborist.getArborist(result.getForest()), Arrays.asList(
                "<expr>x<letter L='1'>a</letter><letterOrNumber><letter L='2'>b</letter></letterOrNumber><letter L='1'>b</letter></expr>",
                "<expr>x<letter L='1'>a</letter><letterOrNumber><number N='2'>b</number></letterOrNumber><letter L='1'>b</letter></expr>"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void testAmbiguity2(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);

        SourceGrammar grammar = new SourceGrammar();

        NonterminalSymbol _letter = grammar.getNonterminal("letter");
        NonterminalSymbol _letterOrNumber = grammar.getNonterminal("letterOrNumber");
        NonterminalSymbol _expr = grammar.getNonterminal("expr");
        NonterminalSymbol _number = grammar.getNonterminal("number");
        NonterminalSymbol _other = grammar.getNonterminal("other");

        grammar.addRule(_expr, _letter, _letterOrNumber, _letter, _letterOrNumber);
        grammar.addRule(_letter, TerminalSymbol.regex("[a-z]"));
        grammar.addRule(_number, TerminalSymbol.regex("[0-9]"));
        grammar.addRule(_other, TerminalSymbol.regex("[A-Z]"));
        grammar.addRule(_letterOrNumber, _letter);
        grammar.addRule(_letterOrNumber, _number);
        grammar.addRule(_letterOrNumber, _other);

        grammar.addRule(_number, TerminalSymbol.ch('b'));
        grammar.addRule(_other, TerminalSymbol.ch('b'));

        GearleyParser parser = grammar.getParser(options, _expr);
        GearleyResult result = parser.parse(Iterators.characterIterator("abab"));

        //result.getForest().serialize("ambiguity2.xml");

        Assertions.assertTrue(result.succeeded());
        Assertions.assertEquals(9, result.getForest().getParseTreeCount());

        expectTrees(Arborist.getArborist(result.getForest()), Arrays.asList(
                "<expr><letter>a</letter><letterOrNumber><letter>b</letter></letterOrNumber><letter>a</letter><letterOrNumber><letter>b</letter></letterOrNumber></expr>",
                "<expr><letter>a</letter><letterOrNumber><letter>b</letter></letterOrNumber><letter>a</letter><letterOrNumber><number>b</number></letterOrNumber></expr>",
                "<expr><letter>a</letter><letterOrNumber><letter>b</letter></letterOrNumber><letter>a</letter><letterOrNumber><other>b</other></letterOrNumber></expr>",
                "<expr><letter>a</letter><letterOrNumber><number>b</number></letterOrNumber><letter>a</letter><letterOrNumber><letter>b</letter></letterOrNumber></expr>",
                "<expr><letter>a</letter><letterOrNumber><number>b</number></letterOrNumber><letter>a</letter><letterOrNumber><number>b</number></letterOrNumber></expr>",
                "<expr><letter>a</letter><letterOrNumber><number>b</number></letterOrNumber><letter>a</letter><letterOrNumber><other>b</other></letterOrNumber></expr>",
                "<expr><letter>a</letter><letterOrNumber><other>b</other></letterOrNumber><letter>a</letter><letterOrNumber><letter>b</letter></letterOrNumber></expr>",
                "<expr><letter>a</letter><letterOrNumber><other>b</other></letterOrNumber><letter>a</letter><letterOrNumber><number>b</number></letterOrNumber></expr>",
                "<expr><letter>a</letter><letterOrNumber><other>b</other></letterOrNumber><letter>a</letter><letterOrNumber><other>b</other></letterOrNumber></expr>"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void testAmbiguity3(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);

        SourceGrammar grammar = new SourceGrammar();

        NonterminalSymbol _word = grammar.getNonterminal("word");
        NonterminalSymbol _letter = grammar.getNonterminal("letter");
        NonterminalSymbol _expr = grammar.getNonterminal("expr");

        grammar.addRule(_expr, _word);
        grammar.addRule(_letter, TerminalSymbol.regex("[a-z]"));
        grammar.addRule(_letter);
        grammar.addRule(_word, _letter, _word);
        grammar.addRule(_word);

        GearleyParser parser = grammar.getParser(options, _expr);
        GearleyResult result = parser.parse(Iterators.characterIterator("word"));

        //result.getForest().serialize("ambiguity3.xml");

        Assertions.assertTrue(result.succeeded());
        Assertions.assertTrue(result.getForest().isInfinitelyAmbiguous());

        Assertions.assertEquals(6, result.getForest().getParseTreeCount());

        expectTrees(Arborist.getArborist(result.getForest()), Arrays.asList(
                "<expr><word><letter>w</letter><word><letter>o</letter><word><letter>r</letter><word><letter>d</letter><word></word></word></word></word></word></expr>"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void testAmbiguity4(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);

        SourceGrammar grammar = new SourceGrammar();

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B0 = grammar.getNonterminal("B", new ParserAttribute("N", "0"));
        NonterminalSymbol _B1 = grammar.getNonterminal("B", new ParserAttribute("N", "1"));
        NonterminalSymbol _B2 = grammar.getNonterminal("B", new ParserAttribute("N", "2"));
        NonterminalSymbol _C = grammar.getNonterminal("C");

        grammar.addRule(_S, _A, _B1, _C);
        grammar.addRule(_S, _A, _B2, _C);
        grammar.addRule(_A, TerminalSymbol.ch('a'));
        grammar.addRule(_B0, TerminalSymbol.ch('b'));
        grammar.addRule(_C, TerminalSymbol.ch('c'));

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(Iterators.characterIterator("abc"));

        Assertions.assertTrue(result.succeeded());
        Assertions.assertEquals(2, result.getForest().getParseTreeCount());

        //result.getForest().serialize("ambiguity4.xml");

        expectTrees(Arborist.getArborist(result.getForest()), Arrays.asList(
                "<S><A>a</A><B N='1'>b</B><C>c</C></S>",
                "<S><A>a</A><B N='2'>b</B><C>c</C></S>"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void testAmbiguity5(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);

        SourceGrammar grammar = new SourceGrammar();

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B = grammar.getNonterminal("B");
        NonterminalSymbol _C = grammar.getNonterminal("C");

        grammar.addRule(_S, _A, _B, _C);
        grammar.addRule(_S, _A, _C);
        grammar.addRule(_A, TerminalSymbol.ch('a'));
        grammar.addRule(_B, TerminalSymbol.ch('b'));
        grammar.addRule(_C, TerminalSymbol.ch('c'));

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(Iterators.characterIterator("abc"));

        //result.getForest().serialize("ambiguity5.xml");

        Assertions.assertTrue(result.succeeded());
        Assertions.assertEquals(1, result.getForest().getParseTreeCount());

        expectTrees(Arborist.getArborist(result.getForest()), Collections.singletonList("<S><A>a</A><B>b</B><C>c</C></S>"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void horiz1(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);

        SourceGrammar grammar = new SourceGrammar();

        /*
        {[+pragma n "https://nineml.org/ns/pragma/"]}
                         S = Z .
        {[n priority 1]} Z = "x", A, B .
                         A = "a" | () .
                         B = "a", "y" | "y" .
         */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _Z = grammar.getNonterminal("Z");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B = grammar.getNonterminal("B");

        grammar.addRule(_S, _Z);
        grammar.addRule(_Z, TerminalSymbol.ch('x'), _A, _B);
        grammar.addRule(_A, TerminalSymbol.ch('a'));
        grammar.addRule(_A);
        grammar.addRule(_B, TerminalSymbol.ch('a'), TerminalSymbol.ch('y'));
        grammar.addRule(_B, TerminalSymbol.ch('y'));

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(Iterators.characterIterator("xay"));

        Assertions.assertTrue(result.succeeded());
        Assertions.assertEquals(2, result.getForest().getParseTreeCount());

        //result.getForest().serialize("horiz1.xml");

        expectTrees(Arborist.getArborist(result.getForest()), Arrays.asList(
                "<S><Z>x<A></A><B>ay</B></Z></S>",
                "<S><Z>x<A>a</A><B>y</B></Z></S>"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void ambigprop(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);

        SourceGrammar grammar = new SourceGrammar();

        /*
           S = A, D | X, Y .
           A = "a" | "c" .
           D = "d" .
           X = "a" | "c" .
           Y = Z .
           Z = "d" .
         */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A", new ParserAttribute(ForestNode.PRIORITY_ATTRIBUTE, "1"));
        NonterminalSymbol _D = grammar.getNonterminal("D", new ParserAttribute(ForestNode.PRIORITY_ATTRIBUTE, "4"));
        NonterminalSymbol _X = grammar.getNonterminal("X", new ParserAttribute(ForestNode.PRIORITY_ATTRIBUTE, "3"));
        NonterminalSymbol _Y = grammar.getNonterminal("Y");
        NonterminalSymbol _Z = grammar.getNonterminal("Z");

        grammar.addRule(_S, _A, _D);
        grammar.addRule(_S, _X, _Y);
        grammar.addRule(_A, TerminalSymbol.ch('a'));
        grammar.addRule(_A, TerminalSymbol.ch('c'));
        grammar.addRule(_D, TerminalSymbol.ch('d'));
        grammar.addRule(_X, TerminalSymbol.ch('a'));
        grammar.addRule(_X, TerminalSymbol.ch('c'));
        grammar.addRule(_Y, _Z);
        grammar.addRule(_Z, TerminalSymbol.ch('d'));

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(Iterators.characterIterator("ad"));

        Assertions.assertTrue(result.succeeded());
        Assertions.assertEquals(2, result.getForest().getParseTreeCount());

        //result.getForest().serialize("ambigprop.xml");

        final String p = ForestNode.PRIORITY_ATTRIBUTE;
        expectTrees(Arborist.getArborist(result.getForest()), Arrays.asList(
                String.format("<S><A %s='1'>a</A><D %s='4'>d</D></S>", p, p),
                String.format("<S><X %s='3'>a</X><Y><Z>d</Z></Y></S>", p)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void ambigcharclass(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);

        SourceGrammar grammar = new SourceGrammar();

        /*
           S = A | B .
           A = "a" .
           B = [Ll] .
         */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B = grammar.getNonterminal("B");

        grammar.addRule(_S, _A);
        grammar.addRule(_S, _B);
        grammar.addRule(_A, TerminalSymbol.ch('a'));
        grammar.addRule(_A, new TerminalSymbol(TokenCharacter.get(128587)));
        grammar.addRule(_B, new TerminalSymbol(TokenCharacterSet.inclusion(CharacterSet.unicodeClass("Ll"))));

        GearleyParser parser = grammar.getParser(options, _S);

        HygieneReport report = parser.getGrammar().getHygieneReport();
        report.checkAmbiguity();
        Assertions.assertFalse(report.reliablyUnambiguous());

        GearleyResult result = parser.parse(Iterators.characterIterator("a"));

        Assertions.assertTrue(result.succeeded());
        Assertions.assertEquals(2, result.getForest().getParseTreeCount());

        //result.getForest().serialize("ambigcharclass.xml");

        expectTrees(Arborist.getArborist(result.getForest()), Arrays.asList(
                "<S><A>a</A></S>",
                "<S><B>a</B></S>"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void ambigproploop(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);

        SourceGrammar grammar = new SourceGrammar();

    /*
    S = A .
    A = "a" | B | () .
    B = C .
    C = A | () .
     */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B = grammar.getNonterminal("B");
        NonterminalSymbol _C = grammar.getNonterminal("C");

        grammar.addRule(_S, _A);
        grammar.addRule(_A, new TerminalSymbol(TokenCharacter.get('a', new ParserAttribute("terminal", "true"))));
        grammar.addRule(_A, _B);
        grammar.addRule(_A);
        grammar.addRule(_B, _C);
        grammar.addRule(_C, _A);
        grammar.addRule(_C);

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(Iterators.characterIterator("a"));

        Assertions.assertTrue(result.succeeded());
        Assertions.assertEquals(2, result.getForest().getParseTreeCount());

        //result.getForest().serialize("ambigproploop.xml");

        expectTrees(Arborist.getArborist(result.getForest()), Arrays.asList(
                "<S><A>a</A></S>",
                "<S><A><B><C><A>a</A></C></B></A></S>"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void simpleambiguity(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);

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
                "<S><A>a</A></S>",
                "<S><B>a</B></S>"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void deeperambiguity(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);
        options.getLogger().setLogLevel("Lumberjack", "trace");

        SourceGrammar grammar = new SourceGrammar();

    /*
    S = A, B .
    A = Q | R .
    B = V | W .
    V = X | Y .
    W = X | Y .
    Q = "a" .
    R = "a" .
    X = "b" .
    Y = "b" .
     */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B = grammar.getNonterminal("B");
        NonterminalSymbol _Q = grammar.getNonterminal("Q");
        NonterminalSymbol _R = grammar.getNonterminal("R");
        NonterminalSymbol _V = grammar.getNonterminal("V");
        NonterminalSymbol _W = grammar.getNonterminal("W");
        NonterminalSymbol _X = grammar.getNonterminal("X");
        NonterminalSymbol _Y = grammar.getNonterminal("Y");

        grammar.addRule(_S, _A, _B);
        grammar.addRule(_A, _Q);
        grammar.addRule(_A, _R);
        grammar.addRule(_B, _V);
        grammar.addRule(_B, _W);
        grammar.addRule(_V, _X);
        grammar.addRule(_V, _Y);
        grammar.addRule(_W, _X);
        grammar.addRule(_W, _Y);

        grammar.addRule(_Q, TerminalSymbol.ch('a'));
        grammar.addRule(_R, TerminalSymbol.ch('a'));
        grammar.addRule(_X, TerminalSymbol.ch('b'));
        grammar.addRule(_Y, TerminalSymbol.ch('b'));

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(Iterators.characterIterator("ab"));

        Assertions.assertTrue(result.succeeded());
        Assertions.assertTrue(result.getForest().isAmbiguous());
        Assertions.assertFalse(result.getForest().isInfinitelyAmbiguous());
        Assertions.assertEquals(8, result.getForest().getParseTreeCount());

        //result.getForest().serialize("deeper.xml");
        expectTrees(Arborist.getArborist(result.getForest()), Arrays.asList(
                "<S><A><Q>a</Q></A><B><V><X>b</X></V></B></S>",
                "<S><A><Q>a</Q></A><B><V><Y>b</Y></V></B></S>",
                "<S><A><Q>a</Q></A><B><W><X>b</X></W></B></S>",
                "<S><A><Q>a</Q></A><B><W><Y>b</Y></W></B></S>",
                "<S><A><R>a</R></A><B><V><X>b</X></V></B></S>",
                "<S><A><R>a</R></A><B><V><Y>b</Y></V></B></S>",
                "<S><A><R>a</R></A><B><W><X>b</X></W></B></S>",
                "<S><A><R>a</R></A><B><W><Y>b</Y></W></B></S>"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void loopambiguity(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);

        SourceGrammar grammar = new SourceGrammar();

    /*
    S = A .
    A = X | "a" .
    X = Y .
    Y = Z .
    Z = A | () .
     */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _X = grammar.getNonterminal("X");
        NonterminalSymbol _Y = grammar.getNonterminal("Y");
        NonterminalSymbol _Z = grammar.getNonterminal("Z");

        grammar.addRule(_S, _A);
        grammar.addRule(_A, _X);
        grammar.addRule(_A, TerminalSymbol.ch('a'));
        grammar.addRule(_X, _Y);
        grammar.addRule(_Y, _Z);
        grammar.addRule(_Z, _A);
        grammar.addRule(_Z, TerminalSymbol.ch('a'));
        grammar.addRule(_Z);

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(Iterators.characterIterator("a"));

        Assertions.assertTrue(result.succeeded());
        Assertions.assertTrue(result.getForest().isAmbiguous());
        Assertions.assertTrue(result.getForest().isInfinitelyAmbiguous());
        Assertions.assertEquals(3, result.getForest().getParseTreeCount());

        //result.getForest().serialize("loop.xml");
        //showTrees(Lumberjack.getLumberjack(result.getForest()));

        expectTrees(Arborist.getArborist(result.getForest()), Arrays.asList(
                "<S><A>a</A></S>",
                "<S><A><X><Y><Z>a</Z></Y></X></A></S>"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void fiveparses(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);

        SourceGrammar grammar = new SourceGrammar();
    /*
                  S = 'x', (A | B1), 'y' .
                  A = 'a' | B2 .
                 B1 = 'b' | A .
                 B2 = 'b' | A .
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
        grammar.addRule(_B2, _A);

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(Iterators.characterIterator("xay"));

        //result.getForest().serialize("fiveparses.xml");

        Assertions.assertTrue(result.succeeded());
        Assertions.assertTrue(result.getForest().isInfinitelyAmbiguous());
        Assertions.assertEquals(5, result.getForest().getParseTreeCount());

        expectTrees(Arborist.getArborist(result.getForest()), Arrays.asList(
                "<S>x<A>a</A>y</S>",
                "<S>x<A><B2><A>a</A></B2></A>y</S>",
                "<S>x<B1><A>a</A></B1>y</S>",
                "<S>x<B1><A><B2><A>a</A></B2></A></B1>y</S>"
        ));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void fourparsesnoloop(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);

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

        expectTrees(Arborist.getArborist(result.getForest()), Arrays.asList(
                "<S>x<A>a</A>y</S>",
                "<S>x<A><B2>a</B2></A>y</S>",
                "<S>x<B1><A>a</A></B1>y</S>",
                "<S>x<B1><A><B2>a</B2></A></B1>y</S>"
        ));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void wordhex(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);

        SourceGrammar grammar = new SourceGrammar();

        /*
        id => word; hex
        word => letter, letter, letter
        hex => digit, digit, digit
        letter => ["a"-"z"; "A"-"Z"]
        digit => ["0"-"9"; "a"-"f"; "A"-"F"]
         */

        NonterminalSymbol _id = grammar.getNonterminal("id");
        NonterminalSymbol _word = grammar.getNonterminal("word");
        NonterminalSymbol _hex = grammar.getNonterminal("hex");
        NonterminalSymbol _letter = grammar.getNonterminal("letter");
        NonterminalSymbol _digit = grammar.getNonterminal("digit");

        grammar.addRule(_id, _word);
        grammar.addRule(_id, _hex);
        grammar.addRule(_word, _letter, _letter, _letter);
        grammar.addRule(_hex, _digit, _digit, _digit);
        grammar.addRule(_letter, TerminalSymbol.regex("[a-zA-Z]"));
        grammar.addRule(_digit, TerminalSymbol.regex("[0-9a-fA-F]"));

        GearleyParser parser = grammar.getParser(options, _id);
        GearleyResult result = parser.parse(Iterators.characterIterator("fab"));

        Assertions.assertTrue(result.succeeded());
        Assertions.assertEquals(2, result.getForest().getParseTreeCount());

        //result.getForest().serialize("wordhex.xml");

        expectTrees(Arborist.getArborist(result.getForest()), Arrays.asList(
                "<id><word><letter>f</letter><letter>a</letter><letter>b</letter></word></id>",
                "<id><hex><digit>f</digit><digit>a</digit><digit>b</digit></hex></id>"
        ));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void disjointparses1(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);

        SourceGrammar grammar = new SourceGrammar();
    /*
                  S = 'x', (A | B), 'y' .
                  A = A1 | A2 | A3 .
                  B = B1 | B2 .
                 A1 = 'a' .
                 A2 = 'a' .
                 A3 = 'a' .
                 B1 = 'a' .
                 B2 = 'a' .
     */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B = grammar.getNonterminal("B");
        NonterminalSymbol _A1 = grammar.getNonterminal("A1");
        NonterminalSymbol _A2 = grammar.getNonterminal("A2");
        NonterminalSymbol _A3 = grammar.getNonterminal("A3");
        NonterminalSymbol _B1 = grammar.getNonterminal("B1");
        NonterminalSymbol _B2 = grammar.getNonterminal("B2");

        grammar.addRule(_S, TerminalSymbol.ch('x'), _A, TerminalSymbol.ch('y'));
        grammar.addRule(_S, TerminalSymbol.ch('x'), _B, TerminalSymbol.ch('y'));
        grammar.addRule(_A, _A1);
        grammar.addRule(_A, _A2);
        grammar.addRule(_A, _A3);
        grammar.addRule(_B, _B1);
        grammar.addRule(_B, _B2);
        grammar.addRule(_A1, TerminalSymbol.ch('a'));
        grammar.addRule(_A2, TerminalSymbol.ch('a'));
        grammar.addRule(_A3, TerminalSymbol.ch('a'));
        grammar.addRule(_B1, TerminalSymbol.ch('a'));
        grammar.addRule(_B2, TerminalSymbol.ch('a'));

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(Iterators.characterIterator("xay"));

        Assertions.assertTrue(result.succeeded());
        Assertions.assertEquals(5, result.getForest().getParseTreeCount());

        //result.getForest().serialize("disjoint1.xml");

        expectTrees(Arborist.getArborist(result.getForest()), Arrays.asList(
                "<S>x<A><A1>a</A1></A>y</S>",
                "<S>x<A><A2>a</A2></A>y</S>",
                "<S>x<A><A3>a</A3></A>y</S>",
                "<S>x<B><B1>a</B1></B>y</S>",
                "<S>x<B><B2>a</B2></B>y</S>"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void disjointparses2(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);

        SourceGrammar grammar = new SourceGrammar();
    /*
                  S = 'x', (A , B), 'y' .
                  A = A1 | A2 | A3 .
                  B = B1 | B2 .
                 A1 = 'a' .
                 A2 = 'a' .
                 A3 = 'a' .
                 B1 = 'a' .
                 B2 = 'a' .
     */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B = grammar.getNonterminal("B");
        NonterminalSymbol _A1 = grammar.getNonterminal("A1");
        NonterminalSymbol _A2 = grammar.getNonterminal("A2");
        NonterminalSymbol _A3 = grammar.getNonterminal("A3");
        NonterminalSymbol _B1 = grammar.getNonterminal("B1");
        NonterminalSymbol _B2 = grammar.getNonterminal("B2");

        grammar.addRule(_S, TerminalSymbol.ch('x'), _A, _B, TerminalSymbol.ch('y'));
        //grammar.addRule(_S, TerminalSymbol.ch('x'), _B, TerminalSymbol.ch('y'));
        grammar.addRule(_A, _A1);
        grammar.addRule(_A, _A2);
        grammar.addRule(_A, _A3);
        grammar.addRule(_B, _B1);
        grammar.addRule(_B, _B2);
        grammar.addRule(_A1, TerminalSymbol.ch('a'));
        grammar.addRule(_A2, TerminalSymbol.ch('a'));
        grammar.addRule(_A3, TerminalSymbol.ch('a'));
        grammar.addRule(_B1, TerminalSymbol.ch('b'));
        grammar.addRule(_B2, TerminalSymbol.ch('b'));

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(Iterators.characterIterator("xaby"));

        Assertions.assertTrue(result.succeeded());
        Assertions.assertEquals(6, result.getForest().getParseTreeCount());

        //result.getForest().serialize("disjoint2.xml");

        expectTrees(Arborist.getArborist(result.getForest()), Arrays.asList(
                "<S>x<A><A1>a</A1></A><B><B1>b</B1></B>y</S>",
                "<S>x<A><A1>a</A1></A><B><B2>b</B2></B>y</S>",
                "<S>x<A><A2>a</A2></A><B><B1>b</B1></B>y</S>",
                "<S>x<A><A2>a</A2></A><B><B2>b</B2></B>y</S>",
                "<S>x<A><A3>a</A3></A><B><B1>b</B1></B>y</S>",
                "<S>x<A><A3>a</A3></A><B><B2>b</B2></B>y</S>"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void screaminghorror(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);

        SourceGrammar grammar = new SourceGrammar();
    /*
    S = A
    A = B | C | X
    B = D | E
    C = F | G | A
    D = H | A
    E = I
    F = I
    G = A
    H = D | K
    I = "t" | K | G
    K = A
    X = A
     */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B = grammar.getNonterminal("B");
        NonterminalSymbol _C = grammar.getNonterminal("C");
        NonterminalSymbol _D = grammar.getNonterminal("D");
        NonterminalSymbol _E = grammar.getNonterminal("E");
        NonterminalSymbol _F = grammar.getNonterminal("F");
        NonterminalSymbol _G = grammar.getNonterminal("G");
        NonterminalSymbol _H = grammar.getNonterminal("H");
        NonterminalSymbol _I = grammar.getNonterminal("I");
        NonterminalSymbol _K = grammar.getNonterminal("K");
        NonterminalSymbol _X = grammar.getNonterminal("X");

        grammar.addRule(_S, _A);

        grammar.addRule(_A, _B);
        grammar.addRule(_A, _C);
        grammar.addRule(_A, _X);

        grammar.addRule(_B, _D);
        grammar.addRule(_B, _E);

        grammar.addRule(_C, _F);
        grammar.addRule(_C, _G);
        grammar.addRule(_C, _A);

        grammar.addRule(_D, _H);
        grammar.addRule(_D, _A);

        grammar.addRule(_E, _I);

        grammar.addRule(_F, _I);

        grammar.addRule(_G, _A);

        grammar.addRule(_H, _D);
        grammar.addRule(_H, _K);

        grammar.addRule(_I, TerminalSymbol.ch('t'));
        grammar.addRule(_I, _K);

        grammar.addRule(_K, _A);
        grammar.addRule(_X, _A);

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(Iterators.characterIterator("t"));

        Assertions.assertTrue(result.succeeded());
        Assertions.assertEquals(10, result.getForest().getParseTreeCount());

        //result.getForest().serialize(String.format("horror-%s.xml", parserType));

        expectTrees(Arborist.getArborist(result.getForest()), Arrays.asList(
                "<S><A><B><E><I>t</I></E></B></A></S>",
                "<S><A><C><F><I>t</I></F></C></A></S>",
                "<S><A><X><A><B><E><I>t</I></E></B></A></X></A></S>",
                "<S><A><X><A><C><F><I>t</I></F></C></A></X></A></S>",
                "<S><A><C><A><B><E><I>t</I></E></B></A></C></A></S>",
                "<S><A><C><A><C><F><I>t</I></F></C></A></C></A></S>",
                "<S><A><C><G><A><B><E><I>t</I></E></B></A></G></C></A></S>",
                "<S><A><C><G><A><C><F><I>t</I></F></C></A></G></C></A></S>",
                "<S><A><X><A><X><A><B><E><I>t</I></E></B></A></X></A></X></A></S>",
                "<S><A><X><A><X><A><C><F><I>t</I></F></C></A></X></A></X></A></S>"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void smallerhorror(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);

        SourceGrammar grammar = new SourceGrammar();

    /*
    S = A
    A = B | C
    B = D
    C = D
    D ="t" | A
     */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B = grammar.getNonterminal("B");
        NonterminalSymbol _C = grammar.getNonterminal("C");
        NonterminalSymbol _D = grammar.getNonterminal("D");

        grammar.addRule(_S, _A);

        grammar.addRule(_A, _B);
        grammar.addRule(_A, _C);

        grammar.addRule(_B, _D);

        grammar.addRule(_C, _D);

        grammar.addRule(_D, _A);
        grammar.addRule(_D, TerminalSymbol.ch('t'));

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(Iterators.characterIterator("t"));

        Assertions.assertTrue(result.succeeded());
        Assertions.assertEquals(4, result.getForest().getParseTreeCount());

        //result.getForest().serialize("smaller.xml");
        //showTrees(Lumberjack.getLumberjack(result.getForest()));

        expectTrees(Arborist.getArborist(result.getForest()), Arrays.asList(
                "<S><A><B><D>t</D></B></A></S>",
                "<S><A><C><D>t</D></C></A></S>"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void mediumhorror(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);

        SourceGrammar grammar = new SourceGrammar();

    /*
    S = A | X
    A = B | C
    B = D
    C = D
    D = "t" | A
    X = D
     */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B = grammar.getNonterminal("B");
        NonterminalSymbol _C = grammar.getNonterminal("C");
        NonterminalSymbol _D = grammar.getNonterminal("D");
        NonterminalSymbol _X = grammar.getNonterminal("X");

        grammar.addRule(_S, _A);
        grammar.addRule(_S, _X);

        grammar.addRule(_A, _B);
        grammar.addRule(_A, _C);
        grammar.addRule(_A, TerminalSymbol.ch('t'));

        grammar.addRule(_B, _D);

        grammar.addRule(_C, _D);

        grammar.addRule(_D, _A);
        grammar.addRule(_D, TerminalSymbol.ch('t'));

        grammar.addRule(_X, _D);

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(Iterators.characterIterator("t"));

        //result.getForest().serialize(String.format("medium-%s.xml", parserType));

        Assertions.assertTrue(result.succeeded());
        Assertions.assertEquals(11, result.getForest().getParseTreeCount());

        //showTrees(Arborist.getLumberjack(result.getForest()), true, 9);

        expectTrees(Arborist.getArborist(result.getForest()), Arrays.asList(
                "<S><A>t</A></S>",
                "<S><X><D>t</D></X></S>",
                "<S><A><B><D>t</D></B></A></S>",
                "<S><A><C><D>t</D></C></A></S>",
                "<S><X><D><A>t</A></D></X></S>",
                "<S><A><B><D><A>t</A></D></B></A></S>",
                "<S><A><C><D><A>t</A></D></C></A></S>",
                "<S><X><D><A><B><D>t</D></B></A></D></X></S>",
                "<S><X><D><A><C><D>t</D></C></A></D></X></S>"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void tinyloop(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);

        SourceGrammar grammar = new SourceGrammar();

    /*
                  S = A .
                  A = B | 't' .
                  B = A .
     */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B = grammar.getNonterminal("B");

        grammar.addRule(_S, _A);
        grammar.addRule(_A, _B);
        grammar.addRule(_A, TerminalSymbol.ch('t'));
        grammar.addRule(_B, _A);

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(Iterators.characterIterator("t"));

        Assertions.assertTrue(result.succeeded());
        Assertions.assertEquals(2, result.getForest().getParseTreeCount());

        //result.getForest().serialize(String.format("tinyloop-%s.xml", parserType));

        expectTrees(Arborist.getArborist(result.getForest()), Arrays.asList(
                "<S><A>t</A></S>",
                "<S><A><B><A>t</A></B></A></S>"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void forcedChoiceWithoutPriority(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);

        SourceGrammar grammar = new SourceGrammar(options);

        /*
        S = A .
        A = A ; B.
        B = 'b'.
         */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B = grammar.getNonterminal("B");

        grammar.addRule(_S, _A);
        grammar.addRule(_A, _A);
        grammar.addRule(_A, _B);
        grammar.addRule(_B, TerminalSymbol.ch('b'));

        String input = "b";

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(input);
        Assertions.assertTrue(result.succeeded());

        Arborist walker = Arborist.getArborist(result.getForest(), new PriorityAxe());
        StringTreeBuilder builder = new StringTreeBuilder();
        walker.getTree(builder);

        Assertions.assertTrue(walker.isAmbiguous());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void forcedChoiceWithPriority(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);

        SourceGrammar grammar = new SourceGrammar(options);

        /*
        S = A .
        A = A ; B.
        B = 'b'.
         */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A", new ParserAttribute(ForestNode.PRIORITY_ATTRIBUTE, "3"));
        NonterminalSymbol _B = grammar.getNonterminal("B", new ParserAttribute(ForestNode.PRIORITY_ATTRIBUTE, "5"));

        grammar.addRule(_S, _A);
        grammar.addRule(_A, _A);
        grammar.addRule(_A, _B);
        grammar.addRule(_B, TerminalSymbol.ch('b'));

        String input = "b";

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(input);
        Assertions.assertTrue(result.succeeded());

        Arborist walker = Arborist.getArborist(result.getForest(), new PriorityAxe());
        StringTreeBuilder builder = new StringTreeBuilder();
        walker.getTree(builder);

        Assertions.assertFalse(walker.isAmbiguous());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void shortCircuit(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);

        // Test that we avoid looking for new trees after the prefix is invalid

        SourceGrammar grammar = new SourceGrammar(options);

        /*
        S = A, B, C, D .
        A = A1 | A2 .
        B = B1 | B2 | B3 .
        C = C1 | C2 | C3 | C4 .
        D = D1 | D2 .
         */

        NonterminalSymbol _S  = grammar.getNonterminal("S");
        NonterminalSymbol _A  = grammar.getNonterminal("A");
        NonterminalSymbol _A1 = grammar.getNonterminal("A1");
        NonterminalSymbol _A2 = grammar.getNonterminal("A2");
        NonterminalSymbol _B  = grammar.getNonterminal("B");
        NonterminalSymbol _B1 = grammar.getNonterminal("B1");
        NonterminalSymbol _B2 = grammar.getNonterminal("B2");
        NonterminalSymbol _B3 = grammar.getNonterminal("B3");
        NonterminalSymbol _C  = grammar.getNonterminal("C");
        NonterminalSymbol _C1 = grammar.getNonterminal("C1");
        NonterminalSymbol _C2 = grammar.getNonterminal("C2");
        NonterminalSymbol _C3 = grammar.getNonterminal("C3");
        NonterminalSymbol _C4 = grammar.getNonterminal("C4");
        NonterminalSymbol _D  = grammar.getNonterminal("D");
        NonterminalSymbol _D1  = grammar.getNonterminal("D1");
        NonterminalSymbol _D2  = grammar.getNonterminal("D2");

        TerminalSymbol _a = TerminalSymbol.ch('a');
        TerminalSymbol _b = TerminalSymbol.ch('b');
        TerminalSymbol _c = TerminalSymbol.ch('c');
        TerminalSymbol _d = TerminalSymbol.ch('d');

        grammar.addRule(_S, _A, _B, _C, _D);
        grammar.addRule(_A, _A1);
        grammar.addRule(_A, _A2);
        grammar.addRule(_B, _B1);
        grammar.addRule(_B, _B2);
        grammar.addRule(_B, _B3);
        grammar.addRule(_C, _C1);
        grammar.addRule(_C, _C2);
        grammar.addRule(_C, _C3);
        grammar.addRule(_C, _C4);
        grammar.addRule(_D, _D1);
        grammar.addRule(_D, _D2);

        grammar.addRule(_A1, _a);
        grammar.addRule(_A2, _a);
        grammar.addRule(_B1, _b);
        grammar.addRule(_B2, _b);
        grammar.addRule(_B3, _b);
        grammar.addRule(_C1, _c);
        grammar.addRule(_C2, _c);
        grammar.addRule(_C3, _c);
        grammar.addRule(_C4, _c);
        grammar.addRule(_D1, _d);
        grammar.addRule(_D2, _d);

        String input = "abcd";

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(input);

        //result.getForest().serialize(String.format("shortcircuit-%s.xml", parserType));

        Assertions.assertTrue(result.succeeded());
        Assertions.assertEquals(48, result.getForest().getParseTreeCount());

        expectTrees(Arborist.getArborist(result.getForest()), Arrays.asList(
                "<S><A><A1>a</A1></A><B><B1>b</B1></B><C><C1>c</C1></C><D><D1>d</D1></D></S>",
                "<S><A><A1>a</A1></A><B><B1>b</B1></B><C><C1>c</C1></C><D><D2>d</D2></D></S>",
                "<S><A><A1>a</A1></A><B><B1>b</B1></B><C><C2>c</C2></C><D><D1>d</D1></D></S>",
                "<S><A><A1>a</A1></A><B><B1>b</B1></B><C><C2>c</C2></C><D><D2>d</D2></D></S>",
                "<S><A><A1>a</A1></A><B><B1>b</B1></B><C><C3>c</C3></C><D><D1>d</D1></D></S>",
                "<S><A><A1>a</A1></A><B><B1>b</B1></B><C><C3>c</C3></C><D><D2>d</D2></D></S>",
                "<S><A><A1>a</A1></A><B><B1>b</B1></B><C><C4>c</C4></C><D><D1>d</D1></D></S>",
                "<S><A><A1>a</A1></A><B><B1>b</B1></B><C><C4>c</C4></C><D><D2>d</D2></D></S>",
                "<S><A><A1>a</A1></A><B><B2>b</B2></B><C><C1>c</C1></C><D><D1>d</D1></D></S>",
                "<S><A><A1>a</A1></A><B><B2>b</B2></B><C><C1>c</C1></C><D><D2>d</D2></D></S>",
                "<S><A><A1>a</A1></A><B><B2>b</B2></B><C><C2>c</C2></C><D><D1>d</D1></D></S>",
                "<S><A><A1>a</A1></A><B><B2>b</B2></B><C><C2>c</C2></C><D><D2>d</D2></D></S>",
                "<S><A><A1>a</A1></A><B><B2>b</B2></B><C><C3>c</C3></C><D><D1>d</D1></D></S>",
                "<S><A><A1>a</A1></A><B><B2>b</B2></B><C><C3>c</C3></C><D><D2>d</D2></D></S>",
                "<S><A><A1>a</A1></A><B><B2>b</B2></B><C><C4>c</C4></C><D><D1>d</D1></D></S>",
                "<S><A><A1>a</A1></A><B><B2>b</B2></B><C><C4>c</C4></C><D><D2>d</D2></D></S>",
                "<S><A><A1>a</A1></A><B><B3>b</B3></B><C><C1>c</C1></C><D><D1>d</D1></D></S>",
                "<S><A><A1>a</A1></A><B><B3>b</B3></B><C><C1>c</C1></C><D><D2>d</D2></D></S>",
                "<S><A><A1>a</A1></A><B><B3>b</B3></B><C><C2>c</C2></C><D><D1>d</D1></D></S>",
                "<S><A><A1>a</A1></A><B><B3>b</B3></B><C><C2>c</C2></C><D><D2>d</D2></D></S>",
                "<S><A><A1>a</A1></A><B><B3>b</B3></B><C><C3>c</C3></C><D><D1>d</D1></D></S>",
                "<S><A><A1>a</A1></A><B><B3>b</B3></B><C><C3>c</C3></C><D><D2>d</D2></D></S>",
                "<S><A><A1>a</A1></A><B><B3>b</B3></B><C><C4>c</C4></C><D><D1>d</D1></D></S>",
                "<S><A><A1>a</A1></A><B><B3>b</B3></B><C><C4>c</C4></C><D><D2>d</D2></D></S>",
                "<S><A><A2>a</A2></A><B><B1>b</B1></B><C><C1>c</C1></C><D><D1>d</D1></D></S>",
                "<S><A><A2>a</A2></A><B><B1>b</B1></B><C><C1>c</C1></C><D><D2>d</D2></D></S>",
                "<S><A><A2>a</A2></A><B><B1>b</B1></B><C><C2>c</C2></C><D><D1>d</D1></D></S>",
                "<S><A><A2>a</A2></A><B><B1>b</B1></B><C><C2>c</C2></C><D><D2>d</D2></D></S>",
                "<S><A><A2>a</A2></A><B><B1>b</B1></B><C><C3>c</C3></C><D><D1>d</D1></D></S>",
                "<S><A><A2>a</A2></A><B><B1>b</B1></B><C><C3>c</C3></C><D><D2>d</D2></D></S>",
                "<S><A><A2>a</A2></A><B><B1>b</B1></B><C><C4>c</C4></C><D><D1>d</D1></D></S>",
                "<S><A><A2>a</A2></A><B><B1>b</B1></B><C><C4>c</C4></C><D><D2>d</D2></D></S>",
                "<S><A><A2>a</A2></A><B><B2>b</B2></B><C><C1>c</C1></C><D><D1>d</D1></D></S>",
                "<S><A><A2>a</A2></A><B><B2>b</B2></B><C><C1>c</C1></C><D><D2>d</D2></D></S>",
                "<S><A><A2>a</A2></A><B><B2>b</B2></B><C><C2>c</C2></C><D><D1>d</D1></D></S>",
                "<S><A><A2>a</A2></A><B><B2>b</B2></B><C><C2>c</C2></C><D><D2>d</D2></D></S>",
                "<S><A><A2>a</A2></A><B><B2>b</B2></B><C><C3>c</C3></C><D><D1>d</D1></D></S>",
                "<S><A><A2>a</A2></A><B><B2>b</B2></B><C><C3>c</C3></C><D><D2>d</D2></D></S>",
                "<S><A><A2>a</A2></A><B><B2>b</B2></B><C><C4>c</C4></C><D><D1>d</D1></D></S>",
                "<S><A><A2>a</A2></A><B><B2>b</B2></B><C><C4>c</C4></C><D><D2>d</D2></D></S>",
                "<S><A><A2>a</A2></A><B><B3>b</B3></B><C><C1>c</C1></C><D><D1>d</D1></D></S>",
                "<S><A><A2>a</A2></A><B><B3>b</B3></B><C><C1>c</C1></C><D><D2>d</D2></D></S>",
                "<S><A><A2>a</A2></A><B><B3>b</B3></B><C><C2>c</C2></C><D><D1>d</D1></D></S>",
                "<S><A><A2>a</A2></A><B><B3>b</B3></B><C><C2>c</C2></C><D><D2>d</D2></D></S>",
                "<S><A><A2>a</A2></A><B><B3>b</B3></B><C><C3>c</C3></C><D><D1>d</D1></D></S>",
                "<S><A><A2>a</A2></A><B><B3>b</B3></B><C><C3>c</C3></C><D><D2>d</D2></D></S>",
                "<S><A><A2>a</A2></A><B><B3>b</B3></B><C><C4>c</C4></C><D><D1>d</D1></D></S>",
                "<S><A><A2>a</A2></A><B><B3>b</B3></B><C><C4>c</C4></C><D><D2>d</D2></D></S>"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void multipleChoices(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);

        SourceGrammar grammar = new SourceGrammar(options);

        /*
         A = B, C .
         B = B1 | B2 .
         B1 = 'b' .
         B2 = B3 | B4 .
         B3 = 'b' .
         B4 = 'b' .
         C = C1 | C2 .
         C1 = 'c' .
         C2 = 'c' .
         */

        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B = grammar.getNonterminal("B");
        NonterminalSymbol _B1 = grammar.getNonterminal("B1");
        NonterminalSymbol _B2 = grammar.getNonterminal("B2");
        NonterminalSymbol _B3 = grammar.getNonterminal("B3");
        NonterminalSymbol _B4 = grammar.getNonterminal("B4");
        NonterminalSymbol _C = grammar.getNonterminal("C");
        NonterminalSymbol _C1 = grammar.getNonterminal("C1");
        NonterminalSymbol _C2 = grammar.getNonterminal("C2");

        grammar.addRule(_A, _B, _C);
        grammar.addRule(_B, _B1);
        grammar.addRule(_B, _B2);
        grammar.addRule(_B1, TerminalSymbol.ch('b'));
        grammar.addRule(_B2, _B3);
        grammar.addRule(_B2, _B4);
        grammar.addRule(_B3, TerminalSymbol.ch('b'));
        grammar.addRule(_B4, TerminalSymbol.ch('b'));
        grammar.addRule(_C, _C1);
        grammar.addRule(_C, _C2);
        grammar.addRule(_C1, TerminalSymbol.ch('c'));
        grammar.addRule(_C2, TerminalSymbol.ch('c'));

        String input = "bc";

        GearleyParser parser = grammar.getParser(options, _A);
        GearleyResult result = parser.parse(input);
        Assertions.assertTrue(result.succeeded());
        Assertions.assertEquals(6, result.getForest().getParseTreeCount());

        //result.getForest().serialize("multiple.xml");

        Arborist walker = Arborist.getArborist(result.getForest(), new PriorityAxe());
        while (walker.hasMoreTrees()) {
            StringTreeBuilder builder = new StringTreeBuilder();
            walker.getTree(builder);
        }
    }

}
