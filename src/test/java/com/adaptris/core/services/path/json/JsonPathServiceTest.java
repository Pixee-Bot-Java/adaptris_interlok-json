package com.adaptris.core.services.path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.ServiceException;
import com.adaptris.core.common.ConstantDataInputParameter;
import com.adaptris.core.common.Execution;
import com.adaptris.core.common.MetadataDataInputParameter;
import com.adaptris.core.common.MetadataDataOutputParameter;
import com.adaptris.core.common.StringPayloadDataInputParameter;
import com.adaptris.core.common.StringPayloadDataOutputParameter;
import com.adaptris.core.json.JsonPathExecution;
import com.adaptris.core.services.path.json.JsonPathService;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.spi.json.JsonSmartJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

public class JsonPathServiceTest extends ServiceCase {

  private static final String PATH_TO_ARRAY = "$.some_integers";
  private static final String PATH_NOT_FOUND = "$.path.not.found";
  private static final String JSON_PATH = "JsonPath";
  private static final String JSON_RESULT_KEY = "JsonResultKey";
  private static final String BASE_DIR_KEY = "JsonPathServiceExamples.baseDir";


  private static final String STORE_BOOK_0_TITLE = "$.store.book[0].title";
  private static final String SAYINGS_OF_THE_CENTURY = "Sayings of the Century";
  private static final String SWORD_OF_HONOUR = "Sword of Honour";
  private static final String STORE_BOOK_1_TITLE = "$.store.book[1].title";

  public JsonPathServiceTest(String name) {
    super(name);
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  AdaptrisMessage createMessage() throws Exception {
    return DefaultMessageFactory.getDefaultInstance().newMessage(sampleJsonContent());
  }

  public void testSimpleResultFromPayloadToMetadata() throws Exception {
    MetadataDataOutputParameter targetMetadataDestination = new MetadataDataOutputParameter(JSON_RESULT_KEY);

    ConstantDataInputParameter constantDataDestination = new ConstantDataInputParameter(STORE_BOOK_1_TITLE);

    JsonPathExecution execution = new JsonPathExecution(constantDataDestination, targetMetadataDestination);

    AdaptrisMessage message = createMessage();
    JsonPathService jsonPathService =
        new JsonPathService(new StringPayloadDataInputParameter(), Arrays.asList(new JsonPathExecution[] {execution}));

    execute(jsonPathService, message);

    assertTrue(message.headersContainsKey(JSON_RESULT_KEY));
    assertEquals(SWORD_OF_HONOUR, message.getMetadataValue(JSON_RESULT_KEY));
  }

  @SuppressWarnings("deprecation")
  public void testPathNotFound_Suppress_Execution() throws Exception {
    MetadataDataOutputParameter targetMetadataDestination = new MetadataDataOutputParameter(JSON_RESULT_KEY);

    ConstantDataInputParameter constantDataDestination = new ConstantDataInputParameter(PATH_NOT_FOUND);

    Execution execution = new Execution(constantDataDestination, targetMetadataDestination);

    AdaptrisMessage message = createMessage();
    JsonPathService jsonPathService =
        new JsonPathService(new StringPayloadDataInputParameter(), Arrays.asList(new Execution[] {execution}));
    jsonPathService.setSuppressPathNotFound(true);
    execute(jsonPathService, message);

    assertFalse(message.headersContainsKey(JSON_RESULT_KEY));
  }

  @SuppressWarnings("deprecation")
  public void testPathNotFound_NoSuppress_Execution() throws Exception {
    MetadataDataOutputParameter targetMetadataDestination = new MetadataDataOutputParameter(JSON_RESULT_KEY);

    ConstantDataInputParameter constantDataDestination = new ConstantDataInputParameter(PATH_NOT_FOUND);

    Execution execution = new Execution(constantDataDestination, targetMetadataDestination);

    AdaptrisMessage message = createMessage();
    JsonPathService jsonPathService =
        new JsonPathService(new StringPayloadDataInputParameter(), Arrays.asList(new Execution[] {execution}));
    try {
      execute(jsonPathService, message);
      fail();
    } catch (ServiceException expected) {

    }

    assertFalse(message.headersContainsKey(JSON_RESULT_KEY));
  }

  public void testPathNotFound_Suppress() throws Exception {
    MetadataDataOutputParameter targetMetadataDestination = new MetadataDataOutputParameter(JSON_RESULT_KEY);

    ConstantDataInputParameter constantDataDestination = new ConstantDataInputParameter(PATH_NOT_FOUND);

    JsonPathExecution execution = new JsonPathExecution(constantDataDestination, targetMetadataDestination);

    AdaptrisMessage message = createMessage();
    JsonPathService jsonPathService = new JsonPathService(new StringPayloadDataInputParameter(),
        Arrays.asList(new JsonPathExecution[]
        {
            execution
        }));
    execution.setSuppressPathNotFound(true);
    execute(jsonPathService, message);

    assertFalse(message.headersContainsKey(JSON_RESULT_KEY));
  }

  public void testPathNotFound_NoSuppress() throws Exception {
    MetadataDataOutputParameter targetMetadataDestination = new MetadataDataOutputParameter(JSON_RESULT_KEY);

    ConstantDataInputParameter constantDataDestination = new ConstantDataInputParameter(PATH_NOT_FOUND);

    JsonPathExecution execution = new JsonPathExecution(constantDataDestination, targetMetadataDestination);

    AdaptrisMessage message = createMessage();
    JsonPathService jsonPathService =
        new JsonPathService(new StringPayloadDataInputParameter(), Arrays.asList(new JsonPathExecution[] {execution}));
    try {
      execute(jsonPathService, message);
      fail();
    } catch (ServiceException expected) {

    }
  }

  public void testService_UnwrapJson() throws Exception {
    MetadataDataOutputParameter targetMetadataDestination = new MetadataDataOutputParameter(JSON_RESULT_KEY);

    ConstantDataInputParameter constantDataDestination = new ConstantDataInputParameter(PATH_TO_ARRAY);

    JsonPathExecution execution = new JsonPathExecution(constantDataDestination, targetMetadataDestination);

    AdaptrisMessage message = createMessage();
    JsonPathService jsonPathService =
        new JsonPathService(new StringPayloadDataInputParameter(), Arrays.asList(new JsonPathExecution[] {execution}));
    jsonPathService.setUnwrapJson(true);
      execute(jsonPathService, message);
    assertTrue(message.headersContainsKey(JSON_RESULT_KEY));
    assertEquals("1,2,3,4", message.getMetadataValue(JSON_RESULT_KEY));
  }

  public void testService_NoUnwrapJson() throws Exception {
    MetadataDataOutputParameter targetMetadataDestination = new MetadataDataOutputParameter(JSON_RESULT_KEY);

    ConstantDataInputParameter constantDataDestination = new ConstantDataInputParameter(PATH_TO_ARRAY);

    JsonPathExecution execution = new JsonPathExecution(constantDataDestination, targetMetadataDestination);

    AdaptrisMessage message = createMessage();
    JsonPathService jsonPathService =
        new JsonPathService(new StringPayloadDataInputParameter(), Arrays.asList(new JsonPathExecution[] {execution}));
    execute(jsonPathService, message);
    assertTrue(message.headersContainsKey(JSON_RESULT_KEY));
    assertEquals("[1,2,3,4]", message.getMetadataValue(JSON_RESULT_KEY));
  }


  public void testNotJson() throws Exception {
    MetadataDataOutputParameter targetMetadataDestination = new MetadataDataOutputParameter(JSON_RESULT_KEY);

    ConstantDataInputParameter constantDataDestination = new ConstantDataInputParameter(PATH_NOT_FOUND);

    JsonPathExecution execution = new JsonPathExecution(constantDataDestination, targetMetadataDestination);

    AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage("");
    JsonPathService jsonPathService =
        new JsonPathService(new StringPayloadDataInputParameter(), Arrays.asList(new JsonPathExecution[] {execution}));
    try {
      execute(jsonPathService, message);
      fail();
    } catch (ServiceException expected) {
      assertEquals(IllegalArgumentException.class, expected.getCause().getClass());
    }
  }

  public void testSimpleResultFromPayloadToMetadataUsingMetadataJsonPath() throws Exception {
    AdaptrisMessage message = createMessage();
    message.addMetadata(JSON_PATH, STORE_BOOK_1_TITLE);

    MetadataDataOutputParameter targetMetadataDestination = new MetadataDataOutputParameter(JSON_RESULT_KEY);

    MetadataDataInputParameter jsonMetadataDestination = new MetadataDataInputParameter(JSON_PATH);
    jsonMetadataDestination.setMetadataKey(JSON_PATH);

    JsonPathExecution execution = new JsonPathExecution(jsonMetadataDestination, targetMetadataDestination);
    JsonPathService jsonPathService =
        new JsonPathService(new StringPayloadDataInputParameter(), Arrays.asList(new JsonPathExecution[] {execution}));


    execute(jsonPathService, message);

    assertTrue(message.headersContainsKey(JSON_RESULT_KEY));
    assertEquals(SWORD_OF_HONOUR, message.getMetadataValue(JSON_RESULT_KEY));
  }

  public void testSimpleResultFromMetadataToPayload() throws Exception {
    MetadataDataInputParameter sourceMetadataDestination = new MetadataDataInputParameter(JSON_RESULT_KEY);

    StringPayloadDataOutputParameter targetPayloadDestination = new StringPayloadDataOutputParameter();

    ConstantDataInputParameter constantDataDestination = new ConstantDataInputParameter(STORE_BOOK_1_TITLE);

    JsonPathExecution execution = new JsonPathExecution(constantDataDestination, targetPayloadDestination);

    JsonPathService jsonPathService =
        new JsonPathService(sourceMetadataDestination, Arrays.asList(new JsonPathExecution[] {execution}));
    AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage();
    message.addMetadata(JSON_RESULT_KEY, sampleJsonContent());

    jsonPathService.setSource(sourceMetadataDestination);
    jsonPathService.setExecutions(Arrays.asList(new JsonPathExecution[] { execution }));
    execute(jsonPathService, message);

    assertEquals(SWORD_OF_HONOUR, message.getContent());
  }

  public void testSimpleResultFromMetadataToPayloadUsingMetadataJsonPath() throws Exception {
    AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage();

    message.addMetadata(JSON_PATH, STORE_BOOK_1_TITLE);
    message.addMetadata(JSON_RESULT_KEY, sampleJsonContent());

    MetadataDataInputParameter sourceMetadataDestination = new MetadataDataInputParameter(JSON_RESULT_KEY);

    MetadataDataInputParameter sourceJsonPathDestination = new MetadataDataInputParameter(JSON_PATH);

    JsonPathExecution execution = new JsonPathExecution(sourceJsonPathDestination, new StringPayloadDataOutputParameter());

    JsonPathService jsonPathService = new JsonPathService(sourceMetadataDestination, Arrays.asList(new JsonPathExecution[] {execution}));


    jsonPathService.setExecutions(Arrays.asList(new JsonPathExecution[] { execution }));
    jsonPathService.setSource(sourceMetadataDestination);

    execute(jsonPathService, message);

    assertEquals(SWORD_OF_HONOUR, message.getContent());
  }

  public void testSimpleResultFromPayloadToMultipleDestinations() throws Exception {
    MetadataDataOutputParameter targetMetadataDestination = new MetadataDataOutputParameter(JSON_RESULT_KEY);

    ConstantDataInputParameter constantDataDestination = new ConstantDataInputParameter(STORE_BOOK_0_TITLE);

    JsonPathExecution exec1 = new JsonPathExecution(constantDataDestination, targetMetadataDestination);

    JsonPathExecution exec2 = new JsonPathExecution(constantDataDestination, new StringPayloadDataOutputParameter());

    AdaptrisMessage message = createMessage();
    JsonPathService jsonPathService =
        new JsonPathService(new StringPayloadDataInputParameter(), Arrays.asList(new JsonPathExecution[] {exec1, exec2}));

    execute(jsonPathService, message);

    assertEquals(SAYINGS_OF_THE_CENTURY, message.getContent());
    assertEquals(SAYINGS_OF_THE_CENTURY, message.getMetadataValue(JSON_RESULT_KEY));
  }

  public void testSimpleResultFromPayloadToMultiplePayloadDestinations() throws Exception {
    ConstantDataInputParameter constantDataDestination1 = new ConstantDataInputParameter(STORE_BOOK_0_TITLE);
    ConstantDataInputParameter constantDataDestination2 = new ConstantDataInputParameter(STORE_BOOK_1_TITLE);

    JsonPathExecution exec1 = new JsonPathExecution(constantDataDestination1, new StringPayloadDataOutputParameter());

    JsonPathExecution exec2 = new JsonPathExecution(constantDataDestination2, new StringPayloadDataOutputParameter());


    AdaptrisMessage message = createMessage();
    JsonPathService jsonPathService =
        new JsonPathService(new StringPayloadDataInputParameter(), Arrays.asList(new JsonPathExecution[] {exec1, exec2}));

    execute(jsonPathService, message);

    assertEquals(SWORD_OF_HONOUR, message.getContent());
  }

  public void testComplexResultFromPayloadToPayload() throws Exception {
    ConstantDataInputParameter constantDataDestination = new ConstantDataInputParameter("$..book[?(@.isbn)]");

    JsonPathExecution execution = new JsonPathExecution(constantDataDestination, new StringPayloadDataOutputParameter());

    AdaptrisMessage message = createMessage();
    JsonPathService jsonPathService =
        new JsonPathService(new StringPayloadDataInputParameter(), Arrays.asList(new JsonPathExecution[] {execution}));

    execute(jsonPathService, message);

    Configuration jsonConfig = new Configuration.ConfigurationBuilder().jsonProvider(new JsonSmartJsonProvider())
        .mappingProvider(new JacksonMappingProvider()).options(EnumSet.noneOf(Option.class)).build();
    ReadContext context = JsonPath.parse(message.getInputStream(), jsonConfig);
    assertEquals("Herman Melville", context.read("$[0].author"));
    assertEquals("J. R. R. Tolkien", context.read("$[1].author"));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    JsonPathService jsonPathService = null;
    ConstantDataInputParameter constantDataDestination1 = new ConstantDataInputParameter(STORE_BOOK_0_TITLE);
    ConstantDataInputParameter constantDataDestination2 = new ConstantDataInputParameter(STORE_BOOK_1_TITLE);

    JsonPathExecution exec1 = new JsonPathExecution(constantDataDestination1, new MetadataDataOutputParameter("targetMetadataKey1"));

    JsonPathExecution exec2 = new JsonPathExecution(constantDataDestination2, new MetadataDataOutputParameter("targetMetadataKey2"));
    jsonPathService = new JsonPathService(new StringPayloadDataInputParameter(),
        new ArrayList<>(Arrays.asList(new JsonPathExecution[] {exec1, exec2})));
    return jsonPathService;
  }

  public static String sampleJsonContent() {
    return "{"
    + "\"store\": {"
    +    "\"book\": ["
    +        "{"
    +            "\"category\": \"reference\","
    +            "\"author\": \"Nigel Rees\","
    +            "\"title\": \"Sayings of the Century\","
    +            "\"price\": 8.95"
    +        "},"
    +        "{"
    +            "\"category\": \"fiction\","
    +            "\"author\": \"Evelyn Waugh\","
    +            "\"title\": \"Sword of Honour\","
    +            "\"price\": 12.99"
    +        "},"
    +        "{"
    +            "\"category\": \"fiction\","
    +            "\"author\": \"Herman Melville\","
    +            "\"title\": \"Moby Dick\","
    +            "\"isbn\": \"0-553-21311-3\","
    +            "\"price\": 8.99"
    +        "},"
    +        "{"
    +            "\"category\": \"fiction\","
    +            "\"author\": \"J. R. R. Tolkien\","
    +            "\"title\": \"The Lord of the Rings\","
    +            "\"isbn\": \"0-395-19395-8\","
    +            "\"price\": 22.99"
    +        "}"
    +    "],"
    +    "\"bicycle\": {"
    +        "\"color\": \"red\","
    +        "\"price\": 19.95"
    +    "}"
    + "},"
    + "\"expensive\": 10,"
    + "\"some_integers\" : [1,2,3,4]"
    + "}";
  }

  private String complexExpected() {
    return "["
        + "{\"author\":\"Herman Melville\",\"price\":8.99,\"isbn\":\"0-553-21311-3\",\"category\":\"fiction\",\"title\":\"Moby Dick\"},"
        + "{\"author\":\"J. R. R. Tolkien\",\"price\":22.99,\"isbn\":\"0-395-19395-8\",\"category\":\"fiction\",\"title\":\"The Lord of the Rings\"}"
        + "]";
//    return "["
//        + "{\"category\":\"fiction\",\"author\":\"Herman Melville\",\"title\":\"Moby Dick\",\"isbn\":\"0-553-21311-3\",\"price\":8.99},"
//        + "{\"category\":\"fiction\",\"author\":\"J. R. R. Tolkien\",\"title\":\"The Lord of the Rings\",\"isbn\":\"0-395-19395-8\",\"price\":22.99}"
//        + "]";
  }
}
