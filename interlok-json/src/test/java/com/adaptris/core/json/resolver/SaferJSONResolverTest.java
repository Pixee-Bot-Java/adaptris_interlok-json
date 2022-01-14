package com.adaptris.core.json.resolver;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.interlok.resolver.UnresolvableException;

public class SaferJSONResolverTest
{
	private static final String KEY = "greeting";
	private static final String GREETING = "Hello \"JSON resolver test\"";
	private static final String JSON_SOURCE = "{\n" +
			"  \"firstName\": \"John\",\n" +
			"  \"lastName\": \"Smith\",\n" +
			"  \"isAlive\": true,\n" +
			"  \"age\": 27,\n" +
			"  \"address\": {\n" +
			"    \"streetAddress\": \"21 2nd Street\",\n" +
			"    \"city\": \"New York\",\n" +
			"    \"state\": \"NY\",\n" +
			"    \"postalCode\": \"10021-3100\"\n" +
			"  },\n" +
			"  \"%message{key}\": \"%asJSONString{%message{" + KEY + "}}\"\n" +
			"}";
	private static final String JSON_RESOLVED = "{\n" +
			"  \"firstName\" : \"John\",\n" +
			"  \"lastName\" : \"Smith\",\n" +
			"  \"isAlive\" : true,\n" +
			"  \"age\" : 27,\n" +
			"  \"address\" : {\n" +
			"    \"streetAddress\" : \"21 2nd Street\",\n" +
			"    \"city\" : \"New York\",\n" +
			"    \"state\" : \"NY\",\n" +
			"    \"postalCode\" : \"10021-3100\"\n" +
			"  },\n" +
			"  \"greeting\" : \"Hello \\\"JSON resolver test\\\"\"\n" +
			"}";

	@Test
	public void testCanResolve()
	{
		assertTrue(new SaferJSONResolver().canHandle(JSON_SOURCE));
	}

	@Test
	public void testResolve()
	{
		AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
		message.addMetadata(KEY, GREETING);
		message.addMetadata("key", KEY);
		SaferJSONResolver resolver = new SaferJSONResolver();
		String result = resolver.resolve(JSON_SOURCE, message);
		JSONAssert.assertEquals(JSON_RESOLVED, result, false);
	}

	@Test
	public void testResolveMessageContent()
	{
		AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON_SOURCE);
		message.addMetadata(KEY, GREETING);
		message.addMetadata("key", KEY);
		SaferJSONResolver resolver = new SaferJSONResolver();
		String result = resolver.resolve(null, message);
		JSONAssert.assertEquals(JSON_RESOLVED, result, false);
	}

	@Test(expected = UnresolvableException.class)
	public void testResolveNoValue()
	{
		AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage();
		SaferJSONResolver resolver = new SaferJSONResolver();
		resolver.resolve(JSON_SOURCE, message);
	}

	@Test(expected = UnresolvableException.class)
	public void testNoMessage()
	{
		new SaferJSONResolver().resolve(JSON_SOURCE);
	}

	@Test(expected = UnresolvableException.class)
	public void testNullMessage()
	{
		new SaferJSONResolver().resolve(JSON_SOURCE, null);
	}

	@Test(expected = UnresolvableException.class)
	public void testNullExpression()
	{
		new SaferJSONResolver().resolve(null, null);
	}
}
