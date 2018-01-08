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
   Copyright 2011-2018 Bo Zimmerman

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

public class Duel extends StdCommand
{
	public Duel()
	{
	}
	
	private final String[]	access	= I(new String[] { "DUEL" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		MOB target=null;
		if(commands.size()<2)
		{
			mob.tell(L("Duel whom?"));
			return false;
		}

		final String whomToKill=CMParms.combine(commands,1);
		target=mob.location().fetchInhabitant(whomToKill);
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			mob.tell(L("I don't see '@x1' here.",whomToKill));
			return false;
		}

		if(mob==target)
			mob.tell(L("You may not duel yourself."));
		else
		if((mob.isMonster()))
			mob.tell(L("You are not allowed to duel @x1.",target.name(mob)));
		else
		{
			final Tattoo uiT=target.findTattoo("IDUEL");
			final Tattoo uuT=target.findTattoo("UDUEL");
			final Tattoo iiT=mob.findTattoo("IDUEL");
			final Tattoo iuT=mob.findTattoo("UDUEL");
			if((uiT==null)&&(iiT==null)&&(uuT==null)&&(iuT==null))
			{
				final int duelTicks=CMProps.getIntVar(CMProps.Int.DUELTICKDOWN);
				mob.addTattoo("IDUEL",duelTicks);
				target.addTattoo("UDUEL",duelTicks);
				final long time = CMProps.getTickMillis() * duelTicks;
				mob.location().show(mob, target, CMMsg.MSG_DUELCHALLENGE, L("^X<S-NAME> <S-HAS-HAVE> challenged <T-NAME> to a duel, which <T-HE-SHE> <T-HAS-HAVE> @x1 seconds to consider.^.^N",""+(time/1000)));
				target.tell(L("^NEnter ^HDUEL @x1^N to accept this challenge and begin fighting.",mob.name(target)));
				return true;
			}
			else
			if((uiT != null)&&(iuT != null))
			{
				target.tell(mob,target,null,L("^X<T-NAME> <T-HAS-HAVE> ACCEPTED <T-YOUPOSS> CHALLENGE!^.^N"));
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
				final Ability A=CMClass.getAbility("Dueler");
				if(A!=null)
					A.invoke(target, mob, true, 0);
			}
			else
			if(uiT!=null)
			{
				mob.tell(mob,target,null,L("<T-NAME> is awaiting a response to a previous challenge and cannot be challenged at this time."));
				return false;
			}
			else
			if(uuT!=null)
			{
				mob.tell(mob,target,null,L("<T-NAME> is considering a response to a previous challenger and cannot be challenged at this time."));
				return false;
			}
			else
			if((iuT!=null)||(iiT!=null))
			{
				final int duelTicks=CMProps.getIntVar(CMProps.Int.DUELTICKDOWN);
				final long time = CMProps.getTickMillis() * duelTicks;
				mob.tell(mob,target,null,L("Your previous challenge has not yet expired.  Please wait @x1 seconds longer and try again.",""+(time/1000)));
				return false;
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
