package org.nineml.coffeegrinder.parser;

import java.util.*;

/**
 * A report on the hygiene of a grammar.
 * <p>A grammar is considered "unhygienic" if it contains any of the following features:
 * unreachable (i.e., unused) nonterminals, unproductive nonterminals, or unproductive
 * rules.</p>
 */
public class HygieneReport {
    public static final String logcategory = "Hygiene";

    private final SourceGrammar grammar;
    private final ParserGrammar parserGrammar;
    private final NonterminalSymbol seed;
    private final AmbiguityReport ambiguityReport;
    private final HashSet<Rule> unproductiveRules = new HashSet<>();
    private final HashSet<NonterminalSymbol> unproductiveSymbols = new HashSet<>();
    private final HashSet<NonterminalSymbol> unreachableSymbols = new HashSet<>();
    private final HashSet<NonterminalSymbol> undefinedSymbols = new HashSet<>();
    private ArrayList<Rule> rules = null;

    protected HygieneReport(ParserGrammar grammar) {
        this.parserGrammar = grammar;
        this.grammar = null;
        this.seed = grammar.getSeed();
        ambiguityReport = new AmbiguityReport(grammar);
    }

    protected HygieneReport(SourceGrammar grammar, NonterminalSymbol seed) {
        this.parserGrammar = null;
        this.grammar = grammar;
        this.seed = seed;
        ambiguityReport = new AmbiguityReport(grammar, seed);
    }

    public void checkGrammar() {
        if (parserGrammar != null && rules != null) {
            return; // no reason to do this twice, it can't change...
        }

        final Map<NonterminalSymbol,List<Rule>> rulesBySymbol;
        rules = new ArrayList<>();
        if (grammar != null) {
            rules.addAll(grammar.getRules());
            rulesBySymbol = grammar.getRulesBySymbol();
        } else {
            assert parserGrammar != null;
            rules.addAll(parserGrammar.getRules());
            rulesBySymbol = parserGrammar.getRulesBySymbol();
        }

        for (NonterminalSymbol nt : undefinedSymbols()) {
            addUndefined(nt);
        }

        HashSet<NonterminalSymbol> reachable = new HashSet<>();
        walk(seed, reachable);
        for (Rule rule : rules) {
            if (!reachable.contains(rule.getSymbol())) {
                addUnreachable(rule.getSymbol());
            }
        }

        for (NonterminalSymbol nt : undefinedSymbols) {
            if (!reachable.contains(nt)) {
                addUnreachable(nt);
            }
        }

        // What about unproductive non-terminals?
        HashSet<NonterminalSymbol> productiveNT = new HashSet<>();
        HashSet<Rule> productiveRule = new HashSet<>();
        int psize = -1;
        int rsize = -1;
        while (psize != productiveNT.size() || rsize != productiveRule.size()) {
            psize = productiveNT.size();
            rsize = productiveRule.size();

            for (NonterminalSymbol nt : rulesBySymbol.keySet()) {
                boolean isProductiveSymbol = false;
                for (Rule rule : rules) {
                    if (nt.equals(rule.getSymbol())) {
                        boolean isProductiveRule = productiveRule.contains(rule);
                        if (!isProductiveRule) {
                            isProductiveRule = true;
                            for (Symbol s : rule.getRhs().symbols) {
                                if (s instanceof NonterminalSymbol && !productiveNT.contains((NonterminalSymbol) s)) {
                                    isProductiveRule = false;
                                    break;
                                }
                            }
                            if (isProductiveRule) {
                                productiveRule.add(rule);
                                isProductiveSymbol = true;
                            }
                        }
                    }
                }
                if (isProductiveSymbol) {
                    productiveNT.add(nt);
                }
            }
        }

        for (NonterminalSymbol s : rulesBySymbol.keySet()) {
            if (!productiveNT.contains(s)) {
                addUnproductive(s);
            }
        }
        for (Rule rule : rules) {
            if (!productiveRule.contains(rule)) {
                addUnproductive(rule);
            }
        }
    }

    public void checkAmbiguity() {
        ambiguityReport.check();
    }

    private void walk(NonterminalSymbol symbol, HashSet<NonterminalSymbol> reachable) {
        reachable.add(symbol);
        for (Rule rule : rules) {
            if (rule.getSymbol().equals(symbol)) {
                for (Symbol s : rule.getRhs().symbols) {
                    if (s instanceof NonterminalSymbol) {
                        NonterminalSymbol nt = (NonterminalSymbol) s;
                        if (!reachable.contains(nt)) {
                            walk(nt, reachable);
                        }
                    }
                }
            }
        }
    }

    protected List<NonterminalSymbol> undefinedSymbols() {
        HashSet<NonterminalSymbol> definedNames = new HashSet<>();
        HashSet<NonterminalSymbol> usedNames = new HashSet<>();

        for (Rule rule : rules) {
            definedNames.add(rule.getSymbol());
            for (Symbol s : rule.getRhs().symbols) {
                if (s instanceof NonterminalSymbol) {
                    usedNames.add((NonterminalSymbol) s);
                }
            }
        }

        ArrayList<NonterminalSymbol> unused = new ArrayList<>();
        for (NonterminalSymbol nt : usedNames) {
            if (!definedNames.contains(nt)) {
                unused.add(nt);
            }
        }

        return unused;
    }

    /**
     * Is this grammar "clean"?
     * @return true iff the grammar has no unhygienic features
     */
    public boolean isClean() {
        return unproductiveRules.isEmpty()
                && unproductiveSymbols.isEmpty()
                && unreachableSymbols.isEmpty()
                && undefinedSymbols.isEmpty();
    }

    /**
     * Get the grammar associated with this report.
     * <p>Note that if the grammar was open when the report was created, it
     * may have changed since this report was created.</p>
     * @return the grammar.
     */
    public ParserGrammar getCompiledGrammar() {
        return parserGrammar;
    }

    /**
     * Get the unproductive rules.
     * @return the unproductive rules.
     */
    public Set<Rule> getUnproductiveRules() {
        return unproductiveRules;
    }

    /**
     * Get the unproductive symbols.
     * @return the unproductive symbols.
     */
    public Set<NonterminalSymbol> getUnproductiveSymbols() {
        return unproductiveSymbols;
    }

    /**
     * Get the unreachable symbols.
     * @return the unreachable symbols.
     */
    public Set<NonterminalSymbol> getUnreachableSymbols() {
        return unreachableSymbols;
    }

    /**
     * Get the unreachable symbols.
     * @return the unreachable symbols.
     */
    public Set<NonterminalSymbol> getUndefinedSymbols() {
        return undefinedSymbols;
    }

    /**
     * Did the dk.brics.grammar.ambiguity analyzer run successfully?
     * @return true, if the analyzer ran successfully
     */
    public boolean ambiguityChecked() {
        return ambiguityReport.getCheckSucceeded();
    }

    /**
     * Did the dk.brics.grammar.ambiguity analyzer report the grammar unambigous?
     * @return true, if the grammar is unambiguous
     */
    public boolean provablyUnambiguous() {
        return ambiguityReport.getUnambiguous();
    }

    /**
     * Did the dk.brics.grammar.ambiguity analyzer run reliably?
     * @return true, if the analyzer detected no uncheckable characters or character classes
     */
    public boolean reliablyUnambiguous() {
        return ambiguityReport.getReliable();
    }

    /**
     * Get the dk.brics.grammar.ambiguity analyzer report.
     * @return the report, or null if no report is available
     */
    public String getAmbiguityReport() {
        return ambiguityReport.getAmbiguityReport();
    }

    protected void addUnreachable(NonterminalSymbol symbol) {
        if (unreachableSymbols.contains(symbol)) {
            return;
        }

        unreachableSymbols.add(symbol);
        if (parserGrammar != null) {
            parserGrammar.getParserOptions().getLogger().warn(logcategory, "Unreachable symbol: %s", symbol);
        }
    }

    protected void addUndefined(NonterminalSymbol symbol) {
        if (undefinedSymbols.contains(symbol)) {
            return;
        }

        undefinedSymbols.add(symbol);
        if (parserGrammar != null) {
            parserGrammar.getParserOptions().getLogger().warn(logcategory, "Undefined symbol: %s", symbol);
        }
    }

    protected void addUnproductive(NonterminalSymbol symbol) {
        if (unproductiveSymbols.contains(symbol)) {
            return;
        }

        unproductiveSymbols.add(symbol);
        if (parserGrammar != null) {
            parserGrammar.getParserOptions().getLogger().warn(logcategory, "Unproductive symbol: %s", symbol);
        }
    }

    protected void addUnproductive(Rule rule) {
        if (unproductiveRules.contains(rule)) {
            return;
        }

        unproductiveRules.add(rule);
        if (parserGrammar != null) {
            parserGrammar.getParserOptions().getLogger().warn(logcategory, "Unproductive rule: %s", rule);
        }
    }
}
