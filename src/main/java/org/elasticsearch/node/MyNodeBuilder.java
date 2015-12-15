package org.elasticsearch.node;

import org.elasticsearch.Version;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugins.Plugin;

import java.util.Collection;

/**
 * Created by Mahdi on 12/15/2015.
 */
public class MyNodeBuilder {
    public static Node nodeBuilder(Settings preparedSettings, Version version, Collection<Class<? extends Plugin>> classpathPlugins) {
        return new Node(preparedSettings,version,classpathPlugins);
    }
}
