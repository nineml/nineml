package org.nineml.coffeegrinder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.tokens.CharacterSet;
import org.nineml.coffeegrinder.tokens.TokenCharacterSet;
import org.nineml.coffeegrinder.trees.Arborist;
import org.nineml.coffeegrinder.trees.NopTreeBuilder;
import org.nineml.coffeegrinder.util.GrammarParser;
import org.nineml.coffeegrinder.util.ParserAttribute;

import java.util.ArrayList;
import java.util.List;

public class ReadmeExampleTests {
    private final ParserOptions options = new ParserOptions();

    @Test
    public void idTestGrammar() {
        String input = "id => word\n" +
                "id => hex\n" +
                "word => letter, letter, letter\n" +
                "hex => digit, digit, digit\n" +
                "letter => [\"a\"-\"z\"; \"A\"-\"Z\"]\n" +
                "digit => [\"0\"-\"9\"; \"a\"-\"f\"; \"A\"-\"F\"]";
        GrammarParser grammarParser = new GrammarParser();
        SourceGrammar grammar = grammarParser.parse(input);
        Assertions.assertNotNull(grammar);

        GearleyParser parser = grammar.getParser(options, "id");
        GearleyResult result = parser.parse("07f");
        Assertions.assertTrue(result.succeeded());

        //result.getForest().serialize("id07f-graph.xml");
        //result.getForest().parse().serialize("id07f-tree.xml");
    }

    private SourceGrammar idGrammar() {
        SourceGrammar grammar = new SourceGrammar();
        NonterminalSymbol id = grammar.getNonterminal("id");
        NonterminalSymbol word = grammar.getNonterminal("word");
        NonterminalSymbol hex = grammar.getNonterminal("hex");

        CharacterSet set_0_9 = CharacterSet.range('0', '9');
        CharacterSet set_a_f = CharacterSet.range('a', 'f');
        CharacterSet set_A_F = CharacterSet.range('A', 'F');
        CharacterSet set_a_z = CharacterSet.range('a', 'z');
        CharacterSet set_A_Z = CharacterSet.range('A', 'Z');

        TerminalSymbol letter = new TerminalSymbol(TokenCharacterSet.inclusion(set_a_z, set_A_Z));
        TerminalSymbol digit = new TerminalSymbol(TokenCharacterSet.inclusion(set_0_9, set_a_f, set_A_F));

        grammar.addRule(id, word);
        grammar.addRule(id, hex);
        grammar.addRule(word, letter, letter, letter);
        grammar.addRule(hex, digit, digit, digit);

        return grammar;
    }

    @Test
    public void idTestApi() {
        SourceGrammar grammar = idGrammar();

        GearleyParser parser = grammar.getParser(options, "id");
        GearleyResult result = parser.parse("07f");
        Assertions.assertTrue(result.succeeded());

        Arborist walker = result.getArborist();
        walker.getTree(new NopTreeBuilder());
    }

    @Test
    public void idTestFab() {
        SourceGrammar grammar = idGrammar();

        GearleyParser parser = grammar.getParser(options, "id");
        GearleyResult result = parser.parse("fab");
        Assertions.assertTrue(result.succeeded());

        Arborist walker = result.getArborist();
        walker.getTree(new NopTreeBuilder());
    }

    @Test
    public void idTestRepeat() {
        String input = "id => word\n" +
                "id => hex\n" +
                "word => letter, _letter\n" +
                "_letter => letter, _letter\n" +
                "_letter => letter\n" +
                "hex => digit, _digit\n" +
                "_digit => digit, _digit\n" +
                "_digit => digit\n" +
                "letter => [\"a\"-\"z\"; \"A\"-\"Z\"]\n" +
                "digit => [\"0\"-\"9\"; \"a\"-\"f\"; \"A\"-\"F\"]";
        GrammarParser grammarParser = new GrammarParser();
        SourceGrammar grammar = grammarParser.parse(input);
        Assertions.assertNotNull(grammar);

        GearleyParser parser = grammar.getParser(options, "id");
        GearleyResult result = parser.parse("12345");
        Assertions.assertTrue(result.succeeded());

        //result.getForest().serialize("repeat-graph.xml");
        //result.getForest().parse().serialize("repeat-tree.xml");
    }

    @Test
    public void idTestRepeatApi() {
        SourceGrammar grammar = new SourceGrammar();
        NonterminalSymbol id = grammar.getNonterminal("id");
        NonterminalSymbol word = grammar.getNonterminal("word");
        NonterminalSymbol hex = grammar.getNonterminal("hex");
        NonterminalSymbol _letter = grammar.getNonterminal("_letter");
        NonterminalSymbol _digit = grammar.getNonterminal("_digit");

        CharacterSet set_0_9 = CharacterSet.range('0', '9');
        CharacterSet set_a_f = CharacterSet.range('a', 'f');
        CharacterSet set_A_F = CharacterSet.range('A', 'F');
        CharacterSet set_a_z = CharacterSet.range('a', 'z');
        CharacterSet set_A_Z = CharacterSet.range('A', 'Z');

        TerminalSymbol letter = new TerminalSymbol(TokenCharacterSet.inclusion(set_a_z, set_A_Z));
        TerminalSymbol digit = new TerminalSymbol(TokenCharacterSet.inclusion(set_0_9, set_a_f, set_A_F));

        grammar.addRule(id, word);
        grammar.addRule(id, hex);
        grammar.addRule(word, letter);
        grammar.addRule(word, letter, _letter);
        grammar.addRule(_letter, letter);
        grammar.addRule(_letter, letter, _letter);
        grammar.addRule(hex, digit);
        grammar.addRule(hex, digit, _digit);
        grammar.addRule(_digit, digit);
        grammar.addRule(_digit, digit, _digit);

        Assertions.assertNotNull(grammar);

        GearleyParser parser = grammar.getParser(options, "id");
        GearleyResult result = parser.parse("12345");
        Assertions.assertTrue(result.succeeded());

        //result.getForest().serialize("repeat-graph.xml");
        //result.getForest().parse().serialize("repeat-tree.xml");
    }

    @Test
    public void idTestRepeatOptionalApi() {
        SourceGrammar grammar = new SourceGrammar();
        NonterminalSymbol id = grammar.getNonterminal("id");
        NonterminalSymbol word = grammar.getNonterminal("word");
        NonterminalSymbol hex = grammar.getNonterminal("hex");
        NonterminalSymbol _letter = grammar.getNonterminal("_letter");
        NonterminalSymbol _digit = grammar.getNonterminal("_digit");

        CharacterSet set_0_9 = CharacterSet.range('0', '9');
        CharacterSet set_a_f = CharacterSet.range('a', 'f');
        CharacterSet set_A_F = CharacterSet.range('A', 'F');
        CharacterSet set_a_z = CharacterSet.range('a', 'z');
        CharacterSet set_A_Z = CharacterSet.range('A', 'Z');

        TerminalSymbol letter = new TerminalSymbol(TokenCharacterSet.inclusion(set_a_z, set_A_Z));
        TerminalSymbol digit = new TerminalSymbol(TokenCharacterSet.inclusion(set_0_9, set_a_f, set_A_F));

        grammar.addRule(id, word);
        grammar.addRule(id, hex);
        grammar.addRule(word, letter, _letter);
        grammar.addRule(_letter, letter, _letter);
        grammar.addRule(_letter);
        grammar.addRule(hex, digit, _digit);
        grammar.addRule(_digit, digit, _digit);
        grammar.addRule(_digit);

        Assertions.assertNotNull(grammar);

        GearleyParser parser = grammar.getParser(options, "id");
        GearleyResult result = parser.parse("12345");
        Assertions.assertTrue(result.succeeded());

        //result.getForest().serialize("repeat-graph.xml");
        //result.getForest().parse().serialize("repeat-tree.xml");
    }

    @Test
    public void idTestRepeatPrunableApi() {
        SourceGrammar grammar = new SourceGrammar();
        NonterminalSymbol id = grammar.getNonterminal("id");
        NonterminalSymbol word = grammar.getNonterminal("word");
        NonterminalSymbol hex = grammar.getNonterminal("hex");

        List<ParserAttribute> attributes = new ArrayList<>();

        NonterminalSymbol _letter = grammar.getNonterminal("_letter", attributes);
        NonterminalSymbol _digit = grammar.getNonterminal("_digit", attributes);

        CharacterSet set_0_9 = CharacterSet.range('0', '9');
        CharacterSet set_a_f = CharacterSet.range('a', 'f');
        CharacterSet set_A_F = CharacterSet.range('A', 'F');
        CharacterSet set_a_z = CharacterSet.range('a', 'z');
        CharacterSet set_A_Z = CharacterSet.range('A', 'Z');

        TerminalSymbol letter = new TerminalSymbol(TokenCharacterSet.inclusion(set_a_z, set_A_Z));
        TerminalSymbol digit = new TerminalSymbol(TokenCharacterSet.inclusion(set_0_9, set_a_f, set_A_F));

        grammar.addRule(id, word);
        grammar.addRule(id, hex);
        grammar.addRule(word, letter, _letter);
        grammar.addRule(_letter, letter, _letter);
        grammar.addRule(_letter);
        grammar.addRule(hex, digit, _digit);
        grammar.addRule(_digit, digit, _digit);
        grammar.addRule(_digit);

        Assertions.assertNotNull(grammar);

        GearleyParser parser = grammar.getParser(options, id);
        GearleyResult result = parser.parse("12345");
        Assertions.assertTrue(result.succeeded());

        //result.getForest().serialize("repeat-graph.xml");
        //result.getForest().parse().serialize("repeat-tree.xml");
    }

}
