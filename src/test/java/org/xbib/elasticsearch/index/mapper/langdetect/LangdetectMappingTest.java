package org.xbib.elasticsearch.index.mapper.langdetect;

import org.elasticsearch.Version;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.base.Charsets;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.inject.Injector;
import org.elasticsearch.common.inject.ModulesBuilder;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsModule;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.EnvironmentModule;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.IndexNameModule;
import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.index.analysis.AnalysisService;
import org.elasticsearch.index.codec.docvaluesformat.DocValuesFormatService;
import org.elasticsearch.index.codec.postingsformat.PostingsFormatService;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.DocumentMapperParser;
import org.elasticsearch.index.mapper.ParseContext;
import org.elasticsearch.index.settings.IndexSettingsModule;
import org.elasticsearch.index.similarity.SimilarityLookupService;

import org.elasticsearch.indices.analysis.IndicesAnalysisModule;
import org.elasticsearch.indices.analysis.IndicesAnalysisService;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;

import static org.elasticsearch.common.io.Streams.copyToString;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class LangdetectMappingTest extends Assert {

    @Test
    public void testSimpleMappings() throws Exception {
        String mapping = copyToStringFromClasspath("simple-mapping.json");
        DocumentMapper docMapper = createMapperParser().parse(mapping);
        String sampleText = copyToStringFromClasspath("english.txt");
        BytesReference json = jsonBuilder().startObject().field("_id", 1).field("someField", sampleText).endObject().bytes();
        ParseContext.Document doc = docMapper.parse(json).rootDoc();
        assertEquals(doc.get(docMapper.mappers().smartName("someField").mapper().names().indexName()), sampleText);
        assertEquals(doc.getFields("someField.lang").length, 1);
        assertEquals(doc.getFields("someField.lang")[0].stringValue(), "en");

        // re-parse it
        String builtMapping = docMapper.mappingSource().string();
        docMapper = createMapperParser().parse(builtMapping);
        json = jsonBuilder().startObject().field("_id", 1).field("someField", sampleText).endObject().bytes();
        doc = docMapper.parse(json).rootDoc();
        assertEquals(doc.get(docMapper.mappers().smartName("someField").mapper().names().indexName()), sampleText);
        assertEquals(doc.getFields("someField.lang").length, 1);
        assertEquals(doc.getFields("someField.lang")[0].stringValue(), "en");
    }

    @Test
    public void testBinary() throws Exception {
        Settings settings = ImmutableSettings.EMPTY;
        String mapping = copyToStringFromClasspath("base64-mapping.json");
        DocumentMapper docMapper = createMapperParser(settings).parse(mapping);
        String sampleBinary = copyToStringFromClasspath("base64.txt");
        String sampleText = copyToStringFromClasspath("base64-decoded.txt");
        BytesReference json = jsonBuilder().startObject().field("_id", 1).field("someField", sampleBinary).endObject().bytes();
        ParseContext.Document doc = docMapper.parse(json).rootDoc();
        assertEquals(doc.get(docMapper.mappers().smartName("someField").mapper().names().indexName()), sampleText);
        assertEquals(doc.getFields("someField.lang").length, 1);
        assertEquals(doc.getFields("someField.lang")[0].stringValue(), "en");

        // re-parse it
        String builtMapping = docMapper.mappingSource().string();
        docMapper = createMapperParser(settings).parse(builtMapping);
        json = jsonBuilder().startObject().field("_id", 1).field("someField", sampleText).endObject().bytes();
        doc = docMapper.parse(json).rootDoc();
        assertEquals(doc.get(docMapper.mappers().smartName("someField").mapper().names().indexName()), sampleText);
        assertEquals(doc.getFields("someField.lang").length, 1);
        assertEquals(doc.getFields("someField.lang")[0].stringValue(), "en");
    }

    @Test
    public void testBinary2() throws Exception {
        Settings settings = ImmutableSettings.EMPTY;
        String mapping = copyToStringFromClasspath("base64-2-mapping.json");
        DocumentMapper docMapper = createMapperParser(settings).parse(mapping);
        String sampleBinary = copyToStringFromClasspath("base64-2.txt");
        String sampleText = copyToStringFromClasspath("base64-2-decoded.txt");
        BytesReference json = jsonBuilder().startObject().field("_id", 1).field("content", sampleBinary).endObject().bytes();
        ParseContext.Document doc = docMapper.parse(json).rootDoc();
        assertEquals(1, doc.getFields("content.language.lang").length);
        assertEquals("en", doc.getFields("content.language.lang")[0].stringValue());

        // re-parse it
        String builtMapping = docMapper.mappingSource().string();
        docMapper = createMapperParser(settings).parse(builtMapping);
        json = jsonBuilder().startObject().field("_id", 1).field("content", sampleText).endObject().bytes();
        doc = docMapper.parse(json).rootDoc();
        assertEquals(doc.get(docMapper.mappers().smartName("content").mapper().names().indexName()), sampleText);
        assertEquals(doc.getFields("content.language.lang").length, 1);
        assertEquals(doc.getFields("content.language.lang")[0].stringValue(), "en");
    }

    @Test
    public void testShortTextProfile() throws Exception {
        String mapping = copyToStringFromClasspath("short-text-mapping.json");
        DocumentMapper docMapper = createMapperParser().parse(mapping);
        String sampleText = copyToStringFromClasspath("english.txt");
        BytesReference json = jsonBuilder().startObject().field("_id", 1).field("someField", sampleText).endObject().bytes();
        ParseContext.Document doc = docMapper.parse(json).rootDoc();
        assertEquals(doc.get(docMapper.mappers().smartName("someField").mapper().names().indexName()), sampleText);
        assertEquals(doc.getFields("someField.lang").length, 1);
        assertEquals(doc.getFields("someField.lang")[0].stringValue(), "en");

        // re-parse it
        String builtMapping = docMapper.mappingSource().string();
        docMapper = createMapperParser().parse(builtMapping);
        json = jsonBuilder().startObject().field("_id", 1).field("someField", sampleText).endObject().bytes();
        doc = docMapper.parse(json).rootDoc();
        assertEquals(doc.get(docMapper.mappers().smartName("someField").mapper().names().indexName()), sampleText);
        assertEquals(doc.getFields("someField.lang").length, 1);
        assertEquals(doc.getFields("someField.lang")[0].stringValue(), "en");
    }

    private DocumentMapperParser createMapperParser() throws IOException {
        return createMapperParser(ImmutableSettings.EMPTY);
    }

    private DocumentMapperParser createMapperParser(Settings fromSettings) throws IOException {
        Index index = new Index("test");
        Settings settings = ImmutableSettings.settingsBuilder()
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                .put(fromSettings)
                .build();
        Injector parentInjector = new ModulesBuilder().add(new SettingsModule(settings),
                new EnvironmentModule(new Environment(settings)),
                new IndicesAnalysisModule())
                .createInjector();
        Injector injector = new ModulesBuilder().add(
                new IndexSettingsModule(index, settings),
                new IndexNameModule(index),
                new AnalysisModule(settings, parentInjector.getInstance(IndicesAnalysisService.class)))
                .createChildInjector(parentInjector);
        AnalysisService service = injector.getInstance(AnalysisService.class);
        DocumentMapperParser mapperParser = new DocumentMapperParser(index,
                settings,
                service,
                new PostingsFormatService(index),
                new DocValuesFormatService(index),
                new SimilarityLookupService(index, settings),
                null
        );
        mapperParser.putTypeParser(LangdetectMapper.CONTENT_TYPE, new LangdetectMapper.TypeParser());
        return mapperParser;
    }

    public String copyToStringFromClasspath(String path) throws IOException {
        return copyToString(new InputStreamReader(getClass().getResource(path).openStream(), Charsets.UTF_8));
    }
}
