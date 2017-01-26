package org.xbib.elasticsearch.index.mapper.langdetect;

import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.compress.CompressedXContent;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.ParseContext;
import org.junit.Assert;
import org.junit.Test;
import org.xbib.elasticsearch.MapperTestUtils;

import java.io.IOException;
import java.io.InputStreamReader;

import static org.elasticsearch.common.io.Streams.copyToString;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class LangdetectMappingTest extends Assert {

    @Test
    public void testSimpleMappings() throws Exception {
        String mapping = copyToStringFromClasspath("simple-mapping.json");
        DocumentMapper docMapper = MapperTestUtils.newDocumentMapperParser("someIndex").parse("someType", new CompressedXContent(mapping));
        String sampleText = copyToStringFromClasspath("english.txt");
        BytesReference json = jsonBuilder().startObject().field("someField", sampleText).endObject().bytes();
        ParseContext.Document doc = docMapper.parse("someIndex", "someType", "1", json).rootDoc();
        assertEquals(1, doc.getFields("someField").length);
        assertEquals("en", doc.getFields("someField")[0].stringValue());
        // re-parse it
        String builtMapping = docMapper.mappingSource().string();
        docMapper = MapperTestUtils.newDocumentMapperParser("someIndex").parse("someType", new CompressedXContent(builtMapping));
        json = jsonBuilder().startObject().field("someField", sampleText).endObject().bytes();
        doc = docMapper.parse("someIndex", "someType", "1", json).rootDoc();
        assertEquals(1, doc.getFields("someField").length);
        assertEquals("en", doc.getFields("someField")[0].stringValue());
    }

    @Test
    public void testBinary() throws Exception {
        String mapping = copyToStringFromClasspath("base64-mapping.json");
        DocumentMapper docMapper = MapperTestUtils.newDocumentMapperParser("someIndex").parse("someType", new CompressedXContent(mapping));
        String sampleBinary = copyToStringFromClasspath("base64.txt");
        String sampleText = copyToStringFromClasspath("base64-decoded.txt");
        BytesReference json = jsonBuilder().startObject().field("someField", sampleBinary).endObject().bytes();
        ParseContext.Document doc = docMapper.parse("someIndex", "someType", "1", json).rootDoc();
        assertEquals(1, doc.getFields("someField").length);
        assertEquals("en", doc.getFields("someField")[0].stringValue());
        // re-parse it
        String builtMapping = docMapper.mappingSource().string();
        docMapper = MapperTestUtils.newDocumentMapperParser("someIndex").parse("someType", new CompressedXContent(builtMapping));
        json = jsonBuilder().startObject().field("someField", sampleText).endObject().bytes();
        doc = docMapper.parse("someIndex", "someType", "1", json).rootDoc();
        assertEquals(1, doc.getFields("someField").length, 1);
        assertEquals("en", doc.getFields("someField")[0].stringValue(), "en");
    }

    @Test
    public void testCustomMappings() throws Exception {
        Settings settings = Settings.builder()
                .put("path.home", System.getProperty("path.home"))
                .loadFromStream("settings.json", getClass().getResourceAsStream("settings.json")).build();
        String mapping = copyToStringFromClasspath("mapping.json");
        DocumentMapper docMapper = MapperTestUtils.newDocumentMapperParser(settings, "someIndex").parse("someType", new CompressedXContent(mapping));
        String sampleText = copyToStringFromClasspath("german.txt");
        BytesReference json = jsonBuilder().startObject().field("someField", sampleText).endObject().bytes();
        ParseContext.Document doc = docMapper.parse("someIndex", "someType", "1", json).rootDoc();
        assertEquals(1, doc.getFields("someField").length);
        assertEquals("Deutsch", doc.getFields("someField")[0].stringValue());
    }

    @Test
    public void testBinary2() throws Exception {
        String mapping = copyToStringFromClasspath("base64-2-mapping.json");
        DocumentMapper docMapper = MapperTestUtils.newDocumentMapperParser("someIndex").parse("someType", new CompressedXContent(mapping));
        //String sampleBinary = copyToStringFromClasspath("base64-2.txt");
        String sampleText = copyToStringFromClasspath("base64-2-decoded.txt");
        BytesReference json = jsonBuilder().startObject().field("content", sampleText).endObject().bytes();
        ParseContext.Document doc = docMapper.parse("someIndex", "someType", "1", json).rootDoc();
        //for (IndexableField field : doc.getFields()) {
        //    logger.info("binary2 {} = {} stored={}", field.name(), field.stringValue(), field.fieldType().stored());
        //}
        assertEquals(1, doc.getFields("content.language").length);
        assertEquals("en", doc.getFields("content.language")[0].stringValue());
        // re-parse it
        String builtMapping = docMapper.mappingSource().string();
        docMapper = MapperTestUtils.newDocumentMapperParser("someIndex").parse("someType", new CompressedXContent(builtMapping));
        json = jsonBuilder().startObject().field("content", sampleText).endObject().bytes();
        doc = docMapper.parse("someIndex", "someType", "1", json).rootDoc();
        assertEquals(1, doc.getFields("content.language").length);
        assertEquals("en", doc.getFields("content.language")[0].stringValue());
    }

    @Test
    public void testShortTextProfile() throws Exception {
        String mapping = copyToStringFromClasspath("short-text-mapping.json");
        DocumentMapper docMapper = MapperTestUtils.newDocumentMapperParser("someIndex").parse("someType", new CompressedXContent(mapping));
        String sampleText = copyToStringFromClasspath("english.txt");
        BytesReference json = jsonBuilder().startObject().field("someField", sampleText).endObject().bytes();
        ParseContext.Document doc = docMapper.parse("someIndex", "someType", "1", json).rootDoc();
        assertEquals(1, doc.getFields("someField").length);
        assertEquals("en", doc.getFields("someField")[0].stringValue());
        // re-parse it
        String builtMapping = docMapper.mappingSource().string();
        docMapper = MapperTestUtils.newDocumentMapperParser("someIndex").parse("someType", new CompressedXContent(builtMapping));
        json = jsonBuilder().startObject().field("someField", sampleText).endObject().bytes();
        doc = docMapper.parse("someIndex", "someType", "1", json).rootDoc();
        assertEquals(1, doc.getFields("someField").length);
        assertEquals("en", doc.getFields("someField")[0].stringValue());
    }

    @Test
    public void testToFields() throws Exception {
        String mapping = copyToStringFromClasspath("mapping-to-fields.json");
        DocumentMapper docMapper = MapperTestUtils.newDocumentMapperParser("someIndex").parse("someType", new CompressedXContent(mapping));
        String sampleText = copyToStringFromClasspath("english.txt");
        BytesReference json = jsonBuilder().startObject().field("someField", sampleText).endObject().bytes();
        ParseContext.Document doc = docMapper.parse("someIndex", "someType", "1", json).rootDoc();
        assertEquals(1, doc.getFields("someField").length);
        assertEquals("en", doc.getFields("someField")[0].stringValue());
        // re-parse it
        String builtMapping = docMapper.mappingSource().string();
        docMapper = MapperTestUtils.newDocumentMapperParser("someIndex").parse("someType", new CompressedXContent(builtMapping));
        json = jsonBuilder().startObject().field("someField", sampleText).endObject().bytes();
        doc = docMapper.parse("someIndex", "someType", "1", json).rootDoc();
        assertEquals(1, doc.getFields("someField").length);
        assertEquals("en", doc.getFields("someField")[0].stringValue());
        assertEquals(1, doc.getFields("english_field").length);
        assertEquals("This is a very small example of a text", doc.getFields("english_field")[0].stringValue());
    }

    private String copyToStringFromClasspath(String path) throws IOException {
        return copyToString(new InputStreamReader(getClass().getResource(path).openStream(), "UTF-8"));
    }
}
