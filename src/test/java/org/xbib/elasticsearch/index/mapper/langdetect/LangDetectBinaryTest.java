package org.xbib.elasticsearch.index.mapper.langdetect;

import org.elasticsearch.action.admin.indices.create.CreateIndexAction;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.index.IndexAction;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchAction;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;
import org.xbib.elasticsearch.NodeTestUtils;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.junit.Assert.assertEquals;

/**
 *
 */
public class LangDetectBinaryTest extends NodeTestUtils {

    @Test
    public void testLangDetectBinary() throws Exception {
        startCluster();
        try {
            CreateIndexRequestBuilder createIndexRequestBuilder =
                    new CreateIndexRequestBuilder(client(), CreateIndexAction.INSTANCE).setIndex("test");
            createIndexRequestBuilder.addMapping("someType", jsonBuilder()
                     .startObject()
                        .startObject("properties")
                           .startObject("content")
                              .field("type", "text")
                              .startObject("fields")
                                 .startObject("language")
                                    .field("type", "langdetect")
                                    .field("binary", true)
                                 .endObject()
                              .endObject()
                           .endObject()
                        .endObject()
                    .endObject());
            createIndexRequestBuilder.execute().actionGet();
            IndexRequestBuilder indexRequestBuilder =
                    new IndexRequestBuilder(client(), IndexAction.INSTANCE)
                            .setIndex("test").setType("someType").setId("1")
                            //\"God Save the Queen\" (alternatively \"God Save the King\"
                            .setSource("content", "IkdvZCBTYXZlIHRoZSBRdWVlbiIgKGFsdGVybmF0aXZlbHkgIkdvZCBTYXZlIHRoZSBLaW5nIg==");
            indexRequestBuilder.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE).execute().actionGet();
            SearchRequestBuilder searchRequestBuilder =
                    new SearchRequestBuilder(client(), SearchAction.INSTANCE)
                            .setIndices("test")
                            .setQuery(QueryBuilders.matchAllQuery())
                            .addStoredField("content.language");
            SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
            assertEquals(1L, searchResponse.getHits().getTotalHits());
            assertEquals("en", searchResponse.getHits().getAt(0).field("content.language").getValue());
        } finally {
            stopCluster();
        }
    }
}
