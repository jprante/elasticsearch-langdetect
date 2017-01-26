package org.xbib.elasticsearch.index.mapper.langdetect;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;
import org.xbib.elasticsearch.NodeTestUtils;
import org.xbib.elasticsearch.action.langdetect.LangdetectRequestBuilder;
import org.xbib.elasticsearch.action.langdetect.LangdetectResponse;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 *
 */
public class LangDetectActionTest extends NodeTestUtils {

    @Test
    public void testLangDetectProfile() throws Exception {
        startCluster();
        try {
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
        } finally {
            stopCluster();
        }
    }

    @Test
    public void testSort() throws Exception {
        startCluster();
        try {

            Settings settings = Settings.builder()
                    .build();

            client().admin().indices().prepareCreate("test")
                    .setSettings(settings)
                    .addMapping("article",
                            jsonBuilder().startObject()
                                    .startObject("article")
                                       .startObject("properties")
                                           .startObject("content")
                                             .field("type", "langdetect")
                                             .array("languages", "de", "en", "fr")
                                           .endObject()
                                       .endObject()
                                    .endObject()
                            .endObject())
                    .execute().actionGet();

            client().admin().cluster().prepareHealth().setWaitForGreenStatus().execute().actionGet();

            client().prepareIndex("test", "article", "1")
                    .setSource(jsonBuilder().startObject()
                            .field("title", "Some title")
                            .field("content", "Oh, say can you see by the dawn`s early light, What so proudly we hailed at the twilight`s last gleaming?")
                            .endObject()).execute().actionGet();
            client().prepareIndex("test", "article", "2")
                    .setSource(jsonBuilder().startObject()
                            .field("title", "Ein Titel")
                            .field("content", "Einigkeit und Recht und Freiheit für das deutsche Vaterland!")
                            .endObject()).execute().actionGet();
            client().prepareIndex("test", "article", "3")
                    .setSource(jsonBuilder().startObject()
                            .field("title", "Un titre")
                            .field("content", "Allons enfants de la Patrie, Le jour de gloire est arrivé!")
                            .endObject()).execute().actionGet();

            client().admin().indices().prepareRefresh().execute().actionGet();

            SearchResponse searchResponse = client().prepareSearch()
                    .setQuery(QueryBuilders.termQuery("content", "en"))
                    .execute().actionGet();
            assertEquals(1L, searchResponse.getHits().totalHits());
            assertEquals("Oh, say can you see by the dawn`s early light, What so proudly we hailed at the twilight`s last gleaming?",
                    searchResponse.getHits().getAt(0).getSource().get("content").toString());

            searchResponse = client().prepareSearch()
                    .setQuery(QueryBuilders.termQuery("content", "de"))
                    .execute().actionGet();
            assertEquals(1L, searchResponse.getHits().totalHits());
            assertEquals("Einigkeit und Recht und Freiheit für das deutsche Vaterland!",
                    searchResponse.getHits().getAt(0).getSource().get("content").toString());

            searchResponse = client().prepareSearch()
                    .setQuery(QueryBuilders.termQuery("content", "fr"))
                    .execute().actionGet();
            assertEquals(1L, searchResponse.getHits().totalHits());
            assertEquals("Allons enfants de la Patrie, Le jour de gloire est arrivé!",
                    searchResponse.getHits().getAt(0).getSource().get("content").toString());
        } finally {
            stopCluster();
        }
    }
}
