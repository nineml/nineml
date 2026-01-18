package org.nineml.coffeegrinder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.nineml.logging.DefaultLogger;
import org.nineml.logging.Logger;

import java.util.Set;

public class LoggerTest {
    @Test
    public void testDefaults() {
        Logger logger = new DefaultLogger();
        Assertions.assertEquals(Logger.ERROR, logger.getDefaultLogLevel());
        Assertions.assertEquals(Logger.ERROR, logger.getLogLevel("fribble-frabble"));
    }

    @Test
    public void testSetters() {
        Logger logger = new DefaultLogger();
        Assertions.assertEquals(Logger.ERROR, logger.getDefaultLogLevel());
        Assertions.assertEquals(Logger.ERROR, logger.getLogLevel("fribble-frabble"));

        try {
            logger.setDefaultLogLevel(null);
            Assertions.fail();
        } catch (NullPointerException ex) {
            // pass
        }

        try {
            logger.setLogLevel("testin", null);
            Assertions.fail();
        } catch (NullPointerException ex) {
            // pass
        }

        try {
            logger.setLogLevels(null);
            // this one we just ignore...perhaps inconsistently.
        } catch (NullPointerException ex) {
            Assertions.fail();
        }

        logger.setDefaultLogLevel("warning");
        Assertions.assertEquals(Logger.WARNING, logger.getDefaultLogLevel());

        logger.setLogLevel("testing", "info");
        Assertions.assertEquals(Logger.INFO, logger.getLogLevel("testing"));

        logger.setDefaultLogLevel("fred");
        Assertions.assertEquals(Logger.ERROR, logger.getDefaultLogLevel());

        logger.setLogLevel("testing", "spoon!");
        Assertions.assertEquals(Logger.ERROR, logger.getLogLevel("testing"));

        logger.setDefaultLogLevel(Logger.WARNING);
        Assertions.assertEquals(Logger.WARNING, logger.getDefaultLogLevel());

        logger.setDefaultLogLevel(-5);
        Assertions.assertEquals(Logger.SILENT, logger.getDefaultLogLevel());

        logger.setLogLevels("*:trace,a:silent,b:random c:debug");
        Assertions.assertEquals(Logger.TRACE, logger.getDefaultLogLevel());
        Assertions.assertEquals(Logger.SILENT, logger.getLogLevel("a"));
        Assertions.assertEquals(Logger.ERROR, logger.getLogLevel("b"));
        Assertions.assertEquals(Logger.DEBUG, logger.getLogLevel("c"));

        logger.setLogLevel("TestTwo", "info");
        Assertions.assertEquals(Logger.INFO, logger.getLogLevel("testTWO"));
    }

    @Test
    public void testInvalidDefaultProperty() {
        String p1 = System.getProperty(Logger.defaultLogLevelProperty);
        String p2 = System.getProperty(Logger.logLevelsProperty);

        System.setProperty(Logger.defaultLogLevelProperty, "fred");
        Logger logger = new DefaultLogger();
        logger.readSystemProperties();

        Assertions.assertEquals(Logger.ERROR, logger.getDefaultLogLevel());

        if (p1 == null) {
            System.clearProperty(Logger.defaultLogLevelProperty);
        } else {
            System.setProperty(Logger.defaultLogLevelProperty, p1);
        }
        if (p2 == null) {
            System.clearProperty(Logger.logLevelsProperty);
        } else {
            System.setProperty(Logger.logLevelsProperty, p2);
        }
    }

    @Test
    public void testProperties() {
        String p1 = System.getProperty(Logger.defaultLogLevelProperty);
        String p2 = System.getProperty(Logger.logLevelsProperty);

        System.setProperty(Logger.logLevelsProperty, "a:1,b:2,c:fred,d:info,e:warning  f:error,g:debug,h:trace,iii:silent");

        Logger logger = new DefaultLogger();
        logger.readSystemProperties();

        Assertions.assertEquals(Logger.ERROR, logger.getLogLevel("a"));
        Assertions.assertEquals(Logger.WARNING, logger.getLogLevel("b"));
        Assertions.assertEquals(Logger.ERROR, logger.getLogLevel("c"));
        Assertions.assertEquals(Logger.INFO, logger.getLogLevel("d"));
        Assertions.assertEquals(Logger.WARNING, logger.getLogLevel("e"));
        Assertions.assertEquals(Logger.ERROR, logger.getLogLevel("f"));
        Assertions.assertEquals(Logger.DEBUG, logger.getLogLevel("g"));
        Assertions.assertEquals(Logger.TRACE, logger.getLogLevel("h"));
        Assertions.assertEquals(Logger.SILENT, logger.getLogLevel("iii"));

        if (p1 == null) {
            System.clearProperty(Logger.defaultLogLevelProperty);
        } else {
            System.setProperty(Logger.defaultLogLevelProperty, p1);
        }
        if (p2 == null) {
            System.clearProperty(Logger.logLevelsProperty);
        } else {
            System.setProperty(Logger.logLevelsProperty, p2);
        }
    }

    @Test
    public void testCategories() {
        Logger logger = new DefaultLogger();
        logger.setLogLevels("a:1,b:2,c:info,d:warning");

        Assertions.assertEquals(Logger.ERROR, logger.getLogLevel("a"));
        Assertions.assertEquals(Logger.WARNING, logger.getLogLevel("b"));
        Assertions.assertEquals(Logger.INFO, logger.getLogLevel("c"));
        Assertions.assertEquals(Logger.WARNING, logger.getLogLevel("d"));

        Set<String> cats = logger.getLogCategories();
        Assertions.assertTrue(cats.contains("a"));
        Assertions.assertTrue(cats.contains("b"));
        Assertions.assertTrue(cats.contains("c"));
        Assertions.assertTrue(cats.contains("d"));
        Assertions.assertEquals(4, cats.size());
    }

    @Test
    public void testClear() {
        Logger logger = new DefaultLogger();
        logger.setLogLevels("a:1,b:2,c:info,d:warning");

        Assertions.assertEquals(Logger.ERROR, logger.getLogLevel("a"));

        Set<String> cats = logger.getLogCategories();
        Assertions.assertTrue(cats.contains("a"));
        Assertions.assertEquals(4, cats.size());

        logger.clearLogLevels();
        Assertions.assertEquals(0, logger.getLogCategories().size());
    }
}
