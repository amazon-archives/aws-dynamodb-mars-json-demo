    package com.amazonaws.services.dynamodbv2.json.demo.mars.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utilities for retrieving data from http URLs.
 */
public final class NetworkUtils {
    /**
     * Logger for {@link NetworkUtils}.
     */
    private static final Logger LOGGER = Logger.getLogger(NetworkUtils.class.getName());
    /**
     * Header field key for ETag.
     */
    public static final String ETAG_HEADER = "ETag";
    /**
     * HTTP method HEAD.
     */
    private static final String HEAD = "HEAD";

    /**
     * Retrieves data from a URL.
     *
     * @param url
     *            The URL to retrieve data from
     * @param connectTimeout
     *            Connection timeout for retrieving data
     * @return byte array containing data
     * @throws IOException
     *             Invalid URL or connection error
     */
    public static byte[] getDataFromURL(final URL url, final int connectTimeout) throws IOException {
        return getDataFromURL(url, null, connectTimeout);
    }

    /**
     * Retrieves data from a URL that supports ETag headers if the current ETag is matches the expected value.
     *
     * @param url
     *            The URL to retrieve data from
     * @param expectedETag
     *            Previously recorded ETag for the resource
     * @param connectTimeout
     *            Connection timeout for retrieving data
     * @return byte array containing retrieved data
     * @throws IOException
     *             Invalid URL or connection error
     */
    public static byte[] getDataFromURL(final URL url, final String expectedETag, final int connectTimeout)
        throws IOException {
        HttpURLConnection conn = null;
        InputStream in;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(connectTimeout);
            conn.connect();
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                in = conn.getInputStream();
                if (expectedETag != null) {
                    final String eTag = conn.getHeaderField(ETAG_HEADER);
                    if (eTag == null || !eTag.equals(expectedETag)) {
                        throw new IllegalStateException("Expected ETag: " + expectedETag + ". Actual ETag: " + eTag);
                    }
                }
                final byte[] data = readStream(in);
                LOGGER.finer("Successfully retreived data from " + url.toExternalForm());
                return data;
            } else {
                LOGGER.log(Level.WARNING,
                    "Could not retrieve data from " + url.toExternalForm() + ": " + conn.getResponseCode() + " - " + conn.getResponseMessage());
                return null;
            }
        } catch (final ClassCastException e) {
            throw new UnsupportedOperationException("URL is not a valid HTTP URL");
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

    }

    /**
     * Gets the ETag header String for a URL.
     *
     * @param url
     *            URL to use to open an HTTPURLConnection
     * @return ETag header String
     * @throws IOException
     *             connection error
     */
    public static String getETag(final URL url) throws IOException {

        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(HEAD);
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                if (conn.getHeaderField(ETAG_HEADER) != null) {
                    return conn.getHeaderField(ETAG_HEADER);
                } else {
                    throw new IOException("No ETag header present in " + url + ". " + conn.getResponseCode() + ": "
                        + conn.getResponseMessage());
                }
            } else {
                throw new IOException("Could not retrieve ETag header from " + url + ". " + conn.getResponseCode()
                    + ": " + conn.getResponseMessage());
            }
        } catch (final ClassCastException e) {
            throw new UnsupportedOperationException("URL is not a valid HTTP URL");
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * Helper method to read an {@link InputStream} to a data source.
     *
     * @param in
     *            Input stream pointing to the data source
     * @return byte array containing data
     * @throws IOException
     *             Connection error
     */
    private static byte[] readStream(final InputStream in) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final int bufSize = 1024 * 1024; // 1MB
        final byte[] buf = new byte[bufSize];
        int n;
        while ((n = in.read(buf, 0, buf.length)) != -1) {
            baos.write(buf, 0, n);
        }
        return baos.toByteArray();
    }

    /**
     * Private constructor for static class.
     */
    private NetworkUtils() {

    }
}
