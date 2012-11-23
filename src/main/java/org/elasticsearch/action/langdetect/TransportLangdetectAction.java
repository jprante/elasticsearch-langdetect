/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.elasticsearch.action.langdetect;

import java.util.List;
import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.support.single.custom.TransportSingleCustomOperationAction;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.block.ClusterBlockLevel;
import org.elasticsearch.cluster.routing.ShardsIterator;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.langdetect.Detector;
import org.elasticsearch.common.langdetect.DetectorFactory;
import org.elasticsearch.common.langdetect.Language;
import org.elasticsearch.common.langdetect.LanguageDetectionException;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

public class TransportLangdetectAction extends TransportSingleCustomOperationAction<LangdetectRequest, LangdetectResponse> {

    private final Detector detector;

    @Inject
    public TransportLangdetectAction(Settings settings, ThreadPool threadPool, 
            ClusterService clusterService, TransportService transportService) {
        super(settings, threadPool, clusterService, transportService);
        try {
            this.detector = DetectorFactory.newInstance().createDefaultDetector();
        } catch (LanguageDetectionException e) {
            throw new ElasticSearchException(e.getMessage(), e);
        }
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
    protected LangdetectResponse shardOperation(LangdetectRequest request, int shardId) throws ElasticSearchException {
        try {
            List<Language> langs = detector.detectAll(request.getText().toUtf8());
            return new LangdetectResponse(langs);
        } catch (LanguageDetectionException e) {
            throw new ElasticSearchException(e.getMessage(), e);
        }
    }
}
