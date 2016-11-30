package org.xbib.elasticsearch.index.mapper.langdetect;

import com.google.common.base.Joiner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.io.Streams;
import org.elasticsearch.common.settings.Settings;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class DetectLanguageTest extends Assert {
    private static final Logger logger = LogManager.getLogger();
    private static final String DEFAULT_LANGUAGES = Joiner.on(",").join(LangdetectService.DEFAULT_LANGUAGES);
    private static final String ALL_DEFAULT_PROFILE_LANGUAGES =
        "af,ar,bg,bn,cs,da,de,el,en,es,et,fa,fi,fr,gu,he,hi,hr,hu,id,it,ja,kn,ko,lt,lv,mk,ml,mr,ne,nl,no,pa,pl,pt,ro," +
        "ru,sk,sl,so,sq,sv,sw,ta,te,th,tl,tr,uk,ur,vi,zh-cn,zh-tw";
    private static final String ALL_SHORT_PROFILE_LANGUAGES =
        "ar,bg,bn,ca,cs,da,de,el,en,es,et,fa,fi,fr,gu,he,hi,hr,hu,id,it,ja,ko,lt,lv,mk,ml,nl,no,pa,pl,pt,ro,ru,si,sq," +
        "sv,ta,te,th,tl,tr,uk,ur,vi,zh-cn,zh-tw";

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
    public void testUdhrAccuraciesDefaultProfileDefaultLanguages() throws IOException {
        testSubstringAccuracies(
            "udhr.tsv",
            new double[][] {
                { 5,   100, 0.26, 0.65 },
                { 10,  100, 0.46, 0.82 },
                { 20,  100, 0.73, 0.94 },
                { 50,  100, 0.85, 0.98 },
                { 100, 100, 0.94, 0.99 },
                { 300, 100, 1.00, 1.00 },
                { 0,   1,   1.00, 1.00 }
            },
            false,
            false
        );
    }

    @Test
    public void testUdhrAccuraciesDefaultProfileAllLanguages() throws IOException {
        testSubstringAccuracies(
            "udhr.tsv",
            new double[][] {
                { 5,   100, 0.25, 0.61 },
                { 10,  100, 0.45, 0.79 },
                { 20,  100, 0.72, 0.92 },
                { 50,  100, 0.86, 0.98 },
                { 100, 100, 0.94, 0.99 },
                { 300, 100, 1.00, 1.00 },
                { 0,   1,   1.00, 1.00 }
            },
            false,
            true
        );
    }

    @Test
    public void testUdhrAccuraciesShortProfileDefaultLanguages() throws IOException {
        testSubstringAccuracies(
            "udhr.tsv",
            new double[][] {
                { 5,   100, 0.16, 0.64 },
                { 10,  100, 0.50, 0.82 },
                { 20,  100, 0.68, 0.93 },
                { 50,  100, 0.86, 0.98 },
                { 100, 100, 0.94, 0.99 },
                { 300, 100, 0.99, 0.99 },
                { 0,   1,   1.00, 1.00 }
            },
            true,
            false
        );
    }

    @Test
    public void testUdhrAccuraciesShortProfileAllLanguages() throws IOException {
        testSubstringAccuracies(
            "udhr.tsv",
            new double[][] {
                { 5,   100, 0.16, 0.64 },
                { 10,  100, 0.49, 0.81 },
                { 20,  100, 0.68, 0.93 },
                { 50,  100, 0.85, 0.98 },
                { 100, 100, 0.94, 0.99 },
                { 300, 100, 0.99, 0.99 },
                { 0,   1,   1.00, 1.00 }
            },
            true,
            true
        );
    }

    @Test
    public void testWordPressTranslationsAccuraciesDefaultProfileDefaultLanguages() throws IOException {
        testSubstringAccuracies(
            "wordpress-translations.tsv",
            new double[][] {
                { 5,  10, 0.25, 0.60 },
                { 10, 10, 0.44, 0.76 },
                { 20, 10, 0.65, 0.88 },
                { 0,  1,  0.78, 0.98 }
            },
            false,
            false
        );
    }

    @Test
    public void testWordPressTranslationsAccuraciesDefaultProfileAllLanguages() throws IOException {
        testSubstringAccuracies(
            "wordpress-translations.tsv",
            new double[][] {
                { 5,  10, 0.17, 0.56 },
                { 10, 10, 0.41, 0.73 },
                { 20, 10, 0.59, 0.87 },
                { 0,  1,  0.78, 0.99 }
            },
            false,
            true
        );
    }

    @Test
    public void testWordPressTranslationsAccuraciesShortProfileDefaultLanguages() throws IOException {
        testSubstringAccuracies(
            "wordpress-translations.tsv",
            new double[][] {
                { 5,  10, 0.23, 0.61 },
                { 10, 10, 0.47, 0.77 },
                { 20, 10, 0.68, 0.90 },
                { 0,  1,  0.94, 0.99 }
            },
            true,
            false
        );
    }

    @Test
    public void testWordPressTranslationsAccuraciesShortProfileAllLanguages() throws IOException {
        testSubstringAccuracies(
            "wordpress-translations.tsv",
            new double[][] {
                { 5,  10, 0.23, 0.61 },
                { 10, 10, 0.40, 0.77 },
                { 20, 10, 0.68, 0.89 },
                { 0,  1,  0.94, 0.99 }
            },
            true,
            true
        );
    }


    /**
     * Test classification accuracies on substrings of texts from a single dataset.
     *
     * For each text and substring length, this test generates a sample of substrings (drawn uniformly with
     * replacement from the set of possible substrings of the given length), runs the language identification code,
     * measures the per-language accuracy (percentage of substrings classified correctly), and fails if the minimum or
     * mean accuracy for the length is below a predetermined threshold.
     *
     * @param datasetPath dataset resource path (see {@link #readMultiLanguageDataset(String)})
     * @param allTrialParams a matrix specifying each trial's parameters. Each row in the matrix must have four items:
     *                       substring length and sample size, which are passed to
     *                       {@link #generateSubstringSample(String, int, int)}, and a per-language accuracy threshold
     *                       and mean accuracy threshold, which are used to determine whether the trial passes or fails
     * @param useShortProfile if true, the short text language profile will be used instead of the default profile
     * @param useAllLanguages if true, all supported languages will be used instead of  just the default ones
     */
    private void testSubstringAccuracies(String datasetPath,
                                         double[][] allTrialParams,
                                         boolean useShortProfile,
                                         boolean useAllLanguages) throws IOException {
        LangdetectService service = new LangdetectService(
            Settings.builder()
                    .put("languages",
                         useAllLanguages ?
                             useShortProfile ? ALL_SHORT_PROFILE_LANGUAGES : ALL_DEFAULT_PROFILE_LANGUAGES :
                             DEFAULT_LANGUAGES)
                    .put("profile", useShortProfile ? "short-text" : "")
                    .build()
        );
        Map<String, List<String>> languageToFullTexts = readMultiLanguageDataset(datasetPath);
        languageToFullTexts.keySet().retainAll(Arrays.asList(service.getSettings().getAsArray("languages")));
        // Sort the languages to make the log output prettier.
        List<String> languages = new ArrayList<>(languageToFullTexts.keySet());
        Collections.sort(languages);
        for (double[] trialParams : allTrialParams) {
            int substringLength = (int) trialParams[0];
            int sampleSize = (int) trialParams[1];
            double minAccuracyThreshold = trialParams[2];
            double meanAccuracyThreshold = trialParams[3];
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
                logger.debug("Substring length: {} Language: {} Accuracy: {}", substringLength, language, accuracy);
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
     * @param path resource path, where the file is in tab-separated format with two columns: language code and text
     * @return a mapping from each language code found in the file to the texts of this language    
     */
    private Map<String, List<String>> readMultiLanguageDataset(String path) throws IOException {
        Map<String, List<String>> languageToFullTexts = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(path),
                                                                          StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitLine = line.split("\t");
                String language = splitLine[0];
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
     * @param substringLength length of each generated substring (set to zero to return a singleton list with the
     *                        text -- sampleSize must be 1 in this case)
     * @param sampleSize number of substrings to include in the sample
     * @return the sample (a list of strings)
     */
    private List<String> generateSubstringSample(String text, int substringLength, int sampleSize) {
        if (substringLength == 0 && sampleSize == 1) {
            return Collections.singletonList(text);
        }
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
