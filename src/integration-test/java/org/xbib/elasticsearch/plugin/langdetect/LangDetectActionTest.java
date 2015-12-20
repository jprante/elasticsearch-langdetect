package org.xbib.elasticsearch.plugin.langdetect;

import org.junit.Test;
import org.xbib.elasticsearch.NodeTestUtils;
import org.xbib.elasticsearch.action.langdetect.LangdetectRequestBuilder;
import org.xbib.elasticsearch.action.langdetect.LangdetectResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class LangDetectActionTest extends NodeTestUtils {

    @Test
    public void testLangDetectProfile() throws Exception {
        LangdetectRequestBuilder langdetectRequestBuilder =
                new LangdetectRequestBuilder(client("1"))
                        .setText("hello this is a test");
        LangdetectResponse response = langdetectRequestBuilder.execute().actionGet();
        assertFalse(response.getLanguages().isEmpty());
        assertEquals("en", response.getLanguages().get(0).getLanguage());
        assertEquals("/langdetect/", response.getProfile());

        LangdetectRequestBuilder langdetectProfileRequestBuilder =
                new LangdetectRequestBuilder(client("1"))
                        .setText("hello this is a test")
                        .setProfile("/langdetect/short-text/");
        LangdetectResponse langdetectProfileResponse = langdetectProfileRequestBuilder.execute().actionGet();
        assertNotNull(langdetectProfileResponse);
        langdetectRequestBuilder =
                new LangdetectRequestBuilder(client("1")).setText("hello this is a test");
        response = langdetectRequestBuilder.execute().actionGet();
        assertFalse(response.getLanguages().isEmpty());
        assertEquals("en", response.getLanguages().get(0).getLanguage());
        assertEquals("/langdetect/short-text/", response.getProfile());
    }
}
