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
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary.ExpertiseDefinition;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.util.Map.Entry;

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

public class Teach extends StdCommand
{
	public Teach()
	{
	}

	private final String[]	access	= I(new String[] { "TEACH" });

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
			CMLib.commands().postCommandFail(mob,origCmds,L("Teach who what?"));
			return false;
		}
		commands.remove(0);

		final MOB student=mob.location().fetchInhabitant(commands.get(0));
		if((student==null)||(!CMLib.flags().canBeSeenBy(student,mob)))
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("That person doesn't seem to be here."));
			return false;
		}
		commands.remove(0);

		final String abilityName=CMParms.combine(commands,0);
		final Ability realAbility=CMClass.findAbility(abilityName,student.charStats());
		Ability myAbility=null;
		if(realAbility!=null)
			myAbility=mob.fetchAbility(realAbility.ID());
		else
			myAbility=mob.findAbility(abilityName);
		if(myAbility==null)
		{
			ExpertiseLibrary.ExpertiseDefinition theExpertise=null;
			final List<ExpertiseDefinition> V=CMLib.expertises().myListableExpertises(mob);
			for(final Enumeration<String> exi=mob.expertises();exi.hasMoreElements();)
			{
				final Pair<String,Integer> e=mob.fetchExpertise(exi.nextElement());
				final List<String> codes = CMLib.expertises().getStageCodes(e.getKey());
				if((codes==null)||(codes.size()==0))
					V.add(CMLib.expertises().getDefinition(e.getKey()));
				else
				for(final String ID : codes)
				{
					final ExpertiseLibrary.ExpertiseDefinition def=CMLib.expertises().getDefinition(ID);
					if((def != null) && (!V.contains(def)))
						V.add(def);
				}
			}
			for(int v=0;v<V.size();v++)
			{
				final ExpertiseLibrary.ExpertiseDefinition def=V.get(v);
				if((def.name().equalsIgnoreCase(abilityName))
				&&(theExpertise==null))
					theExpertise=def;
			}
			if(theExpertise==null)
			{
				for(int v=0;v<V.size();v++)
				{
					final ExpertiseLibrary.ExpertiseDefinition def=V.get(v);
					if((CMLib.english().containsString(def.name(),abilityName)
					&&(theExpertise==null)))
						theExpertise=def;
				}
			}
			if(theExpertise!=null)
			{
				return CMLib.expertises().postTeach(mob,student,theExpertise);
			}
			CMLib.commands().postCommandFail(mob,origCmds,L("You don't seem to know @x1.",abilityName));
			return false;
		}
		return CMLib.expertises().postTeach(mob,student,myAbility);
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
