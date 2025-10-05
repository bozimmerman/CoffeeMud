package com.planet_ink.coffee_web.servlets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import com.planet_ink.coffee_web.http.Cookie;
import com.planet_ink.coffee_web.http.HTTPMethod;
import com.planet_ink.coffee_web.http.HTTPStatus;
import com.planet_ink.coffee_web.http.MIMEType;
import com.planet_ink.coffee_web.interfaces.SimpleServlet;
import com.planet_ink.coffee_web.interfaces.SimpleServletRequest;
import com.planet_ink.coffee_web.interfaces.SimpleServletResponse;

/*
   Copyright 2012-2025 Bo Zimmerman

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
 * Tests both simple get request, and is also a fun way to examine
 * the cookies and form data posted to this servlet, as the data is
 * returned in the headers.
 * @author Bo Zimmerman
 *
 */
public class HelloWorldServlet implements SimpleServlet
{
	public static final String helloResponseStart="<html><body><h1>";
	public static final String defaultResponseBody="Hello World!";
	public static final String helloResponseEnd="</h1></body></html>";
	public static final String helloResponse=helloResponseStart+defaultResponseBody+helloResponseEnd;
	@Override
	public void doGet(final SimpleServletRequest request, final SimpleServletResponse response)
	{
		try
		{
			response.setMimeType(MIMEType.All.html.getType());
			response.getOutputStream().write(helloResponseStart.getBytes());
			response.getOutputStream().write(defaultResponseBody.getBytes());
			response.getOutputStream().write(helloResponseEnd.getBytes());
		}
		catch (final IOException e)
		{
			response.setStatusCode(HTTPStatus.S500_INTERNAL_ERROR);
		}
	}

	public void doDynamicPost(final SimpleServletRequest request, final SimpleServletResponse response)
	{
		try
		{
			response.setMimeType(MIMEType.All.html.getType());
			response.getOutputStream().write(helloResponseStart.getBytes());
			final InputStream in = request.getBody();
			final ByteArrayOutputStream bout=new ByteArrayOutputStream();
			int c;
			while((c=in.read())>=0)
				bout.write((byte)c);
			final String submitted=new String(bout.toByteArray(),Charset.forName("UTF-8"));
			if(submitted.length()==0)
				response.getOutputStream().write(defaultResponseBody.getBytes());
			else
				response.getOutputStream().write(submitted.getBytes());
			response.getOutputStream().write(helloResponseEnd.getBytes());
		}
		catch (final IOException e)
		{
			response.setStatusCode(HTTPStatus.S500_INTERNAL_ERROR);
		}
	}

	@Override
	public void doPost(final SimpleServletRequest request, final SimpleServletResponse response)
	{
		if(request.getHeader("X-DynamicPost")!=null)
			this.doDynamicPost(request, response);
		else
		{
			for(final String cookieName : request.getCookieNames())
				response.setCookie(new Cookie(cookieName, request.getCookie(cookieName)));
			for(final String field : request.getUrlParameters())
				response.setHeader("X-"+field, request.getUrlParameter(field));
			response.setStatusCode(HTTPStatus.S204_NO_CONTENT);
		}
	}

	@Override
	public void init()
	{
	}

	@Override
	public void service(final HTTPMethod method, final SimpleServletRequest request, final SimpleServletResponse response)
	{
		if((method != HTTPMethod.POST) && (method!=HTTPMethod.GET))
			response.setStatusCode(HTTPStatus.S405_METHOD_NOT_ALLOWED);
	}

}
