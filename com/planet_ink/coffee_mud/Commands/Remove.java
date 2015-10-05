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
   Copyright 2004-2015 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
public class Remove extends StdCommand
{
	public Remove(){}

	private final String[] access=I(new String[]{"REMOVE","REM"});
	@Override public String[] getAccessWords(){return access;}



	@Override
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		Vector origCmds=new XVector(commands);
		if(commands.size()<2)
		{
			CMLib.commands().doCommandFail(mob,origCmds,L("Remove what?"));
			return false;
		}
		commands.remove(0);
		if(commands.get(0) instanceof Item)
		{
			final boolean quiet=((commands.size()>1)&&(commands.lastElement() instanceof String)&&(((String)commands.lastElement()).equalsIgnoreCase("QUIETLY")));
			final Item item=(Item)commands.get(0);
			final CMMsg newMsg=CMClass.getMsg(mob,item,null,CMMsg.MSG_REMOVE,quiet?null:L("<S-NAME> remove(s) <T-NAME>."));
			if(mob.location().okMessage(mob,newMsg))
			{
				mob.location().send(mob,newMsg);
				return true;
			}
			return false;
		}

		final List<Item> items=CMLib.english().fetchItemList(mob,mob,null,commands,Wearable.FILTER_WORNONLY,false);
		if(items.size()==0)
			CMLib.commands().doCommandFail(mob,origCmds,L("You don't seem to be wearing that."));
		else
		for(int i=0;i<items.size();i++)
		{
			final Item item=items.get(i);
			final CMMsg newMsg=CMClass.getMsg(mob,item,null,CMMsg.MSG_REMOVE,L("<S-NAME> remove(s) <T-NAME>."));
			if(mob.location().okMessage(mob,newMsg))
				mob.location().send(mob,newMsg);
		}
		return false;
	}
	@Override public double combatActionsCost(final MOB mob, final List<String> cmds){return CMProps.getCommandCombatActionCost(ID());}
	@Override public double actionsCost(final MOB mob, final List<String> cmds){return CMProps.getCommandActionCost(ID());}
	@Override public boolean canBeOrdered(){return true;}


}
