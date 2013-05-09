package com.planet_ink.miniweb.http;

import java.io.InputStream;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.planet_ink.miniweb.interfaces.HTTPRequest;
import com.planet_ink.miniweb.interfaces.SimpleServletManager;
import com.planet_ink.miniweb.interfaces.SimpleServletRequest;
import com.planet_ink.miniweb.interfaces.SimpleServletSession;
import com.planet_ink.miniweb.util.MWThread;
import com.planet_ink.miniweb.util.MiniWebConfig;

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
 * This class is instantiated as an means for servlets to get input from the request.
 * It is generally a wrapper for the internal class HTTPRequest, but with more
 * guards against some idiosyncracies of that class.
 * 
 * See the interface for more comment
 * @author Bo Zimmerman
 *
 */
public class MWServletRequest implements SimpleServletRequest
{
	private final HTTPRequest 			request;
	private final MiniWebConfig			config;
	private final SimpleServletSession  session;
	
	/**
	 * Construct a servlet request input object
	 * @param session the session assigned to the request
	 * @param request the request being processed
	 */
	public MWServletRequest(SimpleServletSession session, HTTPRequest request)
	{
		this.request=request;
		if(Thread.currentThread() instanceof MWThread)
			this.config=((MWThread)Thread.currentThread()).getConfig();
		else
			this.config=null;
		this.session=session;
	}
	
	@Override
	public String getHost()
	{
		return request.getHost();
	}

	@Override
	public String getFullRequest()
	{
		return request.getFullRequest();
	}

	@Override
	public String getUrlPath()
	{
		return request.getUrlPath();
	}

	@Override
	public String getUrlParameter(String name)
	{
		return request.getUrlParameter(name);
	}

	@Override
	public Map<String,String> getUrlParametersCopy()
	{
		return request.getUrlParametersCopy();
	}
	
	@Override
	public boolean isUrlParameter(String name)
	{
		return request.isUrlParameter(name);
	}

	@Override
	public void addFakeUrlParameter(String name, String value)
	{
		request.addFakeUrlParameter(name, value);
	}
	
	@Override
	public String getHeader(String name)
	{
		return request.getHeader(name);
	}

	@Override
	public InetAddress getClientAddress()
	{
		return request.getClientAddress();
	}

	@Override
	public InputStream getBody()
	{
		return request.getBody();
	}

	@Override
	public SimpleServletManager getServletManager()
	{
		return (config!=null)?config.getServletMan():null;
	}
	
	@Override
	public String getCookie(String name)
	{
		return request.getCookie(name);
	}

	@Override
	public List<MultiPartData> getMultiParts()
	{
		return request.getMultiParts();
	}

	@Override
	public Set<String> getUrlParameters()
	{
		return request.getUrlParameters();
	}
	
	@Override 
	public void removeUrlParameter(String name)
	{
		request.removeUrlParameter(name);
	}
	
	@Override
	public Set<String> getCookieNames()
	{
		return request.getCookieNames();
	}
	
	/**
	 * Returns the session object associated with this servlet request
	 * @return the session object
	 */
	@Override
	public SimpleServletSession getSession()
	{
		return session;
	}

	@Override
	public double getSpecialEncodingAcceptability(String type)
	{
		return request.getSpecialEncodingAcceptability(type);
	}

	
	@Override
	public int getClientPort()
	{
		return request.getClientPort();
	}
	
	@Override
	public HTTPMethod getMethod()
	{
		return request.getMethod();
	}
	
	@Override
	public String getFullHost()
	{
		return request.getFullHost();
	}
	
	@Override
	public List<int[]> getRangeAZ()
	{
		return request.getRangeAZ();
	}
	@Override
	public Logger getLogger()
	{
		return config.getLogger();
	}

	@Override
	public Map<String,Object> getRequestObjects()
	{
		return request.getRequestObjects();
	}

	@Override
	public float getHttpVer()
	{
		return request.getHttpVer();
	}
}
