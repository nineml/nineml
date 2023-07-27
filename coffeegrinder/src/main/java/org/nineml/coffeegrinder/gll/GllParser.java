package org.nineml.coffeegrinder.gll;

import org.nineml.coffeegrinder.exceptions.ParseException;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.tokens.TokenCharacter;
import org.nineml.coffeegrinder.tokens.TokenEOF;
import org.nineml.coffeegrinder.tokens.TokenRegex;
import org.nineml.coffeegrinder.util.ParserAttribute;
import org.nineml.coffeegrinder.util.StopWatch;
import org.nineml.logging.Logger;

import java.util.*;

/** The GLL parser.
 *
 * <p>The GLL parser compares an input sequence against a grammar and determines if the input
 * is a sentence in the grammar.</p>
 * <p>This is an attempt to implement the work described in
 * <a href="https://pure.royalholloway.ac.uk/portal/en/publications/derivation-representation-using-binary-subtree-sets(d718368b-d4a4-49c8-b023-bdaae5feaece).html">Derivation
 * representation using binary subtree sets</a> by Elizabeth Scott, Adrian Johnstone, and L. Thomas van Binsbergen.
 * It returns the SPPF style forest derived from the binary subtrees. (There's some aspect of the description
 * of how to extract trees directly from the BSR that I cannot grok.)</p>
 */
public class GllParser implements GearleyParser {
    public static final String logcategory = "Parser";
    public static final String gllexecution = "GLLExecution";

    private final ParserOptions options;
    protected final Logger logger;

    public final ParserGrammar grammar;
    private final ArrayList<State> grammarSlots;
    private final HashMap<Rule, List<State>> ruleSlots;
    private final HashMap<State, Integer> slotLabels;
    private final ParserInput parserInput;

    private String stringInput = null;
    private Token[] I;
    private TokenInfo[] tokenInfo;
    private PoppedNodeBucket[] pnBuckets;
    private final int pnBucketFactor = 10;
    private int c_U;
    private int c_I;
    private HashSet<Descriptor> U;
    private ArrayList<Descriptor> R;
    private HashMap<ClusterNode, ArrayList<CrfNode>> crf;
    private BinarySubtree bsr;
    private final List<Instruction> instructions;
    private int instructionPointer = 0;
    private int nextInstruction = 0;
    private boolean moreInput = false;
    private boolean done = false;
    private long instructionCounter = 0;
    private int progressSize = 0;
    private int progressCount = 0;
    private int highwater = 0;
    int offset = -1;
    private int lineNumber = -1;
    private int columnNumber = -1;

    protected int tokenCount;
    protected Token lastToken;

    public GllParser(ParserGrammar grammar, ParserOptions options) {
        this.grammar = grammar;
        this.options = new ParserOptions(options);
        this.logger = options.getLogger();

        parserInput = new ParserInput(options, grammar.usesRegex);

        grammarSlots = new ArrayList<>();
        ruleSlots = new HashMap<>();
        NonterminalSymbol seed = grammar.getSeed();
        for (Rule rule : grammar.getRulesForSymbol(seed)) {
            List<State> slots = rule.getSlots();
            grammarSlots.addAll(slots);
            ruleSlots.put(rule, slots);
        }
        for (NonterminalSymbol symbol : grammar.getSymbols()) {
            if (!symbol.equals(seed)) {
                for (Rule rule : grammar.getRulesForSymbol(symbol)) {
                    List<State> slots = rule.getSlots();
                    grammarSlots.addAll(slots);
                    ruleSlots.put(rule, slots);
                }
            }
        }

        slotLabels = new HashMap<>();
        instructions = new ArrayList<>();
        compile();
    }

    @Override
    public GllResult parse(Token[] input) {
        parserInput.from(input);
        I = parserInput.tokens();
        stringInput = parserInput.string();
        return parseInput();
    }

    @Override
    public GllResult parse(Iterator<Token> input) {
        parserInput.from(input);
        I = parserInput.tokens();
        stringInput = parserInput.string();
        return parseInput();
    }

    @Override
    public GllResult parse(String input) {
        parserInput.from(input);
        I = parserInput.tokens();
        stringInput = parserInput.string();
        return parseInput();
    }

    private GllResult parseInput() {
        tokenInfo = new TokenInfo[I.length];
        for (int index = 0; index < tokenInfo.length; index++) {
            tokenInfo[index] = new TokenInfo();
        }
        pnBuckets = new PoppedNodeBucket[pnBucketFactor];
        for (int index = 0; index < pnBucketFactor; index++) {
            pnBuckets[index] = new PoppedNodeBucket();
        }

        U = new HashSet<>();
        R = new ArrayList<>();
        crf = new HashMap<>();

        crf.put(new ClusterNode(grammar.getSeed(), 0), new ArrayList<>());

        bsr = new BinarySubtree(grammar.getSeed(), I.length, options);
        c_U = 0;
        c_I = 0;

        ntAdd(grammar.getSeed(), 0);

        options.getLogger().info(gllexecution, "Parsing %,d tokens with GLL parser.", I.length);

        nextInstruction = 1;
        done = false;

        ProgressMonitor monitor = options.getProgressMonitor();
        if (monitor != null) {
            progressSize = monitor.starting(this, I.length);
            progressCount = progressSize;
        }

        StopWatch timer = new StopWatch();
        while (!done) {
            if (bsr.getRightExtent() > highwater) {
                highwater = bsr.getRightExtent();
            }

            if (monitor != null) {
                progressCount--;
                if (progressCount <= 0) {
                    monitor.workingSet(this, R.size(), highwater);
                    progressCount = progressSize;
                }
            }

            instructionPointer = nextInstruction;
            Instruction instruction = instructions.get(instructionPointer);
            nextInstruction++;
            instructionCounter++;
            instruction.run();
        }
        timer.stop();

        if (monitor != null) {
            monitor.finished(this);
        }

        moreInput = bsr.getRightExtent() + 1 < I.length;

        tokenCount = bsr.getRightExtent();
        if (tokenCount < I.length) {
            lastToken = I[tokenCount];
        }
        tokenCount++; // 1-based for the user

        // The parser did not succeed if it didn't consume all the input!
        if (bsr.succeeded(moreInput)) {
            if (timer.duration() == 0) {
                options.getLogger().info(logcategory, "Parse succeeded");
            } else {
                options.getLogger().info(logcategory, "Parse succeeded, %,d tokens in %s (%s tokens/sec)",
                        tokenCount, timer.elapsed(), timer.perSecond(tokenCount));
            }
        } else {
            if (timer.duration() == 0) {
                options.getLogger().info(logcategory, "Parse failed after %,d tokens", tokenCount);
            } else {
                options.getLogger().info(logcategory, "Parse failed after %,d tokens in %s (%s tokens/sec)",
                        tokenCount, timer.elapsed(), timer.perSecond(tokenCount));
            }
        }

        GllResult result = new GllResult(this, bsr);
        result.setParseTime(timer.duration());

        //Instrumentation.report();

        return result;
    }

    void nextDescriptor() {
        done = R.isEmpty();
        if (done) {
            logger.trace(gllexecution, "%4d | ---- exit", instructionCounter, nextInstruction);
        } else {
            Descriptor desc = R.remove(0);
            c_U = desc.k;
            c_I = desc.j;
            if (desc.slot == State.L0) {
                nextInstruction = 1;
            } else {
                nextInstruction = slotLabels.get(desc.slot);
            }

            // The nextDescriptor statement is always at position 1 in the program
            logger.trace(gllexecution, "%4d | %4d c_U = %d; c_I = %d; goto %d", instructionCounter, 1, c_U, c_I, nextInstruction);
        }
    }

    protected void incrementCI() {
        c_I++;
        logger.trace(gllexecution, "%4d | %4d c_I = %d", instructionCounter, instructionPointer, c_I);
    }

    protected void jump(State slot) {
        if (slot == State.L0) {
            nextInstruction = 1;
        } else {
            nextInstruction = slotLabels.get(slot);
        }
        logger.trace(gllexecution, "%4d | %4d goto %d", instructionCounter, instructionPointer, nextInstruction);
    }

    protected void call(State slot) {
        call(slot, c_U, c_I);
    }

    protected void call(State L, int i, int j) {
        //Instrumentation.count("call");
        logger.trace(gllexecution, "%4d | %4d call(%s, %d, %d)", instructionCounter, instructionPointer, L, i, j);
        CrfNode u = getCrfNode(L, i);
        NonterminalSymbol X = (NonterminalSymbol) L.prevSymbol();
        ClusterNode ndV = getClusterNode(X, j);
        if (!crf.containsKey(ndV)) {
            ArrayList<CrfNode> newList = new ArrayList<>();
            newList.add(u);
            crf.put(ndV, newList);
            ntAdd(X, j);
        } else {
            List<CrfNode> v = crf.get(ndV);
            if (!edgeExists(v, u)) {
                v.add(u);
                for (PoppedNode pnd : pnBuckets[j % pnBucketFactor].P) {
                    if (X.equals(pnd.symbol) && j == pnd.k) {
                        dscAdd(L, i, pnd.j);
                        bsrAdd(L, i, j, pnd.j);
                    }
                }
            }
        }
    }

    protected void testSelect(State slot) {
        String expr = "";

        // Don't go to all the trouble of constructing the string if we aren't going to output it
        if (logger.getLogLevel(gllexecution) >= Logger.TRACE) {
            StringBuilder sb = new StringBuilder();
            sb.append("testSelect(").append(I[c_I]).append(", ").append(slot.symbol).append(", ");
            int pos = slot.position;
            while (pos < slot.rhs.length) {
                if (pos > slot.position) {
                    sb.append(" ");
                }
                sb.append(slot.rhs.get(pos));
                pos++;
            }
            expr = sb.toString();
        }

        if (testSelect(I[c_I], slot.symbol, slot)) {
            logger.trace(gllexecution, "%4d | %4d if (%s) then nop", instructionCounter, instructionPointer, expr);
        } else {
            logger.trace(gllexecution, "%4d | %4d if (!%s) then goto %d", instructionCounter, instructionPointer, expr, 1);
            jump(State.L0);
        }
    }

    protected void bsrAdd(State slot, boolean epsilon) {
        if (epsilon) {
            bsrAddEpsilon(slot, c_I);
        } else {
            bsrAdd(slot, c_U, c_I, c_I + 1);
        }
    }

    protected void bsrAdd(State L, int i, int k, int j) {
        if (instructionPointer >= 0) {
            logger.trace(gllexecution, "%4d | %4d bsrAdd(%s, %d, %d, %d)", instructionCounter, instructionPointer, L, i, k, j);
        } else {
            logger.trace(gllexecution, "%4d | ---- bsrAdd(%s, %d, %d, %d)", instructionCounter, L, i, k, j);
        }

        int rightExtent = j;
        if (L.rhs.symbols[L.position - 1] instanceof TerminalSymbol) {
            TerminalSymbol sym = (TerminalSymbol) L.rhs.symbols[L.position - 1];
            if (sym.getToken() instanceof TokenRegex) {
                TokenRegex token = (TokenRegex) sym.getToken();
                String matched = token.matches(stringInput.substring(c_I));
                if (matched != null) {
                    c_I += (matched.length() - 1);
                    rightExtent += (matched.length() - 1);
                }
            }
        }

        bsr.add(L, i, k, rightExtent);
    }

    protected void bsrAddEpsilon(State L, int i) {
        if (instructionPointer >= 0) {
            logger.trace(gllexecution, "%4d | %4d bsrAdd(%s, %d, %d, %d)", instructionCounter, instructionPointer, L, i, i, i);
        } else {
            logger.trace(gllexecution, "%4d | ---- bsrAdd(%s, %d, %d, %d)", instructionCounter, L, i, i, i);
        }
        bsr.addEpsilon(L, i);
    }

    protected void follow(NonterminalSymbol symbol) {
        Token token = c_I >= I.length ? null : I[c_I];
        boolean inFollow = false;
        for (Symbol sym : grammar.getFollow(symbol)) {
            if (sym.matches(token)) {
                inFollow = true;
                break;
            }
        }
        if (inFollow) {
            logger.trace(gllexecution, "%4d | %4d if (%s ∈ follow(%s)) then rtn(%s, %d, %d)", instructionCounter, instructionPointer, token, symbol, symbol, c_U, c_I);
            rtn(symbol, c_U, c_I);
        } else {
            logger.trace(gllexecution, "%4d | %4d if (%s ∉ follow(%s)) then nop", instructionCounter, instructionPointer, token, symbol);
        }
    }

    void label(State slot) {
        // This should never happen, but...
        nextInstruction = instructionPointer++;
    }

    void comment(String text) {
        // This should never happen, but...
        nextInstruction = instructionPointer++;
    }

    protected void rtn(NonterminalSymbol X, int k, int j) {
        PoppedNode pn = new PoppedNode(X, k, j);
        if (!pnBuckets[k % pnBucketFactor].P.contains(pn)) {
            pnBuckets[k % pnBucketFactor].P.add(pn);
            ClusterNode Xk = getClusterNode(X, k);
            if (crf.containsKey(Xk)) {
                for (CrfNode v : crf.get(Xk)) {
                    dscAdd(v.slot, v.i, j);
                    bsrAdd(v.slot, v.i, k, j);
                }
            } else {
                logger.trace(gllexecution, "No key " + Xk + " in crf");
            }
        }
    }

    private CrfNode getCrfNode(State L, int i) {
        CrfNode node = tokenInfo[i].crfNodes.getOrDefault(L, null);
        if (node == null) {
            node = new CrfNode(L, i);
            tokenInfo[i].crfNodes.put(L, node);
        }
        return node;
    }

    private ClusterNode getClusterNode(NonterminalSymbol X, int k) {
        ClusterNode node = tokenInfo[k].clusterNodes.getOrDefault(X, null);
        if (node == null) {
            node = new ClusterNode(X, k);
            tokenInfo[k].clusterNodes.put(X, node);
        }
        return node;
    }

    protected void ntAdd(NonterminalSymbol X, int j) {
        for (State slot : grammarSlots) {
            if (X.equals(slot.symbol) && slot.position == 0) {
                if (testSelect(I[j], X, slot)) {
                    dscAdd(slot, j, j);
                }
            }
        }
    }

    protected boolean testSelect(Token b, NonterminalSymbol X, State alpha) {
        boolean hasEpsilon = false;
        for (Symbol symbol : alpha.getFirst(grammar)) {
            if (symbol.matches(b)) {
                return true;
            }
            hasEpsilon = hasEpsilon || (symbol == TerminalSymbol.EPSILON);
        }

        if (hasEpsilon) {
            for (Symbol symbol : grammar.getFollow(X)) {
                if (symbol.matches(b)) {
                    return true;
                }
            }
        }

        return false;
    }

    protected void dscAdd(State slot, int k, int i) {
        Descriptor desc = slot.getDescriptor(k, i);
        if (!U.contains(desc)) {
            U.add(desc);
            R.add(desc);
        }
    }

    private boolean edgeExists(List<CrfNode> nodes, CrfNode target) {
        for (CrfNode node : nodes) {
            if (node.equals(target)) {
                return true;
            }
        }
        return false;
    }

    public boolean succeeded() {
        return done && bsr.succeeded(moreInput);
    }

    public Token[] getTokens() {
        return I;
    }

    @Override
    public ParserType getParserType() {
        return ParserType.GLL;
    }

    @Override
    public ParserGrammar getGrammar() {
        return grammar;
    }

    @Override
    public NonterminalSymbol getSeed() {
        return grammar.getSeed();
    }

    @Override
    public boolean hasMoreInput() {
        return moreInput;
    }

    @Override
    public int getOffset() {
        computeOffsets();
        return offset;
    }

    @Override
    public int getLineNumber() {
        computeOffsets();
        return lineNumber;
    }

    @Override
    public int getColumnNumber() {
        computeOffsets();
        return columnNumber;
    }

    private void computeOffsets() {
        if (offset >= 0) {
            return;
        }

        offset = 0;
        lineNumber = 1;
        columnNumber = 1;

        for (int pos = 0; pos < highwater; pos++) {
            offset++;
            columnNumber++;
            if (I[pos] instanceof TokenCharacter) {
                if (((TokenCharacter) I[pos]).getCodepoint() == '\n') {
                    lineNumber++;
                    columnNumber = 1;
                }
            }
            if (I[pos].hasAttribute(ParserAttribute.LINE_NUMBER_NAME)) {
                lineNumber = Integer.parseInt(I[pos].getAttributeValue(ParserAttribute.LINE_NUMBER_NAME, "error"));
            }
            if (I[pos].hasAttribute(ParserAttribute.COLUMN_NUMBER_NAME)) {
                columnNumber = Integer.parseInt(I[pos].getAttributeValue(ParserAttribute.COLUMN_NUMBER_NAME, "error"));
            }
            if (I[pos].hasAttribute(ParserAttribute.OFFSET_NAME)) {
                offset = Integer.parseInt(I[pos].getAttributeValue(ParserAttribute.OFFSET_NAME, "error"));
            }
        }
    }

    private void compile() {
        instructions.add(() -> label(State.L0));
        logger.trace(logcategory, "%4d %s:", 0, State.L0);
        instructions.add(this::nextDescriptor);
        logger.trace(logcategory, "%4d \t\tif (R.isEmpty()) then exit else process a descriptor", 1);

        // I don't think the seed symbol has to come first, but I like it better that way
        for (Rule rule : grammar.getRulesForSymbol(grammar.getSeed())) {
            compile(rule);
        }
        for (NonterminalSymbol symbol : grammar.getSymbols()) {
            if (!symbol.equals(grammar.getSeed())) {
                for (Rule rule : grammar.getRulesForSymbol(symbol)) {
                    compile(rule);
                }
            }
        }
    }

    private void compile(Rule rule) {
        ArrayList<State> slots = new ArrayList<>(ruleSlots.get(rule));
        slotLabels.put(slots.get(0), instructions.size() + 1);
        instructions.add(() -> {
            label(slots.get(0));
        });
        logger.trace(logcategory, "%4d %s:", instructions.size() - 1, slots.get(0));

        int pos = 0;
        for (State slot : slots) {
            compile(slot);
            if (pos > 0 && pos < slot.rhs.length) {
                instructions.add(() -> testSelect(slot));

                if (logger.getLogLevel(gllexecution) >= Logger.TRACE) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("\t\tif (!testSelect(I[c_I], ").append(slot.symbol).append(", ");
                    int ipos = slot.position;
                    while (ipos < slot.rhs.length) {
                        if (ipos > slot.position) {
                            sb.append(" ");
                        }
                        sb.append(slot.rhs.get(ipos));
                        ipos++;
                    }
                    sb.append(")) goto L₀");
                    logger.trace(logcategory, "%4d %s", instructions.size() - 1, sb);
                }
            }
            pos++;
        }
        instructions.add(() -> follow(rule.symbol));
        logger.trace(logcategory, "%4d %s", instructions.size() - 1, "\t\tif (I[c_I] ∈ follow(" + rule.symbol + ") then rtn(" + rule.symbol + ", c_U, c_I)");
        instructions.add(() -> jump(State.L0));
        logger.trace(logcategory, "%4d %s", instructions.size() - 1, "\t\tgoto " + State.L0);
    }

    private void compile(State slot) {
        if (slot.position == 0) {
            if (slot.rhs.isEmpty()) {
                compileEpsilon(slot);
            }
            return;
        }

        Symbol prev = slot.prevSymbol();
        if (prev instanceof TerminalSymbol) {
            compileTerminal(slot);
        } else {
            compileNonterminal(slot);
        }
    }

    private void compileEpsilon(State slot) {
        instructions.add(() -> bsrAdd(slot, true));
        logger.trace(logcategory, "%4d %s", instructions.size() - 1, "\t\tbsrAdd(" + slot + ", c_I, c_I, c_I)");
    }

    private void compileTerminal(State slot) {
        instructions.add(() -> bsrAdd(slot, false));
        logger.trace(logcategory, "%4d %s", instructions.size() - 1, "\t\tbsrAdd(" + slot + ", c_U, c_I, c_I+1)");
        instructions.add(this::incrementCI);
        logger.trace(logcategory, "%4d \t\tc_I = c_I + 1", instructions.size() - 1);
    }

    private void compileNonterminal(State slot) {
        //Instrumentation.count("compile Nonterminal");
        instructions.add(() -> call(slot));
        logger.trace(logcategory, "%4d %s", instructions.size() - 1, "\t\tcall(" + slot + ", c_U, c_I)");
        instructions.add(() -> jump(State.L0));
        logger.trace(logcategory, "%4d %s", instructions.size() - 1, "\t\tgoto " + State.L0);
        instructions.add(() -> label(slot));
        logger.trace(logcategory, "%4d %s:", instructions.size() - 1, slot);
        slotLabels.put(slot, instructions.size());
    }

    @FunctionalInterface
    interface Instruction {
        void run();
    }

    private static class TokenInfo {
        public final HashMap<State, CrfNode> crfNodes = new HashMap<>();
        public final HashMap<NonterminalSymbol, ClusterNode> clusterNodes = new HashMap<>();
    }

    private static class PoppedNodeBucket {
        public final HashSet<PoppedNode> P = new HashSet<>();
    }
}
