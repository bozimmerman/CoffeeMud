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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/*
   Copyright 2000-2014 Bo Zimmerman

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
public class Wield extends StdCommand
{
	public Wield(){}

	private final String[] access=_i(new String[]{"WIELD"});
	@Override public String[] getAccessWords(){return access;}

	@Override
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			mob.tell(L("Wield what?"));
			return false;
		}
		commands.removeElementAt(0);
		List<Item> items=null;
		if(commands.elementAt(0) instanceof Item)
		{
			items=new Vector();
			for(int i=0;i<commands.size();i++)
				if(commands.elementAt(i) instanceof Item)
					items.add((Item)commands.elementAt(i));
		}
		else
			items=CMLib.english().fetchItemList(mob,mob,null,commands,Wearable.FILTER_UNWORNONLY,false);
		if(items.size()==0)
			mob.tell(L("You don't seem to be carrying that."));
		else
		for(int i=0;i<items.size();i++)
			if((items.size()==1)||(items.get(i).canWear(mob,Wearable.WORN_WIELD)))
			{
				final Item item=items.get(i);
				final CMMsg newMsg=CMClass.getMsg(mob,item,null,CMMsg.MSG_WIELD,L("<S-NAME> wield(s) <T-NAME>."));
				if(mob.location().okMessage(mob,newMsg))
					mob.location().send(mob,newMsg);
			}
		return false;
	}
	@Override public double combatActionsCost(final MOB mob, final List<String> cmds){return CMProps.getCommandCombatActionCost(ID());}
	@Override public double actionsCost(final MOB mob, final List<String> cmds){return CMProps.getCommandActionCost(ID());}
	@Override public boolean canBeOrdered(){return true;}


}
