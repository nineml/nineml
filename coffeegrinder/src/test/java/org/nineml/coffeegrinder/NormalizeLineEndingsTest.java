package org.nineml.coffeegrinder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.nineml.coffeegrinder.parser.ParserInput;
import org.nineml.coffeegrinder.parser.ParserOptions;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.tokens.TokenCharacter;
import org.nineml.coffeegrinder.tokens.TokenEOF;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.nineml.coffeegrinder.parser.ParserInput.LF;
import static org.nineml.coffeegrinder.parser.ParserInput.CR;
import static org.nineml.coffeegrinder.parser.ParserInput.NEL;
import static org.nineml.coffeegrinder.parser.ParserInput.LINE_SEPARATOR;

public class NormalizeLineEndingsTest {
    private static Map<Integer,String> sep = new HashMap<>();
    static {
        sep.put(LF, "\n");
        sep.put(CR, "\r");
        sep.put(NEL, "\u0085");
        sep.put(LINE_SEPARATOR, "\u2028");
    }

    @ParameterizedTest
    @ValueSource(strings = {"GLL", "Earley"})
    public void normalizeOff_string1(String parserType) {
        ParserOptions options = new ParserOptions();
        options.setNormalizeLineEndings(false);
        options.setParserType(parserType);

        ParserInput parserInput = new ParserInput(options, true);
        parserInput.from("test");

        if ("GLL".equals(parserType)) {
            Assertions.assertEquals(5, parserInput.tokens().length);
            Assertions.assertEquals(4, parserInput.string().length());
            Assertions.assertEquals(TokenEOF.EOF, parserInput.tokens()[4]);
        } else {
            Assertions.assertEquals(4, parserInput.tokens().length);
            Assertions.assertEquals(4, parserInput.string().length());
        }

        Assertions.assertEquals("test", parserInput.string());
    }

    @ParameterizedTest
    @ValueSource(strings = {"GLL", "Earley"})
    public void normalizeOff_string2(String parserType) {
        ParserOptions options = new ParserOptions();
        options.setNormalizeLineEndings(false);
        options.setParserType(parserType);

        ParserInput parserInput = new ParserInput(options, true);
        parserInput.from("test\ntest\r\ntest\u0085test\u2028test\r\rtest");

        if ("GLL".equals(parserType)) {
            Assertions.assertEquals(32, parserInput.tokens().length);
            Assertions.assertEquals(31, parserInput.string().length());
            Assertions.assertEquals(TokenEOF.EOF, parserInput.tokens()[31]);
        } else {
            Assertions.assertEquals(31, parserInput.tokens().length);
            Assertions.assertEquals(31, parserInput.string().length());
        }

        Assertions.assertEquals(sep.get(LF), parserInput.tokens()[4].getValue());
        Assertions.assertEquals(sep.get(CR), parserInput.tokens()[9].getValue());
        Assertions.assertEquals(sep.get(LF), parserInput.tokens()[10].getValue());
        Assertions.assertEquals(sep.get(NEL), parserInput.tokens()[15].getValue());
        Assertions.assertEquals(sep.get(LINE_SEPARATOR), parserInput.tokens()[20].getValue());
        Assertions.assertEquals(sep.get(CR), parserInput.tokens()[25].getValue());
        Assertions.assertEquals(sep.get(CR), parserInput.tokens()[26].getValue());

        Assertions.assertEquals(sep.get(LF), parserInput.string().substring(4, 5));
        Assertions.assertEquals(sep.get(CR), parserInput.string().substring(9, 10));
        Assertions.assertEquals(sep.get(LF), parserInput.string().substring(10, 11));
        Assertions.assertEquals(sep.get(NEL), parserInput.string().substring(15, 16));
        Assertions.assertEquals(sep.get(LINE_SEPARATOR), parserInput.string().substring(20, 21));
        Assertions.assertEquals(sep.get(CR), parserInput.string().substring(25, 26));
        Assertions.assertEquals(sep.get(CR), parserInput.string().substring(26, 27));
    }

    @ParameterizedTest
    @ValueSource(strings = {"GLL", "Earley"})
    public void normalizeOn_string1(String parserType) {
        ParserOptions options = new ParserOptions();
        options.setNormalizeLineEndings(false);
        options.setParserType(parserType);

        ParserInput parserInput = new ParserInput(options, true);
        parserInput.from("test");

        if ("GLL".equals(parserType)) {
            Assertions.assertEquals(5, parserInput.tokens().length);
            Assertions.assertEquals(4, parserInput.string().length());
            Assertions.assertEquals(TokenEOF.EOF, parserInput.tokens()[4]);
        } else {
            Assertions.assertEquals(4, parserInput.tokens().length);
            Assertions.assertEquals(4, parserInput.string().length());
        }

        Assertions.assertEquals("test", parserInput.string());
    }


    @ParameterizedTest
    @ValueSource(strings = {"GLL", "Earley"})
    public void normalizeOn_string2(String parserType) {
        ParserOptions options = new ParserOptions();
        options.setNormalizeLineEndings(true);
        options.setParserType(parserType);

        ParserInput parserInput = new ParserInput(options, true);
        parserInput.from("test\ntest\r\ntest\u0085test\u2028test\r\rtest");

        if ("GLL".equals(parserType)) {
            Assertions.assertEquals(31, parserInput.tokens().length);
            Assertions.assertEquals(30, parserInput.string().length());
            Assertions.assertEquals(TokenEOF.EOF, parserInput.tokens()[30]);
        } else {
            Assertions.assertEquals(30, parserInput.tokens().length);
            Assertions.assertEquals(30, parserInput.string().length());
        }

        Assertions.assertEquals(sep.get(LF), parserInput.tokens()[4].getValue());
        Assertions.assertEquals(sep.get(LF), parserInput.tokens()[9].getValue());
        Assertions.assertEquals(sep.get(LF), parserInput.tokens()[14].getValue());
        Assertions.assertEquals(sep.get(LF), parserInput.tokens()[19].getValue());
        Assertions.assertEquals(sep.get(LF), parserInput.tokens()[24].getValue());
        Assertions.assertEquals(sep.get(LF), parserInput.tokens()[25].getValue());

        Assertions.assertEquals(sep.get(LF), parserInput.string().substring(4, 5));
        Assertions.assertEquals(sep.get(LF), parserInput.string().substring(9, 10));
        Assertions.assertEquals(sep.get(LF), parserInput.string().substring(14, 15));
        Assertions.assertEquals(sep.get(LF), parserInput.string().substring(19, 20));
        Assertions.assertEquals(sep.get(LF), parserInput.string().substring(24, 25));
        Assertions.assertEquals(sep.get(LF), parserInput.string().substring(25, 26));
    }

    @ParameterizedTest
    @ValueSource(strings = {"GLL", "Earley"})
    public void normalizeOff_tokens1(String parserType) {
        ParserOptions options = new ParserOptions();
        options.setNormalizeLineEndings(false);
        options.setParserType(parserType);

        Token[] tokens = new Token[4];
        int pos = 0;
        for (char ch : "test".toCharArray()) {
            tokens[pos] = TokenCharacter.get(ch);
            pos++;
        }

        ParserInput parserInput = new ParserInput(options, true);
        parserInput.from(tokens);

        if ("GLL".equals(parserType)) {
            Assertions.assertEquals(5, parserInput.tokens().length);
            Assertions.assertEquals(4, parserInput.string().length());
            Assertions.assertEquals(TokenEOF.EOF, parserInput.tokens()[4]);
        } else {
            Assertions.assertEquals(4, parserInput.tokens().length);
            Assertions.assertEquals(4, parserInput.string().length());
        }

        Assertions.assertEquals("test", parserInput.string());
    }

    @ParameterizedTest
    @ValueSource(strings = {"GLL", "Earley"})
    public void normalizeOff_tokens2(String parserType) {
        ParserOptions options = new ParserOptions();
        options.setNormalizeLineEndings(false);
        options.setParserType(parserType);

        Token[] tokens = new Token[25];
        int pos = 0;
        for (char ch : "test\ntest\r\ntest\u0085test\u2028test".toCharArray()) {
            tokens[pos] = TokenCharacter.get(ch);
            pos++;
        }

        ParserInput parserInput = new ParserInput(options, true);
        parserInput.from(tokens);

        if ("GLL".equals(parserType)) {
            Assertions.assertEquals(26, parserInput.tokens().length);
            Assertions.assertEquals(25, parserInput.string().length());
            Assertions.assertEquals(TokenEOF.EOF, parserInput.tokens()[25]);
        } else {
            Assertions.assertEquals(25, parserInput.tokens().length);
            Assertions.assertEquals(25, parserInput.string().length());
        }

        Assertions.assertEquals(sep.get(LF), parserInput.tokens()[4].getValue());
        Assertions.assertEquals(sep.get(CR), parserInput.tokens()[9].getValue());
        Assertions.assertEquals(sep.get(LF), parserInput.tokens()[10].getValue());
        Assertions.assertEquals(sep.get(NEL), parserInput.tokens()[15].getValue());
        Assertions.assertEquals(sep.get(LINE_SEPARATOR), parserInput.tokens()[20].getValue());

        Assertions.assertEquals(sep.get(LF), parserInput.string().substring(4, 5));
        Assertions.assertEquals(sep.get(CR), parserInput.string().substring(9, 10));
        Assertions.assertEquals(sep.get(LF), parserInput.string().substring(10, 11));
        Assertions.assertEquals(sep.get(NEL), parserInput.string().substring(15, 16));
        Assertions.assertEquals(sep.get(LINE_SEPARATOR), parserInput.string().substring(20, 21));
    }

    @ParameterizedTest
    @ValueSource(strings = {"GLL", "Earley"})
    public void normalizeOn_tokens1(String parserType) {
        ParserOptions options = new ParserOptions();
        options.setNormalizeLineEndings(false);
        options.setParserType(parserType);

        Token[] tokens = new Token[4];
        int pos = 0;
        for (char ch : "test".toCharArray()) {
            tokens[pos] = TokenCharacter.get(ch);
            pos++;
        }

        ParserInput parserInput = new ParserInput(options, true);
        parserInput.from(tokens);

        if ("GLL".equals(parserType)) {
            Assertions.assertEquals(5, parserInput.tokens().length);
            Assertions.assertEquals(4, parserInput.string().length());
            Assertions.assertEquals(TokenEOF.EOF, parserInput.tokens()[4]);
        } else {
            Assertions.assertEquals(4, parserInput.tokens().length);
            Assertions.assertEquals(4, parserInput.string().length());
        }

        Assertions.assertEquals("test", parserInput.string());
    }


    @ParameterizedTest
    @ValueSource(strings = {"GLL", "Earley"})
    public void normalizeOn_tokens2(String parserType) {
        ParserOptions options = new ParserOptions();
        options.setNormalizeLineEndings(true);
        options.setParserType(parserType);

        Token[] tokens = new Token[31];
        int pos = 0;
        for (char ch : "test\ntest\r\ntest\u0085test\u2028test\r\rtest".toCharArray()) {
            tokens[pos] = TokenCharacter.get(ch);
            pos++;
        }

        ParserInput parserInput = new ParserInput(options, true);
        parserInput.from(tokens);

        if ("GLL".equals(parserType)) {
            Assertions.assertEquals(31, parserInput.tokens().length);
            Assertions.assertEquals(30, parserInput.string().length());
            Assertions.assertEquals(TokenEOF.EOF, parserInput.tokens()[30]);
        } else {
            Assertions.assertEquals(30, parserInput.tokens().length);
            Assertions.assertEquals(30, parserInput.string().length());
        }

        Assertions.assertEquals(sep.get(LF), parserInput.tokens()[4].getValue());
        Assertions.assertEquals(sep.get(LF), parserInput.tokens()[9].getValue());
        Assertions.assertEquals(sep.get(LF), parserInput.tokens()[14].getValue());
        Assertions.assertEquals(sep.get(LF), parserInput.tokens()[19].getValue());
        Assertions.assertEquals(sep.get(LF), parserInput.tokens()[24].getValue());
        Assertions.assertEquals(sep.get(LF), parserInput.tokens()[25].getValue());

        Assertions.assertEquals(sep.get(LF), parserInput.string().substring(4, 5));
        Assertions.assertEquals(sep.get(LF), parserInput.string().substring(9, 10));
        Assertions.assertEquals(sep.get(LF), parserInput.string().substring(14, 15));
        Assertions.assertEquals(sep.get(LF), parserInput.string().substring(19, 20));
        Assertions.assertEquals(sep.get(LF), parserInput.string().substring(24, 25));
        Assertions.assertEquals(sep.get(LF), parserInput.string().substring(25, 26));
    }

    @ParameterizedTest
    @ValueSource(strings = {"GLL", "Earley"})
    public void normalizeOff_iter1(String parserType) {
        ParserOptions options = new ParserOptions();
        options.setNormalizeLineEndings(false);
        options.setParserType(parserType);

        Token[] tokens = new Token[4];
        int pos = 0;
        for (char ch : "test".toCharArray()) {
            tokens[pos] = TokenCharacter.get(ch);
            pos++;
        }

        ParserInput parserInput = new ParserInput(options, true);
        parserInput.from(Arrays.stream(tokens).iterator());

        if ("GLL".equals(parserType)) {
            Assertions.assertEquals(5, parserInput.tokens().length);
            Assertions.assertEquals(4, parserInput.string().length());
            Assertions.assertEquals(TokenEOF.EOF, parserInput.tokens()[4]);
        } else {
            Assertions.assertEquals(4, parserInput.tokens().length);
            Assertions.assertEquals(4, parserInput.string().length());
        }

        Assertions.assertEquals("test", parserInput.string());
    }

    @ParameterizedTest
    @ValueSource(strings = {"GLL", "Earley"})
    public void normalizeOff_iter2(String parserType) {
        ParserOptions options = new ParserOptions();
        options.setNormalizeLineEndings(false);
        options.setParserType(parserType);

        Token[] tokens = new Token[25];
        int pos = 0;
        for (char ch : "test\ntest\r\ntest\u0085test\u2028test".toCharArray()) {
            tokens[pos] = TokenCharacter.get(ch);
            pos++;
        }

        ParserInput parserInput = new ParserInput(options, true);
        parserInput.from(Arrays.stream(tokens).iterator());

        if ("GLL".equals(parserType)) {
            Assertions.assertEquals(26, parserInput.tokens().length);
            Assertions.assertEquals(25, parserInput.string().length());
            Assertions.assertEquals(TokenEOF.EOF, parserInput.tokens()[25]);
        } else {
            Assertions.assertEquals(25, parserInput.tokens().length);
            Assertions.assertEquals(25, parserInput.string().length());
        }

        Assertions.assertEquals(sep.get(LF), parserInput.tokens()[4].getValue());
        Assertions.assertEquals(sep.get(CR), parserInput.tokens()[9].getValue());
        Assertions.assertEquals(sep.get(LF), parserInput.tokens()[10].getValue());
        Assertions.assertEquals(sep.get(NEL), parserInput.tokens()[15].getValue());
        Assertions.assertEquals(sep.get(LINE_SEPARATOR), parserInput.tokens()[20].getValue());

        Assertions.assertEquals(sep.get(LF), parserInput.string().substring(4, 5));
        Assertions.assertEquals(sep.get(CR), parserInput.string().substring(9, 10));
        Assertions.assertEquals(sep.get(LF), parserInput.string().substring(10, 11));
        Assertions.assertEquals(sep.get(NEL), parserInput.string().substring(15, 16));
        Assertions.assertEquals(sep.get(LINE_SEPARATOR), parserInput.string().substring(20, 21));
    }

    @ParameterizedTest
    @ValueSource(strings = {"GLL", "Earley"})
    public void normalizeOn_iter1(String parserType) {
        ParserOptions options = new ParserOptions();
        options.setNormalizeLineEndings(false);
        options.setParserType(parserType);

        Token[] tokens = new Token[4];
        int pos = 0;
        for (char ch : "test".toCharArray()) {
            tokens[pos] = TokenCharacter.get(ch);
            pos++;
        }

        ParserInput parserInput = new ParserInput(options, true);
        parserInput.from(Arrays.stream(tokens).iterator());

        if ("GLL".equals(parserType)) {
            Assertions.assertEquals(5, parserInput.tokens().length);
            Assertions.assertEquals(4, parserInput.string().length());
            Assertions.assertEquals(TokenEOF.EOF, parserInput.tokens()[4]);
        } else {
            Assertions.assertEquals(4, parserInput.tokens().length);
            Assertions.assertEquals(4, parserInput.string().length());
        }

        Assertions.assertEquals("test", parserInput.string());
    }


    @ParameterizedTest
    @ValueSource(strings = {"GLL", "Earley"})
    public void normalizeOn_iter2(String parserType) {
        ParserOptions options = new ParserOptions();
        options.setNormalizeLineEndings(true);
        options.setParserType(parserType);

        Token[] tokens = new Token[31];
        int pos = 0;
        for (char ch : "test\ntest\r\ntest\u0085test\u2028test\r\rtest".toCharArray()) {
            tokens[pos] = TokenCharacter.get(ch);
            pos++;
        }

        ParserInput parserInput = new ParserInput(options, true);
        parserInput.from(Arrays.stream(tokens).iterator());

        if ("GLL".equals(parserType)) {
            Assertions.assertEquals(31, parserInput.tokens().length);
            Assertions.assertEquals(30, parserInput.string().length());
            Assertions.assertEquals(TokenEOF.EOF, parserInput.tokens()[30]);
        } else {
            Assertions.assertEquals(30, parserInput.tokens().length);
            Assertions.assertEquals(30, parserInput.string().length());
        }

        Assertions.assertEquals(sep.get(LF), parserInput.tokens()[4].getValue());
        Assertions.assertEquals(sep.get(LF), parserInput.tokens()[9].getValue());
        Assertions.assertEquals(sep.get(LF), parserInput.tokens()[14].getValue());
        Assertions.assertEquals(sep.get(LF), parserInput.tokens()[19].getValue());
        Assertions.assertEquals(sep.get(LF), parserInput.tokens()[24].getValue());
        Assertions.assertEquals(sep.get(LF), parserInput.tokens()[25].getValue());

        Assertions.assertEquals(sep.get(LF), parserInput.string().substring(4, 5));
        Assertions.assertEquals(sep.get(LF), parserInput.string().substring(9, 10));
        Assertions.assertEquals(sep.get(LF), parserInput.string().substring(14, 15));
        Assertions.assertEquals(sep.get(LF), parserInput.string().substring(19, 20));
        Assertions.assertEquals(sep.get(LF), parserInput.string().substring(24, 25));
        Assertions.assertEquals(sep.get(LF), parserInput.string().substring(25, 26));
    }
}
