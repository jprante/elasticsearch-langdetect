package org.xbib.elasticsearch.index.mapper.langdetect;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.elasticsearch.common.settings.ImmutableSettings;
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
import org.elasticsearch.index.mapper.core.AbstractFieldMapper;
import org.elasticsearch.index.mapper.core.StringFieldMapper;
import org.xbib.elasticsearch.module.langdetect.LangdetectService;
import org.xbib.elasticsearch.index.analysis.langdetect.Language;
import org.xbib.elasticsearch.index.analysis.langdetect.LanguageDetectionException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.index.mapper.MapperBuilders.stringField;

public class LangdetectMapper extends AbstractFieldMapper<Object> {

    public static final String CONTENT_TYPE = "langdetect";

    public static class Builder extends AbstractFieldMapper.Builder<Builder, LangdetectMapper> {

        private StringFieldMapper.Builder contentBuilder;
        private StringFieldMapper.Builder langBuilder;
        private ImmutableSettings.Builder settingsBuilder;

        public Builder(String name) {
            super(name, new FieldType(Defaults.FIELD_TYPE));
            this.builder = this;
            this.contentBuilder = stringField(name);
            this.langBuilder =  stringField("lang");
            this.settingsBuilder = ImmutableSettings.settingsBuilder();
        }

        public Builder content(StringFieldMapper.Builder content) {
            this.contentBuilder = content;
            return this;
        }

        public Builder lang(StringFieldMapper.Builder lang) {
            this.langBuilder = lang;
            return this;
        }

        public Builder ntrials(int trials) {
            settingsBuilder.put("number_of_trials", trials);
            return this;
        }

        public Builder alpha(double alpha) {
            settingsBuilder.put("alpha", alpha);
            return this;
        }

        public Builder alphaWidth(double alphaWidth) {
            settingsBuilder.put("alpha_width", alphaWidth);
            return this;
        }

        public Builder iterationLimit(int iterationLimit) {
            settingsBuilder.put("iteration_limit", iterationLimit);
            return this;
        }

        public Builder probThreshold(double probThreshold) {
            settingsBuilder.put("prob_threshold", probThreshold);
            return this;
        }

        public Builder convThreshold(double convThreshold) {
            settingsBuilder.put("conv_threshold", convThreshold);
            return this;
        }

        public Builder baseFreq(int baseFreq) {
            settingsBuilder.put("base_freq", baseFreq);
            return this;
        }

        public Builder pattern(String pattern) {
            settingsBuilder.put("pattern", pattern);
            return this;
        }

        public Builder max(int max) {
            settingsBuilder.put("max", max);
            return this;
        }

        public Builder binary(boolean binary) {
            settingsBuilder.put("binary", binary);
            return this;
        }

        public Builder map(Map<String,String> map) {
            for (String key : map.keySet()) {
                settingsBuilder.put("map." + key, map.get(key));
            }
            return this;
        }

        public Builder languages(List<String> languages) {
            settingsBuilder.putArray("languages", languages.toArray(new String[languages.size()]));
            return this;
        }

        @Override
        public LangdetectMapper build(BuilderContext context) {
            context.path().add(name);
            StringFieldMapper contentMapper = contentBuilder.build(context);
            StringFieldMapper langMapper = langBuilder.build(context);
            context.path().remove();
            LangdetectService detector = new LangdetectService(settingsBuilder.build());
            detector.start();
            return new LangdetectMapper(new Names(name), contentMapper, langMapper, detector);
        }
    }

    public static class TypeParser implements Mapper.TypeParser {

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public Mapper.Builder parse(String name, Map<String, Object> node, ParserContext parserContext)
                throws MapperParsingException {
            LangdetectMapper.Builder builder = new Builder(name);
            for (Map.Entry<String, Object> entry : node.entrySet()) {
                String fieldName = entry.getKey();
                Object fieldNode = entry.getValue();
                switch (fieldName) {
                    case "fields": {
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
                        break;
                    }
                    case "number_of_trials": {
                        builder.ntrials((Integer)fieldNode);
                        break;
                    }
                    case "alpha": {
                        builder.alpha((Double)fieldNode);
                        break;
                    }
                    case "alpha_width": {
                        builder.alphaWidth((Double)fieldNode);
                        break;
                    }
                    case "iteration_limit": {
                        builder.iterationLimit((Integer)fieldNode);
                        break;
                    }
                    case "prob_threshold": {
                        builder.probThreshold((Double)fieldNode);
                        break;
                    }
                    case "conv_threshold": {
                        builder.convThreshold((Double)fieldNode);
                        break;
                    }
                    case "base_freq": {
                        builder.baseFreq((Integer)fieldNode);
                        break;
                    }
                    case "pattern": {
                        builder.pattern((String)fieldNode);
                        break;
                    }
                    case "max": {
                        builder.max((Integer)fieldNode);
                        break;
                    }
                    case "binary": {
                        builder.binary((Boolean)fieldNode);
                        break;
                    }
                    case "map" : {
                        builder.map((Map<String,String>)fieldNode);
                        break;
                    }
                    case "languages" : {
                        builder.languages((List<String>)fieldNode);
                        break;
                    }
                }
            }
            return builder;
        }
    }

    private final StringFieldMapper contentMapper;

    private final StringFieldMapper langMapper;

    private final LangdetectService detector;

    public LangdetectMapper(Names names, StringFieldMapper contentMapper, StringFieldMapper langMapper,
                            LangdetectService detector) {
        super(names, 1.0f, Defaults.FIELD_TYPE, false, null, null, null, null, null, null, null, null, null, null);
        this.contentMapper = contentMapper;
        this.langMapper = langMapper;
        this.detector = detector;
    }

    @Override
    public FieldType defaultFieldType() {
        return Defaults.FIELD_TYPE;
    }

    @Override
    public FieldDataType defaultFieldDataType() {
        return null;
    }

    @Override
    public Object value(Object value) {
        return null;
    }

    @Override
    public void parse(ParseContext context) throws IOException {
        String content = null;
        XContentParser parser = context.parser();
        XContentParser.Token token = parser.currentToken();
        if (token == XContentParser.Token.VALUE_STRING) {
            content = parser.text();
            if (detector.getSettings().getAsBoolean("binary", false)) {
                try {
                    byte[] b = parser.binaryValue();
                    if (b != null && b.length > 0) {
                        content = new String(b, Charset.forName("UTF-8"));
                    }
                } catch (Exception e) {
                }
            }
        }
        if (content == null) {
            return;
        }
        context = context.createExternalValueContext(content);
        contentMapper.parse(context);
        try {
            List<Language> langs = detector.detectAll(content);
            for (Language lang : langs) {
                context = context.createExternalValueContext(lang.getLanguage());
                langMapper.parse(context);
            }
        } catch (LanguageDetectionException e) {
            context = context.createExternalValueContext("unknown");
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