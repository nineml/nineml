package org.nineml.coffeegrinder.parser;

import java.util.*;

/**
 * The grammar representation.
 * <p>This is an abstract class with two implementations: {@link SourceGrammar} and {@link ParserGrammar}.
 * The source grammar can be updated, the parser grammar is immutable.</p>
 */
public abstract class Grammar {
    public static final String logcategory = "Grammar";

    protected final ArrayList<Rule> rules = new ArrayList<>();
    protected final HashMap<NonterminalSymbol, List<Rule>> rulesBySymbol = new HashMap<>();
    protected final HashMap<String,String> metadata = new HashMap<>();

    /**
     * Get the rules currently defined in this grammar.
     * <p>For a {@link ParserGrammar}, this is the final set.</p>
     * @return Returns the rules that define the grammar.
     */
    public List<Rule> getRules() {
        return rules;
    }

    /**
     * Get the currently defined nonterminals in the grammar.
     * <p>For a {@link ParserGrammar}, this is the final set.</p>
     * @return the set of nonterminals.
     */
    public Set<NonterminalSymbol> getSymbols() {
        return rulesBySymbol.keySet();
    }

    /**
     * Get the rules currently defined in this grammar organized by symbol.
     * <p>For a {@link ParserGrammar}, this is the final set.</p>
     * @return Returns the rules that define the grammar.
     */
    public Map<NonterminalSymbol,List<Rule>> getRulesBySymbol() {
        return rulesBySymbol;
    }

    /**
     * Get the rules currently defined in this grammar for a particular symbol.
     * <p>For a {@link ParserGrammar}, this is the final set.</p>
     * @param symbol The symbol.
     * @return Returns the rules that define the grammar.
     */
    public List<Rule> getRulesForSymbol(NonterminalSymbol symbol) {
        return rulesBySymbol.getOrDefault(symbol, null);
    }

    /**
     * Gets a metadata property.
     * <p>Metadata properties exist solely for annotations by an application.</p>
     * @param name the name of the property
     * @return the value of the property, or null if no such property exists
     * @throws NullPointerException if the name is null
     */
    public String getMetadataProperty(String name) {
        if (name == null) {
            throw new NullPointerException("Name must not be null");
        }
        return metadata.getOrDefault(name, null);
    }

    /**
     * Gets the metadata properties.
     * <p>Metadata properties exist solely for annotations by an application.</p>
     * @return the metadata properties
     */
    public Map<String,String> getMetadataProperies() {
        return metadata;
    }

    /**
     * Is the symbol nullable?
     * <p>A {@link TerminalSymbol} is never nullable.</p>
     * <p>For a {@link ParserGrammar}, the answer is definitive. For an {@link SourceGrammar},
     * a symbol that isn't currently nullable could become nullable by the addition of more rules.</p>
     * @param symbol The symbol.
     * @return true if the symbol is nullable
     */
    public abstract boolean isNullable(Symbol symbol);
}
