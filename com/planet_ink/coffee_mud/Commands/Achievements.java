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
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Achievement;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
 Copyright 2004-2015 Bo Zimmerman

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
@SuppressWarnings("rawtypes")
public class Achievements extends StdCommand
{
	private final String[]	access	= I(new String[] { "ACHIEVEMENTS" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(final MOB mob, Vector commands, int metaFlags) throws java.io.IOException
	{
		String rest = CMParms.combine(commands,1);
		String prefix = "";
		final PlayerStats pStats = mob.playerStats();
		if (pStats == null)
		{
			mob.tell(L("You don't have any achievements."));
			return false;
		}
		
		//TODO: ANNOUNCE
		//TODO: a name to see the achievements of others.
		//TODO: add a room entry achievement
		//TODO: add channel flag for channel announces
		
		List<String> AchievedList = new Vector<String>();
		if(rest.toUpperCase().startsWith("ALL"))
		{
			prefix=L("All ");
			String done=L("DONE!");
			int padding=done.length()+1;
			for(Enumeration<Achievement> a=CMLib.achievements().achievements();a.hasMoreElements();)
			{
				final Achievement A=a.nextElement();
				if(mob.findTattoo(A.getTattoo()) == null)
				{
					AchievementLibrary.Tracker T=pStats.getAchievementTracker(A, mob);
					final int score = (T==null) ? 0 : T.getCount(mob);
					final int targetScore = A.getTargetCount();
					final int len = (""+score+"/"+targetScore).length(); 
					if(len >= padding)
						padding = len+1;
				}
			}
			for(Enumeration<Achievement> a=CMLib.achievements().achievements();a.hasMoreElements();)
			{
				final Achievement A=a.nextElement();
				if(mob.findTattoo(A.getTattoo()) != null)
					AchievedList.add(CMStrings.padRight("^H"+done+"^?", padding)+": "+A.getDisplayStr());
				else
				{
					AchievementLibrary.Tracker T=pStats.getAchievementTracker(A, mob);
					int score = (T==null) ? 0 : T.getCount(mob);
					int targetScore = A.getTargetCount();
					AchievedList.add(CMStrings.padRight("^w"+score+"/"+targetScore, padding)+"^?: "+A.getDisplayStr());
				}
			}
			
		}
		else
		{
			for(Enumeration<Achievement> a=CMLib.achievements().achievements();a.hasMoreElements();)
			{
				final Achievement A=a.nextElement();
				if(mob.findTattoo(A.getTattoo()) != null)
				{
					AchievedList.add(A.getDisplayStr());
				}
			}
		}
		if(AchievedList.size()==0)
			mob.tell("^H"+prefix+L("Achievements: ^NNone!")+"\n\r");
		else
		{
			mob.tell("^H"+prefix+L("Achievements:")+"\n\r");
			mob.tell(CMLib.lister().makeColumns(mob, AchievedList, null, 2).toString());
		}
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}
}
