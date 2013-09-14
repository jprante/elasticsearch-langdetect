
package org.xbib.elasticsearch.action.langdetect;

import java.io.IOException;
import java.util.List;

import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;

import org.xbib.elasticsearch.common.langdetect.Language;

public class LangdetectResponse extends ActionResponse implements ToXContent {

    private List<Language> languages;

    public LangdetectResponse() {
    }
    
    public LangdetectResponse(List<Language> languages) {
        this.languages = languages;
    } 
    
    public List<Language> getLanguages() {
        return languages;
    }    

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startArray("languages");
        for (Language lang : languages) {
              builder.startObject().field("language", lang.getLanguage())
                      .field("probability", lang.getProbability());
        }
        builder.endArray();
        return builder;
    }
    
}
