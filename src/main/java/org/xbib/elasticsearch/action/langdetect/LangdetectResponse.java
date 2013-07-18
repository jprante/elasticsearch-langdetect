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

import java.io.IOException;
import java.util.List;

import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;

import org.xbib.elasticsearch.common.langdetect.Language;

/**
 *
 * @author joerg
 */
public class LangdetectResponse extends ActionResponse implements ToXContent {

    private List<Language> languages;

    public LangdetectResponse() {
        
    }
    
    public LangdetectResponse(List<Language> languages) {
        this.languages = languages;
    } 
    
    public List<Language> getLanguages() {
        return languages;
    }    

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startArray("languages");
        for (Language lang : languages) {
              builder.startObject().field("language", lang.getLanguage())
                      .field("probability", lang.getProbability());
        }
        builder.endArray();
        return builder;
    }
    
}
