/*
 * Licensed to Elastic Search and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Elastic Search licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.xbib.elasticsearch.action.langdetect;

import org.elasticsearch.action.Action;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.internal.InternalGenericClient;

/**
 *
 * @author joerg
 */
public class LangdetectAction extends Action<LangdetectRequest, LangdetectResponse, LangdetectRequestBuilder> {

   public static final LangdetectAction INSTANCE = new LangdetectAction();
    public static final String NAME = "langdetect";

    private LangdetectAction() {
        super(NAME);
    }
    
    @Override
    public LangdetectRequestBuilder newRequestBuilder(Client client) {
        return new LangdetectRequestBuilder((InternalGenericClient)client);
    }

    @Override
    public LangdetectResponse newResponse() {
        return new LangdetectResponse();
    }
    
}
