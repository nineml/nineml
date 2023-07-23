package org.nineml.coffeegrinder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.nineml.coffeegrinder.util.CodepointToString;

public class CodepointToStringTest {
    @Test
    public void hex() {
        CodepointToString.useHex = true;
        CodepointToString.quotePrintableCharacters = false;

        String input = "AbcDefGhi0123";
        String output = "A, b, c, D, e, f, G, h, i, 0, 1, 2, 3";
        Assertions.assertEquals(output, CodepointToString.of(input));

        input = "\r\n \t \f";
        output = "#D, #A, #20, #9, #20, #C";
        String result = CodepointToString.of(input);
        Assertions.assertEquals(output, result);

        CodepointToString.uppercase = false;
        output = "#d, #a, #20, #9, #20, #c";
        result = CodepointToString.of(input);
        Assertions.assertEquals(output, result);

        CodepointToString.uppercase = true;

        input = " \uD83D\uDE38  ￥";
        output = "#A0, \uD83D\uDE38, #2007, #202F, ￥";
        result = CodepointToString.of(input);
        Assertions.assertEquals(output, result);

        CodepointToString.quotePrintableCharacters = true;
    }

    @Test
    public void hexQuoted() {
        CodepointToString.useHex = true;
        CodepointToString.quotePrintableCharacters = true;

        String input = "AbcDefGhi0123\"'";
        String output = "'A', 'b', 'c', 'D', 'e', 'f', 'G', 'h', 'i', '0', '1', '2', '3', '\"', \"'\"";
        Assertions.assertEquals(output, CodepointToString.of(input));

        input = "\r\n \t \f";
        output = "#D, #A, ' ', #9, ' ', #C";
        String result = CodepointToString.of(input);
        Assertions.assertEquals(output, result);

        CodepointToString.uppercase = false;
        output = "#d, #a, ' ', #9, ' ', #c";
        result = CodepointToString.of(input);
        Assertions.assertEquals(output, result);

        CodepointToString.uppercase = true;

        input = " \uD83D\uDE38  ￥";
        output = "#A0, '\uD83D\uDE38', #2007, #202F, '￥'";
        result = CodepointToString.of(input);
        Assertions.assertEquals(output, result);

        CodepointToString.quotePrintableCharacters = true;
    }

    @Test
    public void backslash() {
        CodepointToString.useHex = false;
        CodepointToString.quotePrintableCharacters = false;

        String input = "AbcDefGhi0123";
        String output = "A, b, c, D, e, f, G, h, i, 0, 1, 2, 3";
        Assertions.assertEquals(output, CodepointToString.of(input));

        input = "\r\n \t \f";
        output = "\\r, \\n, \\u0020, \\t, \\u0020, \\f";
        String result = CodepointToString.of(input);
        Assertions.assertEquals(output, result);

        CodepointToString.uppercase = false;
        input = " \uD83D\uDE38  ￥";
        output = "\\u00a0, \uD83D\uDE38, \\u2007, \\u202f, ￥";
        result = CodepointToString.of(input);
        Assertions.assertEquals(output, result);

        CodepointToString.uppercase = true;

        input = " \uD83D\uDE38  ￥";
        output = "\\u00A0, \uD83D\uDE38, \\u2007, \\u202F, ￥";
        result = CodepointToString.of(input);
        Assertions.assertEquals(output, result);

        CodepointToString.useHex = true;
        CodepointToString.quotePrintableCharacters = false;
    }

    @Test
    public void backslashQuoted() {
        CodepointToString.useHex = false;
        CodepointToString.quotePrintableCharacters = true;

        String input = "AbcDefGhi0123\"'";
        String output = "'A', 'b', 'c', 'D', 'e', 'f', 'G', 'h', 'i', '0', '1', '2', '3', '\"', \"'\"";
        Assertions.assertEquals(output, CodepointToString.of(input));

        input = "\r\n \t \f";
        output = "\\r, \\n, ' ', \\t, ' ', \\f";
        String result = CodepointToString.of(input);
        Assertions.assertEquals(output, result);

        CodepointToString.uppercase = false;
        input = " \uD83D\uDE38  ￥";
        output = "\\u00a0, '\uD83D\uDE38', \\u2007, \\u202f, '￥'";
        result = CodepointToString.of(input);
        Assertions.assertEquals(output, result);

        CodepointToString.uppercase = true;

        input = " \uD83D\uDE38  ￥";
        output = "\\u00A0, '\uD83D\uDE38', \\u2007, \\u202F, '￥'";
        result = CodepointToString.of(input);
        Assertions.assertEquals(output, result);

        CodepointToString.useHex = true;
        CodepointToString.quotePrintableCharacters = true;
    }
}
