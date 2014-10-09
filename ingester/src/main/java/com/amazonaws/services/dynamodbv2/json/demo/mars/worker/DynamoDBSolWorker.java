package com.amazonaws.services.dynamodbv2.json.demo.mars.worker;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.amazonaws.services.dynamodbv2.json.demo.mars.util.JSONParser;
import com.amazonaws.services.dynamodbv2.json.demo.mars.util.MarsDynamoDBManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.ISO8601Utils;

/**
 * Retrieves sol JSON and processes it into an ArrayNode containing JSON representations of images ready to be inserted
 * into DynamoDB. In addition to the JSON provided by NASA, the worker adds the key field
 * {@value com.amazonaws.services.dynamodbv2.json.demo.mars.util.MarsDynamoDBManager#IMAGE_TABLE_TIME_GSI_HASH_KEY} and
 * promotes creation time stamp to a top level attribute
 * {@value com.amazonaws.services.dynamodbv2.json.demo.mars.util.MarsDynamoDBManager#IMAGE_TABLE_TIME_GSI_RANGE_KEY} .
 */
public class DynamoDBSolWorker implements Callable<ArrayNode> {
    /**
     * Logger for DynamoDBSolWorker.
     */
    private static final Logger LOGGER = Logger.getLogger(DynamoDBSolWorker.class.getName());
    /**
     * Supported version values for Sol JSON. Used to fail gracefully if NASA changes data format.
     */
    private static final List<String> SUPPORTED_VERSIONS = Arrays.asList("mer-images-1.0", "msl-images-2.0");
    /**
     * JSON key for sol number.
     */
    private static final String SOL_KEY = "sol";
    /**
     * JSON key for version.
     */
    private static final String VERSION_KEY = "type";
    /**
     * JSON key for mission.
     */
    private static final String MISSION_KEY = "mission";
    /**
     * Instrument name regex pattern.
     */
    private static final Pattern INSTRUMENT_PATTERN = Pattern.compile("([a-zA-Z0-9_]+)_images");
    /**
     * JSON key for image array.
     */
    private static final String IMAGE_ARRAY = "images";
    /**
     * JSON key for array id.
     */
    private static final String IMAGE_ARRAY_ID = "id";
    /**
     * JSON key for image id.
     */
    private static final String IMAGE_ID_KEY = "imageid";
    /**
     * JSON key for URL.
     */
    private static final String IMAGE_URL_KEY = "url";
    /**
     * JSON key for time.
     */
    private static final String IMAGE_TIME_KEY = "time";
    /**
     * JSON key for creation time.
     */
    private static final String CREATION_TIME_STAMP_KEY = "creation_timestamp_utc";
    /**
     * Required image fields to check for existence.
     */
    private static final List<String> REQUIRED_IMAGE_FIELDS = Arrays
        .asList(IMAGE_ID_KEY, IMAGE_URL_KEY, IMAGE_TIME_KEY);
    /**
     * Blacklist of instruments that don't provide the fields necessary for this application. (Saves time, resources,
     * and less warnings).
     */
    private static final List<String> DO_NOT_PROCESS_INSTRUMENTS = Arrays.asList("course_plot");
    /**
     * Singleton empty array node.
     */
    private static final ArrayNode EMPTY_ARRAY_NODE = new ArrayNode(JsonNodeFactory.instance);

    /**
     * Gets ArrayNode containing JSON representations of images if the manifest ETag does not match the provided old
     * ETag for the provided URL.
     *
     * @param sol
     *            JSON representation of a sol
     * @return ArrayNode containing JSON representation of images
     * @throws IOException
     *             Invalid URL, invalid JSON, or connection error
     */
    public static ArrayNode getImages(final JsonNode sol) throws IOException {
        final ArrayNode images = new ArrayNode(JsonNodeFactory.instance);
        if (sol != null) {
            // Check version
            if (!sol.has(VERSION_KEY) || !SUPPORTED_VERSIONS.contains(sol.get(VERSION_KEY).asText())) {
                throw new IllegalArgumentException("Unsupported sol type: " + sol.get(VERSION_KEY));
            }
            // Check for mission name
            if (!sol.has(MISSION_KEY)) {
                throw new IllegalArgumentException("Mission name missing");
            }
            // Check for sol number
            if (!sol.has(SOL_KEY)) {
                throw new IllegalArgumentException("Sol number missing");
            }
            final String mission = sol.get(MISSION_KEY).asText();
            final Integer solNum = sol.get(SOL_KEY).asInt();
            final Iterator<Map.Entry<String, JsonNode>> solIt = sol.fields();
            // Process keys that point to instrument image arrays
            // Rather than hard-coding in the keys for the instruments, use
            // pattern matching to find instrument keys
            while (solIt.hasNext()) {
                final Map.Entry<String, JsonNode> entry = solIt.next();
                final String key = entry.getKey();
                final Matcher matcher = INSTRUMENT_PATTERN.matcher(key);
                if (matcher.matches()) {
                    if (matcher.groupCount() == 1) {
                        final String instrument = matcher.group(matcher.groupCount());
                        // Blacklisted instruments do not provide the fields required
                        if (!DO_NOT_PROCESS_INSTRUMENTS.contains(instrument)) {
                            // Process instrument to get images
                            images.addAll(parseInstrumentImages("Sol " + solNum + "->" + key, mission, instrument,
                                entry.getValue()));
                        }
                    } else {
                        LOGGER.warning("Unexpected instrument name: Sol" + solNum + "->" + key);
                    }
                }
            }
        }
        return images;
    }

    /**
     * <p>
     * Parses the ISO-8601 date from the image JSON.
     * </p>
     * <p>
     * Handles the bug in the NASA JSON where not all timestamps comply with ISO-8601 (some are missing the 'Z' for UTC
     * time at the end of the timestamp).
     * </p>
     * Uses Jackson ISO8601Utils to convert the timestamp.
     *
     * @param image
     *            JSON representation of the image
     * @return Java Date object containing the creation time stamp
     */
    protected static Date getTimestamp(final JsonNode image) {
        Date date = null;
        String iso8601 = image.get(IMAGE_TIME_KEY).get(CREATION_TIME_STAMP_KEY).asText();
        try {
            date = ISO8601Utils.parse(iso8601);
        } catch (final IllegalArgumentException e) {
            // Don't like this, but not all times have the Z at the end for
            // ISO-8601
            if (iso8601.charAt(iso8601.length() - 1) != 'Z') {
                iso8601 = iso8601 + "Z";
                date = ISO8601Utils.parse(iso8601);
            } else {
                throw e;
            }
        }
        return date;
    }

    /**
     * Processes an instrument image list into a collection of JSONNodes representing individual images.
     *
     * @param key
     *            Key for the image array in the sol JSON
     * @param mission
     *            Mission name
     * @param instrument
     *            Instrument name
     * @param imageL
     *            JSON representation of the image list to process
     * @return Collection of JSONNodes representing individual images
     */
    protected static Collection<JsonNode> parseInstrumentImages(final String key, final String mission,
        final String instrument, final JsonNode imageL) {
        final List<JsonNode> images = new ArrayList<>();
        // Validate image list
        if (!imageL.isArray()) {
            LOGGER.warning("Unexpected data for " + key);
            return images;
        }
        // Conversion has been checked above
        final ArrayNode imageList = (ArrayNode) imageL;
        // Image list contains image sets which contain the individual images
        for (final JsonNode imageSetO : imageList) {
            // Validate image set
            if (!imageSetO.isObject() || !imageSetO.has(IMAGE_ARRAY_ID) || !imageSetO.has(IMAGE_ARRAY)) {
                LOGGER.warning("Missing image array for " + mission + "->" + key);
                continue;
            }
            // Conversion has been checked above
            final ObjectNode imageSet = (ObjectNode) imageSetO;
            // Validate array of images contained in the image set
            if (!imageSet.get(IMAGE_ARRAY).isArray()) {
                LOGGER.warning("Missing image array for " + mission + "->" + key + "->"
                    + imageSetO.get(IMAGE_ARRAY_ID).asText());
                continue;
            }
            // Conversion has been checked above
            final ArrayNode imageSetImages = (ArrayNode) imageSetO.get(IMAGE_ARRAY);
            // Process individual images
            for (final JsonNode imageO : imageSetImages) {
                // Validate image object
                if (!imageO.isObject()) {
                    LOGGER.warning("Unexpected entry in array for " + mission + "->" + key + "->"
                        + imageSetO.get(IMAGE_ARRAY_ID).asText());
                    continue;
                }
                // Check image for required fields
                boolean skip = false;
                for (final String requiredField : REQUIRED_IMAGE_FIELDS) {
                    if (!imageO.has(requiredField)) {
                        LOGGER.warning("Required field, " + requiredField + ", is missing for " + mission + "->" + key
                            + "->" + imageSetO.get(IMAGE_ARRAY_ID).asText());
                        skip = true;
                    }
                }
                if (skip) {
                    continue;
                }
                if (!imageO.get(IMAGE_TIME_KEY).has(CREATION_TIME_STAMP_KEY)) {
                    LOGGER.warning("Required field, " + IMAGE_TIME_KEY + "->" + CREATION_TIME_STAMP_KEY
                        + ", is missing for " + mission + "->" + key + "->" + imageSetO.get(IMAGE_ARRAY_ID).asText()
                        + "->" + imageO.get(IMAGE_ID_KEY));
                    continue;
                }
                final Date timestamp = getTimestamp(imageO);
                // Injecting our own data model by changing attribute name or adding attributes for
                // imageid, time object, mission, instrument, Mission+Instrument, TimeStamp, data
                final ObjectNode image = (ObjectNode) imageO;
                if (!IMAGE_ID_KEY.equals(MarsDynamoDBManager.IMAGE_TABLE_HASH_KEY)) {
                    final JsonNode imageid = image.remove(IMAGE_ID_KEY);
                    image.put(MarsDynamoDBManager.IMAGE_TABLE_HASH_KEY, imageid);
                }
                // Image Object
                if (!IMAGE_TIME_KEY.equals(MarsDynamoDBManager.IMAGE_TABLE_TIME_ATTRIBUTE)) {
                    final JsonNode time = image.remove(IMAGE_TIME_KEY);
                    image.put(MarsDynamoDBManager.IMAGE_TABLE_TIME_ATTRIBUTE, time);
                }
                // Mission
                if (!MISSION_KEY.equals(MarsDynamoDBManager.IMAGE_TABLE_MISSION_ATTRIBUTE)) {
                    image.remove(MISSION_KEY);
                    image.put(MarsDynamoDBManager.IMAGE_TABLE_MISSION_ATTRIBUTE, mission);
                }
                // Instrument
                image.put(MarsDynamoDBManager.IMAGE_TABLE_INSTRUMENT_ATTRIBUTE, instrument);
                // Mission+Instrument
                image.put(MarsDynamoDBManager.IMAGE_TABLE_GSI_HASH_KEY, mission + "+" + instrument);
                // TimeStamp
                image.put(MarsDynamoDBManager.IMAGE_TABLE_TIME_GSI_RANGE_KEY, timestamp.getTime());

                // Sanitize URL (added because some URLs contain '#' character)
                final String url = sanitizeURL(image.remove(IMAGE_URL_KEY).asText());
                // Move the URL to the correct attribute
                image.put(MarsDynamoDBManager.IMAGE_TABLE_URL_ATTRIBUTE, url);

                images.add(image);
            }

        }
        return images;
    }

    /**
     * Sanitizes the URLs (some NASA URLs contain non-URL-encoded Strings).
     *
     * @param url
     *            The image JSON to sanitize
     * @return Sanitized URL String
     */
    private static String sanitizeURL(final String url) {
        return url.replaceAll("#", "%23");
    }

    // /**
    // * AWSDynamoDB to use to read and write ETag.
    // */
    // private final AmazonDynamoDB dynamoDB;
    // /**
    // * Resource table for reading and storing ETag.
    // */
    // private final String resourceTable;
    /**
     * URL of the sol to process.
     */
    private final String url;
    /**
     * Connection timeout for retreiving sol.
     */
    private final int connectTimeout;

    /**
     * Constructs a sol worker to process the sol with the provided url and connection timeout.
     *
     * @param url
     *            Location of sol to process
     * @param connectTimeout
     *            Timeout for connection
     */
    public DynamoDBSolWorker(/* final AmazonDynamoDB dynamoDB, final String resourceTable */final String url,
        final int connectTimeout) {
        // this.dynamoDB = dynamoDB;
        // this.resourceTable = resourceTable;
        this.url = url;
        this.connectTimeout = connectTimeout;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArrayNode call() throws Exception {
        try {
            JsonNode sol = null;
            sol = JSONParser.getJSONFromURL(new URL(url), connectTimeout);
            final ArrayNode images = getImages(sol);
            LOGGER.finer("Processed sol " + url + " (contains " + images.size() + " new images): ");
            return images;
        } catch (final Exception e) {
            LOGGER.log(Level.WARNING, "Skipping sol: " + url, e);
            return EMPTY_ARRAY_NODE;
        }
    }

}
