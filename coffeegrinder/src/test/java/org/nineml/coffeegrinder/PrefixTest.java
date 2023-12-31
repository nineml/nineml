package org.nineml.coffeegrinder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.tokens.TokenString;
import org.nineml.coffeegrinder.trees.StringTreeBuilder;
import org.nineml.coffeegrinder.util.Iterators;

import java.util.Arrays;
import java.util.Iterator;

public class PrefixTest {
    private final ParserOptions options = new ParserOptions();

    @Test
    public void testAB() {
        ParserOptions options = new ParserOptions();
        options.setPrefixParsing(true);
        SourceGrammar grammar = new SourceGrammar(options);

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B = grammar.getNonterminal("B");
        TerminalSymbol _a = TerminalSymbol.ch('a');
        TerminalSymbol _b = TerminalSymbol.ch('b');

        grammar.addRule(_S, _A, _B);
        grammar.addRule(_A, _a);
        grammar.addRule(_B, _b, _B);
        grammar.addRule(_B);

        GearleyParser parser = grammar.getParser(options, _S);

        if (parser.getParserType() != ParserType.Earley) {
            System.err.println("Prefix parsing is only supported by the Earley parser");
            return;
        }

        Iterator<Token> input = Iterators.characterIterator("abbabbbba");

        GearleyResult result = parser.parse(input);
        Assertions.assertFalse(result.succeeded());
        Assertions.assertTrue(result.prefixSucceeded());

        result = result.continueParsing();
        Assertions.assertFalse(result.succeeded());

        result = result.continueParsing();
        Assertions.assertTrue(result.succeeded());
    }

    @Test
    public void testParens() {
        ParserOptions options = new ParserOptions();
        options.setPrefixParsing(true);
        SourceGrammar grammar = new SourceGrammar(options);

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _B = grammar.getNonterminal("B");
        TerminalSymbol _b = TerminalSymbol.ch('b');
        TerminalSymbol _op = TerminalSymbol.ch('(');
        TerminalSymbol _cp = TerminalSymbol.ch(')');

        grammar.addRule(_S, _B);
        grammar.addRule(_S, _op, _S, _cp);
        grammar.addRule(_B, _b);
        grammar.addRule(_B, _B);

        GearleyParser parser = grammar.getParser(options, _S);

        if (parser.getParserType() != ParserType.Earley) {
            System.err.println("Prefix parsing is only supported by the Earley parser");
            return;
        }

        Iterator<Token> input = Iterators.characterIterator("(b))");

        GearleyResult result = parser.parse(input);
        Assertions.assertFalse(result.succeeded());
        Assertions.assertTrue(result.prefixSucceeded());
    }

    @Test
    public void testChangeParser() {
        ParserOptions options = new ParserOptions();
        options.setPrefixParsing(true);

        SourceGrammar grammar1 = new SourceGrammar(options);
        SourceGrammar grammar2 = new SourceGrammar(options);

        NonterminalSymbol _S1 = grammar1.getNonterminal("S");
        NonterminalSymbol _S2 = grammar2.getNonterminal("S");
        NonterminalSymbol _A = grammar1.getNonterminal("A");
        NonterminalSymbol _B = grammar2.getNonterminal("B");
        TerminalSymbol _a = TerminalSymbol.ch('a');
        TerminalSymbol _b = TerminalSymbol.ch('b');

        grammar1.addRule(_S1, _A);
        grammar1.addRule(_A, _a);
        grammar1.addRule(_A, _a, _A);

        grammar2.addRule(_S2, _B);
        grammar2.addRule(_B, _b);
        grammar2.addRule(_B, _b, _B);

        GearleyParser parser1 = grammar1.getParser(options, _S1);
        GearleyParser parser2 = grammar2.getParser(options, _S2);

        if (parser1.getParserType() != ParserType.Earley) {
            System.err.println("Prefix parsing is only supported by the Earley parser");
            return;
        }

        Iterator<Token> input = Iterators.characterIterator("aaaabbbb");

        GearleyResult result = parser1.parse(input);
        Assertions.assertFalse(result.succeeded());
        Assertions.assertTrue(result.prefixSucceeded());

        result = result.continueParsing(parser2);
        Assertions.assertTrue(result.succeeded());
    }

    @Test
    public void issue17() {
        ParserOptions options = new ParserOptions();
        options.setPrefixParsing(true);
        options.setParserType("Earley");

        SourceGrammar g = new SourceGrammar();
        NonterminalSymbol expr = g.getNonterminal("expression");
        NonterminalSymbol number = g.getNonterminal("number");
        TokenString plusToken = TokenString.get("+");
        TokenString fiveToken = TokenString.get("5");
        TokenString ftToken = TokenString.get("42");
        TerminalSymbol plus = new TerminalSymbol(plusToken);
        g.addRule(expr,number,plus,expr);
        g.addRule(expr,number);
        g.addRule(number,new TerminalSymbol(fiveToken));
        g.addRule(number,new TerminalSymbol(ftToken));
        g.getParserOptions().setPrefixParsing(true);
        GearleyParser parser = g.getParser(g.getParserOptions(),"expression");

        String[] args = new String[] { "5", "+", "42", "5", "+", "5" };

        TokenString[] args2 = Arrays.stream(args).map(TokenString::get).toArray(TokenString[]::new);
        GearleyResult result = parser.parse(args2);

        StringTreeBuilder builder = new StringTreeBuilder();
        result.getArborist().getTree(builder);
        Assertions.assertEquals("<expression><number>5</number>+<expression><number>42</number></expression></expression>", builder.getTree());

        result = result.continueParsing();
        builder = new StringTreeBuilder();
        result.getArborist().getTree(builder);
        Assertions.assertEquals("<expression><number>5</number>+<expression><number>5</number></expression></expression>", builder.getTree());
    }
}
