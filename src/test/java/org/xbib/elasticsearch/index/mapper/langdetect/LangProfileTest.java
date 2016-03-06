package org.xbib.elasticsearch.index.mapper.langdetect;

import org.junit.Assert;
import org.junit.Test;
import org.xbib.elasticsearch.common.langdetect.LangProfile;

public class LangProfileTest extends Assert {

    @Test
    public final void testLangProfile() {
        LangProfile profile = new LangProfile();
        assertEquals(profile.getName(), null);
    }

    @Test
    public final void testLangProfileStringInt() {
        LangProfile profile = new LangProfile();
        profile.setName("en");
        assertEquals(profile.getName(), "en");
    }

    @Test
    public final void testAdd() {
        LangProfile profile = new LangProfile();
        profile.setName("en");
        profile.add("a");
        assertEquals((int) profile.getFreq().get("a"), 1);
        profile.add("a");
        assertEquals((int) profile.getFreq().get("a"), 2);
        //profile.omitLessFreq();
    }

    @Test
    public final void testAddIllegally1() {
        LangProfile profile = new LangProfile();
        profile.add("a");
        assertEquals(profile.getFreq().get("a"), null);
    }

    @Test
    public final void testAddIllegally2() {
        LangProfile profile = new LangProfile();
        profile.setName("en");
        profile.add("a");
        profile.add("");
        profile.add("abcd");
        assertEquals((int) profile.getFreq().get("a"), 1);
        assertEquals(profile.getFreq().get(""), null);
        assertEquals(profile.getFreq().get("abcd"), null);

    }

    @Test
    public final void testOmitLessFreq() {
        LangProfile profile = new LangProfile();
        profile.setName("en");
        String[] grams = "a b c \u3042 \u3044 \u3046 \u3048 \u304a \u304b \u304c \u304d \u304e \u304f".split(" ");
        for (int i = 0; i < 5; ++i) {
            for (String g : grams) {
                profile.add(g);
            }
        }
        profile.add("\u3050");

        assertEquals((int) profile.getFreq().get("a"), 5);
        assertEquals((int) profile.getFreq().get("\u3042"), 5);
        assertEquals((int) profile.getFreq().get("\u3050"), 1);
        //profile.omitLessFreq();
        //assertEquals(profile.freq.get("a"), null);
        //assertEquals((int) profile.freq.get("\u3042"), 5);
        //assertEquals(profile.freq.get("\u3050"), null);
    }

}
