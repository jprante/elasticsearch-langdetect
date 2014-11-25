package org.xbib.elasticsearch.rest.action.langdetect;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.support.RestStatusToXContentListener;
import org.xbib.elasticsearch.action.langdetect.profile.LangdetectProfileAction;
import org.xbib.elasticsearch.action.langdetect.profile.LangdetectProfileRequest;
import org.xbib.elasticsearch.action.langdetect.profile.LangdetectProfileResponse;

import static org.elasticsearch.rest.RestRequest.Method.POST;

public class RestLangdetectProfileAction extends BaseRestHandler {

    @Inject
    public RestLangdetectProfileAction(Settings settings, Client client, RestController controller) {
        super(settings, controller, client);
        controller.registerHandler(POST, "/_langdetect/profile", this);
    }

    @Override
    public void handleRequest(final RestRequest request, final RestChannel channel, final Client client) {
        LangdetectProfileRequest langdetectRequest = new LangdetectProfileRequest()
                .setProfile(request.param("profile", "/langdetect/"));
        client.admin().indices().execute(LangdetectProfileAction.INSTANCE, langdetectRequest,
                new RestStatusToXContentListener<LangdetectProfileResponse>(channel));
    }
}