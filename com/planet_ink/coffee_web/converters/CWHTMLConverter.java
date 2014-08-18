package com.planet_ink.coffee_web.converters;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import com.planet_ink.coffee_web.http.HTTPException;
import com.planet_ink.coffee_web.http.HTTPStatus;
import com.planet_ink.coffee_web.interfaces.HTTPOutputConverter;
import com.planet_ink.coffee_web.interfaces.HTTPRequest;
import com.planet_ink.coffee_web.server.WebServer;
import com.planet_ink.coffee_web.util.CWConfig;

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

public class CWHTMLConverter implements HTTPOutputConverter
{
	private static enum Macro { HTTPSTATUS, HTTPSTATUSINFO, WEBSERVERVERSION, WEBSERVERNAME, WEBSERVERPORT }
	private static Map<String,Macro> macros=new HashMap<String,Macro>();
	static
	{
		for(final Macro m : Macro.values())
			macros.put(m.toString().toLowerCase(), m);
	}
	
	
	/**
	 * Standard method for converting an input buffer for writing to 
	 * the client.   The position and limit of the bytebuffer must
	 * already be set for reading the content.
	 * Call generateOutput() to get the new output.
	 * @param config the http configuration (optional, may be null)
	 * @param request the http request bring processed  (optional, may be null)
	 * @param status the status of the request (so far)
	 * @param buffer the input buffer
	 * @throws HTTPException
	 */
	@Override
	public ByteBuffer convertOutput(CWConfig config, HTTPRequest request, File pageFile, HTTPStatus status, ByteBuffer buffer) throws HTTPException
	{
		final int oldPosition=buffer.position();
		final ByteArrayOutputStream out=new ByteArrayOutputStream();
		int state=-1;
		final StringBuilder macro=new StringBuilder("");
		while(buffer.remaining()>0)
		{
			final char c=(char)buffer.get();
			if(c=='@')
			{
				if(state<0)
					state=buffer.position();
				else
				{
					final Macro m = macros.get(macro.toString().toLowerCase());
					try 
					{ 
						if(m != null)
						{
							switch(m)
							{
							case HTTPSTATUS:
								out.write((""+status.getStatusCode()).getBytes());
								break;
							case HTTPSTATUSINFO:
								out.write(status.description().getBytes());
								break;
							case WEBSERVERVERSION:
								out.write((""+WebServer.VERSION).getBytes());
								break;
							case WEBSERVERNAME:
								out.write((""+WebServer.NAME).getBytes());
								break;
							case WEBSERVERPORT:
								if(request != null)
									out.write((""+request.getClientPort()).getBytes()); 
								break;
							}
						}
						else
							out.write(("@"+macro.toString()+"@").getBytes()); 
					}
					catch(final Exception e){}
					state=-1;
					macro.setLength(0);
				}
			}
			else
			if(state<0)
				out.write(c);
			else
				macro.append(c);
		}
		final ByteBuffer output=ByteBuffer.wrap(out.toByteArray());
		buffer.position(oldPosition);
		return output;
	}
}
