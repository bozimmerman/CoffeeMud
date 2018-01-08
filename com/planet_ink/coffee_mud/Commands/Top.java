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

public class Top extends StdCommand
{
	private final String[] access=I(new String[]{"TOP"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(final MOB mob, List<String> commands, int metaFlags) throws java.io.IOException
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

		List<Pair<String,Integer>> set1;
		List<Pair<String,Integer>> set2;
		List<Pair<String,Integer>> set3;
		final StringBuilder str=new StringBuilder();
		final int width=CMLib.lister().fixColWidth(72, mob)/3;
		final int nameWidth=width - (width/3)-3;
		final String slashes=CMStrings.repeat('=', width);
		for(final TimePeriod period : new TimePeriod[]{TimePeriod.ALLTIME,TimePeriod.MONTH})
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
				data = CMStrings.padRight((set1.size()>i)?("^N"+set1.get(i).first):"",nameWidth)+"^c"+((set1.size()>i)?("^N"+set1.get(i).second):"");
				str.append(CMStrings.padRight("^H"+(i+1)+((i>=9)?"":" ")+". ^N"+data,width)+"^.^N ");
				data = CMStrings.padRight((set2.size()>i)?("^N"+set2.get(i).first):"",nameWidth)+"^c"+((set2.size()>i)?("^N"+set2.get(i).second):"");
				str.append(CMStrings.padRight("^H"+(i+1)+((i>=9)?"":" ")+". ^N"+data,width)+"^.^N ");
				data = CMStrings.padRight((set3.size()>i)?("^N"+set3.get(i).first):"",nameWidth)+"^c"+((set3.size()>i)?("^N"+set3.get(i).second):"");
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
				data = CMStrings.padRight((set1.size()>i)?("^N"+set1.get(i).first):"",nameWidth)+"^c"+((set1.size()>i)?("^N"+set1.get(i).second):"");
				str.append(CMStrings.padRight("^H"+(i+1)+((i>=9)?"":" ")+". ^N"+data,width)+"^.^N ");
				data = CMStrings.padRight((set2.size()>i)?("^N"+set2.get(i).first):"",nameWidth)+"^c"+((set2.size()>i)?("^N"+set2.get(i).second):"");
				str.append(CMStrings.padRight("^H"+(i+1)+((i>=9)?"":" ")+". ^N"+data,width)+"^.^N ");
				data = CMStrings.padRight((set3.size()>i)?("^N"+set3.get(i).first):"",nameWidth)+"^c"+((set3.size()>i)?("^N"+set3.get(i).second):"");
				str.append(CMStrings.padRight("^H"+(i+1)+((i>=9)?"":" ")+". ^N"+data,width)+"^.^N\n\r");
			}
			str.append("\n\r");
		}
		if(mob.session()!=null)
			mob.session().print(str.toString());
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}
}

