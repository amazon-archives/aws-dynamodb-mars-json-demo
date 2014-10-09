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

import org.junit.Test;
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
public class DynamoDBJSONRootWorkerTest {
    private static final String ROOT_JSON_FILE = WorkerTestUtils.getPath("JSON_ROOT.json");
    private static final String EMPTY_MISSION_BODY_FILE = WorkerTestUtils.getPath("JSON_ROOT_empty_mission_body.json");

    private static final Map<String, String> EXPECTED_MAP;
    static {
        EXPECTED_MAP = new HashMap<String, String>();
        EXPECTED_MAP.put("MERA", "https://merpublic.s3.amazonaws.com/oss/mera/images/image_manifest.json");
        EXPECTED_MAP.put("MERB", "https://merpublic.s3.amazonaws.com/oss/merb/images/image_manifest.json");
        EXPECTED_MAP.put("MSL", "https://msl-raws.s3.amazonaws.com/images/image_manifest.json");
    }

    @Test
    public void testEmptyMissionBody() throws IOException {
        final Logger logger = Logger.getLogger(DynamoDBJSONRootWorker.class.getName());
        Handler handler;
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        final URL url = PowerMock.createMock(URL.class);
        PowerMock.mockStatic(JSONUtils.class);
        PowerMock.mockStatic(NetworkUtils.class);
        String manifest = null;
        try {
            manifest = WorkerTestUtils.readFile(EMPTY_MISSION_BODY_FILE);
        } catch (final IOException e1) {
            fail("Could not read file: " + EMPTY_MISSION_BODY_FILE);
        }
        try {
            NetworkUtils.getDataFromURL(url, null, ImageIngester.DEFAULT_CONNECT_TIMEOUT);
            PowerMock.expectLastCall().andReturn(manifest.getBytes());
            PowerMock.replayAll();
            handler = new StreamHandler(os, new SimpleFormatter());
            logger.setUseParentHandlers(false);
            logger.addHandler(handler);
            try {
                DynamoDBJSONRootWorker.getMissionToManifestMap(url, ImageIngester.DEFAULT_CONNECT_TIMEOUT);
            } catch (final IOException e) {
                fail(e.getMessage());
            }
            handler.flush();
            assertTrue(os.toString().contains("WARNING: Missing mission manifest for MERB: {}"));
        } finally {
            try {
                os.close();
            } catch (final IOException e) {
                fail(e.getMessage());
            }
        }
    }

    @Test
    public void testGetMissionToManifestMap() {
        final URL url = PowerMock.createMock(URL.class);
        PowerMock.mockStatic(JSONUtils.class);
        PowerMock.mockStatic(NetworkUtils.class);
        String manifest = null;
        try {
            manifest = WorkerTestUtils.readFile(ROOT_JSON_FILE);
        } catch (final IOException e1) {
            fail("Could not read file: " + ROOT_JSON_FILE);
        }
        try {
            NetworkUtils.getDataFromURL(url, null, ImageIngester.DEFAULT_CONNECT_TIMEOUT);
            PowerMock.expectLastCall().andReturn(manifest.getBytes());
            PowerMock.replayAll();
            final Map<String, String> missionMap = DynamoDBJSONRootWorker.getMissionToManifestMap(url, ImageIngester.DEFAULT_CONNECT_TIMEOUT);
            assertEquals(EXPECTED_MAP, missionMap);
        } catch (final IOException e) {
            fail(e.getMessage());
        }

    }
}
