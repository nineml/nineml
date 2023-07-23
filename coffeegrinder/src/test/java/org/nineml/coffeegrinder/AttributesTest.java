package org.nineml.coffeegrinder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.nineml.coffeegrinder.exceptions.ParseException;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.tokens.TokenString;
import org.nineml.coffeegrinder.trees.*;
import org.nineml.coffeegrinder.util.GrammarParser;
import org.nineml.coffeegrinder.util.Iterators;
import org.nineml.coffeegrinder.util.ParserAttribute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class AttributesTest extends CoffeeGrinderTest {
    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void ifThenElseTest(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);

        SourceGrammar grammar = new SourceGrammar();

        NonterminalSymbol _statement = grammar.getNonterminal("statement");
        NonterminalSymbol _condition = grammar.getNonterminal("condition", new ParserAttribute("test", "this"));
        TerminalSymbol _if = new TerminalSymbol(TokenString.get("if"));
        TerminalSymbol _then = TerminalSymbol.s("then");
        TerminalSymbol _else = TerminalSymbol.s("else");
        NonterminalSymbol _variable = grammar.getNonterminal("variable");
        TerminalSymbol _eqeq = TerminalSymbol.s("==");
        TerminalSymbol _eq = TerminalSymbol.s("=");
        TerminalSymbol _op = new TerminalSymbol(TokenString.get("("), new ParserAttribute("open", "op"));
        TerminalSymbol _cp = TerminalSymbol.s(")");

        grammar.addRule(_statement, _if, _condition, _then, _statement);
        grammar.addRule(_statement, _if, _condition, _then, _statement, _else, _statement);
        grammar.addRule(_statement, _variable, _eq, _variable);
        grammar.addRule(_condition, _op, _variable, _eqeq, _variable, _cp);
        grammar.addRule(_variable, new TerminalSymbol(TokenString.get("a")));
        grammar.addRule(_variable, new TerminalSymbol(TokenString.get("b")));
        grammar.addRule(_variable, new TerminalSymbol(TokenString.get("c")));
        grammar.addRule(_variable, new TerminalSymbol(TokenString.get("d")));

        GearleyParser parser = grammar.getParser(options, _statement);

        ArrayList<ParserAttribute> attrs = new ArrayList<>();
        attrs.add(new ParserAttribute("line", "1"));
        attrs.add(new ParserAttribute("column", "5"));

        Token[] inputTokens = new Token[] {
                TokenString.get("if", attrs),
                TokenString.get("("),
                TokenString.get("a"),
                TokenString.get("=="),
                TokenString.get("b"),
                TokenString.get(")"),
                TokenString.get("then"),
                TokenString.get("c"),
                TokenString.get("="),
                TokenString.get("d")
        };

        final GearleyResult result;
        if ("GLL".equals(parserType)) {
            try {
                result = parser.parse(inputTokens);
                Assertions.fail();
            } catch (ParseException ex) {
                Assertions.assertEquals("P004", ex.getCode());
                return;
            }
        } else {
            result = parser.parse(inputTokens);
            Assertions.assertTrue(result.succeeded());
        }

        Arborist walker = Arborist.getArborist(result.getForest());
        GenericTreeBuilder builder = new GenericTreeBuilder();
        walker.getTree(builder);
        GenericBranch tree = builder.getTree();

        Token t_if = ((GenericLeaf) tree.getChildren().get(0)).token;
        Assertions.assertEquals("if", t_if.getValue());
        Assertions.assertEquals("1", Objects.requireNonNull(t_if.getAttribute("line").getValue()));
        Assertions.assertEquals("5", Objects.requireNonNull(t_if.getAttribute("column").getValue()));

        GenericBranch nt_condition = (GenericBranch) tree.getChildren().get(1);
        Assertions.assertEquals(grammar.getNonterminal("condition"), nt_condition.symbol);
        Assertions.assertEquals("this", Objects.requireNonNull(nt_condition.symbol.getAttribute("test")).getValue());

        GenericLeaf s_paren = (GenericLeaf) nt_condition.getChildren().get(0);
        Assertions.assertEquals("(", s_paren.token.getValue());
        Assertions.assertEquals("op", Objects.requireNonNull(s_paren.getAttribute("open", "fail")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void testChoice(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);

        GrammarParser gparser = new GrammarParser();
        SourceGrammar grammar = gparser.parse(
                "start => X\n" +
                        "X => A, Y\n" +
                        "A => 'a'\n" +
                        "B => 'b'\n" +
                        "C => 'c'\n" +
                        "Y => 'a'\n" +
                        "Y => 'b'\n" +
                        "Y => 'c'\n");
        // grammar.getParseListener().setMessageLevel(ParseListener.DEBUG);

        GearleyParser parser = grammar.getParser(options, grammar.getNonterminal("start"));
        GearleyResult result = parser.parse(Iterators.characterIterator("ab"));
        Assertions.assertTrue(result.succeeded());

        //result.getForest().serialize("testchoice.xml");

        Assertions.assertEquals(1, result.getForest().getParseTreeCount());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void testRHSAttributes(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);

        SourceGrammar grammar = new SourceGrammar();

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B0 = grammar.getNonterminal("B", new ParserAttribute("N", "0"));
        NonterminalSymbol _B1 = grammar.getNonterminal("B", new ParserAttribute("N", "1"));
        NonterminalSymbol _B2 = grammar.getNonterminal("B", new ParserAttribute("N", "2"));
        NonterminalSymbol _C = grammar.getNonterminal("C");
        NonterminalSymbol _D = grammar.getNonterminal("D");

        grammar.addRule(_S, _A, _B1, _C);
        grammar.addRule(_S, _A, _B2, _D);
        grammar.addRule(_A, TerminalSymbol.ch('a'));
        grammar.addRule(_B0, TerminalSymbol.ch('b'));
        grammar.addRule(_B1, TerminalSymbol.ch('b'));
        grammar.addRule(_B2, TerminalSymbol.ch('b'));
        grammar.addRule(_C, TerminalSymbol.ch('c'));
        grammar.addRule(_D, TerminalSymbol.ch('c'));

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(Iterators.characterIterator("abc"));
        Assertions.assertTrue(result.succeeded());

        Assertions.assertEquals(2, result.getForest().getParseTreeCount());

        expectTrees(Arborist.getArborist(result.getForest()), Arrays.asList(
                "<S><A>a</A><B N='1'>b</B><C>c</C></S>",
                "<S><A>a</A><B N='2'>b</B><D>c</D></S>"));
    }
}
