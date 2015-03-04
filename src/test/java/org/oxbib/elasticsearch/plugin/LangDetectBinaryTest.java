package org.oxbib.elasticsearch.plugin;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.indices.IndexMissingException;
import org.junit.Test;
import org.oxbib.elasticsearch.plugin.helper.AbstractNodeTestHelper;

import static org.junit.Assert.assertEquals;

public class LangDetectBinaryTest extends AbstractNodeTestHelper {

    @Test
    public void testLangDetectBinary() throws Exception {
        CreateIndexRequestBuilder createIndexRequestBuilder =
                new CreateIndexRequestBuilder(client("1").admin().indices()).setIndex("test");
        createIndexRequestBuilder.addMapping("someType", "{\n" +
                "  \"someType\" : {\n" +
                "    \"properties\": {\n" +
                "      \"content\": {\n" +
                "        \"type\": \"multi_field\",\n" +
                "        \"fields\": {\n" +
                "          \"content\": {\n" +
                "            \"type\": \"string\"\n" +
                "          },\n" +
                "          \"language\": {\n" +
                "            \"type\": \"langdetect\"\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}");
        createIndexRequestBuilder.execute().actionGet();
        IndexRequestBuilder indexRequestBuilder =
                new IndexRequestBuilder(client("1")).setIndex("test").setType("someType").setId("1")
                .setSource("content", "IkdvZCBTYXZlIHRoZSBRdWVlbiIgKGFsdGVybmF0aXZlbHkgIkdvZCBTYXZlIHRoZSBLaW5nIg==");
        indexRequestBuilder.setRefresh(true).execute().actionGet();
        SearchRequestBuilder searchRequestBuilder =
                new SearchRequestBuilder(client("1"))
                        .setIndices("test")
                        .setQuery(QueryBuilders.matchAllQuery())
                        .addField("content.language.lang");
        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
        assertEquals("en", searchResponse.getHits().getAt(0).field("content.language.lang").getValue());
    }
}
