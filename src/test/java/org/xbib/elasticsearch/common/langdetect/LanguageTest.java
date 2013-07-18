package org.xbib.elasticsearch.common.langdetect;

import org.testng.Assert;
import org.testng.annotations.Test;

public class LanguageTest extends Assert {

    @Test
    public final void testLanguage() {
        Language lang = new Language(null, 0);
        assertEquals(lang.getLanguage(), null);
        assertEquals(lang.getProbability(), 0.0, 0.0001);
        assertEquals(lang.getLanguage(), null);

        Language lang2 = new Language("en", 1.0);
        assertEquals(lang2.getLanguage(), "en");
        assertEquals(lang2.getProbability(), 1.0, 0.0001);
        assertEquals(lang2.getLanguage(), "en");
        assertEquals(lang2.getProbability(), 1.0);        
    }
}
