package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.coffee_web.http.HTTPException;
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

/*
   Copyright 2004-2018 Bo Zimmerman

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
public class AddRandomFile extends StdWebMacro
{
	@Override
	public String name()
	{
		return "AddRandomFile";
	}

	@Override
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		if((parms==null)||(parms.size()==0))
			return "";
		final StringBuffer buf=new StringBuffer("");
		final int d=CMLib.dice().roll(1,parms.size(),0);
		String file=null;
		int i=0;
		boolean LINKONLY=false;
		for(final String val : parms.values())
		{
			if(val.equalsIgnoreCase("LINKONLY"))
				LINKONLY=true;
		}
		for(final String val : parms.values())
		{
			file=val;
			if(file.equalsIgnoreCase("LINKONLY"))
				continue;
			if((++i)==d)
				break;
		}
		if((file!=null)&&(file.length()>0))
		{
			try
			{
				if(LINKONLY)
					buf.append(file);
				else
					buf.append(new String(getHTTPFileData(httpReq,file)));
			}
			catch(final HTTPException e)
			{
				Log.warnOut("Failed "+name()+" "+file);
			}
		}
		return buf.toString();
	}
}
