package com.planet_ink.miniweb.http;

import com.planet_ink.miniweb.interfaces.HTTPIOHandler;

/*
Copyright 2012-2013 Bo Zimmerman

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
 * Class to help formally manage http headers, both those from the
 * client and those sent to it.  Headers defined herein also have methods
 * for easily constructing the "header line" used to output them.
 * @author Bo Zimmerman
 */
public enum HTTPHeader
{
	ACCEPT("Accept"),
	ACCEPT_ENCODING("Accept-Encoding"),
	ACCEPT_RANGES("Accept-Ranges","bytes"),
	ALLOW("Allow",HTTPMethod.getAllowedList()),
	CACHE_CONTROL("Cache-Control"),
	CONNECTION("Connection"),
	CONTENT_DISPOSITION("Content-Disposition"),
	CONTENT_ENCODING("Content-Encoding"),
	CONTENT_LENGTH("Content-Length"),
	CONTENT_RANGE("Content-Range"),
	CONTENT_TYPE("Content-Type"),
	COOKIE("Cookie"),
	DATE("Date"),
	ETAG("ETag"),
	EXPECT("Expect"),
	EXPIRES("Expires"),
	HOST("Host"),
	IF_MODIFIED_SINCE("If-Modified-Since"),
	IF_NONE_MATCH("If-None-Match"),
	KEEP_ALIVE("Keep-Alive"),
	LAST_MODIFIED("Last-Modified"),
	LOCATION("Location"),
	RANGE("Range"),
	SERVER("Server"),
	SET_COOKIE("Set-Cookie"),
	TRANSFER_ENCODING("Transfer-Encoding")
	;
	public static final String		 		KEEP_ALIVE_FMT	= "timeout=%d, max=%d";
	private static String					keepAliveHeader =KEEP_ALIVE_FMT;
	
	private static final String EOLN = HTTPIOHandler.EOLN;
	
	private String name;
	private String defaultValue;
	private String keyName;
	private HTTPHeader(String name, String defaultValue)
	{
		this.name=name;
		this.defaultValue=defaultValue;
		this.keyName=name.toLowerCase();
	}
	private HTTPHeader(String name)
	{
		this(name,"");
	}
	
	/**
	 * Return the right and good outputtable name of this header
	 * @return the disaplayable name
	 */
	public String toString()
	{
		return name;
	}
	/**
	 * Return the default value for this header, if one is defined, or ""
	 * @return the default value of this header
	 */
	public String getDefaultValue()
	{
		return defaultValue;
	}
	/**
	 * Return a lowercase form of this headers name as used in normalized map lookups
	 * @return lowercase name of this header
	 */
	public String lowerCaseName()
	{
		return keyName;
	}
	/**
	 * Return a header line with the given value
	 * @param value the value to assign to the header
	 * @return the name plus the value
	 */
	public String make(String value)
	{
		return name + ": " + value;
	}
	/**
	 * Return a header line with the given value
	 * and an end-of-line character attached
	 * @param value the value to assign to the header
	 * @return the name plus the value
	 */
	public String makeLine(String value)
	{
		return make(value) + EOLN;
	}
	/**
	 * Return a header line with the given value
	 * @param value the value to assign to the header
	 * @return the name plus the value
	 */
	public String make(int value)
	{
		return name + ": " + value;
	}
	/**
	 * Return a header line with the given value
	 * and an end-of-line character attached
	 * @param value the value to assign to the header
	 * @return the name plus the value
	 */
	public String makeLine(int value)
	{
		return make(value) + EOLN;
	}
	/**
	 * Set the static keep alive header from your configuration
	 * @param header
	 */
	public static void setKeepAliveHeader(String header)
	{
		HTTPHeader.keepAliveHeader=header;
	}
	/**
	 * Return the statically defined keep alive header line
	 * @return
	 */
	public static String getKeepAliveHeader()
	{
		return HTTPHeader.keepAliveHeader;
	}
}
