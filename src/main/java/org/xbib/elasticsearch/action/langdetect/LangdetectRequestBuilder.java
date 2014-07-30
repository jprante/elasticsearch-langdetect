package org.xbib.elasticsearch.action.langdetect;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.single.custom.SingleCustomOperationRequestBuilder;
import org.elasticsearch.client.IndicesAdminClient;

public class LangdetectRequestBuilder extends SingleCustomOperationRequestBuilder<LangdetectRequest, LangdetectResponse, LangdetectRequestBuilder> {

    public LangdetectRequestBuilder(IndicesAdminClient client) {
        super(client, new LangdetectRequest());
    }

    @Override
    protected void doExecute(ActionListener<LangdetectResponse> listener) {
        client.execute(LangdetectAction.INSTANCE, request, listener);
    }

}
