package org.xbib.elasticsearch.action.langdetect;

import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import java.io.IOException;

import static org.elasticsearch.action.ValidateActions.addValidationError;

/**
 *
 */
public class LangdetectRequest extends ActionRequest {

    private String profile;

    private String text;

    @Override
    public ActionRequestValidationException validate() {
        ActionRequestValidationException validationException = null;
        if (text == null) {
            validationException = addValidationError("text is missing", null);
        }
        return validationException;
    }

    public String getProfile() {
        return profile;
    }

    public LangdetectRequest setProfile(String profile) {
        this.profile = profile;
        return this;
    }

    public String getText() {
        return text;
    }

    public LangdetectRequest setText(String text) {
        this.text = text;
        return this;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        text = in.readString();
        profile = in.readOptionalString();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeString(text);
        out.writeOptionalString(profile);
    }
}
