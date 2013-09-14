package org.xbib.elasticsearch.common.langdetect;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SimpleDetectorTest extends Assert {

    @Test
    public final void testDetector() throws Exception {

        Detector detect = new Detector();
        detect.loadDefaultProfiles();
        assertEquals("de", detect.detect("Das kann deutsch sein"));
        detect.reset();
        assertEquals("en", detect.detect("This is a very small test"));
    }

}