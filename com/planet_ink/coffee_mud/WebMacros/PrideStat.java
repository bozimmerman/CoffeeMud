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
   Copyright 2014-2024 Bo Zimmerman

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
	public String runMacro(final HTTPRequest httpReq, final String parm, final HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		TimeClock.TimePeriod period=null;
		PrideStats.PrideStat stat=null;
		int which=-1;
		String val=null;
		boolean fixi = false;
		boolean prev = false;
		PlayerLibrary.PrideCat cat=null;
		String catUnit=null;
		int padRight=0;
		int padLeft=0;
		boolean player = true;
		for(final String s : parms.keySet())
		{
			if(CMath.isInteger(s))
				which=CMath.s_int(s.trim());
			else
			if(s.equalsIgnoreCase("account"))
				player=false;
			else
			if(s.equalsIgnoreCase("previous"))
				prev=true;
			else
			if(s.equalsIgnoreCase("fixi"))
				fixi=true;
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
			if(s.equalsIgnoreCase("padleft"))
				padLeft = CMath.s_int(parms.get(s));
			else
			if(s.equalsIgnoreCase("padright"))
				padRight = CMath.s_int(parms.get(s));
			else
			if(s.equalsIgnoreCase("cat"))
				cat=(PlayerLibrary.PrideCat)CMath.s_valueOf(PlayerLibrary.PrideCat.class,parms.get(s).toUpperCase());
			else
			if(s.equalsIgnoreCase("catunit")&&(val!=null))
				catUnit=val.toUpperCase();
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
						stat=PrideStats.PrideStat.valueOf(s.toUpperCase().trim());
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
			return " [error missing valid stat, try "+CMParms.toListString(PrideStats.PrideStat.values())+"]";
		if(val==null)
			return " [error missing value type, try name or value]";
		if(which<1)
			return " [error missing number, try 1-10]";

		final List<Pair<String,Integer>> list;
		if(player)
		{
			if((cat != null)||(catUnit != null))
			{
				if((cat==null)||(catUnit==null))
					return " [error missing cat/catunit pairing]";
				if(prev)
					list = CMLib.players().getPreviousTopPridePlayers(cat, catUnit, period, stat);
				else
					list = CMLib.players().getTopPridePlayers(cat, catUnit, period, stat);
			}
			else
			if(prev)
				list = CMLib.players().getPreviousTopPridePlayers(period, stat);
			else
				list = CMLib.players().getTopPridePlayers(period, stat);
		}
		else
		if(prev)
			list = CMLib.players().getPreviousTopPrideAccounts(period, stat);
		else
			list = CMLib.players().getTopPrideAccounts(period, stat);
		String fval;
		if(which>list.size())
			fval="";
		else
		{
			final Pair<String,Integer> p=list.get(which-1);
			if(val.equals("NAME"))
				fval = p.first;
			else
			{
				fval = p.second.toString();
				if(fixi)
				{
					final int ilen=fval.length();
					if(ilen > 6)
					{
						if(ilen > 9)
							fval=fval.substring(0, ilen-6)+"m";
						else
							fval=fval.substring(0, ilen-3)+"k";
					}
				}
			}
		}
		if(padLeft > 0)
			fval=CMStrings.padLeft(fval, padLeft);
		else
		if(padRight > 0)
			fval=CMStrings.padRight(fval, padRight);
		return fval;
	}
}
