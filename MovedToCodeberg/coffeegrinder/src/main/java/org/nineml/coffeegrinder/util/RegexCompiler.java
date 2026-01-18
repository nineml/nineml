package org.nineml.coffeegrinder.util;

import org.nineml.coffeegrinder.parser.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class RegexCompiler {
    private final List<Rule> sourceRules;
    private Graph dfa = null;

    public RegexCompiler(List<Rule> sourceRules) {
        this.sourceRules = sourceRules;
    }

    public String compile(NonterminalSymbol startSymbol) {
        if (startSymbol == null) {
            throw new NullPointerException("Cannot compile starting with null");
        }

        try {
            dfa = new Graph();
            Node start = dfa.getNode(startSymbol);
            dfa.addEdge(dfa.start, start, TerminalSymbol.EPSILON);
            expand(start, startSymbol);
            dfa.makeFinal();
            if (dfa.finish == null) {
                throw new ExpansionException("Failed to compute final state");
            }
            dfa.collapse();
            return "bang";
        } catch (ExpansionException ex) {
            System.err.println(ex.getMessage());
        }

        return null;
    }

    private void expand(Node start, NonterminalSymbol symbol) {
        boolean found = false;
        for (Rule rule : sourceRules) {
            if (symbol.equals(rule.symbol)) {
                found = true;
                expand(start, rule.rhs);
            }
        }
        if (!found) {
            throw new ExpansionException("No rule for " + symbol);
        }
    }

    private void expand(Node start, RightHandSide rhs) {
        ArrayList<NonterminalSymbol> todo = new ArrayList<>();

        if (rhs.isEmpty()) {
            dfa.addEdge(start, dfa.getNode(), TerminalSymbol.EPSILON);
            return;
        }

        Node current = start;
        for (Symbol symbol : rhs.symbols) {
            if (symbol instanceof TerminalSymbol) {
                Node next = dfa.getNode();
                dfa.addEdge(current, next, (TerminalSymbol) symbol);
                current = next;
            } else {
                if (!dfa.contains((NonterminalSymbol) symbol)) {
                    todo.add((NonterminalSymbol) symbol);
                }
                Node next = dfa.getNode((NonterminalSymbol) symbol);
                dfa.addEdge(current, next, TerminalSymbol.EPSILON);
                current = next;
            }
        }

        for (NonterminalSymbol symbol : todo) {
            expand(dfa.getNode(symbol), symbol);
        }
    }


    private static class ExpansionException extends RuntimeException {
        public ExpansionException(String message) {
            super(message);
        }
    };

    private static class Graph {
        public final Node start;
        public final HashSet<Node> nodes;
        public final HashMap<NonterminalSymbol, Node> nonterminalNodes;
        private FinalState finish = null;
        public Graph() {
            start = new StartState();
            nodes = new HashSet<>();
            nodes.add(start);
            nonterminalNodes = new HashMap<>();
        }
        public boolean contains(NonterminalSymbol symbol) {
            return nonterminalNodes.containsKey(symbol);
        }
        public Node getNode() {
            Node node = new Node();
            nodes.add(node);
            return node;
        }
        public Node getNode(NonterminalSymbol symbol) {
            if (!nonterminalNodes.containsKey(symbol)) {
                Node node = new Node(symbol);
                nodes.add(node);
                nonterminalNodes.put(symbol, node);
            }
            return nonterminalNodes.get(symbol);
        }
        public FinalState getFinalState() {
            if (finish == null) {
                finish = new FinalState();
                nodes.add(finish);
            }
            return finish;
        }
        public void addEdge(Node from, Node to, TerminalSymbol label) {
            Edge edge = new Edge(from, to, label);
            from.edges.add(edge);
        }
        public void makeFinal() {
            ArrayList<Node> terminalStates = new ArrayList<>();
            for (Node node : nodes) {
                if (node.edges.isEmpty()) {
                    terminalStates.add(node);
                }
            }
            for (Node node : terminalStates) {
                addEdge(node, getFinalState(), TerminalSymbol.EPSILON);
            }
        }
        public void collapse() {
            makeEdgesUnique();

            HashSet<Node> remaining = new HashSet<>();
            boolean done = false;
            while (!done) {
                remaining.clear();
                done = true;
                HashSet<Node> iter = new HashSet<>(nodes);
                HashSet<Node> fixup = new HashSet<>();
                for (Node node : iter) {
                    if (node == start || node == finish) {
                        remaining.add(node);
                    } else {
                        Edge epsilonEdge = null;
                        ArrayList<Edge> saveEdges = new ArrayList<>();
                        for (Edge edge : node.edges) {
                            if (epsilonEdge == null
                                && edge.label.equals(TerminalSymbol.EPSILON)
                                && !edge.to.equals(finish)) {
                                epsilonEdge = edge;
                            } else {
                                saveEdges.add(edge);
                            }
                        }
                        if (epsilonEdge != null) {
                            done = false;
                            for (Edge edge : saveEdges) {
                                addEdge(epsilonEdge.to, edge.to, edge.label);
                            }
                            relink(node, epsilonEdge.to);
                            fixup.add(epsilonEdge.to);
                        } else {
                            remaining.add(node);
                        }
                    }
                }
                nodes.clear();
                nodes.addAll(remaining);
                for (Node tofix : fixup) {
                    makeEdgesUnique(tofix);
                }
            }
/*
            HashSet<Node> remaining = new HashSet<>();
            for (Node node : nodes) {
                if (node != start && node != finish
                        && node.edges.size() == 1 && node.edges.get(0).label == TerminalSymbol.EPSILON) {
                    relink(node, node.edges.get(0).to);
                } else {
                    remaining.add(node);
                }
            }
            nodes.clear();
            nodes.addAll(remaining);
*/
        }
        private void relink(Node from, Node to) {
            for (Node node : nodes) {
                ArrayList<Edge> newEdges = new ArrayList<>();
                for (Edge edge : node.edges) {
                    if (from.equals(edge.to)) {
                        newEdges.add(new Edge(edge.from, to, edge.label));
                    } else {
                        newEdges.add(edge);
                    }
                }
                node.edges.clear();
                node.edges.addAll(newEdges);
            }
        }

        private void makeEdgesUnique() {
            HashSet<Node> initial = new HashSet<>(nodes);
            for (Node node : initial) {
                makeEdgesUnique(node);
            }
        }

        private void makeEdgesUnique(Node node) {
            if (node.edges.size() < 2) {
                return;
            }

            ArrayList<Edge> newEdges = new ArrayList<>();
            HashSet<TerminalSymbol> labels = new HashSet<>();
            for (Edge edge : node.edges) {
                if (edge.label.equals(TerminalSymbol.EPSILON)) {
                    newEdges.add(edge);
                } else {
                    labels.add(edge.label);
                }
            }

            if (labels.size() == node.edges.size()) {
                // They're all unique
                return;
            }

            for (TerminalSymbol label : labels) {
                ArrayList<Edge> dupEdges = new ArrayList<>();
                for (Edge edge : node.edges) {
                    if (label.equals(edge.label)) {
                        boolean redundant = false;
                        for (Edge dedge : dupEdges) {
                            redundant = edge.to.equals(dedge.to);
                            if (redundant) {
                                break;
                            }
                        }
                        if (!redundant) {
                            dupEdges.add(edge);
                        }
                    }
                }
                if (dupEdges.size() == 1) {
                    newEdges.addAll(dupEdges);
                } else {
                    ArrayList<Node> discard = new ArrayList<>();
                    Node intermediate = getNode();
                    newEdges.add(new Edge(node, intermediate, label));
                    for (Edge dedge : dupEdges) {
                        discard.add(dedge.to);
                        for (Edge transitive : dedge.to.edges) {
                            addEdge(intermediate, transitive.to, transitive.label);
                        }
                    }
                    for (Node dnode : discard) {
                        nodes.remove(dnode);
                    }
                }
            }

            node.edges.clear();
            node.edges.addAll(newEdges);
        }
    }

    private static class Node {
        private static int nextId = 1;
        public final int id;
        public final NonterminalSymbol label;
        public final ArrayList<Edge> edges;
        public Node() {
            id = nextId++;
            label = null;
            edges = new ArrayList<>();
        }
        public Node(NonterminalSymbol symbol) {
            id = nextId++;
            label = symbol;
            edges = new ArrayList<>();
        }
        @Override
        public String toString() {
            if (label == null) {
                return "[" + id + "]";
            } else {
                return label.toString();
            }
        }
    }

    private static class StartState extends Node {
        @Override
        public String toString() {
            return "⭘";
        }
    }

    private static class FinalState extends Node {
        @Override
        public String toString() {
            return "⦿";
        }
    }

    private static class Edge {
        public final TerminalSymbol label;
        public final Node from;
        public final Node to;
        public Edge(Node from, Node to, TerminalSymbol label) {
            this.label = label;
            this.from = from;
            this.to = to;
        }
        @Override
        public String toString() {
            return from.toString() + "-" + label + "->" + to;
        }

    }


}
