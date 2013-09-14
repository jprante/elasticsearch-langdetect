
package org.xbib.elasticsearch.action.langdetect;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.single.custom.SingleCustomOperationRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.internal.InternalGenericClient;


public class LangdetectRequestBuilder extends SingleCustomOperationRequestBuilder<LangdetectRequest, LangdetectResponse, LangdetectRequestBuilder> {

    public LangdetectRequestBuilder(InternalGenericClient client) {
        super(client, new LangdetectRequest());
    }    
    
    @Override
    protected void doExecute(ActionListener<LangdetectResponse> listener) {
        ((Client)client).execute(LangdetectAction.INSTANCE, request, listener);
    }
    
}
