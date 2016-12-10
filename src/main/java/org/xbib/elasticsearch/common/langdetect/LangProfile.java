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
    private final String name;
    private final Map<String, Long> freq = new HashMap<>();
    private final List<Long> n_words = new ArrayList<>(NGram.N_GRAM);

    /**
     * Create an empty language profile.
     */
    public LangProfile(String name) {
        this.name = name;
        for (int i = 0; i < NGram.N_GRAM; i++) {
            n_words.add(0L);
        }
    }

    /**
     * Create a language profile from a JSON input stream.
     */
    @SuppressWarnings("unchecked")
    public LangProfile(InputStream input) throws IOException {
        XContentParser parser = XContentFactory.xContent(XContentType.JSON).createParser(input);
        Map<String, Object> map = parser.map();
        this.name = (String) map.get("name");
        // Explicity convert the numbers because they may get parsed as Integers or Longs.
        for (Map.Entry<String, Number> entry : ((Map<String, Number>) map.get("freq")).entrySet()) {
            freq.put(entry.getKey(), entry.getValue().longValue());
        }
        for (Number n : (List<Number>) map.get("n_words")) {
            n_words.add(n.longValue());
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
            freq.put(gram, 1L);
        }
    }

    public String getName() {
        return name;
    }

    public List<Long> getNWords() {
        return n_words;
    }

    public Map<String, Long> getFreq() {
        return freq;
    }
}
