package com.microsoft.azure.sdk.iot.service.messaging;

import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class MessageTest {
    protected static Charset UTF8 = StandardCharsets.UTF_8;

    @Test
    public void constructorSavesBody()
    {
        final byte[] body = { 1, 2, 3 };

        Message msg = new Message(body);
        byte[] testBody = msg.getBytes();

        assertThat(testBody, is(body));
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNullBody()
    {
        final byte[] body = null;

        new Message(body);
    }

    @Test
    public void getBodyAsStringReturnsUtf8Body()
    {
        final byte[] body = { 0x61, 0x62, 0x63 };

        Message msg = new Message(body);
        String testBody = new String(msg.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET);

        String expectedBody = new String(body, UTF8);
        assertThat(testBody, is(expectedBody));
    }

    @Test
    public void setPropertiesAndGetPropertiesMatch()
    {
        final byte[] body = { 0x61, 0x62, 0x63 };
        final String name = "test-name";
        final String value = "test-value1";

        Message msg = new Message(body);
        Map<String, String> property = new HashMap<>();
        property.put(name, value);
        msg.setProperties(property);

        Map<String, String> returnedProperties = msg.getProperties();
        String returnedValue = returnedProperties.get(name);

        assertThat(value, is(returnedValue));
    }

    @Test
    public void clearCustomPropertiesTest(){
        final byte[] body = { 0x61, 0x62, 0x63 };
        final String name = "test-name";
        final String value = "test-value1";
        final String deliveryAcknowledgementPropertyName = "iothub-ack";

        Message msg = new Message(body);
        Map<String, String> property = msg.getProperties();
        assertSame(1, property.size());
        Map<String, String> newProperty = new HashMap<>();
        newProperty.put(name, value);
        msg.setProperties(newProperty);
        assertSame(2, property.size());

        msg.clearCustomProperties();
        assertSame(1, property.size());
        assertNull(property.get(name));
        assertNotNull(property.get(deliveryAcknowledgementPropertyName));
    }

    @Test
    public void setExpiryTimeUtc()
    {
        final byte[] body = { 1, 2, 3 };
        Date ext = new Date(System.currentTimeMillis());
        Message msg = new Message(body);
        msg.setExpiryTimeUtc(ext);
        assertSame(msg.getExpiryTimeUtc(), ext);
    }
}
