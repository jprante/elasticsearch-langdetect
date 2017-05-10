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
public class LangDetectChineseTest extends NodeTestUtils {

    @Test
    public void testChineseLanguageCode() throws Exception {
        startCluster();
        try {
            CreateIndexRequestBuilder createIndexRequestBuilder =
                    new CreateIndexRequestBuilder(client(), CreateIndexAction.INSTANCE)
                            .setIndex("test");
            createIndexRequestBuilder.addMapping("someType", jsonBuilder()
                            .startObject()
                              .startObject("properties")
                                .startObject("content")
                                  .field("type", "text")
                                  .startObject("fields")
                                    .startObject("language")
                                       .field("type", "langdetect")
                                       .array("languages", "zh-cn")
                                    .endObject()
                                  .endObject()
                                .endObject()
                              .endObject()
                            .endObject());
            createIndexRequestBuilder.execute().actionGet();
            IndexRequestBuilder indexRequestBuilder = new IndexRequestBuilder(client(), IndexAction.INSTANCE)
                    .setIndex("test").setType("someType").setId("1")
                    .setSource("content", "位于美国首都华盛顿都会圈的希望中文学校５日晚举办活动庆祝建立２０周年。从中国大陆留学生为子女学中文而自发建立的学习班，到学生规模在全美名列前茅的中文学校，这个平台的发展也折射出美国的中文教育热度逐步提升。\n" +
                            "希望中文学校是大华盛顿地区最大中文学校，现有７个校区逾４０００名学生，规模在美国东部数一数二。不过，见证了希望中文学校２０年发展的人们起初根本无法想象这个小小的中文教育平台能发展到今日之规模。");
            indexRequestBuilder.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE).execute().actionGet();
            SearchRequestBuilder searchRequestBuilder =
                    new SearchRequestBuilder(client(), SearchAction.INSTANCE)
                            .setIndices("test")
                            .setTypes("someType")
                            .setQuery(QueryBuilders.termQuery("content.language", "zh-cn"))
                            .addStoredField("content.language");
            SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
            assertEquals(1L, searchResponse.getHits().getTotalHits());
            assertEquals("zh-cn", searchResponse.getHits().getAt(0).getField("content.language").getValue());
        } finally {
            stopCluster();
        }
    }
}
