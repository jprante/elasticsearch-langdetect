package org.xbib.elasticsearch.action.langdetect;

import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;

/**
 *
 */
public class LangdetectRequestBuilder extends ActionRequestBuilder<LangdetectRequest, LangdetectResponse, LangdetectRequestBuilder> {

    public LangdetectRequestBuilder(ElasticsearchClient client) {
        super(client, LangdetectAction.INSTANCE, new LangdetectRequest());
    }

    public LangdetectRequestBuilder setProfile(String string) {
        request.setProfile(string);
        return this;
    }

    public LangdetectRequestBuilder setText(String string) {
        request.setText(string);
        return this;
    }

}
