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
public class PrideStat extends StdWebMacro
{
	@Override
	public String name()
	{
		return "PrideStat";
	}

	@Override
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		TimeClock.TimePeriod period=null;
		AccountStats.PrideStat stat=null;
		int which=-1;
		String val=null;
		boolean player = true;
		for(final String s : parms.keySet())
		{
			if(CMath.isInteger(s))
				which=CMath.s_int(s.trim());
			else
			if(s.equalsIgnoreCase("account"))
				player=false;
			else
			if(s.equalsIgnoreCase("player"))
				player=true;
			else
			if(s.equalsIgnoreCase("name"))
				val="NAME";
			else
			if(s.equalsIgnoreCase("value"))
				val="VALUE";
			else
			{
				try
				{
					period=TimeClock.TimePeriod.valueOf(s.toUpperCase().trim());
				}
				catch(final Exception e)
				{
					try
					{
						stat=AccountStats.PrideStat.valueOf(s.toUpperCase().trim());
					}
					catch(final Exception e2)
					{
						return " [error unknown parameter: "+s+"]";
					}
				}
			}
		}
		if(period==null)
			return " [error missing valid period, try "+CMParms.toListString(TimeClock.TimePeriod.values())+"]";
		if(stat==null)
			return " [error missing valid stat, try "+CMParms.toListString(AccountStats.PrideStat.values())+"]";
		if(val==null)
			return " [error missing value type, try name or value]";
		if(which<1)
			return " [error missing number, try 1-10]";

		final List<Pair<String,Integer>> list=player?CMLib.players().getTopPridePlayers(period, stat):CMLib.players().getTopPrideAccounts(period, stat);
		if(which>list.size())
			return "";
		final Pair<String,Integer> p=list.get(which-1);
		if(val.equals("NAME"))
			return p.first;
		else
			return p.second.toString();
	}
}
