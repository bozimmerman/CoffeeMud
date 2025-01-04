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
   Copyright 2004-2025 Bo Zimmerman

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
public class Sell extends StdCommand
{
	public Sell()
	{
	}

	private final String[] access = I(new String[] { "SELL" });

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
		final Environmental shopkeeper=CMLib.english().parseShopkeeper(mob,commands,"to", "Sell what to whom?");
		if(shopkeeper==null)
			return false;
		final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(shopkeeper);
		if(commands.size()==0)
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("Sell what?"));
			return false;
		}

		final int maxToDo=CMLib.english().parseMaxToGive(mob,commands,true,mob,false);
		if(maxToDo<0)
			return false;

		String whatName=CMParms.combine(commands,0);
		final List<Environmental> itemsV=new ArrayList<Environmental>();
		boolean allFlag=commands.get(0).equalsIgnoreCase("all");
		if (whatName.toUpperCase().startsWith("ALL."))
		{
			allFlag = true;
			whatName = "ALL " + whatName.substring(4);
		}
		if (whatName.toUpperCase().endsWith(".ALL"))
		{
			allFlag = true;
			whatName = "ALL " + whatName.substring(0, whatName.length() - 4);
		}
		final boolean mobCheck = ((SK!=null)
								&& ((SK.getShop().isSold(ShopKeeper.DEAL_CHILDREN))
									));
		int addendum=1;
		String addendumStr="";
		boolean doBugFix = true;
		while(doBugFix || ((allFlag)&&(addendum<=maxToDo)))
		{
			doBugFix=false;
			Environmental itemToDo=mob.fetchItem(null,Wearable.FILTER_UNWORNONLY,whatName+addendumStr);
			if(itemToDo==null)
			{
				if(mobCheck)
				{
					itemToDo = mob.location().fetchInhabitant(whatName+addendumStr);
					if((itemToDo!=null)
					&&((((MOB)itemToDo).isPlayer())
						||(!mob.getGroupMembers(new XTreeSet<MOB>()).contains(itemToDo))))
						itemToDo=null;
				}
				if(itemToDo==null)
					break;
			}
			if((CMLib.flags().canBeSeenBy(itemToDo,mob))
			&&(!(itemToDo instanceof Coins))
			&&(!itemsV.contains(itemToDo)))
				itemsV.add(itemToDo);
			addendumStr="."+(++addendum);
		}

		if(itemsV.size()==0)
			CMLib.commands().postCommandFail(mob,origCmds,L("You don't seem to have '@x1'.",whatName));
		else
		{
			for(int v=0;v<itemsV.size();v++)
			{
				final Environmental thisThang=itemsV.get(v);
				final CMMsg msg=CMClass.getMsg(mob,shopkeeper,thisThang,CMMsg.MSG_SELL,L("<S-NAME> sell(s) <O-NAME> to <T-NAMESELF>."));
				if(mob.location().okMessage(mob,msg))
					mob.location().send(mob,msg);
				else
				if(itemsV.size()==1)
					CMLib.commands().postCommandRejection(msg.source(),msg.target(),msg.tool(),origCmds);
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
		return false;
	}

}
