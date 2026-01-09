package com.planet_ink.coffee_web.http;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import com.planet_ink.coffee_web.interfaces.HTTPResponse;

/*
   Copyright 2025-2026 Bo Zimmerman

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
 * This class is instantiated as an means for requests to alter their output.
 * It puts together certain http defaults, but allows those defaults to be mangled
 * at will by request processors.
 *
 * See the interface for more comment
 * @author Bo Zimmerman
 *
 */
public class HTTPReqResponse implements HTTPResponse
{
	protected HTTPStatus				statusCode 	= HTTPStatus.S200_OK;
	protected final Map<String, String> headers;
	protected final Map<String,Cookie>	cookies;

	/**
	 * Construct a response object
	 */
	public HTTPReqResponse()
	{
		headers = new Hashtable<String, String>();
		cookies	= new Hashtable<String,Cookie>();
	}

	/**
	 * Construct a response object
	 */
	public HTTPReqResponse(final HTTPReqResponse base)
	{
		headers = base.headers;
		cookies = base.cookies;
		statusCode = base.statusCode;
	}

	@Override
	public HTTPStatus getStatus()
	{
		return statusCode;
	}

	@Override
	public void setStatusCode(final HTTPStatus httpStatusCode)
	{
		statusCode = httpStatusCode;
	}

	@Override
	public void setHeader(final String name, final String value)
	{
		headers.put(name, value);
	}

	@Override
	public void setMimeType(final String mimeType)
	{
		headers.put(HTTPHeader.Common.CONTENT_TYPE.toString(), mimeType);
	}

	@Override
	public void setCookie(final Cookie cookie)
	{
		if (cookie == null)
			return;
		cookies.put(cookie.name, cookie);
	}

	@Override
	public String getHeader(final String name)
	{
		return headers.get(name);
	}

	@Override
	public void removeHeader(final String name)
	{
		headers.remove(name);
	}

	@Override
	public Iterator<String> getHeaderNames()
	{
		return headers.keySet().iterator();
	}

	@Override
	public Cookie getCookie(final String name)
	{
		return cookies.get(name);
	}

	@Override
	public String getMimeType()
	{
		return headers.get(HTTPHeader.Common.CONTENT_TYPE.toString());
	}

	@Override
	public Map<HTTPHeader, String> getPopulatedHeaders()
	{
		final Map<HTTPHeader, String> H = new Hashtable<HTTPHeader, String>();
		if (getMimeType() != null)
			H.put(HTTPHeader.Common.CONTENT_TYPE, getMimeType());
		for (final String headKey : headers.keySet())
		{
			final HTTPHeader header = HTTPHeader.Common.findOrCreate(headKey);
			H.put(header, headers.get(headKey));
		}
		for (final String cookieName : cookies.keySet())
			H.put(HTTPHeader.Common.SET_COOKIE, cookies.get(cookieName).toString());
		return H;
	}
}
