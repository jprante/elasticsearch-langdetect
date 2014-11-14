package org.xbib.elasticsearch.index.mapper.langdetect;

import org.elasticsearch.common.settings.ImmutableSettings;
import org.junit.Assert;
import org.junit.Test;

import org.xbib.elasticsearch.module.langdetect.LangdetectService;

public class SimpleDetectorTest extends Assert {

    @Test
    public final void testDetector() throws Exception {

        LangdetectService detect = new LangdetectService(ImmutableSettings.EMPTY);
        detect.start();
        //detect.loadDefaultProfiles();
        assertEquals("de", detect.detectAll("Das kann deutsch sein").get(0).getLanguage());
        //detect.reset();
        assertEquals("en", detect.detectAll("This is a very small test").get(0).getLanguage());
    }

}