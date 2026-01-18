package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.exceptions.ParseException;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.tokens.TokenCharacter;
import org.nineml.coffeegrinder.tokens.TokenRegex;
import org.nineml.coffeegrinder.util.ParserAttribute;
import org.nineml.coffeegrinder.util.StopWatch;

import java.util.*;

/** The Earley parser.
 *
 * <p>The Earley parser compares an input sequence against a grammar and determines if the input
 * is a sentence in the grammar.</p>
 * <p>This is a fairly literal implementation of the parser in §5 of
 * <a href="https://www.sciencedirect.com/science/article/pii/S1571066108001497?via%3Dihub">SPPF-Style
 * Parsing From Earley Recognisers</a>.</p>
 */
public class EarleyParser implements GearleyParser {
    public static final String logcategory = "Parser";

    private final EarleyChart chart = new EarleyChart();
    private final ForestNodeSet V;
    private final ParserGrammar grammar;
    private final ParseForest graph;
    private final NonterminalSymbol S;
    private final HashMap<NonterminalSymbol, List<Rule>> Rho;
    private final ParserInput parserInput;
    protected Token[] input = null;
    protected int inputPos = 0;
    protected int offset = -1;
    protected int lineNumber = -1;
    protected int columnNumber = -1;

    protected boolean restartable = false;
    protected int restartPos = 0;
    private boolean moreInput = false;
    protected final ParserOptions options;
    protected ProgressMonitor monitor = null;
    protected int progressSize = 0;
    protected int progressCount = 0;
    private String stringInput = null;

    protected EarleyParser(ParserGrammar grammar, ParserOptions options) {
        this.grammar = grammar;
        this.options = options;

        parserInput = new ParserInput(options, grammar.usesRegex);

        List<Rule> allRules = allRules(grammar.getRules());

        // I actually only care about the rules, so copy them.
        HashSet<NonterminalSymbol> nulled = new HashSet<>();
        Rho = new HashMap<>();
        for (Rule rule : allRules) {
            if (!Rho.containsKey(rule.getSymbol())) {
                Rho.put(rule.getSymbol(), new ArrayList<>());
            }
            Rho.get(rule.getSymbol()).add(rule);
            if (rule.getRhs().isEmpty()) {
                nulled.add(rule.getSymbol());
            }
        }

        // If there are any nulled symbols that don't have an epsilon rule, add one
        // (Since I rewrote Grammar.expandOptionalSymbols in May 2022, I don't
        // think this condition ever applies anymore.)
        for (NonterminalSymbol symbol : Rho.keySet()) {
            if (grammar.isNullable(symbol) && !nulled.contains(symbol)) {
                Rho.get(symbol).add(new Rule(symbol));
            }
        }

        S = grammar.getSeed();

        if (!Rho.containsKey(S)) {
            throw ParseException.seedNotInGrammar(S.toString());
        }

        graph = new ParseForest(options);
        V = new ForestNodeSet(graph);
    }

    /**
     * Return the parser type.
     * @return {@link ParserType#Earley}
     */
    public ParserType getParserType() {
        return ParserType.Earley;
    }

    private List<Rule> allRules(List<Rule> initialList) {
        HashSet<NonterminalSymbol> defined = new HashSet<>();
        HashSet<NonterminalSymbol> used = new HashSet<>();
        for (Rule rule : initialList) {
            defined.add(rule.symbol);
            for (Symbol symbol : rule.getRhs().symbols) {
                if (symbol instanceof NonterminalSymbol) {
                    used.add((NonterminalSymbol) symbol);
                }
            }
        }

        ArrayList<Rule> rules = new ArrayList<>(initialList);
        for (NonterminalSymbol symbol : used) {
            if (!defined.contains(symbol)) {
                Rule rule = new Rule(symbol, TerminalSymbol.UNDEFINED);
                rules.add(rule);
            }
        }

        return rules;
    }

    /**
     * Get the grammar used by this parser.
     * @return the grammar
     */
    public ParserGrammar getGrammar() {
        return grammar;
    }

    /**
     * Get the {@link NonterminalSymbol} seed value used by this parser.
     * @return the seed
     */
    public NonterminalSymbol getSeed() {
        return S;
    }

    /**
     * Parse an input string against the grammar.
     *
     * <p>This is a shortcut for parsing a sequence of characters.</p>
     *
     * @param input the input string
     * @return a parse result
     */
    public EarleyResult parse(String input) {
        parserInput.from(input);
        this.input = parserInput.tokens();
        this.stringInput = parserInput.string();
        return parseInput();
    }

    /**
     * Parse an array of tokens against the grammar.
     * <p>You <b>must not</b> change the input array.</p>
     * @param input the input array
     * @return a parse result
     */
    public EarleyResult parse(Token[] input) {
        parserInput.from(input);
        this.input = parserInput.tokens();
        this.stringInput = parserInput.string();
        return parseInput();
    }

    /**
     * Parse a sequence of tokens against the grammar.
     *
     * @param input the input sequence
     * @return a parse result
     */
    public EarleyResult parse(Iterator<Token> input) {
        parserInput.from(input);
        this.input = parserInput.tokens();
        this.stringInput = parserInput.string();
        return parseInput();
    }

    private EarleyResult parseInput() {
        return parseInput(0);
    }

    private EarleyResult parseInput(int startPos) {
        /*
        if (grammar.usesRegex) {
            throw ParseException.regexUnsupported();
        }
        */

        inputPos = startPos;
        restartable = false;

        monitor = options.getProgressMonitor();
        if (monitor != null) {
            progressSize = monitor.starting(this, input.length - inputPos);
            progressCount = progressSize;
        }

        ArrayList<EarleyItem> Q = new ArrayList<>();
        ArrayList<EarleyItem> Qprime = new ArrayList<>();
        int tokenCount = 0;
        Token lastInputToken = null;

        boolean emptyInput = true;
        Token currentToken = null;
        if (inputPos < input.length) {
            emptyInput = false;
            currentToken = input[inputPos];
        }
        for (Rule rule : Rho.get(S)) {
            final Symbol alpha = rule.getRhs().getFirst();

            if (alpha == null || alpha instanceof NonterminalSymbol) {
                State state = new State(rule);
                chart.add(0, new EarleyItem(state, 0));
            }

            if (alpha instanceof TerminalSymbol && alpha.matches(currentToken)) {
                State state = new State(rule);
                Qprime.add(new EarleyItem(state, 0));
            }
        }

        Token nextToken = currentToken;
        boolean consumedInput = true;
        boolean done = false;
        boolean lastToken = false;
        int checkpoint = -1;
        int i = 0;

        options.getLogger().info(logcategory, "Parsing %,d tokens with Earley parser.", input.length - startPos);

        StopWatch timer = new StopWatch();

        ArrayList<Hitem> H = new ArrayList<>();
        ArrayList<ForestNode> localRoots = new ArrayList<>();

        while (!done) {
            currentToken = nextToken;

            //System.err.printf("%4d %s%n", inputPos, currentToken);

            // Whether we consumed the input or not matters during the process
            // and also at the end. If there are no more tokens, make sure that
            // consumedInput is true so that we don't think we missed one at the end.
            // (Conversely, if we did just get a token, then we haven't consumed it yet,
            // and we want to keep track of that fact so that if we exit the loop, we
            // know there was a token left over.)
            consumedInput = currentToken == null;

            if (progressSize > 0) {
                if (progressCount == 0) {
                    monitor.progress(this, tokenCount);
                    progressCount = progressSize - 1;
                } else {
                    progressCount--;
                }
            }

            if (currentToken != null) {
                lastInputToken = currentToken;
                tokenCount = i + 1;
                //options.getLogger().trace(logcategory, "Parsing token %d: %s", tokenCount, currentToken);
            }

            H.clear();
            ArrayList<EarleyItem> R = new ArrayList<>(chart.get(i));

            Q.clear();
            Q.addAll(Qprime);

            Qprime.clear();

            while (!R.isEmpty()) {
                //options.getLogger().trace(logcategory, "Processing R: %d", R.size());
                EarleyItem Lambda = R.remove(0);
                if (Lambda.state != null && Lambda.state.nextSymbol() instanceof NonterminalSymbol) {
                    NonterminalSymbol C = (NonterminalSymbol) Lambda.state.nextSymbol();
                    EarleyItem regexItem = null;
                    for (Rule rule : Rho.get(C)) {
                        final Symbol delta = rule.getRhs().getFirst();
                        if (delta == null || delta instanceof NonterminalSymbol) {
                            EarleyItem item = new EarleyItem(new State(rule), i);
                            if (!chart.contains(i, item)) {
                                chart.add(i, item);
                                R.add(item);
                            }
                        }

                        boolean matches = false;
                        if (delta instanceof TerminalSymbol) {
                            TerminalSymbol ts = (TerminalSymbol) delta;
                            if (ts.token instanceof TokenRegex) {
                                String greedyMatch = ((TokenRegex) ts.token).matches(stringInput.substring(i));
                                if (greedyMatch != null) {
                                    ArrayList<Symbol> newRHS = new ArrayList<>();
                                    if (greedyMatch.isEmpty()) {
                                        regexItem = new EarleyItem(new State(C, 0, newRHS), i);
                                        chart.add(i, regexItem);
                                        R.add(regexItem);
                                    } else {
                                        for (int pos = 0; pos < Lambda.state.position; pos++) {
                                            newRHS.add(Lambda.state.rhs.get(pos));
                                        }
                                        for (int pos = 0; pos < greedyMatch.length(); pos++) {
                                            newRHS.add(TerminalSymbol.ch(greedyMatch.charAt(pos)));
                                        }

                                        regexItem = new EarleyItem(new State(C, Lambda.state.position, newRHS), i);
                                        matches = true;
                                    }
                                }
                            } else {
                                matches = delta.matches(currentToken);
                            }

                        }

                        if (matches) {
                            final EarleyItem item;
                            if (regexItem == null) {
                                item = new EarleyItem(new State(rule), i);
                            } else {
                                item = regexItem;
                            }
                            if (!Q.contains(item)) {
                                Q.add(item);
                                consumedInput = true;
                            }
                        }
                    }

                    //options.getLogger().trace(logcategory, "Processing H: %d", H.size());
                    for (Hitem hitem : H) {
                        if (hitem.symbol.equals(C)) {
                            State newState = Lambda.state.advance();
                            ForestNode y = make_node(newState, Lambda.j, i, Lambda.w, hitem.w);
                            Symbol Beta = newState.nextSymbol();
                            EarleyItem item = new EarleyItem(newState, Lambda.j, y);
                            if (Beta == null || Beta instanceof NonterminalSymbol) {
                                if (!chart.contains(i, item)) {
                                    chart.add(i, item);
                                    R.add(item);
                                }
                            }
                            if (Beta instanceof TerminalSymbol && Beta.matches(currentToken)) {
                                if (!Q.contains(item)) {
                                    Q.add(item);
                                    consumedInput = true;
                                }
                            }
                        }
                    }
                }

                if (Lambda.state != null && Lambda.state.completed()) {
                    ForestNode w = Lambda.w;
                    int h = Lambda.j;
                    NonterminalSymbol D = Lambda.state.getSymbol();
                    if (w == null) {
                        w = V.conditionallyCreateNode(D, Lambda.state, i, i);
                        w.addFamily(null, Lambda.state);
                    }
                    if (h == i) {
                        H.add(new Hitem(D, w));
                    }
                    int hpos = 0;
                    while (hpos < chart.get(h).size()) {
                        //options.getLogger().trace(logcategory, "Processing chart: %d: %d of %d", h, hpos, chart.get(h).size());
                        EarleyItem item = chart.get(h).get(hpos);
                        if (item.state != null && D.equals(item.state.nextSymbol())) {
                            State newState = item.state.advance();
                            ForestNode y = make_node(newState, item.j, i, item.w, w);
                            Symbol delta = newState.nextSymbol();
                            EarleyItem nextItem = new EarleyItem(newState, item.j, y);
                            if (delta == null || delta instanceof NonterminalSymbol) {
                                if (!chart.contains(i, nextItem)) {
                                    chart.add(i, nextItem);
                                    R.add(nextItem);
                                }
                            }
                            if (delta instanceof TerminalSymbol && delta.matches(currentToken)) {
                                Q.add(nextItem);
                                consumedInput = true;
                            }
                        }
                        hpos++;
                    }
                }
            }

            if (options.getPrefixParsing() && chart.size() > 0) {
                //options.getLogger().trace(logcategory, "Processing chart: %d: %d", chart.size()-1, chart.get(chart.size()-1).size());
                localRoots.clear();
                for (EarleyItem item : chart.get(chart.size()-1)) {
                    if (item.state.completed() && item.j == 0 && item.state.getSymbol().equals(S)) {
                        if (item.w != null) {
                            localRoots.add(item.w);
                        }
                        checkpoint = graph.size();
                        restartable = true;
                        restartPos = consumedInput ? inputPos + 1 : inputPos;
                    }
                }
                if (!localRoots.isEmpty()) {
                    //options.getLogger().debug(logcategory, "Resetting graph roots, %d new roots", localRoots.size());
                    graph.clearRoots();
                    for (ForestNode node : localRoots) {
                        graph.root(node);
                    }
                }
            }

            V.clear();
            ForestNode v = null;
            if (currentToken != null) {
                v = graph.createNode(new TerminalSymbol(currentToken), i, i+1);
            }

            done = lastToken;
            if (inputPos+1 < input.length) {
                nextToken = input[++inputPos];
            } else {
                nextToken = null;
                lastToken = true;
            }

            while (!Q.isEmpty()) {
                //options.getLogger().trace(logcategory, "Processing Q: %d", Q.size());
                EarleyItem Lambda = Q.remove(0);
                State nextState = Lambda.state.advance();

                ForestNode y = make_node(nextState, Lambda.j, i+1, Lambda.w, v);
                Symbol Beta = nextState.nextSymbol();
                if (Beta == null || Beta instanceof NonterminalSymbol) {
                    EarleyItem nextItem = new EarleyItem(nextState, Lambda.j, y);
                    if (!chart.contains(i+1, nextItem)) {
                        chart.add(i+1, nextItem);
                    }
                }
                if (Beta instanceof TerminalSymbol && Beta.matches(nextToken)) {
                    EarleyItem nextItem = new EarleyItem(nextState, Lambda.j, y);
                    Qprime.add(nextItem);
                }
            }

            i++;

            done = done || (chart.get(i).isEmpty() && Qprime.isEmpty());
        }

        timer.stop();

        if (monitor != null) {
            if (progressSize > 0) {
                monitor.progress(this, tokenCount);
            }
            monitor.finished(this);
        }

        // If there are still tokens left, we bailed early. (No pun intended.)
        boolean success = inputPos == input.length || (inputPos + 1 == input.length && consumedInput && lastToken);
        moreInput = !success;

        if (success) {
            success = false;
            localRoots.clear();
            int index = chart.size() - 1;
            while (index > 0 && chart.get(index).isEmpty()) {
                index--;
            }
            for (EarleyItem item : chart.get(index)) {
                if (item.state.completed() && item.state.getSymbol().equals(S) && item.j == 0) {
                    success = true;
                    // Don't add null to the list of roots. item.w will be null if, for example,
                    // there was no input and the start symbol matched the empty sequence.
                    if (item.w != null) {
                        localRoots.add(item.w);
                    }
                }
            }

            // If we got here because there's no input, make the (only) node in the
            // graph a root, even though it doesn't have any children.
            if (emptyInput && localRoots.isEmpty() && !graph.graph.isEmpty()) {
                localRoots.add(graph.graph.get(0));
            }

            if (!localRoots.isEmpty()) {
                options.getLogger().debug(logcategory, "Resetting graph roots, %d new roots", localRoots.size());
                graph.clearRoots();
                for (ForestNode node : localRoots) {
                    graph.root(node);
                }
            }
        }

        EarleyResult result;
        if (success) {
            if (tokenCount == 0 || timer.duration() == 0) {
                options.getLogger().info(logcategory, "Parse succeeded");
            } else {
                options.getLogger().info(logcategory, "Parse succeeded, %,d tokens in %s (%s tokens/sec)",
                        tokenCount, timer.elapsed(), timer.perSecond(tokenCount));
            }

            graph.prune();

            if (options.getReturnChart()) {
                result = new EarleyResult(this, chart, graph, success, tokenCount, lastInputToken);
            } else {
                chart.clear();
                result = new EarleyResult(this, graph, success, tokenCount, lastInputToken);
            }
        } else {
            // The parse failed, so we're looking at the character we couldn't match
            inputPos--;

            if (timer.duration() == 0) {
                options.getLogger().info(logcategory, "Parse failed after %,d tokens", tokenCount);
            } else {
                options.getLogger().info(logcategory, "Parse failed after %,d tokens in %s (%s tokens/sec)",
                        tokenCount, timer.elapsed(), timer.perSecond(tokenCount));
            }
            if (options.getPrefixParsing() && checkpoint >= 0) {
                graph.rollback(checkpoint);
                graph.prune();
            }
            result = new EarleyResult(this, chart, graph, success, tokenCount, lastInputToken);
            result.setPath(findPath());

            result.addPredicted(V.openPredictions());
        }

        result.setParseTime(timer.duration());

        return result;
    }

    protected EarleyResult continueParsing(GearleyParser newParser) {
        if (!(newParser instanceof EarleyParser)) {
            throw new UnsupportedOperationException("Cannot continue parsing with a GLL parser");
        }
        EarleyParser parser = (EarleyParser) newParser;
        parser.input = input;
        parser.stringInput = stringInput;
        return parser.parseInput(restartPos);
    }

    /**
     * Is there more input?
     * <p>If the parse succeeded, the answer will always be false. But a failed parse
     * can fail because it was unable to process a token or because it ran out of tokens.
     * This method checks if there was any more input after the parse completed.</p>
     * @return true if parsing failed before the entire input was consumed
     */
    public boolean hasMoreInput() {
        return moreInput;
    }

    public int getLineNumber() {
        computeOffsets();
        return lineNumber;
    }

    public int getColumnNumber() {
        computeOffsets();
        return columnNumber;
    }

    public int getOffset() {
        computeOffsets();
        return offset;
    }

    private void computeOffsets() {
        if (offset >= 0) {
            return;
        }

        offset = 0;
        lineNumber = 1;
        columnNumber = 1;

        for (int pos = 0; pos < inputPos; pos++) {
            offset++;
            columnNumber++;
            if (input[pos] instanceof TokenCharacter) {
                if (((TokenCharacter) input[pos]).getCodepoint() == '\n') {
                    lineNumber++;
                    columnNumber = 1;
                }
            }
            if (input[pos].hasAttribute(ParserAttribute.LINE_NUMBER_NAME)) {
                lineNumber = Integer.parseInt(input[pos].getAttributeValue(ParserAttribute.LINE_NUMBER_NAME, "error"));
            }
            if (input[pos].hasAttribute(ParserAttribute.COLUMN_NUMBER_NAME)) {
                columnNumber = Integer.parseInt(input[pos].getAttributeValue(ParserAttribute.COLUMN_NUMBER_NAME, "error"));
            }
            if (input[pos].hasAttribute(ParserAttribute.OFFSET_NAME)) {
                offset = Integer.parseInt(input[pos].getAttributeValue(ParserAttribute.OFFSET_NAME, "error"));
            }
        }
    }

    private ForestNode make_node(State B, int j, int i, ForestNode w, ForestNode v) {
        ForestNode y;

        if (B.completed()) {
            Symbol s = B.getSymbol();
            y = V.conditionallyCreateNode(s, B, j, i);
            if (w == null) {
                y.addFamily(v, B);
            } else {
                y.addFamily(w, v, B);
            }
        } else {
            State s = B;
            if (B.getPosition() == 1 && !B.completed()) {
                y = v;
            } else {
                y = V.conditionallyCreateNode(s, j, i);
                if (w == null) {
                    y.addFamily(v, B);
                } else {
                    y.addFamily(w, v, B);
                }
            }
        }

        return y;
    }

    private EarleyPath findPath() {
        EarleyPath path = new EarleyPath(input);
        int matchCount = 0;

        NonterminalSymbol userRoot = grammar.getSeed();
        // HACK!
        if (userRoot.getName().equals("$$")) {
            Rule rootRule = grammar.getRulesForSymbol(grammar.getSeed()).get(0);
            if (rootRule.rhs.length == 1 && rootRule.rhs.get(0) instanceof NonterminalSymbol) {
                userRoot = (NonterminalSymbol) rootRule.rhs.get(0);
            }
        }

        int rangeStart = Integer.MAX_VALUE;
        int rangeEnd = -1;
        ArrayList<EarleyItem> completed = new ArrayList<>();
        for (int row = chart.size() - 1; row >= 0; row--) {
            for (EarleyItem item : chart.get(row)) {
                if (item.state.completed() && !item.state.symbol.getName().startsWith("$")
                        && item.w != null && item.w.leftExtent+1 < item.w.rightExtent) {
                    completed.add(item);
                    if (matchCount < 8 && !userRoot.equals(item.state.symbol)
                            && (item.w.rightExtent <= rangeStart || item.w.rightExtent >= rangeEnd)) {
                        path.addCompleted(item);
                        matchCount++;
                        rangeStart = item.w.leftExtent;
                        rangeEnd = item.w.rightExtent;
                    }
                }
            }

            if (completed.size() > 8192) {
                // that's far enough...
                break;
            }
        }

        int pos = chart.size();
        matchCount = 0;
        HashSet<NonterminalSymbol> seen = new HashSet<>();
        while (pos > 0) {
            int longest = 0;
            for (EarleyItem item : chart.get(pos)) {
                if (!item.state.symbol.symbolName.startsWith("$")
                        && !item.state.completed()
                        && item.w != null) {
                    if (item.w.rightExtent - item.w.leftExtent > longest) {
                        longest = item.w.rightExtent - item.w.leftExtent;
                    }
                }
            }
            if (longest > 0) {
                for (EarleyItem item : chart.get(pos)) {
                    if (!item.state.symbol.symbolName.startsWith("$")
                            && !item.state.completed()
                            && item.w != null
                            && item.w.rightExtent - item.w.leftExtent == longest) {
                        if (!seen.contains(item.state.symbol)) {
                            // What if we saw this one finish?
                            boolean finished = false;
                            for (EarleyItem citem : completed) {
                                if (citem.w.leftExtent == item.w.leftExtent && citem.state.symbol.equals(item.state.symbol)) {
                                    finished = true;
                                    break;
                                }
                            }

                            if (!finished) {
                                path.addOpen(item);
                                matchCount++;
                                seen.add(item.state.symbol);
                            }
                        }
                    }
                }
            }
            if (matchCount >= 8) {
                break;
            }
            pos--;
        }

        return path;
    }

    private static final class Hitem {
        public final NonterminalSymbol symbol;
        public final ForestNode w;
        public Hitem(NonterminalSymbol symbol, ForestNode w) {
            this.symbol = symbol;
            this.w = w;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Hitem) {
                Hitem other = (Hitem) obj;
                if (w == null) {
                    if (other.w != null) {
                        return false;
                    }
                    return symbol.equals(other.symbol);
                }
                return symbol.equals(other.symbol) && w.equals(other.w);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return symbol.hashCode() + (w == null ? 0 : 19 * w.hashCode());
        }

        @Override
        public String toString() {
            return symbol + ", " + w;
        }
    }
}
