package com.planet_ink.coffee_web.http;

import java.util.Hashtable;
import java.util.Map;

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
 * All the recognized and official HTTP Status codes,
 * along with a utility method or two.
 * 
 * @author Bo Zimmerman
 *
 */
public enum HTTPStatus
{
	S100_CONTINUE("Continue",100),
	S101_SWITCHING_PROTOCOLS("Switching Protocols",101),
	
	S200_OK("OK",200),
	S201_CREATED("Created",201),
	S202_ACCEPTED("Accepted",202),
	S203_NON_AUTHORITATIVE_INFORMATION("Non-Authoritative Information",203),
	S204_NO_CONTENT("No Content",204),
	S205_RESET_CONTENT("Reset Content",205),
	S206_PARTIAL_CONTENT("Partial Content",206),
	
	S300_MULTIPLE_CHOICES("Multiple Choices",300),
	S301_MOVED_PERMANENTLY("Moved Permanently",301),
	S302_FOUND("Found",302),
	S303_SEE_OTHER("See Other",303),
	S304_NOT_MODIFIED("Not Modified",304),
	S305_USE_PROXY("Use Proxy",305),
	S306_UNUSED("UNUSED",306),
	S307_TEMPORARY_REDIRECT("Temporary Redirect",307),
	
	S400_BAD_REQUEST("Bad Request",400),
	S401_UNAUTHORIZED("Unauthorized",401),
	S402_PAYMENT_REQUIRED("Payment Required",402),
	S403_FORBIDDEN("Forbidden",403),
	S404_NOT_FOUND("Not Found",404),
	S405_METHOD_NOT_ALLOWED("Method Not Allowed",405),
	S406_NOT_ACCEPTABLE("Not Acceptable",406),
	S407_PROXY_AUTHENTICATION_REQUIRED("Proxy Authentication Required",407),
	S408_REQUEST_TIMEOUT("Request Timeout",408),
	S409_CONFLICT("Conflict",409),
	S410_GONE("Gone",410),
	S411_LENGTH_REQUIRED("Length Required",411),
	S412_PRECONDITION_FAILED("Precondition Failed",412),
	S413_REQUEST_ENTITY_TOO_LARGE("Request Entity Too Large",413),
	S414_REQUEST_URI_TOO_LONG("Request-URI Too Long",414),
	S415_UNSUPPORTED_MEDIA_TYPE("Unsupported Media Type",415),
	S416_REQUEST_RANGE_NOT_SATISFIED("Requested Range Not Satisfiable",416),
	S417_EXPECTATION_FAILED("Expectation Failed",417),
	
	S500_INTERNAL_ERROR("Internal Server Error",500),
	S501_NOT_IMPLEMENTED("Not Implemented",501),
	S502_BAD_GATEWAY("Bad Gateway",502),
	S503_SERVICE_UNAVAILABLE("Service Unavailable",503),
	S504_GATEWAY_TIMEOUT("Gateway Timeout",504),
	S505_HTTP_VERSION_NOT_SUPPORTED("HTTP Version Not Supported",505)
	;
	private static Map<Integer, HTTPStatus> codeMap = new Hashtable<Integer, HTTPStatus>();
	static
	{
		for(final HTTPStatus status : HTTPStatus.values())
			codeMap.put(Integer.valueOf(status.statusCode), status);
	}
	
	private String 	description;
	private int 	statusCode;
	private boolean isAnError;
	
	private HTTPStatus(String desc, int statusCode)
	{
		this.description = desc;
		this.statusCode = statusCode;
		this.isAnError = statusCode >=400;
	}
	
	/**
	 * Return whether this code is really an error
	 * @return true if its an error, false otherwise
	 */
	public boolean isAnError()
	{
		return isAnError;
	}
	
	/**
	 * Return the numeric part of the status code, such as the 200 in "200 OK"
	 * @return the numeric part of the status code
	 */
	public int getStatusCode()
	{
		return statusCode;
	}
	
	/**
	 * The HTTPStatus description is the block of text typically returned after the status
	 * code number in a response, such as the "OK" part of 200 OK
	 * @return a description of the status
	 */
	public String description()
	{
		return description;
	}
	/**
	 * Return the HTTPStatus object that matches the given code, or null if none found.
	 * @param statusCode an http status code
	 * @return the HTTPStatus object it maps to, or null
	 */
	public static HTTPStatus find(int statusCode)
	{
		return codeMap.get(Integer.valueOf(statusCode));
	}
}
