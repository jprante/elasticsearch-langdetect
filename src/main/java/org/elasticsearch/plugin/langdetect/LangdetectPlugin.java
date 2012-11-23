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

package org.elasticsearch.plugin.langdetect;

import org.elasticsearch.action.ActionModule;
import org.elasticsearch.action.langdetect.LangdetectAction;
import org.elasticsearch.action.langdetect.TransportLangdetectAction;
import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.rest.RestModule;
import org.elasticsearch.rest.action.langdetect.RestLangdetectAction;

public class LangdetectPlugin extends AbstractPlugin {

    @Override public String name() {
        return "langdetect";
    }

    @Override public String description() {
        return "A language detector";
    }

    public void onModule(RestModule module) {
        module.addRestAction(RestLangdetectAction.class);
    }

    public void onModule(ActionModule module) {
        module.registerAction(LangdetectAction.INSTANCE, TransportLangdetectAction.class);        
    }
}
