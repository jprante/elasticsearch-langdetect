
package org.xbib.elasticsearch.rest.action.langdetect;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.XContentRestResponse;
import org.elasticsearch.rest.XContentThrowableRestResponse;
import org.elasticsearch.rest.action.support.RestXContentBuilder;

import org.xbib.elasticsearch.action.langdetect.LangdetectAction;
import org.xbib.elasticsearch.action.langdetect.LangdetectRequest;
import org.xbib.elasticsearch.action.langdetect.LangdetectResponse;
import org.xbib.elasticsearch.common.langdetect.Language;

import java.io.IOException;

import static org.elasticsearch.rest.RestRequest.Method.POST;
import static org.elasticsearch.rest.RestStatus.OK;

public class RestLangdetectAction extends BaseRestHandler {

    @Inject
    public RestLangdetectAction(Settings settings, Client client, RestController controller) {
        super(settings, client);
        controller.registerHandler(POST, "/_langdetect", this);
    }

    @Override
    public void handleRequest(final RestRequest request, final RestChannel channel) {
        LangdetectRequest langdetectRequest = new LangdetectRequest().setText(request.content());
        client.execute(LangdetectAction.INSTANCE, langdetectRequest, new ActionListener<LangdetectResponse>() {

            @Override
            public void onResponse(LangdetectResponse response) {
                try {
                    XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
                    builder.startObject();
                    builder.field("ok", true).startArray("languages");
                    for (Language lang : response.getLanguages()) {
                        builder.startObject().field("language", lang.getLanguage())
                                .field("probability", lang.getProbability()).endObject();
                    }
                    builder.endArray().endObject();
                    channel.sendResponse(new XContentRestResponse(request, OK, builder));
                } catch (Exception e) {                    
                    onFailure(e);
                }
            }

            @Override
            public void onFailure(Throwable e) {
                try {
                    channel.sendResponse(new XContentThrowableRestResponse(request, e));
                } catch (IOException e1) {
                    logger.error("Failed to send failure response", e1);
                }
            }
        });
    }
}