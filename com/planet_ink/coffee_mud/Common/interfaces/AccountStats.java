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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;

/*
   Copyright 2010-2015 Bo Zimmerman

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
public interface AccountStats extends CMCommon
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
	 *
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
	 * Returns a modifiable HashSet that contains the set of player
	 * Names that constitutes this players friends.
	 *
	 * @return a set of player friend names
	 */
	public Set<String> getFriends();

	/**
	 * Returns a modifiable HashSet that contains the set of player
	 * Names that constitutes this players ignored player list.
	 *
	 * @return a set of player ignored player list Names
	 */
	public Set<String> getIgnored();

	/**
	 * Add to one of the pride stats for this player or account
	 * @see PrideStat
	 * @param stat which pride stat to add to
	 * @param amt the amount to add
	 */
	public void bumpPrideStat(PrideStat stat, int amt);

	/**
	 * Get one of the pride stats for this player or account
	 * @see PrideStat
	 * @param period the time period to get the number for
	 * @param stat which pride stat to get
	 */
	public int getPrideStat(TimeClock.TimePeriod period, PrideStat stat);

	/**
	 * Returns an XML representation of all the data in this object, for
	 * persistant storage.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AccountStats#setXML(String)
	 *
	 * @return an XML representation of all the data in this object
	 */
	public String getXML();

	/**
	 * Restores the data in this object from an XML document.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.AccountStats#getXML()
	 *
	 * @param str an XML representation of all the data in this object
	 */
	public void setXML(String str);

	/**
	 * The recorded player and account statistics.
	 * @author Bo Zimmerman
	 *
	 */
	public enum PrideStat
	{
		PVPKILLS, 
		AREAS_EXPLORED, 
		ROOMS_EXPLORED, 
		EXPERIENCE_GAINED, 
		MINUTES_ON, 
		QUESTS_COMPLETED, 
		QUESTPOINTS_EARNED
	}

}
