package com.amazonaws.services.dynamodbv2.json.converter;

import java.io.IOException;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;

/**
 * Utility for transforming between Jackson JSON streaming representation and DynamoDB format.
 */
public interface JacksonStreamReader {

    /**
     * Looks for the beginning of the next JSON object in the JsonParser and generates a map of AttributeValues that
     * represents the JSON object. If the internal JsonParser's current token is the beginning of a JSON array, it
     * advances to the next token and tries to find an object in the array.
     *
     * @return A map of AttributeValues that represents the JSON object. Null if the end of stream is reached.
     * @throws IOException
     *             Error reading stream
     */
    Map<String, AttributeValue> getNextItem() throws IOException;

    /**
     * Seeks for a field specified with the argument and advances the JsonParser to the value of the field.
     *
     * @param fieldName
     *            The key of the field to seek
     * @return True if a field with the specified name is found. False, otherwise.
     * @throws IOException
     *             Error reading stream
     */
    boolean seek(String fieldName) throws IOException;

}
