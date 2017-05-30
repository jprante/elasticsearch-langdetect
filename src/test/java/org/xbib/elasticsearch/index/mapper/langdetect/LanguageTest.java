package org.xbib.elasticsearch.index.mapper.langdetect;

import org.junit.Assert;
import org.junit.Test;
import org.xbib.elasticsearch.common.langdetect.Language;

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
    }
}
