package org.xbib.elasticsearch.rest.action.langdetect;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.CheckedConsumer;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
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
        final LangdetectRequest langdetectRequest = new LangdetectRequest();
        langdetectRequest.setText(request.param("text"));
        langdetectRequest.setProfile(request.param("profile", ""));
        withContent(request, parser -> {
            if (parser != null) {
                XContentParser.Token token;
                while ((token = parser.nextToken()) != null) {
                    if (token == XContentParser.Token.VALUE_STRING) {
                        if ("text".equals(parser.currentName())) {
                            langdetectRequest.setText(parser.text());
                        } else if ("profile".equals(parser.currentName())) {
                            langdetectRequest.setProfile(parser.text());
                        }
                    }
                }
            }
        });
        return channel -> client.execute(LangdetectAction.INSTANCE, langdetectRequest,
                    new RestStatusToXContentListener<>(channel));
    }

    private void withContent(RestRequest restRequest, CheckedConsumer<XContentParser, IOException> withParser)
            throws IOException {
        BytesReference content = restRequest.content();
        XContentType xContentType = XContentType.JSON;
        if (content.length() > 0) {
            try (XContentParser parser = xContentType.xContent().createParser(restRequest.getXContentRegistry(), content)) {
                withParser.accept(parser);
            }
        } else {
            withParser.accept(null);
        }
    }
}