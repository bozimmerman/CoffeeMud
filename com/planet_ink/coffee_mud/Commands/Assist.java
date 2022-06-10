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
   Copyright 2004-2022 Bo Zimmerman

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
public class Assist extends StdCommand
{
	public Assist()
	{
	}

	private final String[] access=I(new String[]{"ASSIST"});
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
		if(commands.size()<2)
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("Assist whom?"));
			return false;
		}
		commands.remove(0);
		final MOB recipientM=mob.location().fetchInhabitant(CMParms.combine(commands,0));
		if((recipientM==null)||(!CMLib.flags().canBeSeenBy(recipientM,mob)))
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("I don't see @x1 here.",CMParms.combine(commands,0)));
			return false;
		}
		final MOB vicM = recipientM.getVictim();
		if(recipientM.isInCombat() && (vicM!=null))
		{
			final Room vicR=vicM.location();
			if(mob.getVictim()==vicM)
			{
				CMLib.commands().postCommandFail(mob,origCmds,L("You are already assisting @x1.",recipientM.name(mob)));
				return false;
			}
			if((vicR==null)||(!CMLib.flags().canBeSeenBy(vicM, mob)))
			{
				CMLib.commands().postCommandFail(mob,origCmds,L("I don't see who @x1 is fighting here.",recipientM.name(mob)));
				return false;
			}
			final String perfectTargetName = vicR.getContextName(vicM);
			final Command C=CMClass.getCommand("Kill");
			return C.execute(mob, new XVector<String>("KILL", perfectTargetName), metaFlags);
		}
		Ability commonA = null;
		for(final Enumeration<Ability> e=recipientM.personalEffects();e.hasMoreElements();)
		{
			final Ability A=e.nextElement();
			if((A instanceof CraftorAbility)
			&&(A.canBeUninvoked())
			&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_COMMON_SKILL)
			&&((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_BUILDINGSKILL))
			{
				final Ability myA=mob.fetchAbility(A.ID());
				if(myA!=null)
					commonA=myA;
			}
		}
		if(commonA != null)
		{
			final String recipientName=mob.location().getContextName(recipientM);
			return commonA.invoke(mob, new XVector<String>("HELP",recipientName), null, false, 0);
		}

		final Pair<Object, List<String>> cmd = recipientM.getTopCommand();
		if((cmd != null)
		&&(cmd.first instanceof Command)
		&&(((Command)cmd.first).ID().equals("Push")||((Command)cmd.first).ID().equals("Pull")))
		{
			mob.clearCommandQueue();
			mob.enqueCommand(cmd.second, metaFlags, 0.0);
			return true;
		}

		CMLib.commands().postCommandFail(mob,origCmds,L("@x1 doesn't seem to be doing anything you can assist @x2 with.",recipientM.name(mob),recipientM.charStats().himher()));
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

