
package org.xbib.elasticsearch.plugin.langdetect;

import com.google.common.base.Charsets;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoAction;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoRequestBuilder;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.io.Streams;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.junit.Assert;
import org.junit.Test;
import org.xbib.elasticsearch.NodeTestUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class SimpleHttpTest extends NodeTestUtils {

    private final static ESLogger logger = ESLoggerFactory.getLogger(SimpleHttpTest.class.getName());

    @Test
    public void httpPost() throws IOException {
        InetSocketTransportAddress httpAddress = findHttpAddress(client("1"));
        if (httpAddress != null) {
            URL base = new URL("http://" + httpAddress.getHost() + ":" + httpAddress.getPort());
            URL url = new URL(base, "_langdetect");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            Streams.copy(new StringReader("Das ist ein Text"), new OutputStreamWriter(connection.getOutputStream(), Charsets.UTF_8));
            StringWriter response = new StringWriter();
            Streams.copy(new InputStreamReader(connection.getInputStream(), Charsets.UTF_8), response);
            logger.info("response = {}", response.toString());
            Assert.assertEquals("{\"profile\":\"/langdetect/\",\"languages\":[{\"language\":\"de\",\"probability\":0.9999967609942226}]}", response.toString());
        } else {
            // fail?
        }
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