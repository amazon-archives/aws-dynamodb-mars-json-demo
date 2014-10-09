package com.amazonaws.services.dynamodbv2.json.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.amazonaws.services.dynamodbv2.json.converter.impl.JacksonStreamReaderImpl;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;

public class TestJacksonStreamReader {
    private static final String imageManifest = ClassLoader.getSystemResource("image_manifest_converter.json").getFile();
    private static final String imageManifestBroken = ClassLoader.getSystemResource("image_manifest_broken.json")
        .getFile();
    private static final String flickrPhoto = ClassLoader.getSystemResource("flickr.json").getFile();
    private JsonFactory jsonFactory;

    @Test
    public void getFirstSolMetadata() throws Exception {
        final JsonParser jp = jsonFactory.createJsonParser(new File(imageManifest));
        final JacksonStreamReaderImpl reader = new JacksonStreamReaderImpl(jp);
        // Seek for sols array
        assertTrue(reader.seek("sols"));

        final Map<String, AttributeValue> item = reader.getNextItem();
        assertEquals("1", item.get("sol").getN());
        assertEquals("150", item.get("num_images").getN());
        assertEquals("2004-01-30T08:11:15.222Z", item.get("most_recent_image").getS());
        assertEquals("2013-12-05T20:37:00.000Z", item.get("last_manifest_update").getS());
        assertEquals("http://merpublic.s3.amazonaws.com/oss/merb/images/images_sol1.json", item.get("url").getS());
    }

    @Test
    public void getLastSolMetadata() throws Exception {
        final JsonParser jp = jsonFactory.createJsonParser(new File(imageManifest));
        final JacksonStreamReaderImpl reader = new JacksonStreamReaderImpl(jp);
        // Seek for sols array
        assertTrue(reader.seek("sols"));

        Map<String, AttributeValue> item = null;
        Map<String, AttributeValue> next;
        while ((next = reader.getNextItem()) != null) {
            item = next;
        }
        assertEquals("3760", item.get("sol").getN());
        assertEquals("0", item.get("num_images").getN());
        assertEquals("1900", item.get("most_recent_image").getS());
        assertEquals("2014-08-22T19:06:50.000Z", item.get("last_manifest_update").getS());
        assertEquals("http://merpublic.s3.amazonaws.com/oss/merb/images/images_sol3760.json", item.get("url").getS());
        assertEquals(null, reader.getNextItem());
    }

    @Test
    public void loadBrokenJsonFile() throws Exception {
        final JsonParser jp = jsonFactory.createJsonParser(new File(imageManifestBroken));
        final JacksonStreamReaderImpl reader = new JacksonStreamReaderImpl(jp);
        // Seek for sols array
        assertTrue(reader.seek("sols"));

        final Map<String, AttributeValue> item = reader.getNextItem();
        // First item can be read correctly
        assertEquals("1", item.get("sol").getN());

        try {
            // Second one should fail because the second item is corrupted
            reader.getNextItem();
        } catch (final JsonParseException e) {
            // Expected behavior
        }
    }

    @Test
    public void loadEmptyArray() throws Exception {
        final JsonParser jp = jsonFactory.createJsonParser("[]");

        final JacksonStreamReaderImpl reader = new JacksonStreamReaderImpl(jp);
        final Map<String, AttributeValue> item = reader.getNextItem();

        assert (item == null);
    }

    @Test
    public void loadEmptyObject() throws Exception {
        final JsonParser jp = jsonFactory.createJsonParser("{}");

        final JacksonStreamReaderImpl reader = new JacksonStreamReaderImpl(jp);
        final Map<String, AttributeValue> item = reader.getNextItem();

        assertEquals(0, item.size());
    }

    @Test
    public void loadFlickrMetadata() throws Exception {
        final JsonParser jp = jsonFactory.createJsonParser(new File(flickrPhoto));

        final JacksonStreamReaderImpl reader = new JacksonStreamReaderImpl(jp);
        final Map<String, AttributeValue> item = reader.getNextItem();

        final ArrayList<AttributeValue> fans = new ArrayList<AttributeValue>();
        fans.add(new AttributeValue().withS("kentay"));

        assertEquals(new AttributeValue().withS("14911691861"), item.get("id"));
        assertEquals(new AttributeValue().withN("6"), item.get("farm"));
        assertEquals(new AttributeValue().withL(fans), item.get("fans"));
        assertEquals(new AttributeValue().withN("2.14735356869"), item.get("views-index"));
        assertEquals(new AttributeValue().withNULL(true), item.get("video"));
    }

    @Test
    public void loadImageManifestAsASingleItem() throws Exception {
        final JsonParser jp = jsonFactory.createJsonParser(new File(imageManifest));

        final JacksonStreamReaderImpl reader = new JacksonStreamReaderImpl(jp);
        final Map<String, AttributeValue> item = reader.getNextItem();
        assertEquals("mer-images-manifest-1.0", item.get("type").getS());
        assertEquals(3286, item.get("sols").getL().size());
    }

    @Before
    public void setup() {
        jsonFactory = new JsonFactory();
    }
}
