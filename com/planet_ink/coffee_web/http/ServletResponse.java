package com.planet_ink.coffee_web.http;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.sql.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;

import com.planet_ink.coffee_web.http.HTTPException;
import com.planet_ink.coffee_web.http.HTTPStatus;
import com.planet_ink.coffee_web.http.MIMEType;
import com.planet_ink.coffee_web.interfaces.DataBuffers;
import com.planet_ink.coffee_web.interfaces.HTTPIOHandler;
import com.planet_ink.coffee_web.interfaces.HTTPRequest;
import com.planet_ink.coffee_web.interfaces.SimpleServletResponse;
import com.planet_ink.coffee_web.util.CWDataBuffers;
import com.planet_ink.coffee_web.util.CWThread;

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
 * This class is instantiated as an means for servlets to generate their output.
 * It puts together certain http defaults, but allows those defaults to be mangled
 * at will by the servlet.
 * As an Output Generator, it must have its generateOutput method called later
 * to get the results of all the servlet did to it.
 * 
 * See the interface for more comment
 * @author Bo Zimmerman
 *
 */
public class ServletResponse implements SimpleServletResponse
{
	private int		 					statusCode 	= HTTPStatus.S200_OK.getStatusCode();
	private String 						statusString= HTTPStatus.S200_OK.description();
	private final Map<String, String> 	headers 	= new Hashtable<String, String>();
	private final ByteArrayOutputStream	bout		= new ByteArrayOutputStream();
	private final Map<String,String>	cookies		= new Hashtable<String,String>();
	private static final String			EOLN		= HTTPIOHandler.EOLN;
	
	/**
	 * Construct a response object for servlets
	 */
	public ServletResponse()
	{
	}
	
	public int getStatusCode()
	{
		return statusCode;
	}
	
	@Override
	public void setStatusCode(int httpStatusCode)
	{
		statusCode = httpStatusCode;
		HTTPStatus status = HTTPStatus.find(httpStatusCode);
		if(status!=null)
			statusString = status.description();
		else
			statusString = "Unknown";
	}

	@Override
	public void setHeader(String name, String value)
	{
		headers.put(name, value);
	}

	@Override
	public void setMimeType(String mimeType)
	{
		headers.put(HTTPHeader.Common.CONTENT_TYPE.toString(), mimeType);
	}

	@Override
	public OutputStream getOutputStream()
	{
		return bout;
	}

	/**
	 * Generates a bytebuffer representing the results of the request 
	 * contained herein.  HTTP errors can still be generated, however,
	 * so those are watched for.
	 * 
	 * Requests can trigger file reads, servlet calls and other ways
	 * of generating body and header data.
	 * 
	 * @param request the request to generate output for
	 * @throws HTTPException
	 * @return the entire full output for this request
	 */
	public DataBuffers generateOutput(HTTPRequest request) throws HTTPException
	{
		StringBuilder str=new StringBuilder("");
		str.append("HTTP/").append(request.getHttpVer()).append(" ").append(statusCode).append(" ").append(statusString).append(EOLN);
		HashSet<String> normalizedHeaders = new HashSet<String>();
		for(String header : headers.keySet())
		{
			normalizedHeaders.add(header.toLowerCase());
			str.append(header).append(": ").append(headers.get(header)).append(EOLN);
		}
		// since the servlet could have overwridden ANY of our default headers, we make
		// sure to check a normalizsed set of servlet generated headers BEFORE writing
		// our own.
		if(bout.size()>0)
		{
			if(!normalizedHeaders.contains(HTTPHeader.Common.CONTENT_TYPE.lowerCaseName()))
				str.append(HTTPHeader.Common.CONTENT_TYPE.makeLine(MIMEType.All.html.getType()));
			if(!normalizedHeaders.contains(HTTPHeader.Common.CONTENT_LENGTH.lowerCaseName()))
				str.append(HTTPHeader.Common.CONTENT_LENGTH.makeLine(bout.size()));
		}
		if((Thread.currentThread() instanceof CWThread) && ((CWThread)Thread.currentThread()).getConfig().isDebugging())
			((CWThread)Thread.currentThread()).getConfig().getLogger().finer("Response Servlet: "+str.toString().replace('\r', ' ').replace('\n', ' '));
		if(!normalizedHeaders.contains(HTTPHeader.Common.SERVER.lowerCaseName()))
			str.append(HTTPIOHandler.SERVER_HEADER);
		if(!normalizedHeaders.contains(HTTPHeader.Common.CONNECTION.lowerCaseName()))
			str.append(HTTPIOHandler.CONN_HEADER);
		if(!normalizedHeaders.contains(HTTPHeader.Common.KEEP_ALIVE.lowerCaseName()))
			str.append(HTTPHeader.Common.getKeepAliveHeader());
		if(!normalizedHeaders.contains(HTTPHeader.Common.DATE.lowerCaseName()))
			str.append(HTTPHeader.Common.DATE.makeLine(HTTPIOHandler.DATE_FORMAT.format(new Date(System.currentTimeMillis()))));
		for(String key : cookies.keySet())
			str.append(HTTPHeader.Common.SET_COOKIE.makeLine(key+"="+cookies.get(key)));
		str.append(EOLN);
		CWDataBuffers bufs=new CWDataBuffers(str.toString().getBytes(), System.currentTimeMillis(),false);
		if(bout.size()>0)
		{
			final byte[] output=bout.toByteArray();
			bufs.add(output, System.currentTimeMillis(),true);
			try{ bout.flush();  bout.reset(); bout.close(); }catch(final Exception e){}
		}
		return bufs;
	}

	@Override
	public void setCookie(String name, String value)
	{
		cookies.put(name, value);
	}
}
