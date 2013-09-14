package org.xbib.elasticsearch.plugin.langdetect;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.AbstractIndexComponent;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AnalysisService;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.settings.IndexSettings;

import org.xbib.elasticsearch.common.langdetect.Detector;
import org.xbib.elasticsearch.index.mapper.langdetect.LangdetectMapper;

public class RegisterLangdetectType extends AbstractIndexComponent {

    @Inject
    public RegisterLangdetectType(Index index, @IndexSettings Settings indexSettings, MapperService mapperService,
                               AnalysisService analysisService,  Detector detector) {
        super(index, indexSettings);
        mapperService.documentMapperParser().putTypeParser("langdetect",
                new LangdetectMapper.TypeParser(analysisService, detector));
    }
}
