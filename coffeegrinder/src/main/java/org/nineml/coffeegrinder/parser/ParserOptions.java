package org.nineml.coffeegrinder.parser;

import org.nineml.logging.DefaultLogger;
import org.nineml.logging.Logger;

/**
 * Options to the parser.
 * <p>This object is extended by other members of the NineML family to provide additional options.
 * It started out as a collection of public fields, but changed to a more traditional collection of
 * getters and setters when it began to develop options that were not entirely independent.</p>
 */
public class ParserOptions {
    private Logger logger;
    private String parserType = "Earley";
    private boolean returnChart = false;
    private boolean prefixParsing = false;
    private String priorityStyle = "max";
    private ProgressMonitor monitor = null;
    private boolean normalizeLineEndings = false;
    private boolean markAmbiguities = false;

    /**
     * Create the parser options.
     * <p>The initial logger will be a {@link DefaultLogger} initialized with
     * {@link DefaultLogger#readSystemProperties readSystemProperties()}.</p>
     */
    public ParserOptions() {
        logger = new DefaultLogger();
        logger.readSystemProperties();

        String ptype = System.getProperty("org.nineml.coffeegrinder.parser", "Earley");
        try {
            setParserType(ptype);
        } catch (IllegalArgumentException ex) {
            logger.error("Grammar", "Invalid parser type in org.nineml.coffeegrinder.parser system property: " + ptype);
        }
    }

    /**
     * Create the parser options with an explicit logger.
     * @param logger the logger.
     */
    public ParserOptions(Logger logger) {
        this.logger = logger;
    }

    /**
     * Create a new set of options from an existing set.
     * <p>Beware that the logger and monitor are not copied, so the copied options have pointers to the
     * same logger and monitor instances.</p>
     * @param copy the options to copy
     */
    public ParserOptions(ParserOptions copy) {
        logger = copy.logger;
        parserType = copy.parserType;
        returnChart = copy.returnChart;
        prefixParsing = copy.prefixParsing;
        monitor = copy.monitor;
        priorityStyle = copy.priorityStyle;
        normalizeLineEndings = copy.normalizeLineEndings;
        markAmbiguities = copy.markAmbiguities;
    }

    /**
     * Return the default parser type.
     * @return The default parser type, "Earley" or "GLL"
     */
    public String getParserType() {
        return parserType;
    }

    /**
     * Set the default parser type.
     * @param parserType the parser type, "Earley" or "GLL"
     * @throws IllegalArgumentException if the parser type is not recognized
     */
    public void setParserType(String parserType) {
        if ("Earley".equals(parserType) || "GLL".equals(parserType)) {
            this.parserType = parserType;
        } else {
            throw new IllegalArgumentException("Unrecognized parser type: " + parserType);
        }
    }

    /**
     * Return the Earley chart even for a successful parse?
     * @return true if the Earley chart should be returned even for an unsuccessful parse
     */
    public boolean getReturnChart() {
        return returnChart;
    }

    /**
     * Set the {@link #getReturnChart()} property.
     * @param returnChart return the chart?
     */
    public void setReturnChart(boolean returnChart) {
        this.returnChart = returnChart;
    }

    /**
     * If a parse fails, but some prefix of the input was successfully parsed, make that available.
     *
     * <p>This is optional mostly because it requires internally buffering some of the input tokens.
     * (Probably no more than two, but I haven't tried to prove that.)</p>
     *
     * @return true if prefix parsing is enabled.
     */
    public boolean getPrefixParsing() {
        return prefixParsing;
    }

    /**
     * Set the {@link #getPrefixParsing()} property.
     * @param prefixParsing prefix parsing?
     */
    public void setPrefixParsing(boolean prefixParsing) {
        this.prefixParsing = prefixParsing;
    }

    /**
     * Return prunable nonterminals in parse trees?
     * <p>If true, prunable nonterminals will be returned. Depending on how your grammar is
     * defined, this may lead to much larger memory allocation when extracting trees from
     * the forest.</p>
     *
     * @return true if prunable nonterminals will be in parse trees.
     */

    /**
     * The parser logger.
     * <p>The logger controls what messages are issued, and how. This component is also used by
     * higher-level components such as CoffeeFilter, CoffeePot, and CoffeeSacks.</p>
     * @return the logger.
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Set the logger.
     * @param logger the logger.
     * @throws NullPointerException if the logger is null.
     */
    public void setLogger(Logger logger) {
        if (logger == null) {
            throw new NullPointerException("Logger must not be null");
        }
        this.logger = logger;
    }

    /**
     * The progress monitor.
     * <p>If this option is not null, the monitor will be called before, during, and after
     * the parse.</p>
     * @return the monitor
     */
    public ProgressMonitor getProgressMonitor() {
        return monitor;
    }

    /**
     * Set the progress monitor.
     * <p>Setting the monitor to <code>null</code> disables monitoring.</p>
     * @param monitor the monitor.
     */
    public void setProgressMonitor(ProgressMonitor monitor) {
        this.monitor = monitor;
    }

    /**
     * The priority style.
     * <p>Priorities can be computed on one of two styles: "<code>max</code>" or "<code>sum</code>".</p>
     * <p>If the priority style is "<code>max</code>", the priority of any given node is the highest priority
     * value in the subgraph rooted at the current node.</p>
     * <p>If the priority style is "<code>sum</code>", the priority of any given node is the sum of the priorities
     * of the nodes in the subgraph rooted at the current node.</p>
     * @return the priority style
     */
    public String getPriorityStyle() {
        return priorityStyle;
    }

    /**
     * Set the priority style.
     * <p>The style can be "<code>max</code>" or "<code>sum</code>". The default is "<code>max</code>".</p>
     * @param style the priority style.
     */
    public void setPriorityStyle(String style) {
        if ("max".equals(style) || "sum".equals(style)) {
            this.priorityStyle = style;
        } else {
            throw new IllegalArgumentException("Unrecognized priority style: " + style);
        }
    }

    /**
     * Should line endings be normalized?
     * <p>If line endings are normalized, all occurrences of #D, #D#A, #85, and #2028 in the input
     * string are replaced with a single #A. This only applies to sequences of characters
     * in the input.</p>
     * @return true if line endings will be normalized
     */
    public boolean getNormalizeLineEndings() {
        return normalizeLineEndings;
    }

    /**
     * Set normalize line endings
     * <p>If line endings are normalized, all occurrences of #D, #D#A, #85, and #2028 in the input
     * string are replaced with a single #A. This only applies to sequences of characters
     * in the input.</p>
     * @param normalize true if line endings should be normalized
     */
    public void setNormalizeLineEndings(boolean normalize) {
        normalizeLineEndings = normalize;
    }

    /**
     * Are individual ambiguities be marked?
     * @return true if ambiguities will be normalized
     */
    public boolean getMarkAmbiguities() {
        return markAmbiguities;
    }

    /**
     * Set ambiguity marking.
     * @param mark true if individual ambiguities should be marked.
     */
    public void setMarkAmbiguities(boolean mark) {
        markAmbiguities = mark;
    }
}
