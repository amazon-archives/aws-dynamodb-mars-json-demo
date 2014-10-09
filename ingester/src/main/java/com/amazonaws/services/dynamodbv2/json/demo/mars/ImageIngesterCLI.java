package com.amazonaws.services.dynamodbv2.json.demo.mars;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Properties;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Processes command line arguments and loads properties file containing configuration.
 * <p/>
 * The configuration file can be loaded using one of the following methods:
 * <ul>
 * <li>Default location: {@value #DEFAULT_CONFIG_FILE_LOCATION}</li>
 * <li>Filepath: specify using CLI option {@value #OPTION_FILE_SHORT}</li>
 * <li>Classpath: specify using CLI option {@value #OPTION_CLASSPATH_SHORT}</li>
 * </ul>
 */
public class ImageIngesterCLI {
    /**
     * Classpath separator for the OS.
     */
    public static final String CLASSPATH_SEPARATOR = System.getProperty("path.separator");
    /**
     * Classpath.
     */
    public static final String CLASSPATH = System.getProperty("java.class.path");
    /**
     * File encoding.
     */
    public static final String FILE_ENCODING = "UTF-8";

    // Constants for CLI
    /**
     * Application name for displaying help.
     */
    private static final String APP_NAME = "DynamoDB Mars Image Ingester";
    /**
     * Options for CLI.
     */
    private static final Options OPTIONS;
    /**
     * Help option description.
     */
    private static final String OPTION_HELP_DESC = "Shows help";
    /**
     * Help short option.
     */
    private static final String OPTION_HELP_SHORT = "h";
    /**
     * Help long option.
     */
    private static final String OPTION_HELP_LONG = "help";
    /**
     * Help has no argument.
     */
    private static final boolean OPTION_HELP_HASARG = false;
    /**
     * Help is not a required flag.
     */
    private static final boolean OPTION_HELP_REQ = false;
    /**
     * Configuration filepath description.
     */
    private static final String OPTION_FILE_DESC = "Load configuration file from filepath";
    /**
     * Configuration filepath short option.
     */
    private static final String OPTION_FILE_SHORT = "f";
    /**
     * Configuration filepath long option.
     */
    private static final String OPTION_FILE_LONG = "filepath-configuration";
    /**
     * Configuration filepath has an argument.
     */
    private static final boolean OPTION_FILE_HASARG = true;
    /**
     * Configuration file is not a required option.
     */
    private static final boolean OPTION_FILE_REQ = false;
    /**
     * Configuration file name in classpath description.
     */
    private static final String OPTION_CLASSPATH_DESC = "Load configuration file by name from classpath";
    /**
     * Configuration file name in classpath short option.
     */
    private static final String OPTION_CLASSPATH_SHORT = "n";
    /**
     * Configuration file name in classpath long option.
     */
    private static final String OPTION_CLASSPATH_LONG = "name-from-classpath";
    /**
     * Configuration file name in classpath has an argument.
     */
    private static final boolean OPTION_CLASSPATH_HASARG = true;
    /**
     * Configuration file name in classpath is not a required option.
     */
    private static final boolean OPTION_CLASSPATH_REQ = false;
    /**
     * Default configuration file name.
     */
    public static final String DEFAULT_CONFIG_FILE_NAME = "ImageIngester.properties";

    static {
        OPTIONS = new Options();
        final Option helpOpt = new Option(OPTION_HELP_SHORT, OPTION_HELP_LONG, OPTION_HELP_HASARG, OPTION_HELP_DESC);
        helpOpt.setRequired(OPTION_HELP_REQ);
        OPTIONS.addOption(helpOpt);
        final Option configOpt = new Option(OPTION_FILE_SHORT, OPTION_FILE_LONG, OPTION_FILE_HASARG, OPTION_FILE_DESC);
        configOpt.setRequired(OPTION_FILE_REQ);
        OPTIONS.addOption(configOpt);
        final Option classpathOpt = new Option(OPTION_CLASSPATH_SHORT, OPTION_CLASSPATH_LONG, OPTION_CLASSPATH_HASARG,
            OPTION_CLASSPATH_DESC);
        classpathOpt.setRequired(OPTION_CLASSPATH_REQ);
        OPTIONS.addOption(classpathOpt);
    }

    // State
    /**
     * CLI arguments.
     */
    private final String[] args;
    /**
     * Loaded configuration.
     */
    private Properties config = null;

    /**
     * Constructs a {@link ImageIngesterCLI} for parsing the supplied CLI arguments.
     *
     * @param args
     *            CLI arguments
     */
    public ImageIngesterCLI(final String[] args) {
        this.args = args.clone();
    }

    /**
     * Called by main program to get the configuration.
     *
     * @return Properties
     * @throws ExitException
     *             Help option specified, syntax error, or error loading configuration file
     */
    public Properties getConfig() throws ExitException {
        parse();
        return config;
    }

    /**
     * Prints help message when help option {@value #OPTION_HELP_SHORT} or {@value #OPTION_HELP_LONG} is specified or
     * illegal syntax is used.
     */
    private void help() {
        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(APP_NAME, OPTIONS);
        throw new HelpException();
    }

    /**
     * Loads the configuration file by name from the classpath.
     *
     * @param file
     *            The name of the file
     * @return Configuration properties
     * @throws ExitException
     *             Error loading configuration file from classpath
     */
    private Properties loadConfigFromClasspath(final String file) throws ExitException {
        final Properties p = new Properties();
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(getClass().getClassLoader().getResourceAsStream(file),
                Charset.forName(FILE_ENCODING));
            p.load(reader);
        } catch (final IOException | NullPointerException e) {
            throw new ExitException("Could not load configuration file from classpath. File= " + file + ", Classpath="
                + CLASSPATH, e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    throw new ExitException("Could not close configuration classpath");
                }
            }
        }
        return p;
    }

    /**
     * Loads the configuration file from a filepath.
     *
     * @param filepath
     *            The path to the file
     * @return Configuration properties
     * @throws ExitException
     *             Error loading configuration file from filepath
     */
    private Properties loadConfigFromFilepath(final String filepath) throws ExitException {
        final Properties p = new Properties();
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(new File(filepath)), Charset.forName(FILE_ENCODING));
            p.load(reader);
        } catch (final IOException e) {
            throw new ExitException("Could not load configuration file from filepath", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    throw new ExitException("Could not close configuration file");
                }
            }
        }
        return p;
    }

    /**
     * Parses command line arguments to load the configuration file.
     *
     * @throws ExitException
     *             Help option specified, syntax error, or error loading configuration file
     */
    private void parse() throws ExitException {
        final BasicParser parser = new BasicParser();
        CommandLine cl = null;
        try {
            cl = parser.parse(OPTIONS, args);
        } catch (final ParseException e) {
            help();

        }
        if (cl.hasOption(OPTION_HELP_SHORT)) {
            help();
        }
        if (cl.hasOption(OPTION_FILE_SHORT) && cl.hasOption(OPTION_CLASSPATH_SHORT)) {
            throw new ExitException("May not specify both of the command line options " + OPTION_FILE_SHORT + " and "
                + OPTION_CLASSPATH_SHORT);
        }
        if (cl.hasOption(OPTION_FILE_SHORT)) {
            final String filePath = cl.getOptionValue(OPTION_FILE_SHORT);
            config = loadConfigFromFilepath(filePath);
        } else {
            config = loadConfigFromClasspath(DEFAULT_CONFIG_FILE_NAME);
        }
        if (cl.hasOption(OPTION_CLASSPATH_SHORT)) {
            final String file = cl.getOptionValue(OPTION_CLASSPATH_SHORT);
            config = loadConfigFromClasspath(file);
        }
    }

}
