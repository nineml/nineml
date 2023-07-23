package org.nineml.coffeegrinder.util;

public class CodepointToString {
    public static boolean useHex = true;
    public static boolean uppercase = true;
    public static boolean quotePrintableCharacters = true;

    public static String of(int codepoint) {
        if (useHex) {
            return hexOf(codepoint);
        }
        return backslashOf(codepoint);
    }

    public static String of(String text) {
        StringBuilder sb = new StringBuilder();
        String comma = "";
        for (int cp : text.codePoints().toArray()) {
            sb.append(comma);
            sb.append(CodepointToString.of(cp));
            comma = ", ";
        }
        return sb.toString();
    }

    private static String hexOf(int codepoint) {
        String format = uppercase ? "#%X" : "#%x";

        if (Character.isISOControl(codepoint)
                || ((codepoint != ' ' || !quotePrintableCharacters) && Character.isWhitespace(codepoint))
                || (codepoint == 0x00A0           // nbsp
                || codepoint == 0x2007        // Figure space
                || codepoint == 0x202F)) {    // Narrow nbsp
            return String.format(format, codepoint);
        }
        if (Character.isBmpCodePoint(codepoint)) {
            if (!quotePrintableCharacters) {
                return "" + (char) codepoint;
            }
            if (codepoint == '\'') {
                return "\"" + (char) codepoint + "\"";
            }
            return "'" + (char) codepoint + "'";
        }
        StringBuilder sb = new StringBuilder();
        if (quotePrintableCharacters) {
            sb.append("'");
            sb.appendCodePoint(codepoint);
            sb.append("'");
        } else {
            sb.appendCodePoint(codepoint);
        }
        return sb.toString();
    }

    private static String backslashOf(int codepoint) {
        String format = uppercase ? "\\u%04X" : "\\u%04x";

        if (Character.isISOControl(codepoint)
                || ((codepoint != ' ' || !quotePrintableCharacters) && Character.isWhitespace(codepoint))
                || (codepoint == 0x00A0           // nbsp
                || codepoint == 0x2007        // Figure space
                || codepoint == 0x202F)) {    // Narrow nbsp
            switch (codepoint) {
                case 0x07:
                    return "\\a";
                case 0x08:
                    return "\\b";
                case 0x1B:
                    return "\\e";
                case 0x0C:
                    return "\\f";
                case 0x0A:
                    return "\\n";
                case 0x0D:
                    return "\\r";
                case 0x09:
                    return "\\t";
                case 0x0B:
                    return "\\v";
                default:
                    return String.format(format, codepoint);
            }
        }

        if (Character.isBmpCodePoint(codepoint)) {
            if (!quotePrintableCharacters) {
                return "" + (char) codepoint;
            }
            if (codepoint == '\'') {
                return "\"" + (char) codepoint + "\"";
            }
            return "'" + (char) codepoint + "'";
        }
        StringBuilder sb = new StringBuilder();
        if (quotePrintableCharacters) {
            sb.append("'");
            sb.appendCodePoint(codepoint);
            sb.append("'");
        } else {
            sb.appendCodePoint(codepoint);
        }
        return sb.toString();
    }
}
