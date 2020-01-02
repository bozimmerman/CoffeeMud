package com.planet_ink.coffee_mud.core.interfaces;

import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Achievement;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Tracker;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;

/*
   Copyright 2019-2020 Bo Zimmerman

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
/**
*
* Something that can tracked for and attain achievements
* @see com.planet_ink.coffee_mud.Common.interfaces.Tattoo
* @author Bo Zimmerman
*
*/
public interface Achievable
{
	/**
	 * Returns the tracker for the given achievement, for the given mob, for the given
	 * tracked thing, or creates it if it does not exist.
	 * @see Achievable#rebuildAchievementTracker(Tattooable, MOB, String)
	 * #see Achievable#killAchievementTracker(Achievement, MOB)
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Tracker
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Achievement
	 * @param A the achievement to get the tracker for
	 * @param tracked the mob or clan being tracked
	 * @param mob the mob to create a tracker for
	 * @return the Tracker object that handles this achievement/mob
	 */
	public Tracker getAchievementTracker(Achievement A, Tattooable tracked, MOB mob);

	/**
	 * Deletes the tracker for the given achievement, for the given mob, for the given
	 * tracked thing.
	 * @see Achievable#rebuildAchievementTracker(Tattooable, MOB, String)
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Tracker
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Achievement
	 * @param A the achievement to kill the tracker for
	 * @param tracked the mob or clan being tracked
	 * @param mob the mob to delete a tracker for
	 */
	public void killAchievementTracker(Achievement A, Tattooable tracked, MOB mob);

	/**
	 * If an Achievement is modified or removed, this method will update the
	 * internal player tracker for that achievement.  It does not delete old
	 * achievements per se, just their trackers!
	 * @param tracked the mob or clan being tracked
	 * @param mob the mob to modify the tracker for.
	 * @param achievementTattoo the tattoo/id of the achievement
	 * @see Achievable#getAchievementTracker(com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Achievement, Tattooable, MOB)
	 */
	public void rebuildAchievementTracker(Tattooable tracked, final MOB mob, String achievementTattoo);

}
