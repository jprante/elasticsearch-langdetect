package org.xbib.elasticsearch.action.langdetect.profile;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.single.custom.SingleCustomOperationRequestBuilder;
import org.elasticsearch.client.IndicesAdminClient;

public class LangdetectProfileRequestBuilder extends SingleCustomOperationRequestBuilder<LangdetectProfileRequest, LangdetectProfileResponse, LangdetectProfileRequestBuilder> {

    public LangdetectProfileRequestBuilder(IndicesAdminClient client) {
        super(client, new LangdetectProfileRequest());
    }

    public LangdetectProfileRequestBuilder setProfile(String string) {
        request.setProfile(string);
        return this;
    }

    @Override
    protected void doExecute(ActionListener<LangdetectProfileResponse> listener) {
        client.execute(LangdetectProfileAction.INSTANCE, request, listener);
    }

}
