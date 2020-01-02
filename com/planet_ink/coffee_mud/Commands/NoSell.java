package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMProps.Int;
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
   Copyright 2019-2020 Bo Zimmerman

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
public class NoSell extends StdCommand
{
	public NoSell()
	{
	}

	private final String[] access=I(new String[]{"NOSELL"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		final Vector<String> origCmds=new XVector<String>(commands);
		boolean forceOn=false;
		boolean forceOff=false;
		if(commands.size()>1)
		{
			final String parm=commands.get(1).toUpperCase().trim();
			forceOn = parm.equals("ON");
			forceOff = parm.equals("OFF");
			if(forceOn || forceOff)
				commands.remove(1);
		}
		if(commands.size()<2)
		{
			if(forceOn)
				CMLib.commands().postCommandFail(mob,origCmds,L("Set what unsellable?"));
			else
			if(forceOff)
				CMLib.commands().postCommandFail(mob,origCmds,L("Set what sellable?"));
			else
				CMLib.commands().postCommandFail(mob,origCmds,L("Toggle the unsellable flag on what?"));
			return false;
		}
		commands.remove(0);
		String whatName="";
		if(commands.size()>0)
			whatName=commands.get(commands.size()-1);
		final int maxToNoSell=CMLib.english().parseMaxToGive(mob,commands,true,mob,false);
		if(maxToNoSell<0)
			return false;

		String whatToGet=CMParms.combine(commands,0);
		boolean allFlag=(commands.size()>0)?commands.get(0).equalsIgnoreCase("all"):false;
		if(whatToGet.toUpperCase().startsWith("ALL."))
		{
			allFlag=true;
			whatToGet="ALL "+whatToGet.substring(4);
		}
		if(whatToGet.toUpperCase().endsWith(".ALL"))
		{
			allFlag=true;
			whatToGet="ALL "+whatToGet.substring(0,whatToGet.length()-4);
		}
		final List<Item> itemsV=new ArrayList<Item>();
		int addendum=1;
		String addendumStr="";
		do
		{
			Environmental getThis=null;
			getThis=mob.fetchItem(null,Wearable.FILTER_UNWORNONLY,whatToGet+addendumStr);
			if(getThis==null)
				break;
			if((getThis instanceof Item)
			&&(CMLib.flags().canBeSeenBy(getThis,mob))
			&&(!itemsV.contains(getThis)))
				itemsV.add((Item)getThis);
			addendumStr="."+(++addendum);
		}
		while((allFlag)&&(addendum<=maxToNoSell))
			;

		if(itemsV.size()==0)
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("You don't see '@x1' here.",whatName));
			return false;
		}

		for(final Item I : itemsV)
		{
			if(I instanceof Coins)
				continue;
			final Ability noSellA=I.fetchEffect("Prop_Unsellable");
			final boolean noSellProp = (noSellA == null) ? false : noSellA.text().toUpperCase().indexOf("DROPOFF") >= 0;
			final boolean setTo = (forceOn ? true : (forceOff ? false : (noSellA == null)));
			if(setTo)
			{
				if(noSellA != null)
					mob.tell(L("@x1 is already marked unsellable.",I.name(mob)));
				else
				{
					final Ability propA=CMClass.getAbility("Prop_Unsellable");
					if(propA != null)
					{
						I.addNonUninvokableEffect(propA);
						propA.setMiscText("MESSAGE=\"@x1 is unsellable.\" DROPOFF=true AMBIANCE=\"(Unsellable)\"");
						I.recoverPhyStats();
						mob.tell(L("@x1 is now marked unsellable.",I.name(mob)));
					}
				}
			}
			else
			{
				if(!noSellProp)
					mob.tell(L("@x1 is not marked unsellable.",I.name(mob)));
				else
				if(noSellA != null)
				{
					noSellA.unInvoke();
					I.delEffect(noSellA);
					mob.tell(L("@x1 is now sellable again.",I.name(mob)));
					I.recoverPhyStats();
				}
			}
		}
		return false;
	}

	@Override
	public double combatActionsCost(final MOB mob, final List<String> cmds)
	{
		final double defaultCost = CMath.div(CMProps.getIntVar(Int.DEFCOMCMDTIME),100.0);
		final double specificCost = CMProps.getCommandCombatActionCost(ID());
		return (defaultCost != specificCost) ? specificCost : 0;
	}

	@Override
	public double actionsCost(final MOB mob, final List<String> cmds)
	{
		final double defaultCost = CMath.div(CMProps.getIntVar(Int.DEFCMDTIME),100.0);
		final double specificCost = CMProps.getCommandActionCost(ID());
		return (defaultCost != specificCost) ? specificCost : 0;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

}
