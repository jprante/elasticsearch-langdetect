package org.xbib.elasticsearch.action.langdetect;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.TransportAction;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;
import org.xbib.elasticsearch.common.langdetect.LangdetectService;
import org.xbib.elasticsearch.common.langdetect.Language;
import org.xbib.elasticsearch.common.langdetect.LanguageDetectionException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class TransportLangdetectAction extends TransportAction<LangdetectRequest, LangdetectResponse> {

    private static final Map<String, LangdetectService> services = new HashMap<>();

    @Inject
    public TransportLangdetectAction(Settings settings, ThreadPool threadPool,
                                     ActionFilters actionFilters,
                                     IndexNameExpressionResolver indexNameExpressionResolver,
                                     TransportService transportService) {
        super(settings, LangdetectAction.NAME, threadPool, actionFilters, indexNameExpressionResolver, transportService.getTaskManager());
        services.put("", new LangdetectService(settings));
    }

    @Override
    protected void doExecute(LangdetectRequest request, ActionListener<LangdetectResponse> listener) {
        String profile = request.getProfile();
        if (profile == null) {
            profile = "";
        }
        if (!services.containsKey(profile)) {
            services.put(profile, new LangdetectService(settings, profile));
        }
        // detectAll() is not thread-safe
        synchronized (services) {
            try {
                List<Language> langs = services.get(profile).detectAll(request.getText());
                listener.onResponse(new LangdetectResponse().setLanguages(langs).setProfile(request.getProfile()));
            } catch (LanguageDetectionException e) {
                listener.onFailure(e);
            }
        }
    }
}
