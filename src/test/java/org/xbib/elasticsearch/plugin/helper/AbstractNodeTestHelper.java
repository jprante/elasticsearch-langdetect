package org.xbib.elasticsearch.plugin.helper;

import org.elasticsearch.Version;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoRequest;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.client.support.AbstractClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.MyNodeBuilder;
import org.elasticsearch.node.Node;
import org.elasticsearch.plugins.Plugin;
import org.junit.After;
import org.junit.Before;
import org.xbib.elasticsearch.plugin.langdetect.LangdetectPlugin;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.elasticsearch.common.settings.Settings.settingsBuilder;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

public abstract class AbstractNodeTestHelper {

    private Map<String, Node> nodes = new HashMap<>();

    private Map<String, AbstractClient> clients = new HashMap<>();

    private AtomicInteger counter = new AtomicInteger();

    private String cluster;

    private String host;

    private int port;

    protected void setClusterName() {
        this.cluster = "test-aggregations-cluster-"
                + getLocalAddress().getHostName()
                + "-" + System.getProperty("user.name")
                + "-" + counter.incrementAndGet();
    }

    protected String getClusterName() {
        return cluster;
    }

    protected String getHome() {
        return System.getProperty("path.home");
    }

    protected Settings getSettings() {
        return settingsBuilder()
                .put("host", host)
                .put("port", port)
                .put("cluster.name", cluster)
                .put("path.home", getHome())
                .put("plugin.types", LangdetectPlugin.class.getName())
                .build();
    }

    protected Settings getNodeSettings() {
        return settingsBuilder()
                .put("cluster.name", cluster)
                .put("path.home", getHome())
                .put("plugin.types", LangdetectPlugin.class.getName())
                .build();
    }

    @Before
    public void startNodes() throws Exception {
        setClusterName();
        startNode("1");
        findNodeAddress();
    }

    @After
    public void stopNodes() throws Exception {
        try {
            client("1").admin().indices().prepareDelete("_all").execute().actionGet();
        } catch (Exception e) {
            //
        } finally {
            closeAllNodes();
        }
    }

    protected Node startNode(String id) {
        return buildNode(id).start();
    }

    public AbstractClient client(String id) {
        return clients.get(id);
    }

    private Node buildNode(String id) {
        Settings finalSettings = settingsBuilder()
                .put(getNodeSettings())
                .put("name", id)
                .build();
        List<Class<? extends Plugin>> classpathPlugins = new ArrayList<>();
        classpathPlugins.add(LangdetectPlugin.class);
        Node node = MyNodeBuilder.nodeBuilder(finalSettings, Version.CURRENT, classpathPlugins);
        AbstractClient client = (AbstractClient)node.client();
        nodes.put(id, node);
        clients.put(id, client);
        return node;
    }

    protected void findNodeAddress() {
        NodesInfoRequest nodesInfoRequest = new NodesInfoRequest().transport(true);
        NodesInfoResponse response = client("1").admin().cluster().nodesInfo(nodesInfoRequest).actionGet();
        Object obj = response.iterator().next().getTransport().getAddress()
                .publishAddress();
        if (obj instanceof InetSocketTransportAddress) {
            InetSocketTransportAddress address = (InetSocketTransportAddress) obj;
            host = address.address().getHostName();
            port = address.address().getPort();
        }
    }

    public void closeAllNodes() throws IOException {
        for (AbstractClient client : clients.values()) {
            client.close();
        }
        clients.clear();
        for (Node node : nodes.values()) {
            if (node != null) {
                node.close();
            }
        }
        nodes.clear();
    }

    private final static InetAddress localAddress;

    static {
        InetAddress address;
        try {
            address = InetAddress.getLocalHost();
        } catch (Throwable e) {
            address = InetAddress.getLoopbackAddress();
        }
        localAddress = address;
    }

    public static InetAddress getLocalAddress() {
        return localAddress;
    }


}
