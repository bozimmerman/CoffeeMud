package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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

public class Kill extends StdCommand
{
	public Kill()
	{
	}

	private final String[]	access	= I(new String[] { "KILL", "K", "ATTACK" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		if(commands==null)
		{
			if(mob.isInCombat())
			{
				CMLib.combat().postAttack(mob,mob.getVictim(),mob.fetchWieldedItem());
				return true;
			}
			return false;
		}

		Vector<String> origCmds=new XVector<String>(commands);
		MOB target=null;
		if(commands.size()<2)
		{
			if(!mob.isInCombat())
			{
				CMLib.commands().postCommandFail(mob,origCmds,L("Kill whom?"));
				return false;
			}
			else
			if(CMProps.getIntVar(CMProps.Int.COMBATSYSTEM)==CombatLibrary.CombatSystem.DEFAULT.ordinal())
				return false;
			else
				target=mob.getVictim();
		}

		boolean reallyKill=false;
		String whomToKill=CMParms.combine(commands,1);
		if(CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.KILLDEAD)&&(!mob.isMonster()))
		{
			if(commands.get(commands.size()-1).equalsIgnoreCase("DEAD"))
			{
				commands.remove(commands.size()-1);
				whomToKill=CMParms.combine(commands,1);
				reallyKill=true;
			}
		}

		if(target==null)
		{
			target=mob.location().fetchInhabitant(whomToKill);
			if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
			{
				CMLib.commands().postCommandFail(mob,origCmds,L("I don't see '@x1' here.",whomToKill));
				return false;
			}
		}

		if(reallyKill)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,null,CMMsg.MSG_OK_ACTION,L("^F^<FIGHT^><S-NAME> touch(es) <T-NAMESELF>.^</FIGHT^>^?"));
			CMLib.color().fixSourceFightColor(msg);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				target.curState().setHitPoints(0);
				CMLib.combat().postDeath(mob,target,null);
			}
			return false;
		}

		if(mob.isInCombat())
		{
			final MOB oldVictim=mob.getVictim();
			if(((oldVictim!=null)&&(oldVictim==target)
			&&(CMProps.getIntVar(CMProps.Int.COMBATSYSTEM)==CombatLibrary.CombatSystem.DEFAULT.ordinal())))
			{
				CMLib.commands().postCommandFail(mob,origCmds,L("^f^<FIGHT^>You are already fighting @x1.^</FIGHT^>^?",mob.getVictim().name()));
				return false;
			}

			if((mob.location().okMessage(mob,CMClass.getMsg(mob,target,CMMsg.MSG_WEAPONATTACK,null)))
			&&(oldVictim!=target))
			{
				if((oldVictim!=null)
				&&(target.getVictim()==oldVictim.getVictim())
				&&(target.rangeToTarget()>=0)
				&&(oldVictim.rangeToTarget()>=0))
				{
					int range=target.rangeToTarget()-oldVictim.rangeToTarget();
					if(mob.rangeToTarget()>=0)
						range+=mob.rangeToTarget();
					if(range>=0)
						mob.setRangeToTarget(range);
				}
				CMLib.commands().postCommandFail(mob,origCmds,L("^f^<FIGHT^>You are now targeting @x1.^</FIGHT^>^?",target.name(mob)));
				mob.setVictim(target);
				return false;
			}
		}

		if(!mob.mayPhysicallyAttack(target))
		{
			// some properties may be protecting the target -- give them a chance to complain
			final CMMsg msg=CMClass.getMsg(mob,target,CMMsg.MSG_NOISYMOVEMENT|CMMsg.MASK_MALICIOUS,null);
			final Room R=target.location();
			if((R==null)||(R.okMessage(mob, msg)))
				CMLib.commands().postCommandFail(mob,origCmds,L("You are not allowed to attack @x1.",target.name(mob)));
		}
		else
		{
			final Item weapon=mob.fetchWieldedItem();
			if(weapon==null)
			{
				final Item possibleOtherWeapon=mob.fetchHeldItem();
				if((possibleOtherWeapon!=null)
				&&(possibleOtherWeapon instanceof Weapon)
				&&possibleOtherWeapon.fitsOn(Wearable.WORN_WIELD)
				&&(CMLib.flags().canBeSeenBy(possibleOtherWeapon,mob))
				&&(CMLib.flags().isRemovable(possibleOtherWeapon)))
				{
					CMLib.commands().postRemove(mob,possibleOtherWeapon,false);
					if(possibleOtherWeapon.amWearingAt(Wearable.IN_INVENTORY))
					{
						final Command C=CMClass.getCommand("Wield");
						if(C!=null)
							C.executeInternal(mob,metaFlags,possibleOtherWeapon);
					}
				}
			}
			CMLib.combat().postAttack(mob,target,mob.fetchWieldedItem());
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
