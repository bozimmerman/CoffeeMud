package com.planet_ink.coffee_mud.Common.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/*
   Copyright 2013-2018 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
/**
 * A simple utility for making HTTP requests, which is
 * implemented as a builder-type object.
 */
public interface HttpClient extends Tickable, CMCommon
{
	/**
	 * Reads the simple raw return content from a given url and returns it as a
	 * byte array.  Good for getting files or web pages! Returns null if any
	 * error occurs, including a 404, timeouts, or read failures.
	 * @param urlStr the url to fetch
	 * @param maxLength the maximum size of the content, or 0 for any size
	 * @param readTimeout the maximum time, in ms, to wait for connects, and reads
	 * @return null or a completed byte array of the returned content
	 */
	public byte[] getRawUrl(final String urlStr, final int maxLength, final int readTimeout);

	/**
	 * Reads the simple raw return content from a given url and returns it as a
	 * byte array.  Good for getting files or web pages! Returns null if any
	 * error occurs, including a 404, timeouts, or read failures.
	 * @param urlStr the url to fetch
	 * @param cookieStr cookies to send, or "", or null for none
	 * @return null or a completed byte array of the returned content
	 */
	public byte[] getRawUrl(final String urlStr, String cookieStr);

	/**
	 * Reads the simple raw return content from a given url and returns it as a
	 * byte array.  Good for getting files or web pages! Returns null if any
	 * error occurs, including a 404, timeouts, or read failures.
	 * @param urlStr the url to fetch
	 * @return null or a completed byte array of the returned content
	 */
	public byte[] getRawUrl(final String urlStr);

	/**
	 * Reads the simple raw return content from a given url and returns it as a
	 * byte array.  Good for getting files or web pages! Returns null if any
	 * error occurs, including a 404, timeouts, or read failures.
	 * @param urlStr the url to fetch
	 * @param cookieStr cookies to send, or "", or null for none
	 * @param maxLength the maximum size of the content, or 0 for any size
	 * @param readTimeout the maximum time, in ms, to wait for connects, and reads
	 * @return null or a completed byte array of the returned content
	 */
	public byte[] getRawUrl(final String urlStr, String cookieStr, final int maxLength, final int readTimeout);

	/**
	 * Calls GET on the given url, waiting no more than a few seconds for connection,
	 * and returns the headers from the response.
	 * @param urlStr the url to GET
	 * @return the map of headers
	 */
	public Map<String,List<String>> getHeaders(final String urlStr);

	/**
	 * An http request builder method that sets the maximum number of
	 * bytes that can be read by the request processor.
	 * @param bytes max bytes to read, or 0 for no limit
	 * @return this
	 */
	public HttpClient maxReadBytes(int bytes);

	/**
	 * An http request builder method that sets the maximum number of
	 * milliseconds that the reader will remain idle waiting for
	 * a byte of data.
	 * @param ms the maximum number of ms to wait, or 0 for unlimited
	 * @return this
	 */
	public HttpClient readTimeout(int ms);

	/**
	 * An http request builder method that sets the maximum number of
	 * milliseconds that the reader will remain idle waiting for
	 * a connection to occur.
	 * @param ms the maximum number of ms to wait, or 0 for unlimited
	 * @return this
	 */
	public HttpClient connectTimeout(int ms);

	/**
	 * An http request builder method that resets the client obj
	 * so that the connection can be used for another request.
	 * @return this
	 */
	public HttpClient reset();

	/**
	 * An http request builder method that sets the body to send.
	 * @param body the body to send
	 * @return this
	 */
	public HttpClient body(String body);

	/**
	 * An http request builder method that sets the body to send.
	 * @param body the body to send
	 * @return this
	 */
	public HttpClient body(byte[] body);

	/**
	 * An http request builder method that adds a header.
	 * @param key the header name
	 * @param value the header value
	 * @return this
	 */
	public HttpClient header(String key, String value);

	/**
	 * An http request builder method that sets the http method
	 * @param meth the method
	 * @return this
	 */
	public HttpClient method(Method meth);

	/**
	 * An http request builder method that causes this request
	 * to occur as a GET
	 * @param url the url to use
	 * @return this
	 * @throws java.io.IOException a socket error
	 */
	public HttpClient doGet(String url) throws IOException;

	/**
	 * An http request builder method that causes this request
	 * to occur as a HEAD
	 * @param url the url to use
	 * @return this
	 * @throws java.io.IOException a socket error
	 */
	public HttpClient doHead(String url) throws IOException;

	/**
	 * An http request builder method that causes this request
	 * to occur.
	 * @param url the url to use
	 * @return this
	 * @throws java.io.IOException a socket error
	 */
	public HttpClient doRequest(String url) throws IOException;

	/**
	 * Returns the headers in the response to this request
	 * @return the headers in the response to this request
	 */
	public Map<String,List<String>> getResponseHeaders();

	/**
	 * Returns the http status code from the response
	 * @return the http status code from the response
	 */
	public int getResponseCode();

	/**
	 * Returns an input stream to the body of the response
	 * to this request.
	 * @return an input stream to the body of the response
	 */
	public InputStream getResponseBody();

	/**
	 * Gets the length of the body of the response to this
	 * request.
	 * @return the length of the body
	 */
	public int getResponseContentLength();

	/**
	 * Closes this connection, entirely.  Always call this
	 * in a finally block!
	 */
	public void finished();

	/**
	 * HTTP Methods that are acceptable
	 * @author Bo Zimmerman
	 */
	public enum Method { GET, POST, HEAD }
}
