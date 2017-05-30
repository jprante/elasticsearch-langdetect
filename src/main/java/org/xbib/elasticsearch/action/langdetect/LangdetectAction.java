package org.xbib.elasticsearch.action.langdetect;

import org.elasticsearch.action.Action;
import org.elasticsearch.client.ElasticsearchClient;

/**
 *
 */
public class LangdetectAction extends Action<LangdetectRequest, LangdetectResponse, LangdetectRequestBuilder> {

    public static final String NAME = "langdetect";

    public static final LangdetectAction INSTANCE = new LangdetectAction();

    private LangdetectAction() {
        super(NAME);
    }

    @Override
    public LangdetectRequestBuilder newRequestBuilder(ElasticsearchClient client) {
        return new LangdetectRequestBuilder(client);
    }

    @Override
    public LangdetectResponse newResponse() {
        return new LangdetectResponse();
    }
}
