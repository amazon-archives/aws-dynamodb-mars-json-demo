package com.amazonaws.services.dynamodbv2.json.demo.mars.util;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ AmazonDynamoDB.class, DynamoDBManager.class })
public class MarsDynamoDBManagerTest {

    private static final ProvisionedThroughput PROVISIONED_THROUGHPUT = new ProvisionedThroughput();
    private static final String TABLE_NAME = "table";

    @Test
    public void testCreateImageTable() {
        final AmazonDynamoDB dynamoDB = PowerMock.createMock(AmazonDynamoDB.class);
        PowerMock.mockStatic(DynamoDBManager.class);
        final CreateTableRequest request = new CreateTableRequest();
        request.setAttributeDefinitions(MarsDynamoDBManager.IMAGE_TABLE_ATTRIBUTE_DEFINITIONS);
        request.setKeySchema(MarsDynamoDBManager.IMAGE_TABLE_KEY_SCHEMA);
        final GlobalSecondaryIndex timeGSI = new GlobalSecondaryIndex();
        timeGSI.setIndexName(MarsDynamoDBManager.IMAGE_TABLE_TIME_GSI_NAME);
        timeGSI.setKeySchema(Arrays.asList(MarsDynamoDBManager.IMAGE_TABLE_TIME_GSI_HASH_KSE,
            MarsDynamoDBManager.IMAGE_TABLE_TIME_GSI_RANGE_KSE));
        timeGSI.setProjection(MarsDynamoDBManager.IMAGE_TABLE_TIME_GSI_PROJECTION);
        timeGSI.setProvisionedThroughput(PROVISIONED_THROUGHPUT);
        final GlobalSecondaryIndex voteGSI = new GlobalSecondaryIndex();
        voteGSI.setIndexName(MarsDynamoDBManager.IMAGE_TABLE_VOTE_GSI_NAME);
        voteGSI.setKeySchema(Arrays.asList(MarsDynamoDBManager.IMAGE_TABLE_VOTE_GSI_HASH_KSE,
            MarsDynamoDBManager.IMAGE_TABLE_VOTE_GSI_RANGE_KSE));
        voteGSI.setProjection(MarsDynamoDBManager.IMAGE_TABLE_VOTE_GSI_PROJECTION);
        voteGSI.setProvisionedThroughput(PROVISIONED_THROUGHPUT);
        request.setGlobalSecondaryIndexes(Arrays.asList(timeGSI, voteGSI));
        request.setProvisionedThroughput(PROVISIONED_THROUGHPUT);
        request.setTableName(TABLE_NAME);

        DynamoDBManager.createTable(dynamoDB, request);
        PowerMock.expectLastCall().andReturn(null);
        PowerMock.replayAll();
        MarsDynamoDBManager.createImageTable(dynamoDB, TABLE_NAME, PROVISIONED_THROUGHPUT, PROVISIONED_THROUGHPUT,
            PROVISIONED_THROUGHPUT);
        PowerMock.verifyAll();

    }

    @Test
    public void testCreateResourceTable() {
        final AmazonDynamoDB dynamoDB = PowerMock.createMock(AmazonDynamoDB.class);
        PowerMock.mockStatic(DynamoDBManager.class);
        final CreateTableRequest request = new CreateTableRequest();
        request.setAttributeDefinitions(MarsDynamoDBManager.RESOURCE_TABLE_ATTRIBUTE_DEFINITIONS);
        request.setKeySchema(MarsDynamoDBManager.RESOURCE_TABLE_KEY_SCHEMA);
        request.setProvisionedThroughput(PROVISIONED_THROUGHPUT);
        request.setTableName(TABLE_NAME);

        DynamoDBManager.createTable(dynamoDB, request);
        PowerMock.expectLastCall().andReturn(null);
        PowerMock.replayAll();
        MarsDynamoDBManager.createResourceTable(dynamoDB, TABLE_NAME, PROVISIONED_THROUGHPUT);
        PowerMock.verifyAll();
    }

}
