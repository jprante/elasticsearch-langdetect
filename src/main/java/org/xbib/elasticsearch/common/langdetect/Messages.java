package org.xbib.elasticsearch.common.langdetect;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {

    private static final ResourceBundle RESOURCE_BUNDLE =
            ResourceBundle.getBundle(Messages.class.getPackage().getName() + ".messages");

    private Messages() {
    }

    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}
