package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;
import com.planet_ink.coffee_mud.core.exceptions.HTTPServerException;

/*
   Copyright 2008-2014 Bo Zimmerman

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
public class FileData extends StdWebMacro
{
	@Override public String name() { return "FileData"; }

	@Override public boolean isAWebPath(){return true;}
	@Override public boolean preferBinary(){return true;}

	@Override
	public void setServletResponse(SimpleServletResponse response, final String filename)
	{
		String file=filename;
		if(file==null)
			file="FileData";
		final int x=file.lastIndexOf('/');
		if((x>=0)&&(x<file.length()-1))
			file=file.substring(x+1);
		super.setServletResponse(response, file);
		response.setHeader("Content-Disposition", "attachment; filename="+file);
	}

	@Override
	public String getFilename(HTTPRequest httpReq, String filename)
	{
		final String path=httpReq.getUrlParameter("PATH");
		if(path==null)
			return filename;
		final String file=httpReq.getUrlParameter("FILE");
		if(file==null)
			return filename;
		return path+"/"+file;
	}

	@Override
	public byte[] runBinaryMacro(HTTPRequest httpReq, String parm) throws HTTPServerException
	{
		final String filename=getFilename(httpReq,"");
		if(filename.length()==0)
			return null;
		final MOB M = Authenticate.getAuthenticatedMob(httpReq);
		if(M==null)
			return null;
		final CMFile F=new CMFile(filename,M);
		if((!F.exists())||(!F.canRead()))
			return null;
		return F.raw();
	}

	@Override
	public String runMacro(HTTPRequest httpReq, String parm) throws HTTPServerException
	{
		return "[Unimplemented string method!]";
	}
}
