
package org.xbib.elasticsearch.module.langdetect;

import org.elasticsearch.common.inject.Binder;
import org.elasticsearch.common.inject.Module;
import org.xbib.elasticsearch.plugin.langdetect.RegisterLangdetectType;

public class LangdetectModule implements Module {

    @Override
    public void configure(Binder binder) {
        binder.bind(RegisterLangdetectType.class).asEagerSingleton();
    }
}
