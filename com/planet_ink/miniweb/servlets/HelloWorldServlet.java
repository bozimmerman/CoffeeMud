package com.planet_ink.miniweb.servlets;

import java.io.IOException;

import com.planet_ink.miniweb.http.HTTPMethod;
import com.planet_ink.miniweb.http.HTTPStatus;
import com.planet_ink.miniweb.http.MIMEType;
import com.planet_ink.miniweb.interfaces.SimpleServlet;
import com.planet_ink.miniweb.interfaces.SimpleServletRequest;
import com.planet_ink.miniweb.interfaces.SimpleServletResponse;

/*
Copyright 2012-2014 Bo Zimmerman

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
	public static final String helloResponse="<html><body><h1>Hello World</h1></body></html>";
	@Override
	public void doGet(SimpleServletRequest request, SimpleServletResponse response)
	{
		try
		{
			response.setMimeType(MIMEType.html.getType());
			response.getOutputStream().write(helloResponse.getBytes());
		}
		catch (IOException e)
		{
			response.setStatusCode(500);
		}
	}

	@Override
	public void doPost(SimpleServletRequest request, SimpleServletResponse response)
	{
		for(String cookieName : request.getCookieNames())
			response.setCookie(cookieName, request.getCookie(cookieName));
		for(String field : request.getUrlParameters())
			response.setHeader("X-"+field, request.getUrlParameter(field));
		response.setStatusCode(HTTPStatus.S204_NO_CONTENT.getStatusCode());
	}

	@Override
	public void init()
	{
	}

	@Override
	public void service(HTTPMethod method, SimpleServletRequest request, SimpleServletResponse response)
	{
		if((method != HTTPMethod.POST) && (method!=HTTPMethod.GET))
			response.setStatusCode(HTTPStatus.S405_METHOD_NOT_ALLOWED.getStatusCode());
	}

}
