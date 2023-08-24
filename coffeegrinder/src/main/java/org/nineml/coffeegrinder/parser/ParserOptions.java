package org.nineml.coffeegrinder.parser;

import org.nineml.logging.DefaultLogger;
import org.nineml.logging.Logger;

/**
 * Options to the parser.
 * <p>Parser options are extended by other members of the NineML family to provide additional options.
 * This class started out as a collection of public fields, but changed to a more traditional collection of
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
     * Create parser options.
     * <p>The initial logger will be a {@link DefaultLogger} initialized with
     * {@link DefaultLogger#readSystemProperties()}.</p>
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
     * Create parser options with an explicit logger.
     * @param logger the logger.
     */
    public ParserOptions(Logger logger) {
        this.logger = logger;
    }

    /**
     * Create parser options by copying an existing set of options.
     * <p>Note that the logger and monitor are not copied, so the created options share the same
     * logger and progress monitor as the options copied.</p>
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
     * Get the parser type.
     * <p>The parser type will be "Earley" or "GLL".</p>
     * @return the current parser type, "Earley" or "GLL"
     */
    public String getParserType() {
        return parserType;
    }

    /**
     * Set the parser type.
     * <p>You must specify "Earley" or "GLL".</p>
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
     * <p>If this option is enabled, the chart will be returned even for a successful parse.
     * (If the parse fails, the chart is always returned.)</p>
     * <p>The chart can only be returned if the Earley parser is used. The GLL parser doesn't
     * manage the parse with state charts in the same way.</p>
     * @return true if the Earley chart should be returned even for a successful parse.
     */
    public boolean getReturnChart() {
        return returnChart;
    }

    /**
     * Set the return chart property.
     * <p>See {@link #getReturnChart()}.</p>
     * @param returnChart return the chart?
     */
    public void setReturnChart(boolean returnChart) {
        this.returnChart = returnChart;
    }

    /**
     * Is prefix parsing enabled?
     * <p>If an Earley parse fails, but some prefix of the input was successfully parsed,
     * make that available for continuing the parse. This is optional mostly because it requires
     * internally buffering some of the input tokens, but probably no more than a few.</p>
     *
     * @return true if prefix parsing is enabled.
     */
    public boolean getPrefixParsing() {
        return prefixParsing;
    }

    /**
     * Enable prefix parsing?
     * @param prefixParsing true if prefix parsing should be enabled
     */
    public void setPrefixParsing(boolean prefixParsing) {
        this.prefixParsing = prefixParsing;
    }

    /**
     * Get the parser logger.
     * <p>The logger controls what messages are displayed, and how. This component is also used by
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
     * Get the progress monitor.
     * <p>If a progress monitor is present, it will be called before, during, and after
     * the parse.</p>
     * @return the monitor, or null if no monitor is enabled
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
     * Get the priority style.
     * <p>Priorities can be computed on one of two styles: <code>max</code> or <code>sum</code>.</p>
     * <ul>
     *     <li>If the priority style is <code>max</code>, the priority of any given node is the highest priority
     *     value in the subgraph rooted at that node.</li>
     *     <li>If the priority style is <code>sum</code>, the priority of any given node is the sum of the priorities
     *     of the nodes in the subgraph below it.</li>
     * </ul>
     * <p>The default is <code>max</code>.</p>
     * @return the priority style
     */
    public String getPriorityStyle() {
        return priorityStyle;
    }

    /**
     * Set the priority style.
     * <p>The style can be <code>max</code> or <code>sum</code>. </p>
     * @param style the priority style.
     * @throws IllegalArgumentException if the style is unrecognized.
     */
    public void setPriorityStyle(String style) {
        if ("max".equals(style) || "sum".equals(style)) {
            this.priorityStyle = style;
        } else {
            throw new IllegalArgumentException("Unrecognized priority style: " + style);
        }
    }

    /**
     * Normalize line endings?
     * <p>If line endings are normalized, all occurrences of #D, #D#A, #85, and #2028 in the input
     * string are replaced with a single #A. This only applies to sequences of characters
     * in the input.</p>
     * @return true if line endings will be normalized
     */
    public boolean getNormalizeLineEndings() {
        return normalizeLineEndings;
    }

    /**
     * Enable normalizing line endings?
     * <p>See {@link #getNormalizeLineEndings()}.</p>
     * @param normalize true if line endings should be normalized
     */
    public void setNormalizeLineEndings(boolean normalize) {
        normalizeLineEndings = normalize;
    }

    /**
     * Are ambiguities marked?
     * <p>CoffeeGrinder doesn't use this option, but it's defined here so that it will be passed
     * to CoffeeFilter. If ambiguities are to be marked, additional markup (attributes or processing
     * instructions) are included in the forests returned in order to indicate where ambiguous choices
     * were made).</p>
     * @return true if ambiguities will be normalized
     */
    public boolean getMarkAmbiguities() {
        return markAmbiguities;
    }

    /**
     * Shall ambiguities be marked?
     * @param mark true if ambiguities should be marked.
     */
    public void setMarkAmbiguities(boolean mark) {
        markAmbiguities = mark;
    }
}
