package org.xbib.elasticsearch.action.langdetect.profile;

import org.elasticsearch.action.support.single.custom.SingleCustomOperationRequest;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import java.io.IOException;

public class LangdetectProfileRequest extends SingleCustomOperationRequest<LangdetectProfileRequest> {

    private String profile;

    public LangdetectProfileRequest() {
    }

    public LangdetectProfileRequest setProfile(String profile) {
        this.profile = profile;
        return this;
    }

    public String getProfile() {
        return profile;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        profile = in.readString();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeString(profile);
    }
}
