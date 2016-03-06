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

public class LangProfile {

    private String name;

    private Map<String, Integer> freq;

    private List<Integer> n_words;

    public LangProfile() {
        this.freq = new HashMap<>();
        this.n_words = new ArrayList<>(NGram.N_GRAM);
        for (int i = 0; i < NGram.N_GRAM; i++) {
            n_words.add(0);
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
        n_words.set(len - 1, n_words.get(len -1) + 1);
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

    public List<Integer> getNWords() {
        return n_words;
    }

    public void setFreq(Map<String, Integer> freq) {
        this.freq = freq;
    }

    public Map<String, Integer> getFreq() {
        return freq;
    }

    @SuppressWarnings("unchecked")
    public void read(InputStream input) throws IOException {
        XContentParser parser = XContentFactory.xContent(XContentType.JSON).createParser(input);
        Map<String,Object> map = parser.map();
        freq = (Map<String, Integer>) map.get("freq");
        name = (String)map.get("name");
        n_words = (List<Integer>)map.get("n_words");
    }

}
