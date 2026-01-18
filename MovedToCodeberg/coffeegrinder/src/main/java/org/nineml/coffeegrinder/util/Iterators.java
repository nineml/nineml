package org.nineml.coffeegrinder.util;

import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.tokens.TokenCharacter;
import org.nineml.coffeegrinder.tokens.TokenString;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Utility class to generate iterators for sequences of characters and strings.
 */
public class Iterators {
    /**
     * An iterator over a character sequence.
     * <p>This method assumes that the input sequence won't be changed while the iterator is running.</p>
     * @param input the character sequence
     * @return an iterator over it.
     */
    public static Iterator<Token> characterIterator(CharSequence input) {
        return new Iterator<Token>() {
            private final CharSequence seq = input;
            private int pos = 0;

            @Override
            public boolean hasNext() {
                return pos < seq.length();
            }

            @Override
            public Token next() {
                if (pos >= seq.length()) {
                    throw new NoSuchElementException("No more characters");
                }

                final TokenCharacter tok;
                char ch = seq.charAt(pos);
                if (ch >= 0xD800 && ch <= 0xDFFF && pos+1 < seq.length()) {
                    // Is there a faster way to do this?
                    char ch2 = seq.charAt(pos+1);
                    String s = "" + ch + ch2;
                    tok = TokenCharacter.get(s.codePointAt(0));
                    pos += 2;
                } else {
                    tok = TokenCharacter.get(ch);
                    pos++;
                }

                return tok;
            }
        };
    }

    /**
     * An iterator over a sequence of strings.
     * @param input the string sequence.
     * @return an iterator over it.
     */
    public static Iterator<Token> stringIterator(String... input) {
        return stringIterator(Arrays.asList(input), false);
    }

    /**
     * An iterator over a sequence of strings.
     * @param input the string list.
     * @return an iterator over it.
     */
    public static Iterator<Token> stringIterator(List<String> input) {
        return stringIterator(input, true);
    }

    private static Iterator<Token> stringIterator(List<String> input, boolean copy) {
        // Do I need to be this defensive?
        Iterator<String> iter;
        if (copy) {
            iter = (new ArrayList<>(input)).iterator();
        } else {
            iter = input.iterator();
        }

        return new Iterator<Token>() {
            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public Token next() {
                if (iter.hasNext()) {
                    return TokenString.get(iter.next());
                }
                throw new NoSuchElementException("No more characters");
            }
        };
    }

    public static Iterator<Token> fileIterator(String filename) throws IOException {
        FileInputStream fis = new FileInputStream(filename);

        return new Iterator<Token>() {
            private boolean nextAvailable = false;
            private int next = 0;
            @Override
            public boolean hasNext() {
                if (nextAvailable || next < 0) {
                    return next >= 0;
                }

                nextAvailable = true;
                try {
                    next = fis.read();
                } catch (IOException ex) {
                    next = -1;
                }

                return next >= 0;
            }

            @Override
            public Token next() {
                if (hasNext()) {
                    nextAvailable = false;
                    return TokenCharacter.get((char) next);
                }
                throw new NoSuchElementException("No more characters");
            }
        };
    }

}
