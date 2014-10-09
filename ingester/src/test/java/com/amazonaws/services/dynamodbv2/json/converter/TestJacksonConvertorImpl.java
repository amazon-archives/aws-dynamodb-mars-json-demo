package com.amazonaws.services.dynamodbv2.json.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.amazonaws.services.dynamodbv2.json.converter.impl.JacksonConverterImpl;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TestJacksonConvertorImpl {
	private static JacksonConverter convertor;
	private static final String testFile = ClassLoader.getSystemResource("flickr.json").getFile();

	@Test
	public void giveLoopedJsonNode() throws Exception {
		final ObjectNode node = JsonNodeFactory.instance.objectNode();

		node.put("child", node);

		try {
			convertor.jsonObjectToMap(node);
		} catch (final JacksonConverterException e){
			// expected behavior
			assert(e.getMessage().startsWith("Max depth reached."));
		}
	}


	@Test
	public void giveLoopedMap() throws Exception {
		final Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
		item.put("child", new AttributeValue().withM(item));


		try {
			convertor.mapToJsonObject(item);
		} catch (final JacksonConverterException e){
			// expected behavior
			assert(e.getMessage().startsWith("Max depth reached."));
		}
	}

	@Test
	public void giveWrongJsonNode() throws Exception {
		final ObjectMapper mapper = new ObjectMapper();
		final JsonNode jsonArray = mapper.readValue("[]", JsonNode.class);
		try {
			convertor.jsonObjectToMap(jsonArray);
		} catch (final JacksonConverterException e){
			// Correct behavior
		}
	}

	@Test
	public void itemToJsonObject() throws Exception {
		final ObjectMapper mapper = new ObjectMapper();
		final JsonNode json = mapper.readValue(new File(testFile), JsonNode.class);
		final Map<String, AttributeValue> item = convertor.jsonObjectToMap(json);

		final JsonNode node = convertor.mapToJsonObject(item);
		assertEquals("14911691861", node.get("id").textValue());
		assertEquals(6, node.get("farm").intValue());
		assertTrue(node.get("views-index").isDouble());
		assertTrue(node.get("fans").isArray());
		assertEquals(JsonNodeFactory.instance.nullNode(), node.get("video"));
	}

	@Test
	public void loadEmptyArray() throws Exception {
		final ObjectMapper mapper = new ObjectMapper();
		final JsonNode json = mapper.readValue("[]", JsonNode.class);

		final List<AttributeValue> item = convertor.jsonArrayToList(json);
		assertEquals(0, item.size());
	}

	@Test
	public void loadEmptyObject() throws Exception {
		final ObjectMapper mapper = new ObjectMapper();
		final JsonNode json = mapper.readValue("{}", JsonNode.class);

		final Map<String, AttributeValue> item = convertor.jsonObjectToMap(json);
		assertEquals(0, item.size());
	}

	@Before
	public void setup(){
		convertor = new JacksonConverterImpl();
	}

	@Test
	public void simpleJsonObjectToItem() throws Exception {
		final ObjectMapper mapper = new ObjectMapper();
		final JsonNode json = mapper.readValue(new File(testFile), JsonNode.class);

		final Map<String, AttributeValue> item = convertor.jsonObjectToMap(json);
		final ArrayList<AttributeValue> fans = new ArrayList<AttributeValue>();
		fans.add(new AttributeValue().withS("kentay"));

		assertEquals(new AttributeValue().withS("14911691861"), item.get("id"));
		assertEquals(new AttributeValue().withN("6"), item.get("farm"));
		assertEquals(new AttributeValue().withL(fans), item.get("fans"));
		assertEquals(new AttributeValue().withN("2.14735356869"), item.get("views-index"));
		assertEquals(new AttributeValue().withNULL(true), item.get("video"));
	}

}
