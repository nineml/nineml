package org.nineml.coffeegrinder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.tokens.*;
import org.nineml.coffeegrinder.trees.Arborist;
import org.nineml.coffeegrinder.trees.GenericTree;
import org.nineml.coffeegrinder.trees.GenericTreeBuilder;
import org.nineml.coffeegrinder.trees.StringTreeBuilder;
import org.nineml.coffeegrinder.util.GrammarParser;
import org.nineml.coffeegrinder.util.Iterators;
import org.nineml.coffeegrinder.util.ParserAttribute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;

public class UndefinedSymbolTest extends CoffeeGrinderTest {
    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void testUndefinedSuccess(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);
        GrammarParser gparser = new GrammarParser();
        SourceGrammar grammar = gparser.parse(
                "S => A, B\n" +
                        "A => 'a'\n" +
                        "B => 'b'\n" +
                        "C => D\n");
        options.getLogger().setDefaultLogLevel("debug");

        GearleyParser parser = grammar.getParser(options, grammar.getNonterminal("S"));
        GearleyResult result = parser.parse(Iterators.characterIterator("ab"));
        Assertions.assertTrue(result.succeeded());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void testUndefinedFailure(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);
        GrammarParser gparser = new GrammarParser();
        SourceGrammar grammar = gparser.parse(
                "S => A, B, C\n" +
                        "A => 'a'\n" +
                        "B => 'b'\n" +
                        "C => D\n");
        options.getLogger().setDefaultLogLevel("debug");

        GearleyParser parser = grammar.getParser(options, grammar.getNonterminal("S"));
        GearleyResult result = parser.parse(Iterators.characterIterator("abc"));
        Assertions.assertFalse(result.succeeded());
    }

}
