package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.exceptions.GrammarException;
import org.nineml.coffeegrinder.gll.GllParser;
import org.nineml.coffeegrinder.util.ParserAttribute;

import java.util.*;

/**
 * A grammar for the parser.
 *
 * <p>A grammar is a list of rules. Each rule defines a non-terminal symbol as a sequence of zero or
 * more symbols ({@link TerminalSymbol terminals} or {@link NonterminalSymbol nonterminals}).</p>
 *
 * <p>A grammar can be used to create a parser. The parser will (successfully) parse inputs that
 * match the rules in the grammar.</p>
 */
public class SourceGrammar extends Grammar {
    public static final String logcategory = "Grammar";
    private static final char[] subscripts = {'₀','₁','₂','₃','₄','₅','₆','₇','₈','₉'};

    private static int nextGrammarId = 0;
    private NonterminalSymbol seed = null;
    private final ParserOptions options;
    protected final int id;
    protected final ParserType defaultParserType;

    /**
     * Create a new grammar with default options.
     */
    public SourceGrammar() {
        this(new ParserOptions());
    }

    /**
     * Create a new grammar with a specific set of options.
     * @param options The options.
     * @throws NullPointerException if options is null.
     */
    public SourceGrammar(ParserOptions options) {
        if (options == null) {
            throw new NullPointerException("Null options");
        }
        id = nextGrammarId++;
        this.options = options;
        if ("Earley".equals(options.getParserType())) {
            defaultParserType = ParserType.Earley;
        } else {
            defaultParserType = ParserType.GLL;
        }
        options.getLogger().debug(logcategory, "Created grammar %d", id);
    }

    /**
     * Create a new grammar from an existing grammar.
     * @param current the grammar to copy
     */
    public SourceGrammar(SourceGrammar current) {
        id = nextGrammarId++;
        options = current.options;
        defaultParserType = current.defaultParserType;
        seed = null;
        options.getLogger().debug(logcategory, "Created grammar %d", id);
    }

    /**
     * Return the nonterminal symbol identified by name.
     * <p>Nonterminal symbols are uniquely identified by their name.</p>
     * <p>Any string can be used as a name.</p>
     * @param name The name of this symbol.
     * @return The nonterminal for the name specified.
     * @throws NullPointerException if the name is null.
     */
    public NonterminalSymbol getNonterminal(String name) {
        ArrayList<ParserAttribute> attr = new ArrayList<>();
        return getNonterminal(name, attr);
    }

    /**
     * Return the nonterminal symbol identified by name.
     * <p>Nonterminal symbols are uniquely identified by their name.</p>
     * <p>Any string can be used as a name.</p>
     * @param name The name of this symbol.
     * @param attribute an attribute.
     * @return The nonterminal for the name specified.
     * @throws NullPointerException if the name is null or the attribute is null.
     */
    public NonterminalSymbol getNonterminal(String name, ParserAttribute attribute) {
        if (attribute == null) {
            throw new NullPointerException("Nonterminal symbol attribute must not be null");
        }
        return getNonterminal(name, Collections.singletonList(attribute));
    }

    /**
     * Return the nonterminal symbol identified by name.
     * <p>Nonterminal symbols are uniquely identified by their name.</p>
     * <p>Any string can be used as a name.</p>
     * @param name The name of this symbol.
     * @param attributes attributes to associate with this symbol, may be null
     * @return The nonterminal for the name specified.
     * @throws NullPointerException if the name is null.
     */
    public NonterminalSymbol getNonterminal(String name, List<ParserAttribute> attributes) {
        options.getLogger().trace(logcategory, "Creating nonterminal %s for grammar %d", name, id);
        return new NonterminalSymbol(this, name, attributes);
    }

    /**
     * Add a rule to the grammar.
     * <p>Multiple rules can exist for the same {@link NonterminalSymbol}. There should be at least
     * one rule for every nonterminal symbol that occurs on the "right hand side" of a rule.
     * A symbol that is undefined but unreachable from the specified start symbol when parsing,
     * isn't forbidden by CoffeeGrinder, but it is by CoffeeFilter.
     * </p>
     * <p>Once added, a rule can never be removed.</p>
     * @param rule The rule to add
     * @throws GrammarException if any nonterminal in the rule is not from this grammar, or if the grammar is closed
     */
    public void addRule(Rule rule) {
        if (seed != null) {
            throw GrammarException.grammarIsClosed();
        }
        if (contains(rule)) {
            options.getLogger().trace(logcategory, "Ignoring duplicate rule: %s", rule);
        } else {
            options.getLogger().trace(logcategory, "Adding rule: %s", rule);
            rules.add(rule);
            if (!rulesBySymbol.containsKey(rule.symbol)) {
                rulesBySymbol.put(rule.symbol, new ArrayList<>());
            }
            rulesBySymbol.get(rule.symbol).add(rule);
        }
    }

    /**
     * Add a rule to the grammar.
     * <p>This is a convenience method that will construct the {@link Rule} for you.</p>
     * <p>Multiple rules can exist for the same {@link NonterminalSymbol}. There must be at least
     * one rule for every nonterminal symbol that occurs on the "right hand side" of a rule.</p>
     * @param nonterminal The nonterminal symbol defined by this rule.
     * @param rhs The list of symbols that define it
     * @throws GrammarException if any nonterminal in the rule is not from this grammar or if the grammar is closed
     */
    public void addRule(NonterminalSymbol nonterminal, Symbol... rhs) {
        addRule(new Rule(nonterminal, rhs));
    }

    /**
     * Add a rule to the grammar.
     * <p>This is a convenience method that will construct the {@link Rule} for you.</p>
     * <p>Multiple rules can exist for the same {@link NonterminalSymbol}. There must be at least
     * one rule for every nonterminal symbol that occurs on the "right hand side" of a rule.</p>
     * @param nonterminal The nonterminal symbol defined by this rule.
     * @param rhs The list of symbols that define it.
     * @throws GrammarException if any nonterminal in the rule is not from this grammar or if the grammar is closed
     */
    public void addRule(NonterminalSymbol nonterminal, List<Symbol> rhs) {
        addRule(new Rule(nonterminal, rhs));
    }

    @Override
    public boolean isNullable(Symbol symbol) {
        // We don't have to be too fussy here. The answer isn't definitive for an InputGrammar.
        if (symbol instanceof NonterminalSymbol) {
            if (rulesBySymbol.containsKey(symbol)) {
                for (Rule rule : rulesBySymbol.get(symbol)) {
                    if (rule.rhs.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public GearleyParser getParser(ParserOptions options, String seed) {
        return getParser(options, getNonterminal(seed));
    }

    public ParserGrammar getCompiledGrammar(NonterminalSymbol seed) {
        return new ParserGrammar(this, defaultParserType, seed);
    }

    public ParserGrammar getCompiledGrammar(ParserType parserType, NonterminalSymbol seed) {
        return new ParserGrammar(this, parserType, seed);
    }

    /**
     * Get a parser for this grammar.
     *
     * <p>Returns a parser that will parse an input against the rules that define this grammar.</p>
     *
     * @param options the options for this parser.
     * @param seed The {@link NonterminalSymbol} that your input is expected to match.
     * @return the parser
     */
    public GearleyParser getParser(ParserOptions options, NonterminalSymbol seed) {
        final ParserType parserType;
        if ("Earley".equals(options.getParserType())) {
            parserType = ParserType.Earley;
        } else if ("GLL".equals(options.getParserType())) {
            parserType = ParserType.GLL;
        } else {
            throw new IllegalStateException("Unexpected parser type: " + options.getParserType());
        }

        SourceGrammar sg = resolveDuplicates();

        ParserGrammar compiled = sg.getCompiledGrammar(parserType, seed);
        if (parserType == ParserType.Earley) {
            return new EarleyParser(compiled, options);
        } else {
            return new GllParser(compiled, options);
        }
    }

    /**
     * Resolve duplicate symbols in the grammar.
     *
     * <p>This method may return a different SourceGrammar. In this new grammar, all of
     * the unique nonterminals will have unique names. (That is, if a nonterminal A occurs
     * twice in the grammar with different parser attributes, it will have two different names
     * in the resolved grammar.)</p>
     * <p>This method is public only so that it is possible to know what all of the
     * nonterminals in the parse grammar will be.</p>
     *
     * @return a grammar with duplicates resolved
     */
    public SourceGrammar resolveDuplicates() {
        // Two nonterminals are .equals() to each other if they have the same name. But here,
        // we need to consider whether they have the same attributes.
        HashMap<String, Integer> symbolMap = new HashMap<>();
        HashMap<String, List<ParserAttribute>> symbolAttributes = new HashMap<>();
        HashMap<String, Integer> nameCounts = new HashMap<>();
        HashMap<String, List<Rule>> rulesBySymbol = new HashMap<>();

        // Do all the rules first, so that the LHS nonterminals are the first
        for (Rule rule : rules) {
            String key = symbolKey(rule.symbol);
            if (!symbolMap.containsKey(key)) {
                if (!nameCounts.containsKey(rule.symbol.symbolName)) {
                    nameCounts.put(rule.symbol.symbolName, 0);
                }
                int next = nameCounts.get(rule.symbol.symbolName);
                symbolMap.put(key, next);
                symbolAttributes.put(key, rule.symbol.getAttributes());

                nameCounts.put(rule.symbol.symbolName, next+1);
            }

            if (!rulesBySymbol.containsKey(rule.symbol.symbolName)) {
                rulesBySymbol.put(rule.symbol.symbolName, new ArrayList<>());
            }
            rulesBySymbol.get(rule.symbol.symbolName).add(rule);
        }

        boolean found = false;
        for (Rule rule : rules) {
            for (Symbol anySymbol : rule.getRhs().symbols) {
                if (!(anySymbol instanceof NonterminalSymbol)) {
                    continue;
                }

                NonterminalSymbol symbol = (NonterminalSymbol) anySymbol;
                String key = symbolKey(symbol);
                if (!symbolMap.containsKey(key)) {
                    found = true;
                    if (!nameCounts.containsKey(symbol.symbolName)) {
                        nameCounts.put(symbol.symbolName, 0);
                    }
                    int next = nameCounts.get(symbol.symbolName);
                    symbolMap.put(key, next);
                    symbolAttributes.put(key, symbol.getAttributes());

                    nameCounts.put(symbol.symbolName, next+1);
                }
            }
        }

        SourceGrammar sg = this;
        if (found) {
            sg = new SourceGrammar(options);

            // Now make a list of rules that includes all the original rules and
            // rules for all the new nonterminals
            ArrayList<Rule> newRules = new ArrayList<>(getRules());
            for (String key : symbolMap.keySet()) {
                if (symbolMap.get(key) == 0) {
                    continue;
                }

                int pos = key.indexOf(":");
                String name = key.substring(0, pos);
                NonterminalSymbol ruleSymbol = getNonterminal(symbolId(name, symbolMap.get(key)), symbolAttributes.get(key));
                ruleSymbol.realName = name;

                for (Rule rule : rulesBySymbol.getOrDefault(name, Collections.emptyList())) {
                    newRules.add(new Rule(ruleSymbol, rule.rhs.symbols));
                }
            }

            // Now for each rule, replace any nonterminals on the RHS with the appropriate new symbol
            // and add that rule to the new grammar.
            for (Rule rule : newRules) {
                ArrayList<Symbol> rhs = new ArrayList<>();
                for (Symbol symbol : rule.getRhs().symbols) {
                    if (symbol instanceof NonterminalSymbol) {
                        int count = symbolMap.get(symbolKey((NonterminalSymbol) symbol));
                        if (count == 0) {
                            rhs.add(symbol);
                        } else {
                            String sid = symbolId(((NonterminalSymbol) symbol).symbolName, count);
                            NonterminalSymbol news = sg.getNonterminal(sid, symbol.getAttributes());
                            news.realName = ((NonterminalSymbol) symbol).realName;
                            rhs.add(news);
                        }
                    } else {
                        rhs.add(symbol);
                    }
                }

                sg.addRule(rule.symbol, rhs);
            }
        }

        return sg;
    }

    private String symbolKey(NonterminalSymbol symbol) {
        ArrayList<String> attributes = new ArrayList<>();
        for (ParserAttribute attr : symbol.getAttributes()) {
            attributes.add(attr.getName().replace("=", "%3D")
                    + "="
                    + attr.getValue().replace(";", "%3B"));
        }

        StringBuilder sb = new StringBuilder();
        sb.append(symbol.getName().replace(":", "%3A")).append(":");
        Collections.sort(attributes);
        for (String attr : attributes) {
            sb.append(attr).append(";");
        }

        return sb.toString();
    }

    private String symbolId(String symbolName, int count) {
        int pos = -1;
        for (char ch : subscripts) {
            pos = symbolName.indexOf(ch);
            if (pos >= 0) {
                break;
            }
        }

        StringBuilder number = new StringBuilder();
        if (count > 0) {
            if (pos >= 0) {
                number.append("₍");
            }
            String cstr = String.valueOf(count);
            for (int cpos = 0; cpos < cstr.length(); cpos++) {
                int digit = (int) cstr.charAt(cpos) - (int) '0';
                number.append(subscripts[digit]);
            }
            if (pos >= 0) {
                number.append("₎");
            }
        }

        final String name;
        if (pos >= 0) {
            String newName = symbolName.substring(0, pos);
            newName += subscripts[0];
            name = newName + symbolName.substring(pos) + number;
        } else {
            name = symbolName + number;
        }

        return name;
    }

    /**
     * Gets the parser options.
     * @return the current options.
     */
    public ParserOptions getParserOptions() {
        return options;
    }

    /**
     * Get a hygiene report for this grammar.
     * @param seed The seed rule for hygiene checking.
     * @return the report.
     */
    public HygieneReport getHygieneReport(NonterminalSymbol seed) {
        HygieneReport report = new HygieneReport(this, seed);
        report.checkGrammar();
        return report;
    }

    /**
     * Does this grammar contain an equivalent rule?
     * <p>Two rules are equivalent if they have the same symbol, the same list of right-hand-side
     * symbols, and if the attributes and optionality of every symbol on the right-hand-side are the same in both rules.</p>
     * @param candidate the candidate rule
     * @return true if the grammar contains an equivalent rule
     */
    public boolean contains(Rule candidate) {
        for (Rule rule : rules) {
            if (rule.getSymbol().equals(candidate.getSymbol())) {
                if (rule.getRhs().length == candidate.getRhs().length) {
                    boolean same = true;
                    for (int pos = 0; pos < rule.getRhs().length; pos++) {
                        Symbol symbol = rule.getRhs().get(pos);
                        Symbol csym = candidate.getRhs().get(pos);
                        if (symbol instanceof NonterminalSymbol) {
                            NonterminalSymbol ntsymbol = (NonterminalSymbol) symbol;
                            same = csym instanceof NonterminalSymbol;
                            if (same) {
                                NonterminalSymbol ntcsym = (NonterminalSymbol) csym;
                                same = ntsymbol.getName().equals(ntcsym.getName());
                                if (same) {
                                    Collection<ParserAttribute> a1 = ntsymbol.getAttributes();
                                    Collection<ParserAttribute> a2 = ntcsym.getAttributes();
                                    same = a1.equals(a2);
                                }
                            }
                        } else {
                            same = symbol.equals(csym);
                        }
                        if (!same) {
                            break;
                        }
                    }
                    if (same) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Sets a metadata property.
     * <p>Metadata properties exist solely for annotations by an application. They have
     * no bearing on the function of the grammar.</p>
     * @param name the name of the property
     * @param value the value of the property, or null to remove a property
     * @throws NullPointerException if the name is null
     */
    public void setMetadataProperty(String name, String value) {
        if (name == null) {
            throw new NullPointerException("Name must not be null");
        }
        if (value == null) {
            metadata.remove(name);
        } else {
            metadata.put(name, value);
        }
    }
}
