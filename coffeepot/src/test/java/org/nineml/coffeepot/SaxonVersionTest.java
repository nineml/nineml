package org.nineml.coffeepot;

import net.sf.saxon.s9api.Processor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * What version of Saxon are we testing with?
 * <p>This set of tests doesn't really test anything, exactly. It allows you to tell from the
 * test report which version of Saxon was used. Three of the four tests will be skipped.</p>
 */
public class SaxonVersionTest {
    private static String saxonEdition = "XX";
    private static int saxonMajor = 0;

    @BeforeAll
    public static void setup() {
        Processor processor = new Processor(true);
        saxonEdition = processor.getSaxonEdition();
        String version = processor.getSaxonProductVersion();
        version = version.substring(0, version.indexOf("."));
        saxonMajor = Integer.parseInt(version);
    }

    @Test
    public void saxon11HE() {
        Assumptions.assumeTrue(saxonMajor == 11, "Skipping 11 test, Saxon version = " + saxonMajor);
        Assumptions.assumeTrue("HE".equals(saxonEdition), "Skipping HE test, Saxon edition = " + saxonEdition);
        Assertions.assertTrue(true);
    }

    @Test
    public void saxon11EE() {
        Assumptions.assumeTrue(saxonMajor == 11, "Skipping 11 test, Saxon version = " + saxonMajor);
        Assumptions.assumeTrue("EE".equals(saxonEdition), "Skipping EE test, Saxon edition = " + saxonEdition);
        Assertions.assertTrue(true);
    }

    @Test
    public void saxon12HE() {
        Assumptions.assumeTrue(saxonMajor == 12, "Skipping 12 test, Saxon version = " + saxonMajor);
        Assumptions.assumeTrue("HE".equals(saxonEdition), "Skipping HE test, Saxon edition = " + saxonEdition);
        Assertions.assertTrue(true);
    }

    @Test
    public void saxon12EE() {
        Assumptions.assumeTrue(saxonMajor == 12, "Skipping 11 test, Saxon version = " + saxonMajor);
        Assumptions.assumeTrue("EE".equals(saxonEdition), "Skipping EE test, Saxon edition = " + saxonEdition);
        Assertions.assertTrue(true);
    }

}
