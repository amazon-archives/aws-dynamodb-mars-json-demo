package com.amazonaws.services.dynamodbv2.json.demo.mars.worker;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.json.demo.mars.util.MarsDynamoDBManager;
import com.amazonaws.services.dynamodbv2.json.demo.mars.worker.DynamoDBWorkerUtils;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ AmazonDynamoDB.class })
public class DynamoDBWorkerUtilsTest {

    private static final String table = "table";
    private static final String resource = "resource";
    private static final String eTag = "eTag";

    @Test
    public void testGetStoredETagExists() {
        AmazonDynamoDB dynamoDB = PowerMock.createMock(AmazonDynamoDB.class);
        Map<String, AttributeValue> resourceKey = new HashMap<String, AttributeValue>();
        resourceKey.put(MarsDynamoDBManager.RESOURCE_TABLE_HASH_KEY, new AttributeValue(resource));
        // Get item
        dynamoDB.getItem(table, resourceKey);
        Map<String, AttributeValue> resourceResult = new HashMap<String, AttributeValue>();
        resourceResult.put(MarsDynamoDBManager.RESOURCE_TABLE_HASH_KEY, new AttributeValue(resource));
        resourceResult.put(DynamoDBWorkerUtils.ETAG_KEY, new AttributeValue(eTag));
        GetItemResult result = new GetItemResult().withItem(resourceResult);
        PowerMock.expectLastCall().andReturn(result);
        PowerMock.replayAll();
        String resultETag = DynamoDBWorkerUtils.getStoredETag(dynamoDB, table, resource);
        assertEquals(eTag, resultETag);
        PowerMock.verifyAll();
    }

    @Test
    public void testGetStoredETagDoesNotExist() {
        AmazonDynamoDB dynamoDB = PowerMock.createMock(AmazonDynamoDB.class);
        Map<String, AttributeValue> resourceKey = new HashMap<String, AttributeValue>();
        resourceKey.put(MarsDynamoDBManager.RESOURCE_TABLE_HASH_KEY, new AttributeValue(resource));
        // Get item
        dynamoDB.getItem(table, resourceKey);
        GetItemResult result = new GetItemResult();
        PowerMock.expectLastCall().andReturn(result);
        PowerMock.replayAll();
        String resultETag = DynamoDBWorkerUtils.getStoredETag(dynamoDB, table, resource);
        assertEquals(null, resultETag);
        PowerMock.verifyAll();
    }

    @Test
    public void testUpdateETag() {
        AmazonDynamoDB dynamoDB = PowerMock.createMock(AmazonDynamoDB.class);
        Map<String, AttributeValue> newItem = new HashMap<String, AttributeValue>();
        newItem.put(MarsDynamoDBManager.RESOURCE_TABLE_HASH_KEY, new AttributeValue(resource));
        newItem.put(DynamoDBWorkerUtils.ETAG_KEY, new AttributeValue(eTag));
        dynamoDB.putItem(table, newItem);
        PowerMock.expectLastCall().andReturn(null);
        PowerMock.replayAll();
        DynamoDBWorkerUtils.updateETag(dynamoDB, table, resource, eTag);
        PowerMock.verifyAll();
    }

}
