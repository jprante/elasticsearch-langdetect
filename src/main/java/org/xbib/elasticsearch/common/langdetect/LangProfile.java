package org.xbib.elasticsearch.common.langdetect;

import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class LangProfile {

    private String name;

    private Map<String, Integer> freq;

    private List<Integer> nWords;

    public LangProfile() {
        this.freq = new HashMap<>();
        this.nWords = new ArrayList<>(NGram.N_GRAM);
        for (int i = 0; i < NGram.N_GRAM; i++) {
            nWords.add(0);
        }
    }

    public void add(String gram) {
        if (name == null || gram == null) {
            return;
        }
        int len = gram.length();
        if (len < 1 || len > NGram.N_GRAM) {
            return;
        }
        nWords.set(len - 1, nWords.get(len - 1) + 1);
        if (freq.containsKey(gram)) {
            freq.put(gram, freq.get(gram) + 1);
        } else {
            freq.put(gram, 1);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Integer> getNWords() {
        return nWords;
    }

    public Map<String, Integer> getFreq() {
        return freq;
    }

    public void setFreq(Map<String, Integer> freq) {
        this.freq = freq;
    }

    @SuppressWarnings("unchecked")
    public void read(InputStream input) throws IOException {
        XContentParser parser = XContentFactory.xContent(XContentType.JSON).createParser(null, input);
        Map<String, Object> map = parser.map();
        freq = (Map<String, Integer>) map.get("freq");
        name = (String) map.get("name");
        nWords = (List<Integer>) map.get("n_words");
    }

}
