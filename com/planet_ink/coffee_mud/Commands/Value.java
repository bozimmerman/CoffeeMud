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

public class Value extends StdCommand
{
	public Value(){}

	private final String[] access=I(new String[]{"VALUE","VAL","V"});
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
		final Environmental shopkeeper=CMLib.english().parseShopkeeper(mob,commands,"Value what with whom?");
		if(shopkeeper==null)
			return false;
		if(commands.size()==0)
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("Value what?"));
			return false;
		}

		int maxToDo=Integer.MAX_VALUE;
		if((commands.size()>1)
		&&(CMath.s_int(commands.get(0))>0))
		{
			maxToDo=CMath.s_int(commands.get(0));
			commands.set(0,"all");
		}

		String whatName=CMParms.combine(commands,0);
		final Vector<Item> V=new Vector<Item>();
		boolean allFlag=commands.get(0).equalsIgnoreCase("all");
		if(whatName.toUpperCase().startsWith("ALL."))
		{
			allFlag=true;
			whatName="ALL "+whatName.substring(4);
		}
		if(whatName.toUpperCase().endsWith(".ALL"))
		{
			allFlag=true;
			whatName="ALL "+whatName.substring(0,whatName.length()-4);
		}
		int addendum=1;
		String addendumStr="";
		boolean doBugFix = true;
		while(doBugFix || ((allFlag)&&(addendum<=maxToDo)))
		{
			doBugFix=false;
			final Item itemToDo=mob.fetchItem(null,Wearable.FILTER_UNWORNONLY,whatName+addendumStr);
			if(itemToDo==null)
				break;
			if((CMLib.flags().canBeSeenBy(itemToDo,mob))
			&&(!V.contains(itemToDo)))
				V.add(itemToDo);
			addendumStr="."+(++addendum);
		}

		if(V.size()==0)
			CMLib.commands().postCommandFail(mob,origCmds,L("You don't seem to have '@x1'.",whatName));
		else
		for(int v=0;v<V.size();v++)
		{
			final Item thisThang=V.get(v);
			final CMMsg newMsg=CMClass.getMsg(mob,shopkeeper,thisThang,CMMsg.MSG_VALUE,null);
			if(mob.location().okMessage(mob,newMsg))
				mob.location().send(mob,newMsg);
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
