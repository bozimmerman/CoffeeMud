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
   Copyright 2004-2020 Bo Zimmerman

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

	private final static Class<?>[][] internalParameters=new Class<?>[][]
	{
		{Item.class},
		{Item.class,Boolean.class}
	};


	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	protected boolean wield(final MOB mob, final Item item, final boolean quietly)
	{
		final CMMsg newMsg=CMClass.getMsg(mob,item,null,CMMsg.MSG_WIELD,quietly?null:L("<S-NAME> wield(s) <T-NAME>."));
		if(mob.location().okMessage(mob,newMsg))
		{
			mob.location().send(mob,newMsg);
			return true;
		}
		return false;
	}

	protected boolean wield(final List<Item> items, final MOB mob)
	{
		for(int i=0;i<items.size();i++)
		{
			if((items.size()==1)||(items.get(i).canWear(mob,Wearable.WORN_WIELD)))
			{
				final Item item=items.get(i);
				if(wield(mob, item, false))
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		final Vector<String> origCmds=new XVector<String>(commands);
		if(commands.size()<2)
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("Wield what?"));
			return false;
		}
		commands.remove(0);
		final List<Item> items=CMLib.english().fetchItemList(mob,mob,null,commands,Wearable.FILTER_UNWORNONLY,false);
		if(items.size()==0)
			CMLib.commands().postCommandFail(mob,origCmds,L("You don't seem to be carrying that."));
		else
			wield(items,mob);
		return false;
	}

	@Override
	public Object executeInternal(final MOB mob, final int metaFlags, final Object... args) throws java.io.IOException
	{
		if(!super.checkArguments(internalParameters, args))
			return Boolean.FALSE;
		final Item targetWearI = (Item)args[0];
		boolean quietly = false;
		for(int i=1;i<args.length;i++)
		{
			if(args[i] instanceof Boolean)
			{
				quietly = ((Boolean)args[i]).booleanValue();
			}
		}
		return Boolean.valueOf(wield(mob,targetWearI,quietly));
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
