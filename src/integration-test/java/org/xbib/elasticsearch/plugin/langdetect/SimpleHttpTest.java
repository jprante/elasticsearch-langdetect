
package org.xbib.elasticsearch.plugin.langdetect;

import org.elasticsearch.action.admin.cluster.node.info.NodesInfoAction;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoRequestBuilder;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.io.Streams;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.junit.Test;
import org.xbib.elasticsearch.NodeTestUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class SimpleHttpTest extends NodeTestUtils {

    @Test
    public void httpPost() throws IOException {
        InetSocketTransportAddress httpAddress = findHttpAddress(client());
        if (httpAddress == null) {
            throw new IllegalArgumentException("no HTTP address found");
        }
        URL base = new URL("http://" + httpAddress.getHost() + ":" + httpAddress.getPort());
        URL url = new URL(base, "_langdetect");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        Streams.copy(new StringReader("Das ist ein Text"), new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8));
        StringWriter response = new StringWriter();
        Streams.copy(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8), response);
        assertEquals("{\"languages\":[{\"language\":\"de\",\"probability\":0.9999967609942226}]}", response.toString());
    }

    @Test
    public void httpPostShortProfile() throws IOException {
        InetSocketTransportAddress httpAddress = findHttpAddress(client());
        if (httpAddress == null) {
            throw new IllegalArgumentException("no HTTP address found");
        }
        URL base = new URL("http://" + httpAddress.getHost() + ":" + httpAddress.getPort());
        URL url = new URL(base, "_langdetect?profile=short-text");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        Streams.copy(new StringReader("Das ist ein Text"), new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8));
        StringWriter response = new StringWriter();
        Streams.copy(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8), response);
        assertEquals("{\"profile\":\"short-text\",\"languages\":[{\"language\":\"de\",\"probability\":0.9999968539079941}]}", response.toString());
    }

    public static InetSocketTransportAddress findHttpAddress(Client client) {
        NodesInfoRequestBuilder nodesInfoRequestBuilder = new NodesInfoRequestBuilder(client, NodesInfoAction.INSTANCE);
        nodesInfoRequestBuilder.setHttp(true).setTransport(false);
        NodesInfoResponse response = nodesInfoRequestBuilder.execute().actionGet();
        Object obj = response.iterator().next().getHttp().getAddress().publishAddress();
        if (obj instanceof InetSocketTransportAddress) {
            return (InetSocketTransportAddress) obj;
        }
        return null;
    }
}