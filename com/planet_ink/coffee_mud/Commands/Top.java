package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2014-2020 Bo Zimmerman

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
public class Top extends StdCommand
{
	private final String[] access=I(new String[]{"TOP"});

	private final static Class<?>[][]	internalParameters	= new Class<?>[][] { {}, { Boolean.class, Integer.class } };

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	protected String fixI(final Integer I)
	{
		if(I==null)
			return "";
		final int ilen=I.toString().length();
		if(ilen > 6)
		{
			if(ilen > 9)
				return I.toString().substring(0, ilen-6)+"m";
			return I.toString().substring(0, ilen-3)+"k";
		}
		return I.toString();
	}

	public String getTopData(final int width, final boolean doPlayers, final TimePeriod[] periods)
	{
		final StringBuilder str=new StringBuilder();
		final int nameWidth=width - (width/3)-3;
		final String slashes=CMStrings.repeat('=', width);
		List<Pair<String,Integer>> set1;
		List<Pair<String,Integer>> set2;
		List<Pair<String,Integer>> set3;
		for(final TimePeriod period : periods)
		{
			final String desc=(period==TimePeriod.ALLTIME)?"All Time":"This Month";
			str.append(L("^xTop @x1 @x2\n\r^x@x3^.^N ^x@x4^.^N ^x@x5^.^N\n\r",(doPlayers?"Characters":"Accounts"),desc,slashes,slashes,slashes));
			str.append(CMStrings.padRight(L("^HPVP Kills"), width)+"^. "+CMStrings.padRight(L("^HXP Gained"), width)+"^. "+CMStrings.padRight(L("^HQuests Completed"), width)+"^.^N\n\r");
			set1=doPlayers?
				CMLib.players().getTopPridePlayers(period, AccountStats.PrideStat.PVPKILLS):
				CMLib.players().getTopPrideAccounts(period, AccountStats.PrideStat.PVPKILLS);
			set2=doPlayers?
				CMLib.players().getTopPridePlayers(period, AccountStats.PrideStat.EXPERIENCE_GAINED):
				CMLib.players().getTopPrideAccounts(period, AccountStats.PrideStat.EXPERIENCE_GAINED);
			set3=doPlayers?
				CMLib.players().getTopPridePlayers(period, AccountStats.PrideStat.QUESTS_COMPLETED):
				CMLib.players().getTopPrideAccounts(period, AccountStats.PrideStat.QUESTS_COMPLETED);
			String data;
			for(int i=0;i<10;i++)
			{
				data = CMStrings.padRight((set1.size()>i)?("^N"+set1.get(i).first):"",nameWidth)+"^c"+((set1.size()>i)?("^N"+fixI(set1.get(i).second)):"");
				str.append(CMStrings.padRight("^H"+(i+1)+((i>=9)?"":" ")+". ^N"+data,width)+"^.^N ");
				data = CMStrings.padRight((set2.size()>i)?("^N"+set2.get(i).first):"",nameWidth)+"^c"+((set2.size()>i)?("^N"+fixI(set2.get(i).second)):"");
				str.append(CMStrings.padRight("^H"+(i+1)+((i>=9)?"":" ")+". ^N"+data,width)+"^.^N ");
				data = CMStrings.padRight((set3.size()>i)?("^N"+set3.get(i).first):"",nameWidth)+"^c"+((set3.size()>i)?("^N"+fixI(set3.get(i).second)):"");
				str.append(CMStrings.padRight("^H"+(i+1)+((i>=9)?"":" ")+". ^N"+data,width)+"^.^N\n\r");
			}
			str.append("\n\r");
			str.append(CMStrings.padRight(L("^HMins Online"), width)+"^. "+CMStrings.padRight(L("^HRooms Explored"), width)+"^. "+CMStrings.padRight(L("^HQuestPoints Earned"), width)+"^.^N\n\r");
			set1=doPlayers?
					CMLib.players().getTopPridePlayers(period, AccountStats.PrideStat.MINUTES_ON):
					CMLib.players().getTopPrideAccounts(period, AccountStats.PrideStat.MINUTES_ON);
				set2=doPlayers?
					CMLib.players().getTopPridePlayers(period, AccountStats.PrideStat.ROOMS_EXPLORED):
					CMLib.players().getTopPrideAccounts(period, AccountStats.PrideStat.ROOMS_EXPLORED);
				set3=doPlayers?
					CMLib.players().getTopPridePlayers(period, AccountStats.PrideStat.QUESTPOINTS_EARNED):
					CMLib.players().getTopPrideAccounts(period, AccountStats.PrideStat.QUESTPOINTS_EARNED);
			for(int i=0;i<10;i++)
			{
				data = CMStrings.padRight((set1.size()>i)?("^N"+set1.get(i).first):"",nameWidth)+"^c"+((set1.size()>i)?("^N"+fixI(set1.get(i).second)):"");
				str.append(CMStrings.padRight("^H"+(i+1)+((i>=9)?"":" ")+". ^N"+data,width)+"^.^N ");
				data = CMStrings.padRight((set2.size()>i)?("^N"+set2.get(i).first):"",nameWidth)+"^c"+((set2.size()>i)?("^N"+fixI(set2.get(i).second)):"");
				str.append(CMStrings.padRight("^H"+(i+1)+((i>=9)?"":" ")+". ^N"+data,width)+"^.^N ");
				data = CMStrings.padRight((set3.size()>i)?("^N"+set3.get(i).first):"",nameWidth)+"^c"+((set3.size()>i)?("^N"+fixI(set3.get(i).second)):"");
				str.append(CMStrings.padRight("^H"+(i+1)+((i>=9)?"":" ")+". ^N"+data,width)+"^.^N\n\r");
			}
			str.append("\n\r");
		}
		return str.toString();
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags) throws java.io.IOException
	{
		boolean doPlayers=true;
		if(commands.size()>1)
		{
			final String what=commands.get(1).toUpperCase();
			if("PLAYERS".startsWith(what))
				doPlayers=true;
			else
			if((CMProps.isUsingAccountSystem())&&("ACCOUNTS".startsWith(what)))
				doPlayers=false;
			else
			{
				mob.tell(L("'@x1' is unknown.  Try PLAYERS or ACCOUNTS",what));
				return true;
			}
		}

		final int width=CMLib.lister().fixColWidth(72, mob)/3;
		final String str=this.getTopData(width, doPlayers, new TimePeriod[]{TimePeriod.ALLTIME,TimePeriod.MONTH});
		if(mob.session()!=null)
			mob.session().print(str.toString());
		return false;
	}

	@Override
	public Object executeInternal(final MOB mob, final int metaFlags, final Object... args) throws java.io.IOException
	{
		if(!super.checkArguments(internalParameters, args))
			return Boolean.FALSE;
		final TimePeriod[] periods = new TimePeriod[]{TimePeriod.MONTH};
		if((args != null)&&(args.length>1))
			return this.getTopData(((Integer)args[1]).intValue(), ((Boolean)args[0]).booleanValue(), periods);
		else
			return this.getTopData(78, true, periods)+"\n\r"+this.getTopData(78, false, periods);
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}
}

