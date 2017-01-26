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

import static org.elasticsearch.rest.RestRequest.Method.POST;

/**
 *
 */
public class RestLangdetectAction extends BaseRestHandler {

    @Inject
    public RestLangdetectAction(Settings settings, RestController controller) {
        super(settings);
        controller.registerHandler(POST, "/_langdetect", this);
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) throws IOException {
        return channel -> client.execute(LangdetectAction.INSTANCE,  new LangdetectRequest()
                            .setProfile(request.param("profile", ""))
                            .setText(request.content().utf8ToString()),
                    new RestStatusToXContentListener<>(channel));
    }
}