package org.xbib.elasticsearch.plugin.langdetect;

import org.elasticsearch.action.ActionModule;
import org.elasticsearch.common.component.LifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.indices.IndicesModule;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.rest.RestModule;
import org.xbib.elasticsearch.action.langdetect.LangdetectAction;
import org.xbib.elasticsearch.action.langdetect.TransportLangdetectAction;
import org.xbib.elasticsearch.index.mapper.langdetect.LangdetectMapper;
import org.xbib.elasticsearch.module.langdetect.LangdetectService;
import org.xbib.elasticsearch.rest.action.langdetect.RestLangdetectAction;

import java.util.ArrayList;
import java.util.Collection;

public class LangdetectPlugin extends Plugin {

    private final Settings settings;

    @Inject
    public LangdetectPlugin(Settings settings) {
        this.settings = settings;
    }

    @Override
    public String name() {
        return "langdetect";
    }

    @Override
    public String description() {
        return "Language detector for Elasticsearch";
    }

    public void onModule(ActionModule module) {
        if (settings.getAsBoolean("plugins.langdetect.enabled", true)) {
            module.registerAction(LangdetectAction.INSTANCE, TransportLangdetectAction.class);
        }
    }

    public void onModule(RestModule module) {
        if (settings.getAsBoolean("plugins.langdetect.enabled", true)) {
            module.addRestAction(RestLangdetectAction.class);
        }
    }

    public void onModule(IndicesModule indicesModule) {
        if (settings.getAsBoolean("plugins.langdetect.enabled", true)) {
            indicesModule.registerMapper(LangdetectMapper.CONTENT_TYPE, new LangdetectMapper.TypeParser());
        }
    }

    @Override
    public Collection<Class<? extends LifecycleComponent>> nodeServices() {
        Collection<Class<? extends LifecycleComponent>> services = new ArrayList<>();
        if (settings.getAsBoolean("plugins.langdetect.enabled", true)) {
            services.add(LangdetectService.class);
        }
        return services;
    }

    /*
    @Override
    public Collection<Module> nodeModules() {
        Collection<Module> modules = new ArrayList<>();
        if (settings.getAsBoolean("plugins.langdetect.enabled", true)) {
            modules.add(new LangdetectModule());
        }
        return modules;
    }
    */

}
