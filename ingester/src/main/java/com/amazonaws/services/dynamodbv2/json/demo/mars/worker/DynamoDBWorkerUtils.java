package com.amazonaws.services.dynamodbv2.json.demo.mars.worker;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.json.demo.mars.util.MarsDynamoDBManager;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;

/**
 * Provides a static method for retrieving an ETag stored in DynamoDB.
 */
public final class DynamoDBWorkerUtils {
    /**
     * DynamoDB item key for ETAG.
     */
    public static final String ETAG_KEY = "ETag";

    /**
     * Retrieves the stored ETag, if one exists, from DynamoDB.
     *
     * @param dynamoDB
     *            DynamoDB client configured with a region and credentials
     * @param table
     *            The resource table name
     * @param resource
     *            The URL String of the resource
     * @return The ETag String of the last copy processed or null if the resource has never been processed
     */
    public static String getStoredETag(final AmazonDynamoDB dynamoDB, final String table, final String resource) {
        String oldETag;
        // Build key to retrieve item
        final Map<String, AttributeValue> resourceKey = new HashMap<String, AttributeValue>();
        resourceKey.put(MarsDynamoDBManager.RESOURCE_TABLE_HASH_KEY, new AttributeValue(resource));
        // Get item
        final GetItemResult result = dynamoDB.getItem(table, resourceKey);
        final Map<String, AttributeValue> item = result.getItem();
        if (item != null && item.containsKey(ETAG_KEY)) {
            // Item was found and contains ETag
            oldETag = item.get(ETAG_KEY).getS();
        } else {
            // Item was not found or did not contain ETag
            oldETag = null;
        }
        return oldETag;
    }

    /**
     * Updates the resource table for the specified resource with the specified ETag.
     *
     * @param dynamoDB
     *            DynamoDB client configured with a region and credentials
     * @param table
     *            The DynamoDB resource table
     * @param resource
     *            The resource URL
     * @param eTag
     *            The new ETag for the resource
     */
    public static void updateETag(final AmazonDynamoDB dynamoDB, final String table, final String resource,
        final String eTag) {
        // Build item
        final Map<String, AttributeValue> newResource = new HashMap<>();
        newResource.put(MarsDynamoDBManager.RESOURCE_TABLE_HASH_KEY, new AttributeValue(resource));
        newResource.put(DynamoDBWorkerUtils.ETAG_KEY, new AttributeValue(eTag));
        dynamoDB.putItem(table, newResource);
    }

    /**
     * Private constructor for static class.
     */
    private DynamoDBWorkerUtils() {

    }
}
