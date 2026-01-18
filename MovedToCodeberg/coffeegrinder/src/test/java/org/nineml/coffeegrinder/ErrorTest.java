package org.nineml.coffeegrinder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.nineml.coffeegrinder.exceptions.GrammarException;
import org.nineml.coffeegrinder.exceptions.ParseException;
import org.nineml.coffeegrinder.parser.ParserOptions;
import org.nineml.coffeegrinder.parser.SourceGrammar;
import org.nineml.coffeegrinder.util.GrammarParser;

public class ErrorTest {
    private final ParserOptions options = new ParserOptions();

    @Test
    public void missingSymbol() {
        GrammarParser gparser = new GrammarParser();
        SourceGrammar grammar = gparser.parse(
                "  S => A, B\n" +
                        "A => 'a'");

        try {
            grammar.getParser(options, grammar.getNonterminal("S"));
        } catch (ParseException ex) {
            Assertions.assertEquals("P001", ex.getCode());
        }
    }

    @Test
    public void invalidCharacterClassLq() {
        GrammarParser gparser = new GrammarParser();
        try {
            gparser.parse(
                    "  S => A, B\n" +
                            "A => 'a'\n" +
                            "B => [Lq]");
            Assertions.fail();
        } catch (GrammarException ex) {
            Assertions.assertEquals("E002", ex.getCode());
        }
    }
}
