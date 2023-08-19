package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.exceptions.GrammarException;
import org.nineml.coffeegrinder.gll.GllParser;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.tokens.TokenRegex;
import org.nineml.coffeegrinder.util.RegexCompiler;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A grammar for the parser.
 *
 * <p>A grammar is a list of rules. Each rule defines a non-terminal symbol as a sequence of zero or
 * more symbols (terminal or nonterminal).</p>
 *
 * <p>A grammar can be used to create a parser for that grammar. The ParserGrammar is derived from
 * a {@link SourceGrammar}. The ParserGrammar is immutable.</p>
 */
public class ParserGrammar extends Grammar {
    public final boolean usesRegex;
    private final ParserType parserType;
    private final NonterminalSymbol seed;
    private final HashSet<Symbol> nullable;
    private final ParserOptions options;
    private final HashMap<NonterminalSymbol, Set<Symbol>> firstSets = new HashMap<>();
    private final HashMap<NonterminalSymbol, Set<Symbol>> followSets = new HashMap<>();
    private boolean computedSets = false;

    protected ParserGrammar(SourceGrammar grammar, ParserType parserType, NonterminalSymbol seed) {
        this.parserType = parserType;
        this.seed = seed;

        boolean sawRegex = false;
        for (Rule rule : grammar.getRules()) {
            if (!sawRegex) {
                for (Symbol symbol : rule.getRhs().symbols) {
                    Token token = (symbol instanceof TerminalSymbol) ? ((TerminalSymbol) symbol).getToken() : null;
                    if (token instanceof TokenRegex) {
                        sawRegex = true;
                        if (rule.getRhs().length != 1) {
                            throw GrammarException.invalidGrammarRegex();
                        }
                    }

                }
            }
        }
        usesRegex = sawRegex;

        rules.addAll(grammar.getRules());

        if (parserType == ParserType.GLL) {
            // If there is a TokenRegex in the grammar, and if that regex can match
            // the empty string, add a rule for Symbol => ε to handle that case.
            // But only once!
            HashMap<NonterminalSymbol, Rule> epsilonRules = new HashMap<>();
            for (Rule rule : rules) {
                if (rule.getRhs().length == 1 && rule.getRhs().get(0) instanceof TerminalSymbol) {
                    Token token = ((TerminalSymbol) rule.getRhs().get(0)).getToken();
                    if (token instanceof TokenRegex) {
                        String expr = token.getValue();
                        Pattern regex = Pattern.compile(expr);
                        Matcher matcher = regex.matcher("");
                        if (matcher.lookingAt() && !epsilonRules.containsKey(rule.getSymbol())) {
                            epsilonRules.put(rule.getSymbol(), new Rule(rule.getSymbol()));
                        }
                    }
                }
            }
            rules.addAll(epsilonRules.values());
        }

        this.metadata.putAll(grammar.getMetadataProperies());
        for (Rule rule : rules) {
            if (!rulesBySymbol.containsKey(rule.symbol)) {
                rulesBySymbol.put(rule.symbol, new ArrayList<>());
            }
            rulesBySymbol.get(rule.symbol).add(rule);
        }

        this.options = grammar.getParserOptions();
        nullable = new HashSet<>();
        if (parserType == ParserType.Earley) {
            computeEarleyNullable();
        } else {
            computeGllNullable();
        }
    }

    public NonterminalSymbol getSeed() {
        return seed;
    }

    public ParserOptions getParserOptions() {
        return options;
    }

    /**
     * Get a parser for this grammar.
     *
     * <p>Returns a parser that will parse an input against the rules that define this grammar.</p>
     * @return the parser
     */
    public GearleyParser getParser() {
        if (parserType == ParserType.Earley) {
            return new EarleyParser(this, options);
        } else {
            return new GllParser(this, options);
        }
    }

    /**
     * Is a symbol nullable?
     * @param symbol the symbol
     * @return true if the symbol is nullable
     * @throws UnsupportedOperationException if the grammar is open
     */
    @Override
    public boolean isNullable(Symbol symbol) {
        if (symbol instanceof NonterminalSymbol) {
            return nullable.contains(symbol);
        }
        return false;
    }

    /**
     * Get a hygiene report for this compiled grammar.
     * @return the report.
     */
    public HygieneReport getHygieneReport() {
        HygieneReport report = new HygieneReport(this);
        report.checkGrammar();
        return report;
    }

    /**
     * Return the set of "first" symbols for the specified symbol
     * <p>The <em>first</em> symbols of a nonterminal are all those symbols that can appear
     * "first" in a matching parse for that nonterminal. The first set of a terminal
     * symbol is just the symbol itself.</p>
     * @param symbol the symbol of interest
     * @return the first set for the symbol
     */
    public Set<Symbol> getFirst(Symbol symbol) {
        computeFirstAndFollowSets();

        if (symbol instanceof NonterminalSymbol) {
            if (firstSets.containsKey(symbol)) {
                return firstSets.get(symbol);
            }
            return Collections.emptySet();
        } else {
            Set<Symbol> firstSet = new HashSet<>();
            firstSet.add(symbol);
            return firstSet;
        }
    }

    public Set<Symbol> getFollow(Symbol symbol) {
        computeFirstAndFollowSets();

        if (symbol instanceof NonterminalSymbol && followSets.containsKey(symbol)) {
            return followSets.get(symbol);
        }

        return Collections.emptySet();
    }

    private void computeFirstAndFollowSets() {
        if (computedSets) {
            return; // we don't need to do this twice
        }

        if (seed == null) {
            throw new NullPointerException("Start symbol is null");
        }
        computedSets = true;

        firstSets.clear();
        followSets.clear();

        for (Rule rule : rules) {
            if (!firstSets.containsKey(rule.symbol)) {
                firstSets.put(rule.symbol, new HashSet<>());
                followSets.put(rule.symbol, new HashSet<>());
            }

            if (rule.epsilonRule()) {
                firstSets.get(rule.symbol).add(TerminalSymbol.EPSILON);
            }

            Set<Symbol> first = firstSets.get(rule.symbol);
            for (Symbol symbol : rule.rhs.symbols) {
                first.add(symbol);
                if (!nullable.contains(symbol)) {
                    break;
                }
            }
        }

        for (NonterminalSymbol symbol : firstSets.keySet()) {
            HashSet<Symbol> first = expandFirstSet(symbol, new HashSet<>());
            firstSets.get(symbol).clear();
            firstSets.get(symbol).addAll(first);
        }

        followSets.get(seed).add(TerminalSymbol.EOF);
        for (Rule rule : rules) {
            for (int pos = 0; pos < rule.rhs.length; pos++) {
                NonterminalSymbol prev = pos > 0 && rule.rhs.get(pos-1) instanceof NonterminalSymbol
                        ? (NonterminalSymbol) rule.rhs.get(pos-1)
                        : null;
                Symbol symbol = rule.rhs.get(pos);

                if (prev != null) {
                    computeFollow(rule, pos, prev);
                }

                if (pos+1 == rule.rhs.length && symbol instanceof NonterminalSymbol) {
                    NonterminalSymbol nt = (NonterminalSymbol) symbol;
                    // N.B. In an unhygienic grammar where there are undefined symbols,
                    // the followSet can be null.
                    Set<Symbol> followSet = followSets.get(nt);
                    if (followSet == null) {
                        followSet = new HashSet<>();
                        followSet.add(rule.symbol);
                        followSets.put(nt, followSet);
                    } else {
                        followSet.add(rule.symbol);
                    }
                }
            }
        }

        for (NonterminalSymbol symbol : followSets.keySet()) {
            HashSet<Symbol> follow = expandFollowSet(symbol, new HashSet<>());
            followSets.get(symbol).clear();
            followSets.get(symbol).addAll(follow);
        }
    }

    private void computeFollow(Rule rule, int pos, NonterminalSymbol symbol) {
        Symbol current = rule.rhs.get(pos);
        if (current instanceof TerminalSymbol) {
            followSets.get(symbol).add(current);
            return;
        }

        // In an unhygienic grammar, the symbol might be unknown.
        if (!firstSets.containsKey((NonterminalSymbol) current)) {
            return;
        }

        Set<Symbol> first = firstSets.get((NonterminalSymbol) current);
        if (!first.contains(TerminalSymbol.EPSILON)) {
            // In an unhygienic grammar, the symbol might be unknown.
            if (followSets.containsKey(symbol)) {
                followSets.get(symbol).addAll(first);
            }
            return;
        }

        for (Symbol fs : first) {
            if (fs != TerminalSymbol.EPSILON) {
                followSets.get(symbol).add(fs);
            }
        }

        if (pos+1 == rule.rhs.length) {
            // In an unhygienic grammar, the symbol might be unknown.
            if (followSets.containsKey(symbol)) {
                followSets.get(symbol).add(rule.symbol);
            }
        } else {
            computeFollow(rule, pos+1, symbol);
        }
    }

    private HashSet<Symbol> expandFirstSet(NonterminalSymbol symbol, HashSet<NonterminalSymbol> seen) {
        HashSet<Symbol> combined = new HashSet<>();
        if (!seen.contains(symbol)) {
            seen.add(symbol);
            // In an unhygienic grammar, the symbol might be unknown.
            if (firstSets.containsKey(symbol)) {
                for (Symbol first : firstSets.get(symbol)) {
                    if (first instanceof TerminalSymbol) {
                        combined.add(first);
                    } else {
                        combined.addAll(expandFirstSet((NonterminalSymbol) first, seen));
                    }
                }
            }
        }
        return combined;
    }

    private HashSet<Symbol> expandFollowSet(NonterminalSymbol symbol, HashSet<NonterminalSymbol> seen) {
        HashSet<Symbol> combined = new HashSet<>();
        if (!seen.contains(symbol)) {
            seen.add(symbol);
            // In an unhygienic grammar, the symbol might be unknown.
            if (followSets.containsKey(symbol)) {
                for (Symbol follow : followSets.get(symbol)) {
                    if (follow instanceof TerminalSymbol) {
                        combined.add(follow);
                    } else {
                        combined.addAll(expandFollowSet((NonterminalSymbol) follow, seen));
                    }
                }
            }
        }
        return combined;
    }

    private void computeEarleyNullable() {
        // For the Earley parser, the only symbols that are nullable are the ones
        // that have an epsilon production.
        nullable.clear();
        for (Rule rule : rules) {
            if (rule.rhs.isEmpty()) {
                nullable.add(rule.symbol);
            }
        }
    }

    private void computeGllNullable() {
        // For the GLL parser, any symbol that can lead to an epsilon production
        // is nullable.
        computeEarleyNullable();

        boolean changed = true;
        while (changed) {
            changed = false;
            for (Rule rule : rules) {
                if (!nullable.contains(rule.symbol)) {
                    if (rule.rhs.isEmpty()) {
                        nullable.add(rule.symbol);
                        changed = true;
                    } else {
                        boolean canBeNull = true;
                        int pos = 0;
                        while (canBeNull && pos < rule.rhs.symbols.length) {
                            Symbol symbol = rule.rhs.symbols[pos];
                            if (symbol instanceof TerminalSymbol || !nullable.contains(symbol)) {
                                canBeNull = false;
                            }
                            pos++;
                        }
                        if (canBeNull) {
                            nullable.add(rule.symbol);
                            changed = true;
                        }
                    }
                }
            }
        }
    }

    private String compileRegex(NonterminalSymbol start, List<Rule> sourceRules) {
        RegexCompiler compiler = new RegexCompiler(sourceRules);
        return compiler.compile(start);
    }

}
