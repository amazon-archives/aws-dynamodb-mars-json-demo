package com.amazonaws.services.dynamodbv2.json.demo.mars.worker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AmazonDynamoDB.class})
public class DynamoDBSolWorkerTest {
    private static final String MSL_SOL_FILE = WorkerTestUtils.getPath("SOL_MSL.json");
    private static final String MSL_SOL_EXPECTED_FILE = WorkerTestUtils.getPath("SOL_MSL_EXPECTED.json");
    private static final String MER_SOL_FILE = WorkerTestUtils.getPath("SOL_MER.json");
    private static final String MER_SOL_EXPECTED_FILE = WorkerTestUtils.getPath("SOL_MER_EXPECTED.json");
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testParseMERSol() {
        testParseSol(MER_SOL_FILE, MER_SOL_EXPECTED_FILE);
    }

    @Test
    public void testParseMSLSol() {
        testParseSol(MSL_SOL_FILE, MSL_SOL_EXPECTED_FILE);
    }

    private void testParseSol(final String file, final String expectedFile) {
        try {
            final JsonNode sol = mapper.readTree(new File(file));
            final ArrayNode expected = (ArrayNode) mapper.readTree(new File(expectedFile));

            final ArrayNode result = DynamoDBSolWorker.getImages(sol);
            assertEquals(expected, result);
        } catch (final IOException e) {
            fail(e.getMessage());
        }

    }
}
