package org.xbib.elasticsearch.index.mapper.langdetect;

import org.elasticsearch.common.settings.Settings;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import org.xbib.elasticsearch.common.langdetect.LangProfile;
import org.xbib.elasticsearch.common.langdetect.LanguageDetectionException;
import org.xbib.elasticsearch.module.langdetect.LangdetectService;

public class DetectorTests extends Assert {

    private static final String TRAINING_EN = "a a a b b c c d e";

    private static final String TRAINING_FR = "a b b c c c d d d";

    private static final String TRAINING_JA = "\u3042 \u3042 \u3042 \u3044 \u3046 \u3048 \u3048";

    private static LangdetectService detect;

    @BeforeClass
    public static void setUp() throws Exception {

        Settings settings = Settings.settingsBuilder()
                .put("languages", "")
                .build();
        detect = new LangdetectService(settings);
        detect.start();

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

        //detect.reset();
    }

    @Test
    public void testDetector1() throws LanguageDetectionException {
        assertEquals(detect.detectAll("a").get(0).getLanguage(), "en");
    }

    @Test
    public void testDetector2() throws LanguageDetectionException {
        assertEquals(detect.detectAll("b d").get(0).getLanguage(), "fr");
    }

    @Test
    public void testDetector3() throws LanguageDetectionException {
        assertEquals(detect.detectAll("d e").get(0).getLanguage(), "en");
    }

    @Test
    public void testDetector4() throws LanguageDetectionException {
        assertEquals(detect.detectAll("\u3042\u3042\u3042\u3042a").get(0).getLanguage(), "ja");
    }

    @Test
    public void testPunctuation() throws LanguageDetectionException {
        assertTrue(detect.detectAll("...").isEmpty());
    }


}