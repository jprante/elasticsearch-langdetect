package org.xbib.elasticsearch.common.langdetect;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class Detector extends AbstractLifecycleComponent<Detector> {

    private static final double ALPHA_DEFAULT = 0.5;

    private static final double ALPHA_WIDTH = 0.05;

    private static final int ITERATION_LIMIT = 1000;

    private static final double PROB_THRESHOLD = 0.1;

    private static final double CONV_THRESHOLD = 0.99999;

    private static final int BASE_FREQ = 10000;

    private static final String UNKNOWN_LANG = "unknown";

    private Map<String, double[]> wordLangProbMap = new HashMap<String, double[]>();

    private List<String> langlist = new LinkedList<String>();

    private double alpha;

    private int n_trial;

    private double[] priorMap;

    public Detector() {
        super(ImmutableSettings.EMPTY);
    }

    @Inject
    public Detector(Settings settings) {
        super(settings);
        try {
            loadDefaultProfiles();
        } catch (IOException e) {
            throw new ElasticsearchException(e.getMessage());
        }
        reset();
    }

    @Override
    protected void doStart() throws ElasticsearchException {
    }

    @Override
    protected void doStop() throws ElasticsearchException {
    }

    @Override
    protected void doClose() throws ElasticsearchException {
    }

    public void loadDefaultProfiles() throws IOException {
        load(ResourceBundle.getBundle(getClass().getPackage().getName() + ".languages"));
        reset();
    }

    public void loadProfiles(String bundleName) throws IOException {
        load(ResourceBundle.getBundle(bundleName));
    }

    public void load(ResourceBundle bundle) throws IOException {
        Enumeration<String> en = bundle.getKeys();
        int index = 0;
        int size = bundle.keySet().size();
        while (en.hasMoreElements()) {
            String line = en.nextElement();
            InputStream in = getClass().getResourceAsStream(line);
            if (in == null) {
                throw new IOException("i/o error in profile locading");
            }
            loadProfile(in, index++, size);
        }
    }

    public void loadProfile(InputStream in, int index, int langsize) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        LangProfile profile = mapper.readValue(in, LangProfile.class);
        addProfile(profile, index, langsize);
    }

    public void addProfile(LangProfile profile, int index, int langsize) throws IOException {
        String lang = profile.name;
        if (langlist.contains(lang)) {
            throw new IOException("duplicate the same language profile");
        }
        langlist.add(lang);
        for (String word : profile.freq.keySet()) {
            if (!wordLangProbMap.containsKey(word)) {
                wordLangProbMap.put(word, new double[langsize]);
            }
            int length = word.length();
            if (length >= 1 && length <= 3) {
                double prob = profile.freq.get(word).doubleValue() / profile.n_words[length - 1];
                wordLangProbMap.get(word)[index] = prob;
            }
        }
    }

    public Detector setWordLangProbMap(Map<String, double[]> wordLangProbMap) {
        this.wordLangProbMap = wordLangProbMap;
        return this;
    }

    public Detector setLangList(List<String> langlist) {
        this.langlist = langlist;
        return this;
    }

    public List<String> getLangList() {
        return Collections.unmodifiableList(langlist);
    }

    public final void reset() {
        this.priorMap = null;
        this.alpha = ALPHA_DEFAULT;
        this.n_trial = 7;
    }

    /**
     * Set smoothing parameter. The default value is 0.5(i.e. Expected
     * Likelihood Estimate).
     *
     * @param alpha the smoothing parameter
     */
    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    /**
     * Set prior information about language probabilities.
     *
     * @param priorMap the priorMap to set
     * @throws LanguageDetectionException
     */
    public void setPriorMap(HashMap<String, Double> priorMap) throws LanguageDetectionException {
        this.priorMap = new double[langlist.size()];
        double sump = 0;
        for (int i = 0; i < this.priorMap.length; ++i) {
            String lang = langlist.get(i);
            if (priorMap.containsKey(lang)) {
                double p = priorMap.get(lang);
                if (p < 0) {
                    throw new LanguageDetectionException("Prior probability must be non-negative");
                }
                this.priorMap[i] = p;
                sump += p;
            }
        }
        if (sump <= 0) {
            throw new LanguageDetectionException("More one of prior probability must be non-zero");
        }
        for (int i = 0; i < this.priorMap.length; ++i) {
            this.priorMap[i] /= sump;
        }
    }

    private final static Pattern word = Pattern.compile("[\\P{IsWord}]", Pattern.UNICODE_CHARACTER_CLASS);

    /**
     * Detect language of the target text and return the language name which has
     * the highest probability.
     *
     * @return detected language name which has most probability.
     * @throws LanguageDetectionException
     */
    public String detect(String text) throws LanguageDetectionException {
        List<Language> probabilities =
                detectAll(text.replaceAll(word.pattern(), " "));
        //detectAll(normalize(text));
        if (probabilities.size() > 0) {
            return probabilities.get(0).getLanguage();
        }
        return UNKNOWN_LANG;
    }

    public List<Language> detectAll(String text) throws LanguageDetectionException {
        return sortProbability(detectBlock(/*normalize(text)*/text.replaceAll(word.pattern(), " ")));
    }

    private double[] detectBlock(String text) throws LanguageDetectionException {
        //text = clean(text);
        List<String> ngrams = extractNGrams(text);
        if (ngrams.isEmpty()) {
            throw new LanguageDetectionException("no features in text");
        }
        double[] langprob = new double[langlist.size()];
        Random rand = new Random();
        Long seed = 0L;
        if (seed != null) {
            rand.setSeed(seed);
        }
        for (int t = 0; t < n_trial; ++t) {
            double[] prob = initProbability();
            double a = this.alpha + rand.nextGaussian() * ALPHA_WIDTH;
            for (int i = 0; ; ++i) {
                int r = rand.nextInt(ngrams.size());
                updateLangProb(prob, ngrams.get(r), a);
                if (i % 5 == 0) {
                    if (normalizeProb(prob) > CONV_THRESHOLD || i >= ITERATION_LIMIT) {
                        break;
                    }
                }
            }
            for (int j = 0; j < langprob.length; ++j) {
                langprob[j] += prob[j] / n_trial;
            }
        }
        return langprob;
    }

    private double[] initProbability() {
        double[] prob = new double[langlist.size()];
        if (priorMap != null) {
            for (int i = 0; i < prob.length; ++i) {
                prob[i] = priorMap[i];
            }
        } else {
            for (int i = 0; i < prob.length; ++i) {
                prob[i] = 1.0 / langlist.size();
            }
        }
        return prob;
    }

    private List<String> extractNGrams(String text) {
        List<String> list = new ArrayList<String>();
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
        return list;
    }

    private boolean updateLangProb(double[] prob, String word, double alpha) {
        if (word == null || !wordLangProbMap.containsKey(word)) {
            return false;
        }
        double[] langProbMap = wordLangProbMap.get(word);
        double weight = alpha / BASE_FREQ;
        for (int i = 0; i < prob.length; ++i) {
            prob[i] *= weight + langProbMap[i];
        }
        return true;
    }

    private double normalizeProb(double[] prob) {
        double maxp = 0, sump = 0;
        for (int i = 0; i < prob.length; ++i) {
            sump += prob[i];
        }
        for (int i = 0; i < prob.length; ++i) {
            double p = prob[i] / sump;
            if (maxp < p) {
                maxp = p;
            }
            prob[i] = p;
        }
        return maxp;
    }

    private List<Language> sortProbability(double[] prob) {
        List<Language> list = new ArrayList<Language>();
        for (int j = 0; j < prob.length; ++j) {
            double p = prob[j];
            if (p > PROB_THRESHOLD) {
                for (int i = 0; i <= list.size(); ++i) {
                    if (i == list.size() || list.get(i).getProbability() < p) {
                        list.add(i, new Language(langlist.get(j), p));
                        break;
                    }
                }
            }
        }
        return list;
    }

}
