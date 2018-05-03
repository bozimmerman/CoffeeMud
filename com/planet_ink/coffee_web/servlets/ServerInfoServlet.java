package com.planet_ink.coffee_web.servlets;

import java.io.IOException;
import java.util.Arrays;

import com.planet_ink.coffee_web.http.HTTPMethod;
import com.planet_ink.coffee_web.http.HTTPStatus;
import com.planet_ink.coffee_web.http.MIMEType;
import com.planet_ink.coffee_web.interfaces.SimpleServlet;
import com.planet_ink.coffee_web.interfaces.SimpleServletRequest;
import com.planet_ink.coffee_web.interfaces.SimpleServletResponse;
import com.planet_ink.coffee_web.util.CWThread;
import com.planet_ink.coffee_web.util.CWConfig;

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
public class ServerInfoServlet implements SimpleServlet
{

	@Override
	public void doGet(SimpleServletRequest request, SimpleServletResponse response)
	{
		try
		{
			response.setMimeType(MIMEType.All.html.getType());
			response.getOutputStream().write("<html><body>".getBytes());
			if(Thread.currentThread() instanceof CWThread)
			{
				final CWConfig config=((CWThread)Thread.currentThread()).getConfig();
				response.getOutputStream().write("<table width=\"500\"><tr><td><b>Field</b></td><td>Value</td></tr>".getBytes());
				response.getOutputStream().write(("<tr><td>Bind address</td><td>"+config.getBindAddress()+"</td></tr>").getBytes());
				response.getOutputStream().write(("<tr><td>Debug flag</td><td>"+config.getDebugFlag()+"</td></tr>").getBytes());
				response.getOutputStream().write(("<tr><td>Default page</td><td>"+config.getDefaultPage()+"</td></tr>").getBytes());
				response.getOutputStream().write(("<tr><td>Error page</td><td>"+config.getErrorPage()+"</td></tr>").getBytes());
				response.getOutputStream().write(("<tr><td>Browse page</td><td>"+config.getBrowsePage()+"</td></tr>").getBytes());
				response.getOutputStream().write(("<tr><td>File cache expire ms</td><td>"+config.getFileCacheExpireMs()+"</td></tr>").getBytes());
				response.getOutputStream().write(("<tr><td>File cache max bytes</td><td>"+config.getFileCacheMaxBytes()+"</td></tr>").getBytes());
				response.getOutputStream().write(("<tr><td>File cache max file bytes</td><td>"+config.getFileCacheMaxFileBytes()+"</td></tr>").getBytes());
				response.getOutputStream().write(("<tr><td>File compression max bytes</td><td>"+config.getFileCompMaxFileBytes()+"</td></tr>").getBytes());
				response.getOutputStream().write(("<tr><td>Thread core pool size</td><td>"+config.getCoreThreadPoolSize()+"</td></tr>").getBytes());
				response.getOutputStream().write(("<tr><td>Thread max idle ms</td><td>"+config.getMaxThreadIdleMs()+"</td></tr>").getBytes());
				response.getOutputStream().write(("<tr><td>Thread max pool size</td><td>"+config.getMaxThreadPoolSize()+"</td></tr>").getBytes());
				response.getOutputStream().write(("<tr><td>Thread max queue size</td><td>"+config.getMaxThreadQueueSize()+"</td></tr>").getBytes());
				response.getOutputStream().write(("<tr><td>Thread max timeout secs</td><td>"+config.getMaxThreadTimeoutSecs()+"</td></tr>").getBytes());
				response.getOutputStream().write(("<tr><td>Request line buf size</td><td>"+config.getRequestLineBufBytes()+"</td></tr>").getBytes());
				response.getOutputStream().write(("<tr><td>Request max alive secs</td><td>"+config.getRequestMaxAliveSecs()+"</td></tr>").getBytes());
				response.getOutputStream().write(("<tr><td>Request max body size bytes</td><td>"+config.getRequestMaxBodyBytes()+"</td></tr>").getBytes());
				response.getOutputStream().write(("<tr><td>Request max idle ms</td><td>"+config.getRequestMaxIdleMs()+"</td></tr>").getBytes());
				response.getOutputStream().write(("<tr><td>SSL Key Encoding</td><td>"+config.getSslKeyManagerEncoding()+"</td></tr>").getBytes());
				//response.getOutputStream().write(("<tr><td> </td><td>"+config.getSslKeystorePassword()+"</td></tr>").getBytes());
				response.getOutputStream().write(("<tr><td>SSL Key path</td><td>"+config.getSslKeystorePath()+"</td></tr>").getBytes());
				response.getOutputStream().write(("<tr><td>SSL Keystore type</td><td>"+config.getSslKeystoreType()+"</td></tr>").getBytes());
				response.getOutputStream().write(("<tr><td>HTTP ports</td><td>"+Arrays.toString(config.getHttpListenPorts())+"</td></tr>").getBytes());
				response.getOutputStream().write(("<tr><td>HTTPS ports</td><td>"+Arrays.toString(config.getHttpsListenPorts())+"</td></tr>").getBytes());
				response.getOutputStream().write("</table>".getBytes());
			}
			response.getOutputStream().write("</body></html>".getBytes());
		}
		catch (final IOException e)
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
