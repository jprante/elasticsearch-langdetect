
package org.xbib.elasticsearch.action.langdetect;

import java.util.List;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.support.single.custom.TransportSingleCustomOperationAction;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.cluster.routing.ShardsIterator;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

import org.xbib.elasticsearch.common.langdetect.Detector;
import org.xbib.elasticsearch.common.langdetect.Language;
import org.xbib.elasticsearch.common.langdetect.LanguageDetectionException;

public class TransportLangdetectAction extends TransportSingleCustomOperationAction<LangdetectRequest, LangdetectResponse> {

    private final Detector detector;

    @Inject
    public TransportLangdetectAction(Settings settings, ThreadPool threadPool, 
            ClusterService clusterService, TransportService transportService) {
        super(settings, threadPool, clusterService, transportService);
        this.detector = new Detector(settings);
    }

    @Override
    protected String transportAction() {
        return LangdetectAction.NAME;
    }

    @Override
    protected String executor() {
        return ThreadPool.Names.GENERIC;
    }

    @Override
    protected ShardsIterator shards(ClusterState state, LangdetectRequest request) {
        return null; // execute always locally
    }

    @Override
    protected LangdetectRequest newRequest() {
        return new LangdetectRequest();
    }

    @Override
    protected LangdetectResponse newResponse() {
        return new LangdetectResponse();
    }

    @Override
    protected ClusterBlockException checkGlobalBlock(ClusterState state, LangdetectRequest request) {
        return state.blocks().globalBlockedException(ClusterBlockLevel.READ);
    }

    @Override
    protected ClusterBlockException checkRequestBlock(ClusterState state, LangdetectRequest request) {
        return null; // no blocks
    }

    @Override
    protected LangdetectResponse shardOperation(LangdetectRequest request, int shardId) throws ElasticsearchException {
        try {
            List<Language> langs = detector.detectAll(request.getText().toUtf8());
            return new LangdetectResponse(langs);
        } catch (LanguageDetectionException e) {
            throw new ElasticsearchException(e.getMessage(), e);
        }
    }
}
