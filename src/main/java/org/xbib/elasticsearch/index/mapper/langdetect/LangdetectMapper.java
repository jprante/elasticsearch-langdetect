
package org.xbib.elasticsearch.index.mapper.langdetect;

import org.elasticsearch.index.analysis.NamedAnalyzer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Hashtable;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.analysis.AnalysisService;
import org.elasticsearch.index.mapper.*;
import org.elasticsearch.index.mapper.core.StringFieldMapper;
import org.xbib.elasticsearch.common.langdetect.Detector;
import org.xbib.elasticsearch.common.langdetect.Language;
import org.xbib.elasticsearch.common.langdetect.LanguageDetectionException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.index.mapper.MapperBuilders.stringField;


public class LangdetectMapper implements Mapper {

    public static final String CONTENT_TYPE = "langdetect";
 
    public static class MultiLangBuilder  {
      
            private Hashtable langAnalysers =  new Hashtable();
            private BuilderContext builderContext;

            public class LangAnalyser {
                private StringFieldMapper.Builder builder;
                private StringFieldMapper mapper;

                public LangAnalyser(StringFieldMapper.Builder builder, StringFieldMapper mapper) {
                     this.builder = builder;
                     this.mapper = mapper;
                }
            }

            public MultiLangBuilder() {
                this.langAnalysers = new Hashtable();
           }

            public void build(BuilderContext context) {
                 this.builderContext = context;
                  for (Object k : langAnalysers.keySet()) {
                       LangAnalyser existingBuilder = (LangAnalyser) this.langAnalysers.get(k);
                        langAnalysers.put(k,  new LangAnalyser(existingBuilder.builder, existingBuilder.builder.build(builderContext)));
                }
            }

            public void parse(String lang, Map<String, Object> properties, AnalysisService analysisService)  {

                StringFieldMapper.Builder multiLangBuilder = stringField(lang);

                if (properties.containsKey("index_analyzer")) {
                    multiLangBuilder.indexAnalyzer(analysisService.analyzer(properties.get("index_analyzer").toString()));
                }
                if (properties.containsKey("search_analyzer")) {
                    multiLangBuilder.searchAnalyzer(analysisService.analyzer(properties.get("search_analyzer").toString()));
                }
                if (properties.containsKey("analyzer")) {
                    NamedAnalyzer na = analysisService.analyzer(properties.get("analyzer").toString());
                    multiLangBuilder.searchAnalyzer(na);
                    multiLangBuilder.indexAnalyzer(na);
                }

                this.langAnalysers.put(lang, new LangAnalyser(multiLangBuilder, null));
            }

            public void set_builder(String language, StringFieldMapper.Builder builder) {
                LangAnalyser existing = (LangAnalyser) this.langAnalysers.get(language);
                this.langAnalysers.put(language, new LangAnalyser(builder, existing.mapper));
            }

           public void parse(String language, ParseContext context) throws IOException {
                LangAnalyser existing = (LangAnalyser) this.langAnalysers.get(language);
                if (existing != null) {
                    existing.mapper.parse(context);
               } else {
                    // consume context external value
                     context.externalValue();
               }
           }

           public void traverse(FieldMapperListener fl) {
                Iterator iterator = langAnalysers.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry mapEntry = (Map.Entry) iterator.next();
                    ((LangAnalyser)mapEntry.getValue()).mapper.traverse(fl);
                }
           }

            public void close() {
                Iterator iterator = langAnalysers.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry mapEntry = (Map.Entry) iterator.next();
                    ((LangAnalyser)mapEntry.getValue()).mapper.close();
                }
           }

            public ArrayList<StringFieldMapper> get_mappers()  {
               ArrayList<StringFieldMapper> mappers = new ArrayList<StringFieldMapper>();
                Iterator iterator = langAnalysers.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry mapEntry = (Map.Entry) iterator.next();
                   mappers.add(((LangAnalyser)mapEntry.getValue()).mapper);
                }
                return mappers;
           }

    }
    

    public static class Builder extends Mapper.Builder<Builder, LangdetectMapper> {

        private StringFieldMapper.Builder contentBuilder;
        private StringFieldMapper.Builder langBuilder = stringField("lang");
        private LangdetectMapper.MultiLangBuilder multiLangBuilder;
        private Detector detector;

        public Builder(String name, Detector detector) {
            super(name);
            this.detector = detector;
            this.contentBuilder = stringField(name);
            this.multiLangBuilder = new MultiLangBuilder();
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
            this.multiLangBuilder.build(context);
            context.path().remove();
            return new LangdetectMapper(name, detector, contentMapper, langMapper, multiLangBuilder);
        }
    }

    public static class TypeParser implements Mapper.TypeParser {

        private AnalysisService analysisService;
        private Detector detector;

        public TypeParser(AnalysisService analysisService, Detector detector) {
            this.analysisService = analysisService;
            this.detector = detector;
        }

        @SuppressWarnings({"unchecked","rawtypes"})
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
                        } else if (detector.getLangList().contains(propName)) {
                            Map<String, Object> langProperties = ( Map<String, Object>)propNode;
                            builder.multiLangBuilder.parse(propName, langProperties, analysisService);
                        }
                    }
                }
                /* personal analyser for content.lang field */
                if (fieldName.equals("person_analyzer")) {
                    builder.langBuilder.searchAnalyzer(analysisService.analyzer(fieldNode.toString()));
                    builder.langBuilder.indexAnalyzer(analysisService.analyzer(fieldNode.toString()));
                }
                /* analysers for content field */
                if (fieldName.equals("index_analyzer")) {
                    builder.contentBuilder.indexAnalyzer(analysisService.analyzer(fieldNode.toString()));
                }
                if (fieldName.equals("search_analyzer")) {
                    builder.contentBuilder.searchAnalyzer(analysisService.analyzer(fieldNode.toString()));
                }
                if (fieldName.equals("analyzer")) {
                    builder.contentBuilder.indexAnalyzer(analysisService.analyzer(fieldNode.toString()));
                    builder.contentBuilder.searchAnalyzer(analysisService.analyzer(fieldNode.toString()));
                }
            }

            return builder;
        }
    }

    private final String name;
    private final Detector detector;
    private final StringFieldMapper contentMapper;
    private final StringFieldMapper langMapper;
    private final MultiLangBuilder contentLang;

    public LangdetectMapper(String name, Detector detector, StringFieldMapper contentMapper, StringFieldMapper langMapper, MultiLangBuilder contentLang) {
        this.name = name;
        this.detector = detector;
        this.contentMapper = contentMapper;
        this.langMapper = langMapper;
        this.contentLang = contentLang;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void parse(ParseContext context) throws IOException {
        String content = null;

        XContentParser parser = context.parser();
        XContentParser.Token token = parser.currentToken();

        if (token == XContentParser.Token.VALUE_STRING) {
            content = parser.text();
        }

        context.externalValue(content);
        contentMapper.parse(context);

        List<Language> langs = null;
        try {
            langs = detector.detectAll(content);
            for (Language lang : langs) {
                context.externalValue(lang.getLanguage());
                langMapper.parse(context);
            }
        } catch(LanguageDetectionException e) {
            // language detection failed, continue
        }
        if (langs !=null && !langs.isEmpty())
        {
            context.externalValue(content);
            contentLang.parse(langs.get(0).getLanguage(), context);
        }
    }

    @Override
    public void merge(Mapper mergeWith, MergeContext mergeContext) throws MergeMappingException {
    }

    @Override
    public void traverse(FieldMapperListener fieldMapperListener) {
        contentMapper.traverse(fieldMapperListener);
        langMapper.traverse(fieldMapperListener);
        contentLang.traverse(fieldMapperListener);

    }

    @Override
    public void traverse(ObjectMapperListener objectMapperListener) {
    }

    @Override
    public void close() {
        contentMapper.close();
        langMapper.close();
        contentLang.close();
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(name);
        builder.field("type", CONTENT_TYPE);

        builder.startObject("fields");
        contentMapper.toXContent(builder, params);
        langMapper.toXContent(builder, params);
        ArrayList<StringFieldMapper> contentlangmappers = contentLang.get_mappers();
        for(int i = 0; i < contentlangmappers.size(); i++) {
            contentlangmappers.get(i).toXContent(builder, params);
        }
        builder.endObject();
       
        
        builder.endObject();
        return builder;
    }
}