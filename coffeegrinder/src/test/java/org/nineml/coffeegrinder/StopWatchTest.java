package org.nineml.coffeegrinder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.nineml.coffeegrinder.util.StopWatch;

public class StopWatchTest {
    @Test
    public void testTimer() {
        StopWatch stopWatch = new StopWatch();
        long first = 0;
        long second = 0;
        try {
            Thread.sleep(250);
            first = stopWatch.duration();
            Thread.sleep(250);
            stopWatch.stop();
            second = stopWatch.duration();
            Thread.sleep(250);
        } catch (InterruptedException ex) {
            Assertions.fail();
        }
        Assertions.assertTrue(first > 200);
        Assertions.assertTrue(second > first);
        Assertions.assertEquals(second, stopWatch.duration());
    }

    @Test
    public void testPerSecond() {
        StopWatch stopWatch = new StopWatch();
        try {
            Thread.sleep(500);
            stopWatch.stop();
        } catch (InterruptedException ex) {
            Assertions.fail();
        }

        // I don't trust Thread.sleep to be that accurate...
        double perS = Double.parseDouble(stopWatch.perSecond(1000));

        Assertions.assertTrue(perS > 1900.0);
        Assertions.assertTrue(perS < 2100.0);
    }

    @Test
    public void testFormatter() {
        StopWatch stopWatch = new StopWatch();
        Assertions.assertEquals("3.01s", stopWatch.elapsed(3010));
        Assertions.assertEquals("30.00s", stopWatch.elapsed(30000));
        Assertions.assertEquals("5m1.00s", stopWatch.elapsed(301000));
        Assertions.assertEquals("50m10.00s", stopWatch.elapsed(3010000));
        Assertions.assertEquals("1d4h36m50.00s", stopWatch.elapsed(103010000));
        Assertions.assertEquals("24d8h10m10.00s", stopWatch.elapsed(2103010000));
    }
}
