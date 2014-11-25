package org.oxbib.elasticsearch.plugin;

import org.junit.Test;
import org.oxbib.elasticsearch.plugin.helper.AbstractNodeTestHelper;
import org.xbib.elasticsearch.action.langdetect.LangdetectRequestBuilder;
import org.xbib.elasticsearch.action.langdetect.LangdetectResponse;
import org.xbib.elasticsearch.action.langdetect.profile.LangdetectProfileRequestBuilder;
import org.xbib.elasticsearch.action.langdetect.profile.LangdetectProfileResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class LangDetectActionTest extends AbstractNodeTestHelper {

    @Test
    public void testLangDetectProfile() throws Exception {
        LangdetectRequestBuilder langdetectRequestBuilder =
                new LangdetectRequestBuilder(client("1").admin().indices()).setText("hello this is a test");
        LangdetectResponse response = langdetectRequestBuilder.execute().actionGet();
        assertEquals("en", response.getLanguages().get(0).getLanguage());
        assertEquals("/langdetect/", response.getProfile());

        LangdetectProfileRequestBuilder langdetectProfileRequestBuilder =
                new LangdetectProfileRequestBuilder(client("1").admin().indices())
                        .setProfile("/langdetect/short-text/");
        LangdetectProfileResponse langdetectProfileResponse = langdetectProfileRequestBuilder.execute().actionGet();
        assertNotNull(langdetectProfileResponse);

        langdetectRequestBuilder =
                new LangdetectRequestBuilder(client("1").admin().indices()).setText("hello this is a test");
        response = langdetectRequestBuilder.execute().actionGet();
        assertEquals("en", response.getLanguages().get(0).getLanguage());
        assertEquals("/langdetect/short-text/", response.getProfile());
    }
}
