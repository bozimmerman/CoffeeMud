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

public class Pose extends StdCommand
{
	public Pose(){}

	private final String[] access=I(new String[]{"POSE","NOPOSE"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		if((commands.size()>0)&&(commands.get(0).toString().equalsIgnoreCase("NOPOSE")))
		{
			final PlayerStats pstats = mob.playerStats();
			if(pstats != null)
			{
				if((pstats.getSavedPose()==null)||(pstats.getSavedPose().length()==0))
				{
					mob.tell(L("You are not currently posing."));
					return false;
				}
				pstats.setSavedPose("");
				mob.setDisplayText("");
				mob.tell(L("You stop posing."));
			}
			return false;
		}
		if(commands.size()<2)
		{
			if(mob.displayText().length()==0)
				mob.tell(L("POSE how?"));
			else
				mob.tell(L("Your current pose is: @x1",mob.displayText(mob)));
			return false;
		}
		String combinedCommands=CMParms.combine(commands,1);
		combinedCommands=CMProps.applyINIFilter(combinedCommands,CMProps.Str.POSEFILTER);
		if(combinedCommands.trim().startsWith("'")||combinedCommands.trim().startsWith("`"))
			combinedCommands=combinedCommands.trim();
		else
			combinedCommands=" "+combinedCommands.trim();
		final String emote="^E<S-NAME>"+combinedCommands+" ^?";
		final CMMsg msg=CMClass.getMsg(mob,null,null,CMMsg.MSG_EMOTE | CMMsg.MASK_ALWAYS,L("^E@x1@x2 ^?",mob.name(),combinedCommands),emote,emote);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			mob.setDisplayText(mob.Name()+combinedCommands);
			final PlayerStats pstats = mob.playerStats();
			if(pstats != null)
				pstats.setSavedPose(mob.Name()+combinedCommands);
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
