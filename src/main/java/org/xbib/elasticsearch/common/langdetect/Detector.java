/*
 * Licensed to Elastic Search and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Elastic Search licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.xbib.elasticsearch.common.langdetect;

import java.lang.Character.UnicodeBlock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

public class Detector {

    private static final double ALPHA_DEFAULT = 0.5;
    private static final double ALPHA_WIDTH = 0.05;
    private static final int ITERATION_LIMIT = 1000;
    private static final double PROB_THRESHOLD = 0.1;
    private static final double CONV_THRESHOLD = 0.99999;
    private static final int BASE_FREQ = 10000;
    private static final String UNKNOWN_LANG = "unknown";
    private static final Pattern URL_REGEX = Pattern.compile("https?://[-_.?&~;+=/#0-9A-Za-z]+");
    private static final Pattern MAIL_REGEX = Pattern.compile("[-_.0-9A-Za-z]+@[-_0-9A-Za-z]+[-_.0-9A-Za-z]+");
    private final Map<String, double[]> wordLangProbMap;
    private final List<String> langlist;
    private double alpha;
    private int n_trial;
    private double[] priorMap;
    private Long seed = 0L;

    public Detector(Map<String, double[]> wordLangProbMap, List<String> langlist) {
        this.wordLangProbMap = wordLangProbMap;
        this.langlist = langlist;
        reset();
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
     * @throws LangDetectException
     */
    public void setPriorMap(HashMap<String, Double> priorMap) throws LanguageDetectionException {
        this.priorMap = new double[langlist.size()];
        double sump = 0;
        for (int i = 0; i < this.priorMap.length; ++i) {
            String lang = langlist.get(i);
            if (priorMap.containsKey(lang)) {
                double p = priorMap.get(lang);
                if (p < 0) {
                    throw new LanguageDetectionException("Prior probability must be non-negative.");
                }
                this.priorMap[i] = p;
                sump += p;
            }
        }
        if (sump <= 0) {
            throw new LanguageDetectionException("More one of prior probability must be non-zero.");
        }
        for (int i = 0; i < this.priorMap.length; ++i) {
            this.priorMap[i] /= sump;
        }
    }

    /**
     * Detect language of the target text and return the language name which has
     * the highest probability.
     *
     * @return detected language name which has most probability.
     * @throws LangDetectException 
     */
    public String detect(String text) throws LanguageDetectionException {
        List<Language> probabilities = detectAll(normalize(text));
        if (probabilities.size() > 0) {
            return probabilities.get(0).getLanguage();
        }
        return UNKNOWN_LANG;
    }

    public List<Language> detectAll(String text) throws LanguageDetectionException {
        return sortProbability(detectBlock(normalize(text)));
    }

    private double[] detectBlock(String text) throws LanguageDetectionException {
        text = clean(text);
        List<String> ngrams = extractNGrams(text);
        if (ngrams.isEmpty()) {
            throw new LanguageDetectionException( "no features in text");
        }
        double[] langprob = new double[langlist.size()];
        Random rand = new Random();
        if (seed != null) {
            rand.setSeed(seed);
        }
        for (int t = 0; t < n_trial; ++t) {
            double[] prob = initProbability();
            double a = this.alpha + rand.nextGaussian() * ALPHA_WIDTH;
            for (int i = 0;; ++i) {
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
        List<String> list = new ArrayList();
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

    private String normalize(String text) {
        StringBuilder sb = new StringBuilder();
        text = URL_REGEX.matcher(text).replaceAll(" ");
        text = MAIL_REGEX.matcher(text).replaceAll(" ");
        char pre = 0;
        for (int i = 0; i < text.length(); ++i) {
            char c = NGram.normalize(text.charAt(i));
            if (c != ' ' || pre != ' ') {
                sb.append(c);
            }
            pre = c;
        }
        return sb.toString();
    }

    private String clean(String text) {
        int latinCount = 0, nonLatinCount = 0;
        for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            if (c <= 'z' && c >= 'A') {
                ++latinCount;
            } else if (c >= '\u0300' && UnicodeBlock.of(c) != UnicodeBlock.LATIN_EXTENDED_ADDITIONAL) {
                ++nonLatinCount;
            }
        }
        if (latinCount * 2 < nonLatinCount) {
            StringBuilder textWithoutLatin = new StringBuilder();
            for (int i = 0; i < text.length(); ++i) {
                char c = text.charAt(i);
                if (c > 'z' || c < 'A') {
                    textWithoutLatin.append(c);
                }
            }
            text = textWithoutLatin.toString();
        }
        return text;
    }    
}
