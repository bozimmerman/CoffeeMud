package com.planet_ink.coffee_web.converters;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.planet_ink.coffee_web.http.HTTPException;
import com.planet_ink.coffee_web.http.HTTPStatus;
import com.planet_ink.coffee_web.interfaces.HTTPOutputConverter;
import com.planet_ink.coffee_web.interfaces.HTTPRequest;
import com.planet_ink.coffee_web.server.WebServer;
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

public class CWHTMLConverter implements HTTPOutputConverter
{
	private static enum Macro 
	{ 
		HTTPSTATUS, 
		HTTPSTATUSINFO, 
		WEBSERVERVERSION, 
		WEBSERVERNAME, 
		WEBSERVERPORT,
		STARTFILELOOP,
		ENDFILELOOP,
		FILETYPE,
		FILENAME,
		FILESIZE,
		FILELASTMOD,
		FILEURLPATH,
		URLPATH
	}
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
	 * @param pageFile the file whose data is being converted
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
		int fileLoopCounter = -1;
		final File[] files = (pageFile.isDirectory() ? pageFile.listFiles() : null);
		if((files != null) && (files.length>0))
		{
			Arrays.sort(files,new Comparator<File>(){
				@Override
				public int compare(File arg0, File arg1) 
				{
					final boolean isDir0=arg0.isDirectory();
					final boolean isDir1=arg1.isDirectory();
					if(isDir0 && (!isDir1))
						return -1;
					if(isDir1 && (!isDir0))
						return 1;
					return arg0.getName().compareToIgnoreCase(arg1.getName());
				}
			});
		}
		int fileLoopBufferPosition = -1;
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
							case URLPATH:
								if(request != null)
									out.write((""+request.getUrlPath()).getBytes());
								break;
							case HTTPSTATUS:
								if(status != null)
									out.write((""+status.getStatusCode()).getBytes());
								break;
							case HTTPSTATUSINFO:
								if(status != null)
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
							case STARTFILELOOP:
								fileLoopCounter = 0;
								fileLoopBufferPosition = buffer.position();
								break;
							case ENDFILELOOP:
								fileLoopCounter++ ;
								if((files != null) && (fileLoopCounter >=0) && (fileLoopCounter < files.length))
									buffer.position(fileLoopBufferPosition);
								break;
							case FILETYPE:
								if((files != null) && (fileLoopCounter >=0) && (fileLoopCounter < files.length))
								{
									final File file=files[fileLoopCounter];
									if(file.isDirectory())
										out.write("directory".getBytes());
									else
										out.write("file".getBytes());
								}
								break;
							case FILENAME:
								if((files != null) && (fileLoopCounter >=0) && (fileLoopCounter < files.length))
								{
									final File file=files[fileLoopCounter];
									out.write(file.getName().getBytes());
								}
								break;
							case FILEURLPATH:
								if((files != null) && (fileLoopCounter >=0) && (fileLoopCounter < files.length))
								{
									final File file=files[fileLoopCounter];
									if(request != null)
										out.write((request.getUrlPath()+file.getName()).getBytes());
									if(file.isDirectory())
										out.write("/".getBytes());
								}
								break;
							case FILESIZE:
								if((files != null) && (fileLoopCounter >=0) && (fileLoopCounter < files.length))
								{
									final File file=files[fileLoopCounter];
									if(!file.isDirectory())
										out.write(Long.toString(file.length()).getBytes());
								}
								break;
							case FILELASTMOD:
								if((files != null) && (fileLoopCounter >=0) && (fileLoopCounter < files.length))
								{
									final File file=files[fileLoopCounter];
									out.write(SimpleDateFormat.getDateTimeInstance().format(new Date(file.lastModified())).getBytes());
								}
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
