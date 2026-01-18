package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.util.BricsAmbiguity;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/** An ambiguity report.
 * <p>This is a report derived from the
 * <a href="https://www.brics.dk/grammar.html">dk.brics.grammar</a> ambiguity analyzer.</p>
 */
public class AmbiguityReport {
    final Grammar grammar;
    final NonterminalSymbol seed;
    final BricsAmbiguity ambiguityChecker;
    private String ambiguityReport = null;

    /**
     * Construct a report for the specified grammar.
     * @param grammar The grammar.
     */
    protected AmbiguityReport(ParserGrammar grammar) {
        this.grammar = grammar;
        this.seed = grammar.getSeed();
        ambiguityChecker = new BricsAmbiguity();
    }

    /**
     * Construct a report for the specified grammar with a particular starting seed.
     * @param grammar The grammar.
     * @param seed The seed.
     */
    protected AmbiguityReport(SourceGrammar grammar, NonterminalSymbol seed) {
        this.grammar = grammar;
        this.seed = seed;
        ambiguityChecker = new BricsAmbiguity();
    }

    /**
     * Is the report reliable?
     * <p>Some grammars cannot be reliably analyzed.</p>
     * @return true if the report is reliable.
     */
    public boolean getReliable() {
        return ambiguityChecker.getReliable();
    }

    /**
     * Is the grammar unambiguous?
     * @return True if the grammar is unambiguous.
     */
    public boolean getUnambiguous() {
        return ambiguityChecker.getUnambiguous();
    }

    /**
     * Did the check succeed?
     * @return true if the check was successful.
     */
    public boolean getCheckSucceeded() {
        return ambiguityChecker.getCheckSucceeded();
    }

    /**
     * Get the report.
     * <p>The report is the text output from the brics analyzer.</p>
     * @return The report.
     */
    public String getAmbiguityReport() {
        return ambiguityReport;
    }

    /**
     * Run the checker.
     */
    public void check() {
        ByteArrayOutputStream reportbytes = new ByteArrayOutputStream();
        PrintWriter report = new PrintWriter(reportbytes);
        ambiguityChecker.checkGrammar(grammar, seed, report);
        report.close();
        try {
            ambiguityReport = reportbytes.toString("utf-8");
        } catch (UnsupportedEncodingException ex) {
            // This can't happen.
            throw new RuntimeException(ex);
        }
    }



}
