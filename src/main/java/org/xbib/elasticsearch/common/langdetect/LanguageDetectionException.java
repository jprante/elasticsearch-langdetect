package org.xbib.elasticsearch.common.langdetect;

import java.io.IOException;

/**
 *
 */
public class LanguageDetectionException extends IOException {

    private static final long serialVersionUID = 752257035371915875L;

    public LanguageDetectionException(String message) {
        super(message);
    }
}
