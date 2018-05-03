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

public class Throw extends StdCommand
{
	public Throw()
	{
	}

	private final String[]	access	= I(new String[] { "THROW", "TOSS" });

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
		if((commands.size()==2)&&(mob.isInCombat()))
			commands.add(mob.getVictim().location().getContextName(mob.getVictim()));
		if(commands.size()<3)
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("Throw what, where or at whom?"));
			return false;
		}
		commands.remove(0);
		final String str=commands.get(commands.size()-1);
		commands.remove(str);
		final String what=CMParms.combine(commands,0);
		Item item=mob.fetchItem(null,Wearable.FILTER_WORNONLY,what);
		if(item==null)
			item=mob.findItem(null,what);
		if((item==null)||(!CMLib.flags().canBeSeenBy(item,mob)))
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("You don't seem to have a '@x1'!",what));
			return false;
		}
		if((!item.amWearingAt(Wearable.WORN_HELD))&&(!item.amWearingAt(Wearable.WORN_WIELD)))
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("You aren't holding or wielding @x1!",item.name()));
			return false;
		}

		final int dir=CMLib.directions().getGoodDirectionCode(str);
		Environmental target=null;
		if(dir<0)
			target=mob.location().fetchInhabitant(str);
		else
		{
			target=mob.location().getRoomInDir(dir);
			if((target==null)
			||(mob.location().getExitInDir(dir)==null)
			||(!mob.location().getExitInDir(dir).isOpen()))
			{
				if(CMLib.flags().isFloatingFreely(mob))
				{
					target=mob.location();
				}
				else
				{
					CMLib.commands().postCommandFail(mob,origCmds,L("You can't throw anything that way!"));
					return false;
				}
			}
			final boolean amOutside=((mob.location().domainType()&Room.INDOORS)==0);
			final boolean isOutside=((((Room)target).domainType()&Room.INDOORS)==0);
			final boolean isUp=(mob.location().getRoomInDir(Directions.UP)==target);
			final boolean isDown=(mob.location().getRoomInDir(Directions.DOWN)==target);

			if(amOutside&&isOutside&&(!isUp)&&(!isDown)
			&&((((Room)target).domainType()&Room.DOMAIN_OUTDOORS_AIR)==0))
			{
				CMLib.commands().postCommandFail(mob,origCmds,L("That's too far to throw @x1.",item.name()));
				return false;
			}
		}
		if((dir<0)&&((target==null)||((target!=mob.getVictim())&&(!CMLib.flags().canBeSeenBy(target,mob)))))
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("You can't target @x1 at '@x2'!",item.name(),str));
			return false;
		}

		if(!(target instanceof Room))
		{
			final CMMsg newMsg=CMClass.getMsg(mob,item,null,CMMsg.MSG_REMOVE,null);
			if(mob.location().okMessage(mob,newMsg))
			{
				mob.location().send(mob,newMsg);
				int targetMsg=CMMsg.MSG_THROW;
				if(target instanceof MOB)
				{
					if(item instanceof Weapon)
						targetMsg=CMMsg.MSG_WEAPONATTACK;
					else
					if(item instanceof SpellHolder)
					{
						final List<Ability> V=((SpellHolder)item).getSpells();
						for(int v=0;v<V.size();v++)
						{
							if(V.get(v).abstractQuality()==Ability.QUALITY_MALICIOUS)
							{
								targetMsg=CMMsg.MSG_WEAPONATTACK;
								break;
							}
						}
					}
				}
				final CMMsg msg=CMClass.getMsg(mob,target,item,CMMsg.MSG_THROW,targetMsg,CMMsg.MSG_THROW,L("<S-NAME> throw(s) <O-NAME> at <T-NAMESELF>."));
				if(mob.location().okMessage(mob,msg))
					mob.location().send(mob,msg);
			}
		}
		else
		{
			final boolean useShipDirs=((mob.location() instanceof BoardableShip)||(mob.location().getArea() instanceof BoardableShip));
			final int opDir=Directions.getOpDirectionCode(dir);
			final String inDir=useShipDirs?CMLib.directions().getShipInDirectionName(dir):CMLib.directions().getInDirectionName(dir);
			final String fromDir=useShipDirs?CMLib.directions().getFromShipDirectionName(opDir):CMLib.directions().getFromCompassDirectionName(opDir);
			final CMMsg msg=CMClass.getMsg(mob,target,item,CMMsg.MSG_THROW,L("<S-NAME> throw(s) <O-NAME> @x1.",inDir.toLowerCase()));
			final CMMsg msg2=CMClass.getMsg(mob,target,item,CMMsg.MSG_THROW,L("<O-NAME> fl(ys) in from @x1.",fromDir.toLowerCase()));
			if(mob.location()==target)
			{
				if(mob.location().okMessage(mob,msg))
					mob.location().send(mob,msg);
			}
			else
			{
				if(mob.location().okMessage(mob,msg)&&((Room)target).okMessage(mob,msg2))
				{
					mob.location().send(mob,msg);
					((Room)target).sendOthers(mob,msg2);
				}
			}
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
