package com.amazonaws.services.dynamodbv2.json.demo.mars.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Properties;

import org.junit.Test;

import com.amazonaws.services.dynamodbv2.json.demo.mars.ExitException;

public class ConfigParserTest {

    private static final String LONG_TEST_VALUE = Long.toString(Long.MAX_VALUE);
    private static final String INT_TEST_VALUE = Integer.toString(Integer.MAX_VALUE);
    private static final String STRING_TEST_VALUE = "String";
    private static final String BOOLEAN_TEST_VALUE = "true";

    @Test(expected = ExitException.class)
    public void testCheckParamExists() throws ExitException {
        final Properties properties = new Properties();
        final String key = "p1";
        properties.put(key, key);
        try {
            ConfigParser.checkParamExists(properties, key);
        } catch (final ExitException e) {
            fail("Parameter exists");
        }
        properties.remove(key);
        ConfigParser.checkParamExists(properties, key);
    }

    @Test
    public void testParseBoolean() {
        Properties properties;
        final String param = "param";
        final boolean defaultValue = true;
        // Test valid required param
        try {
            properties = new Properties();
            properties.put(param, Boolean.toString(defaultValue));
            final boolean b = ConfigParser.parseBoolean(properties, param);
            assertEquals(defaultValue, b);
        } catch (final ExitException e) {
            fail("value is valid");
        }
        // Test invalid required param
        try {
            properties = new Properties();
            properties.put(param, "invalid boolean");
            ConfigParser.parseBoolean(properties, param);
        } catch (final ExitException e) {
            ; // Correct behavior
        }
        // Test valid not required param
        try {
            properties = new Properties();
            properties.put(param, Boolean.toString(!defaultValue));
            final boolean b = ConfigParser.parseBoolean(properties, param, defaultValue);
            assertEquals(defaultValue, !b);
        } catch (final ExitException e) {
            fail("value is valid");
        }
        // Test invalid not required param
        try {
            properties = new Properties();
            properties.put(param, "invalid boolean");
            final boolean b = ConfigParser.parseBoolean(properties, param, defaultValue);
            assertEquals(defaultValue, b);
        } catch (final ExitException e) {
            fail("default value is provided");
        }
    }

    @Test
    public void testParseInteger() {
        Properties properties;
        final String param = "param";
        final int defaultValue = 3;
        // Test valid required param
        try {
            properties = new Properties();
            properties.put(param, Integer.toString(defaultValue));
            final int i = ConfigParser.parseInteger(properties, param);
            assertEquals(defaultValue, i);
        } catch (final ExitException e) {
            fail("value is valid");
        }
        // Test invalid required param
        try {
            properties = new Properties();
            properties.put(param, "invalid integer");
            ConfigParser.parseInteger(properties, param);
        } catch (final ExitException e) {
            ; // Correct behavior
        }
        // Test valid not required param
        try {
            properties = new Properties();
            properties.put(param, Integer.toString(defaultValue - 1));
            final int i = ConfigParser.parseInteger(properties, param, defaultValue);
            assertEquals(defaultValue, i + 1);
        } catch (final ExitException e) {
            fail("value is valid");
        }
        // Test invalid not required param
        try {
            properties = new Properties();
            properties.put(param, "invalid integer");
            final int i = ConfigParser.parseInteger(properties, param, defaultValue);
            assertEquals(defaultValue, i);
        } catch (final ExitException e) {
            fail("default value is provided");
        }
    }

    @Test
    public void testParseLong() {
        Properties properties;
        final String param = "param";
        final long defaultValue = Long.MAX_VALUE - 10;
        // Test valid required param
        try {
            properties = new Properties();
            properties.put(param, Long.toString(defaultValue));
            final long l = ConfigParser.parseLong(properties, param);
            assertEquals(defaultValue, l);
        } catch (final ExitException e) {
            fail("value is valid");
        }
        // Test invalid required param
        try {
            properties = new Properties();
            properties.put(param, "invalid long");
            ConfigParser.parseLong(properties, param);
        } catch (final ExitException e) {
            ; // Correct behavior
        }
        // Test valid not required param
        try {
            properties = new Properties();
            properties.put(param, Long.toString(defaultValue + 1));
            final long l = ConfigParser.parseLong(properties, param, defaultValue);
            assertEquals(defaultValue, l - 1);
        } catch (final ExitException e) {
            fail("value is valid");
        }
        // Test invalid not required param
        try {
            properties = new Properties();
            properties.put(param, "invalid long");
            final long l = ConfigParser.parseLong(properties, param, defaultValue);
            assertEquals(defaultValue, l);
        } catch (final ExitException e) {
            fail("default value is provided");
        }
    }

    @Test
    public void testParseString() {

    }

    @Test (expected = ExitException.class)
    public void testValidateBooleanParam() throws ExitException {
        // Test correct input
        try {
            ConfigParser.validateBooleanParam("true");
            ConfigParser.validateBooleanParam("false");
        } catch (NullPointerException | ExitException e) {
            fail("value is valid");
        }
        // Test invalid input
        try {
            ConfigParser.validateBooleanParam("Not a boolean");
            fail("value is invalid");
        } catch (final ExitException e) {
            assertTrue(true); // Correct behavior
        }
        ConfigParser.validateBooleanParam(null);
        fail("value is null");

    }

    @Test
    public void testValidateConfig() {
        try {
            final Properties properties = new Properties();
            final String requiredStrings[] = {"p1", "p2", "p3"};
            final String requiredBooleans[] = {"p4", "p5", "p6"};
            final String requiredIntegers[] = {"p7", "p8", "p9"};
            final String requiredLongs[] = {"p10", "p11", "p12"};
            for (final String param : requiredStrings) {
                properties.put(param, STRING_TEST_VALUE);
            }
            for (final String param : requiredBooleans) {
                properties.put(param, BOOLEAN_TEST_VALUE);
            }
            for (final String param : requiredIntegers) {
                properties.put(param, INT_TEST_VALUE);
            }
            for (final String param : requiredLongs) {
                properties.put(param, LONG_TEST_VALUE);
            }
            ConfigParser.validateConfig(properties, requiredStrings, requiredBooleans, requiredIntegers, requiredLongs);
        } catch (final ExitException e) {
            fail();
        }
    }

    @Test(expected = ExitException.class)
    public void testValidateConfigMissingBoolean() throws ExitException {
        final Properties properties = new Properties();
        final String requiredStrings[] = {};
        final String requiredBooleans[] = {"p1", "p2", "p3"};
        final String requiredIntegers[] = {};
        final String requiredLongs[] = {};
        properties.put("p1", BOOLEAN_TEST_VALUE);
        properties.put("p3", BOOLEAN_TEST_VALUE);
        ConfigParser.validateConfig(properties, requiredStrings, requiredBooleans, requiredIntegers, requiredLongs);
    }

    @Test(expected = ExitException.class)
    public void testValidateConfigMissingInteger() throws ExitException {
        final Properties properties = new Properties();
        final String requiredStrings[] = {};
        final String requiredBooleans[] = {};
        final String requiredIntegers[] = {"p1", "p2", "p3"};
        final String requiredLongs[] = {};
        properties.put("p1", LONG_TEST_VALUE);
        properties.put("p3", LONG_TEST_VALUE);
        ConfigParser.validateConfig(properties, requiredStrings, requiredBooleans, requiredIntegers, requiredLongs);
    }

    @Test(expected = ExitException.class)
    public void testValidateConfigMissingLong() throws ExitException {
        final Properties properties = new Properties();
        final String requiredStrings[] = {};
        final String requiredBooleans[] = {};
        final String requiredIntegers[] = {};
        final String requiredLongs[] = {"p1", "p2", "p3"};
        properties.put("p1", "9e18");
        properties.put("p3", "9e18");
        ConfigParser.validateConfig(properties, requiredStrings, requiredBooleans, requiredIntegers, requiredLongs);
    }

    @Test(expected = ExitException.class)
    public void testValidateConfigMissingString() throws ExitException {
        final Properties properties = new Properties();
        final String requiredStrings[] = {"p1", "p2", "p3"};
        final String requiredBooleans[] = {};
        final String requiredIntegers[] = {};
        final String requiredLongs[] = {};
        properties.put("p1", STRING_TEST_VALUE);
        properties.put("p3", STRING_TEST_VALUE);
        ConfigParser.validateConfig(properties, requiredStrings, requiredBooleans, requiredIntegers, requiredLongs);
    }

    @Test
    public void testValidateIntegerParam() {
        // Test correct input
        try {
            ConfigParser.validateIntegerParam(Integer.toString(Integer.MAX_VALUE));
            ConfigParser.validateIntegerParam(Integer.toString(Integer.MIN_VALUE));
            ConfigParser.validateIntegerParam(Integer.toString(0));
        } catch (NullPointerException | ExitException e) {
            fail("value is valid");
        }
        // Test invalid input
        try {
            ConfigParser.validateIntegerParam("0.0"); // invalid integer
            fail("value is invalid");
        } catch (final ExitException e) {
            ; // Correct behavior
        }
        // Test null input
        try {
            ConfigParser.validateIntegerParam(null);
            fail("value is null");
        } catch (final ExitException e) {
            fail("value is null");
        } catch (final NullPointerException e) {
            ; // Correct behavior
        }
    }

    @Test
    public void testValidateLongParam() {
        // Test correct input
        try {
            ConfigParser.validateLongParam(Long.toString(Long.MAX_VALUE));
            ConfigParser.validateLongParam(Long.toString(Long.MIN_VALUE));
            ConfigParser.validateLongParam(Long.toString(0));
        } catch (NullPointerException | ExitException e) {
            fail("value is valid");
        }
        // Test invalid input
        try {
            ConfigParser.validateLongParam("0.0"); // invalid long
            fail("value is invalid");
        } catch (final ExitException e) {
            ; // Correct behavior
        }
        // Test null input
        try {
            ConfigParser.validateLongParam(null);
            fail("value is null");
        } catch (final ExitException e) {
            fail("value is null");
        } catch (final NullPointerException e) {
            ; // Correct behavior
        }
    }

}
