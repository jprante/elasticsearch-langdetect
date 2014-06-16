package org.xbib.elasticsearch.common.langdetect;

import org.elasticsearch.common.io.Streams;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

public class DetectLanguageTest extends Assert {

    @Test
    public void testEnglish() throws IOException {
        testLanguage("/english.txt", "en");
    }

    @Test
    public void testChinese() throws IOException {
        testLanguage("/chinese.txt", "zh-cn");
    }

    @Test
    public void testJapanese() throws IOException {
        testLanguage("/japanese.txt", "ja");
    }

    @Test
    public void testKorean() throws IOException {
        testLanguage("/korean.txt", "ko");
    }

    private void testLanguage(String path, String lang) throws IOException {
        Reader reader = new InputStreamReader(getClass().getResourceAsStream(path), "UTF-8");
        Writer writer = new StringWriter();
        Streams.copy(reader, writer);
        reader.close();
        writer.close();
        Detector detect = new Detector();
        detect.loadDefaultProfiles();
        assertEquals(detect.detect(writer.toString()), lang);
    }

}
