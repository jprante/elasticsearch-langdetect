package org.xbib.elasticsearch.action.langdetect.profile;

import org.elasticsearch.action.admin.indices.IndicesAction;
import org.elasticsearch.client.IndicesAdminClient;

public class LangdetectProfileAction extends IndicesAction<LangdetectProfileRequest, LangdetectProfileResponse, LangdetectProfileRequestBuilder> {

    public static final String NAME = "langdetect.profile";

    public static final LangdetectProfileAction INSTANCE = new LangdetectProfileAction();

    private LangdetectProfileAction() {
        super(NAME);
    }

    @Override
    public LangdetectProfileRequestBuilder newRequestBuilder(IndicesAdminClient client) {
        return new LangdetectProfileRequestBuilder(client);
    }

    @Override
    public LangdetectProfileResponse newResponse() {
        return new LangdetectProfileResponse();
    }

}
