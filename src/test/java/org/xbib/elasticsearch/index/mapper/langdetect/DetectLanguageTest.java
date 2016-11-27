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

    /**
     * Test classification accuracies on translations of the Universal Declaration of Human Rights (UDHR).
     *
     * The translations were obtained from http://unicode.org/udhr/. Some minimal processing was done to create the
     * udhr.tsv resource file: matched the dataset's language code with the one returned by the library, and removed
     * each file's English intro and redundant whitespace.
     *
     * For each translation and substring length, this test generates a sample of substrings (drawn uniformly with
     * replacement from the set of possible substrings of the given length), runs the language identification code,
     * measures the per-language accuracy (percentage of substrings classified correctly), and fails if the minimum or
     * mean accuracy for the length is below a predetermined threshold. 
     */
    @Test
    public void testUdhrAccuracies() throws IOException {
        LangdetectService service = new LangdetectService();
        Map<String, List<String>> languageToFullTexts = readMultiLanguageDataset("udhr.tsv");
        // Sort the languages to make the log output prettier.
        List<String> languages = new ArrayList<>(languageToFullTexts.keySet());
        Collections.sort(languages);
        // Group the test parameters: substring length, minimum per-language threshold, and minimum mean threshold. 
        // TODO: Set language-specific thresholds?
        double[][] testParams = { { 5,   0.26, 0.65 },
                                  { 10,  0.46, 0.82 },
                                  { 20,  0.73, 0.94 },
                                  { 50,  0.85, 0.98 },
                                  { 100, 0.94, 0.99 },
                                  { 300, 1.00, 1.00 } };
        // TODO: tune this?
        int sampleSize = 100;
        for (double[] trialParams : testParams) {
            int substringLength = (int) trialParams[0];
            double minAccuracyThreshold = trialParams[1];
            double meanAccuracyThreshold = trialParams[2];
            double sumAccuracies = 0;
            double minAccuracy = Double.POSITIVE_INFINITY;
            for (String language : languages) {
                double numCorrect = 0;
                List<String> fullTexts = languageToFullTexts.get(language);
                for (String text : fullTexts) {
                    for (String substring : generateSubstringSample(text, substringLength, sampleSize)) {
                        if (Objects.equals(getTopLanguageCode(service, substring), language)) {
                            numCorrect++;
                        }
                    }
                }
                double accuracy = numCorrect / (fullTexts.size() * sampleSize);
                sumAccuracies += accuracy;
                minAccuracy = Math.min(minAccuracy, accuracy);
                logger.info("Substring length: {} Language: {} Accuracy: {}", substringLength, language, accuracy);
            }
            double meanAccuracy = sumAccuracies / languages.size();
            logger.info("* Substring length: {} Accuracy: min={} mean={}", substringLength, minAccuracy, meanAccuracy);
            assertTrue(minAccuracy >= minAccuracyThreshold);
            assertTrue(meanAccuracy >= meanAccuracyThreshold);
        }
    }

    /**
     * Test that the contents of the file at the provided path are correctly detected as being in language lang. 
     */
    private void testLanguage(String path, String lang) throws IOException {
        try (Reader reader = new InputStreamReader(getClass().getResourceAsStream(path), StandardCharsets.UTF_8)) {
            assertEquals(getTopLanguageCode(new LangdetectService(), Streams.copyToString(reader)), lang);
        }
    }

    /**
     * Read and parse a multi-language dataset from the given path.
     *
     * @param path location of a file in tab-separated format with two columns: language code and text
     * @return a mapping from each language code found in the file to the texts of this language    
     */
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

    /**
     * Return the text's language as detected by the given service object (may be null if no languages are returned).
     */
    private String getTopLanguageCode(LangdetectService service, String text) throws LanguageDetectionException {
        List<Language> languages = service.detectAll(text);
        return languages.size() > 0 ? languages.get(0).getLanguage() : null;
    }

    /**
     * Generate a random sample of substrings from the given text.
     *
     * Sampling is performed uniformly with replacement from the set of substrings of the provided text, ignoring
     * whitespace-only substrings. The random seed is set to a deterministic function of the method's parameters, so
     * repeated calls to this method with the same parameters will return the same sample.
     *
     * @param text the text from which the substring sample is drawn
     * @param substringLength length of each generated substring
     * @param sampleSize number of substrings to include in the sample
     * @return the sample (a list of strings)
     */
    private List<String> generateSubstringSample(String text, int substringLength, int sampleSize) {
        if (substringLength > text.trim().length()) {
            throw new IllegalArgumentException("Provided text is too short.");
        }
        Random rnd = new Random(Objects.hash(text, substringLength, sampleSize)); 
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
