package com.adaptris.core.services.path.json;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.common.StringPayloadDataInputParameter;
import com.adaptris.core.common.StringPayloadDataOutputParameter;
import com.adaptris.core.json.JsonInsertExecution;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.config.DataInputParameter;
import com.adaptris.interlok.config.DataOutputParameter;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.WriteContext;
import com.jayway.jsonpath.spi.json.JsonSmartJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * This service allows you to insert a JSON value from metadata into
 * the message.
 * <p>
 * The searching works in much the same way as XPath, for more
 * information on how to build a JSON path see the
 * <a href="https://github.com/jayway/JsonPath">JSONPath</a>
 * documentation.
 * </p>
 * For example, if you have a message with the following payload:
 *
 * <pre>{@code
 * {
 *   "store": {
 *     "book": [ {
 *       "category": "reference",
 *       "author": "Nigel Rees",
 *       "price": 8.95
 *     }, {
 *       "category": "fiction",
 *       "author": "Evelyn Waugh",
 *       "price": 12.99
 *     }, {
 *       "category": "fiction",
 *       "author": "Herman Melville",
 *       "title": "Moby Dick",
 *       "isbn": "0-553-21311-3",
 *       "price": 8.99
 *     }, {
 *       "category": "fiction",
 *       "author": "J. R. R. Tolkien",
 *       "title": "The Lord of the Rings",
 *       "isbn": "0-395-19395-8",
 *       "price": 22.99
 *     } ],
 *     "bicycle": {
 *       "color": "red",
 *       "price": 19.95
 *     }
 *   },
 *   "expensive": 10
 * }
 * }</pre>
 * <p>
 * You could configure 2 target destinations, each one inserting the
 * metadata values into the specified path, like this:
 *
 * <pre>{@code
 * <json-path-service>
 *   <json-path-execution>
 *     <source class="metadata-data-input-parameter">
 *       <metadata-key>metadata-key-1</metadata-key>
 *     </source>
 *     <target class="constant-data-output-parameter">
 *       <value>$.store.book[0].title</value>
 *     </target>
 *   </json-path-execution>
 *   <json-path-execution>
 *     <source class="metadata-data-input-parameter">
 *       <metadata-key>metadata-key-2</metadata-key>
 *     </source>
 *     <target class="constant-data-output-parameter">
 *       <value>$.store.book[1].title</value>
 *     </target>
 *   </json-path-execution>
 * </json-path-service>
 * }</pre>
 * <p>
 * The first target above will set the first book title, the second
 * target will set the second book title. Each target-destination will
 * be executed in the order they are configured and therefore with the
 * two targets shown here, your message, after the service has run,
 * will have set the titles to what the metadata is:
 *
 * <ul>
 * <li>metadata-key-1 = "Sayings of the Century"</li>
 * <li>metadata-key-2 = "Sword of Honour"</li>
 * </ul>
 * </p>
 * <pre>{@code
 * {
 *   "store": {
 *     "book": [ {
 *       "category": "reference",
 *       "author": "Nigel Rees",
 *       "title": "Sayings of the Century",
 *       "price": 8.95
 *     }, {
 *       "category": "fiction",
 *       "author": "Evelyn Waugh",
 *       "title": "Sword of Honour",
 *       "price": 12.99
 *     }, {
 *       "category": "fiction",
 *       "author": "Herman Melville",
 *       "title": "Moby Dick",
 *       "isbn": "0-553-21311-3",
 *       "price": 8.99
 *     }, {
 *       "category": "fiction",
 *       "author": "J. R. R. Tolkien",
 *       "title": "The Lord of the Rings",
 *       "isbn": "0-395-19395-8",
 *       "price": 22.99
 *     } ],
 *     "bicycle": {
 *       "color": "red",
 *       "price": 19.95
 *     }
 *   },
 *   "expensive": 10
 * }
 * }</pre>
 *
 * @author aanderson
 * @config json-path-service
 */


public class JsonInsertService extends ServiceImp
{
	private static final transient Logger log = LoggerFactory.getLogger(ServiceImp.class.getName());

	@NotNull
	@AutoPopulated
	@AdvancedConfig
	private DataInputParameter<String> source = new StringPayloadDataInputParameter();

	@NotNull
	@AutoPopulated
	@AdvancedConfig
	private DataOutputParameter<String> target = new StringPayloadDataOutputParameter();

	@NotNull
	@Valid
	@AutoPopulated
	@XStreamImplicit
	private List<JsonInsertExecution> executions = new ArrayList<>();

	protected transient Configuration jsonConfig;

	public JsonInsertService()
	{
		super();
	}

	public JsonInsertService(DataInputParameter<String> source, List<JsonInsertExecution> executions)
	{
		this();
		setSource(source);
		setExecutions(executions);
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public void doService(AdaptrisMessage message) throws ServiceException
	{
		log.debug("Started service");
		try
		{
			String json = source.extract(message);
			log.trace("JSON document = " + json);
			WriteContext context = JsonPath.parse(json, jsonConfig);
			for (JsonInsertExecution execution : executions)
			{
				execute(execution, context, message);
			}
			json = context.jsonString();
			target.insert(json, message);
			log.trace("Update JSON = ", json);
		}
		catch (Exception e)
		{
			log.error("Could not insert value into JSON document", e);
			throw ExceptionHelper.wrapServiceException(e);
		}
		finally
		{
			log.debug("Finished service");
		}
	}

	private void execute(JsonInsertExecution execution, WriteContext context, AdaptrisMessage msg) throws InterlokException
	{
		/*
		 * You'd think that context.set() would work, but no.
		 * Hence getting the key from the final part of the path.
		 */
		String s = execution.getJsonPath().extract(msg);
		String path[] = s.split("\\.");
		String key = path[path.length - 1];
		StringBuilder sb = new StringBuilder();
		boolean dot = false;
		for (int i = 0; i < path.length - 1; i++)
		{
			if (dot)
			{
				sb.append('.');
			}
			sb.append(path[i]);
			dot = true;
		}
		JsonPath jsonPath = JsonPath.compile(sb.toString());
		String value = execution.getNewValue().extract(msg);
		log.trace("JSON path = " + jsonPath);
		log.trace("New key   = " + key);
		log.trace("New value = " + value);
		context.put(jsonPath, key, value);
	}

	@Override
	public void prepare()
	{
		jsonConfig = new Configuration.ConfigurationBuilder().jsonProvider(new JsonSmartJsonProvider())
				.mappingProvider(new JacksonMappingProvider()).options(EnumSet.noneOf(Option.class)).build();
	}

	@Override
	protected void closeService()
	{
		/* unused/empty method */
	}

	@Override
	protected void initService()
	{
		// nothing to do.
	}

	/**
	 * Get the source.
	 *
	 * @return The source.
	 */
	public DataInputParameter<String> getSource()
	{
		return source;
	}

	/**
	 * Set the source.
	 *
	 * @param source The source.
	 */
	public void setSource(final DataInputParameter<String> source)
	{
		this.source = Args.notNull(source, "Source");
	}

	/**
	 * Get the target.
	 *
	 * @return The target.
	 */
	public DataOutputParameter<String> getTarget()
	{
		return target;
	}

	/**
	 * Set the target.
	 *
	 * @param target The target.
	 */
	public void setTarget(final DataOutputParameter<String> target)
	{
		this.target = Args.notNull(target, "Target");
	}

	/**
	 * Get the list of execution.
	 *
	 * @return The list of executions.
	 */
	public List<JsonInsertExecution> getExecutions()
	{
		return executions;
	}

	/**
	 * Set the list of executions.
	 *
	 * @param executions The list of executions.
	 */
	public void setExecutions(final List<JsonInsertExecution> executions)
	{
		this.executions = Args.notNull(executions, "executions");
	}
}
