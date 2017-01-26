package org.xbib.elasticsearch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthAction;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoAction;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoRequestBuilder;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.LocalTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.node.MockNode;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeValidationException;
import org.xbib.elasticsearch.plugin.langdetect.LangdetectPlugin;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class NodeTestUtils {

    private static final Logger logger = LogManager.getLogger("test");

    private Node node;

    private Client client;

    private AtomicInteger counter = new AtomicInteger();

    private String clustername;

    private String host;

    public void startCluster() {
        try {
            logger.info("settings cluster name");
            setClusterName();
            logger.info("starting nodes");
            this.node = startNode();
            this.client = node.client();
            findNodeAddress();
            ClusterHealthResponse healthResponse = client.execute(ClusterHealthAction.INSTANCE,
                    new ClusterHealthRequest().waitForStatus(ClusterHealthStatus.YELLOW)
                            .timeout(TimeValue.timeValueSeconds(30))).actionGet();
            if (healthResponse != null && healthResponse.isTimedOut()) {
                throw new IOException("cluster state is " + healthResponse.getStatus().name()
                        + ", from here on, everything will fail!");
            }
            logger.info("nodes are started");
        } catch (Throwable t) {
            logger.error("start of nodes failed", t);
        }
    }

    public void stopCluster() {
        try {
            logger.info("stopping nodes");
            closeNodes();
        } catch (Throwable e) {
            logger.error("can not close nodes", e);
        } finally {
            try {
                deleteFiles();
                logger.info("data files wiped");
                Thread.sleep(2000L); // let OS commit changes
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    protected void setClusterName() {
        this.clustername = "test-helper-cluster-"
                + "-" + System.getProperty("user.name")
                + "-" + counter.incrementAndGet();
    }

    protected String getClusterName() {
        return clustername;
    }

    protected Settings getNodeSettings() {
        //String hostname = NetworkUtils.getLocalAddress().getHostName();
        return Settings.builder()
                .put("cluster.name", clustername)
                .put("transport.type", "local")
                .put("http.enabled", false)
                .put("path.home", getHome())
                //.put("node.max_local_storage_nodes", 1)
                .build();
    }

    protected String getHome() {
        return System.getProperty("path.home");
    }

    public Node startNode() throws IOException {
        try {
           return buildNode().start();
        } catch (NodeValidationException e) {
            throw new IOException(e);
        }
    }

    public Client client() {
        return client;
    }

    private void closeNodes() throws IOException {
        if (client != null) {
            client.close();
        }
        if (node != null) {
            node.close();
        }
    }

    protected void findNodeAddress() {
        NodesInfoRequestBuilder nodesInfoRequestBuilder = new NodesInfoRequestBuilder(client, NodesInfoAction.INSTANCE);
        nodesInfoRequestBuilder.setTransport(true);
        NodesInfoResponse response = nodesInfoRequestBuilder.execute().actionGet();
        Object obj = response.getNodes().iterator().next().getTransport().getAddress().publishAddress();
        if (obj instanceof InetSocketTransportAddress) {
            InetSocketTransportAddress address = (InetSocketTransportAddress) obj;
            host = address.address().getHostName();
        } else if (obj instanceof LocalTransportAddress) {
            LocalTransportAddress address = (LocalTransportAddress) obj;
            host = address.getHost();
        } else {
            logger.info("class=" + obj.getClass());
        }
        if (host == null) {
            throw new IllegalArgumentException("host not found");
        }
    }

    public static String findHttpAddress(Client client) {
        NodesInfoRequestBuilder nodesInfoRequestBuilder = new NodesInfoRequestBuilder(client, NodesInfoAction.INSTANCE);
        nodesInfoRequestBuilder.setHttp(true).setTransport(false);
        NodesInfoResponse response = nodesInfoRequestBuilder.execute().actionGet();
        Object obj = response.getNodes().iterator().next().getHttp().getAddress().publishAddress();
        if (obj instanceof InetSocketTransportAddress) {
            InetSocketTransportAddress httpAddress = (InetSocketTransportAddress) obj;
            return "http://" + httpAddress.getHost() + ":" + httpAddress.getPort();
        } else if (obj instanceof LocalTransportAddress) {
            LocalTransportAddress httpAddress = (LocalTransportAddress) obj;
            return "http://" + httpAddress.getHost() + ":" + httpAddress.getPort();
        } else {
            logger.info("class=" + obj.getClass());
        }
        return null;
    }

    public Node buildNodeWithoutPlugins() throws IOException {
        Settings nodeSettings = Settings.builder()
                .put(getNodeSettings())
                .build();
        logger.info("settings={}", nodeSettings.getAsMap());
        return new MockNode(nodeSettings, Collections.emptyList());
    }

    public Node buildNode() throws IOException {
        Settings nodeSettings = Settings.builder()
                .put(getNodeSettings())
                .build();
        logger.info("settings={}", nodeSettings.getAsMap());
        return new MockNode(nodeSettings, Collections.singletonList(LangdetectPlugin.class));
    }


    private static void deleteFiles() throws IOException {
        Path directory = Paths.get(System.getProperty("path.home") + "/data");
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }

        });
    }
}
