package com.adaptris.core.services.splitter.json;

import java.util.List;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.transform.json.JsonToXmlTransformServiceTest;

import com.adaptris.core.util.CloseableIterable;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

public class JsonMetadataSplitterTest extends SplitterServiceExample
{
	public JsonMetadataSplitterTest(final String name)
	{
		super(name);
	}

	public static final String PAYLOAD = "[\n"
			+ "{\"colour\": \"red\"},\n"
			+ "{\"colour\": \"green\"},\n"
			+ "{\"colour\": \"blue\"},\n"
			+ "{\"colour\": \"black\"}\n"
			+ "]";

	public void testSplitArray() throws Exception
	{
		final AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD);

		try (final CloseableIterable<AdaptrisMessage> i = createSplitter().splitMessage(message))
		{
			int count = 0;
			for (final AdaptrisMessage m : i)
			{
				switch (m.getMetadataValue("colour"))
				{
					case "red":
					case "green":
					case "blue":
					case "black":
						count++;
						break;
					default:
						fail();
				}
			}
			assertEquals(4, count);
		}
	}

	public void testSplitArrayWithMetadata() throws Exception
	{
		final AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage(PAYLOAD);
		message.addMetadata("a", "b");
		message.addMetadata("b", "c");

		try (final CloseableIterable<AdaptrisMessage> i = createSplitter().splitMessage(message))
		{
			int count = 0;
			for (final AdaptrisMessage m : i)
			{
				assertEquals("b", m.getMetadataValue("a"));
				assertEquals("c", m.getMetadataValue("b"));
				switch (m.getMetadataValue("colour"))
				{
					case "red":
					case "green":
					case "blue":
					case "black":
						count++;
						break;
					default:
						fail();
				}
			}
			assertEquals(4, count);
		}
	}

	public void testSplitEmptyArray() throws Exception
	{
		final AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage("[]");

		try (final CloseableIterable<AdaptrisMessage> i = createSplitter().splitMessage(message))
		{
			for (final AdaptrisMessage m : i)
			{
				fail("Was not expecting any split messages; received : " + m);
			}
		}
	}

	public void testSplitNotJson()
	{
		try
		{
			final AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage("hello world");

			createSplitter().splitMessage(message);
			fail();
		}
		catch (CoreException expected)
		{
			// expected behaviour
		}
	}

	@Override
	protected String getExampleCommentHeader(Object o)
	{
		return super.getExampleCommentHeader(o) + "\n<!-- \n If the incoming document is \n\n"
				+ JsonToXmlTransformServiceTest.JSON_INPUT
				+ "\n\nthis would create 3 new messages, 1 each for 'entry', 'notes', 'version'." + "\nIf the incoming document is \n\n"
				+ PAYLOAD + "\n\nthis would create 4 messages, 1 for each element" + "\n-->\n";
	}

	@Override
	JsonMetadataSplitter createSplitter()
	{
		return new JsonMetadataSplitter();
	}
}