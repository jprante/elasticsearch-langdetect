package org.xbib.elasticsearch.index.analysis.langdetect;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Streamable;

import java.io.IOException;

public class Language implements Streamable {

    private String lang;
    private double prob;

    public Language(String lang, double prob) {
        this.lang = lang;
        this.prob = prob;
    }

    public String getLanguage() {
        return lang;
    }

    public double getProbability() {
        return prob;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        this.lang = in.readString();
        this.prob = in.readDouble();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeString(lang);
        out.writeDouble(prob);
    }
}
