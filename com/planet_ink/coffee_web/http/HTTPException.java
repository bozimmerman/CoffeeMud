package com.planet_ink.coffee_web.http;

import java.io.File;
import java.sql.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Logger;

import com.planet_ink.coffee_web.interfaces.DataBuffers;
import com.planet_ink.coffee_web.interfaces.HTTPIOHandler;
import com.planet_ink.coffee_web.interfaces.HTTPOutputConverter;
import com.planet_ink.coffee_web.interfaces.HTTPRequest;
import com.planet_ink.coffee_web.interfaces.ProtocolHandler;
import com.planet_ink.coffee_web.util.CWDataBuffers;
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
 * An http exception is a throwable class that represents some server response
 * that the client didn't expect or prefer, such as a 404, 403, etc..
 * They are constructed from HTTPStatus and then tossed up the chain where,
 * as an OutputGenerator, they can be used to generate their own full buffer
 * output for transmission to the client.
 * 
 * @author Bo Zimmerman
 *
 */
public class HTTPException extends Exception
{
	private static final long 	serialVersionUID = 9016082700560347737L;

	private final HTTPStatus 			status;		// status to return
	private final String 				body;		// optional body to send back with the error, usually ""
	private final Map<HTTPHeader,String>errorHeaders = new Hashtable<HTTPHeader,String>(); // any optional/extraneous headers to send back
	private final boolean				isDebugging;
	private final Logger				debugLogger;
	private final CWConfig				config;
	private final static String			EOLN		 = HTTPIOHandler.EOLN;
	private volatile ProtocolHandler 	protoHandler = null;
	
	/**
	 * Construct with body -- a strange case for now
	 * @param status HTTPStatus object 
	 * @param body the html body
	 */
	public HTTPException(HTTPStatus status, String body)
	{
		super(status.description());
		this.body=body;
		this.status = status;
		if(Thread.currentThread() instanceof CWThread)
		{
			this.config=((CWThread)Thread.currentThread()).getConfig();
			this.isDebugging = config.isDebugging();
			this.debugLogger=(isDebugging)?config.getLogger():null;
		}
		else
		{
			this.config=null;
			this.isDebugging=false;
			this.debugLogger=null;
		}
	}

	/**
	 * Construct new exception w/o a body
	 * @param status the HTTPStatus to return
	 */
	public HTTPException(HTTPStatus status)
	{
		super(status.description());
		this.status = status;
		this.body="";
		if(Thread.currentThread() instanceof CWThread)
		{
			this.config=((CWThread)Thread.currentThread()).getConfig();
			this.isDebugging = config.isDebugging();
			this.debugLogger=(isDebugging)?config.getLogger():null;
		}
		else
		{
			this.config=null;
			this.isDebugging=false;
			this.debugLogger=null;
		}
	}

	/**
	 * Get the status code object for this exception
	 * @return the status code object
	 */
	public HTTPStatus getStatus()
	{
		return status;
	}

	/**
	 * Get the extraneous headers for this exception
	 * For reading or adding-to
	 * @return headers map
	 */
	public Map<HTTPHeader, String> getErrorHeaders()
	{
		return errorHeaders;
	}
	
	/**
	 * When an exception is of type 101-switching protocols,
	 * this method allows the switcher to specify what the
	 * new protocol handler class will, in fact, be.
	 * @see HTTPException#getNewProtocolHandler()
	 * @param handler the new protocol handler
	 */
	public void setNewProtocolHandler(ProtocolHandler handler)
	{
		this.protoHandler = handler;
	}
	
	/**
	 * When an exception is of type 101-switching protocols,
	 * this method allows the switcher to specify what the
	 * new protocol handler class will, in fact, be.
	 * @see HTTPException#setNewProtocolHandler(ProtocolHandler)
	 * @return handler the new protocol handler
	 */
	public ProtocolHandler getNewProtocolHandler()
	{
		return this.protoHandler;
	}
	
	/**
	 * Like all HTTPIOHandlers, this class generates its own output buffer.
	 * The buffer generated here is typically sent straight to the socket.
	 * @param request the request to generate output for
	 * @return the client-writable, fully formed output buffer, ready to go
	 */
	public DataBuffers generateOutput(HTTPRequest request) throws HTTPException
	{
		final StringBuilder str = new StringBuilder("");
		str.append("HTTP/").append(request.getHttpVer()).append(" ").append(getStatus().getStatusCode()).append(" ").append(getMessage());
		final Map<HTTPHeader,String> headers=getErrorHeaders();
		str.append(EOLN);
		str.append(HTTPIOHandler.SERVER_HEADER);
		if(!headers.containsKey(HTTPHeader.Common.CONNECTION))
		{
			str.append(HTTPIOHandler.CONN_HEADER);
			str.append(HTTPHeader.Common.getKeepAliveHeader());
		}
		final long time = System.currentTimeMillis();
		final Date date=new Date(time);
		try
		{
			str.append(HTTPHeader.Common.DATE.makeLine(HTTPIOHandler.DATE_FORMAT.format(date)));
		}
		catch(java.lang.ArrayIndexOutOfBoundsException e)
		{
			try
			{
				str.append(HTTPHeader.Common.DATE.makeLine(HTTPIOHandler.DATE_FORMAT.format(new Date(System.currentTimeMillis()))));
			}
			catch(java.lang.ArrayIndexOutOfBoundsException e2)
			{
			}
		}
		if(isDebugging)
		{
			final StringBuilder dbgBuilder=new StringBuilder(str.toString().replace('\r', ',').replace('\n', ' ')); 
			for(HTTPHeader h : headers.keySet())
				dbgBuilder.append(h.toString()+": "+headers.get(h)+", ");
			debugLogger.finer("Response Exception: "+dbgBuilder.toString());
		}
		
		DataBuffers finalBody=null;
		if((body.length()==0)
		&&(status.isAnError())
		&&(config!=null)
		&&(config.getErrorPage().length()>0))
		{
			final File errorFile=config.getFileManager().createFileFromPath(config.getErrorPage());
			DataBuffers fileBytes=null;
			try
			{
				fileBytes=config.getFileCache().getFileData(errorFile, null);
				final MIMEType mimeType=MIMEType.All.getMIMEType(config.getErrorPage());
				if(mimeType!=null)
				{
					headers.put(HTTPHeader.Common.CONTENT_TYPE, mimeType.getType());
					final Class<? extends HTTPOutputConverter> converterClass=config.getConverters().findConverter(mimeType);
					if(converterClass != null)
					{
						final HTTPOutputConverter converter=converterClass.newInstance();
						finalBody=new CWDataBuffers(converter.convertOutput(config, request, errorFile, status, fileBytes.flushToBuffer()),0,true);
					}
					else
						finalBody=fileBytes;
				}
				else
					finalBody=fileBytes;
			}
			catch(final Exception e)
			{
				if(fileBytes!=null)
					fileBytes.close();
			}
		}
		if(finalBody==null)
		{
			finalBody=new CWDataBuffers(body.getBytes(), 0, true);
		}
		for(final HTTPHeader header : headers.keySet())
		{
			str.append(header.makeLine(headers.get(header)));
		}
		if(finalBody.getLength()>0)
		{
			if(!headers.containsKey(HTTPHeader.Common.CONTENT_TYPE.lowerCaseName()))
				str.append(HTTPHeader.Common.CONTENT_TYPE.makeLine(MIMEType.All.html.getType()));
			str.append(HTTPHeader.Common.CONTENT_LENGTH.makeLine(finalBody.getLength()));
		}
		else
			str.append(HTTPHeader.Common.CONTENT_LENGTH.makeLine(0));
		str.append(EOLN);
		finalBody.insertTop(str.toString().getBytes(), 0, false);
		return finalBody;
	}
	
	/**
	 * Simple cache to save memory and garbage collection time
	 */
	private static final Map<HTTPStatus,Map<CWConfig,HTTPException>> factoryExceptions=new Hashtable<HTTPStatus,Map<CWConfig,HTTPException>>();

	/**
	 * Static accessor for fetching completely standard-issue exceptions
	 * @param status the status of the exception to grab
	 * @return a standard issue exception
	 */
	public static final HTTPException standardException(final HTTPStatus status)
	{
		Map<CWConfig,HTTPException> xMap=factoryExceptions.get(status);
		final CWConfig config = ((CWThread)Thread.currentThread()).getConfig();
		if(xMap != null)
		{
			if((config != null)&&(xMap.containsKey(config)))
				return xMap.get(config);
		}
		else
		{
			xMap = new Hashtable<CWConfig,HTTPException>();
			factoryExceptions.put(status, xMap);
		}
		final HTTPException exception=new HTTPException(status);
		if(config != null)
		{
			xMap.put(config, exception);
		}
		return exception;
	}
}
