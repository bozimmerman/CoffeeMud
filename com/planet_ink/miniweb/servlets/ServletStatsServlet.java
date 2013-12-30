package com.planet_ink.miniweb.servlets;

import java.io.IOException;

import com.planet_ink.miniweb.http.HTTPMethod;
import com.planet_ink.miniweb.http.HTTPStatus;
import com.planet_ink.miniweb.http.MIMEType;
import com.planet_ink.miniweb.interfaces.SimpleServlet;
import com.planet_ink.miniweb.interfaces.SimpleServletRequest;
import com.planet_ink.miniweb.interfaces.SimpleServletResponse;
import com.planet_ink.miniweb.util.MWRequestStats;

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
 * Displays statistics kept about servlet calls and performance
 * @author Bo Zimmerman
 *
 */
public class ServletStatsServlet implements SimpleServlet
{

	private void appendStats(MWRequestStats stats, StringBuilder str)
	{
		str.append("Requests total: ").append(stats.getNumberOfRequests()).append("<br>");
		str.append("Average time (ns): ").append(stats.getAverageEllapsedNanos()).append("<br>");
		str.append("In progress: ").append(stats.getNumberOfRequestsInProcess()).append("<br>");
	}
	
	@Override
	public void doGet(SimpleServletRequest request, SimpleServletResponse response)
	{
		try
		{
			response.setMimeType(MIMEType.html.getType());
			StringBuilder str = new StringBuilder("");
			str.append("<html><body>");
			
			MWRequestStats stats;
			for(Class<? extends SimpleServlet> servletClass : request.getServletManager().getServlets())
			{
				stats = request.getServletManager().getServletStats(servletClass);
				str.append("<P><h2>"+servletClass.getSimpleName()+"</h2></p><br>");
				appendStats(stats, str);
			}
			str.append("</body></html>");
			response.getOutputStream().write(str.toString().getBytes());
		}
		catch (IOException e)
		{
			response.setStatusCode(500);
		}
	}

	@Override
	public void doPost(SimpleServletRequest request, SimpleServletResponse response)
	{
		response.setStatusCode(HTTPStatus.S405_METHOD_NOT_ALLOWED.getStatusCode());
	}

	@Override
	public void init()
	{
	}

	@Override
	public void service(HTTPMethod method, SimpleServletRequest request, SimpleServletResponse response)
	{
		if(method!=HTTPMethod.GET)
			response.setStatusCode(HTTPStatus.S405_METHOD_NOT_ALLOWED.getStatusCode());
	}

}
