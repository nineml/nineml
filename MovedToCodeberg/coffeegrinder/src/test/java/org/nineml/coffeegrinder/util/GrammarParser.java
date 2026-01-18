package org.nineml.coffeegrinder.util;

import org.nineml.coffeegrinder.parser.SourceGrammar;
import org.nineml.coffeegrinder.parser.NonterminalSymbol;
import org.nineml.coffeegrinder.parser.Symbol;
import org.nineml.coffeegrinder.parser.TerminalSymbol;
import org.nineml.coffeegrinder.tokens.CharacterSet;
import org.nineml.coffeegrinder.tokens.TokenCharacterSet;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Cheap and cheerful parser to make grammars from a text representation.
// There's nothing standard here, or intended to be used elsewhere, it's
// just a time saver in construtcting test grammars.
public class GrammarParser {
    private static Pattern rnt = Pattern.compile("^([-a-zA-Z_\\|][-a-zA-Z_0-9]*)(\\s*,)?(.*)$");
    private static Pattern rcs = Pattern.compile("^(~?)\\[([^\\]]+)\\](\\s*,)?(.*)$");
    private static Pattern rhex = Pattern.compile("^#([0-9a-fA-F]+)(\\s*,)?(.*)$");
    private static Pattern rclass = Pattern.compile("^([A-Z][a-z]?)\\s*(.*)$");

    private SourceGrammar grammar;
    private String line;

    public SourceGrammar parse(String input) {
        grammar = new SourceGrammar();

        // Break it into lines
        for (String line : input.split("\\n+")) {
            int pos = 0;

            line = line.trim();
            if ("".equals(line)) {
                continue;
            }

            NonterminalSymbol nonterminal = null;
            String rest = null;
            if (line.contains("=>")) {
                pos = line.indexOf("=>");
                String nts = line.substring(0, pos).trim();
                if (nts.startsWith("-") || nts.startsWith("^") || nts.startsWith("@")) {
                    nts = nts.substring(1);
                }
                nonterminal = grammar.getNonterminal(nts);
                rest = line.substring(pos+2).trim();
            } else {
                throw new RuntimeException("Unparseable: " + line);
            }

            List<Symbol> rhs = tokenize(rest);
            grammar.addRule(nonterminal, rhs);
        }

        return grammar;
    }

    public SourceGrammar parseFile(String filename) {
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(filename))));
            String line = reader.readLine();
            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = reader.readLine();
            }
            return parse(sb.toString());
        } catch (IOException err) {
            throw new RuntimeException(err);
        }
    }

    private List<Symbol> tokenize(String grammarLine) {
        line = grammarLine;

        ArrayList<Symbol> tokens = new ArrayList<>();
        Symbol symbol = null;
        line = line.trim();
        while (line != null && !"".equals(line)) {
            Matcher match = rnt.matcher(line);
            if (match.matches()) {
                ArrayList<ParserAttribute> attrs = new ArrayList<>();
                symbol = grammar.getNonterminal(match.group(1), attrs);
                tokens.add(symbol);
                line = match.group(3);
                if (line != null) { line = line.trim(); }
                continue;
            }

            match = rcs.matcher(line);
            if (match.matches()) {
                symbol = parseCharset();
                if (line.startsWith("?")) {
                    line = line.substring(1);
                }
                if (line.trim().startsWith(",")) {
                    line = line.trim().substring(1).trim();
                }
                tokens.add(symbol);
                continue;
            }

            match = rhex.matcher(line);
            if (match.matches()) {
                StringBuilder sb = new StringBuilder();
                sb.appendCodePoint(Integer.parseInt(match.group(1), 16));
                symbol = TerminalSymbol.ch(sb.toString().charAt(0));
                tokens.add(symbol);
                line = match.group(3);
                if (line != null) { line = line.trim(); }
                continue;
            }

            if (line.startsWith("\"") || line.startsWith("'")) {
                symbol = TerminalSymbol.s(parseString());
                if (line.trim().startsWith(",")) {
                    line = line.trim().substring(1).trim();
                }
                tokens.add(symbol);
                continue;
            }

            throw new RuntimeException("Unparseable: " + line + "... (" + grammarLine + ")");
        }

        return tokens;
    }

    private String parseString() {
        String origLine = line;
        char quote = line.charAt(0);
        StringBuilder sb = new StringBuilder();
        line = line.substring(1);
        while (!"".equals(line)) {
            if (line.charAt(0) == quote) {
                if (line.length() > 1 && line.charAt(1) == quote) {
                    sb.append(quote);
                    line = line.substring(2);
                } else {
                    line = line.substring(1).trim();
                    return sb.toString();
                }
            } else {
                sb.append(line.charAt(0));
                line = line.substring(1);
            }
        }
        throw new RuntimeException("Failed to parse string: " + origLine);
    }

    private TerminalSymbol parseCharset() {
        boolean negated = false;
        if (line.startsWith("~")) {
            negated = true;
            line = line.substring(1);
        }
        if (!line.startsWith("[")) {
            throw new RuntimeException("Charset doesn't begin with '['?");
        }
        line = line.substring(1).trim();
        ArrayList<CharacterSet> sets = new ArrayList<>();
        boolean done = false;
        while (!done) {
            sets.add(scanCharset());
            line = line.trim();
            if (line.startsWith("]")) {
                done = true;
                line = line.substring(1).trim();
            } else if (line.startsWith(";")) {
                line = line.substring(1).trim();
            } else {
                throw new RuntimeException("Unparseable charset: " + line);
            }
        }

        ArrayList<ParserAttribute> attrs = new ArrayList<>();

        if (negated) {
            return new TerminalSymbol(TokenCharacterSet.exclusion(sets), attrs);
        } else {
            return new TerminalSymbol(TokenCharacterSet.inclusion(sets), attrs);
        }
    }

    private CharacterSet scanCharset() {
        if (line.startsWith("\"") || line.startsWith("'")) {
            String start = parseString();
            if (line.startsWith("-")) {
                line = line.substring(1).trim();
                if (line.startsWith("\"") || line.startsWith("'")) {
                    String end = parseString();
                    if (start.length() == 1 && end.length() == 1) {
                        return CharacterSet.range(start.charAt(0), end.charAt(0));
                    }
                } else {
                    throw new RuntimeException("Unparsable character set: " + line);
                }
            } else {
                return CharacterSet.literal(start);
            }
        }

        Matcher match = rclass.matcher(line);
        if (match.matches()) {
            line = match.group(2);
            return CharacterSet.unicodeClass(match.group(1));
        }

        match = rhex.matcher(line);
        if (match.matches()) {
            String digits = match.group(1);
            line = match.group(4).trim();
            int cp = Integer.parseInt(digits, 16);
            return CharacterSet.range(cp, cp);
        }

        throw new RuntimeException("Unparsable character set: " + line);
    }
}
