package org.nineml.examples;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.trees.Arborist;
import org.nineml.coffeegrinder.trees.StringTreeBuilder;

/** Examples of CoffeeGrinder usage.
 * <p>This class forms the basis of the examples in the documentation.</p>
 */
public class CoffeeGrinderExamples {
    /** Populate a source grammar with symbols and rules.
     * <p>This grammar unambiguously recognizes decimal integers (3, 5, -7),
     * floating point numbers (3.14, -74.2, 0.8), and numbers in
     * "scientific notation" (1.3E7, 2.4E-3, -1.6E4).</p>
     * @param grammar the grammar to update
     */
    public void constructNumberGrammar(SourceGrammar grammar) {
        // The grammar that follows recognizes decimal numbers
        NonterminalSymbol number = grammar.getNonterminal("number");
        NonterminalSymbol integer = grammar.getNonterminal("integer");
        NonterminalSymbol floatpt = grammar.getNonterminal("float");
        NonterminalSymbol scientific = grammar.getNonterminal("scientific");

        // A number is an integer, or a float, or scientific
        grammar.addRule(number, integer);
        grammar.addRule(number, floatpt);
        grammar.addRule(number, scientific);

        // We'll allow a leading plus or minus sign
        TerminalSymbol plus = TerminalSymbol.ch('+');
        TerminalSymbol minus = TerminalSymbol.ch('-');
        NonterminalSymbol sign = grammar.getNonterminal("sign");
        grammar.addRule(sign, plus);
        grammar.addRule(sign, minus);
        grammar.addRule(sign);

        // A digit is a 0 or 1 or 2 or ...
        NonterminalSymbol digit = grammar.getNonterminal("digit");
        for (char ch = '0'; ch <= '9'; ch++) {
            grammar.addRule(digit, TerminalSymbol.ch(ch));
        }

        // For convenience, we'll make a nonterminal for 1 or more digits
        NonterminalSymbol digits = grammar.getNonterminal("digits");
        grammar.addRule(digits, digit);
        grammar.addRule(digits, digit, digits);

        // Now we can define our numbers. An integer is digits with a sign
        grammar.addRule(integer, sign, digits);

        // A floating point number must
        // have a '.' and must have digits on both sides.
        TerminalSymbol point = TerminalSymbol.ch('.');
        grammar.addRule(floatpt, sign, digits, point, digits);

        // Numbers with exponents can be either integers or floats
        TerminalSymbol exp = TerminalSymbol.ch('E');
        grammar.addRule(scientific, integer, exp, digits);
        grammar.addRule(scientific, floatpt, exp, digits);
    }

    public String parseNumber(String number) {
        ParserOptions options = new ParserOptions();
        SourceGrammar grammar = new SourceGrammar(options);

        constructNumberGrammar(grammar);

        NonterminalSymbol startSymbol = grammar.getNonterminal("number");
        ParserGrammar pgrammar = grammar.getCompiledGrammar(startSymbol);

        GearleyParser parser = pgrammar.getParser();
        GearleyResult result = parser.parse(number);

        if (result.succeeded()) {
            StringTreeBuilder builder = new StringTreeBuilder();
            ParseForest forest = result.getForest();
            Arborist.getArborist(forest).getTree(builder);
            String tree = builder.getTree();
            return String.format("%s is a number: %s", number, tree);
        } else {
            return String.format("%s is not a number", number);
        }
    }

    @Test
    public void parseInteger() {
        String result = parseNumber("42");
        System.out.println(result);
        Assertions.assertTrue(result.startsWith("42 is a number"));
    }

    @Test
    public void parseFloat() {
        String result = parseNumber("3.14");
        System.out.println(result);
        Assertions.assertTrue(result.startsWith("3.14 is a number"));
    }

    @Test
    public void parseScientific() {
        String result = parseNumber("1.0E6");
        System.out.println(result);
        Assertions.assertTrue(result.startsWith("1.0E6 is a number"));
    }

    @Test
    public void parse1dot2dot3() {
        String result = parseNumber("1.2.3");
        System.out.println(result);
        Assertions.assertTrue(result.startsWith("1.2.3 is not a number"));
    }

    /** Populate a source grammar with symbols and rules.
     * <p>This grammar recognizes US dates (m/d/year) or EU dates (d/m/year).
     * @param grammar the grammar to update
     */
    public void constructDateGrammar(SourceGrammar grammar) {
        // The grammar that follows recognizes m/d/y or d/m/y dates:
        // "US dates" or "EU dates"
        NonterminalSymbol date = grammar.getNonterminal("date");
        NonterminalSymbol usdate = grammar.getNonterminal("usdate");
        NonterminalSymbol eudate = grammar.getNonterminal("eudate");

        // Let's make some digits
        NonterminalSymbol digits0_9 = grammar.getNonterminal("digits0_9");
        NonterminalSymbol digits1_9 = grammar.getNonterminal("digits1_9");
        TerminalSymbol[] digits = new TerminalSymbol[10];
        for (char ch = '0'; ch <= '9'; ch++) {
            TerminalSymbol d = TerminalSymbol.ch(ch);
            digits[ch - '0'] = d;
            grammar.addRule(digits0_9, d);
            if (ch > '0') {
                grammar.addRule(digits1_9, d);
            }
        }

        // month = 0-9 or 0[1-9] or 10 or 11 or 12
        NonterminalSymbol month = grammar.getNonterminal("month");
        grammar.addRule(month, digits1_9);
        grammar.addRule(month, digits[0], digits1_9);
        grammar.addRule(month, digits[1], digits[0]);
        grammar.addRule(month, digits[1], digits[1]);
        grammar.addRule(month, digits[1], digits[2]);

        // day = 0-9 or 0[1-9] or 1[0-9] or 2[0-9] or 30 or 31
        // We're going to ignore the month/day co-constraints
        NonterminalSymbol day = grammar.getNonterminal("day");
        grammar.addRule(day, digits1_9);
        grammar.addRule(day, digits[0], digits1_9);
        grammar.addRule(day, digits[1], digits0_9);
        grammar.addRule(day, digits[2], digits0_9);
        grammar.addRule(day, digits[3], digits[0]);
        grammar.addRule(day, digits[3], digits[1]);

        // year = 19[0-9][0-9] or 2[0-9][0-9][0-9]
        NonterminalSymbol year = grammar.getNonterminal("year");
        grammar.addRule(year, digits[1], digits[9], digits0_9, digits0_9);
        grammar.addRule(year, digits[2], digits0_9, digits0_9, digits0_9);

        TerminalSymbol slash = TerminalSymbol.ch('/');

        grammar.addRule(date, usdate);
        grammar.addRule(date, eudate);

        grammar.addRule(eudate, day, slash, month, slash, year);
        grammar.addRule(usdate, month, slash, day, slash, year);
    }

    public String parseDate(String date) {
        ParserOptions options = new ParserOptions();
        SourceGrammar grammar = new SourceGrammar(options);

        constructDateGrammar(grammar);

        NonterminalSymbol startSymbol = grammar.getNonterminal("date");

        ParserGrammar pgrammar = grammar.getCompiledGrammar(startSymbol);

        GearleyParser parser = pgrammar.getParser();
        GearleyResult result = parser.parse(date);

        if (result.succeeded()) {
            StringTreeBuilder builder = new StringTreeBuilder();
            ParseForest forest = result.getForest();
            Arborist.getArborist(forest).getTree(builder);
            String tree = builder.getTree();

            int parseCount = forest.getParseTreeCount();
            if (parseCount > 1) {
                if (forest.isInfinitelyAmbiguous()) {
                    return String.format("%s is a date (in infinite ways): %s", date, tree);
                }
                return String.format("%s is a date (%d ways): %s", date, parseCount, tree);
            }
            return String.format("%s is a date: %s", date, tree);
        } else {
            return String.format("%s is not a date", date);
        }
    }

    @Test
    public void parseUsDate() {
        String result = parseDate("01/19/2038");
        System.out.println(result);
        Assertions.assertTrue(result.startsWith("01/19/2038 is a date"));
    }

    @Test
    public void parseEuDate() {
        String result = parseDate("19/01/2038");
        System.out.println(result);
        Assertions.assertTrue(result.startsWith("19/01/2038 is a date"));
    }

    @Test
    public void parseAmbiguousDate() {
        String result = parseDate("08/01/2038");
        System.out.println(result);
        Assertions.assertTrue(result.startsWith("08/01/2038 is a date (2 ways)"));
    }

}
