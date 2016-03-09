package org.xbib.elasticsearch.plugin.langdetect;

import org.junit.Test;
import org.xbib.elasticsearch.NodeTestUtils;
import org.xbib.elasticsearch.action.langdetect.LangdetectRequestBuilder;
import org.xbib.elasticsearch.action.langdetect.LangdetectResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class LangDetectActionTest extends NodeTestUtils {

    @Test
    public void testLangDetectProfile() throws Exception {

        // normal profile
        LangdetectRequestBuilder langdetectRequestBuilder =
                new LangdetectRequestBuilder(client())
                        .setText("hello this is a test");
        LangdetectResponse response = langdetectRequestBuilder.execute().actionGet();
        assertFalse(response.getLanguages().isEmpty());
        assertEquals("en", response.getLanguages().get(0).getLanguage());
        assertNull(response.getProfile());

        // short-text profile
        LangdetectRequestBuilder langdetectProfileRequestBuilder =
                new LangdetectRequestBuilder(client())
                        .setText("hello this is a test")
                        .setProfile("short-text");
        response = langdetectProfileRequestBuilder.execute().actionGet();
        assertNotNull(response);
        assertFalse(response.getLanguages().isEmpty());
        assertEquals("en", response.getLanguages().get(0).getLanguage());
        assertEquals("short-text", response.getProfile());

        // again normal profile
        langdetectRequestBuilder = new LangdetectRequestBuilder(client())
                        .setText("hello this is a test");
        response = langdetectRequestBuilder.execute().actionGet();
        assertNotNull(response);
        assertFalse(response.getLanguages().isEmpty());
        assertEquals("en", response.getLanguages().get(0).getLanguage());
        assertNull(response.getProfile());
    }
}
