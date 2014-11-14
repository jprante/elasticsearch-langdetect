package org.xbib.elasticsearch.index.analysis.langdetect;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is used by ObjectMapper, it requires public attributes
 */
public class LangProfile {

    public String name = null;

    public Map<String, Integer> freq = new HashMap<String, Integer>();

    public int[] n_words = new int[NGram.N_GRAM];

    public LangProfile() {
    }

    public LangProfile(String name) {
        this.name = name;
    }

    public void add(String gram) {
        if (name == null || gram == null) {
            return;
        }
        int len = gram.length();
        if (len < 1 || len > NGram.N_GRAM) {
            return;
        }
        ++n_words[len - 1];
        if (freq.containsKey(gram)) {
            freq.put(gram, freq.get(gram) + 1);
        } else {
            freq.put(gram, 1);
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setNWords() {
        this.n_words = n_words;
    }

    public int[] getNWords() {
        return n_words;
    }

    public void setFreq(Map<String, Integer> freq) {
        this.freq = freq;
    }

    public Map<String, Integer> getFreq() {
        return freq;
    }

}
