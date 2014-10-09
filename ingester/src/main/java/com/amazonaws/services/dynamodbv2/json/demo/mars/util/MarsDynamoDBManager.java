package com.amazonaws.services.dynamodbv2.json.demo.mars.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Logger;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProjectionType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.TableDescription;

/**
 * Contains the schema constants for the image ingester tables. Provides static utility methods to create the tables.
 */
public final class MarsDynamoDBManager {
    /**
     * Logger for {@link MarsDynamoDBManager}.
     */
    private static final Logger LOGGER = Logger.getLogger(MarsDynamoDBManager.class.getName());
    /*
     * RESOURCE TABLE SCHEMA
     */
    /**
     * Resource table attribute definitions.
     */
    public static final Collection<AttributeDefinition> RESOURCE_TABLE_ATTRIBUTE_DEFINITIONS;
    /**
     * Resource table hash key.
     */
    public static final String RESOURCE_TABLE_HASH_KEY = "resource";
    static {
        RESOURCE_TABLE_ATTRIBUTE_DEFINITIONS = Arrays.asList(new AttributeDefinition(RESOURCE_TABLE_HASH_KEY,
            ScalarAttributeType.S));
    }
    /**
     * Resource table key schema.
     */
    protected static final Collection<KeySchemaElement> RESOURCE_TABLE_KEY_SCHEMA;
    static {
        RESOURCE_TABLE_KEY_SCHEMA = Arrays.asList(new KeySchemaElement(RESOURCE_TABLE_HASH_KEY, KeyType.HASH));
    }

    /*
     * IMAGE TABLE SCHEMA
     */
    /**
     * Image table hash key.
     */
    public static final String IMAGE_TABLE_HASH_KEY = "imageid";
    /**
     * Image table global secondary index hash key.
     */
    public static final String IMAGE_TABLE_GSI_HASH_KEY = "Mission+InstrumentID";
    /**
     * Image table time global secondary index range key.
     */
    public static final String IMAGE_TABLE_TIME_GSI_RANGE_KEY = "TimeStamp";
    /**
     * Image table range global secondary index range key.
     */
    public static final String IMAGE_TABLE_VOTE_GSI_RANGE_KEY = "votes";
    /**
     * Non-key attribute for image URL.
     */
    public static final String IMAGE_TABLE_URL_ATTRIBUTE = "url";
    /**
     * Non-key attribute for image thumbnail.
     */
    public static final String IMAGE_TABLE_THUMBNAIL_ATTRIBUTE = "data";
    /**
     * Non-key attribute for image time object.
     */
    public static final String IMAGE_TABLE_TIME_ATTRIBUTE = "time";
    /**
     * Non-key attribute for image mission.
     */
    public static final String IMAGE_TABLE_MISSION_ATTRIBUTE = "mission";
    /**
     * Non-key attribute for image instrument.
     */
    public static final String IMAGE_TABLE_INSTRUMENT_ATTRIBUTE = "instrument";
    /**
     * Image table attribute definitions.
     */
    public static final Collection<AttributeDefinition> IMAGE_TABLE_ATTRIBUTE_DEFINITIONS;
    static {
        IMAGE_TABLE_ATTRIBUTE_DEFINITIONS = Arrays.asList(new AttributeDefinition(IMAGE_TABLE_HASH_KEY,
            ScalarAttributeType.S), new AttributeDefinition(IMAGE_TABLE_GSI_HASH_KEY, ScalarAttributeType.S),
            new AttributeDefinition(IMAGE_TABLE_TIME_GSI_RANGE_KEY, ScalarAttributeType.N), new AttributeDefinition(
                IMAGE_TABLE_VOTE_GSI_RANGE_KEY, ScalarAttributeType.N));
    }
    /**
     * Image table key schema.
     */
    public static final Collection<KeySchemaElement> IMAGE_TABLE_KEY_SCHEMA;
    static {
        IMAGE_TABLE_KEY_SCHEMA = Arrays.asList(new KeySchemaElement(IMAGE_TABLE_HASH_KEY, KeyType.HASH));
    }

    /*
     * IMAGE TABLE TIME GSI
     */
    /**
     * Image table time global secondary index name.
     */
    public static final String IMAGE_TABLE_TIME_GSI_NAME = "date-gsi";
    /**
     * Non-key projected attributes for the vote global secondary index.
     */
    public static final Collection<String> IMAGE_TABLE_TIME_GSI_NON_KEY_PROJECTED_ATTRIBUTES = Arrays.asList(
        IMAGE_TABLE_TIME_ATTRIBUTE, IMAGE_TABLE_URL_ATTRIBUTE, IMAGE_TABLE_MISSION_ATTRIBUTE,
        IMAGE_TABLE_INSTRUMENT_ATTRIBUTE, IMAGE_TABLE_VOTE_GSI_RANGE_KEY);
    /**
     * Image table time global secondary index projection.
     */
    public static final Projection IMAGE_TABLE_TIME_GSI_PROJECTION = new Projection().withProjectionType(
        ProjectionType.INCLUDE).withNonKeyAttributes(IMAGE_TABLE_TIME_GSI_NON_KEY_PROJECTED_ATTRIBUTES);
    /**
     * Image table time global secondary index hash key schema element.
     */
    public static final KeySchemaElement IMAGE_TABLE_TIME_GSI_HASH_KSE = new KeySchemaElement(IMAGE_TABLE_GSI_HASH_KEY,
        KeyType.HASH);
    /**
     * Image table time global secondary index range key schema element.
     */
    public static final KeySchemaElement IMAGE_TABLE_TIME_GSI_RANGE_KSE = new KeySchemaElement(
        IMAGE_TABLE_TIME_GSI_RANGE_KEY, KeyType.RANGE);
    /*
     * IMAGE TABLE VOTE GSI
     */
    /**
     * Image table vote global secondary index name.
     */
    public static final String IMAGE_TABLE_VOTE_GSI_NAME = "vote-gsi";
    /**
     * Non-key projected attributes for the vote global secondary index.
     */
    public static final Collection<String> IMAGE_TABLE_VOTE_GSI_NON_KEY_PROJECTED_ATTRIBUTES = Arrays.asList(
        IMAGE_TABLE_TIME_ATTRIBUTE, IMAGE_TABLE_URL_ATTRIBUTE, IMAGE_TABLE_MISSION_ATTRIBUTE,
        IMAGE_TABLE_INSTRUMENT_ATTRIBUTE);
    /**
     * Image table vote global secondary index projection.
     */
    public static final Projection IMAGE_TABLE_VOTE_GSI_PROJECTION = new Projection().withProjectionType(
        ProjectionType.INCLUDE).withNonKeyAttributes(IMAGE_TABLE_VOTE_GSI_NON_KEY_PROJECTED_ATTRIBUTES);
    /**
     * Image table vote global secondary index hash key schema element.
     */
    public static final KeySchemaElement IMAGE_TABLE_VOTE_GSI_HASH_KSE = new KeySchemaElement(IMAGE_TABLE_GSI_HASH_KEY,
        KeyType.HASH);
    /**
     * Image table vote global secondary index range key schema element.
     */
    public static final KeySchemaElement IMAGE_TABLE_VOTE_GSI_RANGE_KSE = new KeySchemaElement(
        IMAGE_TABLE_VOTE_GSI_RANGE_KEY, KeyType.RANGE);

    /**
     * Creates the table that stores images.
     *
     * @param dynamoDB
     *            {@link AmazonDynamoDB} used to create the image table
     * @param tableName
     *            name of the table to create
     * @param tableProvisionedThroughput
     *            initial provisioned throughput for the table
     * @param timeGSIProvisionedThroughput
     *            initial provisioned throughput for the time-based global secondary index
     * @param voteGSIProvisionedThroughput
     *            initial provisioned throughput for the vote-based global secondary index
     * @see <a href="http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/GSI.html">Global Secondary
     *      Indexes</a> ======= initial provisioned throughput for the time-based global secondary index
     * @see <a
     *      href="http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/ProvisionedThroughputIntro.html">Provisioned
     *      Throughput in Amazon DynamoDB</a>
     */
    public static void createImageTable(final AmazonDynamoDB dynamoDB, final String tableName,
        final ProvisionedThroughput tableProvisionedThroughput,
        final ProvisionedThroughput timeGSIProvisionedThroughput,
        final ProvisionedThroughput voteGSIProvisionedThroughput) {
        // Set up time GSI
        final GlobalSecondaryIndex timeGSI = new GlobalSecondaryIndex();
        timeGSI.setIndexName(IMAGE_TABLE_TIME_GSI_NAME);
        timeGSI.setKeySchema(Arrays.asList(IMAGE_TABLE_TIME_GSI_HASH_KSE, IMAGE_TABLE_TIME_GSI_RANGE_KSE));
        timeGSI.setProjection(IMAGE_TABLE_TIME_GSI_PROJECTION);
        timeGSI.setProvisionedThroughput(timeGSIProvisionedThroughput);
        // Set up vote GSI
        final GlobalSecondaryIndex voteGSI = new GlobalSecondaryIndex();
        voteGSI.setIndexName(IMAGE_TABLE_VOTE_GSI_NAME);
        voteGSI.setKeySchema(Arrays.asList(IMAGE_TABLE_VOTE_GSI_HASH_KSE, IMAGE_TABLE_VOTE_GSI_RANGE_KSE));
        voteGSI.setProjection(IMAGE_TABLE_VOTE_GSI_PROJECTION);
        voteGSI.setProvisionedThroughput(voteGSIProvisionedThroughput);
        // Create table
        final CreateTableRequest request = new CreateTableRequest();
        request.setAttributeDefinitions(IMAGE_TABLE_ATTRIBUTE_DEFINITIONS);
        request.setKeySchema(IMAGE_TABLE_KEY_SCHEMA);
        request.setGlobalSecondaryIndexes(Arrays.asList(timeGSI, voteGSI));
        request.setProvisionedThroughput(tableProvisionedThroughput);
        request.setTableName(tableName);
        LOGGER.info("Creating image table: " + request);
        final TableDescription result = DynamoDBManager.createTable(dynamoDB, request);
        LOGGER.info("Image table successfully created: " + result);
    }

    /**
     * Creates the table that stores resources with an ETag.
     *
     * @param dynamoDB
     *            {@link AmazonDynamoDB} used to create DynamoDB table
     * @param tableName
     *            name of the table to create
     * @param provisionedThroughput
     *            initial provisioned throughput for the table
     * @see <a
     *      href="http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/ProvisionedThroughputIntro.html">Provisioned
     *      Througput in Amazon DynamoDB</a>
     */
    public static void createResourceTable(final AmazonDynamoDB dynamoDB, final String tableName,
        final ProvisionedThroughput provisionedThroughput) {
        final CreateTableRequest request = new CreateTableRequest();
        request.setAttributeDefinitions(RESOURCE_TABLE_ATTRIBUTE_DEFINITIONS);
        request.setKeySchema(RESOURCE_TABLE_KEY_SCHEMA);
        request.setProvisionedThroughput(provisionedThroughput);
        request.setTableName(tableName);
        LOGGER.info("Creating resource table: " + request);
        DynamoDBManager.createTable(dynamoDB, request);
        LOGGER.info("Resource table successfully created");
    }

    /**
     * Private constructor for utility class.
     */
    private MarsDynamoDBManager() {

    }
}
