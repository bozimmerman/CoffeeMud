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
@SuppressWarnings("rawtypes")
public class Undress extends StdCommand
{
	public Undress(){}

	private final String[] access={"UNDRESS"};
	@Override public String[] getAccessWords(){return access;}
	@Override
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(commands.size()<3)
		{
			mob.tell(_("Undress whom? What would you like to remove?"));
			return false;
		}
		if(mob.isInCombat())
		{
			mob.tell(_("Not while you are in combat!"));
			return false;
		}
		commands.removeElementAt(0);
		final String what=(String)commands.lastElement();
		commands.removeElement(what);
		final String whom=CMParms.combine(commands,0);
		final MOB target=mob.location().fetchInhabitant(whom);
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			mob.tell(_("I don't see @x1 here.",whom));
			return false;
		}
		if(target.willFollowOrdersOf(mob)||(CMLib.flags().isBoundOrHeld(target)))
		{
			final Item item=target.findItem(null,what);
			if((item==null)
			   ||(!CMLib.flags().canBeSeenBy(item,mob))
			   ||(item.amWearingAt(Wearable.IN_INVENTORY)))
			{
				mob.tell(_("@x1 doesn't seem to be equipped with '@x2'.",target.name(mob),what));
				return false;
			}
			if(target.isInCombat())
			{
				mob.tell(_("Not while @x1 is in combat!",target.name(mob)));
				return false;
			}
			CMMsg msg=CMClass.getMsg(mob,target,null,CMMsg.MSG_QUIETMOVEMENT,null);
			if(mob.location().okMessage(mob,msg))
			{
				msg=CMClass.getMsg(target,item,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_REMOVE,CMMsg.MSG_REMOVE,CMMsg.MSG_REMOVE,null);
				if(mob.location().okMessage(mob,msg))
				{
					mob.location().send(mob,msg);
					msg=CMClass.getMsg(target,item,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_DROP,CMMsg.MSG_DROP,CMMsg.MSG_DROP,null);
					if(mob.location().okMessage(mob,msg))
					{
						mob.location().send(mob,msg);
						if(CMLib.commands().postGet(mob,null,item,true))
							mob.location().show(mob,target,item,CMMsg.MASK_ALWAYS|CMMsg.MSG_QUIETMOVEMENT,_("<S-NAME> take(s) <O-NAME> off <T-NAMESELF>."));
					}
					else
						mob.tell(_("You cannot seem to get @x1 off @x2.",item.name(),target.name(mob)));
				}
				else
					mob.tell(_("You cannot seem to get @x1 off of @x2.",item.name(),target.name(mob)));
			}
		}
		else
			mob.tell(_("@x1 won't let you.",target.name(mob)));
		return false;
	}
	@Override public double combatActionsCost(final MOB mob, final List<String> cmds){return CMProps.getCombatActionCost(ID());}
	@Override public double actionsCost(final MOB mob, final List<String> cmds){return CMProps.getActionCost(ID());}
	@Override public boolean canBeOrdered(){return true;}


}
