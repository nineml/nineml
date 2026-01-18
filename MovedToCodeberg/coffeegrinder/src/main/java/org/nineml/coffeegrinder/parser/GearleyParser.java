package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.tokens.Token;

import java.util.Iterator;

/**
 * The parser interface for Earley and GLL parsers.
 */
public interface GearleyParser {
    /** The parser type.
     *
     * @return The parser type.
     */
    ParserType getParserType();

    /** The grammar used by this parser.
     *
     * @return The grammar.
     */
    ParserGrammar getGrammar();

    /** The seed used by this parser.
     *
     * @return The seed token for this parse.
     */
    NonterminalSymbol getSeed();

    /** Parse an array of tokens.
     * <p>Note that the GLL parser only parses character tokens.</p>
     *
     * @param input The array of tokens.
     * @return The parse result.
     */
    GearleyResult parse(Token[] input);

    /** Parse a sequence of tokens provided by an iterator.
     * <p>Note that the GLL parser only parses character tokens.</p>
     *
     * @param input The token iterator.
     * @return The parse result.
     */
    GearleyResult parse(Iterator<Token> input);

    /** Parse a string.
     *
     * @param input The input string.
     * @return The parse result.
     */
    GearleyResult parse(String input);

    /** Returns true of the parse ended without consuming all input.
     *
     * @return true if there was more input.
     */
    boolean hasMoreInput();

    /** Returns the last offset read by the parser.
     *
     * @return the offset.
     */
    int getOffset();

    /** Returns the line number of the last line read by the parser.
     *
     * @return the line number.
     */
    int getLineNumber();

    /** Returns the column number of the last character on the last line read by the parser.
     *
     * @return the column number.
     */
    int getColumnNumber();
}
