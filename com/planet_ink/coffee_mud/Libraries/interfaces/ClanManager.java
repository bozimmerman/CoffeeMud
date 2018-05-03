package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.MemberRecord;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.Trophy;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
/*
   Copyright 2005-2018 Bo Zimmerman

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
 * This library manages the list of Clans in the game, and helps
 * administer their most basic functions.
 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan
 */
public interface ClanManager extends CMLibrary
{
	/**
	 * Returns a list of all available clans names in the game,
	 * as Clan objects.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan
	 * @return a list of all available clans names in the game,
	 */
	public Enumeration<String> clansNames();
	/**
	 * Returns the number of clans in the game.
	 * @return the number of clans in the game.
	 */
	public int numClans();

	/**
	 * This method is used to determine the basic relationship between two clans.  The
	 * two clans are evaluated, based on their declared relationship to each other, and
	 * the relations they inherit from allys.  It is then compared with the passed in
	 * relationship constant.  If they match, true is returned, and false otherwise.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#REL_DESCS
	 * @param clanName1 the first clan to evaluate
	 * @param clanName2 the second clan to evaluate
	 * @param relation the clan relation to compare to
	 * @return true if the common relationship matches the given relation, and false otherwise
	 */
	public boolean isCommonClanRelations(String clanName1, String clanName2, int relation);

	/**
	 * This method is used to determine the basic relationship between two clan members.  The
	 * two mobs first rivalrous clans are evaluated, based on their declared relationship to
	 * each other, and the relations they inherit from allys.  It is then compared with the
	 * passed the War status.  If they match, true is returned, and false otherwise.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#REL_DESCS
	 * @param M1 the first mobs first rivalrous clan to evaluate
	 * @param M2 the second mobs first rivalrous clan to evaluate
	 * @return true if the common relationship matches war, and false otherwise
	 */
	public boolean isAtClanWar(MOB M1, MOB M2);

	/**
	 * Returns whether the two mobs share ANY common clans, even
	 * non-rivalrous ones.
	 * @param M1 first mob
	 * @param M2 second mob
	 * @return true if they share a clan, false otherwise
	 */
	public boolean isAnyCommonClan(MOB M1, MOB M2);

	/**
	 * This method is used to determine the basic relationship between two mobs clans. The
	 * two sets of clans are evaluated, based on their declared relationship to each other, and
	 * the relations they inherit from allys.  This relationship is then checked against the given
	 * relation.  If any pairing matches the relation, true is returned.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#REL_DESCS
	 * @param M1 the first mob whose clans to evaluate
	 * @param M2 the second mob whose clans to evaluate
	 * @param relation the relation to look for
	 * @return true if any clans relate in the given way, false otherwise
	 */
	public boolean findAnyClanRelations(MOB M1, MOB M2, int relation);

	/**
	 * This method is used to determine the basic relationship between two clans.  The
	 * two clans are evaluated, based on their declared relationship to each other, and
	 * the relations they inherit from allys.  This relationship is then returned as
	 * a relation constant number.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#REL_DESCS
	 * @param clanID1 the first clan to evaluate
	 * @param clanID2 the second clan to evaluate
	 * @return the relation code integer
	 */
	public int getClanRelations(String clanID1, String clanID2);

	/**
	 * Get last time governments were loaded/updated
	 * @return time in ms
	 */
	public long getLastGovernmentLoad();

	/**
	 * Returns the clan which may or may not be assigned to the absolute file path specified.
	 * @param sitePath the absolute path of the web dir assigned to this clan
	 * @return a clan at that path, or NULL for none
	 */
	public Clan getWebPathClan(String sitePath);

	/**
	 * Returns whether the given clan set contains a clan that
	 * allows the given mob to do the given function in the clan.
	 * @param mob the mob to check the clans of
	 * @param func the function to check for
	 * @return true if allowed, false otherwise
	 */
	public boolean checkClanPrivilege(MOB mob, Clan.Function func);

	/**
	 * Returns whether the given mob belongs to the given clan, and if
	 * so, whether they can do the given function in the clan.
	 * @param mob the mob to look for privileges for
	 * @param clanID the clanID to check for to check for
	 * @param func the function to check for
	 * @return true if allowed, false otherwise
	 */
	public boolean checkClanPrivilege(MOB mob, String clanID, Clan.Function func);

	/**
	 * If the given mob belongs to a clan, and if they can do the
	 * given function in a clan, this will return that clan object
	 * and the integer.
	 * @param mob the mob to check the clans of
	 * @param func the function to check for
	 * @return the clan and role integer
	 */
	public Pair<Clan,Integer> findPrivilegedClan(MOB mob, Clan.Function func);

	/**
	 * If the given mob belongs to a clan, and if they can do the
	 * given function in a clan, this will return those clan objects
	 * and their role integer.
	 * @param mob the mob to check the clans of
	 * @param func the function to check for
	 * @return the clan and role integers
	 */
	public List<Pair<Clan,Integer>> findPrivilegedClans(MOB mob, Clan.Function func);

	/**
	 * Returns a list of clans that the source mob belongs to which the filter
	 * mob does NOT also belong.  The clans will be rivalrous only.
	 * @param clanSourceMob the mob to source the clan list from
	 * @param filterMob the mob to use to filter out source clans
	 * @return the clan and role integers
	 */
	public List<Pair<Clan,Integer>> findRivalrousClans(MOB clanSourceMob, MOB filterMob);

	/**
	 * Searches for all clans that can be rivalrous with other clans that are
	 * commonly shared between two mobs, along with their two roles
	 * @param mob1 the first mob to check the clans of
	 * @param mob2 the second mob to check the clans of
	 * @return potentially rivalrous clans and the mobs roles in them
	 */
	public List<Triad<Clan,Integer,Integer>> findCommonRivalrousClans(MOB mob1, MOB mob2);

	/**
	 * Searches for a clan in the list that can be rivalrous with other clans.
	 * First one found is returned.
	 * @param mob the mob to check the clans of
	 * @return First clan of the mobs that is rivalrous
	 */
	public Clan findRivalrousClan(MOB mob);

	/**
	 * Searches for a clan in the list that can be conquerable with other clans.
	 * First one found is returned.
	 * @param mob the mob to check the clans of
	 * @return First clan of the mobs that is conquerable of others
	 */
	public Clan findConquerableClan(MOB mob);

	/**
	 * Searches for all clans that can be rivalrous with other clans.
	 * @param mob the mob to check the clans of
	 * @return List of the mobs clans that are rivalrous
	 */
	public List<Pair<Clan,Integer>> findRivalrousClans(MOB mob);

	/**
	 * Returns the Clan object associated with the given clan name
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan
	 * @param id the clan name
	 * @return the Clan object associated with the given clan name
	 */
	public Clan getClan(String id);

	/**
	 * Returns the Clan object associated with the given clan name, or
	 * if the name is not found, the name that most closely matches it.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan
	 * @param id the clan name
	 * @return the Clan object associated with the given clan name
	 */
	public Clan findClan(String id);

	/**
	 * Returns an enumeration of all the Clans in the game
	 * @return an enumeration of all the Clans in the game
	 */
	public Enumeration<Clan> clans();

	/**
	 * Returns all clans and their last-cached accept-positions
	 * The auto-positions may be inaccurate.
	 * @return all clans and their last-cached accept-positions
	 */
	public Iterable<Pair<Clan,Integer>> clanRoles();

	/**
	 * Adds the given clan to the games list
	 * @param C the clan to add
	 */
	public void addClan(Clan C);

	/**
	 * Removes the given clan from the games list
	 * @param C the clan to remove
	 */
	public void removeClan(Clan C);

	/**
	 * Returns the clan associated with a given specific web path.
	 * It is derived from the clanwebsites entry in coffeemud.ini file.
	 * @param webPath a full vfs resource path
	 * @return the clan associated with the path, or null
	 */
	public Clan getWebPathClanMapping(String webPath);

	/**
	 * Returns the template vfs path associated with a given specific
	 * web path. It is derived from the clanwebsites entry in
	 * coffeemud.ini file.
	 * @param webPath a full vfs resource path
	 * @return the template path associated with the resource path, or null
	 */
	public String getClanWebTemplateDir(String webPath);

	/**
	 * Forces all clans to go through their maintenance process, which
	 * normally only occurs infrequently.  This does things like handle
	 * automatic promotions, manage votes, and clean out inactive clans.
	 */
	public void tickAllClans();

	/**
	 * Sends a message to the games official CLAN chat channel.  This
	 * is normally for messages that may interest all clans.
	 * @param msg the message to send
	 */
	public void clanAnnounceAll(String msg);

	/**
	 * Returns a descriptive name for the given trophy code number.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan.Trophy
	 * @param trophy the trophy code number
	 * @return the descriptive name
	 */
	public String translatePrize(Trophy trophy);

	/**
	 * Returns whether this mud has activated its trophy system for clans.
	 * @return whether this mud has activated its trophy system for clans.
	 */
	public boolean trophySystemActive();
	/**
	 * Returns whether the given MOB is a member of any of the families
	 * represented by the given list of clan members.
	 * @param M the mob to evaluate
	 * @param members the members of a clan
	 * @return true if the mob is a family member, and false otherwise
	 */
	public boolean isFamilyOfMembership(MOB M, List<MemberRecord> members);

	/**
	 * Returns the list of clans this mob belongs to in the given category.
	 * @param M the mob to evaluate
	 * @param category the clan goverment category
	 * @return the list of clans this mob belongs to
	 */
	public List<Pair<Clan,Integer>> getClansByCategory(MOB M, String category);

	/**
	 * Returns help on the government type named, if it is available
	 * @param mob the viewer of the government type
	 * @param named the possible name of the government
	 * @param exact true to only match exact, or false otherwise
	 * @return null, or the help for the government named
	 */
	public String getGovernmentHelp(MOB mob, String named, boolean exact);

	/**
	 * Returns a government definition object of the given internal
	 * stock clangovernments.xml id.  See /resources/clangovernments.xml
	 * Also:
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ClanGovernment
	 * @param typeid the internal typeid
	 * @return the clan government object
	 */
	public ClanGovernment getStockGovernment(int typeid);

	/**
	 * Returns the default government definition object.
	 * See /resources/clangovernments.xml
	 * Also:
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ClanGovernment
	 * @return the clan government object
	 */
	public ClanGovernment getDefaultGovernment();

	/**
	 * Creates, but does not add, a sample government object
	 * @return a sample government object
	 */
	public ClanGovernment createSampleGovernment();

	/**
	 * Returns all government definition objects from internal
	 * stock clangovernments.xml file.  See /resources/clangovernments.xml
	 * Also:
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ClanGovernment
	 * @return the clan government object
	 */
	public ClanGovernment[] getStockGovernments();

	/**
	 * Converts a given clan government object into xml.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ClanGovernment
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.ClanManager#makeGovernmentXML(com.planet_ink.coffee_mud.Common.interfaces.ClanGovernment[])
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.ClanManager#parseGovernmentXML(StringBuffer)
	 * @param gvt the clan government object
	 * @return the xml
	 */
	public String makeGovernmentXML(ClanGovernment gvt);

	/**
	 * Converts a given clan government objects into xml.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ClanGovernment
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.ClanManager#makeGovernmentXML(com.planet_ink.coffee_mud.Common.interfaces.ClanGovernment)
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.ClanManager#parseGovernmentXML(StringBuffer)
	 * @param gvts the clan government objects
	 * @return the xml
	 */
	public String makeGovernmentXML(ClanGovernment gvts[]);

	/**
	 * Converts xml into clan government objects.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.ClanGovernment
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.ClanManager#makeGovernmentXML(com.planet_ink.coffee_mud.Common.interfaces.ClanGovernment)
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.ClanManager#makeGovernmentXML(com.planet_ink.coffee_mud.Common.interfaces.ClanGovernment[])
	 * @param xml the xml
	 * @return the clan government objects
	 */
	public ClanGovernment[] parseGovernmentXML(StringBuffer xml);

	/**
	 * Forces the stock governments to be re-saved to clangovernments.xml.
	 */
	public void reSaveGovernmentsXML();

	/**
	 * Creates the new stock government
	 * @param name governmentname
	 * @return new stock government
	 */
	public ClanGovernment createGovernment(String name);

	/**
	 * Deletes the stock government.. confusing all the clans that
	 * currently use it.
	 * @param government the government to delete
	 * @return true if it was there to remove, false otherwise
	 */
	public boolean removeGovernment(ClanGovernment government);

	/**
	 * Makes an announcement to the clan announcement channel from
	 * the given mob.  These are channels marked in the coffeemud.ini file
	 * as receiving clan info messages.  These messages are only seen
	 * by the authors clan members.
	 * @param mob the mob who is announcing the message
	 * @param msg string message to send to the clan info channels
	 */
	public void clanAnnounce(MOB mob, String msg);

	/**
	 * Examines the given command string, which is based on the given clan function code.
	 * If the given Clan requires a vote to accomplish it, the vote will be created. If
	 * the given clan forbids the given mob from performing the given function, or from
	 * even starting a vote on it, the method will return false. If the mob is allowed
	 * to perform
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan.Function#ACCEPT
	 * @param mob the player who wants to perform the function
	 * @param C the clan that the player belongs to
	 * @param commands the command list describing the function that wants to be executed
	 * @param function the function code described by the commands list
	 * @param voteIfNecessary true to start a vote if one is needed, false to just return true.
	 * @return true to execute the given command, and false not to.
	 */
	public boolean goForward(MOB mob, Clan C, List<String> commands, Clan.Function function, boolean voteIfNecessary);

	/**
	 * Force the clans maintenance thread
	 */
	public void forceTick();
}
