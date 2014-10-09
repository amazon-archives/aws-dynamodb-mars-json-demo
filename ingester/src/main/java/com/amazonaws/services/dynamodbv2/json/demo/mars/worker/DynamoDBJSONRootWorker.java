package com.amazonaws.services.dynamodbv2.json.demo.mars.worker;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import com.amazonaws.services.dynamodbv2.json.demo.mars.ExitException;
import com.amazonaws.services.dynamodbv2.json.demo.mars.util.JSONParser;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Retrieves JSON from root URL and parses mission manifest URLs. Provides a map of mission name to mission URL.
 */
public class DynamoDBJSONRootWorker implements Callable<Map<String, String>> {
    /**
     * Logger for the {@link DynamoDBJSONRootWorker}.
     */
    private static final Logger LOGGER = Logger.getLogger(DynamoDBJSONRootWorker.class.getName());
    /**
     * JSON key for mission manifests.
     */
    private static final String IMAGE_RESOURCE_KEY = "image_manifest";

    /**
     * Processes the JSON root for Mars missions into a map of Mars mission names (i.e. MSL, MERA, MERB) to the JSON
     * Image Manifest URL.
     *
     * @param rootJsonUrl
     *            The URL of the JSON root
     * @param connectTimeout
     *            Timeout for retrieving the root JSON
     * @return Map of mission names to mission JSON URL
     * @throws IOException
     *             Invalid URL, invalid JSON, or connection error
     */
    public static Map<String, String> getMissionToManifestMap(final URL rootJsonUrl, final int connectTimeout)
        throws IOException {
        final Map<String, String> map = new HashMap<String, String>();
        // Retrieve JSON
        final JsonNode root = JSONParser.getJSONFromURL(rootJsonUrl, connectTimeout);
        final Iterator<Entry<String, JsonNode>> it = root.fields();
        // Top level is array of mission names
        while (it.hasNext()) {
            final Entry<String, JsonNode> pair = it.next();
            final String mission = pair.getKey();
            if (pair.getKey() == null) {
                // Log error and skip mission
                LOGGER.warning("Null mission name");
                continue;
            }
            final JsonNode missionObject = pair.getValue();
            if (missionObject == null) {
                // Log error and skip mission
                LOGGER.warning("Null mission object for: " + mission);
                continue;
            }
            if (!missionObject.has(IMAGE_RESOURCE_KEY)) {
                // Log error and skip mission
                LOGGER.warning("Missing mission manifest for " + mission + ": " + missionObject.toString());
                continue;
            }
            // Image manifest key has URL
            final String manifestValue = missionObject.get(IMAGE_RESOURCE_KEY).asText();
            map.put(mission, manifestValue);
        }
        return map;
    }

    // state
    /**
     * URL of the root JSON.
     */
    private final String rootURL;
    /**
     * Connection timeout for retrieving the root JSON.
     */
    private final int connectTimeout;

    /**
     * Constructs a {@link DynamoDBJSONRootWorker} that retrieves and parses the root JSON at the provided URL using the
     * specified timeout.
     *
     * @param rootURL
     *            URL of the root JSON
     * @param connectTimeout
     *            Connection timeout for retrieving the root JSON
     */
    public DynamoDBJSONRootWorker(final String rootURL, final int connectTimeout) {
        this.rootURL = rootURL;
        this.connectTimeout = connectTimeout;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> call() throws Exception {
        Map<String, String> topLevelManifests = null;
        try {
            // Get JSON root and process to map
            topLevelManifests = DynamoDBJSONRootWorker.getMissionToManifestMap(new URL(rootURL), connectTimeout);
        } catch (final IOException e) {
            throw new ExitException("Error connecting to " + rootURL, e);
        }
        return topLevelManifests;
    }
}
