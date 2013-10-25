package org.xbib.elasticsearch.module.langdetect;

import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.collect.Maps;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AnalysisService;
import org.elasticsearch.index.analysis.AnalyzerProviderFactory;
import org.elasticsearch.index.analysis.AnalyzerScope;
import org.elasticsearch.index.analysis.PreBuiltAnalyzerProviderFactory;
import org.elasticsearch.index.codec.postingsformat.PostingsFormatService;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.DocumentMapperParser;
import org.elasticsearch.index.similarity.SimilarityLookupService;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.xbib.elasticsearch.common.langdetect.Detector;
import org.xbib.elasticsearch.index.mapper.langdetect.LangdetectMapper;

import java.io.IOException;
import java.util.Map;

import static org.elasticsearch.common.io.Streams.copyToStringFromClasspath;
import static org.elasticsearch.common.settings.ImmutableSettings.settingsBuilder;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class LangdetectMappingTest extends Assert {

    private DocumentMapperParser mapperParser;

    @BeforeClass
    public void setupMapperParser() throws IOException {
        Index index = new Index("test");
        Map<String, AnalyzerProviderFactory> analyzerFactoryFactories = Maps.newHashMap();
        analyzerFactoryFactories.put("keyword",
                new PreBuiltAnalyzerProviderFactory("keyword", AnalyzerScope.INDEX, new KeywordAnalyzer()));
        AnalysisService analysisService = new AnalysisService(index,
                ImmutableSettings.Builder.EMPTY_SETTINGS, null, analyzerFactoryFactories, null, null, null);
        mapperParser = new DocumentMapperParser(index, analysisService, new PostingsFormatService(index),
                new SimilarityLookupService(index, ImmutableSettings.Builder.EMPTY_SETTINGS));
        Settings settings = settingsBuilder()
                .build();
        Detector detector = new Detector(settings);
        detector.start();
        mapperParser.putTypeParser(LangdetectMapper.CONTENT_TYPE,
                new LangdetectMapper.TypeParser(detector));
    }

    @Test
    public void testSimpleMappings() throws Exception {
        String mapping = copyToStringFromClasspath("/simple-mapping.json");
        DocumentMapper docMapper = mapperParser.parse(mapping);

        String sampleText = copyToStringFromClasspath("/english.txt");
        BytesReference json = jsonBuilder().startObject().field("_id", 1).field("someField", sampleText).endObject().bytes();
        Document doc = docMapper.parse(json).rootDoc();

        assertEquals(doc.get(docMapper.mappers().smartName("someField").mapper().names().indexName()), sampleText);
        assertEquals(doc.getFields("someField.lang").length, 1);
        assertEquals(doc.getFields("someField.lang")[0].stringValue(), "en");

        // re-parse it
        String builtMapping = docMapper.mappingSource().string();
        docMapper = mapperParser.parse(builtMapping);

        json = jsonBuilder().startObject().field("_id", 1).field("someField", sampleText).endObject().bytes();
        doc = docMapper.parse(json).rootDoc();

        assertEquals(doc.get(docMapper.mappers().smartName("someField").mapper().names().indexName()), sampleText);
        assertEquals(doc.getFields("someField.lang").length, 1);
        assertEquals(doc.getFields("someField.lang")[0].stringValue(), "en");
    }

    @Test
    public void testBase64() throws Exception {
        String mapping = copyToStringFromClasspath("/base64-mapping.json");
        DocumentMapper docMapper = mapperParser.parse(mapping);

        String sampleBinary = copyToStringFromClasspath("/base64.txt");
        String sampleText = copyToStringFromClasspath("/base64-decoded.txt");

        BytesReference json = jsonBuilder().startObject().field("_id", 1).field("someField", sampleBinary).endObject().bytes();
        Document doc = docMapper.parse(json).rootDoc();

        assertEquals(doc.get(docMapper.mappers().smartName("someField").mapper().names().indexName()), sampleText);
        assertEquals(doc.getFields("someField.lang").length, 1);
        assertEquals(doc.getFields("someField.lang")[0].stringValue(), "en");

        // re-parse it
        String builtMapping = docMapper.mappingSource().string();
        docMapper = mapperParser.parse(builtMapping);

        json = jsonBuilder().startObject().field("_id", 1).field("someField", sampleText).endObject().bytes();
        doc = docMapper.parse(json).rootDoc();

        assertEquals(doc.get(docMapper.mappers().smartName("someField").mapper().names().indexName()), sampleText);
        assertEquals(doc.getFields("someField.lang").length, 1);
        assertEquals(doc.getFields("someField.lang")[0].stringValue(), "en");
    }

}
