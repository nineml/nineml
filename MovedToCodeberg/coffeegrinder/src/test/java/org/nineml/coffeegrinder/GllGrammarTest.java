package org.nineml.coffeegrinder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.tokens.CharacterSet;
import org.nineml.coffeegrinder.tokens.TokenCharacter;
import org.nineml.coffeegrinder.tokens.TokenCharacterSet;
import org.nineml.coffeegrinder.tokens.TokenRegex;
import org.nineml.coffeegrinder.trees.*;
import org.nineml.coffeegrinder.util.ParserAttribute;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;

public class GllGrammarTest extends CoffeeGrinderTest {
    @Test
    public void testBsrExample54() {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType("GLL");
        
        SourceGrammar grammar = new SourceGrammar();

        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol A = grammar.getNonterminal("A");
        NonterminalSymbol B = grammar.getNonterminal("B");
        NonterminalSymbol C = grammar.getNonterminal("C");
        TerminalSymbol a = new TerminalSymbol(TokenCharacter.get('a'));
        TerminalSymbol b = new TerminalSymbol(TokenCharacter.get('b'));

        grammar.addRule(S, A, C, a, B);
        grammar.addRule(S, A, B, a, a);
        grammar.addRule(A, a, A);
        grammar.addRule(A, a);
        grammar.addRule(B, b, B);
        grammar.addRule(B, b);
        grammar.addRule(C, b, C);
        grammar.addRule(C, b);

        GearleyParser parser = grammar.getParser(options, S);
        GearleyResult result = parser.parse("abaa");

        Assertions.assertTrue(result.succeeded());
    }

    @Test
    public void testGrammar0() {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType("GLL");

        SourceGrammar grammar = new SourceGrammar();

        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol B = grammar.getNonterminal("B");
        TerminalSymbol a = new TerminalSymbol(TokenCharacter.get('a'));
        TerminalSymbol b = new TerminalSymbol(TokenCharacter.get('b'));

        grammar.addRule(S, a, B);
        grammar.addRule(B, b);

        GearleyParser parser = grammar.getParser(options, S);
        GearleyResult result = parser.parse("ab");
        Assertions.assertTrue(result.succeeded());
    }

    @Test
    public void testGrammar05() {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType("GLL");

        SourceGrammar grammar = new SourceGrammar();

        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol A = grammar.getNonterminal("A");
        NonterminalSymbol B = grammar.getNonterminal("B");
        NonterminalSymbol C = grammar.getNonterminal("C");
        NonterminalSymbol D = grammar.getNonterminal("D");
        NonterminalSymbol X = grammar.getNonterminal("X");
        TerminalSymbol a = new TerminalSymbol(TokenCharacter.get('a'));
        TerminalSymbol b = new TerminalSymbol(TokenCharacter.get('b'));
        TerminalSymbol c = new TerminalSymbol(TokenCharacter.get('c'));
        TerminalSymbol d = new TerminalSymbol(TokenCharacter.get('d'));

        grammar.addRule(S, A, B);
        grammar.addRule(A, a);
        grammar.addRule(B, b);

        /*
        grammar.addRule(S, A, X, d, X);
        grammar.addRule(A, a);
        grammar.addRule(B, b);
        grammar.addRule(C, c);
        grammar.addRule(D, d);
        grammar.addRule(X, B, C);

         */

        GearleyParser parser = grammar.getParser(options, S);
        GearleyResult result = parser.parse("ab");

        Assertions.assertTrue(result.succeeded());
    }

    @Test
    public void testGrammar1() {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType("GLL");

        SourceGrammar grammar = new SourceGrammar();

        NonterminalSymbol S = grammar.getNonterminal("S");
        TerminalSymbol a = new TerminalSymbol(TokenCharacter.get('a'));
        TerminalSymbol d = new TerminalSymbol(TokenCharacter.get('d'));

        grammar.addRule(S, d);
        grammar.addRule(S, S, a);

        GearleyParser parser = grammar.getParser(options, S);
        GearleyResult result = parser.parse("da");

        Assertions.assertTrue(result.succeeded());
    }

    @Test
    public void testGrammar2() {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType("GLL");

        SourceGrammar grammar = new SourceGrammar();

        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol A = grammar.getNonterminal("A");
        NonterminalSymbol B = grammar.getNonterminal("B");
        NonterminalSymbol C = grammar.getNonterminal("C");
        NonterminalSymbol X = grammar.getNonterminal("X");
        NonterminalSymbol Y = grammar.getNonterminal("Y");
        NonterminalSymbol Z = grammar.getNonterminal("Z");
        TerminalSymbol a = new TerminalSymbol(TokenCharacter.get('a'));
        TerminalSymbol b = new TerminalSymbol(TokenCharacter.get('b'));

        grammar.addRule(S, A, C, a, A);
        grammar.addRule(S, A, B, a, X);
        grammar.addRule(A, a, A);
        grammar.addRule(A, a);
        grammar.addRule(B, b, B);
        grammar.addRule(B, b);
        grammar.addRule(C, b, C);
        grammar.addRule(C, b);

        grammar.addRule(X, Y);
        grammar.addRule(X, Z);

        grammar.addRule(Y, A);
        grammar.addRule(Y, B);

        grammar.addRule(Z, A);
        grammar.addRule(Z, B);
        grammar.addRule(Z, a);

        GearleyParser parser = grammar.getParser(options, S);
        GearleyResult result = parser.parse("abaa");

        Assertions.assertTrue(result.succeeded());
    }

    @Test
    public void testGrammar3() {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType("GLL");

        SourceGrammar grammar = new SourceGrammar();

        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol A = grammar.getNonterminal("A");
        NonterminalSymbol B = grammar.getNonterminal("B");
        NonterminalSymbol C = grammar.getNonterminal("C");
        NonterminalSymbol X = grammar.getNonterminal("X");
        NonterminalSymbol Y = grammar.getNonterminal("Y");
        NonterminalSymbol Z = grammar.getNonterminal("Z");
        TerminalSymbol a = new TerminalSymbol(TokenCharacter.get('a'));
        TerminalSymbol b = new TerminalSymbol(TokenCharacter.get('b'));

        grammar.addRule(S, A, C, a, A);
        grammar.addRule(S, A, B, a, X);
        grammar.addRule(A, a, A);
        grammar.addRule(A, a);
        grammar.addRule(B, b, B);
        grammar.addRule(B, b);
        grammar.addRule(C, b, C);
        grammar.addRule(C, b);

        grammar.addRule(X, Y);
        grammar.addRule(X, Z);

        grammar.addRule(Y, A);
        grammar.addRule(Y, B);

        grammar.addRule(Z, A);
        grammar.addRule(Z, B);
        grammar.addRule(Z, a);

        GearleyParser parser = grammar.getParser(options, S);
        GearleyResult result = parser.parse("abaa");

        Assertions.assertTrue(result.succeeded());
    }

    @Test
    public void testGrammar4() {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType("GLL");

        SourceGrammar grammar = new SourceGrammar();

        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol A = grammar.getNonterminal("A");
        NonterminalSymbol B = grammar.getNonterminal("B");
        TerminalSymbol a = new TerminalSymbol(TokenCharacter.get('a'));
        TerminalSymbol b = new TerminalSymbol(TokenCharacter.get('b'));
        TerminalSymbol c = new TerminalSymbol(TokenCharacter.get('c'));

        grammar.addRule(S, a, A, B);
        grammar.addRule(S, a, A, b);
        grammar.addRule(A, a);
        grammar.addRule(A, c);
        grammar.addRule(A);
        grammar.addRule(B, b);
        grammar.addRule(B, B, c);
        grammar.addRule(B);

        GearleyParser parser = grammar.getParser(options, S);
        GearleyResult result = parser.parse("aab");

        grammar.getParserOptions().getLogger().setDefaultLogLevel(99);
        StringTreeBuilder builder = new StringTreeBuilder();
        result.getArborist().getTree(builder);

        Assertions.assertTrue(result.succeeded());
    }

    @Test
    public void testGrammar5() {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType("GLL");

        SourceGrammar grammar = new SourceGrammar();

        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol A = grammar.getNonterminal("A");
        NonterminalSymbol B = grammar.getNonterminal("B");
        NonterminalSymbol C = grammar.getNonterminal("C");
        TerminalSymbol semi = new TerminalSymbol(TokenCharacter.get(';'));
        TerminalSymbol value = new TerminalSymbol(TokenRegex.get("[^;]"));

        grammar.addRule(S, A, semi, B, C, C);
        grammar.addRule(A, value);
        grammar.addRule(B, TerminalSymbol.ch('b'));
        grammar.addRule(B);
        grammar.addRule(C, value);

        GearleyParser parser = grammar.getParser(options, S);
        GearleyResult result = parser.parse("a;cc");

        Assertions.assertTrue(result.succeeded());
    }

    @Test
    public void testGrammar6() {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType("GLL");

        SourceGrammar grammar = new SourceGrammar();

        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol A = grammar.getNonterminal("A");
        NonterminalSymbol B = grammar.getNonterminal("B");
        NonterminalSymbol C = grammar.getNonterminal("C");
        TerminalSymbol a = new TerminalSymbol(TokenCharacter.get('a'));
        TerminalSymbol b = new TerminalSymbol(TokenCharacter.get('b'));
        TerminalSymbol c = new TerminalSymbol(TokenCharacter.get('c'));

        grammar.addRule(S, A, B, C);
        grammar.addRule(A, a, A);
        grammar.addRule(A, a);
        grammar.addRule(B, b, B);
        grammar.addRule(B, b);
        grammar.addRule(B);
        grammar.addRule(C, c, C);
        grammar.addRule(C, c);

        GearleyParser parser = grammar.getParser(options, S);
        GearleyResult result = parser.parse("ac");

        Assertions.assertTrue(result.succeeded());
    }

    @Test
    public void testGrammar7() {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType("GLL");

        SourceGrammar grammar = new SourceGrammar();

        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol A = grammar.getNonterminal("A");
        NonterminalSymbol B = grammar.getNonterminal("B");
        NonterminalSymbol C = grammar.getNonterminal("C");
        TerminalSymbol a = new TerminalSymbol(TokenCharacter.get('a'));
        TerminalSymbol b = new TerminalSymbol(TokenCharacter.get('b'));
        TerminalSymbol c = new TerminalSymbol(TokenCharacter.get('c'));

        grammar.addRule(S, A, B, C);
        grammar.addRule(A, a, A);
        grammar.addRule(A, a);
        grammar.addRule(B, b, B);
        grammar.addRule(B, b);
        grammar.addRule(B, C);
        grammar.addRule(B);
        grammar.addRule(C, c, C);
        grammar.addRule(C, c);
        grammar.addRule(C, B);

        GearleyParser parser = grammar.getParser(options, S);
        GearleyResult result = parser.parse("ac");

        Assertions.assertTrue(result.succeeded());
    }

    @Test
    public void testGrammar8() {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType("GLL");

        SourceGrammar grammar = new SourceGrammar();

        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol A = grammar.getNonterminal("A");
        NonterminalSymbol B = grammar.getNonterminal("B");
        NonterminalSymbol C = grammar.getNonterminal("C");
        NonterminalSymbol D = grammar.getNonterminal("D");
        NonterminalSymbol X = grammar.getNonterminal("X");
        TerminalSymbol a = new TerminalSymbol(TokenCharacter.get('a'));
        TerminalSymbol b = new TerminalSymbol(TokenCharacter.get('b'));
        TerminalSymbol c = new TerminalSymbol(TokenCharacter.get('c'));
        TerminalSymbol d = new TerminalSymbol(TokenCharacter.get('d'));
        TerminalSymbol x = new TerminalSymbol(TokenCharacter.get('x'));

        grammar.addRule(S, A, X, D);
        grammar.addRule(A, a, a, a);
        grammar.addRule(B, b, b);
        grammar.addRule(C, c);
        grammar.addRule(D, d, d, d);
        grammar.addRule(X, x, B, C, x);
        grammar.addRule(X, x, B, x);
        grammar.addRule(X, x, C, x);

        GearleyParser parser = grammar.getParser(options, S);
        GearleyResult result = parser.parse("aaaxbbcxddd");

        Assertions.assertTrue(result.succeeded());
    }

    @Test
    public void testGrammar9() {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType("GLL");

        SourceGrammar grammar = new SourceGrammar();

        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol block = grammar.getNonterminal("block");
        NonterminalSymbol rule = grammar.getNonterminal("rule");
        NonterminalSymbol opt_rule = grammar.getNonterminal("opt_rule");
        NonterminalSymbol opt = grammar.getNonterminal("opt");
        TerminalSymbol ob = new TerminalSymbol(TokenCharacter.get('{'));
        TerminalSymbol cb = new TerminalSymbol(TokenCharacter.get('}'));
        TerminalSymbol x = new TerminalSymbol(TokenCharacter.get('x'));

        grammar.addRule(S, block);
        grammar.addRule(block, ob, opt_rule, cb);
        grammar.addRule(rule, x);
        grammar.addRule(opt_rule, rule);
        grammar.addRule(opt_rule, opt);
        grammar.addRule(opt);

        GearleyParser parser = grammar.getParser(options, S);
        GearleyResult result = parser.parse("{}");
        Assertions.assertTrue(result.succeeded());
    }

    @Test
    public void testAttributes() {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType("GLL");

        SourceGrammar grammar = new SourceGrammar();

        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol A = grammar.getNonterminal("A");
        NonterminalSymbol B1 = grammar.getNonterminal("B1", new ParserAttribute("number", "one"));
        NonterminalSymbol B2 = grammar.getNonterminal("B2", new ParserAttribute("number", "two"));
        TerminalSymbol a = new TerminalSymbol(TokenCharacter.get('a'));
        TerminalSymbol b = new TerminalSymbol(TokenCharacter.get('b'));

        grammar.addRule(S, A, B1);
        grammar.addRule(S, A, B2);
        grammar.addRule(A, a);
        grammar.addRule(B1, b);
        grammar.addRule(B2, b);

        GearleyParser parser = grammar.getParser(options, S);
        GearleyResult result = parser.parse("ab");

        Assertions.assertTrue(result.succeeded());
    }

    @Test
    public void testFirstAndFollow() {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType("GLL");

        SourceGrammar grammar = new SourceGrammar();

        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol ixml = grammar.getNonterminal("ixml");
        NonterminalSymbol rule = grammar.getNonterminal("rule");
        TerminalSymbol namestart = new TerminalSymbol(TokenCharacterSet.inclusion(CharacterSet.range('A', 'Z')));
        TerminalSymbol namefollow = new TerminalSymbol(TokenCharacterSet.inclusion(CharacterSet.range('a', 'z')));
        NonterminalSymbol name = grammar.getNonterminal("name");
        NonterminalSymbol more = grammar.getNonterminal("more");

        grammar.addRule(S, ixml);
        grammar.addRule(ixml, rule);
        grammar.addRule(rule, name);
        grammar.addRule(name, namestart, more);
        grammar.addRule(more, namestart, more);
        grammar.addRule(more, namefollow, more);
        grammar.addRule(more);

        GearleyParser parser = grammar.getParser(options, S);
        GearleyResult result = parser.parse("Ab");

        Assertions.assertTrue(result.succeeded());
    }

    @Test
    public void testInsertionNT() {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType("GLL");

        SourceGrammar grammar = new SourceGrammar();

        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol A = grammar.getNonterminal("A");
        NonterminalSymbol B = grammar.getNonterminal("B");
        NonterminalSymbol I = grammar.getNonterminal("INSERTION", new ParserAttribute("insert", "text"));
        TerminalSymbol a = new TerminalSymbol(TokenCharacter.get('a'));
        TerminalSymbol b = new TerminalSymbol(TokenCharacter.get('b'));

        grammar.addRule(S, A, I, B);
        grammar.addRule(A, a);
        grammar.addRule(B, b);
        grammar.addRule(I);

        GearleyParser parser = grammar.getParser(options, S);
        GearleyResult result = parser.parse("ab");

        Assertions.assertTrue(result.succeeded());

        Arborist walker = Arborist.getArborist(result.getForest());
        GenericTreeBuilder builder = new GenericTreeBuilder();
        walker.getTree(builder);
        GenericTree tree = builder.getTree();

        GenericBranch s_insertion = (GenericBranch) tree.getChildren().get(1);

        Assertions.assertEquals(s_insertion.symbol, grammar.getNonterminal("INSERTION"));
        Assertions.assertEquals("text", s_insertion.getAttribute("insert", "failed"));
    }

    @Disabled
    public void parseUnicodeTest() {
        ParserOptions options = new ParserOptions(globalOptions);
        options.setParserType("GLL");

        SourceGrammar grammar = new SourceGrammar();

/*
S ⇒ UnicodeData
UnicodeData ⇒ record_pp_LF, LF
record ⇒ codepoint, ';', name, ';', category, ';', combining, ';', bidi, ';', decomposition, ';', decimal, ';', digit, ';', numeric, ';', mirrored, ';', name_1_0, ';', comment, ';', uppercase, ';', lowercase, ';', titlecase
LF ⇒ '\n'
bidi ⇒ value
category ⇒ value
codepoint ⇒ hexPlus
combining ⇒ value
comment ⇒ value
decimal ⇒ value
decomposition ⇒ value
digit ⇒ value
hexPlus ⇒ ["0"-"9";"A"-"F"], hex_opt
hex_opt ⇒
hex_opt ⇒ hex_rep
hex_rep ⇒ ["0"-"9";"A"-"F"], hex_opt
lowercase ⇒ value
mirrored ⇒ value
name ⇒ notSemiPlus
name_1_0 ⇒ value
notSemiPlus ⇒ ~[";"], optMoreNotSemi
notSemiRep ⇒ ~[";"]
notSemiRep ⇒ ~[";"], notSemiRep
notSemiStar ⇒
notSemiStar ⇒ notSemiRep
numeric ⇒ value
optMoreNotSemi ⇒
optMoreNotSemi ⇒ optNextNotSemi
optNextNotSemi ⇒ ~[";"], optMoreNotSemi
opt_LF_record ⇒
opt_LF_record ⇒ opt_record
opt_record ⇒ LF, record
opt_record ⇒ LF, record, opt_record
record_pp_LF ⇒ record
record_pp_LF ⇒ record, opt_LF_record
titlecase ⇒ value
uppercase ⇒ value
value ⇒
value ⇒ notSemiStar
*/

        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol UnicodeData = grammar.getNonterminal("UnicodeData");
        NonterminalSymbol bidi = grammar.getNonterminal("bidi");
        NonterminalSymbol category = grammar.getNonterminal("category");
        NonterminalSymbol codepoint = grammar.getNonterminal("codepoint");
        NonterminalSymbol combining = grammar.getNonterminal("combining");
        NonterminalSymbol comment = grammar.getNonterminal("comment");
        NonterminalSymbol decimal = grammar.getNonterminal("decimal");
        NonterminalSymbol decomposition = grammar.getNonterminal("decomposition");
        NonterminalSymbol digit = grammar.getNonterminal("digit");
        NonterminalSymbol hexPlus = grammar.getNonterminal("hexPlus");
        NonterminalSymbol hex_opt = grammar.getNonterminal("hex_opt");
        NonterminalSymbol hex_rep = grammar.getNonterminal("hex_rep");
        NonterminalSymbol lowercase = grammar.getNonterminal("lowercase");
        NonterminalSymbol mirrored = grammar.getNonterminal("mirrored");
        NonterminalSymbol name = grammar.getNonterminal("name");
        NonterminalSymbol name_1_0 = grammar.getNonterminal("name_1_0");
        NonterminalSymbol notSemiPlus = grammar.getNonterminal("notSemiPlus");
        NonterminalSymbol notSemiRep = grammar.getNonterminal("notSemiRep");
        NonterminalSymbol notSemiStar = grammar.getNonterminal("notSemiStar");
        NonterminalSymbol numeric = grammar.getNonterminal("numeric");
        NonterminalSymbol optMoreNotSemi = grammar.getNonterminal("optMoreNotSemi");
        NonterminalSymbol optNextNotSemi = grammar.getNonterminal("optNextNotSemi");
        NonterminalSymbol opt_LF_record = grammar.getNonterminal("opt_LF_record");
        NonterminalSymbol opt_record = grammar.getNonterminal("opt_record");
        NonterminalSymbol record = grammar.getNonterminal("record");
        NonterminalSymbol record_pp_LF = grammar.getNonterminal("record_pp_LF");
        NonterminalSymbol titlecase = grammar.getNonterminal("titlecase");
        NonterminalSymbol uppercase = grammar.getNonterminal("uppercase");
        NonterminalSymbol value = grammar.getNonterminal("value");

        TerminalSymbol LF = TerminalSymbol.ch('\n');
        TerminalSymbol semi = TerminalSymbol.ch(';');
        TerminalSymbol notsemi = TerminalSymbol.regex("[^;]");
        TerminalSymbol notsemix = TerminalSymbol.regex("[^;]*");
        TerminalSymbol hexdigit = TerminalSymbol.regex("[0-9,A-F]");

        // 0000;<control>;Cc;0;BN;;;;;N;NULL;;;;
        grammar.addRule(S, UnicodeData);
        grammar.addRule(UnicodeData, record_pp_LF, LF);
        grammar.addRule(record, codepoint, semi, name, semi, category, semi, combining, semi, bidi, semi,
                decomposition, semi, decimal, semi, digit, semi, numeric, semi, mirrored, semi, name_1_0,
                semi, comment, semi, uppercase, semi, lowercase, semi, titlecase);
        grammar.addRule(bidi, value);
        grammar.addRule(category, value);
        grammar.addRule(codepoint, hexPlus);
        grammar.addRule(combining, value);
        grammar.addRule(comment, value);
        grammar.addRule(decimal, value);
        grammar.addRule(decomposition, value);
        grammar.addRule(digit, value);
        grammar.addRule(hexPlus, hexdigit, hex_opt);
        grammar.addRule(hex_opt);
        grammar.addRule(hex_opt, hex_rep);
        grammar.addRule(hex_rep, hexdigit, hex_opt);
        grammar.addRule(lowercase, value);
        grammar.addRule(mirrored, value);
        grammar.addRule(name, notSemiPlus);
        grammar.addRule(name_1_0, value);
        grammar.addRule(notSemiPlus, notsemi, optMoreNotSemi);
        grammar.addRule(notSemiRep, notsemi);
        grammar.addRule(notSemiRep, notsemi, notSemiRep);
        grammar.addRule(notSemiStar);
        grammar.addRule(notSemiStar, notSemiRep);
        grammar.addRule(numeric, value);
        grammar.addRule(optMoreNotSemi);
        grammar.addRule(optMoreNotSemi, optNextNotSemi);
        grammar.addRule(optNextNotSemi, notsemi, optMoreNotSemi);
        grammar.addRule(opt_LF_record);
        grammar.addRule(opt_LF_record, opt_record);
        grammar.addRule(opt_record, LF, record);
        grammar.addRule(opt_record, LF, record, opt_record);
        //grammar.addRule(record_pp_LF, record);
        grammar.addRule(record_pp_LF, record, opt_LF_record);
        grammar.addRule(titlecase, value);
        grammar.addRule(uppercase, value);
        grammar.addRule(value);
        grammar.addRule(value, notSemiStar);

        try {
            GearleyParser gllParser = grammar.getParser(options, S);

            int lineCount = 0;

            //BufferedInputStream stream = new BufferedInputStream(Files.newInputStream(Paths.get("src/test/resources/SmallData.txt")));
            BufferedReader reader = new BufferedReader(new FileReader("src/test/resources/SmallData.txt"));
            StringBuilder sb = new StringBuilder();
            String line = reader.readLine();
            while (line != null) {
                sb.append(line).append("\n");
                lineCount++;
                line = reader.readLine();
            }

            long start = Calendar.getInstance().getTimeInMillis();

            GearleyResult gresult = gllParser.parse(sb.toString());

            long duration = Calendar.getInstance().getTimeInMillis() - start;
            double rate = (1.0 * lineCount) / duration;

            System.err.printf("%s: %dms (%5.2f lps)%n", gresult.succeeded(), duration, rate * 1000);
            Assertions.assertTrue(gresult.succeeded());
        } catch (IOException ex) {
            Assertions.fail();
        }
    }
}
