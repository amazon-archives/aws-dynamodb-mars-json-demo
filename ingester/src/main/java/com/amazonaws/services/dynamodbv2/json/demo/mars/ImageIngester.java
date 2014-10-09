package com.amazonaws.services.dynamodbv2.json.demo.mars;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.json.demo.mars.util.ConfigParser;
import com.amazonaws.services.dynamodbv2.json.demo.mars.util.DynamoDBManager;
import com.amazonaws.services.dynamodbv2.json.demo.mars.util.MarsDynamoDBManager;
import com.amazonaws.services.dynamodbv2.json.demo.mars.worker.DynamoDBImageWorker;
import com.amazonaws.services.dynamodbv2.json.demo.mars.worker.DynamoDBJSONRootWorker;
import com.amazonaws.services.dynamodbv2.json.demo.mars.worker.DynamoDBMissionWorker;
import com.amazonaws.services.dynamodbv2.json.demo.mars.worker.DynamoDBSolWorker;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * <p>
 * Downloads mars image JSON and ingests it into DynamoDB.
 * </p>
 * <p>
 * Makes use of 2 DynamoDB Tables:
 * <ul>
 * <li>Resource table: stores ETags for manifests and image resources.</li>
 * <li>Image table: stores the images from the Mars rover missions.</li>
 * </ul>
 * </p>
 */
public class ImageIngester implements Runnable {
    /**
     * Logger for the {@link ImageIngester}.
     */
    public static final Logger LOGGER = Logger.getLogger(ImageIngester.class.getName());

    // Configuration constants
    /**
     * Default number of threads for a thread pool.
     */
    public static final int DEFAULT_THREADS = 1;
    /**
     * Properties key for number of threads in the pool for {@link DynamoDBJSONRootWorker}s and
     * {@link DynamoDBMissionWorker}s.
     */
    public static final String CONFIG_NUM_MANIFEST_THREADS = "ingester.manifest.threads";
    /**
     * Properties key for number of threads in the pool for {@link DynamoDBSolWorker}s.
     */
    public static final String CONFIG_NUM_SOL_THREADS = "ingester.sol.threads";
    /**
     * Properties key for number of threads in the pool for {@link DynamoDBImageWorker}s.
     */
    public static final String CONFIG_NUM_IMAGE_THREADS = "ingester.image.threads";
    /**
     * Flag for whether resources should be tracked by ETag in a DynamoDB table.
     */
    public static final String CONFIG_TRACK_RESOURCES = "ingester.track-resources";
    /**
     * Default behavior is to not track resources.
     */
    public static final boolean DEFAULT_TRACK_RESOURCES = false;
    /**
     * Flag for whether to store image thumbnails in the DynamoDB table.
     */
    public static final String CONFIG_STORE_THUMBNAILS = "ingester.store-thumbnails";
    /**
     * Default is to not include thumbnails.
     */
    public static final boolean DEFAULT_STORE_THUMBNAILS = false;
    /**
     * Properties key for the amount of time to wait between checking asynchronous tasks for completion.
     */
    public static final String CONFIG_WAIT_TIME = "ingester.waitTime";
    /**
     * Default amount of time to wait between checking asynchronous tasks for completion.
     */
    public static final long DEFAULT_WAIT_TIME = 20 * 1000; // 20 seconds
    /**
     * Properties key for the connect timeout when retrieving an HTTP resource.
     */
    public static final String CONFIG_CONNECT_TIMEOUT = "ingester.timeout";
    /**
     * Default connect timeout when retrieving an HTTP resource.
     */
    public static final int DEFAULT_CONNECT_TIMEOUT = 1000;
    /**
     * Properties key for image thumbnail width in pixels.
     */
    public static final String CONFIG_THUMBNAIL_WIDTH = "ingester.image.thumbnail.width";
    /**
     * Properties key for image thumbnail height in pixels.
     */
    public static final String CONFIG_THUMBNAIL_HEIGHT = "ingester.image.thumbnail.height";
    /**
     * Default value for image thumbnail width in pixels.
     */
    public static final int DEFAULT_THUMBNAIL_WIDTH = 100;
    /**
     * Default value for image thumbnail height in pixels.
     */
    public static final int DEFAULT_THUMBNAIL_HEIGHT = 100;
    /**
     * Properties key for DynamoDB endpoint.
     */
    public static final String CONFIG_ENDPOINT = "dynamodb.endpoint";
    /**
     * Properties key for the JSON root URL.
     */
    public static final String CONFIG_JSON_ROOT = "JSON.root";
    /**
     * Properties key for the DynamoDB resource table name.
     */
    public static final String CONFIG_RESOURCE_TABLE = "dynamodb.resource";
    /**
     * Properties key for the DynamoDB image table name.
     */
    public static final String CONFIG_IMAGE_TABLE = "dynamodb.image";
    /**
     * Properties key for the resource table create flag.
     */
    public static final String CONFIG_RESOURCE_TABLE_CREATE = "dynamodb.resource.create";
    /**
     * Properties key for the resource table read capacity units.
     */
    public static final String CONFIG_RESOURCE_TABLE_RCU = "dynamodb.resource.readCapacityUnits";
    /**
     * Properties key for the resource table write capacity units.
     */
    public static final String CONFIG_RESOURCE_TABLE_WCU = "dynamodb.resource.writeCapacityUnits";
    /**
     * Properties key for the image table create flag.
     */
    public static final String CONFIG_IMAGE_TABLE_CREATE = "dynamodb.image.create";
    /**
     * Properties key for the image table read capacity units.
     */
    public static final String CONFIG_IMAGE_TABLE_RCU = "dynamodb.image.readCapacityUnits";
    /**
     * Properties key for the image table write capacity units.
     */
    public static final String CONFIG_IMAGE_TABLE_WCU = "dynamodb.image.writeCapacityUnits";
    /**
     * Properties key for the read capacity units of the time global secondary index on the image table.
     */
    public static final String CONFIG_IMAGE_TABLE_TIME_GSI_RCU = "dynamodb.image.globalSecondaryIndex.time.readCapacityUnits";
    /**
     * Properties key for the write capacity units of the time global secondary index on the image table.
     */
    public static final String CONFIG_IMAGE_TABLE_TIME_GSI_WCU = "dynamodb.image.globalSecondaryIndex.time.writeCapacityUnits";
    /**
     * Properties key for the read capacity units of the vote global secondary index on the image table.
     */
    public static final String CONFIG_IMAGE_TABLE_VOTE_GSI_RCU = "dynamodb.image.globalSecondaryIndex.vote.readCapacityUnits";
    /**
     * Properties key for the write capacity units of the vote global secondary index on the image table.
     */
    public static final String CONFIG_IMAGE_TABLE_VOTE_GSI_WCU = "dynamodb.image.globalSecondaryIndex.vote.writeCapacityUnits";
    /**
     * Required String properties in configuration.
     */
    private static final String[] REQUIRED_STRING_CONFIGURATIONS = {CONFIG_ENDPOINT, CONFIG_JSON_ROOT,
        CONFIG_IMAGE_TABLE};
    /**
     * Required Boolean properties in configuration.
     */
    private static final String[] REQUIRED_BOOLEAN_CONFIGURATIONS = {CONFIG_IMAGE_TABLE_CREATE};
    /**
     * Required Integer properties in configuration.
     */
    private static final String[] REQUIRED_INTEGER_CONFIGURATIONS = {};
    /**
     * Required Long properties in configuration.
     */
    private static final String[] REQUIRED_LONG_CONFIGURATIONS = {};

    /**
     * Creates an {@link ImageIngester} with a {@link DefaultAWSCredentialsProviderChain}.
     *
     * @param args
     *            The command line arguments for locating the application configuration.
     */
    public static void main(final String[] args) {
        try {
            new Thread(new ImageIngester(args, new DefaultAWSCredentialsProviderChain())).start();
        } catch (final ExitException e) {
            LOGGER.warning("Exiting: " + e.getMessage());
        } catch (final HelpException e) {
            assert true; // Do nothing except quit
        }
    }

    /**
     * Checks for required DynamoDB tables. If the user specifies, this method will create the tables and block until
     * they have an ACTIVE TableStatus. If after these actions the tables are not setup properly, the program will exit.
     *
     * @param dynamoDB
     *            {@link AmazonDynamoDB} to use to create DynamoDB tables
     * @param config
     *            Configuration containing table creation parameters.
     * @throws ExitException
     *             Error parsing the configuration
     */
    private static void setupTables(final AmazonDynamoDB dynamoDB, final Properties config) throws ExitException {
        boolean eTagTableExists = false;
        boolean imageTableExists = false;
        boolean eTagTableActive = false;
        boolean imageTableActive = false;

        final boolean trackResources = ConfigParser.parseBoolean(config, CONFIG_TRACK_RESOURCES,
            DEFAULT_TRACK_RESOURCES);

        if (trackResources) {
            final String eTagTable = ConfigParser.parseString(config, CONFIG_RESOURCE_TABLE);
            final boolean createETagTable = ConfigParser.parseBoolean(config, CONFIG_RESOURCE_TABLE_CREATE);

            if (DynamoDBManager.doesTableExist(dynamoDB, eTagTable)) {
                LOGGER.info("Resource table " + eTagTable + " exists");
                eTagTableExists = true;
            } else if (createETagTable) {
                try {
                    final long eTagTableReadCapacityUnits = ConfigParser.parseLong(config, CONFIG_RESOURCE_TABLE_RCU);
                    final long eTagTableWriteCapacityUnits = ConfigParser.parseLong(config, CONFIG_RESOURCE_TABLE_WCU);
                    final ProvisionedThroughput eTagTablePT = new ProvisionedThroughput(eTagTableReadCapacityUnits,
                        eTagTableWriteCapacityUnits);
                    MarsDynamoDBManager.createResourceTable(dynamoDB, eTagTable, eTagTablePT);
                    eTagTableExists = true;
                } catch (final Exception e) {
                    LOGGER.severe(e.getMessage());
                }
            } else {
                LOGGER.warning("Resource table " + eTagTable + " does not exist");
            }
        }

        final String imageTable = ConfigParser.parseString(config, CONFIG_IMAGE_TABLE);
        final boolean createImageTable = ConfigParser.parseBoolean(config, CONFIG_IMAGE_TABLE_CREATE);

        if (DynamoDBManager.doesTableExist(dynamoDB, imageTable)) {
            LOGGER.info("Image table " + imageTable + " exists");
            imageTableExists = true;
        } else if (createImageTable) {
            try {
                final long imageTableReadCapacityUnits = ConfigParser.parseLong(config, CONFIG_IMAGE_TABLE_RCU);
                final long imageTableWriteCapacityUnits = ConfigParser.parseLong(config, CONFIG_IMAGE_TABLE_WCU);
                final ProvisionedThroughput imageTablePT = new ProvisionedThroughput(imageTableReadCapacityUnits,
                    imageTableWriteCapacityUnits);
                final long imageTableTimeGSIReadCapacityUnits = ConfigParser.parseLong(config,
                    CONFIG_IMAGE_TABLE_TIME_GSI_RCU);
                final long imageTableTimeGSIWriteCapacityUnits = ConfigParser.parseLong(config,
                    CONFIG_IMAGE_TABLE_TIME_GSI_WCU);
                final ProvisionedThroughput imageTableTimeGSIPT = new ProvisionedThroughput(
                    imageTableTimeGSIReadCapacityUnits, imageTableTimeGSIWriteCapacityUnits);
                final long imageTableVoteGSIReadCapacityUnits = ConfigParser.parseLong(config,
                    CONFIG_IMAGE_TABLE_VOTE_GSI_RCU);
                final long imageTableVoteGSIWriteCapacityUnits = ConfigParser.parseLong(config,
                    CONFIG_IMAGE_TABLE_VOTE_GSI_WCU);
                final ProvisionedThroughput imageTableVoteGSIPT = new ProvisionedThroughput(
                    imageTableVoteGSIReadCapacityUnits, imageTableVoteGSIWriteCapacityUnits);
                MarsDynamoDBManager.createImageTable(dynamoDB, imageTable, imageTablePT, imageTableTimeGSIPT,
                    imageTableVoteGSIPT);
                imageTableExists = true;
            } catch (final Exception e) {
                LOGGER.severe(e.getMessage());
            }
        } else {
            LOGGER.warning("Image table " + imageTable + " does not exist");
        }

        if ((!trackResources || eTagTableExists) && imageTableExists) {
            try {
                if (trackResources) {
                    final String eTagTable = ConfigParser.parseString(config, CONFIG_RESOURCE_TABLE);
                    DynamoDBManager.waitForTableToBecomeActive(dynamoDB, eTagTable);
                    eTagTableActive = true;
                }
                DynamoDBManager.waitForTableToBecomeActive(dynamoDB, imageTable);
                imageTableActive = true;
            } catch (final IllegalStateException e) {
                LOGGER.severe(e.getMessage());
            }
        }

        if ((trackResources && !eTagTableActive) || !imageTableActive) {
            throw new ExitException("Tables are not set up properly");
        }
    }

    // State variables
    /**
     * Configuration for the {@link ImageIngester} application.
     */
    private final Properties config;
    /**
     * DynamoDB table for reading and writing image ETags.
     */
    private final String resourceTable;
    /**
     * DynamoDB table for persisting images.
     */
    private final String imageTable;
    /**
     * {@link AmazonDynamoDB} for accessing Amazon Web Services resources.
     */
    private final AmazonDynamoDB dynamoDB;
    /**
     * Wait time between checks for asynchronous tasks to complete.
     */
    private final long waitTime;
    /**
     * Timeout for retrieving HTTP URL resources.
     */
    private final int connectTimeout;
    /**
     * Thread pool for {@link DynamoDBJSONRootWorker} and {@link DynamoDBMissionWorker}.
     */
    private final ExecutorService manifestPool;
    /**
     * Thread pool for {@link DynamoDBSolWorker}s.
     */
    private ExecutorService solPool;
    /**
     * Thread pool for {@link DynamoDBImageWorker}s.
     */
    private ExecutorService imagePool;

    /**
     * Constructs a {@link ImageIngester} with the specified command line arguments and Amazon Web Services credentials
     * provider.
     *
     * @param args
     *            Command line arguments for retrieving the configuration
     * @param credentialsProvider
     *            Amazon Web Services credentials provider
     * @throws ExitException
     *             Error parsing configuration
     */
    public ImageIngester(final String[] args, final AWSCredentialsProvider credentialsProvider) throws ExitException {
        // Parse command line arguments to locate configuration file
        final ImageIngesterCLI cli = new ImageIngesterCLI(args);
        config = cli.getConfig();
        // Validate the configuration file
        ConfigParser.validateConfig(config, REQUIRED_STRING_CONFIGURATIONS, REQUIRED_BOOLEAN_CONFIGURATIONS,
            REQUIRED_INTEGER_CONFIGURATIONS, REQUIRED_LONG_CONFIGURATIONS);
        // Parse configuration settings
        resourceTable = ConfigParser.parseString(config, CONFIG_RESOURCE_TABLE);
        imageTable = ConfigParser.parseString(config, CONFIG_IMAGE_TABLE);
        waitTime = ConfigParser.parseLong(config, CONFIG_WAIT_TIME, DEFAULT_WAIT_TIME);
        connectTimeout = ConfigParser.parseInteger(config, CONFIG_CONNECT_TIMEOUT, DEFAULT_CONNECT_TIMEOUT);
        final String endpoint = ConfigParser.parseString(config, CONFIG_ENDPOINT);
        final int numManifestThreads = ConfigParser.parseInteger(config, CONFIG_NUM_MANIFEST_THREADS, DEFAULT_THREADS);
        // Setup state
        dynamoDB = new AmazonDynamoDBClient(credentialsProvider);
        dynamoDB.setEndpoint(endpoint);
        manifestPool = Executors.newFixedThreadPool(numManifestThreads);
    }

    /**
     * Waits for all {@link DynamoDBImageWorker} tasks to complete.
     *
     * @param imageFutures
     *            Collection of futures corresponding to {@link DynamoDBImageWorker} tasks
     */
    private void awaitTermination(final Collection<Future<?>> imageFutures) {
        while (!imageFutures.isEmpty()) {
            LOGGER.info(imageFutures.size() + " images left to process");
            try {
                Thread.sleep(waitTime);
            } catch (final InterruptedException e) {
                LOGGER.warning(e.getMessage());
            }
            final Iterator<Future<?>> it = imageFutures.iterator();
            while (it.hasNext()) {
                final Future<?> f = it.next();
                if (f.isDone()) {
                    it.remove();
                }
            }
        }
        LOGGER.info("Ingestion completed.");
    }

    /**
     * <p>
     * Submits a {@link DynamoDBMissionWorker} for each mission. Gets results from mission futures as they become
     * available and submits a new {@link DynamoDBSolWorker} to process each sol in the mission. Returns a future for
     * each sol that contains an {@link ArrayNode} of all the images in the sol
     * </p>
     * <p>
     * If there is an error parsing a mission, a warning is logged and the mission is skipped.
     * </p>
     *
     * @param topLevelManifests
     *            Map of mission to its manifest URL
     *
     * @return futures for each sol that will provide an {@link ArrayNode} of images in the sol
     * @throws ExitException
     *             Error parsing configuration
     */
    private Collection<Future<ArrayNode>> processMissions(final Map<String, String> topLevelManifests)
        throws ExitException {
        final Collection<Future<Map<Integer, String>>> missions = new ArrayList<>();
        final Collection<Future<ArrayNode>> solFutures = new ArrayList<>();
        // Submit task for each mission
        for (final Entry<String, String> manifest : topLevelManifests.entrySet()) {
            final String resource = manifest.getValue();
            final DynamoDBMissionWorker worker = new DynamoDBMissionWorker(resource, connectTimeout);
            final Future<Map<Integer, String>> future = manifestPool.submit(worker);
            // Add future to collection
            missions.add(future);
        }
        manifestPool.shutdown();
        final int numSolThreads = ConfigParser.parseInteger(config, CONFIG_NUM_SOL_THREADS, DEFAULT_THREADS);
        solPool = Executors.newFixedThreadPool(numSolThreads);
        // Process all mission futures
        while (!missions.isEmpty()) {
            final Iterator<Future<Map<Integer, String>>> it = missions.iterator();
            while (it.hasNext()) {
                final Future<Map<Integer, String>> missionFuture = it.next();
                if (missionFuture.isDone()) {
                    // Process finished mission future
                    try {
                        final Map<Integer, String> mission = missionFuture.get();
                        // Submit task for each sol in the mission
                        for (final String solURL : mission.values()) {
                            final DynamoDBSolWorker worker = new DynamoDBSolWorker(
                            /* dynamoDB, resourceTable, */solURL, connectTimeout);
                            final Future<ArrayNode> future = solPool.submit(worker);
                            // Add sol future to collection
                            solFutures.add(future);
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        // Skip mission if there was an error, but report
                        // warning
                        LOGGER.warning(e.getMessage());
                    } finally {
                        it.remove(); // future is processed (successful or
                                     // error), remove from collection
                    }
                }
            }
            // Wait a bit for the tasks to finish before checking again
            try {
                Thread.sleep(waitTime);
            } catch (final InterruptedException e) {
                LOGGER.warning(e.getMessage());
            }

        }
        solPool.shutdown();
        return solFutures;
    }

    /**
     * Retrieves and parses the root JSON to get Mars mission manifests.
     *
     * @return Map of mission to manifest URL
     * @throws ExitException
     *             if the parser cannot access or process the root manifest
     */
    private Map<String, String> processRootJSON() throws ExitException {
        // Parse parameters from configuration
        final String rootURL = ConfigParser.parseString(config, CONFIG_JSON_ROOT);
        // Get and parse top level manifest
        final DynamoDBJSONRootWorker rootWorker = new DynamoDBJSONRootWorker(rootURL, connectTimeout);
        final Future<Map<String, String>> rootFuture = manifestPool.submit(rootWorker);
        Map<String, String> topLevelManifests;
        try {
            topLevelManifests = rootFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ExitException("Could not process root JSON", e);
        }
        return topLevelManifests;
    }

    /**
     * <p>
     * Gets the results from each sol future as they become available and submits a new {@link DynamoDBImageWorker} to
     * process each image contained in the sol.
     * </p>
     * <p>
     * If there is an error processing a sol, a warning is logged and the sol is skipped.
     * </p>
     *
     * @param solFutures
     *            Futures for each sol that provides an {@link ArrayNode} that contains the images from the sol
     * @return Collection of {@link Future}s for monitoring {@link DynamoDBImageWorker} progress
     * @throws ExitException
     *             Error parsing configuration
     */
    private Collection<Future<?>> processSolFutures(final Collection<Future<ArrayNode>> solFutures)
        throws ExitException {
        final Collection<DynamoDBImageWorker> workers = new ArrayList<>();
        final Collection<Future<?>> imageFutures = new ArrayList<>();
        final int numImageThreads = ConfigParser.parseInteger(config, CONFIG_NUM_IMAGE_THREADS, DEFAULT_THREADS);
        imagePool = Executors.newFixedThreadPool(numImageThreads);
        // Process all sol futures
        while (!solFutures.isEmpty()) {
            LOGGER.info(solFutures.size() + " sols remaining");
            // Wait a bit for the tasks to finish before checking again
            try {
                Thread.sleep(waitTime);
            } catch (final InterruptedException e) {
                LOGGER.warning(e.getMessage());
            }
            final Iterator<Future<ArrayNode>> it = solFutures.iterator();
            while (it.hasNext()) {
                final Future<ArrayNode> solFuture = it.next();
                if (solFuture.isDone()) {
                    try {
                        final ArrayNode images = solFuture.get();
                        final int thumbnailWidth = ConfigParser.parseInteger(config, CONFIG_THUMBNAIL_WIDTH,
                            DEFAULT_THUMBNAIL_WIDTH);
                        final int thumbnailHeight = ConfigParser.parseInteger(config, CONFIG_THUMBNAIL_HEIGHT,
                            DEFAULT_THUMBNAIL_HEIGHT);
                        final boolean trackResources = ConfigParser.parseBoolean(config, CONFIG_TRACK_RESOURCES,
                            DEFAULT_TRACK_RESOURCES);
                        final boolean storeThumbnails = ConfigParser.parseBoolean(config, CONFIG_STORE_THUMBNAILS,
                            DEFAULT_STORE_THUMBNAILS);
                        // Submit task for each image in the sol
                        for (final JsonNode image : images) {
                            if (!image.isObject()) {
                                LOGGER.warning("Unexpected image: " + image);
                                continue;
                            }
                            final DynamoDBImageWorker worker = new DynamoDBImageWorker(dynamoDB, imageTable,
                                resourceTable, (ObjectNode) image, connectTimeout, thumbnailWidth, thumbnailHeight,
                                trackResources, storeThumbnails);
                            workers.add(worker);
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        // Skip sol if there was an error, but report warning
                        LOGGER.warning(e.getMessage());
                    } finally {
                        it.remove(); // future is processed (successful or
                                     // error), remove from collection
                    }
                }
            }
        }
        LOGGER.info("All sols processed.");
        for (final DynamoDBImageWorker worker : workers) {
            imageFutures.add(imagePool.submit(worker));
        }
        imagePool.shutdown();
        return imageFutures;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        try {
            setupTables(dynamoDB, config);
            final Map<String, String> missions = processRootJSON();
            final Collection<Future<ArrayNode>> imageArrayFutures = processMissions(missions);
            final Collection<Future<?>> imageFutures = processSolFutures(imageArrayFutures);
            awaitTermination(imageFutures);
        } catch (final ExitException e) {
            return;
        }
    }
}
