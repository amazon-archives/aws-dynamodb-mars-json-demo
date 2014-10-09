package com.amazonaws.services.dynamodbv2.json.converter;

/**
 * Exception occurred while transforming between representations.
 */
public class JacksonConverterException extends Exception {

    /**
     * Serial Version.
     */
    private static final long serialVersionUID = 8457313895307710705L;

    /**
     * Constructs a new {@link JacksonConverterException} with the provided message.
     *
     * @param message
     *            Error message detailing exception
     */
    public JacksonConverterException(final String message) {
        super(message);
    }

}
