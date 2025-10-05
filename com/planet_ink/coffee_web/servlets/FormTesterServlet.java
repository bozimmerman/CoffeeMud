package com.planet_ink.coffee_web.servlets;

import java.io.IOException;

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
 * Purely for testing POST of form or urlencoded data
 * @author Bo Zimmerman
 *
 */
public class FormTesterServlet implements SimpleServlet
{
	@Override
	public void doGet(final SimpleServletRequest request, final SimpleServletResponse response)
	{
		response.setStatusCode(HTTPStatus.S405_METHOD_NOT_ALLOWED);
	}

	@Override
	public void doPost(final SimpleServletRequest request, final SimpleServletResponse response)
	{
		try
		{
			response.setMimeType(MIMEType.All.html.getType());
			response.getOutputStream().write("<html><body><h1>Form Field Values</h1><br>".getBytes());
			for(final String cookieName : request.getCookieNames())
				response.getOutputStream().write(("Cookie \""+cookieName+"\": "+request.getCookie(cookieName)+"<br>").getBytes());
			for(final String field : request.getUrlParameters())
				response.getOutputStream().write(("Url Field \""+field+"\": "+request.getUrlParameter(field)+"<br>").getBytes());
			response.getOutputStream().write("</body></html>".getBytes());
		}
		catch (final IOException e)
		{
			response.setStatusCode(HTTPStatus.S500_INTERNAL_ERROR);
		}
	}

	@Override
	public void init()
	{
	}

	@Override
	public void service(final HTTPMethod method, final SimpleServletRequest request, final SimpleServletResponse response)
	{
		if(method != HTTPMethod.POST)
			response.setStatusCode(HTTPStatus.S405_METHOD_NOT_ALLOWED);
	}

}
