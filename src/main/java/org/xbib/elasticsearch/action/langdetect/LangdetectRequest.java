
package org.xbib.elasticsearch.action.langdetect;

import java.io.IOException;
import org.elasticsearch.action.support.single.custom.SingleCustomOperationRequest;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

public class LangdetectRequest extends SingleCustomOperationRequest<LangdetectRequest> {

    BytesReference text;

    public LangdetectRequest() {
    }

    public LangdetectRequest setText(BytesReference text) {
        this.text = text;
        return this;
    }
    
    public BytesReference getText() {
        return text;
    }
    
    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        text = in.readBytesReference();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeBytesReference(text);
    }
}
