package org.xbib.elasticsearch.common.langdetect;

import org.testng.Assert;
import org.testng.annotations.Test;

public class LangProfileTest extends Assert {

    @Test
    public final void testLangProfile() {
        LangProfile profile = new LangProfile();
        assertEquals(profile.name, null);
    }

    @Test
    public final void testLangProfileStringInt() {
        LangProfile profile = new LangProfile("en");
        assertEquals(profile.name, "en");
    }

    @Test
    public final void testAdd() {
        LangProfile profile = new LangProfile("en");
        profile.add("a");
        assertEquals((int) profile.freq.get("a"), 1);
        profile.add("a");
        assertEquals((int) profile.freq.get("a"), 2);
        profile.omitLessFreq();
    }

    @Test
    public final void testAddIllegally1() {
        LangProfile profile = new LangProfile();
        profile.add("a");
        assertEquals(profile.freq.get("a"), null);
    }

    @Test
    public final void testAddIllegally2() {
        LangProfile profile = new LangProfile("en");
        profile.add("a");
        profile.add("");
        profile.add("abcd");
        assertEquals((int) profile.freq.get("a"), 1);
        assertEquals(profile.freq.get(""), null);
        assertEquals(profile.freq.get("abcd"), null);

    }

    @Test
    public final void testOmitLessFreq() {
        LangProfile profile = new LangProfile("en");
        String[] grams = "a b c \u3042 \u3044 \u3046 \u3048 \u304a \u304b \u304c \u304d \u304e \u304f".split(" ");
        for (int i = 0; i < 5; ++i) {
            for (String g : grams) {
                profile.add(g);
            }
        }
        profile.add("\u3050");

        assertEquals((int) profile.freq.get("a"), 5);
        assertEquals((int) profile.freq.get("\u3042"), 5);
        assertEquals((int) profile.freq.get("\u3050"), 1);
        profile.omitLessFreq();
        assertEquals(profile.freq.get("a"), null);
        assertEquals((int) profile.freq.get("\u3042"), 5);
        assertEquals(profile.freq.get("\u3050"), null);
    }

    @Test
    public final void testOmitLessFreqIllegally() {
        LangProfile profile = new LangProfile();
        profile.omitLessFreq();
    }
}
