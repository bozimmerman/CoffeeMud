package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.coffee_web.http.HTTPHeader;
import com.planet_ink.coffee_web.http.MIMEType;
import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PAData;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.UnsupportedEncodingException;
import java.util.*;

import com.planet_ink.coffee_mud.core.exceptions.HTTPServerException;

/*
   Copyright 2026-2026 Bo Zimmerman

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
public class PlayerExport extends StdWebMacro
{
	@Override
	public String name()
	{
		return "PlayerExport";
	}

	@Override
	public boolean isAWebPath()
	{
		return true;
	}

	@Override
	public boolean preferBinary()
	{
		return true;
	}

	@Override
	public byte[] runBinaryMacro(final HTTPRequest httpReq, final String parm, final HTTPResponse httpResp) throws HTTPServerException
	{
		try
		{
			return runMacro(httpReq,parm,httpResp).getBytes("utf-8");
		}
		catch (UnsupportedEncodingException e)
		{
			throw new HTTPServerException(e);
		}
	}

	@Override
	public String runMacro(final HTTPRequest httpReq, final String parm, final HTTPResponse httpResp) throws HTTPServerException
	{
		httpResp.setHeader(HTTPHeader.Common.CONTENT_TYPE.toString(), "application/cmare");
		if(!CMProps.isState(CMProps.HostState.RUNNING))
			return CMProps.getVar(CMProps.Str.MUDSTATUS);
		final String last=httpReq.getUrlParameter("PLAYER");
		if(last==null)
			return " @break@";
		if(last.length()>0)
		{
			MOB M=CMLib.players().getLoadPlayer(last);
			if(M==null)
			{
				final MOB authM=Authenticate.getAuthenticatedMob(httpReq, httpResp);
				if((authM!=null)&&(authM.Name().equalsIgnoreCase(last)))
					M=authM;
				else
					return " @break@";
			}
			final Set<CMObject> custom=new HashSet<CMObject>();
			final Set<String> files=new HashSet<String>();
			StringBuilder rawXml = new StringBuilder("<PLAYER>");
			rawXml.append(CMLib.coffeeMaker().getPlayerXML(M, custom, files));
			rawXml.append("</PLAYER>");
			rawXml.append(CMLib.coffeeMaker().getExtraCustomXML(custom, files));
			httpResp.setHeader(HTTPHeader.Common.CONTENT_DISPOSITION.toString(), "attachment; filename="+M.Name()+".cmare");
			return rawXml.toString();
		}
		return "";
	}
}
