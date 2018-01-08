package com.planet_ink.coffee_web.http;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Map;

import com.planet_ink.coffee_mud.core.collections.Pair;
import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.coffee_web.http.HTTPException;
import com.planet_ink.coffee_web.http.HTTPStatus;
import com.planet_ink.coffee_web.interfaces.HTTPIOHandler;
import com.planet_ink.coffee_web.interfaces.HTTPOutputConverter;
import com.planet_ink.coffee_web.interfaces.HTTPRequest;
import com.planet_ink.coffee_web.server.WebServer;
import com.planet_ink.coffee_web.util.CWConfig;

/*
   Copyright 2014-2018 Bo Zimmerman

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

public class CGIProcessor implements HTTPOutputConverter
{
	private final String executeablePath;
	private final String cgiUrl;
	private final String cgiPathInfo;
	private final String docRoot;

	public CGIProcessor(String executeablePath, String docRoot, String cgiUrl, String cgiPathInfo)
	{
		this.executeablePath=executeablePath;
		this.cgiPathInfo = cgiPathInfo;
		this.cgiUrl = cgiUrl;
		this.docRoot = docRoot;
	}

	private static enum EnvironmentVariables 
	{ 
		AUTH_TYPE,
		CONTENT_LENGTH,
		CONTENT_TYPE,
		GATEWAY_INTERFACE,
		DOCUMENT_ROOT,
		HTTP_,
		PATH_INFO,
		PATH_TRANSLATED,
		QUERY_STRING,
		REMOTE_ADDR,
		REMOTE_HOST,
		REMOTE_IDENT,
		REMOTE_USER,
		REQUEST_URI,
		REQUEST_METHOD,
		SCRIPT_NAME,
		SCRIPT_FILENAME,
		SERVER_ADMIN,
		SERVER_NAME,
		SERVER_PORT,
		SERVER_PROTOCOL,
		SERVER_SIGNATURE, 
		SERVER_SOFTWARE, 
		REDIRECT_STATUS
	}
	
	
	/**
	 * Standard method for converting an input buffer for writing to 
	 * the client.   The position and limit of the bytebuffer must
	 * already be set for reading the content.
	 * Call generateOutput() to get the new output.
	 * @param config the http configuration (optional, may be null)
	 * @param request the http request bring processed  (optional, may be null)
	 * @param pageFile the file being converted
	 * @param status the status of the request (so far)
	 * @param buffer the input buffer
	 * @throws HTTPException
	 */
	@Override
	public ByteBuffer convertOutput(CWConfig config, HTTPRequest request, File pageFile, HTTPStatus status, ByteBuffer buffer) throws HTTPException
	{
		// http://tools.ietf.org/html/draft-robinson-www-interface-00
		if(request == null)
		{
			Log.errOut("CGIConverter requires a non-null request.");
			return buffer;
		}
		final ProcessBuilder builder = new ProcessBuilder(executeablePath);
		final Map<String, String> env = builder.environment();
		env.remove(EnvironmentVariables.AUTH_TYPE.name());
		final String contentLength= request.getHeader(HTTPHeader.Common.CONTENT_LENGTH.toString());
		if(contentLength != null)
			env.put(EnvironmentVariables.CONTENT_LENGTH.name(),contentLength);
		final String contentType= request.getHeader(HTTPHeader.Common.CONTENT_TYPE.toString());
		if(contentType != null)
			env.put(EnvironmentVariables.CONTENT_TYPE.name(),contentType);
		final Pair<String,String> rootMount = config.getMount(request.getHost(), request.getClientPort(), "/");
		if(rootMount != null)
		{
			final File docRootFile = config.getFileManager().createFileFromPath(rootMount.second);
			env.put(EnvironmentVariables.DOCUMENT_ROOT.name(),docRootFile.getAbsolutePath());
		}
		env.put(EnvironmentVariables.GATEWAY_INTERFACE.name(),"CGI/1.1");
		env.put(EnvironmentVariables.PATH_INFO.name(),cgiPathInfo);
		env.put(EnvironmentVariables.PATH_TRANSLATED.name(),"HTTP://"+request.getHost()+":"+request.getClientPort()+cgiPathInfo);
		String queryString = request.getQueryString();
		if(queryString.startsWith("?"))
			queryString=queryString.substring(1);
		env.put(EnvironmentVariables.QUERY_STRING.name(),queryString);
		env.put(EnvironmentVariables.REMOTE_ADDR.name(),request.getClientAddress().toString());
		//env.put(EnvironmentVariables.REMOTE_HOST.name(),null);
		//env.put(EnvironmentVariables.REMOTE_IDENT.name(),null);
		//env.put(EnvironmentVariables.REMOTE_USER.name(),null);
		env.put(EnvironmentVariables.REQUEST_URI.name(),request.getUrlPath()+(queryString.length()==0?"":("?"+queryString)));
		env.put(EnvironmentVariables.REQUEST_METHOD.name(),request.getMethod().toString());
		env.put(EnvironmentVariables.SCRIPT_NAME.name(),cgiUrl);
		String scriptFilename=cgiUrl;
		if(scriptFilename.length()>0)
		{
			final Pair<String,String> mountPath=config.getMount(request.getHost(),request.getClientPort(),scriptFilename);
			if(mountPath != null)
			{
				String newFullPath=scriptFilename.substring(mountPath.first.length());
				if(newFullPath.startsWith("/")&&mountPath.second.endsWith("/"))
					newFullPath=newFullPath.substring(1);
				scriptFilename = (mountPath.second+newFullPath);
			}
		}
		final File scriptFilenameFile = config.getFileManager().createFileFromPath(scriptFilename.replace('/', config.getFileManager().getFileSeparator()));
		env.put(EnvironmentVariables.SCRIPT_FILENAME.name(),scriptFilenameFile.getAbsolutePath());
		env.put(EnvironmentVariables.SERVER_ADMIN.name(),"unknonwn@nowhere.com"); //TODO: add this to config -- nice idea
		env.put(EnvironmentVariables.SERVER_NAME.name(),request.getHost());
		env.put(EnvironmentVariables.SERVER_PORT.name(),""+request.getClientPort());
		env.put(EnvironmentVariables.SERVER_PROTOCOL.name(),"HTTP/"+request.getHttpVer());
		env.put(EnvironmentVariables.SERVER_SIGNATURE.name(),HTTPIOHandler.SERVER_HEADER+" Port "+request.getClientPort());
		env.put(EnvironmentVariables.SERVER_SOFTWARE.name(),WebServer.NAME+" "+WebServer.VERSION);
		env.put(EnvironmentVariables.REDIRECT_STATUS.name(),"200");
		for(HTTPHeader header : HTTPHeader.Common.values())
		{
			final String value=request.getHeader(header.name());
			if(value != null)
			{
				env.put("HTTP_"+header.name().replace('-','_'),value);
			}
		}
		try 
		{
			builder.directory(config.getFileManager().createFileFromPath(docRoot));
			final InputStream bodyIn = request.getBody();
			final ByteArrayOutputStream bout=new ByteArrayOutputStream();
			final Process process = builder.start();
			final InputStream in = process.getInputStream();
			final OutputStream out = process.getOutputStream();
			final byte[] bytes = new byte[1024];
			int len;
			if(bodyIn != null)
			{
				while ((len = bodyIn.read(bytes)) != -1) 
				{
					out.write(bytes, 0, len);
				}
			}
			out.close();
			while ((len = in.read(bytes)) != -1) 
			{
				bout.write(bytes, 0, len);
			}
			int retCode = process.waitFor();
			if(retCode != 0)
			{
				final InputStream errin = process.getErrorStream();
				StringBuilder errMsg = new StringBuilder("");
				while ((len = errin.read(bytes)) != -1) 
				{
					errMsg.append(new String(bytes,0,len));
				}
				Log.errOut(errMsg.toString());
			}
			return ByteBuffer.wrap(bout.toByteArray());
		} 
		catch (IOException e) 
		{
			Log.errOut("CGIConverter", e);
		} 
		catch (InterruptedException e) 
		{
			Log.errOut("CGIConverter", e);
		}
		return buffer;
	}
}
