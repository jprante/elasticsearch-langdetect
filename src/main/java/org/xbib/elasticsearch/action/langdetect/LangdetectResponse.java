package org.xbib.elasticsearch.action.langdetect;

import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.common.xcontent.StatusToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.RestStatus;
import org.xbib.elasticsearch.index.analysis.langdetect.Language;

import java.io.IOException;
import java.util.List;

import static org.elasticsearch.rest.RestStatus.OK;

public class LangdetectResponse extends ActionResponse implements StatusToXContent {

    private List<Language> languages;

    public LangdetectResponse() {
    }

    public LangdetectResponse setLanguages(List<Language> languages) {
        this.languages = languages;
        return this;
    }

    public List<Language> getLanguages() {
        return languages;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startArray("languages");
        for (Language lang : languages) {
            builder.startObject().field("language", lang.getLanguage())
                    .field("probability", lang.getProbability()).endObject();
        }
        builder.endArray();
        return builder;
    }

    @Override
    public RestStatus status() {
        return OK;
    }
}
