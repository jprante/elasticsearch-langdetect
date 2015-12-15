package org.xbib.elasticsearch.index;

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
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.settings.IndexSettingsModule;
import org.elasticsearch.index.similarity.SimilarityLookupService;
import org.elasticsearch.indices.analysis.IndicesAnalysisService;

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

    public static DocumentMapperParser newMapperParser() {
        return newMapperParser(Settings.builder()
                .put("path.home", System.getProperty("path.home"))
                .build());
    }

    public static DocumentMapperParser newMapperParser(Settings settings) {
        Settings forcedSettings = Settings.builder()
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                .put(settings)
                .build();
        SimilarityLookupService similarityLookupService = newSimilarityLookupService(forcedSettings);
        MapperService mapperService = new MapperService(new Index("test"),
                forcedSettings,
                newAnalysisService(forcedSettings),
                similarityLookupService,
                null);
        return new DocumentMapperParser(
                forcedSettings,
                mapperService,
                MapperTestUtils.newAnalysisService(forcedSettings),
                similarityLookupService,
                null);
    }
}
