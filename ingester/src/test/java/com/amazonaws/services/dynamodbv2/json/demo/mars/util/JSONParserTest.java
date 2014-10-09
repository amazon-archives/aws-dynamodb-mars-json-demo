package com.amazonaws.services.dynamodbv2.json.demo.mars.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.powermock.api.easymock.PowerMock.replayAll;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.dynamodbv2.json.demo.mars.ImageIngester;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ URL.class, JSONParser.class, NetworkUtils.class })
public class JSONParserTest {
    private static final String ROOT_JSON = "{\"MERA\":{\"image_manifest\":\"https://merpublic.s3.amazonaws.com/oss/mera/images/image_manifest.json\"},\"MSL\":{\"image_manifest\":\"https://msl-raws.s3.amazonaws.com/images/image_manifest.json\"},\"MERB\":{\"image_manifest\":\"https://merpublic.s3.amazonaws.com/oss/merb/images/image_manifest.json\"}}";

    @Test(expected = IOException.class)
    public void testBadURL() throws IOException {
        final URL url = PowerMock.createMock(URL.class);
        url.openConnection();
        PowerMock.expectLastCall().andThrow(new IOException());
        replayAll();
        JSONParser.getJSONFromURL(url, ImageIngester.DEFAULT_CONNECT_TIMEOUT);

    }

    @Test
    public void testgetJSONFromURL() {
        PowerMock.mockStatic(NetworkUtils.class);
        try {
            final URL url = PowerMock.createMock(URL.class);
            NetworkUtils.getDataFromURL(url, null, ImageIngester.DEFAULT_CONNECT_TIMEOUT);
            PowerMock.expectLastCall().andReturn(ROOT_JSON.getBytes());
            replayAll();
            final ObjectMapper mapper = new ObjectMapper();
            final JsonNode json = mapper.readTree(ROOT_JSON);
            final JsonNode result = JSONParser.getJSONFromURL(url, ImageIngester.DEFAULT_CONNECT_TIMEOUT);
            assertEquals(json, result);
        } catch (final IOException e) {
            fail();
        }

    }

    @Test(expected = IOException.class)
    public void testInvalidURL() throws IOException {
        JSONParser.getJSONFromURL(new URL("invalidURL"), ImageIngester.DEFAULT_CONNECT_TIMEOUT);
        fail("URL should be invalid");

    }

    @Test(expected = IOException.class)
    public void testNotJSON() throws IOException {
        PowerMock.mockStatic(NetworkUtils.class);
        final String notJSON = "I am not json";
        final URL url = PowerMock.createMock(URL.class);
        InputStream is = null;
        try {
            is = new ByteArrayInputStream(notJSON.getBytes());
            NetworkUtils.getDataFromURL(url, null, ImageIngester.DEFAULT_CONNECT_TIMEOUT);
            PowerMock.expectLastCall().andReturn(notJSON.getBytes());
            replayAll();
            JSONParser.getJSONFromURL(url, null, ImageIngester.DEFAULT_CONNECT_TIMEOUT);
        } finally {
            if (is != null) {
                is.close();
            }
        }
        fail("Source is invalid json");
    }

    @Test(expected = NullPointerException.class)
    public void testNull() {
        try {
            JSONParser.getJSONFromURL(null, ImageIngester.DEFAULT_CONNECT_TIMEOUT);
        } catch (final IOException e) {
            fail(e.getMessage());
        }
    }

    @Test(expected = NullPointerException.class)
    public void testNullURL() {
        try {
            JSONParser.getJSONFromURL(null, null, ImageIngester.DEFAULT_CONNECT_TIMEOUT);
        } catch (final IOException e) {
            fail("URL is null");
        }
    }

}
