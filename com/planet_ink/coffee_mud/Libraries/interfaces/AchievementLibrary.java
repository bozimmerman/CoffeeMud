package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
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
import com.planet_ink.coffee_web.interfaces.*;

import java.util.*;

/*
   Copyright 2008-2015 Bo Zimmerman

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
public interface AchievementLibrary extends CMLibrary
{
	public enum Event
	{
		KILLS,
		STATVALUE,
		FACTION,
		EXPLORE,
		CRAFTING,
		MENDER,
		SKILLUSE,
		QUESTOR,
		ACHIEVER
	}
	
	public interface Achievement
	{
		public Event getEvent();
		
		public String getTattoo();
		
		public Tracker getTracker(int oldCount);
		
		public String parseParms(String parms);
		
		public String getDisplayStr();
		
		public String getTitleAward();
		
		public String[] getRewards();
		
		public int getTargetCount();
		
		public boolean isTargetFloor();
		
		public boolean isSavableTracker();
	}
	
	public interface Tracker
	{
		public Achievement getAchievement();
		
		public boolean isAchieved(MOB mob);
		
		public boolean testBump(MOB mob, Object... parms);
		
		/**
		 * Returns the count/score to show for the given mob.  If the
		 * achievement of this tracker is Savable, then the mob may be
		 * null, since the count would then be internally stored.
		 * @param mob the mob to get a count for -- required ONLY for unsavable
		 * @return the score for this achievement and this mob
		 */
		public int getCount(MOB mob);
	}
	
	public String evaluateAchievement(String row, boolean addIfPossible);
	public void reloadAchievements();
	public boolean evaluateAchievements(MOB mob);
	public Enumeration<Achievement> achievements();
	public void possiblyBumpAchievement(final MOB mob, final Event E, Object... parms);
}
