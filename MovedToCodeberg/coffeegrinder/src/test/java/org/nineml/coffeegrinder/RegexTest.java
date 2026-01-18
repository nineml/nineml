package org.nineml.coffeegrinder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.tokens.TokenRegex;
import org.nineml.coffeegrinder.trees.Arborist;
import org.nineml.coffeegrinder.trees.StringTreeBuilder;

public class RegexTest extends CoffeeGrinderTest {
    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void nonregex(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);

        SourceGrammar grammar = new SourceGrammar();

        /*
        S = A, B, A.
        A = 'a'
        B = X
        X = 'b'
         */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B = grammar.getNonterminal("B");
        NonterminalSymbol _X = grammar.getNonterminal("X");
        grammar.addRule(_S,_A, _B, _A);
        grammar.addRule(_A, TerminalSymbol.ch('a'));
        grammar.addRule(_B, _X);
        grammar.addRule(_X, TerminalSymbol.ch('b'));

        String input = "aba";

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(input);
        Assertions.assertTrue(result.succeeded());

        StringTreeBuilder builder = new StringTreeBuilder();
        result.getArborist().getTree(builder);
        Assertions.assertEquals("<S><A>a</A><B><X>b</X></B><A>a</A></S>", builder.getTree());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void regexDigits(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);

        SourceGrammar grammar = new SourceGrammar();
        /*
        S: A, D, A
        A: 'a'
        D: ['0'-'9']+
         */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _D = grammar.getNonterminal("D");

        grammar.addRule(_S, _A, _D, _A);
        grammar.addRule(_A, TerminalSymbol.ch('a'));
        grammar.addRule(_D, TerminalSymbol.regex("[0-9]+"));

        String input = "a012333a";

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(input);
        Assertions.assertTrue(result.succeeded());

        StringTreeBuilder builder = new StringTreeBuilder();
        result.getArborist().getTree(builder);
        Assertions.assertEquals("<S><A>a</A><D>012333</D><A>a</A></S>", builder.getTree());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void regexAlts(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);

        SourceGrammar grammar = new SourceGrammar();

        /*
        S: A, D, A
        A: 'a'
        D: ['0'-'5']+
        D: ['6'-'9']+
         */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _digits = grammar.getNonterminal("digits");
        NonterminalSymbol _D = grammar.getNonterminal("D");

        grammar.addRule(_S, _A, _digits, _A);
        grammar.addRule(_A, TerminalSymbol.ch('a'));
        grammar.addRule(_digits, _D, _digits);
        grammar.addRule(_digits, _D);
        grammar.addRule(_D, TerminalSymbol.regex("[0-7]"));
        grammar.addRule(_D, TerminalSymbol.regex("[3-9]"));

        String input = "a02468a";

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(input);
        Assertions.assertEquals(1, result.getForest().getParseTreeCount());
        StringTreeBuilder builder = new StringTreeBuilder();
        result.getArborist().getTree(builder);
        Assertions.assertEquals("<S><A>a</A><digits><D>0</D><digits><D>2</D><digits><D>4</D><digits><D>6</D><digits><D>8</D></digits></digits></digits></digits></digits><A>a</A></S>", builder.getTree());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void regex1match(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);

        SourceGrammar grammar = new SourceGrammar();

        /*
        S: 'a', L, 'z'
        L: (regex) | 'X'
         */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _L = grammar.getNonterminal("L");

        TerminalSymbol b = new TerminalSymbol(TokenRegex.get("[\\.b-y]+"));

        grammar.addRule(_S, TerminalSymbol.ch('a'), _L, TerminalSymbol.ch('z'));
        grammar.addRule(_L, b);
        grammar.addRule(_L, TerminalSymbol.ch('X'));

        String input = "abcd...wxyz";

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(input);
        Assertions.assertTrue(result.succeeded());

        StringTreeBuilder builder = new StringTreeBuilder();
        result.getArborist().getTree(builder);
        Assertions.assertEquals("<S>a<L>bcd...wxy</L>z</S>", builder.getTree());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void regex1nomatch(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);

        SourceGrammar grammar = new SourceGrammar();

        /*
        S: 'a', L, 'z'
        L: (regex) | 'X'
         */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _L = grammar.getNonterminal("L");

        TerminalSymbol b = new TerminalSymbol(TokenRegex.get("[\\.b-y]+"));

        grammar.addRule(_S, TerminalSymbol.ch('a'), _L, TerminalSymbol.ch('z'));
        grammar.addRule(_L, b);
        grammar.addRule(_L, TerminalSymbol.ch('X'));

        String input = "aXz";

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(input);
        Assertions.assertTrue(result.succeeded());

        StringTreeBuilder builder = new StringTreeBuilder();
        result.getArborist().getTree(builder);
        Assertions.assertEquals("<S>a<L>X</L>z</S>", builder.getTree());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void regex2match(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType(parserType);

        SourceGrammar grammar = new SourceGrammar();

        /*
        file  = lines
        lines = line, lines
        lines =
        line = linea
        line = lineb
        linea = [a-zA-Z0-9 ]*, ';', NL
        lineb = [a-zA-Z0-9 ]*, ':', NL
        NL = #a
         */

        NonterminalSymbol _file = grammar.getNonterminal("file");
        NonterminalSymbol _lines = grammar.getNonterminal("lines");
        NonterminalSymbol _line = grammar.getNonterminal("line");
        NonterminalSymbol _linea = grammar.getNonterminal("linea");
        NonterminalSymbol _lineb = grammar.getNonterminal("lineb");
        NonterminalSymbol _nl = grammar.getNonterminal("nl");
        NonterminalSymbol _chars = grammar.getNonterminal("chars");

        grammar.addRule(_file, _lines);
        grammar.addRule(_lines, _line, _lines);
        grammar.addRule(_lines);
        grammar.addRule(_line, _linea);
        grammar.addRule(_line, _lineb);
        grammar.addRule(_linea, _chars, TerminalSymbol.ch(';'), _nl);
        grammar.addRule(_lineb, _chars, TerminalSymbol.ch(':'), _nl);
        grammar.addRule(_chars, new TerminalSymbol(TokenRegex.get("[a-zA-Z0-9 ]*")));
        grammar.addRule(_nl, TerminalSymbol.ch('\n'));

        String input = "abc;\ndef:\nghi;\n";

        GearleyParser parser = grammar.getParser(options, _file);
        GearleyResult result = parser.parse(input);
        StringTreeBuilder builder = new StringTreeBuilder();
        result.getArborist().getTree(builder);
        //System.err.println(builder.getTree());
        Assertions.assertEquals("<file><lines><line><linea><chars>abc</chars>;<nl>\n" +
                "</nl></linea></line><lines><line><lineb><chars>def</chars>:<nl>\n" +
                "</nl></lineb></line><lines><line><linea><chars>ghi</chars>;<nl>\n" +
                "</nl></linea></line><lines></lines></lines></lines></lines></file>", builder.getTree());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void regexZeroOrMore_selectnone(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        //options.getLogger().setDefaultLogLevel("trace");
        options.setParserType(parserType);

        SourceGrammar grammar = new SourceGrammar();
        /*
        S: A, D, A
        A: 'a'
        D: ['0'-'9']*
         */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _D = grammar.getNonterminal("D");

        grammar.addRule(_S, _A, _D, _A);
        grammar.addRule(_A, TerminalSymbol.ch('a'));
        grammar.addRule(_D, TerminalSymbol.regex("[0-9]*"));
        //grammar.addRule(_D);

        String input = "aa";

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(input);
        Assertions.assertTrue(result.succeeded());

        StringTreeBuilder builder = new StringTreeBuilder();
        result.getArborist().getTree(builder);
        Assertions.assertEquals("<S><A>a</A><D></D><A>a</A></S>", builder.getTree());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void regexZeroOrMore_selectsome(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        //options.getLogger().setDefaultLogLevel("trace");
        options.setParserType(parserType);

        SourceGrammar grammar = new SourceGrammar();
        /*
        S: A, D, A
        A: 'a'
        D: ['0'-'9']*
         */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _D = grammar.getNonterminal("D");

        grammar.addRule(_S, _A, _D, _A);
        grammar.addRule(_A, TerminalSymbol.ch('a'));
        grammar.addRule(_D, TerminalSymbol.regex("[0-9]*"));
        //grammar.addRule(_D);

        String input = "a123a";

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(input);
        Assertions.assertTrue(result.succeeded());

        StringTreeBuilder builder = new StringTreeBuilder();
        result.getArborist().getTree(builder);
        Assertions.assertEquals("<S><A>a</A><D>123</D><A>a</A></S>", builder.getTree());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void regexOverlap1(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        //options.getLogger().setDefaultLogLevel("trace");
        options.setParserType(parserType);

        SourceGrammar grammar = new SourceGrammar();
        /*
        S: A, D, A
        A: 'a'
        D: D1 | D2
        D1: [0-9]*
        D2: [0,2,4,6,8]*
         */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _D = grammar.getNonterminal("D");
        NonterminalSymbol _D1 = grammar.getNonterminal("D1");
        NonterminalSymbol _D2 = grammar.getNonterminal("D2");

        grammar.addRule(_S, _A, _D, _A);
        grammar.addRule(_A, TerminalSymbol.ch('a'));
        grammar.addRule(_D, _D1);
        grammar.addRule(_D, _D2);
        grammar.addRule(_D1, TerminalSymbol.regex("[0-9]*"));
        grammar.addRule(_D2, TerminalSymbol.regex("[0,2,4,6,8]*"));

        String input = "a246a";

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(input);
        Assertions.assertTrue(result.succeeded());

        StringTreeBuilder builder = new StringTreeBuilder();
        Arborist walker = result.getArborist();
        walker.getTree(builder);

        Assertions.assertEquals(2, result.getForest().getParseTreeCount());

        String xml1 = builder.getTree();

        Assertions.assertTrue("<S><A>a</A><D><D1>246</D1></D><A>a</A></S>".equals(xml1)
                || "<S><A>a</A><D><D2>246</D2></D><A>a</A></S>".equals(xml1));

        builder = new StringTreeBuilder();
        walker.getTree(builder);
        String xml2 = builder.getTree();

        Assertions.assertTrue("<S><A>a</A><D><D1>246</D1></D><A>a</A></S>".equals(xml2)
                || "<S><A>a</A><D><D2>246</D2></D><A>a</A></S>".equals(xml2));

        Assertions.assertNotEquals(xml1, xml2);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Earley", "GLL"})
    public void regexOverlap2(String parserType) {
        ParserOptions options = new ParserOptions(globalOptions);
        //options.getLogger().setDefaultLogLevel("trace");
        options.setParserType(parserType);

        SourceGrammar grammar = new SourceGrammar();
        /*
        S: A, D, A
        A: 'a'
        D: [0-9]*
        D: [0,2,4,6,8]*
         */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _D = grammar.getNonterminal("D");

        grammar.addRule(_S, _A, _D, _A);
        grammar.addRule(_A, TerminalSymbol.ch('a'));
        grammar.addRule(_D, TerminalSymbol.regex("[0-9]*"));
        grammar.addRule(_D, TerminalSymbol.regex("[0,2,4,6,8]*"));

        String input = "a246a";

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(input);
        Assertions.assertTrue(result.succeeded());

        StringTreeBuilder builder = new StringTreeBuilder();
        result.getArborist().getTree(builder);

        Assertions.assertEquals(1, result.getForest().getParseTreeCount());
        String xml = builder.getTree();

        Assertions.assertEquals("<S><A>a</A><D>246</D><A>a</A></S>", xml);
    }
}
