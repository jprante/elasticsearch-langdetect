package org.xbib.elasticsearch.action.langdetect.profile;

import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.common.xcontent.StatusToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.RestStatus;

import java.io.IOException;

import static org.elasticsearch.rest.RestStatus.OK;

public class LangdetectProfileResponse extends ActionResponse implements StatusToXContent {

    public LangdetectProfileResponse() {
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.field("ok", true);
        return builder;
    }

    @Override
    public RestStatus status() {
        return OK;
    }
}
