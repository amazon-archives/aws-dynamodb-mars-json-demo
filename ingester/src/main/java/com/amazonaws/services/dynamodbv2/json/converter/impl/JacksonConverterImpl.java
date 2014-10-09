package com.amazonaws.services.dynamodbv2.json.converter.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.amazonaws.services.dynamodbv2.json.converter.JacksonConverter;
import com.amazonaws.services.dynamodbv2.json.converter.JacksonConverterException;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Implementation of the {@link JacksonConverter}.
 */
public class JacksonConverterImpl implements JacksonConverter {
    /**
     * Maximum JSON depth.
     */
    private static final int MAX_DEPTH = 50;

    /**
     * Constructs a {@link JacksonConverterImpl}.
     */
    public JacksonConverterImpl() {
    }

    /**
     * Asserts the depth is not greater than {@link #MAX_DEPTH}.
     *
     * @param depth
     *            Current JSON depth
     * @throws JacksonConverterException
     *             Depth is greater than {@link #MAX_DEPTH}
     */
    private void assertDepth(final int depth) throws JacksonConverterException {
        if (depth > MAX_DEPTH) {
            throw new JacksonConverterException("Max depth reached. The object/array has too much depth.");
        }
    }

    /**
     * Gets an DynamoDB representation of a JsonNode.
     *
     * @param node
     *            The JSON to convert
     * @param depth
     *            Current JSON depth
     * @return DynamoDB representation of the JsonNode
     * @throws JacksonConverterException
     *             Unknown JsonNode type or JSON is too deep
     */
    private AttributeValue getAttributeValue(final JsonNode node, final int depth) throws JacksonConverterException {
        assertDepth(depth);
        switch (node.asToken()) {
            case VALUE_STRING:
                return new AttributeValue().withS(node.textValue());
            case VALUE_NUMBER_INT:
            case VALUE_NUMBER_FLOAT:
                return new AttributeValue().withN(node.numberValue().toString());
            case VALUE_TRUE:
            case VALUE_FALSE:
                return new AttributeValue().withBOOL(node.booleanValue());
            case VALUE_NULL:
                return new AttributeValue().withNULL(true);
            case START_OBJECT:
                return new AttributeValue().withM(jsonObjectToMap(node, depth));
            case START_ARRAY:
                return new AttributeValue().withL(jsonArrayToList(node, depth));
            default:
                throw new JacksonConverterException("Unknown node type: " + node);
        }
    }

    /**
     * Converts a DynamoDB attribute to a JSON representation.
     *
     * @param av
     *            DynamoDB attribute
     * @param depth
     *            Current JSON depth
     * @return JSON representation of the DynamoDB attribute
     * @throws JacksonConverterException
     *             Unknown DynamoDB type or JSON is too deep
     */
    private JsonNode getJsonNode(final AttributeValue av, final int depth) throws JacksonConverterException {
        assertDepth(depth);
        if (av.getS() != null) {
            return JsonNodeFactory.instance.textNode(av.getS());
        } else if (av.getN() != null) {
            try {
                return JsonNodeFactory.instance.numberNode(Integer.parseInt(av.getN()));
            } catch (final NumberFormatException e) {
                // Not an integer
                try {
                    return JsonNodeFactory.instance.numberNode(Float.parseFloat(av.getN()));
                } catch (final NumberFormatException e2) {
                    // Not a number
                    throw new JacksonConverterException(e.getMessage());
                }
            }
        } else if (av.getBOOL() != null) {
            return JsonNodeFactory.instance.booleanNode(av.getBOOL());
        } else if (av.getNULL() != null) {
            return JsonNodeFactory.instance.nullNode();
        } else if (av.getL() != null) {
            return listToJsonArray(av.getL(), depth);
        } else if (av.getM() != null) {
            return mapToJsonObject(av.getM(), depth);
        } else {
            throw new JacksonConverterException("Unknown type value " + av);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonNode itemListToJsonArray(final List<Map<String, AttributeValue>> items) throws JacksonConverterException {
        if (items != null) {
            final ArrayNode array = JsonNodeFactory.instance.arrayNode();
            for (final Map<String, AttributeValue> item : items) {
                array.add(mapToJsonObject(item, 0));
            }
            return array;
        }
        throw new JacksonConverterException("Items cannnot be null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AttributeValue> jsonArrayToList(final JsonNode node) throws JacksonConverterException {
        return jsonArrayToList(node, 0);
    }

    /**
     * Helper method to convert a JsonArrayNode to a DynamoDB list.
     *
     * @param node
     *            Array node to convert
     * @param depth
     *            Current JSON depth
     * @return DynamoDB list representation of the array node
     * @throws JacksonConverterException
     *             JsonNode is not an array or depth is too great
     */
    private List<AttributeValue> jsonArrayToList(final JsonNode node, final int depth) throws JacksonConverterException {
        assertDepth(depth);
        if (node != null && node.isArray()) {
            final List<AttributeValue> result = new ArrayList<AttributeValue>();
            final Iterator<JsonNode> children = node.elements();
            while (children.hasNext()) {
                final JsonNode child = children.next();
                result.add(getAttributeValue(child, depth));
            }
            return result;
        }
        throw new JacksonConverterException("Expected JSON array, but received " + node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, AttributeValue> jsonObjectToMap(final JsonNode node) throws JacksonConverterException {
        return jsonObjectToMap(node, 0);
    }

    /**
     * Transforms a JSON object to a DynamoDB object.
     *
     * @param node
     *            JSON object
     * @param depth
     *            Current JSON depth
     * @return DynamoDB object representation of JSON
     * @throws JacksonConverterException
     *             JSON is not an object or depth is too great
     */
    private Map<String, AttributeValue> jsonObjectToMap(final JsonNode node, final int depth)
        throws JacksonConverterException {
        assertDepth(depth);
        if (node != null && node.isObject()) {
            final Map<String, AttributeValue> result = new HashMap<String, AttributeValue>();
            final Iterator<String> keys = node.fieldNames();
            while (keys.hasNext()) {
                final String key = keys.next();
                result.put(key, getAttributeValue(node.get(key), depth + 1));
            }
            return result;
        }
        throw new JacksonConverterException("Expected JSON Object, but received " + node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonNode listToJsonArray(final List<AttributeValue> item) throws JacksonConverterException {
        return listToJsonArray(item, 0);
    }

    /**
     * Converts a DynamoDB list to a JSON list.
     *
     * @param item
     *            DynamoDB list
     * @param depth
     *            Current JSON depth
     * @return JSON array node representation of DynamoDB list
     * @throws JacksonConverterException
     *             Null DynamoDB list or JSON too deep
     */
    private JsonNode listToJsonArray(final List<AttributeValue> item, final int depth) throws JacksonConverterException {
        assertDepth(depth);
        if (item != null) {
            final ArrayNode node = JsonNodeFactory.instance.arrayNode();
            for (final AttributeValue value : item) {
                node.add(getJsonNode(value, depth + 1));
            }
            return node;
        }
        throw new JacksonConverterException("Item cannot be null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonNode mapToJsonObject(final Map<String, AttributeValue> item) throws JacksonConverterException {
        return mapToJsonObject(item, 0);
    }

    /**
     * Converts a DynamoDB object to a JSON map.
     *
     * @param item
     *            DynamoDB object
     * @param depth
     *            Current JSON depth
     * @return JSON map representation of the DynamoDB object
     * @throws JacksonConverterException
     *             Null DynamoDB object or JSON too deep
     */
    private JsonNode mapToJsonObject(final Map<String, AttributeValue> item, final int depth)
        throws JacksonConverterException {
        assertDepth(depth);
        if (item != null) {
            final ObjectNode node = JsonNodeFactory.instance.objectNode();

            for (final Entry<String, AttributeValue> entry : item.entrySet()) {
                node.put(entry.getKey(), getJsonNode(entry.getValue(), depth + 1));
            }
            return node;
        }
        throw new JacksonConverterException("Item cannot be null");
    }

}
