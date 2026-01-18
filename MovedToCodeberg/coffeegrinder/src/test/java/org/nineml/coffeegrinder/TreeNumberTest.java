package org.nineml.coffeegrinder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.nineml.coffeegrinder.trees.TreeNumber;

import static org.junit.jupiter.api.Assertions.fail;

public class TreeNumberTest {
    @Test
    public void twobits() {
        TreeNumber number = new TreeNumber();
        number = new TreeNumber(number, 2);
        number = new TreeNumber(number, 2);

        Assertions.assertTrue(number.hasDigits(0,0));
        number.advance();
        Assertions.assertTrue(number.hasDigits(0,1));
        try {
            number.advance();
            fail();
        } catch (IllegalStateException ex) {
            // nop;
        }
    }

    @Test
    public void mixedBits() {
        TreeNumber number = new TreeNumber();
        number = new TreeNumber(number, 2);
        number = new TreeNumber(number, 3);
        number = new TreeNumber(number, 3);

        Assertions.assertTrue(number.hasDigits(0,0,0));
        number.advance(); Assertions.assertTrue(number.hasDigits(0,0,1));
        number.advance(); Assertions.assertTrue(number.hasDigits(0,0,2));
        try {
            number.advance();
            fail();
        } catch (IllegalStateException ex) {
            // nop;
        }
    }



}
