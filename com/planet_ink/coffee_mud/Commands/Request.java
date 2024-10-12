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
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2024-2024 Bo Zimmerman

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
public class Request extends StdCommand
{
	public Request()
	{
	}

	private final String[] access=I(new String[]{"REQUEST"});
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
		final List<Environmental> all = CMLib.coffeeShops().getAllShopkeepers(mob.location(), mob);
		for(final Iterator<Environmental> e=all.iterator();e.hasNext();)
		{
			final ShopKeeper SK = CMLib.coffeeShops().getShopKeeper(e.next());
			if((SK==null)||(!(SK instanceof CraftBroker)))
				e.remove();
		}
		if(all.size()==0)
		{
			origCmds.set(0, "REQUEST");
			final Command C = CMClass.getCommand("Say");
			if(C != null)
				return C.execute(mob, origCmds, metaFlags);
			else
			{
				CMLib.commands().postCommandFail(mob,origCmds,L("Request how much, of what, for how much, from whom?"));
				return false;
			}
		}

		final Environmental shopkeeper=CMLib.english().parseShopkeeper(mob,commands,"from", "Request how much, of what, for how much, from whom?");
		if(shopkeeper==null)
			return false;
		if(!(CMLib.coffeeShops().getShopKeeper(shopkeeper) instanceof CraftBroker))
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("@x1 is not a broker!",shopkeeper.name()));
			return false;
		}
		if("CANCEL".startsWith(commands.get(0).toUpperCase()))
		{
			final String msgStr = L("<S-NAME> cancel(s) all item requests with <T-NAME>.");
			final CMMsg msg = CMClass.getMsg(mob, shopkeeper, null , CMMsg.MSG_BROKERADD,msgStr,
					CMMsg.MSG_BROKERADD,CMParms.combineQuoted(commands,0),CMMsg.MSG_BROKERADD,msgStr);
			if(mob.location().okMessage(mob, msg))
			{
				mob.location().send(mob, msg);
				return true;
			}
			return false;
		}

		if(commands.size()<3)
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("Request how much, of what, for how much, from whom?"));
			return false;
		}

		if((commands.size()>2)&&(commands.get(commands.size()-1).equalsIgnoreCase("for")))
			commands.remove(commands.size()-1);

		final String bidStr=commands.remove(commands.size()-1);
		final ShopKeeper SK = CMLib.coffeeShops().getShopKeeper(shopkeeper);
		final String currency = SK.getFinalCurrency();
		final Triad<String,Double,Long> bidThang=CMLib.english().parseMoneyStringSDL(currency, bidStr);
		if(bidThang == null)
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("It does not look like '@x1' is a valid price.",bidStr));
			return false;
		}

		if(CMath.s_int(commands.get(0))<=0)
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("It does not look like '@x1' is a valid numer of items.",commands.get(0)));
			return false;
		}
		final int numToAskFor=CMath.s_int(commands.remove(0));
		final CompiledZMask mask = CMLib.masking().parseSpecialItemMask(commands);
		if((mask==null)&&(commands.size()>0))
		{
			final String help = CMLib.help().getHelpText("REQUEST", mob, false);
			CMLib.commands().postCommandFail(mob,origCmds,commands.get(0)+"\n\r"+help);
			return false;
		}
		commands.add(0,""+numToAskFor);
		commands.add(bidStr);
		final String msgStr = L("<S-NAME> list(s) a new item request with <T-NAME>.");
		final CMMsg msg = CMClass.getMsg(mob, shopkeeper, null , CMMsg.MSG_BROKERADD,msgStr,
				CMMsg.MSG_BROKERADD,CMParms.combineQuoted(commands,0),CMMsg.MSG_BROKERADD,msgStr);
		if(mob.location().okMessage(mob, msg))
		{
			mob.location().send(mob, msg);
			return true;
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
