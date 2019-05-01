package com.adaptris.core.transform.json;

import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.transform.TransformServiceExample;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.text.xml.XPath;

public class JsonXmlTransformServiceTest extends TransformServiceExample {

  public JsonXmlTransformServiceTest(final String name) {
    super(name);
  }

  // Input/output for Default Xml -> JSON transformation
  static final String DEFAULT_XML_INPUT = "<xml><version>0.5</version>\n" + "" + "<entry>\n" + "<name>Production System</name>\n"
      + "<location>Seattle</location>\n" + "" + "</entry>\n" + "<entry>\n" + "<name>R&amp;D sandbox</name>\n"
      + "<location>New York</location>\n" + "</entry>\n" + "<notes>Some Notes</notes>\n</xml>";
  static final String JSON_OUTPUT =
      "{\"version\":\"0.5\",\"entry\":[{\"name\":\"Production System\",\"location\":\"Seattle\"},{\"name\":\"R&D sandbox\",\"location\":\"New York\"}],\"notes\":\"Some Notes\"}";

  // Input/output for Default and Simple JSON -> Xml transformation
  static final String JSON_INPUT =
      "{\n\"entry\":[\n" + "{\n\"location\":\"Seattle\"," + "\n\"name\":\"Production System\"},\n" + "{\"location\":\"New York\",\n"
          + "\"name\":\"R&D sandbox\"\n" + "}\n" + "],\n" + "\"notes\":\"Some Notes\",\n" + "\"version\":0.5\n" + "}";
  static final String DEFAULT_XML_OUTPUT =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<o><entry class=\"array\"><e class=\"object\">"
          + "<location type=\"string\">Seattle</location><name type=\"string\">Production System</name></e><e class=\"object\">"
          + "<location type=\"string\">New York</location><name type=\"string\">R&amp;D sandbox</name></e></entry><notes type=\"string\">Some Notes</notes>"
          + "<version type=\"number\">0.5</version></o>\r\n";
  static final String SIMPLE_XML_OUTPUT =
      "<json><entry><location>Seattle</location><name>Production System</name></entry><entry><location>New York</location>"
          + "<name>R&amp;D sandbox</name></entry><notes>Some Notes</notes><version>0.5</version></json>";

  // Input/output for Simple Xml -> JSON transformation
  static final String SIMPLE_XML_INPUT = "<json><version>0.5</version>\n" + "" + "<entry>\n" + "<name>Production System</name>\n"
      + "<location>Seattle</location>\n" + "" + "</entry>\n" + "<entry>\n" + "<name>R&amp;D sandbox</name>\n"
      + "<location>New York</location>\n" + "</entry>\n" + "<notes>Some Notes</notes>\n</json>";

  static final String SIMPLE_JSON_OUTPUT =
      "{\"entry\":[{\"location\":\"Seattle\",\"name\":\"Production System\"},{\"location\":\"New York\",\"name\":\"R&D sandbox\"}]"
          + ",\"notes\":\"Some Notes\",\"version\":0.5}";

  static final String ARRAY_JSON_INPUT =
      "[ { \"type\": \"Tfl.Api.Presentation.Entities.Line, Tfl.Api.Presentation.Entities\", " + "\"id\": \"victoria\", "
          + "\"name\": \"Victoria\", " + "\"modeName\": \"tube\", " + "\"created\": \"2015-07-23T14:35:19.787\", "
          + "\"modified\": \"2015-07-23T14:35:19.787\", " + "\"lineStatuses\": [], " + "\"routeSections\": [] }]";

  static final String BAD_JSON_INPUT =
      "[ { \"$type\": \"Tfl.Api.Presentation.Entities.Line, Tfl.Api.Presentation.Entities\", " + "\"id\": \"victoria\", "
          + "\"name\": \"Victoria\", " + "\"modeName\": \"tube\", " + "\"created\": \"2015-07-23T14:35:19.787\", "
          + "\"modified\": \"2015-07-23T14:35:19.787\", " + "\"lineStatuses\": [], " + "\"routeSections\": [] }]";

  public void testTransformToXml() throws Exception {
    final AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON_INPUT);
    final JsonXmlTransformService svc = new JsonXmlTransformService();
    svc.setDirection(TransformationDirection.JSON_TO_XML);
    execute(svc, msg);
    assertEquals(DEFAULT_XML_OUTPUT, msg.getContent());
    execute(svc, AdaptrisMessageFactory.getDefaultInstance().newMessage(ARRAY_JSON_INPUT));
  }

  public void testTransformToXml_StripIllegalXmlElement() throws Exception {
    final AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(BAD_JSON_INPUT);
    final JsonXmlTransformService svc = new JsonXmlTransformService();
    svc.setDirection(TransformationDirection.JSON_TO_XML);
    execute(svc, msg);
    System.err.println(msg.getContent());
  }

  public void testTransformToXml_ArrayNotObject() throws Exception {
    final AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(ARRAY_JSON_INPUT);
    final JsonXmlTransformService svc = new JsonXmlTransformService();
    svc.setDriver(new JsonObjectTransformationDriver());
    svc.setDirection(TransformationDirection.JSON_TO_XML);
    try {
      // Shouldn't parse because JsonArray input isn't valid.
      execute(svc, msg);
      fail();
    } catch (final ServiceException expected) {
    }
    svc.setDriver(new JsonArrayTransformationDriver());
    // This should be OK.
    execute(svc, msg);
  }

  public void testTransformToXml_ObjectNotArray() throws Exception {
    final AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON_INPUT);
    final JsonXmlTransformService svc = new JsonXmlTransformService();
    svc.setDriver(new JsonArrayTransformationDriver());
    svc.setDirection(TransformationDirection.JSON_TO_XML);
    try {
      execute(svc, msg);
      fail();
    } catch (final ServiceException expected) {
    }
    svc.setDriver(new JsonObjectTransformationDriver());
    // This should be OK.
    execute(svc, msg);
  }

  public void testTransformToJson() throws Exception {
    final AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(DEFAULT_XML_INPUT);

    final JsonXmlTransformService svc = new JsonXmlTransformService();
    svc.setDirection(TransformationDirection.XML_TO_JSON);
    execute(svc, msg);
    assertEquals(JSON_OUTPUT, msg.getContent());
  }

  public void testTransformJsonToSimpleXml() throws Exception {
    final AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON_INPUT);
    final JsonXmlTransformService svc = new JsonXmlTransformService();
    svc.setDirection(TransformationDirection.JSON_TO_XML);
    svc.setDriver(new SimpleJsonTransformationDriver());
    execute(svc, msg);
    doXmlAssertions(msg);
  }

  public void testTransformSimpleXmlToJson() throws Exception {
    final AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(SIMPLE_XML_INPUT);

    final JsonXmlTransformService svc = new JsonXmlTransformService();
    svc.setDirection(TransformationDirection.XML_TO_JSON);
    svc.setDriver(new SimpleJsonTransformationDriver());
    execute(svc, msg);
    doJsonAssertions(msg);
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new JsonXmlTransformService();
  }

  @Override
  protected String getExampleCommentHeader(final Object o) {
    return super.getExampleCommentHeader(o) + "\n<!-- \nThe example JSON input for this could be\n" + JSON_INPUT + "\n"
        + "\n\nThis will generate XML output that looks similar to this (without the formatting...):" + "\n\n" + DEFAULT_XML_INPUT
        + "\n-->\n";
  }

  public static void doXmlAssertions(AdaptrisMessage msg) throws Exception {
    Document d = XmlHelper.createDocument(msg);
    XPath xp = new XPath();
    assertEquals("Seattle", xp.selectSingleTextItem(d, "/json/entry[1]/location"));
    assertEquals("New York", xp.selectSingleTextItem(d, "/json/entry[2]/location"));
  }

  public static void doJsonAssertions(AdaptrisMessage msg) throws Exception {
    JSONObject obj = new JSONObject(msg.getContent());
    List<String> names = Arrays.asList(JSONObject.getNames(obj));
    assertTrue(names.contains("entry"));
    JSONArray array = obj.getJSONArray("entry");
    assertEquals(2, array.length());
    JSONObject seattle = (JSONObject) array.get(0);
    assertEquals("Seattle", seattle.getString("location"));
    JSONObject newyork = (JSONObject) array.get(1);
    assertEquals("New York", newyork.getString("location"));

  }

}
