package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.exceptions.ForestException;
import org.nineml.coffeegrinder.trees.*;
import org.nineml.coffeegrinder.util.ParserAttribute;
import org.nineml.coffeegrinder.util.StopWatch;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static java.lang.Math.abs;

/**
 * An SPPF is a shared packed parse forest.
 * <p>The SPPF is a graph representation of all the (possibly infinite) parses that can be used
 * to recognize the input sequence as a sentence in the grammar.</p>
 */
public class ParseForest {
    public static final String logcategory = "Forest";
    protected int nextForestNodeId = 0;
    protected int nextFamilyId = 0;
    protected final ArrayList<ForestNode> graph = new ArrayList<>();
    protected final ArrayList<ForestNode> roots = new ArrayList<>();
    protected final HashSet<Integer> graphIds = new HashSet<>();
    protected final HashSet<Integer> rootIds = new HashSet<>();
    protected final ArrayList<ForestNode> ambiguousNodes = new ArrayList<>();
    protected final HashSet<Family> loops = new HashSet<>();

    protected final ParserOptions options;
    protected boolean ambiguous = false;
    protected boolean infinitelyAmbiguous = false;
    protected int parseTreeCount = 0;

    public ParseForest(ParserOptions options) {
        this.options = options;
    }

    /**
     * Is the grammar represented by this graph ambiguous?
     * <p>A grammar is ambiguous if there are more than two parses that will recognize the input.</p>
     *
     * @return true if the grammar is ambiguous
     */
    public boolean isAmbiguous() {
        return ambiguous;
    }

    /**
     * Is the grammar represented by this graph infinitely ambiguous?
     * <p>If the answer is "true", then the graph is infinitely ambiguous. If the graph is ambiguous
     * and the anwer is "false", then all that can be said is the single parse explored to check
     * ambiguity did not encounter infinite ambiguity. It is not an assertion that no unexplored
     * part of the graph contains a loop.</p>
     *
     * @return true if the parse forest is known to be infinitely ambiguous
     */
    public boolean isInfinitelyAmbiguous() {
        return infinitelyAmbiguous;
    }

    /**
     * How many parse trees are there in this forest?
     * <p>In an infinitely ambiguous graph, there are an infinite number of parse trees. However,
     * CoffeeGrinder will never follow the same edge twice when constructing a tree, it won't loop.
     * So the number of available trees is always a finite number.</p>
     *
     * @return the parse tree count
     */
    public int getParseTreeCount() {
        return parseTreeCount;
    }

    public List<ForestNode> getAmbiguousNodes() {
        return ambiguousNodes;
    }

    /**
     * How big is the graph?
     *
     * @return the number of nodes in the graph
     */
    public int size() {
        return graph.size();
    }

    /**
     * Get the nodes in the graph.
     *
     * @return the nodes in the graph.
     */
    public List<ForestNode> getNodes() {
        return graph;
    }

    private List<ForestNode> getRoots() {
        // When the graph is first constructed, there can be multiple roots. But
        // after we prune the forest, there can be only one.
        if (roots.size() > 1) {
            throw new IllegalStateException("Graph has more than one root node");
        }
        return roots;
    }

    public ForestNode getRoot() {
        return roots.size() > 0 ? getRoots().get(0) : null;
    }

    /**
     * Get the options for this forest.
     *
     * @return the options.
     */
    public ParserOptions getOptions() {
        return options;
    }

    /**
     * Serialize the graph as XML.
     *
     * @return an XML serialization as a string
     */
    public String serialize() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        serialize(ps);
        try {
            return baos.toString("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalArgumentException("Unexpected (i.e. impossible) unsupported encoding exception", ex);
        }
    }

    /**
     * Serialize the graph as XML.
     *
     * @param stream the stream on which to write the XML serialization
     */
    public void serialize(PrintStream stream) {
        stream.printf("<sppf parser='%s'>%n", options.getParserType());
        int count = 0;
        for (ForestNode node : graph) {
            stream.printf("  <u%d id='%s'", count, id(node.id));

            String symstr = null;
            String stastr = null;

            if (node.symbol != null) {
                symstr = node.symbol.toString().replace("&", "&amp;");
                symstr = symstr.replace("<", "&lt;").replace("\"", "&quot;");
            }

            if (node.state != null) {
                if (node.families.size() != 1 || node.families.get(0).v != null) {
                    stastr = node.state.toString().replace("&", "&amp;");
                    stastr = stastr.replace("<", "&lt;").replace("\"", "&quot;");
                }
            }

            StringBuilder attrs = new StringBuilder();
            if (node.symbol == null) {
                assert node.state != null;
                stream.printf(" label=\"%s\"", stastr);
                stream.print(" type='state'");
            } else {
                if (stastr == null) {
                    stream.printf(" label=\"%s\"", symstr);
                } else {
                    stream.printf(" label=\"%s\" state=\"%s\"", symstr, stastr);
                }
                if (node.symbol instanceof TerminalSymbol) {
                    stream.print(" type='terminal'");
                } else {
                    stream.print(" type='nonterminal'");
                }

                Collection<ParserAttribute> pattrs;
                if (node.symbol instanceof TerminalSymbol) {
                    pattrs = ((TerminalSymbol) node.symbol).getToken().getAttributes();
                } else {
                    pattrs = node.symbol.getAttributes();
                }
                for (ParserAttribute attr : pattrs) {
                    attrs.append("    <attr name=\"").append(attr.getName());
                    attrs.append("\" value=\"").append(attr.getValue()).append("\"/>\n");
                }
            }
            stream.printf(" leftExtent='%d' rightExtent='%d'", node.leftExtent, node.rightExtent);
            stream.printf(" priority='%d'", node.priority);
            if (!node.families.isEmpty()) {
                stream.printf(" trees='%d'", node.families.size());
            }

            if (node.families.isEmpty()) {
                if ("".contentEquals(attrs)) {
                    stream.println("/>");
                } else {
                    stream.println(">");
                    stream.print(attrs);
                    stream.printf("  </u%d>\n", count);
                }
            } else {
                stream.println(">");
                for (Family family : node.families) {
                    if (family.w != null) {
                        if (family.v != null) {
                            stream.printf("    <pair priority='%d'>%n", family.getPriority());
                            stream.printf("      <link target='%s'/>%n", id(family.w.id));
                            stream.printf("      <link target='%s'/>%n", id(family.v.id));
                            stream.println("    </pair>");
                        } else {
                            stream.printf("      <link target='%s'/>%n", id(family.w.id));
                        }
                    } else {
                        if (family.v == null) {
                            stream.println("    <epsilon/>");
                        } else {
                            stream.printf("    <link target='%s'/>\n", id(family.v.id));
                        }
                    }
                }
                stream.printf("  </u%d>\n", count);
            }
            count++;
        }
        stream.println("</sppf>");
    }

    /**
     * Serialize the graph as XML.
     * <p>This method attempts to write the XML to a file.</p>
     *
     * @param filename the name of the file
     * @throws ForestException if a error occurs attempt to write to the file
     */
    public void serialize(String filename) {
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            PrintStream stream = new PrintStream(fos);
            serialize(stream);
            stream.close();
            fos.close();
        } catch (IOException ex) {
            throw ForestException.ioError(filename, ex);
        }
    }

    protected ForestNode createNode(Symbol symbol, int j, int i) {
        ForestNode node = new ForestNode(this, symbol, j, i);
        graph.add(node);
        graphIds.add(node.id);
        return node;
    }

    protected ForestNode createNode(Symbol symbol, State state, int j, int i) {
        ForestNode node = new ForestNode(this, symbol, state, j, i);
        graph.add(node);
        graphIds.add(node.id);
        return node;
    }

    protected ForestNode createNode(State state, int j, int i) {
        ForestNode node = new ForestNode(this, state, j, i);
        graph.add(node);
        graphIds.add(node.id);
        return node;
    }

    protected void root(ForestNode w) {
        if (rootIds.contains(w.id)) {
            return;
        }

        if (graphIds.contains(w.id)) {
            roots.add(w);
            rootIds.add(w.id);
            return;
        }

        throw ForestException.noSuchNode(w.toString());
    }

    protected void clearRoots() {
        roots.clear();
        rootIds.clear();
    }

    protected void prune() {
        StopWatch timer = new StopWatch();

        for (ForestNode root : roots) {
            root.reach(roots.size());
        }

        int count = 0;
        ArrayList<ForestNode> prunedGraph = new ArrayList<>();
        HashSet<Integer> prunedMap = new HashSet<>();
        for (ForestNode node : graph) {
            if (node.reachable > 0) {
                prunedGraph.add(node);
                prunedMap.add(node.id);
            } else {
                count++;
            }
        }

        graph.clear();
        graph.addAll(prunedGraph);
        graphIds.clear();
        graphIds.addAll(prunedMap);

        timer.stop();
        options.getLogger().debug(logcategory, "Pruned %,d unreachable nodes from graph in %,dms; %,d remain", count, timer.duration(), graph.size());
    }

    private String id(int code) {
        // Avoid "-" in hash codes. Because it confuses graphviz, basically.
        if (code < 0) {
            return "id_" + abs(code);
        } else {
            return "id" + code;
        }
    }

    protected void rollback(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Cannot rollback to less than zero nodes");
        }
        while (graph.size() > size) {
            ForestNode node = graph.remove(graph.size() - 1);
            graphIds.remove(node.id);
            rootIds.remove(node.id);
        }
    }
}
