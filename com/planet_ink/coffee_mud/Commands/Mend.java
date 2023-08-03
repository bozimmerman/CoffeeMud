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
   Copyright 2023-2023 Bo Zimmerman

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
public class Mend extends StdCommand
{
	public Mend()
	{
	}

	private final String[]	access	= I(new String[] { "MEND" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		final Vector<String> origCmds=new XVector<String>(commands);
		final String whatToMend=CMParms.combine(commands,1);
		if(whatToMend.length()==0)
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("Mend what?"));
			return false;
		}
		final Physical mendThis=mob.location().fetchFromMOBRoomFavorsItems(mob, null, whatToMend, Wearable.FILTER_UNWORNONLY);
		if((mendThis==null)
		||(!CMLib.flags().canBeSeenBy(mendThis,mob)))
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("You don't seem to have '@x1'.",whatToMend));
			return false;
		}
		if((mendThis instanceof Item) && ((Item)mendThis).amBeingWornProperly())
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("You need to remove '@x1' first.",whatToMend));
			return false;
		}
		final List<MendingSkill> choices = new ArrayList<MendingSkill>();
		for(final Enumeration<Ability> a = mob.allAbilities();a.hasMoreElements();)
		{
			final Ability A = a.nextElement();
			if(A instanceof MendingSkill)
			{
				final MendingSkill cA = (MendingSkill)A;
				if(cA.supportsMending(mendThis))
					choices.add(cA);
			}
		}
		final MendingSkill skillA = (choices.size()==0)?null:choices.get(CMLib.dice().roll(1, choices.size(), -1));
		if(skillA == null)
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("You don't know how to mend @x1.", mendThis.name(mob)));
			return false;
		}
		final List<String> mendCmds;
		if(skillA instanceof CraftorAbility)
			mendCmds = new XVector<String>("MEND", mob.getContextName(mendThis));
		else
			mendCmds = new XVector<String>(mob.getContextName(mendThis));
		return skillA.invoke(mob, mendCmds, mendThis, false, 0);
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

	@Override
	public boolean putInCommandlist()
	{
		return true;
	}

	@Override
	public boolean securityCheck(final MOB mob)
	{
		return true;
	}
}
