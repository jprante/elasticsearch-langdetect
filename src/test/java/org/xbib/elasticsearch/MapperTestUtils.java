package org.xbib.elasticsearch;

import org.elasticsearch.Version;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.inject.Injector;
import org.elasticsearch.common.inject.ModulesBuilder;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsModule;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.EnvironmentModule;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.IndexNameModule;
import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.index.analysis.AnalysisService;
import org.elasticsearch.index.mapper.DocumentMapperParser;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.mapper.MetadataFieldMapper;
import org.elasticsearch.index.mapper.core.BinaryFieldMapper;
import org.elasticsearch.index.mapper.core.BooleanFieldMapper;
import org.elasticsearch.index.mapper.core.ByteFieldMapper;
import org.elasticsearch.index.mapper.core.CompletionFieldMapper;
import org.elasticsearch.index.mapper.core.DateFieldMapper;
import org.elasticsearch.index.mapper.core.DoubleFieldMapper;
import org.elasticsearch.index.mapper.core.FloatFieldMapper;
import org.elasticsearch.index.mapper.core.IntegerFieldMapper;
import org.elasticsearch.index.mapper.core.LongFieldMapper;
import org.elasticsearch.index.mapper.core.ShortFieldMapper;
import org.elasticsearch.index.mapper.core.StringFieldMapper;
import org.elasticsearch.index.mapper.core.TokenCountFieldMapper;
import org.elasticsearch.index.mapper.core.TypeParsers;
import org.elasticsearch.index.mapper.geo.GeoPointFieldMapper;
import org.elasticsearch.index.mapper.internal.AllFieldMapper;
import org.elasticsearch.index.mapper.internal.IdFieldMapper;
import org.elasticsearch.index.mapper.internal.IndexFieldMapper;
import org.elasticsearch.index.mapper.internal.ParentFieldMapper;
import org.elasticsearch.index.mapper.internal.RoutingFieldMapper;
import org.elasticsearch.index.mapper.internal.SourceFieldMapper;
import org.elasticsearch.index.mapper.internal.TTLFieldMapper;
import org.elasticsearch.index.mapper.internal.TimestampFieldMapper;
import org.elasticsearch.index.mapper.internal.TypeFieldMapper;
import org.elasticsearch.index.mapper.internal.UidFieldMapper;
import org.elasticsearch.index.mapper.internal.VersionFieldMapper;
import org.elasticsearch.index.mapper.ip.IpFieldMapper;
import org.elasticsearch.index.mapper.object.ObjectMapper;
import org.elasticsearch.index.settings.IndexSettingsModule;
import org.elasticsearch.index.similarity.SimilarityLookupService;
import org.elasticsearch.indices.analysis.IndicesAnalysisService;
import org.elasticsearch.indices.mapper.MapperRegistry;
import org.xbib.elasticsearch.common.langdetect.LangdetectService;
import org.xbib.elasticsearch.index.mapper.langdetect.LangdetectMapper;

import java.util.LinkedHashMap;
import java.util.Map;

public class MapperTestUtils {

    public static AnalysisService newAnalysisService(Settings indexSettings) {
        Injector parentInjector = new ModulesBuilder().add(new SettingsModule(indexSettings),
                new EnvironmentModule(new Environment(indexSettings))).createInjector();
        Index index = new Index("test");
        Injector injector = new ModulesBuilder().add(
                new IndexSettingsModule(index, indexSettings),
                new IndexNameModule(index),
                new AnalysisModule(indexSettings, parentInjector.getInstance(IndicesAnalysisService.class))).createChildInjector(parentInjector);

        return injector.getInstance(AnalysisService.class);
    }

    public static SimilarityLookupService newSimilarityLookupService(Settings indexSettings) {
        return new SimilarityLookupService(new Index("test"), indexSettings);
    }

    public static DocumentMapperParser newDocumentMapperParser() {
        return newDocumentMapperParser(Settings.builder()
                .put("path.home", System.getProperty("path.home"))
                .build());
    }

    public static DocumentMapperParser newDocumentMapperParser(Settings settings) {
        Settings forcedSettings = Settings.builder()
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                .put(settings)
                .build();
        SimilarityLookupService similarityLookupService = newSimilarityLookupService(forcedSettings);
        Map<String, Mapper.TypeParser> mappers = registerBuiltInMappers();
        mappers.put(LangdetectMapper.CONTENT_TYPE, new LangdetectMapper.TypeParser());
        Map<String, MetadataFieldMapper.TypeParser> metadataMappers = registerBuiltInMetadataMappers();
        MapperRegistry mapperRegistry = new MapperRegistry(mappers, metadataMappers);
        MapperService mapperService = new MapperService(new Index("test"),
                forcedSettings,
                newAnalysisService(forcedSettings),
                similarityLookupService,
                null,
                mapperRegistry);
        return new DocumentMapperParser(
                forcedSettings,
                mapperService,
                MapperTestUtils.newAnalysisService(forcedSettings),
                similarityLookupService,
                null,
                mapperRegistry);
    }

    // copy from org.elasticsearch.indices.IndicesModule
    private static Map<String, Mapper.TypeParser> registerBuiltInMappers() {
        Map<String, Mapper.TypeParser> mapperParsers = new LinkedHashMap<>();
        mapperParsers.put(ByteFieldMapper.CONTENT_TYPE, new ByteFieldMapper.TypeParser());
        mapperParsers.put(ShortFieldMapper.CONTENT_TYPE, new ShortFieldMapper.TypeParser());
        mapperParsers.put(IntegerFieldMapper.CONTENT_TYPE, new IntegerFieldMapper.TypeParser());
        mapperParsers.put(LongFieldMapper.CONTENT_TYPE, new LongFieldMapper.TypeParser());
        mapperParsers.put(FloatFieldMapper.CONTENT_TYPE, new FloatFieldMapper.TypeParser());
        mapperParsers.put(DoubleFieldMapper.CONTENT_TYPE, new DoubleFieldMapper.TypeParser());
        mapperParsers.put(BooleanFieldMapper.CONTENT_TYPE, new BooleanFieldMapper.TypeParser());
        mapperParsers.put(BinaryFieldMapper.CONTENT_TYPE, new BinaryFieldMapper.TypeParser());
        mapperParsers.put(DateFieldMapper.CONTENT_TYPE, new DateFieldMapper.TypeParser());
        mapperParsers.put(IpFieldMapper.CONTENT_TYPE, new IpFieldMapper.TypeParser());
        mapperParsers.put(StringFieldMapper.CONTENT_TYPE, new StringFieldMapper.TypeParser());
        mapperParsers.put(TokenCountFieldMapper.CONTENT_TYPE, new TokenCountFieldMapper.TypeParser());
        mapperParsers.put(ObjectMapper.CONTENT_TYPE, new ObjectMapper.TypeParser());
        mapperParsers.put(ObjectMapper.NESTED_CONTENT_TYPE, new ObjectMapper.TypeParser());
        mapperParsers.put(TypeParsers.MULTI_FIELD_CONTENT_TYPE, TypeParsers.multiFieldConverterTypeParser);
        mapperParsers.put(CompletionFieldMapper.CONTENT_TYPE, new CompletionFieldMapper.TypeParser());
        mapperParsers.put(GeoPointFieldMapper.CONTENT_TYPE, new GeoPointFieldMapper.TypeParser());
        return mapperParsers;
    }

    // copy from org.elasticsearch.indices.IndicesModule
    private static Map<String, MetadataFieldMapper.TypeParser> registerBuiltInMetadataMappers() {
        Map<String, MetadataFieldMapper.TypeParser> metadataMapperParsers = new LinkedHashMap<>();
        metadataMapperParsers.put(UidFieldMapper.NAME, new UidFieldMapper.TypeParser());
        metadataMapperParsers.put(IdFieldMapper.NAME, new IdFieldMapper.TypeParser());
        metadataMapperParsers.put(RoutingFieldMapper.NAME, new RoutingFieldMapper.TypeParser());
        metadataMapperParsers.put(IndexFieldMapper.NAME, new IndexFieldMapper.TypeParser());
        metadataMapperParsers.put(SourceFieldMapper.NAME, new SourceFieldMapper.TypeParser());
        metadataMapperParsers.put(TypeFieldMapper.NAME, new TypeFieldMapper.TypeParser());
        metadataMapperParsers.put(AllFieldMapper.NAME, new AllFieldMapper.TypeParser());
        metadataMapperParsers.put(TimestampFieldMapper.NAME, new TimestampFieldMapper.TypeParser());
        metadataMapperParsers.put(TTLFieldMapper.NAME, new TTLFieldMapper.TypeParser());
        metadataMapperParsers.put(VersionFieldMapper.NAME, new VersionFieldMapper.TypeParser());
        metadataMapperParsers.put(ParentFieldMapper.NAME, new ParentFieldMapper.TypeParser());
        return metadataMapperParsers;
    }
}
