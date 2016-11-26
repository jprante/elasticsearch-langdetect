package org.xbib.elasticsearch.index.mapper.langdetect;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.io.Streams;
import org.junit.Assert;
import org.junit.Test;
import org.xbib.elasticsearch.common.langdetect.LangdetectService;
import org.xbib.elasticsearch.common.langdetect.Language;
import org.xbib.elasticsearch.common.langdetect.LanguageDetectionException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class DetectLanguageTest extends Assert {
    private static final Logger logger = LogManager.getLogger();

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

    @Test
    public void testUdhrAccuracies() throws IOException { // TODO: document this and the helper methods
        LangdetectService service = new LangdetectService();
        Map<String, List<String>> languageToFullTexts = readMultiLanguageDataset("udhr.tsv");
        // Sort the languages the make the log output prettier.
        List<String> languages = new ArrayList<>(languageToFullTexts.keySet());
        Collections.sort(languages);
        int[] substringLengths = new int[]                     {5,    10,   20,   50,   100};
        double[] expectedMeanAccuracyThresholds = new double[] {0.65, 0.82, 0.94, 0.99, 0.99};
        double[] expectedMinAccuracyThresholds = new double[]  {0.24, 0.46, 0.67, 0.87, 0.93};
        // TODO: tune this?
        int sampleSize = 100;
        for (int i = 0; i < substringLengths.length; i++) {
            int substringLength = substringLengths[i];
            Map<String, Integer> languageToNumTexts = new HashMap<>();
            Map<String, Integer> languageToNumCorrect = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : languageToFullTexts.entrySet()) {
                String language = entry.getKey();
                languageToNumTexts.put(language, 0);
                languageToNumCorrect.put(language, 0);
                for (String text : entry.getValue()) {
                    for (String substring : generateSubstringSample(text, substringLength, sampleSize)) {
                        languageToNumTexts.put(language, languageToNumTexts.get(language) + 1);
                        if (Objects.equals(getTopLanguageCode(service, substring), language)) {
                            languageToNumCorrect.put(language, languageToNumCorrect.get(language) + 1);
                        }
                    }
                }
            }
            double sumAccuracies = 0;
            for (String language : languages) {
                double accuracy = languageToNumCorrect.get(language) / (double) languageToNumTexts.get(language);
                sumAccuracies += accuracy;
                logger.info("Substring length: {} Language: {} Accuracy: {}", substringLength, language, accuracy);
                // TODO: Set language-specific thresholds?
                assertTrue(accuracy >= expectedMinAccuracyThresholds[i]);
            }
            double meanAccuracy = sumAccuracies / languages.size();
            logger.info("* Substring length: {} Mean accuracy: {}", substringLength, meanAccuracy);
            assertTrue(meanAccuracy > expectedMeanAccuracyThresholds[i]);
        }
    }

    private void testLanguage(String path, String lang) throws IOException {
        Reader reader = new InputStreamReader(getClass().getResourceAsStream(path), StandardCharsets.UTF_8);
        Writer writer = new StringWriter();
        Streams.copy(reader, writer);
        reader.close();
        writer.close();
        assertEquals(getTopLanguageCode(new LangdetectService(), writer.toString()), lang);
    }

    private Map<String, List<String>> readMultiLanguageDataset(String path) throws IOException {
        // TODO: investigate why some languages are commented out
        Set<String> supportedLanguages = new HashSet<>(Arrays.asList(LangdetectService.DEFAULT_LANGUAGES));
        Map<String, List<String>> languageToFullTexts = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(path),
                StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitLine = line.split("\t");
                String language = splitLine[0];
                if (!supportedLanguages.contains(language)) {
                    continue;
                }
                if (!languageToFullTexts.containsKey(language)) {
                    languageToFullTexts.put(language, new ArrayList<String>());
                }
                languageToFullTexts.get(language).add(splitLine[1]);
            }
        }
        return languageToFullTexts;        
    }
    
    private String getTopLanguageCode(LangdetectService service, String text) throws LanguageDetectionException {
        List<Language> languages = service.detectAll(text);
        return languages.size() > 0 ? languages.get(0).getLanguage() : null;
    }

    private List<String> generateSubstringSample(String text, int substringLength, int sampleSize) {
        if (substringLength > text.trim().length()) {
            throw new IllegalArgumentException("Provided text is too short.");
        }
        Random rnd = new Random(0); 
        List<String> sample = new ArrayList<>(sampleSize);
        while (sample.size() < sampleSize) {
            int startIndex = rnd.nextInt(text.length() - substringLength + 1);
            String substring = text.substring(startIndex, startIndex + substringLength);
            if (!substring.trim().isEmpty()) {
                sample.add(substring);
            }
        }
        return sample;
    }
}
