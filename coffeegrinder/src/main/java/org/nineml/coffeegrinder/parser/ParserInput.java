package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.exceptions.ParseException;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.tokens.TokenCharacter;
import org.nineml.coffeegrinder.tokens.TokenEOF;

import java.util.ArrayList;
import java.util.Iterator;

public class ParserInput {
    private final boolean usesRegex;
    private final String parserType;
    private final boolean normalizeLineEndings;
    private boolean parsed = false;
    private String inputString = null;
    private Token[] inputTokens = null;

    public ParserInput(ParserOptions options) {
        this(options, false);
    }

    public ParserInput(ParserOptions options, boolean usesRegex) {
        this.parserType = options.getParserType();
        this.normalizeLineEndings = options.getNormalizeLineEndings();
        this.usesRegex = usesRegex;
    }

    public String string() {
        if (!parsed) {
            throw new IllegalStateException("No input from a source");
        }
        return inputString;
    }

    public Token[] tokens() {
        if (!parsed) {
            throw new IllegalStateException("No input from a source");
        }
        return inputTokens;
    }

    public void from(String input) {
        inputString = usesRegex ? input : null;
        int[] codepoints = input.codePoints().toArray();
        if ("GLL".equals(parserType)) {
            inputTokens = new Token[codepoints.length+1];
            inputTokens[codepoints.length] = TokenEOF.EOF;
        } else {
            inputTokens = new Token[codepoints.length];
        }

        for (int pos = 0; pos < codepoints.length; pos++) {
            inputTokens[pos] = TokenCharacter.get(codepoints[pos]);
        }

        parsed = true;
    }

    public void from(Token[] input) {
        if ("GLL".equals(parserType)) {
            inputTokens = new Token[input.length+1];
            inputTokens[input.length] = TokenEOF.EOF;
            for (Token token : input) {
                if (!(token instanceof TokenCharacter)) {
                    throw ParseException.invalidInputForGLL();
                }
            }
        } else {
            inputTokens = new Token[input.length];
        }
        System.arraycopy(input, 0, inputTokens, 0, input.length);

        if (usesRegex) {
            StringBuilder sb = new StringBuilder();
            for (Token token : input) {
                if (token instanceof TokenCharacter) {
                    sb.append(token.getValue());
                } else {
                    throw ParseException.invalidInputForRegex();
                }
            }
            inputString = sb.toString();
        } else {
            inputString = null;
        }

        parsed = true;
    }

    public void from(Iterator<Token> input) {
        boolean requireCharacters = "GLL".equals(parserType);
        StringBuilder sb = new StringBuilder();
        ArrayList<Token> list = new ArrayList<>();
        while (input.hasNext()) {
            Token token = input.next();
            if (requireCharacters && !(token instanceof TokenCharacter)) {
                throw ParseException.invalidInputForGLL();
            }
            if (usesRegex) {
                sb.append(token.getValue());
            }
            list.add(token);
        }
        inputString = usesRegex ? sb.toString() : null;

        if ("GLL".equals(parserType)) {
            inputTokens = new Token[list.size()+1];
            inputTokens[list.size()] = TokenEOF.EOF;
        } else {
            inputTokens = new Token[list.size()];
        }
        for (int pos = 0; pos < list.size(); pos++) {
            inputTokens[pos] = list.get(pos);
        }

        parsed = true;
    }
}
