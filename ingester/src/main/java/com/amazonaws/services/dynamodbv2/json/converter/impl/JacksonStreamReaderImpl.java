package com.amazonaws.services.dynamodbv2.json.converter.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.json.converter.JacksonStreamReader;
import com.amazonaws.services.dynamodbv2.json.converter.JacksonStreamReaderException;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 * Implementation of JacksonStreamReader transformer.
 */
public class JacksonStreamReaderImpl implements JacksonStreamReader {
    /**
     * JsonParser for getting tokens.
     */
    private final JsonParser jp;

    /**
     * Constructs a {@link JacksonStreamReaderImpl} with the provided {@link JsonParser}.
     *
     * @param jp
     *            JsonParser from which to get tokens
     * @throws IOException
     *             Null JsonParser or error getting token
     */
    public JacksonStreamReaderImpl(final JsonParser jp) throws IOException {
        if (jp == null) {
            throw new JacksonStreamReaderException("JsonParser cannot be null", JsonLocation.NA);
        }
        this.jp = jp;
        if (jp.getCurrentToken() == null) {
            jp.nextToken();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, AttributeValue> getNextItem() throws IOException {
        if (isEndReached()) {
            return null;
        }

        if (isObject()) {
            return getNextMap();
        } else if (isArray() || isEndOfArray() || isEndOfObject()) {
            jp.nextToken();
            return getNextItem();
        } else {
            throw new JacksonStreamReaderException("The start of next item needs to be an object, but was "
                + jp.getCurrentToken(), jp.getCurrentLocation());
        }
    }

    /**
     * Gets the next list from the JsonParser in a DynamoDB representation.
     *
     * @return DynamoDB representation of the next list from the JsonParser
     * @throws IOException
     *             Error getting token or unknown value type.
     */
    private List<AttributeValue> getNextList() throws IOException {
        final List<AttributeValue> list = new ArrayList<AttributeValue>();
        while (JsonToken.END_ARRAY != jp.nextToken()) {
            switch (jp.getCurrentToken()) {
                case VALUE_STRING:
                    list.add(new AttributeValue().withS(jp.getText()));
                    break;
                case VALUE_NUMBER_INT:
                case VALUE_NUMBER_FLOAT:
                    list.add(new AttributeValue().withN(jp.getValueAsString()));
                    break;
                case VALUE_TRUE:
                case VALUE_FALSE:
                    list.add(new AttributeValue().withBOOL(jp.getBooleanValue()));
                    break;
                case VALUE_NULL:
                    list.add(new AttributeValue().withNULL(true));
                    break;
                case START_OBJECT:
                    list.add(new AttributeValue().withM(getNextMap()));
                    break;
                case START_ARRAY:
                    list.add(new AttributeValue().withL(getNextList()));
                    break;
                default:
                    throw new JacksonStreamReaderException("Unknown value type: " + jp.getCurrentToken().name(),
                        jp.getCurrentLocation());
            }
        }
        return list;
    }

    /**
     * Gets the next map from the JsonParser in a DynamoDB representation.
     *
     * @return DynamoDB representation of the next map from the JsonParser
     * @throws IOException
     *             Error getting token or unknown value type
     */
    private Map<String, AttributeValue> getNextMap() throws IOException {
        final Map<String, AttributeValue> map = new HashMap<String, AttributeValue>();
        while (JsonToken.END_OBJECT != jp.nextToken()) {
            switch (jp.getCurrentToken()) {
                case FIELD_NAME:
                    continue;
                case VALUE_STRING:
                    map.put(jp.getCurrentName(), new AttributeValue().withS(jp.getText()));
                    break;
                case VALUE_NUMBER_INT:
                case VALUE_NUMBER_FLOAT:
                    map.put(jp.getCurrentName(), new AttributeValue().withN(jp.getValueAsString()));
                    break;
                case VALUE_TRUE:
                case VALUE_FALSE:
                    map.put(jp.getCurrentName(), new AttributeValue().withBOOL(jp.getBooleanValue()));
                    break;
                case VALUE_NULL:
                    map.put(jp.getCurrentName(), new AttributeValue().withNULL(true));
                    break;
                case START_OBJECT:
                    map.put(jp.getCurrentName(), new AttributeValue().withM(getNextMap()));
                    break;
                case START_ARRAY:
                    map.put(jp.getCurrentName(), new AttributeValue().withL(getNextList()));
                    break;
                default:
                    throw new JacksonStreamReaderException("Unknown value type: " + jp.getCurrentToken().name(),
                        jp.getCurrentLocation());
            }
        }
        return map;
    }

    /**
     * Checks if current token is the start of an array.
     *
     * @return True if current token is the start of an array.
     */
    private boolean isArray() {
        return jp.getCurrentToken() == JsonToken.START_ARRAY;
    }

    /**
     * Checks if the current token is the end of an array.
     *
     * @return True if the current token is the end of an array
     */
    private boolean isEndOfArray() {
        return jp.getCurrentToken() == JsonToken.END_ARRAY;
    }

    /**
     * Checks if the current token is the end of an object.
     *
     * @return True if the current token is the end of an object
     */
    private boolean isEndOfObject() {
        return jp.getCurrentToken() == JsonToken.END_OBJECT;
    }

    /**
     * Checks if the current token is null.
     *
     * @return True if the current token is null
     */
    private boolean isEndReached() {
        return jp.getCurrentToken() == null;
    }

    /**
     * Checks if the current token is the start of an object.
     *
     * @return True if the current token is the start of an object
     */
    private boolean isObject() {
        return jp.getCurrentToken() == JsonToken.START_OBJECT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean seek(final String fieldName) throws IOException {
        if (fieldName == null) {
            return false;
        }
        do {
            if (fieldName.equals(jp.getCurrentName())) {
                jp.nextValue();
                return true;
            }
            jp.nextToken();
        } while (!isEndReached());
        return false;
    }
}
