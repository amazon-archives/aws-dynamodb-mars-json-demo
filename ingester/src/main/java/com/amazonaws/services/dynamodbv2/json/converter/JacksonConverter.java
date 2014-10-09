package com.amazonaws.services.dynamodbv2.json.converter;

import java.util.List;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Utility for transforming between Jackson JSON and DynamoDB representations.
 */
public interface JacksonConverter {

    /**
     * Converts a list of maps of AttributeValues to a JsonNode instance that represents the list of maps.
     *
     * @param items
     *            A list of maps of AttributeValues
     * @return A JsonNode instance that represents the converted JSON array.
     * @throws JacksonConverterException
     *             Error converting DynamoDB item to JSON
     */
    JsonNode itemListToJsonArray(List<Map<String, AttributeValue>> items) throws JacksonConverterException;

    /**
     * Converts a JSON array to a list of AttributeValues.
     *
     * @param array
     *            A JsonNode instance that represents the target JSON array.
     * @return A list of AttributeValues that represents the JSON array.
     * @throws JacksonConverterException
     *             if JsonNode is not an array
     */
    List<AttributeValue> jsonArrayToList(JsonNode array) throws JacksonConverterException;

    /**
     * Converts a JSON object to a map of AttributeValues.
     *
     * @param object
     *            A JsonNode instance that represents the target JSON object.
     * @return A map of AttributeValues that represents the JSON object.
     * @throws JacksonConverterException
     *             if JsonNode is not an object.
     */
    Map<String, AttributeValue> jsonObjectToMap(JsonNode object) throws JacksonConverterException;

    /**
     * Converts a list of AttributeValues to a JsonNode instance that represents the list.
     *
     * @param list
     *            A list of AttributeValues
     * @return A JsonNode instance that represents the converted JSON array.
     * @throws JacksonConverterException
     *             Error converting DynamoDB item to JSON
     */
    JsonNode listToJsonArray(List<AttributeValue> list) throws JacksonConverterException;

    /**
     * Converts a map of AttributeValues to a JsonNode instance that represents the map.
     *
     * @param map
     *            A map of AttributeValues
     * @return A JsonNode instance that represents the converted JSON object.
     * @throws JacksonConverterException
     *             Error converting DynamoDB item to JSON
     */
    JsonNode mapToJsonObject(Map<String, AttributeValue> map) throws JacksonConverterException;
}
