package com.amazonaws.services.dynamodbv2.json.converter;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;

/**
 * Exception occurred while transforming between representations.
 */
public class JacksonStreamReaderException extends JsonParseException {

    /**
     * Serial Version.
     */
    private static final long serialVersionUID = -210292231601427891L;

    /**
     * Constructs a {@link JacksonStreamReaderException} with the provided message at the specified location.
     *
     * @param message
     *            Error message
     * @param location
     *            Location of exception
     */
    public JacksonStreamReaderException(final String message, final JsonLocation location) {
        super(message, location);
    }

}
