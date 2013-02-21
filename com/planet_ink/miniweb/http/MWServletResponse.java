package com.planet_ink.miniweb.http;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.sql.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;

import com.planet_ink.miniweb.http.HTTPException;
import com.planet_ink.miniweb.http.HTTPHeader;
import com.planet_ink.miniweb.http.HTTPStatus;
import com.planet_ink.miniweb.http.MIMEType;
import com.planet_ink.miniweb.interfaces.DataBuffers;
import com.planet_ink.miniweb.interfaces.HTTPIOHandler;
import com.planet_ink.miniweb.interfaces.HTTPRequest;
import com.planet_ink.miniweb.interfaces.SimpleServletResponse;
import com.planet_ink.miniweb.util.MWDataBuffers;
import com.planet_ink.miniweb.util.MWThread;

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
public class MWServletResponse implements SimpleServletResponse
{
	private int		 				statusCode 	= HTTPStatus.S200_OK.getStatusCode();
	private String 					statusString= HTTPStatus.S200_OK.description();
	private Map<String, String> 	headers 	= new Hashtable<String, String>();
	private ByteArrayOutputStream	bout		= new ByteArrayOutputStream();
	private Map<String,String>		cookies		= new Hashtable<String,String>();
	private static final String		EOLN		= HTTPIOHandler.EOLN;
	
	/**
	 * Construct a response object for servlets
	 */
	public MWServletResponse()
	{
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
		headers.put(HTTPHeader.CONTENT_TYPE.toString(), mimeType);
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
			if(!normalizedHeaders.contains(HTTPHeader.CONTENT_TYPE.lowerCaseName()))
				str.append(HTTPHeader.CONTENT_TYPE.makeLine(MIMEType.html.getType()));
			if(!normalizedHeaders.contains(HTTPHeader.CONTENT_LENGTH.lowerCaseName()))
				str.append(HTTPHeader.CONTENT_LENGTH.makeLine(bout.size()));
		}
		if((Thread.currentThread() instanceof MWThread) && ((MWThread)Thread.currentThread()).getConfig().isDebugging())
			((MWThread)Thread.currentThread()).getConfig().getLogger().fine("Response Servlet: "+str.toString().replace('\r', ' ').replace('\n', ' '));
		if(!normalizedHeaders.contains(HTTPHeader.SERVER.lowerCaseName()))
			str.append(HTTPIOHandler.SERVER_HEADER);
		if(!normalizedHeaders.contains(HTTPHeader.CONNECTION.lowerCaseName()))
			str.append(HTTPIOHandler.CONN_HEADER);
		if(!normalizedHeaders.contains(HTTPHeader.KEEP_ALIVE.lowerCaseName()))
			str.append(HTTPHeader.getKeepAliveHeader());
		if(!normalizedHeaders.contains(HTTPHeader.DATE.lowerCaseName()))
			str.append(HTTPHeader.DATE.makeLine(HTTPIOHandler.DATE_FORMAT.format(new Date(System.currentTimeMillis()))));
		for(String key : cookies.keySet())
			str.append(HTTPHeader.SET_COOKIE.makeLine(key+"="+cookies.get(key)));
		str.append(EOLN);
		MWDataBuffers bufs=new MWDataBuffers(str.toString().getBytes(), System.currentTimeMillis());
		if(bout.size()>0)
		{
			byte[] output=bout.toByteArray();
			bufs.add(output, System.currentTimeMillis());
			try{ bout.flush();  bout.reset(); bout.close(); }catch(Exception e){}
		}
		return bufs;
	}

	@Override
	public void setCookie(String name, String value)
	{
		cookies.put(name, value);
	}
}
