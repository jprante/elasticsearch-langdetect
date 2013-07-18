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

package org.xbib.elasticsearch.common.langdetect;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class DetectorFactory {

    private final static DetectorFactory INSTANCE = new DetectorFactory();
    private Map<String, double[]> wordLangProbMap;
    private List<String> langlist;

    private DetectorFactory() {
        wordLangProbMap = new HashMap();
        langlist = new ArrayList();
    }
    
    public static DetectorFactory getInstance() {
        return INSTANCE;
    }

    public static DetectorFactory newInstance() {
        return new DetectorFactory();
    }
    
    public Map<String, double[]> getWordLangProbMap() {
        return wordLangProbMap;
    }

    public List<String> getLangList() {
        return Collections.unmodifiableList(langlist);
    }

    public Detector createDetector() throws LanguageDetectionException {
        if (langlist.isEmpty()) {
            throw new LanguageDetectionException("need to load profiles");
        }
        Detector detector = new Detector(wordLangProbMap, langlist);
        return detector;
    }

    public Detector createDetector(double alpha) throws LanguageDetectionException {
        Detector detector = createDetector();
        detector.setAlpha(alpha);
        return detector;
    }
    
    public Detector createDefaultDetector() throws LanguageDetectionException {
        ResourceBundle bundle = ResourceBundle.getBundle(DetectorFactory.class.getPackage().getName() + ".languages");
        return createDetector(bundle);
    }
    
    public Detector createDetector(ResourceBundle bundle) throws LanguageDetectionException {
        load(bundle);
        Detector detector = createDetector();
        return detector;
    }
    
    public void loadProfiles(String bundleName) throws LanguageDetectionException {
        ResourceBundle bundle = ResourceBundle.getBundle(bundleName);
        load(bundle);
    }

    public void load(ResourceBundle bundle) throws LanguageDetectionException {
        Enumeration<String> en = bundle.getKeys();
        int index = 0;
        int size = bundle.keySet().size();
        while (en.hasMoreElements()) {
            String line = en.nextElement();
            InputStream in = DetectorFactory.class.getResourceAsStream(line);
            if (in == null) {
                throw new LanguageDetectionException("i/o error in profile locading");
            }
            loadProfile(in, index++, size);
        }
    }

    public void loadProfile(InputStream in, int index, int langsize) throws LanguageDetectionException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            LangProfile profile = mapper.readValue(in, LangProfile.class);
            addProfile(profile, index, langsize);
            index++;
        } catch (IOException e) {
            throw new LanguageDetectionException("i/o error");
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
            }
        }
    }

    public void addProfile(LangProfile profile, int index, int langsize) throws LanguageDetectionException {
        String lang = profile.name;
        if (langlist.contains(lang)) {
            throw new LanguageDetectionException("duplicate the same language profile");
        }
        langlist.add(lang);
        for (String word : profile.freq.keySet()) {
            if (!wordLangProbMap.containsKey(word)) {
                wordLangProbMap.put(word, new double[langsize]);
            }
            int length = word.length();
            if (length >= 1 && length <= 3) {
                double prob = profile.freq.get(word).doubleValue() / profile.n_words[length - 1];
                wordLangProbMap.get(word)[index] = prob;
            }
        }
    }

}
