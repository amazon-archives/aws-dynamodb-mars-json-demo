package com.amazonaws.services.dynamodbv2.json.demo.mars;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.junit.Before;
import org.junit.Test;

public class PhotoIngesterCLITest {

    private static final String JSON_ROOT = "JSON.root";
    private static final String JSON_ROOT_VALUE = "http://json.jpl.nasa.gov/data.json";
    private static final String JSON_ROOT_VALUE2 = "https://s3.amazonaws.com/dynamodb-mars-json/root.json";
    private static final PrintStream nullStream = new PrintStream(new OutputStream() {
        @Override
        public void write(final int b) throws IOException {
        }
    });

    @Before
    public void setup() {
        System.setOut(nullStream);
        System.setErr(nullStream);
    }

    @Test
    public void testEmpty() throws ExitException {
        final String args[] = { "" };
        final ImageIngesterCLI cli = new ImageIngesterCLI(args);
        assertTrue(cli.getConfig() != null);
        assertTrue(cli.getConfig().get(JSON_ROOT).equals(JSON_ROOT_VALUE) || cli.getConfig().get(JSON_ROOT).equals(JSON_ROOT_VALUE2));
    }

    @Test
    public void testFile() throws ExitException {
        final String args[] = { "-f", "ImageIngester.properties" };
        final ImageIngesterCLI cli = new ImageIngesterCLI(args);
        assertTrue(cli.getConfig() != null);
        assertTrue(cli.getConfig().get(JSON_ROOT).equals(JSON_ROOT_VALUE) || cli.getConfig().get(JSON_ROOT).equals(JSON_ROOT_VALUE2));
    }

    @Test(expected = HelpException.class)
    public void testHelp() throws ExitException {
        final String args[] = { "-f", "file.properties", "-h" };
        new ImageIngesterCLI(args).getConfig();

    }

    @Test(expected = HelpException.class)
    public void testHelp2() throws ExitException {
        final String args[] = { "-f", "file.properties", "--help" };
        new ImageIngesterCLI(args).getConfig();
    }

    @Test(expected = HelpException.class)
    public void testInvalidOption() throws ExitException {
        final String args[] = { "-f", "file.properties", "--invalid-option" };
        new ImageIngesterCLI(args).getConfig();
    }
}
