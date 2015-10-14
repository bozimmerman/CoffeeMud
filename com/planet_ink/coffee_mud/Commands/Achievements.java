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
		final PlayerStats pStats = mob.playerStats();
		if (pStats == null)
		{
			mob.tell(L("You don't have any achievements."));
			return false;
		}
		
		if(CMLib.achievements().evaluateAchievements(mob))
		{
			CMLib.s_sleep(4000); 
			// yes, I know, but if I call tick down, or some other
			// async method, I'll get a freaking prompt.
		}
		
		String prefix = "";
		List<String> AchievedList = new Vector<String>();
		boolean announce=rest.toUpperCase().equals("ANNOUNCE");
		if(announce)
			rest="";
			
		if(rest.toUpperCase().equals("ALL"))
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
		if(rest.toUpperCase().equals("NOW"))
		{
			prefix=L("Progress in ");
			String done=L("DONE!");
			int padding=done.length()+1;
			for(Enumeration<Achievement> a=CMLib.achievements().achievements();a.hasMoreElements();)
			{
				final Achievement A=a.nextElement();
				if(mob.findTattoo(A.getTattoo()) == null)
				{
					AchievementLibrary.Tracker T=pStats.getAchievementTracker(A, mob);
					final int score = (T==null) ? 0 : T.getCount(mob);
					if(score != 0)
					{
						final int targetScore = A.getTargetCount();
						final int len = (""+score+"/"+targetScore).length(); 
						if(len >= padding)
							padding = len+1;
					}
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
					if(score != 0)
					{
						int targetScore = A.getTargetCount();
						AchievedList.add(CMStrings.padRight("^w"+score+"/"+targetScore, padding)+"^?: "+A.getDisplayStr());
					}
				}
			}
			
		}
		else
		if(rest.length()==0)
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
		else
		{
			MOB M=CMLib.players().getLoadPlayer(rest);
			if(M==null)
				mob.tell(L("There is no such player as '@x1'.",rest));
			else
			if(M.playerStats()!=null)
			{
				prefix=M.Name()+L("'s ");
				for(Enumeration<Achievement> a=CMLib.achievements().achievements();a.hasMoreElements();)
				{
					final Achievement A=a.nextElement();
					if(M.findTattoo(A.getTattoo()) != null)
					{
						AchievedList.add(A.getDisplayStr());
					}
				}
			}

		}
		String finalResponse;
		if(AchievedList.size()==0)
			finalResponse = "^H"+prefix+L("Achievements: ^NNone!")+"^w\n\r";
		else
		{
			finalResponse = "^H"+prefix+L("Achievements:")+"^w\n\r";
			finalResponse += CMLib.lister().makeColumns(mob, AchievedList, null, 2).toString();
		}
		if(announce)
			CMLib.commands().postSay(mob, finalResponse);
		else
			mob.tell(finalResponse);
		
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}
}
