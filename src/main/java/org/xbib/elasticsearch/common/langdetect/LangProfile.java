package org.xbib.elasticsearch.common.langdetect;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class LangProfile {

    private static final int MINIMUM_FREQ = 2;

    private static final int LESS_FREQ_RATIO = 100000;

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

    public void omitLessFreq() {
        if (name == null) {
            return;
        }
        int threshold = n_words[0] / LESS_FREQ_RATIO;
        if (threshold < MINIMUM_FREQ) {
            threshold = MINIMUM_FREQ;
        }
        Set<String> keys = freq.keySet();
        int roman = 0;
        for (Iterator<String> i = keys.iterator(); i.hasNext(); ) {
            String key = i.next();
            int count = freq.get(key);
            if (count <= threshold) {
                n_words[key.length() - 1] -= count;
                i.remove();
            } else {
                if (key.matches("^[A-Za-z]$")) {
                    roman += count;
                }
            }
        }
        if (roman < n_words[0] / 3) {
            Set<String> keys2 = freq.keySet();
            for (Iterator<String> i = keys2.iterator(); i.hasNext(); ) {
                String key = i.next();
                if (key.matches(".*[A-Za-z].*")) {
                    n_words[key.length() - 1] -= freq.get(key);
                    i.remove();
                }
            }
        }
    }
}
