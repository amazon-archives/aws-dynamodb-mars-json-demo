package com.amazonaws.services.dynamodbv2.json.demo.mars.worker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.dynamodbv2.json.demo.mars.ImageIngester;
import com.amazonaws.services.dynamodbv2.json.demo.mars.util.JSONParser;
import com.amazonaws.services.dynamodbv2.json.demo.mars.util.NetworkUtils;
import com.amazonaws.util.json.JSONUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ URL.class, JSONParser.class, NetworkUtils.class })
public class DynamoDBMissionWorkerTest {

    private static final String IMAGE_RESOURCE_FILE = WorkerTestUtils.getPath("IMAGE_MANIFEST.json");

    private static final String INVALID_VERSION_FILE = WorkerTestUtils.getPath("IMAGE_MANIFEST_invalid_version.json");
    private static final String MISSING_SOL_LIST_FILE = WorkerTestUtils.getPath("IMAGE_MANIFEST_missing_sol_list.json");
    private static final String MISSING_SOL_ID_FILE = WorkerTestUtils.getPath("IMAGE_MANIFEST_missing_sol_id.json");
    private static final String MISSING_SOL_URL_FILE = WorkerTestUtils.getPath("IMAGE_MANIFEST_missing_sol_url.json");

    private static final Map<Integer, String> EXPECTED_MAP;
    static {
        EXPECTED_MAP = new HashMap<Integer, String>();
        for (int i = 0; i <= 753; i++) {
            EXPECTED_MAP.put(i, "http://msl-raws.s3.amazonaws.com/images/images_sol" + i + ".json");
        }
        final int[] missingSols = { 557, 570, 577, 596, 598, 599, 600, 616, 625, 693 };
        for (final Integer missingSol : missingSols) {
            EXPECTED_MAP.remove(missingSol);
        }
    }

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void testGetSolJSON() {
        final URL url = PowerMock.createMock(URL.class);
        PowerMock.mockStatic(JSONUtils.class);
        PowerMock.mockStatic(NetworkUtils.class);
        String manifest = null;
        try {
            manifest = WorkerTestUtils.readFile(IMAGE_RESOURCE_FILE);
        } catch (final IOException e1) {
            fail("Could not read file: " + IMAGE_RESOURCE_FILE);
        }
        try {
            NetworkUtils.getDataFromURL(url, null, ImageIngester.DEFAULT_CONNECT_TIMEOUT);
            PowerMock.expectLastCall().andReturn(manifest.getBytes());
            PowerMock.replayAll();
            final Map<Integer, String> actual = DynamoDBMissionWorker.getSolJSON(url, ImageIngester.DEFAULT_CONNECT_TIMEOUT);
            assertEquals(EXPECTED_MAP, actual);
        } catch (final IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testInvalidVersion() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("version verification failed");
        final URL url = PowerMock.createMock(URL.class);
        PowerMock.mockStatic(JSONUtils.class);
        PowerMock.mockStatic(NetworkUtils.class);
        String manifest = null;
        try {
            manifest = WorkerTestUtils.readFile(INVALID_VERSION_FILE);
        } catch (final IOException e) {
            fail("Could not read file: " + INVALID_VERSION_FILE);
        }
        try {
            NetworkUtils.getDataFromURL(url, null, ImageIngester.DEFAULT_CONNECT_TIMEOUT);
            PowerMock.expectLastCall().andReturn(manifest.getBytes());
            PowerMock.replayAll();
            DynamoDBMissionWorker.getSolJSON(url, ImageIngester.DEFAULT_CONNECT_TIMEOUT);
        } catch (final IOException e) {
            fail(e.getMessage());
        }
        fail("Version is incorrect");
    }

    @Test
    public void testMissingSolID() {
        final Logger logger = Logger.getLogger(DynamoDBMissionWorker.class.getName());
        Handler handler;
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        final URL url = PowerMock.createMock(URL.class);
        PowerMock.mockStatic(JSONUtils.class);
        PowerMock.mockStatic(NetworkUtils.class);
        String manifest = null;
        try {
            manifest = WorkerTestUtils.readFile(MISSING_SOL_ID_FILE);
        } catch (final IOException e) {
            fail("Could not read file: " + MISSING_SOL_ID_FILE);
        }
        try {
            NetworkUtils.getDataFromURL(url, null, ImageIngester.DEFAULT_CONNECT_TIMEOUT);
            PowerMock.expectLastCall().andReturn(manifest.getBytes());
            PowerMock.replayAll();
            handler = new StreamHandler(os, new SimpleFormatter());
            logger.setUseParentHandlers(false);
            logger.addHandler(handler);
            DynamoDBMissionWorker.getSolJSON(url, ImageIngester.DEFAULT_CONNECT_TIMEOUT);
            handler.flush();
            assertTrue(os.toString().contains("WARNING: Sol missing required keys"));
        } catch (final IOException e) {
            fail(e.getMessage());
        } finally {
            try {
                os.close();
            } catch (final IOException e) {
                fail(e.getMessage());
            }
        }
    }

    @Test
    public void testMissingSolList() {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("does not contain a sol list");
        final URL url = PowerMock.createMock(URL.class);
        PowerMock.mockStatic(JSONUtils.class);
        PowerMock.mockStatic(NetworkUtils.class);
        String manifest = null;
        try {
            manifest = WorkerTestUtils.readFile(MISSING_SOL_LIST_FILE);
        } catch (final IOException e) {
            fail("Could not read file: " + MISSING_SOL_LIST_FILE);
        }
        try {
            NetworkUtils.getDataFromURL(url, null, ImageIngester.DEFAULT_CONNECT_TIMEOUT);
            PowerMock.expectLastCall().andReturn(manifest.getBytes());
            PowerMock.replayAll();
            DynamoDBMissionWorker.getSolJSON(url, ImageIngester.DEFAULT_CONNECT_TIMEOUT);
        } catch (final IOException e) {
            fail(e.getMessage());
        }
        fail("Missing sol list");
    }

    @Test
    public void testMissingSolURL() {
        final Logger logger = Logger.getLogger(DynamoDBMissionWorker.class.getName());
        Handler handler;
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        final URL url = PowerMock.createMock(URL.class);
        PowerMock.mockStatic(JSONUtils.class);
        PowerMock.mockStatic(NetworkUtils.class);
        String manifest = null;
        try {
            manifest = WorkerTestUtils.readFile(MISSING_SOL_URL_FILE);
        } catch (final IOException e) {
            fail("Could not read file: " + MISSING_SOL_URL_FILE);
        }
        try {
            NetworkUtils.getDataFromURL(url, null, ImageIngester.DEFAULT_CONNECT_TIMEOUT);
            PowerMock.expectLastCall().andReturn(manifest.getBytes());
            PowerMock.replayAll();
            handler = new StreamHandler(os, new SimpleFormatter());
            logger.setUseParentHandlers(false);
            logger.addHandler(handler);
            DynamoDBMissionWorker.getSolJSON(url, ImageIngester.DEFAULT_CONNECT_TIMEOUT);
            handler.flush();
            assertTrue(os.toString().contains("WARNING: Sol missing required keys"));
        } catch (final IOException e) {
            fail(e.getMessage());
        } finally {
            try {
                os.close();
            } catch (final IOException e) {
                fail(e.getMessage());
            }
        }
    }

}
