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

/*
   Copyright 2008-2018 Bo Zimmerman

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
@SuppressWarnings("rawtypes")
public class ThinPlayerData extends StdWebMacro {

	@Override
	public String name()
	{
		return "ThinPlayerData";
	}

	@Override
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp)
	{
		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return CMProps.getVar(CMProps.Str.MUDSTATUS);

		final java.util.Map<String,String> parms=parseParms(parm);
		final String last=httpReq.getUrlParameter("PLAYER");
		if(last==null)
			return " @break@";
		final StringBuffer str=new StringBuffer("");
		if(last.length()>0)
		{
			String sort=httpReq.getUrlParameter("SORTBY");
			if(sort==null)
				sort="";
			PlayerLibrary.ThinPlayer player = null;
			final Enumeration pe=CMLib.players().thinPlayers(sort, httpReq.getRequestObjects());
			for(;pe.hasMoreElements();)
			{
				final PlayerLibrary.ThinPlayer TP=(PlayerLibrary.ThinPlayer)pe.nextElement();
				if(TP.name().equalsIgnoreCase(last))
				{
					player = TP;
					break;
				}
			}
			if(player == null)
				return " @break@";
			for(final String key : parms.keySet())
			{
				final int x=CMLib.players().getCharThinSortCode(key.toUpperCase().trim(),false);
				if(x>=0)
				{
					String value = CMLib.players().getThinSortValue(player, x);
					if(PlayerLibrary.CHAR_THIN_SORT_CODES[x].equals("LAST"))
						value=CMLib.time().date2String(CMath.s_long(value));
					else
					if(PlayerLibrary.CHAR_THIN_SORT_CODES[x].equals("HOURS"))
						value=""+(CMath.s_long(value)/60);
					str.append(value+", ");
				}
			}
		}
		String strstr=str.toString();
		if(strstr.endsWith(", "))
			strstr=strstr.substring(0,strstr.length()-2);
		return clearWebMacros(strstr);
	}

}
