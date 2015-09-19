package org.xbib.elasticsearch.index.mapper.langdetect;

import org.apache.lucene.index.IndexableField;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.DocumentMapperParser;
import org.elasticsearch.index.mapper.ParseContext;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;

import static org.elasticsearch.common.io.Streams.copyToString;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class LangdetectMappingTests extends Assert {

    private final static ESLogger logger = ESLoggerFactory.getLogger("test");

    @Test
    public void testSimpleMappings() throws Exception {
        String mapping = copyToStringFromClasspath("simple-mapping.json");
        DocumentMapper docMapper = newMapperParser().parse(mapping);
        String sampleText = copyToStringFromClasspath("english.txt");
        BytesReference json = jsonBuilder().startObject().field("_id", 1).field("someField", sampleText).endObject().bytes();
        ParseContext.Document doc = docMapper.parse("someIndex", "someType", "1", json).rootDoc();
        for (IndexableField field : doc.getFields()) {
            logger.info("{} = {}", field.name(), field.stringValue());
        }
        assertEquals(1, doc.getFields("someField").length);
        assertEquals("en", doc.getFields("someField")[0].stringValue());
        // re-parse it
        String builtMapping = docMapper.mappingSource().string();
        docMapper = newMapperParser().parse(builtMapping);
        json = jsonBuilder().startObject().field("_id", 1).field("someField", sampleText).endObject().bytes();
        doc = docMapper.parse("someIndex", "someType", "1", json).rootDoc();
        assertEquals(1, doc.getFields("someField").length);
        assertEquals("en", doc.getFields("someField")[0].stringValue());
    }

    @Test
    public void testBinary() throws Exception {
        String mapping = copyToStringFromClasspath("base64-mapping.json");
        DocumentMapper docMapper = newMapperParser().parse(mapping);
        String sampleBinary = copyToStringFromClasspath("base64.txt");
        String sampleText = copyToStringFromClasspath("base64-decoded.txt");
        BytesReference json = jsonBuilder().startObject().field("_id", 1)
                .field("someField", sampleBinary)
                .endObject().bytes();
        ParseContext.Document doc = docMapper.parse("someIndex", "someType", "1", json).rootDoc();
        for (IndexableField field : doc.getFields()) {
            logger.info("binary {} = {}", field.name(), field.stringValue());
        }
        assertEquals(1, doc.getFields("someField").length);
        assertEquals("en", doc.getFields("someField")[0].stringValue());
        // re-parse it
        String builtMapping = docMapper.mappingSource().string();
        docMapper = newMapperParser().parse(builtMapping);
        json = jsonBuilder().startObject().field("_id", 1).field("someField", sampleText).endObject().bytes();
        doc = docMapper.parse("someIndex", "someType", "1", json).rootDoc();
        assertEquals(1, doc.getFields("someField").length);
        assertEquals("en", doc.getFields("someField")[0].stringValue());
    }

    @Test
    public void testBinary2() throws Exception {
        String mapping = copyToStringFromClasspath("base64-2-mapping.json");
        DocumentMapper docMapper = newMapperParser().parse(mapping);
        //String sampleBinary = copyToStringFromClasspath("base64-2.txt");
        String sampleText = copyToStringFromClasspath("base64-2-decoded.txt");
        BytesReference json = jsonBuilder().startObject().field("_id", 1)
                .field("content", sampleText)
                .endObject().bytes();
        ParseContext.Document doc = docMapper.parse("someIndex", "someType", "1", json).rootDoc();
        for (IndexableField field : doc.getFields()) {
            logger.info("binary2 {} = {} stored={}", field.name(), field.stringValue(), field.fieldType().stored());
        }
        assertEquals(1, doc.getFields("content.language").length);
        assertEquals("en", doc.getFields("content.language")[0].stringValue());
        // re-parse it
        String builtMapping = docMapper.mappingSource().string();
        docMapper = newMapperParser().parse(builtMapping);
        json = jsonBuilder().startObject().field("_id", 1).field("content", sampleText).endObject().bytes();
        doc = docMapper.parse("someIndex", "someType", "1", json).rootDoc();
        assertEquals(1, doc.getFields("content.language").length);
        assertEquals("en", doc.getFields("content.language")[0].stringValue());
    }

    @Test
    public void testShortTextProfile() throws Exception {
        String mapping = copyToStringFromClasspath("short-text-mapping.json");
        DocumentMapper docMapper = newMapperParser().parse(mapping);
        String sampleText = copyToStringFromClasspath("english.txt");
        BytesReference json = jsonBuilder().startObject().field("_id", 1).field("someField", sampleText).endObject().bytes();
        ParseContext.Document doc = docMapper.parse("someIndex", "someType", "1", json).rootDoc();
        assertEquals(1, doc.getFields("someField").length);
        assertEquals("en", doc.getFields("someField")[0].stringValue());
        // re-parse it
        String builtMapping = docMapper.mappingSource().string();
        docMapper = newMapperParser().parse(builtMapping);
        json = jsonBuilder().startObject().field("_id", 1).field("someField", sampleText).endObject().bytes();
        doc = docMapper.parse("someIndex", "someType", "1", json).rootDoc();
        assertEquals(1, doc.getFields("someField").length);
        assertEquals("en", doc.getFields("someField")[0].stringValue());
    }

    private DocumentMapperParser newMapperParser() {
        DocumentMapperParser mapperParser = MapperTestUtils.newMapperParser();
        mapperParser.putTypeParser(LangdetectMapper.CONTENT_TYPE, new LangdetectMapper.TypeParser());
        return mapperParser;
    }

    public String copyToStringFromClasspath(String path) throws IOException {
        return copyToString(new InputStreamReader(getClass().getResource(path).openStream(), "UTF-8"));
    }
}
