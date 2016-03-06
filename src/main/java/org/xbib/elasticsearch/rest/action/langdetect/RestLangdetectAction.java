package org.xbib.elasticsearch.rest.action.langdetect;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.support.RestStatusToXContentListener;
import org.xbib.elasticsearch.action.langdetect.LangdetectAction;
import org.xbib.elasticsearch.action.langdetect.LangdetectRequest;
import org.xbib.elasticsearch.action.langdetect.LangdetectResponse;

import static org.elasticsearch.rest.RestRequest.Method.POST;

public class RestLangdetectAction extends BaseRestHandler {

    @Inject
    public RestLangdetectAction(Settings settings, Client client, RestController controller) {
        super(settings, controller, client);
        controller.registerHandler(POST, "/_langdetect", this);
    }

    @Override
    public void handleRequest(final RestRequest request, final RestChannel channel, final Client client) {
        LangdetectRequest langdetectRequest = new LangdetectRequest()
                .setProfile(request.param("profile", "/langdetect/"))
                .setText(request.content().toUtf8());
        client.execute(LangdetectAction.INSTANCE, langdetectRequest,
                new RestStatusToXContentListener<LangdetectResponse>(channel));
    }
}