package org.xbib.elasticsearch.index.mapper.langdetect;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.index.AbstractIndexComponent;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.settings.IndexSettingsService;

public class RegisterLangdetectType extends AbstractIndexComponent {

    @Inject
    public RegisterLangdetectType(Index index,
                                  IndexSettingsService indexSettingsService,
                                  MapperService mapperService) {
        super(index, indexSettingsService.indexSettings());
        mapperService.documentMapperParser().putTypeParser(LangdetectMapper.CONTENT_TYPE,
                new LangdetectMapper.TypeParser());
    }

}
