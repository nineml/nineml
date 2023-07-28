package org.nineml.coffeegrinder.util;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.grammar.*;
import dk.brics.grammar.ambiguity.AmbiguityAnalyzer;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.parser.Grammar;
import org.nineml.coffeegrinder.tokens.CharacterSet;
import org.nineml.coffeegrinder.tokens.TokenCharacterSet;

import java.io.*;
import java.util.*;

public class BricsAmbiguity {
    private static final HashMap<String, Automaton> classAutomata = new HashMap<>();
    private static final HashSet<String> unreliableClasses = new HashSet<>();
    private HashMap<String, ArrayList<CodepointRange>> codepoints = null;
    private boolean reliable = false;
    private boolean unambiguous = false;
    private boolean checkSucceeded = false;

    public boolean getReliable() {
        return reliable;
    }

    public boolean getUnambiguous() {
        return unambiguous;
    }

    public boolean getCheckSucceeded() {
        return checkSucceeded;
    }

    public void checkGrammar(Grammar grammar, NonterminalSymbol seed, PrintWriter report) {
        reliable = true;
        unambiguous = true;
        checkSucceeded = true;

        try {
            HashMap<NonterminalSymbol, NonterminalEntity> ntEntities = new HashMap<>();
            HashMap<TerminalSymbol, TerminalEntity> tEntities = new HashMap<>();

            Map<NonterminalSymbol, List<Rule>> ruleMap = grammar.getRulesBySymbol();
            for (NonterminalSymbol nt : ruleMap.keySet()) {
                ntEntities.put(nt, new NonterminalEntity(nt.getName(), nt.getName(), null));
            }

            ArrayList<Production> productions = new ArrayList<>();
            for (Rule rule : grammar.getRules()) {
                ArrayList<Entity> rhs = new ArrayList<>();
                for (Symbol symbol : rule.rhs.symbols) {
                    if (symbol instanceof NonterminalSymbol) {
                        rhs.add(ntEntities.get(symbol));
                    } else {
                        TerminalSymbol ts = (TerminalSymbol) symbol;
                        if (!tEntities.containsKey(ts)) {
                            if (ts.getToken() instanceof TokenCharacterSet) {
                                // Let's turn character sets into regular expressions.
                                Automaton auto = new Automaton();
                                for (CharacterSet cset : ((TokenCharacterSet) ts.getToken()).getCharacterSets()) {
                                    final Automaton next;
                                    if (cset.isRange()) {
                                        reliable = reliable && cset.getRangeFrom() < 65536 && cset.getRangeTo() < 65536;
                                        next = Automaton.makeCharRange((char) Math.min(cset.getRangeFrom(), 65535), (char) Math.min(cset.getRangeTo(), 65535));
                                    } else if (cset.isSetOfCharacters()) {
                                        checkReliability(cset.getCharacters());
                                        next = BasicAutomata.makeCharSet(cset.getCharacters());
                                    } else if (cset.isUnicodeCharacterClass()) {
                                        reliable = reliable && !unreliableClasses.contains(cset.getUnicodeCharacterClass());
                                        next = getAutomaton(cset.getUnicodeCharacterClass());
                                    } else {
                                        throw new IllegalStateException("Impossible character set: " + cset);
                                    }
                                    auto = auto.union(next);
                                }
                                if (!((TokenCharacterSet) ts.getToken()).isInclusion()) {
                                    auto = auto.complement();
                                }
                                tEntities.put(ts, new RegexpTerminalEntity(auto, false, null, null, null));
                            } else {
                                checkReliability(ts.getToken().getValue());
                                tEntities.put(ts, new StringTerminalEntity(ts.getToken().getValue()));
                            }
                        }
                        rhs.add(tEntities.get(symbol));
                    }
                }
                productions.add(new Production(rule.getSymbol().getName(), rhs, false, new ProductionID(), 0));
            }

            dk.brics.grammar.Grammar bricsGrammar = new dk.brics.grammar.Grammar(seed.getName(), productions);
            AmbiguityAnalyzer analyzer = new AmbiguityAnalyzer(report, false);
            unambiguous = analyzer.analyze(bricsGrammar);
        } catch (NoClassDefFoundError ncdf) {
            reliable = false;
            unambiguous = false;
            checkSucceeded = false;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void checkReliability(String str) {
        if (reliable) {
            for (int offset = 0; offset < str.length(); ) {
                int codepoint = str.codePointAt(offset);
                if (codepoint > 0xFFFF) {
                    reliable = false;
                    return;
                }
                offset += Character.charCount(codepoint);
            }
        }
    }

    private void loadUnicodeData() {
        codepoints = new HashMap<>();
        String rsrcname = "/org/nineml/coffeegrinder/UnicodeData.txt";
        try {
            InputStream stream = BricsAmbiguity.class.getResourceAsStream(rsrcname);
            assert stream != null;
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String pname = null;
            int pcodepoint = -1;
            String line = reader.readLine();
            while (line != null) {
                String[] fields = line.split(";");
                int codepoint = Integer.parseInt(fields[0], 16);
                String name = fields[1];
                String uclass = fields[2];

                if (codepoint <= 65535) {
                    if (pcodepoint+1 != codepoint && pname != null && pname.contains("First>") && name.contains("Last>")) {
                        for (int rcp = pcodepoint+1; rcp <= codepoint; rcp++) {
                            addCodepoint(uclass, rcp);
                        }
                    } else {
                        addCodepoint(uclass, codepoint);
                    }
                } else {
                    unreliableClasses.add(uclass);
                }

                pcodepoint = codepoint;
                pname = name;
                line = reader.readLine();
            }

            // https://www.unicode.org/reports/tr44/#General_Category_Values
            ArrayList<CodepointRange> range = new ArrayList<>();
            for (String uclass : new String[] { "Lu", "Ll", "Lt" }) {
                range.addAll(codepoints.get(uclass));
                if (unreliableClasses.contains(uclass)) {
                    unreliableClasses.add("LC");
                }
            }
            codepoints.put("LC", new ArrayList<>(range));

            if (unreliableClasses.contains("LC")) {
                unreliableClasses.add("L");
            }
            for (String uclass : new String[] { "Lm", "Lo" }) {
                range.addAll(codepoints.get(uclass));
                if (unreliableClasses.contains(uclass)) {
                    unreliableClasses.add("L");
                }
            }
            codepoints.put("L", range);

            range = new ArrayList<>();
            for (String uclass : new String[] { "Mn", "Mc", "Me" }) {
                range.addAll(codepoints.get(uclass));
                if (unreliableClasses.contains(uclass)) {
                    unreliableClasses.add("M");
                }
            }
            codepoints.put("M", range);

            range = new ArrayList<>();
            for (String uclass : new String[] { "Nd", "Nl", "No" }) {
                range.addAll(codepoints.get(uclass));
                if (unreliableClasses.contains(uclass)) {
                    unreliableClasses.add("N");
                }
            }
            codepoints.put("N", range);

            range = new ArrayList<>();
            for (String uclass : new String[] { "Pc", "Pd", "Ps", "Pe", "Pi", "Pf", "Po" }) {
                range.addAll(codepoints.get(uclass));
                if (unreliableClasses.contains(uclass)) {
                    unreliableClasses.add("P");
                }
            }
            codepoints.put("P", range);

            range = new ArrayList<>();
            for (String uclass : new String[] { "Sm", "Sc", "Sk", "So" }) {
                range.addAll(codepoints.get(uclass));
                if (unreliableClasses.contains(uclass)) {
                    unreliableClasses.add("S");
                }
            }
            codepoints.put("S", range);

            range = new ArrayList<>();
            for (String uclass : new String[] { "Zs", "Zl", "Zp" }) {
                range.addAll(codepoints.get(uclass));
                if (unreliableClasses.contains(uclass)) {
                    unreliableClasses.add("Z");
                }
            }
            codepoints.put("Z", range);

            range = new ArrayList<>();
            for (String uclass : new String[] { "Cc", "Cf", "Cs", "Co" }) {
                range.addAll(codepoints.get(uclass));
                if (unreliableClasses.contains(uclass)) {
                    unreliableClasses.add("C");
                }
            }
            if (codepoints.containsKey("Cn")) {
                range.addAll(codepoints.get("Cn")); // Unassigned
                if (unreliableClasses.contains("Cn")) {
                    unreliableClasses.add("C");
                }
            }
            codepoints.put("C", range);
        } catch (IOException err) {
            throw new RuntimeException(err);
        }

        for (String uclass : codepoints.keySet()) {
            Automaton auto = new Automaton();
            for (CodepointRange range : codepoints.get(uclass)) {
                final Automaton next;
                if (range.first == range.last) {
                    next = Automaton.makeChar((char) range.first);
                } else {
                    next = Automaton.makeCharRange((char) range.first, (char) range.last);
                }
                auto = auto.union(next);
            }
            auto.reduce();
            classAutomata.put(uclass, auto);
        }

        codepoints = null;
    }

    private synchronized void checkUnicode() {
        if (classAutomata.isEmpty()) {
            loadUnicodeData();
        }
    }

    public Automaton getAutomaton(String charClass) {
        checkUnicode();
        if (!classAutomata.containsKey(charClass)) {
            throw new IllegalArgumentException("Invalid character class: " + charClass);
        }
        return classAutomata.get(charClass);
    }

    private void addCodepoint(String uclass, int cp) {
        if (!codepoints.containsKey(uclass)) {
            ArrayList<CodepointRange> ranges = new ArrayList<>();
            ranges.add(new CodepointRange(cp));
            codepoints.put(uclass, ranges);
            return;
        }

        ArrayList<CodepointRange> ranges = codepoints.get(uclass);
        CodepointRange range = ranges.get(ranges.size() - 1);
        if (range.last+1 == cp) {
            range.last = cp;
            return;
        }

        range = new CodepointRange(cp);
        ranges.add(range);
    }

    private static class CodepointRange {
        public int first, last;
        public CodepointRange(int first) {
            this.first = first;
            this.last = first;
        }
        public CodepointRange(int first, int last) {
            this.first = first;
            this.last = last;
        }

        @Override
        public String toString() {
            return String.format("\\u%04x-\\u%04x", this.first, this.last);
        }
    }
}
