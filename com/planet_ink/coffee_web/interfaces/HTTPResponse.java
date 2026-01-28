package com.planet_ink.coffee_web.interfaces;

import java.util.Iterator;
import java.util.Map;

import com.planet_ink.coffee_web.http.Cookie;
import com.planet_ink.coffee_web.http.HTTPHeader;
import com.planet_ink.coffee_web.http.HTTPStatus;

/*
   Copyright 2015-2026 Bo Zimmerman

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
 * Basic interface for HTTP Response, with its headers and such
 * @author Bo Zimmerman
 *
 */
public interface HTTPResponse
{
	/**
	 * Set the response HTTP code
	 *
	 * @param httpStatusCode
	 */
	public void setStatusCode(HTTPStatus httpStatusCode);

	/**
	 * Gets the response HTTP code
	 *
	 * @return the response HTTP code
	 */
	public HTTPStatus getStatus();

	/**
	 * Sets the response header to specified value
	 *
	 * @param name The parameter name
	 * @param value The parameter value
	 */
	public void setHeader(String name, String value);

	/**
	 * Gets the response header value
	 *
	 * @param name The parameter name
	 * @return The parameter value
	 */
	public String getHeader(String name);

	/**
	 * Removes the response header
	 *
	 * @param name The parameter name
	 */
	public void removeHeader(String name);

	/**
	 * Gets an iterator over all the response header names
	 *
	 * @return an iterator over all the response header names
	 */
	public Iterator<String> getHeaderNames();

	/**
	 * Populates the final map with all current headers,
	 * including mime type, cookies, and other headers.
	 *
	 * @return the map populated
	 */
	public Map<HTTPHeader, String> getPopulatedHeaders();

	/**
	 * Gets the cookie value
	 *
	 * @param name The parameter name
	 * @return The parameter value
	 */
	public Cookie getCookie(String name);

	/**
	 * Sets the cookie to specified value
	 *
	 * @param cookie the cookie definition
	 */
	public void setCookie(Cookie name);

	/**
	 * Sets the mime type to be returned to the client
	 *
	 * @param mimeType The mime type to set
	 */
	public void setMimeType(String mimeType);

	/**
	 * Gets the mime type to be returned to the client
	 *
	 * @return The mime type
	 */
	public String getMimeType();
}
