package org.xbib.elasticsearch.index.mapper.langdetect;

import org.elasticsearch.common.io.Streams;
import org.junit.Assert;
import org.junit.Test;
import org.xbib.elasticsearch.common.langdetect.LangdetectService;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public class DetectLanguageTest extends Assert {

    @Test
    public void testEnglish() throws IOException {
        testLanguage("english.txt", "en");
    }

    @Test
    public void testChinese() throws IOException {
        testLanguage("chinese.txt", "zh-cn");
    }

    @Test
    public void testJapanese() throws IOException {
        testLanguage("japanese.txt", "ja");
    }

    @Test
    public void testKorean() throws IOException {
        testLanguage("korean.txt", "ko");
    }

    private void testLanguage(String path, String lang) throws IOException {
        Reader reader = new InputStreamReader(getClass().getResourceAsStream(path), StandardCharsets.UTF_8);
        Writer writer = new StringWriter();
        Streams.copy(reader, writer);
        reader.close();
        writer.close();
        LangdetectService detect = new LangdetectService();
        assertEquals(lang, detect.detectAll(writer.toString()).get(0).getLanguage());
    }

}
