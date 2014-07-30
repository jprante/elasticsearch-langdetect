package org.xbib.elasticsearch.action.langdetect;

import org.elasticsearch.action.admin.indices.IndicesAction;
import org.elasticsearch.client.IndicesAdminClient;

public class LangdetectAction extends IndicesAction<LangdetectRequest, LangdetectResponse, LangdetectRequestBuilder> {

    public static final String NAME = "langdetect";

    public static final LangdetectAction INSTANCE = new LangdetectAction();

    private LangdetectAction() {
        super(NAME);
    }

    @Override
    public LangdetectRequestBuilder newRequestBuilder(IndicesAdminClient client) {
        return new LangdetectRequestBuilder(client);
    }

    @Override
    public LangdetectResponse newResponse() {
        return new LangdetectResponse();
    }

}
