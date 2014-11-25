package org.xbib.elasticsearch.action.langdetect.profile;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.TransportAction;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.threadpool.ThreadPool;
import org.xbib.elasticsearch.index.analysis.langdetect.LanguageDetectionException;
import org.xbib.elasticsearch.module.langdetect.LangdetectService;

public class TransportLangdetectProfileAction extends TransportAction<LangdetectProfileRequest, LangdetectProfileResponse> {

    private final LangdetectService service;

    @Inject
    public TransportLangdetectProfileAction(Settings settings, ThreadPool threadPool,
                                            ActionFilters actionFilters, LangdetectService service) {
        super(settings, LangdetectProfileAction.NAME, threadPool, actionFilters);
        this.service = service;
    }

    @Override
    protected void doExecute(LangdetectProfileRequest request, ActionListener<LangdetectProfileResponse> listener) {
        try {
            service.setProfile(request.getProfile());
            listener.onResponse(new LangdetectProfileResponse());
        } catch (LanguageDetectionException e) {
            listener.onFailure(e);
        }
    }
}
