package org.xbib.elasticsearch.index.mapper.langdetect;

import org.elasticsearch.common.io.Streams;
import org.junit.Ignore;
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

/**
 *
 */
@Ignore
public class SimpleHttpTest extends NodeTestUtils {

    @Test
    public void httpPost() throws IOException {
        startCluster();
        try {
            String httpAddress = findHttpAddress(client());
            if (httpAddress == null) {
                throw new IllegalArgumentException("no HTTP address found");
            }
            URL base = new URL(httpAddress);
            URL url = new URL(base, "_langdetect");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            Streams.copy(new StringReader("Das ist ein Text"), new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8));
            StringWriter response = new StringWriter();
            Streams.copy(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8), response);
            assertEquals("{\"languages\":[{\"language\":\"de\",\"probability\":0.9999967609942226}]}", response.toString());
        } finally {
            stopCluster();
        }
    }

    @Test
    public void httpPostShortProfile() throws IOException {
        startCluster();
        try {
            String httpAddress = findHttpAddress(client());
            if (httpAddress == null) {
                throw new IllegalArgumentException("no HTTP address found");
            }
            URL base = new URL(httpAddress);
            URL url = new URL(base, "_langdetect?profile=short-text");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            Streams.copy(new StringReader("Das ist ein Text"), new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8));
            StringWriter response = new StringWriter();
            Streams.copy(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8), response);
            assertEquals("{\"profile\":\"short-text\",\"languages\":[{\"language\":\"de\",\"probability\":0.9999968539079941}]}", response.toString());
        } finally {
            stopCluster();
        }
    }
}
