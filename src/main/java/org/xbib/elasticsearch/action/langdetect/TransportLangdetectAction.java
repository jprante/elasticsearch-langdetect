package org.xbib.elasticsearch.action.langdetect;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.TransportAction;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.threadpool.ThreadPool;
import org.xbib.elasticsearch.index.analysis.langdetect.Language;
import org.xbib.elasticsearch.index.analysis.langdetect.LanguageDetectionException;
import org.xbib.elasticsearch.module.langdetect.LangdetectService;

import java.util.List;

public class TransportLangdetectAction extends TransportAction<LangdetectRequest, LangdetectResponse> {

    private final LangdetectService service;

    @Inject
    public TransportLangdetectAction(Settings settings, ThreadPool threadPool,
                                     ActionFilters actionFilters, LangdetectService service) {
        super(settings, LangdetectAction.NAME, threadPool, actionFilters);
        this.service = service;
    }

    @Override
    protected void doExecute(LangdetectRequest request, ActionListener<LangdetectResponse> listener) {
        try {
            List<Language> langs = service.detectAll(request.getText());
            listener.onResponse(new LangdetectResponse().setLanguages(langs).setProfile(service.getProfile()));
        } catch (LanguageDetectionException e) {
            listener.onFailure(e);
        }
    }
}
