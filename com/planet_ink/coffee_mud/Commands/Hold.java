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
public class Hold extends StdCommand
{
	public Hold()
	{
	}

	private final String[] access=I(new String[]{"HOLD","HOL","HO","H"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	private final static Class<?>[][] internalParameters=new Class<?>[][]
	{
		{Item.class},
		{Item.class,Boolean.class}
	};

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		final Vector<String> origCmds=new XVector<String>(commands);
		if(commands.size()<2)
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("Hold what?"));
			return false;
		}
		commands.remove(0);
		final List<Item> items=CMLib.english().fetchItemList(mob,mob,null,commands,Wearable.FILTER_UNWORNONLY,false);
		if(items.size()==0)
			CMLib.commands().postCommandFail(mob,origCmds,L("You don't seem to be carrying that."));
		else
		for(int i=0;i<items.size();i++)
		{
			if((items.size()==1)||(items.get(i).canWear(mob,Wearable.WORN_HELD)))
			{
				final Item item=items.get(i);
				hold(mob,item,false);
			}
		}
		return false;
	}

	public boolean hold(final MOB mob, final Item item, final boolean quiet)
	{
		int msgType=CMMsg.MSG_HOLD;
		String str=L("<S-NAME> hold(s) <T-NAME>.");
		if((mob.freeWearPositions(Wearable.WORN_WIELD,(short)0,(short)0)>0)
		&&((item.rawProperLocationBitmap()==Wearable.WORN_WIELD)
		||(item.rawProperLocationBitmap()==(Wearable.WORN_HELD|Wearable.WORN_WIELD))))
		{
			str=L("<S-NAME> wield(s) <T-NAME>.");
			msgType=CMMsg.MSG_WIELD;
		}
		final CMMsg newMsg=CMClass.getMsg(mob,item,null,msgType,quiet?null:str);
		if(mob.location().okMessage(mob,newMsg))
		{
			mob.location().send(mob,newMsg);
			return true;
		}
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
		return Boolean.valueOf(hold(mob,targetWearI,quietly));
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
