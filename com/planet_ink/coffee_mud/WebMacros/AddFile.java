package com.planet_ink.coffee_mud.WebMacros;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.HTTPRedirectException;
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
import com.planet_ink.coffee_web.http.HTTPException;
import com.planet_ink.coffee_web.http.HTTPMethod;
import com.planet_ink.coffee_web.http.MultiPartData;
import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_web.util.CWThread;

import java.io.InputStream;
import java.net.InetAddress;
import java.util.*;

/*
   Copyright 2002-2025 Bo Zimmerman

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
public class AddFile extends StdWebMacro
{
	@Override
	public String name()
	{
		return "AddFile";
	}

	@Override
	public String runMacro(final HTTPRequest httpReq, final String parm, final HTTPResponse httpResp)
	{
		final PairSVector<String,String> parms=super.parseOrderedParms(parm,true);
		if((parms==null)||(parms.size()==0))
			return "";
		final StringBuffer buf=new StringBuffer("");
		boolean webify=false;
		boolean replace=false;
		String template="";
		for(final Pair<String,String> p : parms)
		{
			final String key=p.first;
			final String file = p.second;
			if(file.length()>0)
			{
				try
				{
					if(key.trim().equalsIgnoreCase("template"))
						template=file;
					else
					if(key.trim().equalsIgnoreCase("new"))
					{
						if(template.length()==0)
							return " @break@";
						else
						{
							final MOB mob=Authenticate.getAuthenticatedMob(httpReq, httpResp);
							final CMFile tF = new CMFile(template,mob);
							if((!tF.exists())||(!tF.canRead()))
								return " @break@";
							final CMFile nF = new CMFile(file,mob);
							if(!nF.canWrite())
								return " @break@";
							final long[] processStartTime=new long[]{System.currentTimeMillis()};
							final String[] lastFoundMacro=new String[]{""};
							final byte[] savable = CMLib.webMacroFilter().virtualPageFilter(httpReq,
									httpReq.getRequestObjects(),
									processStartTime,
									lastFoundMacro,
									new StringBuffer(new String(tF.raw())),httpResp).toString().getBytes();
							nF.saveRaw(savable);
						}
					}
					else
					if(key.trim().equalsIgnoreCase("webify"))
						webify=true;
					else
					if(key.trim().equalsIgnoreCase("replace"))
						replace=true;
					else
					if(replace)
					{
						int x=buf.indexOf(key);
						while(x>=0)
						{
							if(webify)
								buf.replace(x,x+key.length(),webify(new StringBuffer(file)).toString());
							else
								buf.replace(x,x+key.length(),file);
							x=buf.indexOf(key,x+key.length());
						}
					}
					else
					if(webify)
						buf.append(webify(new StringBuffer(new String(getHTTPFileData(httpReq,file.trim())))));
					else
						buf.append(new String(getHTTPFileData(httpReq,file.trim())));
				}
				catch(final HTTPException e)
				{
					Log.warnOut("Failed "+name()+" "+file);
				}
				catch (final HTTPRedirectException e)
				{
					Log.warnOut("Failed "+name()+" "+file);
				}
			}
		}
		return buf.toString();
	}
}
