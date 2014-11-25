package org.xbib.elasticsearch.module.langdetect;

import org.elasticsearch.common.inject.Binder;
import org.elasticsearch.common.inject.Module;

public class LangdetectIndexModule implements Module {

    @Override
    public void configure(Binder binder) {
        binder.bind(RegisterLangdetectType.class).asEagerSingleton();
    }

}
