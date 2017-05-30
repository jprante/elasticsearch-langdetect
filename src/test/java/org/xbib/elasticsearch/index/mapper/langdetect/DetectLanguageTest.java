package org.xbib.elasticsearch.index.mapper.langdetect;

import org.elasticsearch.common.io.Streams;
import org.junit.Assert;
import org.junit.Test;
import org.xbib.elasticsearch.common.langdetect.LangdetectService;
import org.xbib.elasticsearch.common.langdetect.Language;
import org.xbib.elasticsearch.common.langdetect.LanguageDetectionException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;

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

    /**
     * Test that the contents of the file at the provided path are correctly detected as being in language lang. 
     */
    private void testLanguage(String path, String lang) throws IOException {
        try (Reader reader = new InputStreamReader(getClass().getResourceAsStream(path), StandardCharsets.UTF_8)) {
            assertEquals(lang, getTopLanguageCode(new LangdetectService(), Streams.copyToString(reader)));
        }
    }

    /**
     * Return the text's language as detected by the given service object (may be null if no languages are returned).
     */
    static String getTopLanguageCode(LangdetectService service, String text) throws LanguageDetectionException {
        List<Language> languages = service.detectAll(text);
        return languages.size() > 0 ? languages.get(0).getLanguage() : null;
    }
}
