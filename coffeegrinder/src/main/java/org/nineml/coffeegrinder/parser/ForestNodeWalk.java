package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.util.ParserAttribute;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class ForestNodeWalk {
    private final String logcategory;
    private final ParseForest graph;
    private final ForestNode root;

    protected ForestNodeWalk(ParseForest graph, ForestNode root) {
        this.graph = graph;
        this.root = root;
        this.logcategory = ForestNode.logcategory;
    }

    // Loop-based treewalk to avoid stack overflow.
    protected void treewalk() {
        Stack<ForestNode> pending = new Stack<>();
        Stack<ForestNode> parents = new Stack<>();
        Stack<TreeTraversal> traversal = new Stack<>();

        pending.push(root);
        while (!pending.isEmpty()) {
            ForestNode current = pending.pop();
            ForestNode parent = parents.isEmpty() ? null :parents.pop();
            if (current == null) {
                continue;
            }

            traversal.push(new TreeTraversal(parent, current));
            if (!current.families.isEmpty()) {
                if (current.families.size() > 1) {
                    graph.ambiguous = true;
                    if (!graph.ambiguousNodes.contains(current)) {
                        graph.ambiguousNodes.add(current);
                    }
                }

                for (Family family : current.families) {
                    parents.push(current);
                    parents.push(current);

                    ForestNode left = family.getLeftNode();
                    ForestNode right = family.getRightNode();
                    if (parents.contains(left) || parents.contains(right)) {
                        graph.ambiguous = true;
                        graph.infinitelyAmbiguous = true;
                        graph.loops.add(family);
                        if (parents.contains(left)) {
                            left = null;
                        }
                        if (parents.contains(right)) {
                            right = null;
                        }
                    }

                    pending.push(left);
                    pending.push(right);
                }
            }
        }

        int lastTotal = 0;
        Map<ForestNode,Integer> choices = new HashMap<>();
        while (!traversal.isEmpty()) {
            TreeTraversal top = traversal.pop();

            int total = 0;
            for (Family family : top.node.families) {
                // If the left or right nodes aren't in choices, then there's a loop in
                // the graph and the counts are meaningless anyway...
                int count = 1;
                ForestNode left = family.getLeftNode();
                if (choices.containsKey(left)) {
                    count = count * Math.max(1, choices.get(left));
                }
                ForestNode right = family.getRightNode();
                if (choices.containsKey(right)) {
                    count = count * Math.max(1, choices.get(right));
                }
                total += count;
            }

            lastTotal = total;
            choices.put(top.node, total);
            process_node(top.parent, top.node);
        }

        graph.parseTreeCount = lastTotal;
    }

    private void process_node(ForestNode parent, ForestNode current) {
        current.reachable++;
        int value = 0;
        boolean assignedPriority = false;
        if (current.getSymbol() != null) {
            ParserAttribute pattr = current.getSymbol().getAttribute(ForestNode.PRIORITY_ATTRIBUTE);
            if (pattr != null) {
                try {
                    value = Integer.parseInt(pattr.getValue());
                    assignedPriority = true;

                    if (value < 0) {
                        graph.getOptions().getLogger().error(logcategory, "Invalid priority: %s (must be non-negative)",
                                pattr.getValue());
                        value = 0;
                    }
                } catch (NumberFormatException ex) {
                    graph.getOptions().getLogger().error(logcategory, "Invalid priority: %s (must be an integer)",
                            pattr.getValue());
                }
            }
        }

        if (assignedPriority) {
            if ("max".equals(current.getPriorityStyle())) {
                current.priority = value;
            } else {
                current.priority += value;
            }
        }

        if (parent != null) {
            if ("max".equals(current.getPriorityStyle())) {
                parent.priority = Math.max(value, Math.max(parent.priority, current.priority));
            } else {
                parent.priority += current.priority;
            }
        }
    }

    private static class TreeTraversal {
        public final ForestNode parent;
        public final ForestNode node;
        public TreeTraversal(ForestNode parent, ForestNode node) {
            this.node = node;
            this.parent = parent;
        }
    }
}
