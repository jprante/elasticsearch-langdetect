package org.xbib.elasticsearch;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.MockNode;
import org.elasticsearch.node.Node;
import org.junit.After;
import org.junit.Before;
import org.xbib.elasticsearch.plugin.langdetect.LangdetectPlugin;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class NodeTestUtils {

    private Node node;
    private Client client;

    public static Node createNode() {
        Settings nodeSettings = Settings.settingsBuilder()
                .put("path.home", System.getProperty("path.home"))
                .put("index.number_of_shards", 1)
                .put("index.number_of_replica", 0)
                .build();
        // ES 2.1 renders NodeBuilder as useless
        Node node = new MockNode(nodeSettings, LangdetectPlugin.class);
        node.start();
        return node;
    }

    public static void releaseNode(Node node) throws IOException {
        if (node != null) {
            node.close();
            deleteFiles();
        }
    }

    @Before
    public void setupNode() throws IOException {
        node = createNode();
        client = node.client();
    }

    protected Client client(String id) {
        return client;
    }

    @After
    public void cleanupNode() throws IOException {
        releaseNode(node);
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
