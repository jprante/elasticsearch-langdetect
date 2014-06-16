package org.xbib.elasticsearch.plugin.langdetect;

import org.elasticsearch.action.ActionModule;
import org.elasticsearch.common.component.LifecycleComponent;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.rest.RestModule;
import org.xbib.elasticsearch.action.langdetect.LangdetectAction;
import org.xbib.elasticsearch.action.langdetect.TransportLangdetectAction;
import org.xbib.elasticsearch.common.langdetect.Detector;
import org.xbib.elasticsearch.module.langdetect.LangdetectModule;
import org.xbib.elasticsearch.rest.action.langdetect.RestLangdetectAction;

import java.util.Collection;

import static org.elasticsearch.common.collect.Lists.newArrayList;

public class LangdetectPlugin extends AbstractPlugin {

    @Override
    public String name() {
        return "langdetect-" +
                Build.getInstance().getVersion() + "-" +
                Build.getInstance().getShortHash();
    }

    @Override
    public String description() {
        return "A language detector";
    }

    public void onModule(RestModule module) {
        module.addRestAction(RestLangdetectAction.class);
    }

    public void onModule(ActionModule module) {
        module.registerAction(LangdetectAction.INSTANCE, TransportLangdetectAction.class);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Collection<Class<? extends LifecycleComponent>> services() {
        Collection<Class<? extends LifecycleComponent>> services = newArrayList();
        services.add(Detector.class);
        return services;
    }

    @Override
    public Collection<Class<? extends Module>> indexModules() {
        Collection<Class<? extends Module>> modules = newArrayList();
        modules.add(LangdetectModule.class);
        return modules;
    }
}
