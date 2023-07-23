package org.nineml.coffeegrinder;

import org.junit.jupiter.api.Assertions;
import org.nineml.coffeegrinder.parser.ParseForest;
import org.nineml.coffeegrinder.parser.ParserOptions;
import org.nineml.coffeegrinder.trees.Arborist;
import org.nineml.coffeegrinder.trees.StringTreeBuilder;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

public class CoffeeGrinderTest {
    protected final ParserOptions globalOptions = new ParserOptions();

    protected void expectForestCount(ParseForest forest, int expected) {
        int count = 0;
        Arborist xxx = Arborist.getArborist(forest);
        while (xxx.hasMoreTrees()) {
            StringTreeBuilder builder = new StringTreeBuilder();
            xxx.getTree(builder);
            count++;
        }
        Assertions.assertEquals(expected, count);
    }

    protected void expectTrees(Arborist walker, List<String> trees) {
        HashMap<String, Integer> expected = new HashMap<>();
        for (String tree : trees) {
            if (expected.containsKey(tree)) {
                fail("Duplicate tree in expected list");
            }
            expected.put(tree, 0);
        }
        int count = 0;
        StringTreeBuilder builder = new StringTreeBuilder(true);
        while (walker.hasMoreTrees()) {
            walker.getTree(builder);
            String tree = builder.getTree();
            if (!expected.containsKey(tree)) {
                fail(String.format("Unexpected tree: %s", tree));
            }
            if (expected.get(tree) != 0) {
                fail(String.format("Duplicate tree: %s", tree));
            }
            count++;

            if (walker.forest.isInfinitelyAmbiguous() && count == expected.size()) {
                break;
            }
        }
        if (count < expected.size()) {
            fail(String.format("Expected %d trees, got %d", trees.size(), count));
        }
    }

    protected void showTrees(Arborist walker) {
        showTrees(walker, false, -1);
    }

    protected void showTrees(Arborist walker, int count) {
        showTrees(walker, false, count);
    }

    protected void showTrees(Arborist walker, boolean asList, int maxCount) {
        if (asList) {
            System.err.println("expectTrees(result.getArborist(), Arrays.asList(");
        }

        int count = 0;
        StringTreeBuilder builder = new StringTreeBuilder(true);
        while (walker.hasMoreTrees() && (maxCount < 0 || count < maxCount)) {
            walker.getTree(builder);
            String tree = builder.getTree();
            if (asList) {
                if (count > 0) {
                    System.err.println(",");
                }
                System.err.printf("\"%s\"", tree);
            } else {
                System.err.println(tree);
            }
            count++;
            if (count > 100) {
                fail("Unreasonable number of trees?");
            }
        }

        if (asList) {
            System.err.println("));");
        }
    }
}
