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

public class Dig extends StdCommand
{
	public Dig()
	{
	}

	private final String[]	access	= I(new String[] { "DIG" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	public int getDiggingDepth(Item item)
	{
		if(item==null)
			return 1;
		switch(item.material()&RawMaterial.MATERIAL_MASK)
		{
		case RawMaterial.MATERIAL_METAL:
		case RawMaterial.MATERIAL_MITHRIL:
		case RawMaterial.MATERIAL_WOODEN:
			if(item.Name().toLowerCase().indexOf("shovel")>=0)
				return 5+item.phyStats().weight();
			return 1+(item.phyStats().weight()/5);
		case RawMaterial.MATERIAL_SYNTHETIC:
		case RawMaterial.MATERIAL_ROCK:
		case RawMaterial.MATERIAL_GLASS:
			if(item.Name().toLowerCase().indexOf("shovel")>=0)
				return 14+item.phyStats().weight();
			return 1+(item.phyStats().weight()/7);
		default:
			return 1;
		}
	}

	public boolean isOccupiedWithOtherWork(MOB mob)
	{
		if(mob==null)
			return false;
		for(final Enumeration<Ability> a=mob.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)
			&&(!A.isAutoInvoked())
			&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_COMMON_SKILL))
				return true;
		}
		return false;
	}

	@Override
	public boolean preExecute(MOB mob, List<String> commands, int metaFlags, int secondsElapsed, double actionsRemaining)
	throws java.io.IOException
	{
		
		if(secondsElapsed==0)
		{
			if(isOccupiedWithOtherWork(mob))
			{
				CMLib.commands().postCommandFail(mob,new StringXVector(commands),L("You are too busy to dig right now."));
				return false;
			}
			if(mob.isInCombat())
			{
				CMLib.commands().postCommandFail(mob,new StringXVector(commands),L("You are too busy fighting right now."));
				return false;
			}

			final String msgStr=L("<S-NAME> start(s) digging a hole with <O-NAME>.");
			Item I=mob.fetchWieldedItem();
			if(I==null)
				I=mob.getNaturalWeapon();
			final CMMsg msg=CMClass.getMsg(mob,mob.location(),I,CMMsg.MSG_DIG,msgStr);
			msg.setValue(1);
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
			else
				return false;
		}
		else
		if((secondsElapsed % 8)==0)
		{
			if(mob.isInCombat())
			{
				CMLib.commands().postCommandFail(mob,new StringXVector(commands),L("You stop digging."));
				return false;
			}
			final String msgStr=L("<S-NAME> continue(s) digging a hole with <O-NAME>.");
			Item I=mob.fetchWieldedItem();
			if(I==null)
				I=mob.getNaturalWeapon();
			final CMMsg msg=CMClass.getMsg(mob,mob.location(),I,CMMsg.MSG_DIG,msgStr);
			msg.setValue(getDiggingDepth(I));
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
			else
				return false;
		}
		return true;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		final CMMsg msg=CMClass.getMsg(mob,null,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> stop(s) digging."));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			mob.clearCommandQueue();
		}
		return false;
	}

	@Override
	public double combatActionsCost(final MOB mob, final List<String> cmds)
	{
		return 30.0 * mob.phyStats().speed();
	}

	@Override
	public double actionsCost(final MOB mob, final List<String> cmds)
	{
		return 10.0 * mob.phyStats().speed();
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}
}
