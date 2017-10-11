package com.adaptris.core.json.jdbc;

import java.sql.Connection;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.json.JsonUtil;
import com.adaptris.core.services.splitter.json.LargeJsonArraySplitter;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.JdbcUtil;
import com.adaptris.core.util.LoggingHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Convenience service for inserting a JSON array into a database.
 * 
 * <p>
 * This creates insert statements based on the contents of each JSON object inside the array. A new insert statement will generated
 * for each JSON object in the array. You will get better performance from {@link BatchInsertJsonArray}; use this if the fields in
 * the JSON object can change but you need to insert into the same table...
 * </p>
 * <pre>
 * {@code 
 * [
 *   { "firstname":"alice", "lastname":"smith", "dob":"2017-01-01" },
 *   { "firstname":"bob", "lastname":"smith", "dob":"2017-01-02" },
 *   { "firstname":"carol", "lastname":"smith", "dob":"2017-01-03" }
 * ]}
 * </pre>
 * will effectively execute the following statement {@code INSERT INTO table (firstname,lastname,dob) VALUES (?,?,?)} 3 times with
 * no batching.
 * </p>
 * <p>
 * Note that no parsing/assertion of the column names will be done, so if they are invalid SQL columns then it's going to be
 * fail. Additionally, nested JSON objects will be rendered as strings before being passed into the appropriate statement; so
 * {@code { "firstname":"alice", "lastname":"smith", "address": { "address" : "Buckingham Palace", "postcode":"SW1A 1AA"}}} would
 * still be 3 parameters, the address parameter will be {@code '{ "address" : "Buckingham Palace", "postcode":"SW1A 1AA"}'}
 * </p>
 * 
 * @config json-array-jdbc-insert
 * @since 3.6.5
 *
 */
@AdapterComponent
@ComponentProfile(summary = "Insert a JSON array into a database", tag = "service,json,jdbc")
@XStreamAlias("json-array-jdbc-insert")
@DisplayOrder(order = {"table"})
public class InsertJsonArray extends InsertJsonObject {

  public InsertJsonArray() {

  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    Connection conn = null;
    try {
      log.trace("Beginning doService in {}", LoggingHelper.friendlyName(this));
      conn = getConnection(msg);
      // Use the already existing LargeJsonArraySplitter, but force it with a default-mf
      LargeJsonArraySplitter splitter =
          new LargeJsonArraySplitter().withMessageFactory(AdaptrisMessageFactory.getDefaultInstance());
      for (AdaptrisMessage m : splitter.splitMessage(msg)) {
        handleInsert(conn, JsonUtil.mapifyJson(m));
      }
      commit(conn, msg);
    } catch (Exception e) {
      rollback(conn, msg);
      throw ExceptionHelper.wrapServiceException(e);
    } finally {
      JdbcUtil.closeQuietly(conn);
    }
  }
}
