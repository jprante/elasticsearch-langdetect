
package org.xbib.elasticsearch.action.langdetect;

import org.elasticsearch.action.Action;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.internal.InternalGenericClient;

public class LangdetectAction extends Action<LangdetectRequest, LangdetectResponse, LangdetectRequestBuilder> {

   public static final LangdetectAction INSTANCE = new LangdetectAction();
    public static final String NAME = "langdetect";

    private LangdetectAction() {
        super(NAME);
    }
    
    @Override
    public LangdetectRequestBuilder newRequestBuilder(Client client) {
        return new LangdetectRequestBuilder((InternalGenericClient)client);
    }

    @Override
    public LangdetectResponse newResponse() {
        return new LangdetectResponse();
    }
    
}
