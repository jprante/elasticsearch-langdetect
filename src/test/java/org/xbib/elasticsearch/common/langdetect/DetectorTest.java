package org.xbib.elasticsearch.common.langdetect;

import java.util.List;

import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DetectorTest extends Assert {

    private static final String UNKNOWN_LANG = "unknown";

    private static final String TRAINING_EN = "a a a b b c c d e";

    private static final String TRAINING_FR = "a b b c c c d d d";

    private static final String TRAINING_JA = "\u3042 \u3042 \u3042 \u3044 \u3046 \u3048 \u3048";

    private Detector detect;

    @BeforeClass
    public void setUp() throws Exception {

        detect = new Detector();

        LangProfile profile_en = new LangProfile("en");
        for (String w : TRAINING_EN.split(" ")) {
            profile_en.add(w);
        }
        detect.addProfile(profile_en, 0, 3);

        LangProfile profile_fr = new LangProfile("fr");
        for (String w : TRAINING_FR.split(" ")) {
            profile_fr.add(w);
        }
        detect.addProfile(profile_fr, 1, 3);

        LangProfile profile_ja = new LangProfile("ja");
        for (String w : TRAINING_JA.split(" ")) {
            profile_ja.add(w);
        }
        detect.addProfile(profile_ja, 2, 3);

        detect.reset();

    }

    @Test
    public void testDetector1() {
        assertEquals(detect.detect("a"), "en");
    }

    @Test
    public void testDetector2() {
        assertEquals(detect.detect("b d"), "fr");
    }

    @Test
    public void testDetector3() {
        assertEquals(detect.detect("d e"), "en");
    }

    @Test
    public void testDetector4() {
        assertEquals(detect.detect("\u3042\u3042\u3042\u3042a"), "ja");
    }

    @Test
    public void testLangList() {
        List<String> langList = detect.getLangList();
        assertEquals(langList.size(), 3);
        assertEquals(langList.get(0), "en");
        assertEquals(langList.get(1), "fr");
        assertEquals(langList.get(2), "ja");
    }

    @Test
    public void testPunctuation() {
        assertEquals(detect.detect("..."), UNKNOWN_LANG);
    }


}