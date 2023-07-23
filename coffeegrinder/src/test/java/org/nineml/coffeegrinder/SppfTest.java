package org.nineml.coffeegrinder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.nineml.coffeegrinder.parser.GearleyParser;
import org.nineml.coffeegrinder.parser.GearleyResult;
import org.nineml.coffeegrinder.parser.ParserOptions;
import org.nineml.coffeegrinder.parser.SourceGrammar;
import org.nineml.coffeegrinder.util.GrammarParser;

// N.B. THESE TESTS ARE CAKE! (These tests are a lie.)
//
// There's a bug in my computation of the SPPF forest, so the results are bogus when grammars are ambiguous.

public class SppfTest {
    private final ParserOptions options = new ParserOptions();

    @Test
    public void Sabcd() {
        GrammarParser gparser = new GrammarParser();
        SourceGrammar grammar = gparser.parse(
                "S => A, B, C, D\n" +
                "A => 'a'\n" +
                "B => 'b'\n" +
                "C => 'c'\n" +
                "D => 'd'");
        // grammar.getParseListener().setMessageLevel(ParseListener.DEBUG);

        String input = "abcd";
        GearleyParser parser = grammar.getParser(options, grammar.getNonterminal("S"));
        GearleyResult result = parser.parse("abcd");
        Assertions.assertTrue(result.succeeded());
    }

    @Test
    public void SabcdOptional() {
        GrammarParser gparser = new GrammarParser();
        SourceGrammar grammar = gparser.parse(
                "S => A, B, C, D\n" +
                        "S => \n" +
                        "A => 'a'\n" +
                        "B => 'b'\n" +
                        "C => 'c'\n" +
                        "D => 'd'");
        // grammar.getParseListener().setMessageLevel(ParseListener.DEBUG);

        String input = "abcd";
        GearleyParser parser = grammar.getParser(options, grammar.getNonterminal("S"));
        GearleyResult result = parser.parse(input);
        Assertions.assertTrue(result.succeeded());
    }

    @Test
    public void bbb() {
        GrammarParser gparser = new GrammarParser();
        SourceGrammar grammar = gparser.parse(
                "S => S, S\n" +
                        "S => 'b'");
        // grammar.getParseListener().setMessageLevel(ParseListener.DEBUG);

        String input = "bbb";

        GearleyParser parser = grammar.getParser(options, grammar.getNonterminal("S"));
        GearleyResult result = parser.parse(input);
        Assertions.assertTrue(result.succeeded());
    }

    @Test
    public void Sabbb() {
        GrammarParser gparser = new GrammarParser();
        SourceGrammar grammar = gparser.parse(
                "S => A, T\n" +
                        "S => 'a', T\n" +
                        "A => 'a'\n" +
                        "A => B, A\n" +
                        "B => 'b'\n" +
                        "T => 'b', 'b', 'b'");
        // grammar.getParseListener().setMessageLevel(ParseListener.DEBUG);

        String input = "abbb";

        GearleyParser parser = grammar.getParser(options, grammar.getNonterminal("S"));
        GearleyResult result = parser.parse(input);
        Assertions.assertTrue(result.succeeded());
    }

    @Test
    public void sum() {
        GrammarParser gparser = new GrammarParser();
        SourceGrammar grammar = gparser.parseFile("src/test/resources/expression.grammar");
        // grammar.getParseListener().setMessageLevel(ParseListener.DEBUG);

        String input = "1+(2*3-4)";

        GearleyParser parser = grammar.getParser(options, grammar.getNonterminal("Sum"));
        GearleyResult result = parser.parse(input);
        Assertions.assertTrue(result.succeeded());
    }
}
