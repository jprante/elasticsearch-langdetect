package org.xbib.elasticsearch.action.langdetect;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.TransportAction;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.threadpool.ThreadPool;
import org.xbib.elasticsearch.common.langdetect.Language;
import org.xbib.elasticsearch.common.langdetect.LanguageDetectionException;
import org.xbib.elasticsearch.common.langdetect.LangdetectService;

import java.util.List;

public class TransportLangdetectAction extends TransportAction<LangdetectRequest, LangdetectResponse> {

    private final LangdetectService service;

    private final LangdetectService shortService;

    @Inject
    public TransportLangdetectAction(Settings settings, ThreadPool threadPool,
                                     ActionFilters actionFilters,  IndexNameExpressionResolver indexNameExpressionResolver) {
        super(settings, LangdetectAction.NAME, threadPool, actionFilters, indexNameExpressionResolver);
        this.service = new LangdetectService(settings);
        this.shortService = new LangdetectService(settings, "short-text");
    }

    @Override
    protected void doExecute(LangdetectRequest request, ActionListener<LangdetectResponse> listener) {
        try {
            List<Language> langs;
            if ("short-text".equals(request.getProfile())) {
                langs = shortService.detectAll(request.getText());
            } else {
                langs = service.detectAll(request.getText());
            }
            listener.onResponse(new LangdetectResponse().setLanguages(langs).setProfile(request.getProfile()));
        } catch (LanguageDetectionException e) {
            listener.onFailure(e);
        }
    }
}
