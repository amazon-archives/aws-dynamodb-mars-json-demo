package com.amazonaws.services.dynamodbv2.json.demo.mars.util;

import java.util.logging.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableResult;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.model.TableStatus;

/**
 * Provides static utility methods for managing DynamoDB tables.
 */
public final class DynamoDBManager {
    /**
     * Logger for {@link DynamoDBManager}.
     */
    private static final Logger LOGGER = Logger.getLogger(DynamoDBManager.class.getName());
    /**
     * Amount of time to wait between checking if a table has become ACTIVE.
     */
    private static final long RETRY_DELAY = 20 * 1000; // 20 seconds
    /**
     * Number of times to to check if a table has become ACTIVE before failing.
     */
    private static final int RETRY_COUNT = 3;

    /**
     * Creates DynamoDB table. If the table already exists, it validates the key schema. If the key schemas match, a
     * warning is logged, otherwise an exception is raised.
     *
     * @param dynamoDB
     *            {@link AmazonDynamoDB} used to create the table specified in the request.
     * @param request
     *            Request for creating a table.
     * @return TableDescription of the existing table or newly created table
     */
    public static TableDescription createTable(final AmazonDynamoDB dynamoDB, final CreateTableRequest request) {
        try {
            final DescribeTableResult result = dynamoDB.describeTable(request.getTableName());
            if (!request.getKeySchema().equals(result.getTable().getKeySchema())) {
                throw new IllegalStateException("Table " + request.getTableName()
                    + " already exists and has an invalid schema");
            }
            LOGGER.warning("Table " + request.getTableName() + " already exists");
            return result.getTable();
        } catch (final ResourceNotFoundException e) {
            return dynamoDB.createTable(request).getTableDescription();
        }
    }

    /**
     * Checks if a table exists in DynamoDB.
     *
     * @param dynamoDB
     *            A dynamoDB client configured for the proper region and credentials
     * @param tableName
     *            The table to check for existence
     * @return True if the table exists, false if the table does not exist or an error occurs
     */
    public static boolean doesTableExist(final AmazonDynamoDB dynamoDB, final String tableName) {
        try {
            dynamoDB.describeTable(tableName);
            return true;
        } catch (final ResourceNotFoundException e) {
            return false;
        } catch (final AmazonClientException e) {
            LOGGER.severe(e.getMessage());
            return false;
        }

    }

    /**
     * Gets the table status.
     *
     * @param dynamoDB
     *            The {@link AmazonDynamoDB} to use to get the table status
     * @param tableName
     *            The table to get the status of
     * @return The <a href= "http://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_TableDescriptio n
     *         .html#DDB-Type-TableDescription -TableStatus">table status</a>
     */
    public static TableStatus getTableStatus(final AmazonDynamoDB dynamoDB, final String tableName) {
        return TableStatus.fromValue(dynamoDB.describeTable(tableName).getTable().getTableStatus());
    }

    /**
     * Blocks until the specified table becomes active or {@link #RETRY_COUNT} checks. There is a delay of
     * {@link #RETRY_DELAY} milliseconds between checks.
     *
     * @param dynamoDB
     *            The {@link AmazonDynamoDB} to use to get the table status
     * @param tableName
     *            The table to wait for an ACTIVE status
     */
    public static void waitForTableToBecomeActive(final AmazonDynamoDB dynamoDB, final String tableName) {
        int numTries = 0;
        TableStatus currentState;
        try {
            currentState = getTableStatus(dynamoDB, tableName);
        } catch (final ResourceNotFoundException e) {
            throw new IllegalStateException("Table " + tableName + " does not exist");
        }
        while (numTries < RETRY_COUNT) {
            try {
                Thread.sleep(RETRY_DELAY);
            } catch (final InterruptedException e) {
                LOGGER.severe(e.getMessage());
            }
            currentState = getTableStatus(dynamoDB, tableName);
            numTries++;
            LOGGER.info("Table " + tableName + " is in " + currentState + " state");
            switch (currentState) {
                case ACTIVE:
                    return;
                case DELETING:
                    throw new IllegalStateException("Table " + tableName
                        + " has DELETING status and will never become active");
                case UPDATING:
                    break;
                case CREATING:
                    break;
                default:
                    throw new IllegalStateException("Unknown table status: " + currentState);
            }
        }

        final DescribeTableResult result = dynamoDB.describeTable(tableName);
        throw new IllegalStateException("Table " + tableName + " never went ACTIVE" + result);
    }

    /**
     * Private constructor for a static class.
     */
    private DynamoDBManager() {
    }
}
