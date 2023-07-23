package org.nineml.coffeegrinder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.tokens.TokenCharacter;
import org.nineml.coffeegrinder.trees.StdoutTreeBuilder;
import org.nineml.coffeegrinder.trees.TreeBuilder;

public class GreedyTest extends CoffeeGrinderTest {
    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void testABC(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);

        SourceGrammar grammar = new SourceGrammar();

        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol A = grammar.getNonterminal("A");
        NonterminalSymbol B = grammar.getNonterminal("B");
        NonterminalSymbol C = grammar.getNonterminal("C");
        NonterminalSymbol bplus = grammar.getNonterminal("bplus");
        NonterminalSymbol bstar = grammar.getNonterminal("bstar");
        NonterminalSymbol boption = grammar.getNonterminal("boption");
        TerminalSymbol a = new TerminalSymbol(TokenCharacter.get('a'));
        TerminalSymbol b = new TerminalSymbol(TokenCharacter.get('b'));
        TerminalSymbol c = new TerminalSymbol(TokenCharacter.get('c'));
        TerminalSymbol d = new TerminalSymbol(TokenCharacter.get('d'));

        grammar.addRule(S, A, B, C);
        grammar.addRule(A, a);
        grammar.addRule(C, c);
        /*
        grammar.addRule(B, b);
        grammar.addRule(B, b, B);
         */
        grammar.addRule(B, bplus);
        grammar.addRule(bplus, b, boption);
        grammar.addRule(bstar, boption);
        grammar.addRule(boption);
        grammar.addRule(boption, b, bstar);

        //options.getLogger().setDefaultLogLevel(99);
        GearleyParser parser = grammar.getParser(options, S);
        GearleyResult result = parser.parse("abbbbbc");
        Assertions.assertTrue(result.succeeded());
    }
}
