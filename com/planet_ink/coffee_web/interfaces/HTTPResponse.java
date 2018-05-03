package com.planet_ink.coffee_web.interfaces;

/*
   Copyright 2012-2018 Bo Zimmerman

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
	public void setStatusCode(int httpStatusCode);
	
	/**
	 * Sets the response header to specified value 
	 * 
	 * @param name The parameter name
	 * @param value The parameter value
	 */
	public void setHeader(String name, String value);
	
	/**
	 * Sets the cookie to specified value 
	 * 
	 * @param name The parameter name
	 * @param value The parameter value
	 */
	public void setCookie(String name, String value);
	
	/**
	 * Sets the mime type to be returned to the client
	 * 
	 * @param mimeType The mime type to set
	 */
	public void setMimeType(String mimeType);
}
