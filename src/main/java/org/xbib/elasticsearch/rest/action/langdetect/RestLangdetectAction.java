package org.xbib.elasticsearch.rest.action.langdetect;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.RestStatusToXContentListener;
import org.xbib.elasticsearch.action.langdetect.LangdetectAction;
import org.xbib.elasticsearch.action.langdetect.LangdetectRequest;

import java.io.IOException;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestRequest.Method.POST;

/**
 *
 */
public class RestLangdetectAction extends BaseRestHandler {

    @Inject
    public RestLangdetectAction(Settings settings, RestController controller) {
        super(settings);
        controller.registerHandler(GET, "/_langdetect", this);
        controller.registerHandler(GET, "/_langdetect/{profile}", this);
        controller.registerHandler(POST, "/_langdetect", this);
        controller.registerHandler(POST, "/_langdetect/{profile}", this);
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) throws IOException {
        // read request.param early to "consume" parameter, avoiding HTTP 400
        final String profile = request.param("profile", "");
        return channel -> client.execute(LangdetectAction.INSTANCE,  new LangdetectRequest()
                            .setProfile(profile)
                            .setText(request.content().utf8ToString()),
                    new RestStatusToXContentListener<>(channel));
    }
}