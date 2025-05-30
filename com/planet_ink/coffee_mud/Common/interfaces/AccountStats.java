package com.planet_ink.coffee_mud.Common.interfaces;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Achievement;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Tracker;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2010-2025 Bo Zimmerman

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
 * An interface for a base player account. Shared by PlayerAccount
 * and PlayerStats (since the account system is optional)
 */
public interface AccountStats extends CMCommon, Achievable, PrideStats
{
	/**
	 * Returns the players email address, if available.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AccountStats#setEmail(String)
	 *
	 * @return the players email address, if available.
	 */
	public String getEmail();

	/**
	 * Sets the players email address, if available.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AccountStats#getEmail()
	 *
	 * @param newAdd the players email address, if available.
	 */
	public void setEmail(String newAdd);

	/**
	 * The time, in milis since 1970, that the player last logged off.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AccountStats#setLastDateTime(long)
	 *
	 * @return time, in milis since 1970, that the player last logged off.
	 */
	public long getLastDateTime();

	/**
	 * Sets the time, in milis since 1970, that the player last logged off.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AccountStats#getLastDateTime()
	 *
	 * @param C the time, in milis since 1970, that the player last logged off.
	 */
	public void setLastDateTime(long C);

	/**
	 * The time, in milis since 1970, that the player was last saved.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AccountStats#setLastUpdated(long)
	 *
	 * @return the time, in milis since 1970, that the player was last saved.
	 */
	public long getLastUpdated();

	/**
	 * Sets the time, in milis since 1970, that the player was last saved.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AccountStats#getLastUpdated()
	 *
	 * @param time the time, in milis since 1970, that the player was last saved.
	 */
	public void setLastUpdated(long time);

	/**
	 * Returns the players password, perhaps encoded, perhaps plain text.
	 * Use matchesPassword(String) to do actual checks.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AccountStats#setPassword(String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AccountStats#matchesPassword(String)
	 *
	 * @return the players password.
	 */
	public String getPasswordStr();

	/**
	 * Returns the players password, perhaps encoded, perhaps plain text.
	 * Use matchesPassword(String) to do actual checks.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AccountStats#setPassword(String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AccountStats#getPasswordStr()
	 * @param str the password to compare reality to
	 * @return the players password.
	 */
	public boolean matchesPassword(String str);

	/**
	 * Sets the players password.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AccountStats#getPasswordStr()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AccountStats#matchesPassword(String)
	 *
	 * @param newPassword the players password.
	 */
	public void setPassword(String newPassword);

	/**
	 * The last IP address this player logged in from.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AccountStats#setLastIP(String)
	 *
	 * @return the last IP address this player logged in from.
	 */
	public String getLastIP();

	/**
	 * Sets the last IP address this player logged in from.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AccountStats#getLastIP()
	 *
	 * @param ip the last IP address this player logged in from.
	 */
	public void setLastIP(String ip);

	/**
	 * If the ACCOUNT system is used, this returns the time, in milis since
	 * 1970, that this account will expire (meaning the player will no longer
	 * be able to log in)
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AccountStats#setAccountExpiration(long)
	 *
	 * @return the time, in milis, that this player expires.
	 */
	public long getAccountExpiration();

	/**
	 * If the ACCOUNT system is used, this sets the time, in milis since
	 * 1970, that this account will expire (meaning the player will no longer
	 * be able to log in)
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AccountStats#getAccountExpiration()
	 *
	 * @param newVal the time, in milis, that this player expires.
	 */
	public void setAccountExpiration(long newVal);

	/**
	 * Returns the administrative notes entered about this player.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AccountStats#setNotes(String)
	 *
	 * @return the administrative notes entered about this player.
	 */
	public String getNotes();

	/**
	 * Sets the administrative notes entered about this player.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AccountStats#getNotes()
	 *
	 * @param newnotes the administrative notes entered about this player.
	 */
	public void setNotes(String newnotes);

	/**
	 * Returns the number of bonus char stat points to assign to stats
	 * during character creation.  This is independent of the account
	 * bonus.
	 * @see AccountStats#setBonusCharStatPoints(int)
	 *
	 * @return the number of bonus charstat points
	 */
	public int getBonusCharStatPoints();

	/**
	 * Sets the number of bonus char stat points to assign to stats
	 * during character creation.  This is independent of the account
	 * bonus.
	 * @see AccountStats#getBonusCharStatPoints()
	 *
	 * @param bonus the number of bonus charstat points
	 */
	public void setBonusCharStatPoints(int bonus);

	/**
	 * Returns the number of bonus common skills available to
	 * this character.  This is independent of the account
	 * bonus.
	 *
	 * @see AccountStats#setBonusCommonSkillLimits(int)
	 *
	 * @return the number of bonus Common points
	 */
	public int getBonusCommonSkillLimits();

	/**
	 * Sets the number of bonus common skills available to
	 * this character.  This is independent of the account
	 * bonus.
	 *
	 * @see AccountStats#getBonusCommonSkillLimits()
	 *
	 * @param bonus the number of bonus Common points
	 */
	public void setBonusCommonSkillLimits(int bonus);

	/**
	 * Returns the number of bonus Crafting skills available to
	 * this character.  This is independent of the account
	 * bonus.
	 *
	 * @see AccountStats#setBonusCraftingSkillLimits(int)
	 *
	 * @return the number of bonus Crafting points
	 */
	public int getBonusCraftingSkillLimits();

	/**
	 * Sets the number of bonus Crafting skills available to
	 * this character.  This is independent of the account
	 * bonus.
	 *
	 * @see AccountStats#getBonusCraftingSkillLimits()
	 *
	 * @param bonus the number of bonus Crafting points
	 */
	public void setBonusCraftingSkillLimits(int bonus);

	/**
	 * Returns the number of bonus Gathering skills available to
	 * this character.  This is independent of the account
	 * bonus.
	 *
	 * @see AccountStats#setBonusNonCraftingSkillLimits(int)
	 *
	 * @return the number of bonus Gathering points
	 */
	public int getBonusNonCraftingSkillLimits();

	/**
	 * Sets the number of bonus Gathering skills available to
	 * this character.  This is independent of the account
	 * bonus.
	 *
	 * @see AccountStats#getBonusNonCraftingSkillLimits()
	 *
	 * @param bonus the number of bonus Gathering points
	 */
	public void setBonusNonCraftingSkillLimits(int bonus);

	/**
	 * Returns the number of bonus Language skills available to
	 * this character.  This is independent of the account
	 * bonus.
	 *
	 * @see AccountStats#setBonusLanguageLimits(int)
	 *
	 * @return the number of bonus Language points
	 */
	public int getBonusLanguageLimits();

	/**
	 * Sets the number of bonus Language skills available to
	 * this character.  This is independent of the account
	 * bonus.
	 *
	 * @see AccountStats#getBonusLanguageLimits()
	 *
	 * @param bonus the number of bonus Language points
	 */
	public void setBonusLanguageLimits(int bonus);

	/**
	 * Returns a modifiable Set that contains the set of journal
	 * subscriptions, normalized to uppercase, that the player
	 * has subscribed to and wishes to get notifications about.
	 *
	 * @return a set of journal names
	 */
	public Set<String> getSubscriptions();

	/**
	 * Returns a modifiable Set that contains the set of player
	 * Names that constitutes this players friends.
	 *
	 * @return a set of player friend names
	 */
	public Set<String> getFriends();

	/**
	 * Returns a modifiable Set that contains the set of player
	 * Names that constitutes this players ignored player list.
	 * @see AccountStats#isIgnored(String)
	 * @see AccountStats#isIgnored(String, MOB)
	 *
	 * @return a set of player ignored player list Names
	 */
	public Set<String> getIgnored();


	/**
	 * Returns whether the given player or account name is being ignored.
	 * @see AccountStats#getIgnored()
	 * @see AccountStats#isIgnored(String, MOB)
	 *
	 * @param name the name to check
	 * @return true if the given name is ignored
	 */
	public boolean isIgnored(final String name);

	/**
	 * Returns whether the given player or account name is being ignored
	 * generally, or even just in the given category
	 * @see AccountStats#getIgnored()
	 * @see AccountStats#isIgnored(String)
	 *
	 * @param category the name of the category
	 * @param name the name to check
	 * @return true if the given name is ignored
	 */
	public boolean isIgnored(final String category, final MOB mob);

	/**
	 * A simple enum for picking between a player and an account
	 * @author Bo Zimmerman
	 *
	 */
	public enum Agent
	{
		PLAYER,
		ACCOUNT,
		CLAN
	}

}
