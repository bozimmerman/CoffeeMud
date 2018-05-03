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
   Copyright 2001-2018 Bo Zimmerman

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

public class Dress extends StdCommand
{
	public Dress(){}

	private final String[] access=I(new String[]{"DRESS"});
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
			CMLib.commands().postCommandFail(mob,origCmds,L("Dress whom in what?"));
			return false;
		}
		if(mob.isInCombat())
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("Not while you are in combat!"));
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
		if((target.willFollowOrdersOf(mob))||(CMLib.flags().isBoundOrHeld(target)))
		{
			final Item item=mob.findItem(null,what);
			if((item==null)||(!CMLib.flags().canBeSeenBy(item,mob)))
			{
				CMLib.commands().postCommandFail(mob,origCmds,L("I don't see @x1 here.",what));
				return false;
			}
			if(CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.ORDER)
			||(CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDROOMS)&&(target.isMonster()))
			||(CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDMOBS)&&(target.isMonster())))
			{
				mob.location().show(mob,target,item,CMMsg.MASK_ALWAYS|CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> mystically put(s) <O-NAME> on <T-NAMESELF>."));
				item.unWear();
				target.moveItemTo(item);
				item.wearIfPossible(target);
				if(item.rawWornCode()!=0)
					target.executeMsg(target, CMClass.getMsg(target, item,CMMsg.MSG_WEAR|CMMsg.MASK_ALWAYS, null));
				if((item.rawProperLocationBitmap()!=0)&&(item.amWearingAt(Wearable.IN_INVENTORY))&&(target.isMonster()))
				{
					if(item.rawLogicalAnd())
						item.wearAt(item.rawProperLocationBitmap());
					else
					{
						for(final long wornCode : Wearable.CODES.ALL())
						{
							if(wornCode != Wearable.IN_INVENTORY)
							{
								if(item.fitsOn(wornCode)&&(wornCode!=Wearable.WORN_HELD))
								{
									item.wearAt(wornCode);
									break;
								}
							}
						}
						if(item.amWearingAt(Wearable.IN_INVENTORY))
							item.wearAt(Wearable.WORN_HELD);
					}
				}
				target.location().recoverRoomStats();
			}
			else
			{
				if(!item.amWearingAt(Wearable.IN_INVENTORY))
				{
					CMLib.commands().postCommandFail(mob,origCmds,L("You might want to remove that first."));
					return false;
				}
				if(item instanceof Coins)
				{
					CMLib.commands().postCommandFail(mob,origCmds,L("I don't think you want to dress someone in @x1.",item.name()));
					return false;
				}
				if(target.isInCombat())
				{
					CMLib.commands().postCommandFail(mob,origCmds,L("Not while @x1 is in combat!",target.name(mob)));
					return false;
				}
				CMMsg msg=CMClass.getMsg(mob,target,null,CMMsg.MSG_QUIETMOVEMENT,null);
				if(mob.location().okMessage(mob,msg))
				{
					if(CMLib.commands().postDrop(mob,item,true,false,false))
					{
						msg=CMClass.getMsg(target,item,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_GET,CMMsg.MSG_GET,CMMsg.MSG_GET,null);
						if(mob.location().okMessage(mob,msg))
						{
							mob.location().send(mob,msg);
							msg=CMClass.getMsg(target,item,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_WEAR,CMMsg.MSG_WEAR,CMMsg.MSG_WEAR,null);
							if(mob.location().okMessage(mob,msg))
							{
								mob.location().send(mob,msg);
								mob.location().show(mob,target,item,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> put(s) <O-NAME> on <T-NAMESELF>."));
							}
							else
								CMLib.commands().postCommandFail(mob,origCmds,L("You cannot seem to get @x1 on @x2.",item.name(),target.name(mob)));
						}
						else
							CMLib.commands().postCommandFail(mob,origCmds,L("You cannot seem to get @x1 to @x2.",item.name(),target.name(mob)));
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
