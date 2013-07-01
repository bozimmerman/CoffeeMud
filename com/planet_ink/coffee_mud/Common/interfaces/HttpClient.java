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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/* 
   Copyright 2000-2013 Bo Zimmerman

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
 * A simple utility for making HTTP requests
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
	public HttpClient connectTimeout(int ms);
	public HttpClient reset();
	public HttpClient body(String body);
	public HttpClient body(byte[] body);
	public HttpClient header(String key, String value);
	public HttpClient method(Method meth);
	public HttpClient doGet(String url) throws IOException;
	public HttpClient doHead(String url) throws IOException;
	public HttpClient doRequest(String url) throws IOException;
	public Map<String,List<String>> getResponseHeaders();
	public int getResponseCode();
	public InputStream getResponseBody();
	public int getResponseContentLength();
	public void finished();
	
	/**
	 * HTTP Methods that are acceptable
	 * @author BZ
	 */
	public enum Method { GET, POST, HEAD }
}
