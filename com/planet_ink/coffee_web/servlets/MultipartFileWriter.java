package com.planet_ink.coffee_web.servlets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import com.planet_ink.coffee_web.http.HTTPMethod;
import com.planet_ink.coffee_web.http.HTTPStatus;
import com.planet_ink.coffee_web.http.MIMEType;
import com.planet_ink.coffee_web.http.MultiPartData;
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
 * Relies on multi-part form data to write all received files to the current
 * web server directory.
 * @author Bo Zimmerman
 *
 */
public class MultipartFileWriter implements SimpleServlet
{

	@Override
	public void doGet(SimpleServletRequest request, SimpleServletResponse response)
	{
		response.setStatusCode(HTTPStatus.S405_METHOD_NOT_ALLOWED.getStatusCode());
	}

	private void writeFilesFromParts(List<MultiPartData> parts, StringBuilder filesList) throws IOException
	{
		if(parts != null)
		{
			for(final MultiPartData part : parts)
			{
				final String filename = part.getVariables().get("filename");
				if(filename != null)
				{
					final File f = new File(filename);
					FileOutputStream fout=null;
					try
					{
						fout=new FileOutputStream(f);
						fout.write(part.getData());
						filesList.append(filename).append("<br>");
					}
					finally
					{
						if(fout!=null)
							fout.close();
					}
				}
				writeFilesFromParts(part.getSubParts(), filesList);
			}
		}
	}
	
	@Override
	public void doPost(SimpleServletRequest request, SimpleServletResponse response)
	{
		try
		{
			final StringBuilder filesList=new StringBuilder("");
			response.setMimeType(MIMEType.All.html.getType());
			writeFilesFromParts(request.getMultiParts(), filesList);
			response.getOutputStream().write(("<html><body><h1>Done</h1><br>"+filesList.toString()+"</body></html>").getBytes());
		}
		catch (final IOException e)
		{
			response.setStatusCode(500);
		}
	}

	@Override
	public void init()
	{
	}

	@Override
	public void service(HTTPMethod method, SimpleServletRequest request, SimpleServletResponse response)
	{
		if(method != HTTPMethod.POST)
			response.setStatusCode(HTTPStatus.S405_METHOD_NOT_ALLOWED.getStatusCode());
	}

}
