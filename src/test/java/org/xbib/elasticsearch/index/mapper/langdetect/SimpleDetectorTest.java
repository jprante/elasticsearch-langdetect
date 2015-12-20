package org.xbib.elasticsearch.index.mapper.langdetect;

import org.elasticsearch.common.settings.Settings;
import org.junit.Assert;
import org.junit.Test;

import org.xbib.elasticsearch.module.langdetect.LangdetectService;

public class SimpleDetectorTest extends Assert {

    @Test
    public final void testDetector() throws Exception {
        LangdetectService detect = new LangdetectService(Settings.EMPTY);
        detect.start();
        assertEquals("de", detect.detectAll("Das kann deutsch sein").get(0).getLanguage());
        assertEquals("en", detect.detectAll("This is a very small test").get(0).getLanguage());
    }

}