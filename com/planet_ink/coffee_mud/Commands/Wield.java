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

public class Wield extends StdCommand
{
	public Wield()
	{
	}
	
	private final String[]	access	= I(new String[] { "WIELD" });
	
	@SuppressWarnings("rawtypes")
	private final static Class[][] internalParameters=new Class[][]{{Item.class}};

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	protected boolean wield(List<Item> items, MOB mob)
	{
		for(int i=0;i<items.size();i++)
		{
			if((items.size()==1)||(items.get(i).canWear(mob,Wearable.WORN_WIELD)))
			{
				final Item item=items.get(i);
				final CMMsg newMsg=CMClass.getMsg(mob,item,null,CMMsg.MSG_WIELD,L("<S-NAME> wield(s) <T-NAME>."));
				if(mob.location().okMessage(mob,newMsg))
				{
					mob.location().send(mob,newMsg);
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		Vector<String> origCmds=new XVector<String>(commands);
		if(commands.size()<2)
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("Wield what?"));
			return false;
		}
		commands.remove(0);
		List<Item> items=CMLib.english().fetchItemList(mob,mob,null,commands,Wearable.FILTER_UNWORNONLY,false);
		if(items.size()==0)
			CMLib.commands().postCommandFail(mob,origCmds,L("You don't seem to be carrying that."));
		else
			wield(items,mob);
		return false;
	}
	
	@Override
	public Object executeInternal(MOB mob, int metaFlags, Object... args) throws java.io.IOException
	{
		if(!super.checkArguments(internalParameters, args))
			return Boolean.FALSE;
		final List<Item> items=new XVector<Item>((Item)args[0]);
		return Boolean.valueOf(wield(items, mob));
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
