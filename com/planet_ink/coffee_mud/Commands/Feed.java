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

public class Feed extends StdCommand
{
	public Feed(){}

	private final String[] access=I(new String[]{"FEED"});
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
		if(commands.size()<3)
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("Feed who what?"));
			return false;
		}
		commands.remove(0);
		final String what=commands.get(commands.size()-1);
		commands.remove(what);
		final String whom=CMParms.combine(commands,0);
		final MOB target=mob.location().fetchInhabitant(whom);
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("I don't see @x1 here.",whom));
			return false;
		}
		if(mob.isInCombat())
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("Not while you are in combat!"));
			return false;
		}
		if(target.willFollowOrdersOf(mob)||(CMLib.flags().isBoundOrHeld(target)))
		{
			final Item item=mob.findItem(null,what);
			if((item==null)||(!CMLib.flags().canBeSeenBy(item,mob)))
			{
				CMLib.commands().postCommandFail(mob,origCmds,L("I don't see @x1 here.",what));
				return false;
			}
			if(!item.amWearingAt(Wearable.IN_INVENTORY))
			{
				CMLib.commands().postCommandFail(mob,origCmds,L("You might want to remove that first."));
				return false;
			}
			if((!(item instanceof Food))&&(!(item instanceof Drink)))
			{
				CMLib.commands().postCommandFail(mob,origCmds,L("You might want to try feeding them something edibile or drinkable."));
				return false;
			}
			if(target.isInCombat())
			{
				CMLib.commands().postCommandFail(mob,origCmds,L("Not while @x1 is in combat!",target.name(mob)));
				return false;
			}
			CMMsg msg=CMClass.getMsg(mob,target,item,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> feed(s) @x1 to <T-NAMESELF>.",item.name()));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if((CMLib.commands().postDrop(mob,item,true,false,false))
				   &&(mob.location().isContent(item)))
				{
					msg=CMClass.getMsg(target,item,CMMsg.MASK_ALWAYS|CMMsg.MSG_GET,null);
					target.location().send(target,msg);
					if(target.isMine(item))
					{
						if(item instanceof Food)
							msg=CMClass.getMsg(target,item,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_EAT,CMMsg.MSG_EAT,CMMsg.MSG_EAT,null);
						else
							msg=CMClass.getMsg(target,item,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_DRINK,CMMsg.MSG_DRINK,CMMsg.MSG_DRINK,null);
						if(target.location().okMessage(target,msg))
							target.location().send(target,msg);
						if(target.isMine(item))
						{
							msg=CMClass.getMsg(target,item,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_DROP,CMMsg.MSG_DROP,CMMsg.MSG_DROP,null);
							if(mob.location().okMessage(mob,msg))
							{
								mob.location().send(mob,msg);
								CMLib.commands().postGet(mob,null,item,true);
							}
						}
					}
				}
			}
		}
		else
			CMLib.commands().postCommandFail(mob,origCmds,L("@x1 won't let you.",target.name(mob)));
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
