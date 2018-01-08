package com.planet_ink.coffee_web.servlets;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import com.planet_ink.coffee_web.http.HTTPHeader;
import com.planet_ink.coffee_web.http.HTTPMethod;
import com.planet_ink.coffee_web.http.HTTPStatus;
import com.planet_ink.coffee_web.interfaces.SimpleServlet;
import com.planet_ink.coffee_web.interfaces.SimpleServletRequest;
import com.planet_ink.coffee_web.interfaces.SimpleServletResponse;

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
 * Returns information about your web server in a page
 * @author Bo Zimmerman
 *
 */
public class FormLoggerServlet implements SimpleServlet
{

	@Override
	public void doGet(SimpleServletRequest request, SimpleServletResponse response)
	{
		response.setStatusCode(HTTPStatus.S405_METHOD_NOT_ALLOWED.getStatusCode());
	}

	@Override
	public void doPost(SimpleServletRequest request, SimpleServletResponse response)
	{
		request.getLogger().info(" vvv-------------------- PayloadLogger ----------------------vvv");
		request.getLogger().info("Request: "+request.getFullRequest());
		for(String field : request.getUrlParameters())
			request.getLogger().info("Url Field \""+field+"\": "+request.getUrlParameter(field));
		int contentLength = 0;
		try {
			contentLength = Integer.parseInt(request.getHeader(HTTPHeader.Common.CONTENT_LENGTH.lowerCaseName()));
		} catch (Exception e) { }
		if(contentLength > 0) {
			try {
				Reader bodyReader = new InputStreamReader(request.getBody());
				char[] buf = new char[contentLength];
				bodyReader.read(buf);
				request.getLogger().info("Body: "+new String(buf));
			} catch (IOException e) { }
		}
		request.getLogger().info(" ^^^-------------------- PayloadLogger ----------------------^^^");
	}

	@Override
	public void init()
	{
	}

	@Override
	public void service(HTTPMethod method, SimpleServletRequest request, SimpleServletResponse response)
	{
		if(method!=HTTPMethod.POST)
			response.setStatusCode(HTTPStatus.S405_METHOD_NOT_ALLOWED.getStatusCode());
	}

}
