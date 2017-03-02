package org.xbib.elasticsearch.common.langdetect;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.settings.Settings;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

/**
 *
 */
public class LangdetectService {

    private static final String[] DEFAULT_LANGUAGES = new String[]{
            // "af",
            "ar",
            "bg",
            "bn",
            "cs",
            "da",
            "de",
            "el",
            "en",
            "es",
            "et",
            "fa",
            "fi",
            "fr",
            "gu",
            "he",
            "hi",
            "hr",
            "hu",
            "id",
            "it",
            "ja",
            // "kn",
            "ko",
            "lt",
            "lv",
            "mk",
            "ml",
            // "mr",
            // "ne",
            "nl",
            "no",
            "pa",
            "pl",
            "pt",
            "ro",
            "ru",
            // "sk",
            //"sl",
            // "so",
            "sq",
            "sv",
            // "sw",
            "ta",
            "te",
            "th",
            "tl",
            "tr",
            "uk",
            "ur",
            "vi",
            "zh-cn",
            "zh-tw"
    };
    private static final Logger logger = LogManager.getLogger(LangdetectService.class.getName());
    private static final Pattern word = Pattern.compile("[\\P{IsWord}]", Pattern.UNICODE_CHARACTER_CLASS);
    private static final Settings DEFAULT_SETTINGS = Settings.builder()
            .putArray("languages", DEFAULT_LANGUAGES)
            .build();
    private final Settings settings;
    private Map<String, double[]> wordLangProbMap = new HashMap<>();

    private List<String> langlist = new LinkedList<>();

    private Map<String, String> langmap = new HashMap<>();

    private String profile;

    private double alpha;

    private double alphaWidth;

    private int nTrial;

    private double[] priorMap;

    private int iterationLimit;

    private double probThreshold;

    private double convThreshold;

    private int baseFreq;

    private Pattern filterPattern;

    private boolean isStarted;

    public LangdetectService() {
        this(DEFAULT_SETTINGS);
    }

    public LangdetectService(Settings settings) {
        this(settings, null);
    }

    public LangdetectService(Settings settings, String profile) {
        this.settings = settings;
        this.profile = settings.get("profile", profile);
        load(settings);
        init();
    }

    public Settings getSettings() {
        return settings;
    }

    private void load(Settings settings) {
        if (settings.equals(Settings.EMPTY)) {
            return;
        }
        try {
            String[] keys = settings.getAsArray("languages");
            if (keys.length == 0) {
                keys = DEFAULT_LANGUAGES;
            }
            int index = 0;
            int size = keys.length;
            for (String key : keys) {
                if (key != null && !key.isEmpty()) {
                    loadProfileFromResource(key, index++, size);
                }
            }
            logger.debug("language detection service installed for {}", langlist);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ElasticsearchException(e.getMessage() + " profile=" + profile);
        }
        try {
            // map by settings
            Settings map = Settings.builder().put(settings.getByPrefix("map.")).build();
            if (map.getAsMap().isEmpty()) {
                // is in "map" a resource name?
                String s = settings.get("map") != null ?
                        settings.get("map") : this.profile + "language.json";
                InputStream in = getClass().getResourceAsStream(s);
                if (in != null) {
                    map = Settings.builder().loadFromStream(s, in).build();
                }
            }
            this.langmap = map.getAsMap();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ElasticsearchException(e.getMessage());
        }
    }

    private void init() {
        this.priorMap = null;
        this.nTrial = settings.getAsInt("number_of_trials", 7);
        this.alpha = settings.getAsDouble("alpha", 0.5);
        this.alphaWidth = settings.getAsDouble("alpha_width", 0.05);
        this.iterationLimit = settings.getAsInt("iteration_limit", 10000);
        this.probThreshold = settings.getAsDouble("prob_threshold", 0.1);
        this.convThreshold = settings.getAsDouble("conv_threshold", 0.99999);
        this.baseFreq = settings.getAsInt("base_freq", 10000);
        this.filterPattern = settings.get("pattern") != null ?
                Pattern.compile(settings.get("pattern"), Pattern.UNICODE_CHARACTER_CLASS) : null;
        isStarted = true;
    }

    public void loadProfileFromResource(String resource, int index, int langsize) throws IOException {
        String thisProfile = "/langdetect/" + (this.profile != null ? this.profile + "/" : "");
        InputStream in = getClass().getResourceAsStream(thisProfile + resource);
        if (in == null) {
            throw new IOException("profile '" + resource + "' not found");
        }
        LangProfile langProfile = new LangProfile();
        langProfile.read(in);
        addProfile(langProfile, index, langsize);
    }

    public void addProfile(LangProfile profile, int index, int langsize) throws IOException {
        String lang = profile.getName();
        if (langlist.contains(lang)) {
            throw new IOException("duplicate of the same language profile: " + lang);
        }
        langlist.add(lang);
        for (String s : profile.getFreq().keySet()) {
            if (!wordLangProbMap.containsKey(s)) {
                wordLangProbMap.put(s, new double[langsize]);
            }
            int length = s.length();
            if (length >= 1 && length <= 3) {
                double prob = profile.getFreq().get(s).doubleValue() / profile.getNWords().get(length - 1);
                wordLangProbMap.get(s)[index] = prob;
            }
        }
    }

    public String getProfile() {
        return profile;
    }

    public List<Language> detectAll(String text) throws LanguageDetectionException {
        if (!isStarted) {
            load(settings);
            init();
        }
        List<Language> languages = new ArrayList<>();
        if (filterPattern != null && !filterPattern.matcher(text).matches()) {
            return languages;
        }
        List<String> list = new ArrayList<>();
        languages = sortProbability(languages, detectBlock(list, text));
        return languages.subList(0, Math.min(languages.size(), settings.getAsInt("max", languages.size())));
    }

    private double[] detectBlock(List<String> list, String string) throws LanguageDetectionException {
        // clean all non-work characters from text
        String text = string.replaceAll(word.pattern(), " ");
        extractNGrams(list, text);
        double[] langprob = new double[langlist.size()];
        if (list.isEmpty()) {
            return langprob;
        }
        Random rand = new Random();
        Long seed = 0L;
        rand.setSeed(seed);
        for (int t = 0; t < nTrial; ++t) {
            double[] prob = initProbability();
            double a = this.alpha + rand.nextGaussian() * alphaWidth;
            for (int i = 0; ; ++i) {
                int r = rand.nextInt(list.size());
                updateLangProb(prob, list.get(r), a);
                if (i % 5 == 0 && normalizeProb(prob) > convThreshold || i >= iterationLimit) {
                    break;
                }
            }
            for (int j = 0; j < langprob.length; ++j) {
                langprob[j] += prob[j] / nTrial;
            }
        }
        return langprob;
    }

    private double[] initProbability() {
        double[] prob = new double[langlist.size()];
        if (priorMap != null) {
            System.arraycopy(priorMap, 0, prob, 0, prob.length);
        } else {
            for (int i = 0; i < prob.length; ++i) {
                prob[i] = 1.0 / langlist.size();
            }
        }
        return prob;
    }

    private void extractNGrams(List<String> list, String text) {
        NGram ngram = new NGram();
        for (int i = 0; i < text.length(); ++i) {
            ngram.addChar(text.charAt(i));
            for (int n = 1; n <= NGram.N_GRAM; ++n) {
                String w = ngram.get(n);
                if (w != null && wordLangProbMap.containsKey(w)) {
                    list.add(w);
                }
            }
        }
    }

    private boolean updateLangProb(double[] prob, String word, double alpha) {
        if (word == null || !wordLangProbMap.containsKey(word)) {
            return false;
        }
        double[] langProbMap = wordLangProbMap.get(word);
        double weight = alpha / baseFreq;
        for (int i = 0; i < prob.length; ++i) {
            prob[i] *= weight + langProbMap[i];
        }
        return true;
    }

    private double normalizeProb(double[] prob) {
        if (prob.length == 0) {
            return 0d;
        }
        double sump = prob[0];
        for (int i = 1; i < prob.length; i++) {
            sump += prob[i];
        }
        double maxp = 0d;
        for (int i = 0; i < prob.length; i++) {
            double p = prob[i] / sump;
            if (maxp < p) {
                maxp = p;
            }
            prob[i] = p;
        }
        return maxp;
    }

    private List<Language> sortProbability(List<Language> list, double[] prob) {
        for (int j = 0; j < prob.length; ++j) {
            double p = prob[j];
            if (p > probThreshold) {
                for (int i = 0; i <= list.size(); ++i) {
                    if (i == list.size() || list.get(i).getProbability() < p) {
                        String code = langlist.get(j);
                        if (langmap != null && langmap.containsKey(code)) {
                            code = langmap.get(code);
                        }
                        list.add(i, new Language(code, p));
                        break;
                    }
                }
            }
        }
        return list;
    }
}
