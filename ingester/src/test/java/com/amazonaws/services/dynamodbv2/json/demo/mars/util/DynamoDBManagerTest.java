package com.amazonaws.services.dynamodbv2.json.demo.mars.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.api.easymock.annotation.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.CreateTableResult;
import com.amazonaws.services.dynamodbv2.model.DescribeTableResult;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.model.TableStatus;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ AmazonDynamoDBClient.class, Thread.class, DynamoDBManager.class })
public class DynamoDBManagerTest {

    @Mock
    public AmazonDynamoDB dynamoDB = PowerMock.createMock(AmazonDynamoDB.class);;

    static final String tableName = "table";

    @Before
    public void setUp() throws InterruptedException {
        PowerMock.resetAll();
        PowerMock.mockStatic(Thread.class);
        Thread.sleep(EasyMock.anyLong());
        PowerMock.expectLastCall().andThrow(new InterruptedException()).anyTimes();
    }

    @Test
    public void testCreateTableTableAlreadyExistsCorrectKeySchema() {
        final Collection<AttributeDefinition> ads = Arrays.asList(new AttributeDefinition("Hash", ScalarAttributeType.S));
        final Collection<KeySchemaElement> kses = Arrays.asList(new KeySchemaElement("Hash", KeyType.HASH));
        final TableDescription description = new TableDescription().withAttributeDefinitions(ads).withKeySchema(kses)
            .withTableName(tableName);
        final DescribeTableResult result = new DescribeTableResult().withTable(description);
        EasyMock.expect(dynamoDB.describeTable(tableName)).andReturn(result);
        final CreateTableRequest request = new CreateTableRequest().withAttributeDefinitions(ads).withKeySchema(kses)
            .withTableName(tableName);
        PowerMock.replayAll();
        assertEquals(description, DynamoDBManager.createTable(dynamoDB, request));
        PowerMock.verifyAll();
    }

    @Test(expected = IllegalStateException.class)
    public void testCreateTableTableAlreadyExistsIncorrectKeySchema() {
        final Collection<AttributeDefinition> ads = Arrays.asList(new AttributeDefinition("Hash", ScalarAttributeType.S));
        final Collection<KeySchemaElement> kses = Arrays.asList(new KeySchemaElement("Hash", KeyType.HASH));
        final TableDescription description = new TableDescription().withAttributeDefinitions(ads).withKeySchema(kses)
            .withTableName(tableName);
        final DescribeTableResult result = new DescribeTableResult().withTable(description);
        EasyMock.expect(dynamoDB.describeTable(tableName)).andReturn(result);
        final Collection<AttributeDefinition> ads2 = Arrays.asList(new AttributeDefinition("Hash2", ScalarAttributeType.S));
        final Collection<KeySchemaElement> kses2 = Arrays.asList(new KeySchemaElement("Hash2", KeyType.HASH));
        final CreateTableRequest request = new CreateTableRequest().withAttributeDefinitions(ads2).withKeySchema(kses2)
            .withTableName(tableName);
        PowerMock.replayAll();
        DynamoDBManager.createTable(dynamoDB, request);
    }

    @Test
    public void testCreateTableTableDoesNotExist() {
        final Collection<AttributeDefinition> ads = Arrays.asList(new AttributeDefinition("Hash", ScalarAttributeType.S));
        final Collection<KeySchemaElement> kses = Arrays.asList(new KeySchemaElement("Hash", KeyType.HASH));
        final TableDescription description = new TableDescription().withAttributeDefinitions(ads).withKeySchema(kses)
            .withTableName(tableName);
        final CreateTableResult cTR = new CreateTableResult().withTableDescription(description);
        EasyMock.expect(dynamoDB.describeTable(tableName)).andThrow(new ResourceNotFoundException(null));
        final CreateTableRequest request = new CreateTableRequest().withAttributeDefinitions(ads).withKeySchema(kses)
            .withTableName(tableName);
        EasyMock.expect(dynamoDB.createTable(request)).andReturn(cTR);
        PowerMock.replayAll();
        assertEquals(description, DynamoDBManager.createTable(dynamoDB, request));
        PowerMock.verifyAll();
    }

    @Test
    public void testGetTableStatus() {
        final TableDescription description = new TableDescription();
        final DescribeTableResult result = new DescribeTableResult().withTable(description);
        EasyMock.expect(dynamoDB.describeTable(tableName)).andReturn(result).anyTimes();
        for (final TableStatus status : TableStatus.values()) {
            description.setTableStatus(status);
            PowerMock.replayAll();
            assertEquals(status, DynamoDBManager.getTableStatus(dynamoDB, tableName));
            PowerMock.verifyAll();

        }
    }

    @Test
    public void testTableDoesNotExist() {
        EasyMock.expect(dynamoDB.describeTable(tableName)).andThrow(new ResourceNotFoundException(""));
        PowerMock.replayAll();
        assertFalse(DynamoDBManager.doesTableExist(dynamoDB, tableName));
        PowerMock.verifyAll();
    }

    @Test
    public void testTableExistException() {
        EasyMock.expect(dynamoDB.describeTable(tableName)).andThrow(new AmazonClientException(""));
        PowerMock.replayAll();
        assertFalse(DynamoDBManager.doesTableExist(dynamoDB, tableName));
        PowerMock.verifyAll();
    }

    @Test
    public void testTableExists() {
        final DescribeTableResult result = new DescribeTableResult();
        dynamoDB.describeTable(tableName);
        PowerMock.expectLastCall().andReturn(result);
        PowerMock.replayAll();
        assertTrue(DynamoDBManager.doesTableExist(dynamoDB, tableName));
        PowerMock.verifyAll();
    }

    @Test
    public void testWaitForTableToBecomeActiveAlreadyActive() {
        final TableDescription table = new TableDescription();
        final DescribeTableResult result = new DescribeTableResult().withTable(table);
        table.setTableStatus(TableStatus.ACTIVE);
        dynamoDB.describeTable(tableName);
        PowerMock.expectLastCall().andReturn(result);
        PowerMock.expectLastCall().andReturn(result);
        PowerMock.replayAll();
        DynamoDBManager.waitForTableToBecomeActive(dynamoDB, tableName);
    }

    @Test
    public void testWaitForTableToBecomeActiveCreatingThenActive() {
        // Creating table
        final TableDescription table1 = new TableDescription();
        table1.setTableStatus(TableStatus.CREATING);
        final DescribeTableResult result1 = new DescribeTableResult().withTable(table1);
        // Active table
        final TableDescription table2 = new TableDescription();
        table2.setTableStatus(TableStatus.ACTIVE);
        final DescribeTableResult result2 = new DescribeTableResult().withTable(table2);
        EasyMock.expect(dynamoDB.describeTable(tableName)).andReturn(result1);
        EasyMock.expect(dynamoDB.describeTable(tableName)).andReturn(result2);
        PowerMock.replayAll();
        DynamoDBManager.waitForTableToBecomeActive(dynamoDB, tableName);
    }

    @Test(expected = IllegalStateException.class)
    public void testWaitForTableToBecomeActiveDeleting() {
        final TableDescription table = new TableDescription().withTableStatus(TableStatus.DELETING);
        final DescribeTableResult result = new DescribeTableResult().withTable(table);
        PowerMock.expectLastCall().andReturn(result);
        PowerMock.replayAll();
        DynamoDBManager.waitForTableToBecomeActive(dynamoDB, tableName);
    }

    @Test(expected = IllegalStateException.class)
    public void testWaitForTableToBecomeActiveNeverGoingActive() {
        final TableDescription table = new TableDescription();
        final DescribeTableResult result = new DescribeTableResult().withTable(table);
        table.setTableStatus(TableStatus.CREATING);
        EasyMock.expect(dynamoDB.describeTable(tableName)).andReturn(result).anyTimes();
        PowerMock.replayAll();
        DynamoDBManager.waitForTableToBecomeActive(dynamoDB, tableName);
    }

    @Test(expected = IllegalStateException.class)
    public void testWaitForTableToBecomeActiveTableDoesNotExist() {
        EasyMock.expect(dynamoDB.describeTable(tableName)).andThrow(new ResourceNotFoundException(null));
        PowerMock.replayAll();
        DynamoDBManager.waitForTableToBecomeActive(dynamoDB, tableName);
    }

    @Test
    public void testWaitForTableToBecomeActiveUpdatingThenActive() {
        // Updating table
        final TableDescription table1 = new TableDescription();
        table1.setTableStatus(TableStatus.UPDATING);
        final DescribeTableResult result1 = new DescribeTableResult().withTable(table1);
        // Active table
        final TableDescription table2 = new TableDescription();
        table2.setTableStatus(TableStatus.ACTIVE);
        final DescribeTableResult result2 = new DescribeTableResult().withTable(table2);
        EasyMock.expect(dynamoDB.describeTable(tableName)).andReturn(result1);
        EasyMock.expect(dynamoDB.describeTable(tableName)).andReturn(result2);
        PowerMock.replayAll();
        DynamoDBManager.waitForTableToBecomeActive(dynamoDB, tableName);
    }

}
