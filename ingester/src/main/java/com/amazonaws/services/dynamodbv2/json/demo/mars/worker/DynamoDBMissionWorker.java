package com.amazonaws.services.dynamodbv2.json.demo.mars.worker;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.amazonaws.services.dynamodbv2.json.demo.mars.util.JSONParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * Retrieves a mission manifest and processes it into a map of sol number to sol URL.
 */
public class DynamoDBMissionWorker implements Callable<Map<Integer, String>> {
    /**
     * Logger for DynamoDBMissionWorker.
     */
    private static final Logger LOGGER = Logger.getLogger(DynamoDBMissionWorker.class.getName());
    // Parsing constants
    /**
     * JSON key for resource type.
     */
    private static final String RESOURCE_TYPE_KEY = "type";
    /**
     * Supported resource types. Used to fail gracefully if NASA changes data model.
     */
    private static final List<String> SUPPORTED_TYPES = Arrays.asList("mer-images-manifest-1.0",
        "msl-images-manifest-2.0");
    /**
     * JSON key for sols array.
     */
    private static final String SOLS_LIST_KEY = "sols";
    /**
     * JSON key for sol number.
     */
    private static final String SOL_ID_KEY = "sol";
    /**
     * JSON key for sol URL.
     */
    private static final String SOL_URL_KEY = "url";

    /**
     * Retrieves and parses a mission manifest to a map of sol numbers to sol URLs.
     *
     * @param url
     *            Location of the mission manifest
     * @param connectTimeout
     *            Timeout for retrieving the mission manifest
     * @return Map of sol number to sol URL contained in the mission manifest
     * @throws IOException
     *             Invalid URL, invalid JSON data, or connection error
     */
    public static Map<Integer, String> getSolJSON(final URL url, final int connectTimeout) throws IOException {
        final Map<Integer, String> map = new HashMap<Integer, String>();
        // Retrieve the JSON data
        final JsonNode manifest = JSONParser.getJSONFromURL(url, connectTimeout);
        // Validate the JSON data version
        if (!manifest.has(RESOURCE_TYPE_KEY) || !SUPPORTED_TYPES.contains(manifest.get(RESOURCE_TYPE_KEY).asText())) {
            throw new IllegalArgumentException("Manifest version verification failed");
        }
        // Validate that the JSON data contains a sol list
        if (!manifest.has(SOLS_LIST_KEY)) {
            throw new IllegalArgumentException("Manifest does not contain a sol list");
        }
        final ArrayNode sols = (ArrayNode) manifest.get(SOLS_LIST_KEY);
        // Process each sol in the sol list
        for (int i = 0; i < sols.size(); i++) {
            final JsonNode sol = sols.path(i);
            if (sol.has(SOL_ID_KEY) && sol.has(SOL_URL_KEY)) {
                final Integer solID = sol.get(SOL_ID_KEY).asInt();
                final String solURL = sol.get(SOL_URL_KEY).asText();
                if (solID != null && solURL != null) {
                    // Add valid sol to the map
                    map.put(solID, solURL);
                } else {
                    LOGGER.warning("Sol contains unexpected values: " + sol);
                }
            } else {
                LOGGER.warning("Sol missing required keys: ");
            }
        }
        return map;
    }

    // State
    /**
     * URL of the mission manifest.
     */
    private final String manifestURL;
    /**
     * Timeout for retreiving mission manifest.
     */
    private final int connectTimeout;

    /**
     * Constructs new {@link DynamoDBMissionWorker} to retrieve manifest at the specified URL. Will use specified
     * timeout when connecting.
     *
     * @param manifestURL
     *            URL of the mission manifest to retrieve
     * @param connectTimeout
     *            Amount of time in milliseconds to timeout while retrieving manifest
     */
    public DynamoDBMissionWorker(final String manifestURL, final int connectTimeout) {
        this.manifestURL = manifestURL;
        this.connectTimeout = connectTimeout;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Integer, String> call() throws Exception {
        try {
            // Always check manifest - sol could have updated
            final Map<Integer, String> sols = getSolJSON(new URL(manifestURL), connectTimeout);
            LOGGER.info("Processed Manifest (" + sols.size() + " sols): " + manifestURL);
            return sols;

        } catch (final Exception e) {
            LOGGER.log(Level.WARNING, "Skipping manifest: " + manifestURL, e);
            return Collections.emptyMap();
        }
    }
}
