package org.xbib.elasticsearch.index.mapper.langdetect;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.AbstractIndexComponent;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.settings.IndexDynamicSettings;


public class RegisterLangdetectType extends AbstractIndexComponent {

    @Inject
    public RegisterLangdetectType(Index index,
                                  @IndexDynamicSettings Settings indexSettings,
                                  MapperService mapperService) {
        super(index, indexSettings);
        mapperService.documentMapperParser().putTypeParser(LangdetectMapper.CONTENT_TYPE,
                new LangdetectMapper.TypeParser());
    }
}
