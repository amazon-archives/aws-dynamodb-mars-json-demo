package com.amazonaws.services.dynamodbv2.json.demo.mars.util;

import java.util.Properties;
import java.util.logging.Logger;

import com.amazonaws.services.dynamodbv2.json.demo.mars.ExitException;

/**
 * Provides static utility methods for parsing configuration options from a properties object.
 */
public final class ConfigParser {

    /**
     * Logger for {@link ConfigParser}.
     */
    private static final Logger LOGGER = Logger.getLogger(ConfigParser.class.getName());

    /**
     * Checks if a key is present in the properties object.
     *
     * @param properties
     *            Properties to check for key
     * @param param
     *            Key to look for in properties
     * @throws ExitException
     *             Key is not present in the properties object
     */
    public static void checkParamExists(final Properties properties, final String param) throws ExitException {
        if (!properties.containsKey(param)) {
            throw new ExitException("Missing configuration option: " + param);
        }
    }

    /**
     * Parses a required boolean parameter from a properties object.
     *
     * @param properties
     *            Properties to parse
     * @param param
     *            Required parameter key for properties
     * @return boolean value for parameter key
     * @throws ExitException
     *             Key is missing, or invalid boolean value
     */
    public static boolean parseBoolean(final Properties properties, final String param) throws ExitException {
        return parseBoolean(properties, param, null);
    }

    /**
     * Parses a boolean parameter from a properties object.
     *
     * @param properties
     *            Properties to parse
     * @param param
     *            Parameter key for properties
     * @param defaultValue
     *            Default boolean value if key is missing. If null, parameter is enforced as required.
     * @return boolean value for parameter key
     * @throws ExitException
     *             Key is missing from properties (for required parameter) or invalid boolean value
     */
    public static boolean parseBoolean(final Properties properties, final String param, final Boolean defaultValue)
        throws ExitException {
        String val = null;
        if (defaultValue == null) {
            checkParamExists(properties, param);
            val = properties.getProperty(param);
        } else {
            val = properties.getProperty(param, Boolean.toString(defaultValue));
        }
        try {
            validateBooleanParam(val);
        } catch (final ExitException e) {
            if (defaultValue == null) {
                LOGGER.severe("Invalid boolean value for configuration parameter " + param + ": " + val);
                throw e;
            } else {
                return defaultValue;
            }
        }
        return Boolean.parseBoolean(val);
    }

    /**
     * Parses a required integer parameter from a properties object.
     *
     * @param properties
     *            Properties to parse
     * @param param
     *            Required parameter key for properties
     * @return integer value for parameter key
     * @throws ExitException
     *             Key is missing, or invalid integer value
     */
    public static int parseInteger(final Properties properties, final String param) throws ExitException {
        return parseInteger(properties, param, null);
    }

    /**
     * Parses an integer parameter from a properties object.
     *
     * @param properties
     *            Properties to parse
     * @param param
     *            Parameter key for properties
     * @param defaultValue
     *            Default integer value if key is missing. If null, parameter is enforced as required.
     * @return boolean value for parameter key
     * @throws ExitException
     *             Key is missing from properties (for required parameter) or invalid integer value
     */
    public static int parseInteger(final Properties properties, final String param, final Integer defaultValue)
        throws ExitException {
        String val = null;
        if (defaultValue == null) {
            checkParamExists(properties, param);
            val = properties.getProperty(param);
        } else {
            val = properties.getProperty(param, Integer.toString(defaultValue));
        }
        try {
            validateIntegerParam(val);
        } catch (final ExitException e) {
            if (defaultValue == null) {
                LOGGER.severe("Invalid integer value for configuration parameter " + param + ": " + val);
                throw e;
            } else {
                return defaultValue;
            }
        }
        return Integer.parseInt(val);
    }

    /**
     * Parses a required long parameter from a properties object.
     *
     * @param properties
     *            Properties to parse
     * @param param
     *            Required parameter key for properties
     * @return long value for parameter key
     * @throws ExitException
     *             Key is missing, or invalid long value
     */
    public static long parseLong(final Properties properties, final String param) throws ExitException {
        return parseLong(properties, param, null);
    }

    /**
     * Parses a long parameter from a properties object.
     *
     * @param properties
     *            Properties to parse
     * @param param
     *            Parameter key for properties
     * @param defaultValue
     *            Default long value if key is missing. If null, parameter is enforced as required.
     * @return Long value for parameter key
     * @throws ExitException
     *             Key is missing from properties (for required parameter) or invalid long value
     */
    public static long parseLong(final Properties properties, final String param, final Long defaultValue)
        throws ExitException {
        String val = null;
        if (defaultValue == null) {
            checkParamExists(properties, param);
            val = properties.getProperty(param);
        } else {
            val = properties.getProperty(param, Long.toString(defaultValue));
        }
        try {
            validateLongParam(val);
        } catch (final ExitException e) {
            if (defaultValue == null) {
                LOGGER.severe("Invalid long value for configuration parameter " + param + ": " + val);
                throw e;
            } else {
                return defaultValue;
            }
        }
        return Long.parseLong(val);
    }

    /**
     * Parses a required String parameter from a properties object.
     *
     * @param properties
     *            Properties to parse
     * @param param
     *            Required parameter key for properties
     * @return String value for parameter key
     * @throws ExitException
     *             Key is missing
     */
    public static String parseString(final Properties properties, final String param) throws ExitException {
        return parseString(properties, param, null);
    }

    /**
     * Parses a String parameter from a properties object.
     *
     * @param properties
     *            Properties to parse
     * @param param
     *            Parameter key for properties
     * @param defaultValue
     *            Default String value if key is missing. If null, parameter is enforced as required.
     * @return String value for parameter key
     * @throws ExitException
     *             Key is missing from properties (for required parameter)
     */
    public static String parseString(final Properties properties, final String param, final String defaultValue)
        throws ExitException {
        if (defaultValue == null) {
            checkParamExists(properties, param);
        }
        return properties.getProperty(param, defaultValue);
    }

    /**
     * Validates a String representation of a boolean property.
     *
     * @param val
     *            String value to validate
     * @throws ExitException
     *             Validation fails
     */
    public static void validateBooleanParam(final String val) throws ExitException {
        if (val == null) {
            throw new ExitException("Null string");
        }
        final String lc = val.toLowerCase();
        if (!(lc.equals("true") || lc.equals("false"))) {
            throw new ExitException("Invalid boolean value: " + val);
        }
    }

    /**
     * Checks for required configuration options and validates types in a properties object.
     *
     * @param config
     *            The properties file to validate
     * @param requiredStrings
     *            Required String property keys
     * @param requiredBooleans
     *            Required Boolean property keys
     * @param requiredIntegers
     *            Required Integer property keys
     * @param requiredLongs
     *            Required Long property keys
     * @throws ExitException
     *             Validation fails
     */
    public static void validateConfig(final Properties config, final String[] requiredStrings,
        final String[] requiredBooleans, final String[] requiredIntegers, final String[] requiredLongs)
        throws ExitException {
        for (final String param : requiredStrings) {
            parseString(config, param);
        }
        for (final String param : requiredBooleans) {
            parseBoolean(config, param);
        }
        for (final String param : requiredIntegers) {
            parseInteger(config, param);
        }
        for (final String param : requiredLongs) {
            parseLong(config, param);
        }
    }

    /**
     * Validates a String representation of a integer property.
     *
     * @param val
     *            String value to validate
     * @throws ExitException
     *             Validation fails
     */
    public static void validateIntegerParam(final String val) throws ExitException {
        if (val == null) {
            throw new NullPointerException();
        }
        try {
            Integer.parseInt(val);
        } catch (final NumberFormatException e) {
            throw new ExitException("Invalid integer: " + val, e);
        }
    }

    /**
     * Validates a String representation of a long property.
     *
     * @param val
     *            String value to validate
     * @throws ExitException
     *             Validation fails
     */
    public static void validateLongParam(final String val) throws ExitException {
        if (val == null) {
            throw new NullPointerException();
        }
        try {
            Long.parseLong(val);
        } catch (final NumberFormatException e) {
            throw new ExitException("Invalid long: " + val, e);
        }
    }

    /**
     * Private constructor for a static class.
     */
    private ConfigParser() {
    }

}
