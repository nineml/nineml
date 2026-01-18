package org.nineml.coffeegrinder.trees;

import java.util.Objects;

/**
 * A numbering of potential trees.
 * <p>This object is used to keep track of what sequential parses have yet to be discovered.</p>
 */
public class TreeNumber {
    private final int[] digits;
    private final int max;
    public final int length;

    public TreeNumber() {
        digits = new int[0];
        max = 0;
        length = 0;
    }

    public TreeNumber(TreeNumber seed, int base) {
        length = seed.digits.length + 1;
        digits = new int[length];
        System.arraycopy(seed.digits, 0, digits, 0, seed.digits.length);
        max = base - 1;
    }

    public boolean isMax() {
        if (digits.length == 0) {
            return true;
        }
        return digits[digits.length - 1] == max;
    }

    public void advance() {
        if (isMax()) {
            throw new IllegalStateException("Overflow");
        }
        int digit = digits[digits.length - 1];
        digits[digits.length - 1] = digit + 1;
    }

    public int digit(int pos) {
        if (pos >= 0 && pos < length) {
            return digits[pos];
        }
        throw new ArrayIndexOutOfBoundsException("No such position");
    }

    public boolean hasDigits(Integer... check) {
        if (check.length != digits.length) {
            return false;
        }

        for (int pos = 0; pos < digits.length; pos++) {
            if (!Objects.equals(digits[pos], check[pos])) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("«");
        for (int pos = 0; pos < digits.length; pos++) {
            if (pos > 0) {
                sb.append(",");
            }
            sb.append(digits[pos]);
        }
        sb.append("»");
        return sb.toString();
    }
}
