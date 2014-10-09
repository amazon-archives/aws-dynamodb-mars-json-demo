package com.amazonaws.services.dynamodbv2.json.demo.mars.util;

import java.io.IOException;
import java.net.URL;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Provides static utility methods for retrieving JSON data from a URL.
 */
public final class JSONParser {
    /**
     * {@link ObjectMapper} used to parse data to a Jackson JSON tree representation.
     */
    public static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Retrieves JSON from a URL.
     *
     * @param url
     *            The URL to retrieve JSON data from
     * @param connectTimeout
     *            Timeout for retrieving JSON data
     * @return {@link JsonNode} pointing to the head of the Jackson tree representation of the JSON data
     * @throws IOException
     *             Invalid URL, JSON data, or connection error
     */
    public static JsonNode getJSONFromURL(final URL url, final int connectTimeout) throws IOException {
        return getJSONFromURL(url, null, connectTimeout);
    }

    /**
     * Retrieves JSON from a URL that supports ETag headers if the current ETag is equal to the specified expected
     * value.
     *
     * @param url
     *            The URL to retrieve JSON data from
     * @param expectedETag
     *            Expected value for the ETag field when requesting the resource
     * @param connectTimeout
     *            Timeout for retrieving JSON
     * @return {@link JsonNode} containing the head of the Jackson tree representation of the JSON data and the new ETag
     * @throws IOException
     *             Invalid URL, JSON data, or connection error
     */
    public static JsonNode getJSONFromURL(final URL url, final String expectedETag, final int connectTimeout)
        throws IOException {
        final byte[] data = NetworkUtils.getDataFromURL(url, expectedETag, connectTimeout);
        if (data != null) {
            return MAPPER.readTree(data);
        } else {
            return null;
        }
    }

    /**
     * Private constructor for a static class.
     */
    private JSONParser() {
    }
}
