package org.xbib.elasticsearch.module.langdetect;

import org.elasticsearch.common.inject.Binder;
import org.elasticsearch.common.inject.Module;
import org.xbib.elasticsearch.index.mapper.langdetect.RegisterLangdetectType;

public class LangdetectModule implements Module {

    @Override
    public void configure(Binder binder) {
        binder.bind(RegisterLangdetectType.class).asEagerSingleton();
        binder.bind(LangdetectService.class).asEagerSingleton();
    }

}
