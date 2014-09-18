package org.xbib.elasticsearch.index.mapper.langdetect;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.fielddata.FieldDataType;
import org.elasticsearch.index.mapper.FieldMapperListener;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.MapperParsingException;
import org.elasticsearch.index.mapper.MergeContext;
import org.elasticsearch.index.mapper.MergeMappingException;
import org.elasticsearch.index.mapper.ObjectMapperListener;
import org.elasticsearch.index.mapper.ParseContext;
import org.elasticsearch.index.mapper.core.StringFieldMapper;
import org.xbib.elasticsearch.common.langdetect.Detector;
import org.xbib.elasticsearch.common.langdetect.Language;
import org.xbib.elasticsearch.common.langdetect.LanguageDetectionException;

import org.elasticsearch.index.mapper.core.AbstractFieldMapper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;


import static org.elasticsearch.index.mapper.MapperBuilders.stringField;


public class LangdetectMapper extends AbstractFieldMapper<Object>{ //#implements Mapper {

    public static final String CONTENT_TYPE = "langdetect";

    protected LangdetectMapper(Names names,
                               Detector detector,
                               StringFieldMapper contentMapper,
                               StringFieldMapper langMapper
                               ){
        super(names, 1.0f, Defaults.FIELD_TYPE, false, null, null, null, null, null, null, null, null, null, null);
        this.contentMapper = contentMapper;
        this.langMapper = langMapper;
        this.detector = detector;
    }


    @Override
    public Object value(Object value) {
        return null;
    }

    public static class Builder extends AbstractFieldMapper.Builder<Builder, LangdetectMapper> {

        private StringFieldMapper.Builder contentBuilder;
        private StringFieldMapper.Builder langBuilder = stringField("lang");
        private Detector detector;

        public Builder(String name, Detector detector) {
            super(name, new FieldType(Defaults.FIELD_TYPE));
            this.detector = detector;
            this.contentBuilder = stringField(name);
            this.builder = this;
        }

        public Builder content(StringFieldMapper.Builder content) {
            this.contentBuilder = content;
            return this;
        }

        public Builder lang(StringFieldMapper.Builder lang) {
            this.langBuilder = lang;
            return this;
        }

        @Override
        public LangdetectMapper build(BuilderContext context) {
            context.path().add(name);
            StringFieldMapper contentMapper = contentBuilder.build(context);
            StringFieldMapper langMapper = langBuilder.build(context);
            context.path().remove();
            return new LangdetectMapper(new Names(name), detector, contentMapper, langMapper);
        }
    }

    public static class TypeParser implements Mapper.TypeParser {

        private Detector detector;

        public TypeParser(Detector detector) {
            this.detector = detector;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public Mapper.Builder parse(String name, Map<String, Object> node, ParserContext parserContext)
                throws MapperParsingException {
            LangdetectMapper.Builder builder = new Builder(name, detector);
            for (Map.Entry<String, Object> entry : node.entrySet()) {
                String fieldName = entry.getKey();
                Object fieldNode = entry.getValue();

                if (fieldName.equals("fields")) {
                    Map<String, Object> fieldsNode = (Map<String, Object>) fieldNode;
                    for (Map.Entry<String, Object> fieldsEntry : fieldsNode.entrySet()) {
                        String propName = fieldsEntry.getKey();
                        Object propNode = fieldsEntry.getValue();

                        if (name.equals(propName)) {
                            builder.content((StringFieldMapper.Builder) parserContext.typeParser("string").parse(name,
                                    (Map<String, Object>) propNode, parserContext));
                        } else if ("lang".equals(propName)) {
                            builder.lang((StringFieldMapper.Builder) parserContext.typeParser("string").parse("lang",
                                    (Map<String, Object>) propNode, parserContext));
                        }
                    }
                }
            }

            return builder;
        }
    }

    private final StringFieldMapper contentMapper;
    private final StringFieldMapper langMapper;
    private final Detector detector;

    @Override
    public FieldType defaultFieldType() {
        return Defaults.FIELD_TYPE;
    }

    @Override
    public FieldDataType defaultFieldDataType() {
        return null;
    }

    @Override
    public void parse(ParseContext context) throws IOException {
        String content = null;

        XContentParser parser = context.parser();
        XContentParser.Token token = parser.currentToken();

        if (token == XContentParser.Token.VALUE_STRING) {
            // try decode UTF-8 base64 (e.g. from attachment mapper plugin)
            content = parser.text();
            try {
                byte[] b = parser.binaryValue();
                if (b != null && b.length > 0) {
                    content = new String(b, Charset.forName("UTF-8"));
                }
            } catch (Exception e) {
                // ignore
            }
        }

        context.externalValue(content);
        contentMapper.parse(context);

        if (content == null) {
            return;
        }

        try {
            List<Language> langs = detector.detectAll(content);
            for (Language lang : langs) {
                context.externalValue(lang.getLanguage());
                langMapper.parse(context);
            }
        } catch (LanguageDetectionException e) {
            context.externalValue("unknown");
            langMapper.parse(context);
        }
    }

    @Override
    protected void parseCreateField(ParseContext context, List<Field> fields) throws IOException {

    }

    @Override
    public void merge(Mapper mergeWith, MergeContext mergeContext) throws MergeMappingException {
    }

    @Override
    public void traverse(FieldMapperListener fieldMapperListener) {
        contentMapper.traverse(fieldMapperListener);
        langMapper.traverse(fieldMapperListener);
    }

    @Override
    public void traverse(ObjectMapperListener objectMapperListener) {
    }

    @Override
    public void close() {
        contentMapper.close();
        langMapper.close();
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(name());
        builder.field("type", CONTENT_TYPE);

        builder.startObject("fields");
        contentMapper.toXContent(builder, params);
        langMapper.toXContent(builder, params);
        builder.endObject();

        builder.endObject();
        return builder;
    }

    @Override
    protected String contentType() {
        return CONTENT_TYPE;
    }
}