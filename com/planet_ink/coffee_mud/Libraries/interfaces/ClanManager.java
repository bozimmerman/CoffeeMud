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
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;
/* 
   Copyright 2000-2012 Bo Zimmerman

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
	 * This method is used to determine the basic relationship between two clans.  The
	 * two clans are evaluated, based on their declared relationship to each other, and
	 * the relations they inherit from allys.  This relationship is then returned as
	 * a relation constant number.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#REL_DESCS
	 * @param clanName1 the first clan to evaluate
	 * @param clanName2 the second clan to evaluate
	 * @return the relation code integer
	 */
	public int getClanRelations(String clanName1, String clanName2);
	
	/**
	 * Get last time governments were loaded/updated
	 * @return time in ms
	 */
	public long getLastGovernmentLoad();

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
	 * @param id
	 * @return the Clan object associated with the given clan name
	 */
	public Clan findClan(String id);
	
	/**
	 * Returns an enumeration of all the Clans in the game 
	 * @return an enumeration of all the Clans in the game
	 */
	public Enumeration<Clan> clans();
	
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
	 * If the clan exists, it will check to see if the given role
	 * is allowed (or at least not disallowed) from the given
	 * clan function
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan
	 * @param clanID the clan name
	 * @param roleID the clan roleID
	 * @param function the clan function
	 * @return true if they aren't disallowed, false otherwise
	 */
	public boolean authCheck(String clanID, int roleID, Clan.Function function);
	
	/**
	 * Returns a descriptive name for the given trophy code number.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#TROPHY_DESCS
	 * @param trophy the trophy code number
	 * @return the descriptive name
	 */
	public String translatePrize(int trophy);
	
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
	public boolean goForward(MOB mob, Clan C, Vector commands, Clan.Function function, boolean voteIfNecessary);
}
