package org.nineml.coffeegrinder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.trees.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoopTest extends CoffeeGrinderTest {
    @Test
    public void loop0() {
        ParserOptions options = new ParserOptions(globalOptions);
        SourceGrammar grammar = new SourceGrammar(new ParserOptions());

            /*
            S = A, C .
            A = 'a', 'b' .
            C = C .
            C = 'c' .
             */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _C = grammar.getNonterminal("C");

        grammar.addRule(_S, _A, _C);
        grammar.addRule(_A, TerminalSymbol.ch('a'), TerminalSymbol.ch('b'));
        grammar.addRule(_C, _C);
        grammar.addRule(_C, TerminalSymbol.ch('c'));

        try {
            GearleyParser parser = grammar.getParser(options, _S);
            GearleyResult result = parser.parse("abc");

            SequentialAxe sel = new SequentialAxe();
            Arborist walker = Arborist.getArborist(result.getForest(), sel);

            expectTrees(Arborist.getArborist(result.getForest()), Arrays.asList(
                    "<S><A>ab</A><C>c</C></S>",
                    "<S><A>ab</A><C><C>c</C></C></S>"));
        } catch (Exception ex) {
            Assertions.fail();
        }
    }

    @Test
    public void loop1() {
        ParserOptions options = new ParserOptions(globalOptions);
        SourceGrammar grammar = new SourceGrammar(new ParserOptions());

            /*
            S = B | C .
            B = D .
            C = D .
            D = S .
            D = 't' .
             */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _B = grammar.getNonterminal("B");
        NonterminalSymbol _C = grammar.getNonterminal("C");
        NonterminalSymbol _D = grammar.getNonterminal("D");

        TerminalSymbol _t = TerminalSymbol.ch('t');

        grammar.addRule(_S, _B);
        grammar.addRule(_S, _C);
        grammar.addRule(_B, _D);
        grammar.addRule(_C, _D);
        grammar.addRule(_D, _S);
        grammar.addRule(_D, _t);

        try {
            GearleyParser parser = grammar.getParser(options, _S);
            GearleyResult result = parser.parse("t");

            Axe sel = new SequentialAxe();
            Arborist walker = Arborist.getArborist(result.getForest(), sel);

            expectTrees(Arborist.getArborist(result.getForest()), Arrays.asList(
                    "<S><B><D>t</D></B></S>",
                    "<S><C><D>t</D></C></S>",
                    "<S><B><D><S><B><D>t</D></B></S></D></B></S>",
                    "<S><B><D><S><C><D>t</D></C></S></D></B></S>",
                    "<S><C><D><S><B><D>t</D></B></S></D></C></S>",
                    "<S><C><D><S><C><D>t</D></C></S></D></C></S>",
                    "<S><B><D><S><B><D><S><B><D>t</D></B></S></D></B></S></D></B></S>",
                    "<S><B><D><S><B><D><S><C><D>t</D></C></S></D></B></S></D></B></S>",
                    "<S><B><D><S><C><D><S><B><D>t</D></B></S></D></C></S></D></B></S>",
                    "<S><B><D><S><C><D><S><C><D>t</D></C></S></D></C></S></D></B></S>"));
        } catch (Exception ex) {
            Assertions.fail();
        }
    }

    @Test
    public void loop1NoLoops() {
        ParserOptions options = new ParserOptions(globalOptions);
        SourceGrammar grammar = new SourceGrammar(new ParserOptions());

            /*
            S = B | C .
            B = D .
            C = D .
            D = S .
            D = 't' .
             */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _B = grammar.getNonterminal("B");
        NonterminalSymbol _C = grammar.getNonterminal("C");
        NonterminalSymbol _D = grammar.getNonterminal("D");

        TerminalSymbol _t = TerminalSymbol.ch('t');

        grammar.addRule(_S, _B);
        grammar.addRule(_S, _C);
        grammar.addRule(_B, _D);
        grammar.addRule(_C, _D);
        grammar.addRule(_D, _S);
        grammar.addRule(_D, _t);

        try {
            GearleyParser parser = grammar.getParser(options, _S);
            GearleyResult result = parser.parse("t");

            //result.getForest().serialize("loop1no.xml");

            Axe sel = new SequentialAxe(true);
            Arborist walker = Arborist.getArborist(result.getForest(), sel);

            expectTrees(walker, Arrays.asList(
                    "<S><B><D>t</D></B></S>",
                    "<S><C><D>t</D></C></S>"));
        } catch (Exception ex) {
            Assertions.fail();
        }
    }

    @Test
    public void loop2() {
        ParserOptions options = new ParserOptions(globalOptions);
        SourceGrammar grammar = new SourceGrammar(new ParserOptions());

        /*
        S = A, B, C .
        A = 'a' .
        B = B | 'b' .
        C = 'c' .
        */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B = grammar.getNonterminal("B");
        NonterminalSymbol _C = grammar.getNonterminal("C");

        grammar.addRule(_S, _A, _B, _C);
        grammar.addRule(_A, TerminalSymbol.ch('a'));
        grammar.addRule(_B, _B);
        grammar.addRule(_B, TerminalSymbol.ch('b'));
        grammar.addRule(_C, TerminalSymbol.ch('c'));

        try {
            GearleyParser parser = grammar.getParser(options, _S);
            GearleyResult result = parser.parse("abc");

            expectTrees(result.getArborist(), Arrays.asList(
                    "<S><A>a</A><B>b</B><C>c</C></S>",
                    "<S><A>a</A><B><B>b</B></B><C>c</C></S>",
                    "<S><A>a</A><B><B><B>b</B></B></B><C>c</C></S>",
                    "<S><A>a</A><B><B><B><B>b</B></B></B></B><C>c</C></S>",
                    "<S><A>a</A><B><B><B><B><B>b</B></B></B></B></B><C>c</C></S>",
                    "<S><A>a</A><B><B><B><B><B><B>b</B></B></B></B></B></B><C>c</C></S>",
                    "<S><A>a</A><B><B><B><B><B><B><B>b</B></B></B></B></B></B></B><C>c</C></S>",
                    "<S><A>a</A><B><B><B><B><B><B><B><B>b</B></B></B></B></B></B></B></B><C>c</C></S>",
                    "<S><A>a</A><B><B><B><B><B><B><B><B><B>b</B></B></B></B></B></B></B></B></B><C>c</C></S>",
                    "<S><A>a</A><B><B><B><B><B><B><B><B><B><B>b</B></B></B></B></B></B></B></B></B></B><C>c</C></S>"));
        } catch (Exception ex) {
            Assertions.fail();
        }
    }

    @Test
    public void longLoop() {
        SourceGrammar grammar = new SourceGrammar(new ParserOptions());

            /*
            S = X, A, Z.
            X = 'x'.
            Z = 'z'.
            A = A | 'y'.
             */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _X = grammar.getNonterminal("X");
        NonterminalSymbol _Z = grammar.getNonterminal("Z");

        TerminalSymbol _x = TerminalSymbol.ch('x');
        TerminalSymbol _y = TerminalSymbol.ch('y');
        TerminalSymbol _z = TerminalSymbol.ch('z');

        grammar.addRule(_S, _X, _A, _Z);
        grammar.addRule(_X, _x);
        grammar.addRule(_Z, _z);
        grammar.addRule(_A, _A);
        grammar.addRule(_A, _y);

        try {
            GearleyParser parser = grammar.getParser(globalOptions, _S);
            GearleyResult result = parser.parse("xyz");

            Axe loopingTreeSelector = new LoopingTreeSelector();
            StringTreeBuilder builder = new StringTreeBuilder();
            Arborist walker = Arborist.getArborist(result.getForest(), loopingTreeSelector);
            walker.getTree(builder);

            Assertions.assertTrue(result.getForest().isAmbiguous());
            Assertions.assertTrue(result.getForest().isInfinitelyAmbiguous());
            Assertions.assertEquals(2, result.getForest().getParseTreeCount());

            Assertions.assertEquals("<S><X>x</X><A><A><A><A><A><A><A><A><A><A>y</A></A></A></A></A></A></A></A></A></A><Z>z</Z></S>", builder.getTree());
        } catch (Exception ex) {
            Assertions.fail();
        }
    }

    private static class LoopingTreeSelector implements Axe {
        @Override
        public boolean isSpecialist() {
            return false;
        }

        @Override
        public List<Family> select(ParseTree tree, ForestNode forestNode, int nodeCount, List<Family> choices) {
            final int choice_A;
            final int choice_y;
            if (choices.get(0).getRightNode().symbol instanceof NonterminalSymbol) {
                choice_A = 0;
                choice_y = 1;
            } else {
                choice_A = 1;
                choice_y = 0;
            }

            ArrayList<Family> selected = new ArrayList<>();
            if (nodeCount < 9) {
                selected.add(choices.get(choice_A));
            } else {
                selected.add(choices.get(choice_y));
            }

            return selected;
        }

        @Override
        public boolean wasAmbiguousSelection() {
            return false;
        }

        @Override
        public void forArborist(Arborist arborist) {
            // nop
        }
    }

}
