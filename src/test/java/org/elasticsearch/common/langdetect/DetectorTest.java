package org.elasticsearch.common.langdetect;

import java.util.List;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DetectorTest extends Assert {
    
    private static final String TRAINING_EN = "a a a b b c c d e";
    private static final String TRAINING_FR = "a b b c c c d d d";
    private static final String TRAINING_JA = "\u3042 \u3042 \u3042 \u3044 \u3046 \u3048 \u3048";

    private final DetectorFactory factory = DetectorFactory.newInstance();
    
    @BeforeClass
    public void setUp() throws Exception {

        LangProfile profile_en = new LangProfile("en");
        for (String w : TRAINING_EN.split(" ")) {
            profile_en.add(w);
        }
        factory.addProfile(profile_en, 0, 3);

        LangProfile profile_fr = new LangProfile("fr");
        for (String w : TRAINING_FR.split(" ")) {
            profile_fr.add(w);
        }
        factory.addProfile(profile_fr, 1, 3);

        LangProfile profile_ja = new LangProfile("ja");
        for (String w : TRAINING_JA.split(" ")) {
            profile_ja.add(w);
        }
        factory.addProfile(profile_ja, 2, 3);
    }

    @Test
    public final void testDetector1() throws LanguageDetectionException {
        Detector detect = factory.createDetector();
        assertEquals(detect.detect("a"), "en");
    }

    @Test
    public final void testDetector2() throws LanguageDetectionException {
        Detector detect = factory.createDetector();
        assertEquals(detect.detect("b d"), "fr");
    }

    @Test
    public final void testDetector3() throws LanguageDetectionException {
        Detector detect = factory.createDetector();
        assertEquals(detect.detect("d e"), "en");
    }

    @Test
    public final void testDetector4() throws LanguageDetectionException {
        Detector detect = factory.createDetector();
        assertEquals(detect.detect("\u3042\u3042\u3042\u3042a"), "ja");
    }

    @Test
    public final void testLangList() throws LanguageDetectionException {
        List<String> langList = factory.getLangList();
        assertEquals(langList.size(), 3);
        assertEquals(langList.get(0), "en");
        assertEquals(langList.get(1), "fr");
        assertEquals(langList.get(2), "ja");
    }

}