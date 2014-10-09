package com.amazonaws.services.dynamodbv2.json.demo.mars.worker;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.json.converter.JacksonConverter;
import com.amazonaws.services.dynamodbv2.json.converter.impl.JacksonConverterImpl;
import com.amazonaws.services.dynamodbv2.json.demo.mars.util.JSONParser;
import com.amazonaws.services.dynamodbv2.json.demo.mars.util.MarsDynamoDBManager;
import com.amazonaws.services.dynamodbv2.json.demo.mars.util.NetworkUtils;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Takes a JSON representation of an image and puts it into the DynamoDB image table. Skips the image and reports a
 * warning if there is an error.
 */
public class DynamoDBImageWorker implements Runnable {
    /**
     * Logger for the {@link DynamoDBImageWorker}.
     */
    private static final Logger LOGGER = Logger.getLogger(DynamoDBImageWorker.class.getName());
    /**
     * Transformer for converting JSON to a DynamoDB item.
     */
    private static final JacksonConverter CONVERTER = new JacksonConverterImpl();

    /**
     * Helper method to process image from URL to thumbnail as base-64-encoded String.
     *
     * @param imageURL
     *            URL to retrieve image from
     * @param expectedETag
     *            The ETag to expect when retrieving the image
     * @param connectTimeout
     *            Timeout for retrieving the image
     * @param thumbnailWidth
     *            Width for resulting thumbnail
     * @param thumbnailHeight
     *            Height for resulting thumbnail
     * @return Base-64-encoded String representation of the image thumbnail
     * @throws IOException
     *             Error retrieving image or corrupt image data.
     */
    protected static String getBase64EncodedImageFromURL(final URL imageURL, final String expectedETag,
        final int connectTimeout, final int thumbnailWidth, final int thumbnailHeight) throws IOException {
        final byte[] image = NetworkUtils.getDataFromURL(imageURL, expectedETag, connectTimeout);
        // Scale image down to thumbnail as byte array
        final byte[] thumbnail = makeThumbnail(image, thumbnailWidth, thumbnailHeight);
        // Base-64-encode the image
        final String encodedThumbnail = JSONParser.MAPPER.convertValue(thumbnail, String.class);
        return encodedThumbnail;
    }

    /**
     * Uses the java.awt library to scale the image.
     *
     * @param byteArray
     *            byte array representation of the original image
     * @param thumbnailWidth
     *            Width for resulting thumbnail
     * @param thumbnailHeight
     *            Height for resulting thumbnail
     * @return byte array representation of the scaled thumbnail
     * @throws IOException
     *             error reading or writing image
     */
    protected static byte[] makeThumbnail(final byte[] byteArray, final int thumbnailWidth, final int thumbnailHeight)
        throws IOException {
        final ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final BufferedImage thumb = new BufferedImage(thumbnailWidth, thumbnailHeight, BufferedImage.TYPE_INT_RGB);
        final BufferedImage im = ImageIO.read(bais);
        final Graphics g = thumb.createGraphics();
        g.drawImage(im, 0, 0, thumbnailWidth, thumbnailHeight, null);
        g.dispose();
        ImageIO.write(thumb, "jpg", baos);
        return baos.toByteArray();
    }

    /**
     * {@link AmazonDynamoDB} used to persist image to DynamoDB.
     */
    private final AmazonDynamoDB dynamoDB;
    /**
     * Raw JSON of the image from the sol.
     */
    private final ObjectNode image;
    /**
     * DynamoDB table for persisting images.
     */
    private final String imageTable;
    /**
     * DynamoDB table to read and write image ETAGs.
     */
    private final String resourceTable;
    /**
     * Timeout for retrieving image data.
     */
    private final int connectTimeout;
    /**
     * Width to process image to for thumbnail.
     */
    private final int thumbnailWidth;
    /**
     * Height to process image to for thumbnail.
     */
    private final int thumbnailHeight;
    /**
     * Flag for tracking resources by ETag in a resource table.
     */
    private final boolean trackResources;
    /**
     * Flag for storing thumbnail data in the image table.
     */
    private final boolean storeThumbnail;

    /**
     * Constructs A {@link DynamoDBImageWorker} to retrieve binary image and persist to DynamoDB.
     *
     * @param dynamoDB
     *            Used to persist image to DynamoDB
     * @param imageTable
     *            DynamoDB table for persisting images
     * @param resourceTable
     *            DynamoDB table to read and write image ETAGs
     * @param image
     *            Raw JSON of the image from the sol
     * @param connectTimeout
     *            Timeout for retrieving image data
     * @param thumbnailWidth
     *            Width to process image to for thumbnail
     * @param thumbnailHeight
     *            Height to process image to for thumbnail
     * @param trackResources
     *            Flag for tracking resources by ETag in a resource table
     * @param storeThumbnail
     *            Flag for storing thumbnail data in the image table
     */
    public DynamoDBImageWorker(final AmazonDynamoDB dynamoDB, final String imageTable, final String resourceTable,
        final ObjectNode image, final int connectTimeout, final int thumbnailWidth, final int thumbnailHeight,
        final boolean trackResources, final boolean storeThumbnail) {
        this.dynamoDB = dynamoDB;
        this.image = image;
        this.imageTable = imageTable;
        this.resourceTable = resourceTable;
        this.connectTimeout = connectTimeout;
        this.thumbnailWidth = thumbnailWidth;
        this.thumbnailHeight = thumbnailHeight;
        this.trackResources = trackResources;
        this.storeThumbnail = storeThumbnail;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {

        try {
            // Retrieve image as thumbnail
            final String imageURL = image.get(MarsDynamoDBManager.IMAGE_TABLE_URL_ATTRIBUTE).asText();
            String expectedETag = null;
            if (storeThumbnail) {
                String data = null;

                while (data == null) {
                    if (trackResources) {
                        final String oldETag = DynamoDBWorkerUtils.getStoredETag(dynamoDB, resourceTable, imageURL);
                        final String newETag = NetworkUtils.getETag(new URL(imageURL));
                        if (newETag.equals(oldETag)) {
                            LOGGER.fine("No change in image: "
                                + image.get(MarsDynamoDBManager.IMAGE_TABLE_HASH_KEY).asText());
                            return;
                        } else {
                            expectedETag = newETag;
                        }
                    }
                    data = getBase64EncodedImageFromURL(new URL(imageURL), expectedETag, connectTimeout,
                        thumbnailWidth, thumbnailHeight);
                }
                // Store thumbnail data in the JSON
                image.put(MarsDynamoDBManager.IMAGE_TABLE_THUMBNAIL_ATTRIBUTE, data);
            }
            // Build the item
            final Map<String, AttributeValue> item = CONVERTER.jsonObjectToMap(image);
            // Put item into DynamoDB
            dynamoDB.putItem(imageTable, item);
            LOGGER.fine("Updated image: " + image.get(MarsDynamoDBManager.IMAGE_TABLE_HASH_KEY).asText());
            if (trackResources) {
                DynamoDBWorkerUtils.updateETag(dynamoDB, resourceTable, imageURL, expectedETag);
            }
        } catch (final Exception e) {
            LOGGER.log(Level.WARNING, "Could not update image: " + image.get(MarsDynamoDBManager.IMAGE_TABLE_HASH_KEY),
                e);
        }

    }
}
