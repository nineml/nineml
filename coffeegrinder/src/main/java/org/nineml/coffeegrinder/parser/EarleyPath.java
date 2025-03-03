package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.util.ParserAttribute;

import java.util.ArrayList;
import java.util.List;

public class EarleyPath {
    private final Token[] input;
    private final ArrayList<EarleyItem> completed = new ArrayList<>();
    private final ArrayList<EarleyItem> open = new ArrayList<>();

    public EarleyPath(Token[] input) {
        this.input = input;
    }

    public List<EarleyItem> getCompleted() {
        return completed;
    }

    public List<EarleyItem> getOpen() {
        return open;
    }

    public void addCompleted(EarleyItem item) {
        completed.add(item);
    }

    public void addOpen(EarleyItem item) {
        open.add(item);
    }

    public String getInputString(EarleyItem item) {
        int leftExtent = item.w.leftExtent;
        int rightExtent = item.w.rightExtent;

        StringBuilder sb = new StringBuilder();
        for (int index = leftExtent; index < rightExtent; index++) {
            String value = input[index].getValue();
            if (value.length() == 1) {
                char ch = value.charAt(0);
                if (ch < ' ') {
                    int[] codepoints = new int[1];
                    codepoints[0] = 0x2400 + ch;
                    sb.append(new String(codepoints, 0, 1));
                } else {
                    sb.append(ch);
                }
            } else {
                sb.append(value);
            }

            if (input[index].getAttributeValue(ParserAttribute.OFFSET_NAME, null) != null) {
                sb.append("…");
            }

        }
        String text = sb.toString();
        if (text.length() > 32) {
            text = text.substring(0, 15) + " ... " + text.substring(text.length() - 15);
        }
        return text;
    }
}
