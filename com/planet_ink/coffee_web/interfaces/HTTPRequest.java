package com.planet_ink.coffee_web.interfaces;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.planet_ink.coffee_web.http.HTTPMethod;
import com.planet_ink.coffee_web.http.MultiPartData;

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
 * Basic interface for HTTP Request information, once its parsed
 * @author Bo Zimmerman
 *
 */
public interface HTTPRequest
{
	/**
	 * Gets the Host parameter as supplied by the client
	 * 
	 * @return The host 
	 */
	public String getHost();
	
	/** 
	 * Get the entire request line, including method, path, etc
	 * Returns null if the request line has not yet been received
	 * @return the entire request line, including method, path, etc
	 */
	public String getFullRequest();
	
	/**
	 * Gets the entire url path as supplied by the client, including request parameters
	 * 
	 * @return The host 
	 */
	public String getUrlPath();

	/**
	 * Gets a specific parameter as parsed from request url
	 * 
	 * @param name The parameter name
	 * @return The parameter value
	 */
	public String getUrlParameter(String name);

	/**
	 * Gets whether a specific parameter is from request url
	 * 
	 * @param name The parameter name
	 * @return true if the parameter exists, false otherwise
	 */
	public boolean isUrlParameter(String name);

	/**
	 * Gets the key fields from the url/form parms
	 * 
	 * @return The parameter names
	 */
	public Set<String> getUrlParameters();

	/**
	 * Gets the key fields from the url/form parms
	 * and their values as a copied map
	 * 
	 * @return The parameter names and values
	 */
	public Map<String,String> getUrlParametersCopy();
	
	/**
	 * The type of this request, or null if the request
	 * line has not yet been received
	 * @return the type of this request
	 */
	public HTTPMethod getMethod();

	/**
	 * Gets a request header as supplied by the client
	 * 
	 * @param name The header name
	 * @return The header value
	 */
	public String getHeader(String name);
	
	/**
	 * Gets the client's network address
	 * 
	 * @return The clients network address
	 */
	public java.net.InetAddress getClientAddress();
	
	/**
	 * Gets the client's connected-to port
	 * 
	 * @return The clients connected-to port
	 */
	public int getClientPort();
	
	/**
	 * Access the body of the request as an input stream
	 * @return the body of the request as an input stream.
	 */
	public InputStream getBody();
	
	/**
	 * Return the value of a cookie sent to the server in this request.
	 * @return the cookie value
	 */
	public String getCookie(String name);

	/**
	 * Return the query string, all the stuff in the request after the ?
	 * @return the query string
	 */
	public String getQueryString();
	
	/**
	 * Gets the key cookie names
	 * 
	 * @return The cookie names
	 */
	public Set<String> getCookieNames();
	
	/**
	 * Returns a list of multi-part sections from this request, or NULL
	 * if either the data was not in multi-part form, or was parsed another
	 * way.  Keep in mind that multipart/form-data is places into UrlParameters,
	 * along with urlencoded body data. 
	 * @return a list of multi-part sections from this request, or NULL
	 */
	public List<MultiPartData> getMultiParts();
	
	/**
	 * Returns whether the given encoding is acceptable, and to what
	 * extent.  If the value returned is 0, then the encoding given
	 * is NOT acceptable.  Any other value depends on the encoding.
	 * @param type an encoding type, such as compress or gzip
	 * @return the value of the coding that is acceptable
	 */
	public double getSpecialEncodingAcceptability(String type);
	
	/**
	 * Returns the FULL host as the requestor asked for it
	 * This is like https://blahblah.com:8080
	 * @return full host info
	 */
	public String getFullHost();
	
	/**
	 * If this is a ranged request, this will include a list of
	 * all ranges requested as integer arrays.  Each integer array
	 * can be 1 or 2 dimensional, with the first dimension always
	 * being "from" and the second (if available) the "to".
	 * @return a list of integer arrays for ranges requested
	 */
	public List<long[]> getRangeAZ();
	
	/**
	 * Adds a new url parameter keypair, just as if it had been sent by the client.
	 * Like the RequestObjects set, this can be used for inter-component
	 * communication, and would be reflected if getFullUrl is called.
	 * @param name the name of the parameter
	 * @param value the value of the parameter
	 */
	public void addFakeUrlParameter(String name, String value);
	
	/**
	 * Removes a url parameter, just as if it had never been sent by the client.
	 * Like the RequestObjects set, this can be used for inter-component
	 * communication, and would be reflected if getFullUrl is called.
	 * @param name the name of the parameter
	 */
	public void removeUrlParameter(String name);
	
	/**
	 * Gets a map of specific objects associated with this request.
	 * These are definitely for inter-servlet-component
	 * communication, but are not reflected in full urls,
	 * and don't appear as url parameters.
	 * 
	 * @return The objects map
	 */
	public Map<String,Object> getRequestObjects();
	
	/**
	 * Returns the http version of this request (1.0, 1.1, etc)
	 * @return the http version of this reques
	 */
	public float getHttpVer();
}
