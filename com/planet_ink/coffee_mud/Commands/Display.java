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
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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

public class Display extends StdCommand
{
	public Display(){}

	private final String[] access=I(new String[]{"DISPLAY","SHOW"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		Vector<String> origCmds=new XVector<String>(commands);
		if(commands.size()<2)
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("Show what to whom?"));
			return false;
		}
		commands.remove(0);
		if(commands.size()<2)
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("To whom should I show that?"));
			return false;
		}

		final MOB recipient=mob.location().fetchInhabitant(commands.get(commands.size()-1));
		if((recipient==null)||(!CMLib.flags().canBeSeenBy(recipient,mob)))
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("I don't see anyone called @x1 here.",commands.get(commands.size()-1)));
			return false;
		}
		commands.remove(commands.size()-1);
		if((commands.size()>0)&&(commands.get(commands.size()-1).equalsIgnoreCase("to")))
			commands.remove(commands.size()-1);

		final int maxToGive=CMLib.english().calculateMaxToGive(mob,commands,true,mob,false);
		if(maxToGive<0)
			return false;

		String thingToGive=CMParms.combine(commands,0);
		int addendum=1;
		String addendumStr="";
		final Vector<Environmental> V=new Vector<Environmental>();
		boolean allFlag=(commands.size()>0)?commands.get(0).equalsIgnoreCase("all"):false;
		if(thingToGive.toUpperCase().startsWith("ALL."))
		{
			allFlag=true;
			thingToGive="ALL "+thingToGive.substring(4);
		}
		if(thingToGive.toUpperCase().endsWith(".ALL"))
		{
			allFlag=true;
			thingToGive="ALL "+thingToGive.substring(0,thingToGive.length()-4);
		}
		boolean doBugFix = true;
		while(doBugFix || ((allFlag)&&(addendum<=maxToGive)))
		{
			doBugFix=false;
			Environmental giveThis=CMLib.english().bestPossibleGold(mob,null,thingToGive);
			if(giveThis!=null)
			{
				if(((Coins)giveThis).getNumberOfCoins()<CMLib.english().numPossibleGold(mob,thingToGive))
					return false;
			}
			else
				giveThis=mob.fetchItem(null,Wearable.FILTER_UNWORNONLY,thingToGive+addendumStr);
			if((giveThis==null)
			&&(V.size()==0)
			&&(addendumStr.length()==0)
			&&(!allFlag))
				giveThis=mob.fetchItem(null,Wearable.FILTER_WORNONLY,thingToGive);
			if(giveThis==null)
				break;
			if(CMLib.flags().canBeSeenBy(giveThis,mob))
				V.add(giveThis);
			addendumStr="."+(++addendum);
		}

		if(V.size()==0)
			CMLib.commands().postCommandFail(mob,origCmds,L("You don't seem to be carrying that."));
		else
		for(int i=0;i<V.size();i++)
		{
			final Environmental giveThis=V.get(i);
			final CMMsg newMsg=CMClass.getMsg(recipient,giveThis,mob,CMMsg.MSG_LOOK,L("<O-NAME> show(s) <T-NAME> to <S-NAMESELF>."));
			if(mob.location().okMessage(recipient,newMsg))
			{
				recipient.tell(recipient,giveThis,mob,L("<O-NAME> show(s) <T-NAME> to <S-NAMESELF>."));
				mob.location().send(recipient,newMsg);
			}

		}
		return false;
	}

	@Override
	public double combatActionsCost(final MOB mob, final List<String> cmds)
	{
		return CMProps.getCommandCombatActionCost(ID());
	}

	@Override
	public double actionsCost(final MOB mob, final List<String> cmds)
	{
		return CMProps.getCommandActionCost(ID());
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

}
