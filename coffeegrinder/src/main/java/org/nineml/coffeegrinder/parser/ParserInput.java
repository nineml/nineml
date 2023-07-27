package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.exceptions.ParseException;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.tokens.TokenCharacter;
import org.nineml.coffeegrinder.tokens.TokenEOF;

import java.util.ArrayList;
import java.util.Iterator;

public class ParserInput {
    public static final int CR = 0x000D;
    public static final int LF = 0x000A;
    public static final int NEL = 0x0085;
    public static final int LINE_SEPARATOR = 0x2028;

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
        int[] codepoints = input.codePoints().toArray();
        if ("GLL".equals(parserType)) {
            inputTokens = new Token[codepoints.length+1];
            inputTokens[codepoints.length] = TokenEOF.EOF;
        } else {
            inputTokens = new Token[codepoints.length];
        }

        if (normalizeLineEndings) {
            StringBuilder sb = new StringBuilder();
            int outpos = 0;
            boolean skipLF = false;
            for (int codepoint : codepoints) {
                if (codepoint == LF && skipLF) {
                    skipLF = false;
                    continue;
                }

                if (codepoint == CR || codepoint == NEL || codepoint == LINE_SEPARATOR) {
                    skipLF = (codepoint == CR);
                    codepoint = LF;
                }

                if (usesRegex) {
                    sb.append(Character.toChars(codepoint));
                }
                inputTokens[outpos] = TokenCharacter.get(codepoint);
                outpos++;
            }

            if (usesRegex) {
                inputString = sb.toString();
            }

            if (outpos < codepoints.length) {
                inputTokens = copyTokens(inputTokens, outpos);
            }
        } else {
            inputString = usesRegex ? input : null;
            for (int pos = 0; pos < codepoints.length; pos++) {
                inputTokens[pos] = TokenCharacter.get(codepoints[pos]);
            }
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

        if (normalizeLineEndings) {
            StringBuilder sb = new StringBuilder();
            int outpos = 0;
            boolean skipLF = false;
            for (Token token : input) {
                if (token instanceof TokenCharacter) {
                    int codepoint = ((TokenCharacter) token).getCodepoint();
                    if (codepoint == LF && skipLF) {
                        skipLF = false;
                        continue;
                    }

                    if (codepoint == CR || codepoint == NEL || codepoint == LINE_SEPARATOR) {
                        skipLF = (codepoint == CR);
                        codepoint = LF;
                    }

                    inputTokens[outpos] = TokenCharacter.get(codepoint);
                    if (usesRegex) {
                        sb.append(Character.toChars(codepoint));
                    }
                    outpos++;
                } else {
                    skipLF = false;
                    inputTokens[outpos] = token;
                    outpos++;
                    if (usesRegex) {
                        throw ParseException.invalidInputForRegex();
                    }
                }
            }

            if (usesRegex) {
                inputString = sb.toString();
            }
            if (outpos < input.length) {
                inputTokens = copyTokens(inputTokens, outpos);
            }
        } else {
            System.arraycopy(input, 0, inputTokens, 0, input.length);
        }

        if (usesRegex) {
            if (!normalizeLineEndings) {
                StringBuilder sb = new StringBuilder();
                for (Token token : input) {
                    if (token instanceof TokenCharacter) {
                        sb.append(token.getValue());
                    } else {
                        throw ParseException.invalidInputForRegex();
                    }
                }
                inputString = sb.toString();
            }
        } else {
            inputString = null;
        }

        parsed = true;
    }

    public void from(Iterator<Token> input) {
        boolean requireCharacters = "GLL".equals(parserType);
        StringBuilder sb = new StringBuilder();
        ArrayList<Token> list = new ArrayList<>();
        boolean skipLF = false;
        while (input.hasNext()) {
            Token token = input.next();
            if (token instanceof TokenCharacter) {
                int codepoint = ((TokenCharacter) token).getCodepoint();
                if (normalizeLineEndings) {
                    if (codepoint == LF && skipLF) {
                        skipLF = false;
                        continue;
                    }

                    if (codepoint == CR || codepoint == NEL || codepoint == LINE_SEPARATOR) {
                        skipLF = (codepoint == CR);
                        codepoint = LF;
                    }
                    list.add(TokenCharacter.get(codepoint));
                } else {
                    list.add(token);
                }

                if (usesRegex) {
                    sb.append(Character.toChars(codepoint));
                }
            } else {
                if (requireCharacters) {
                    throw ParseException.invalidInputForGLL();
                }
                if (usesRegex) {
                    throw ParseException.invalidInputForRegex();
                }
                list.add(token);
                skipLF = false;
            }
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

    private Token[] copyTokens(Token[] source, int length) {
        final Token[] reduced;
        if ("GLL".equals(parserType)) {
            reduced = new Token[length+1];
            reduced[length] = TokenEOF.EOF;
        } else {
            reduced = new Token[length];
        }
        System.arraycopy(inputTokens, 0, reduced, 0, length);
        return reduced;
    }

}
