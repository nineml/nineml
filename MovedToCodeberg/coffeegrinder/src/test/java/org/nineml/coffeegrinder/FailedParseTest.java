package org.nineml.coffeegrinder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.tokens.TokenRegex;
import org.nineml.coffeegrinder.util.Iterators;

import java.util.ArrayList;

public class FailedParseTest extends CoffeeGrinderTest {

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void testFailEEE(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);

        SourceGrammar grammar = new SourceGrammar(options);

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _E = grammar.getNonterminal("E");
        TerminalSymbol _plus = TerminalSymbol.ch('+');

        grammar.addRule(_S, _E);
        grammar.addRule(_E, _E, _plus, _E);
        grammar.addRule(_E, new TerminalSymbol(TokenRegex.get("[0-9]")));

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(Iterators.characterIterator("1+2-3"));
        Assertions.assertFalse(result.succeeded());
        Assertions.assertEquals(4, result.getTokenCount());
        Assertions.assertEquals("-", result.getLastToken().getValue());
    }

    private TerminalSymbol[] stringToChars(String str) {
        ArrayList<TerminalSymbol> symbols = new ArrayList<>();
        for (int offset = 0; offset < str.length(); ) {
            int codepoint = str.codePointAt(offset);
            symbols.add(TerminalSymbol.ch((char) codepoint));
            offset += Character.charCount(codepoint);
        }
        return symbols.toArray(new TerminalSymbol[0]);
    }

    private SourceGrammar monthsGrammar(ParserOptions options) {
        SourceGrammar grammar = new SourceGrammar(options);
        NonterminalSymbol _month = grammar.getNonterminal("month");
        grammar.addRule(_month, stringToChars("January"));
        grammar.addRule(_month, stringToChars("February"));
        grammar.addRule(_month, stringToChars("March"));
        grammar.addRule(_month, stringToChars("April"));
        grammar.addRule(_month, stringToChars("May"));
        grammar.addRule(_month, stringToChars("June"));
        grammar.addRule(_month, stringToChars("July"));
        grammar.addRule(_month, stringToChars("August"));
        grammar.addRule(_month, stringToChars("September"));
        grammar.addRule(_month, stringToChars("October"));
        grammar.addRule(_month, stringToChars("November"));
        grammar.addRule(_month, stringToChars("December"));
        return grammar;
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void testMonths_March(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);

        try {
            SourceGrammar grammar = monthsGrammar(options);
            NonterminalSymbol start = grammar.getNonterminal("month");
            GearleyParser parser = grammar.getParser(options, start);
            GearleyResult result = parser.parse("March");
            Assertions.assertTrue(result.succeeded());

            if (result instanceof EarleyResult) {
                Assertions.assertTrue(((EarleyResult) result).getPredictedTerminals().isEmpty());
            }

        } catch (Exception ex) {
            Assertions.fail();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void testMonths_Max(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);

        try {
            SourceGrammar grammar = monthsGrammar(options);
            NonterminalSymbol start = grammar.getNonterminal("month");
            GearleyParser parser = grammar.getParser(options, start);
            GearleyResult result = parser.parse("Max");
            Assertions.assertFalse(result.succeeded());

            if (result instanceof EarleyResult) {
                Assertions.assertEquals(2, ((EarleyResult) result).getPredictedTerminals().size());
                for (TerminalSymbol t : ((EarleyResult) result).getPredictedTerminals()) {
                    String value = t.getToken().getValue();
                    Assertions.assertTrue("r".equals(value) || "y".equals(value));
                }
            }
        } catch (Exception ex) {
            Assertions.fail();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void testMonths_Marsh(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);

        try {
            SourceGrammar grammar = monthsGrammar(options);
            NonterminalSymbol start = grammar.getNonterminal("month");
            GearleyParser parser = grammar.getParser(options, start);
            GearleyResult result = parser.parse("Marsh");
            Assertions.assertFalse(result.succeeded());
            if (result instanceof EarleyResult) {
                Assertions.assertEquals(1, ((EarleyResult) result).getPredictedTerminals().size());
                for (TerminalSymbol t : ((EarleyResult) result).getPredictedTerminals()) {
                    String value = t.getToken().getValue();
                    Assertions.assertEquals("c", value);
                }
            }
        } catch (Exception ex) {
            Assertions.fail();
        }
    }

}
