package org.nineml.coffeegrinder.parser;

/**
 * The ProgressMonitor allows you to track the progress of a parse.
 * <p>For each parse, the {@link EarleyParser} will call <code>starting</code>
 * just before the parse begins, <code>progress</code> periodically during the
 * parse, and <code>finished</code> when the parse is complete.</p>
 */
public interface ProgressMonitor {
    /**
     * Indicates that the parse is about to begin.
     * <p>The value returned is a measure of the update frequency. The <code>progress</code>
     * method will be called every time that many tokens has been processed. A value of
     * zero (or a negative value) will disable the <code>progress</code> callbacks, but
     * <code>finished</code> will still be called.</p>
     * @param parser the parser
     * @param tokens the total size of the input
     * @return the update frequency
     */
    int starting(GearleyParser parser, int tokens);

    /**
     * Indicates progress in the parse.
     * @param parser the parser
     * @param tokens the number of tokens processed so far.
     */
    void progress(GearleyParser parser, int tokens);

    /**
     * Indicates progress in the parse.
     * @param parser the parser
     * @param size the number of items left to process.
     * @param highwater the last token seen.
     */
    void workingSet(GearleyParser parser, int size, int highwater);

    /**
     * Indicates that the parse has finished.
     * @param parser the parser
     */
    void finished(GearleyParser parser);
}
