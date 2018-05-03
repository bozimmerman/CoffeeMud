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
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.ForumJournal;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2018 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

   * Clan is the basis for clan objects.
   * A Clan is basically a collection of {@link MOB} objects,
   * including, but not limited to:
   * <ul>
   * <li> Ranks
   * <li> Jobs/Positions
   * <li> Clan Homes
   * </ul>
   * In this interface, we provide the common functions, including:
   * <li> Add/remove member
   * <li> Get/set Clan recall and donation room
   * <li> Get average alignment
   * </ul>
   * @author=Bo Zimmerman, Jeremy Vyska
  */
public interface Clan extends Cloneable, Tickable, CMCommon, Modifiable
{
	/**
	 * Returns whether the given roleID is allowed to perform the
	 * given function.  The return value can designate that they
	 * may do it now, that they may not do it, or that it requires
	 * a vote.  A return value 1 means they can do it now, 0 means
	 * it requires a vote, and -1 means they can never do it. The
	 * functions are defined in the Clan interface as FUNC_*
	 * constants.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan.Authority
	 * @param roleID the roleID whose priviledges to check.
	 * @param function the ClanFunction function constant to use
	 * @return the ClanPositionPower enum
	 */
	public Authority getAuthority(int roleID, Function function);

	/**
	 * Sends a message to all members of all clans.
	 * @param msg the message to send
	 */
	public void clanAnnounce(String msg);

	/**
	 * If the clan type allows voting, this returns a
	 * series of ClanVote objects
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan.ClanVote
	 * @return the set of clan votes
	 */
	public Enumeration<ClanVote> votes();

	/**
	 * Orders the system to save any changes to existing
	 * ongoing votes for this
	 */
	public void updateVotes();

	/**
	 * Adds a new ClanVote object to the list of ongoing
	 * votes for this
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan.ClanVote
	 * @param CV the clanvote object to add
	 */
	public void addVote(ClanVote CV);

	/**
	 * Removes a new ClanVote object from the list of ongoing
	 * votes for this
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan.ClanVote
	 * @param CV the clanvote object to remove
	 */
	public void delVote(ClanVote CV);

	/**
	 * Returns the number of members allowed to vote on the
	 * given function.  The function is one of the FUNC_*
	 * constants.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan
	 * @param function the FUNC_* constant
	 * @return the number of members of this clan who can vote on it
	 */
	public int getNumVoters(Function function);

	/**
	 * Returns the number of members of this
	 * @return the membership count.
	 */
	public int getSize();

	/**
	 * Returns the name of the clan, which is almost always the
	 * same as the ID, which is why clans can't change their names.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#setName(String)
	 * @return the name of the
	 */
	public String getName();

	/**
	 * Returns the unique identifying ID of the clan for reference
	 * elsewhere.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#getName()
	 * @return the unique identifier of the
	 */
	public String clanID();

	/**
	 * Sets a new name for this
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#getName()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#clanID()
	 * @param newName the new name of this clan
	 */
	public void setName(String newName);

	/**
	 * Returns the category of the clan, which is almost always the
	 * same as the government category.  This is significant since a player
	 * can only belong to N clans of a given category.  See also MAXCLANS
	 * in coffeemud.ini.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#setCategory(String)
	 * @return the name of the
	 */
	public String getCategory();

	/**
	 * Sets a new category for this, which is almost always the
	 * same as the government category.  This is significant since a player
	 * can only belong to N clans of a given category.  See also MAXCLANS
	 * in coffeemud.ini.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#getCategory()
	 * @param newCategory the new cata of this clan
	 */
	public void setCategory(String newCategory);

	/**
	 * Returns Clan, Republic, or another one of the
	 * clan government types.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan
	 * @return the clans government types
	 */
	public String getGovernmentName();

	/**
	 * This method is called when a given mobs clan status
	 * changes, for instance if they are added to the clan,
	 * removed from it, or change their rank.
	 * @param mob the mob whose status needs updating
	 * @return whether the update succeeded
	 */
	public boolean updateClanPrivileges(MOB mob);

	/**
	 * Return a vector of skills, spells, and other abilities granted to the given
	 * mob of the given mobs level.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability
	 * @param mob the mob to grant the abilities to
	 * @return a vector of the Ability objects
	 */
	public SearchIDList<Ability> clanAbilities(MOB mob);

	/**
	 * Return a vector of skills, spells, and other effects granted to the given
	 * mob of the given mobs level.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability
	 * @param mob the mob to grant the abilities to
	 * @return a vector of the Ability objects
	 */
	public ChameleonList<Ability> clanEffects(MOB mob);

	/**
	 * Return size of a vector of skills, spells, and other effects granted to the given
	 * mob of the given mobs level. Much more efficient than getting the whole list
	 * and checking its size.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability
	 * @param mob the mob to grant the abilities to
	 * @return a size of a vector of the Ability objects
	 */
	public int numClanEffects(MOB mob);

	/**
	 * Retrieves this Clan's basic story.
	 * This is to make the Clan's more RP based and so we can
	 * provide up-to-date information on Clans on the web server.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#setPremise(String)
	 * @return the story of this clan
	 */
	public String getPremise();

	/**
	 * Returns whether the high role id outranks the low roldid
	 * @param highRoleID first
	 * @param lowRoleID second
	 * @return true if highRoleiD outranks lowRoleID
	 */
	public boolean doesOutRank(int highRoleID, int lowRoleID);

	/**
	 * Sets this Clan's basic story.
	 * @param newPremise the new premise text
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#getPremise()
	 */
	public void setPremise(String newPremise);

	/**
	 * Sets this Clan's enforced character class.
	 * @param newClass the new enforced character class
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#getClanClass()
	 */
	public void setClanClass(String newClass);

	/**
	 * Retrieves this Clan's enforced character class.
	 * All players who join this clan become this class.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#setClanClass(String)
	 * @return the class of this clan
	 */
	public String getClanClass();

	/**
	 * Creates the string for the 'clandetail' command, based on the
	 * security of the given mob who will view the details.
	 * @param mob the one who will view the details
	 * @return the details view of this clan for the given mob
	 */
	public String getDetail(MOB mob);

	/**
	 * Returns a mask used to identify whether a player is allowed
	 * to join a particular
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#setAcceptanceSettings(String)
	 * @return the mask used to identify prospects
	 */
	public String getAcceptanceSettings();

	/**
	 * Sets a mask used to identify whether a player is allowed
	 * to join a particular
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#getAcceptanceSettings()
	 * @param newSettings the mask used to identify prospects
	 */
	public void setAcceptanceSettings(String newSettings);

	/**
	 * Returns an XML string used to identify the political relations
	 * between this clan and others.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#setPolitics(String)
	 * @return xml document describing the politics of this clan
	 */
	public String getPolitics();

	/**
	 * Sets an XML string used to identify the political relations
	 * between this clan and others.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#getPolitics()
	 * @param politics xml document describing the politics of this clan
	 */
	public void setPolitics(String politics);

	/**
	 * Returns whether membership is automatically approved to this
	 * And if so, what the default position for members are.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#setAutoPosition(int)
	 * @return xml document describing the politics of this clan
	 */
	public int getAutoPosition();

	/**
	 * Sets whether membership is automatically approved to this
	 * And if so, what the default position for members are.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#getAutoPosition()
	 * @param pos the positition to give to applicants
	 */
	public void setAutoPosition(int pos);

	/**
	 * Returns one of the CLANSTAT_* constants describing the pending
	 * status of this clan for acceptable, or whether its one its way out.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#setStatus(int)
	 * @return a CLANSTAT_* constant
	 */
	public int getStatus();

	/**
	 * Sets one of the CLANSTAT_* constants describing the pending
	 * status of this clan for acceptable, or whether its one its way out.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#getStatus()
	 * @param newStatus a CLANSTAT_* constant
	 */
	public void setStatus(int newStatus);

	/**
	 * Returns true if this clan is rivalrous with other rivalrous clans,
	 * meaning that pvp is enabled between them, and war can be declared
	 * between them.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#setRivalrous(boolean)
	 * @return true or false
	 */
	public boolean isRivalrous();

	/**
	 * Set to true if this clan is rivalrous with other rivalrous clans,
	 * meaning that pvp is enabled between them, and war can be declared
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#isRivalrous()
	 * @param isRivalrous true or false
	 */
	public void setRivalrous(boolean isRivalrous);

	/**
	 * Returns the current clan level
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#setClanLevel(int)
	 * @return the current clan level
	 */
	public int getClanLevel();

	/**
	 * Sets the new clan level
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#getClanLevel()
	 * @param newClanLevel the new clan level
	 */
	public void setClanLevel(int newClanLevel);

	/**
	 * Returns the roomID of this clans recall room (their clan home)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#setRecall(String)
	 * @return the roomid of this clans clan home
	 */
	public String getRecall();
	/**
	 * Sets the roomID of this clans recall room (their clan home)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#getRecall()
	 * @param newRecall the roomid of this clans clan home
	 */
	public void setRecall(String newRecall);

	/**
	 * Returns the roomID of this clans morgue room
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#setMorgue(String)
	 * @return the roomID of this clans morgue room
	 */
	public String getMorgue();

	/**
	 * Sets the roomID of this clans morgue room
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#getMorgue()
	 * @param newRecall the roomID of this clans morgue room
	 */
	public void setMorgue(String newRecall);

	/**
	 * Returns a bitmap representing the trophies won by this
	 * The bitmap is made up of TROPHY_* constants.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#setTrophies(int)
	 * @return a bitmap of the trophies
	 */
	public int getTrophies();
	/**
	 * Sets a bitmap representing the trophies won by this
	 * The bitmap is made up of TROPHY_* constants.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#getTrophies()
	 * @param trophyFlag a bitmap of the trophies
	 */
	public void setTrophies(int trophyFlag);

	/**
	 * Returns the roomID of this clans donation room
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#setDonation(String)
	 * @return the roomID of this clans donation room
	 */
	public String getDonation();

	/**
	 * Sets the roomID of this clans donation room
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#getDonation()
	 * @param newDonation the roomID of this clans donation room
	 */
	public void setDonation(String newDonation);

	/**
	 * Gets the amount of experience earned by this
	 * @return the experience earned
	 */
	public long getExp();

	/**
	 * Sets the amount of experience earned by this
	 * @param exp the experience earned
	 */
	public void setExp(long exp);

	/**
	 * Returns the inner (government usually)
	 * requirements to even apply to this clan
	 * @return the zapper mask that applies
	 */
	public String getBasicRequirementMask();

	/**
	 * Adjusts the amount of experience earned by this
	 * @param howMuch the experience adjustment, + or -
	 */
	public void adjExp(int howMuch);

	/**
	 * Adjusts the amount of experience earned by a player based
	 * on the tax rate.  Will automatically adjust the exp of
	 * the clan and save it.
	 * @param exp the old experience
	 * @return the exp adjusted by the clan, if at all.
	 */
	public int applyExpMods(int exp);

	/**
	 * Called when a member of this clan kills a member of another
	 * Will update the clan in the database.
	 * @param killer the member of this clan that did the killing
	 * @param killed the mob or member of other clan killed
	 */
	public void recordClanKill(MOB killer, MOB killed);

	/**
	 * Returns the number of other-clan kills this clan has recorded.
	 * @param killer the member of this clan that did the killing or NULL for all
	 * @return the number of kills.
	 */
	public int getCurrentClanKills(MOB killer);

	/**
	 * Returns the total control points represented by the list of
	 * controlled areas given.
	 * @see Clan#calculateMapPoints()
	 * @see Clan#getControlledAreas()
	 * @see com.planet_ink.coffee_mud.Behaviors.Conquerable
	 * @param controlledAreas the areas controlled
	 * @return the number of control points represented
	 */
	public long calculateMapPoints(List<Area> controlledAreas);

	/**
	 * Returns the total control points earned by this clan
	 * @see Clan#calculateMapPoints(List)
	 * @see Clan#getControlledAreas()
	 * @see com.planet_ink.coffee_mud.Behaviors.Conquerable
	 * @return the number of control points represented
	 */
	public long calculateMapPoints();

	/**
	 * Returns all the areas on the map controlled by this
	 * @see com.planet_ink.coffee_mud.Behaviors.Conquerable
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area
	 * @return a vector of area objects
	 */
	public List<Area> getControlledAreas();

	/**
	 * Sets the tax rate for this
	 * @see Clan#getTaxes()
	 * @param rate the tax rate 0-100.0
	 */
	public void setTaxes(double rate);

	/**
	 * Gets the tax rate for this
	 * @see Clan#setTaxes(double)
	 * @return rate the tax rate 0-100.0
	 */
	public double getTaxes();

	/**
	 * Returns the maximum number of players who can hold the given
	 * role in this   For assignment purposes.
	 * @param roleID the role to get a max for
	 * @return the max
	 */
	public int getMostInRole(int roleID);

	/**
	 * Returns the friendly descriptive name of a given role in a clan, based
	 * on very specific criteria.
	 * @param roleID the role code of the role to describe
	 * @param titleCase whether or not to uppercase the first word
	 * @param plural whether or not to return the word as a plural
	 * @return the friendly descriptive name of a given role in a clan
	 */
	public String getRoleName(int roleID, boolean titleCase, boolean plural);

	/**
	 * For the clan government type, this function will return
	 * the clan role that most closely matches the given string "position".
	 * It will return -1 if no position is found that matches the string.
	 * The returned value will an an official role bitmask.
	 * @param position the name of the position to look for
	 * @return the role mask/code number for this government, oe -1
	 */
	public int getRoleFromName(String position);

	/**
	 * Returns whether this clan gets listed with the clanlist command.
	 * Does not affect clanlog messaging.
	 * @param mob the person viewing
	 * @return true if it is, false otherwise.
	 */
	public boolean isPubliclyListedFor(MOB mob);

	/**
	 * Returns whether this clan only accepts applicants
	 * from people who are family of current members/founder.
	 * @return true if it is, false otherwise.
	 */
	public boolean isOnlyFamilyApplicants();

	/**
	 * Returns the list of roles for people in this clan, from
	 * lowest rank to the highest.  Must correspond with roleIDs
	 * @return the role list
	 */
	public String[] getRolesList();

	/**
	 * Returns the set of members, where
	 * each row represents a MemberRecord
	 * @see Clan#getMemberList(int)
	 * @see Clan#getFullMemberList()
	 * @see MemberRecord
	 * @return the membership
	 */
	public List<MemberRecord> getMemberList();

	/**
	 * Returns the set of members, where
	 * each row represents a FullMemberRecord
	 * @see Clan#getMemberList()
	 * @see FullMemberRecord
	 * @return the membership
	 */
	public List<FullMemberRecord> getFullMemberList();

	/**
	 * Returns the set of members, where
	 * each row represents a MemberRecord.
	 * Will filter by the given POS_* constant.
	 * @see Clan
	 * @see MemberRecord
	 * @param PosFilter the position filter
	 * @return the membership
	 */
	public List<MemberRecord> getMemberList(int PosFilter);

	/**
	 * Returns the highest ranking member of this
	 * @return the mob object for the highest ranking member.
	 */
	public MOB getResponsibleMember();

	/**
	 * Returns a REL_* constant denoting the relationship
	 * between this clan and the clan of the given name.
	 * @see Clan#setClanRelations(String, int, long)
	 * @see Clan#getLastRelationChange(String)
	 * @param id the name of another
	 * @return the REL_* constant
	 */
	public int getClanRelations(String id);

	/**
	 * Returns the time/date stamp when this clan last changed
	 * its relations with the given
	 * @see Clan#setClanRelations(String, int, long)
	 * @see Clan#getClanRelations(String)
	 * @param id the other clan
	 * @return when the last change was
	 */
	public long getLastRelationChange(String id);

	/**
	 * Sets a new relationship between this clan and the clan
	 * with the given id.
	 * @param id another clan
	 * @param rel the REL_* constant for the new relationship
	 * @param time a current date/time stamp for this change
	 */
	public void setClanRelations(String id, int rel, long time);

	/**
	 * Returns the id representing the government type
	 * of this
	 * @see Clan
	 * @see Clan#setGovernmentID(int)
	 * @see Clan#getGovernment()
	 * @return the if constant
	 */
	public int getGovernmentID();

	/**
	 * Returns the object representing the government
	 * of this
	 * @see ClanGovernment
	 * @see Clan#getGovernmentID()
	 * @see Clan#setGovernmentID(int)
	 * @return the if constant
	 */
	public ClanGovernment getGovernment();

	/**
	 * Returns the minimum clan members for the clan
	 * to become active, or to prevent being purged.
	 * A value of 0 keeps the clan alive forever
	 * @return minimum number of clan members.
	 */
	public int getMinClanMembers();

	/**
	 * Sets the minimum clan members for the clan
	 * to become active, or to prevent being purged.
	 * A value of 0 keeps the clan alive forever
	 * @param amt the new min clan members
	 */
	public void setMinClanMembers(int amt);
	/**
	 * Returns whether this clan attains conquest loyalty
	 * through giving out clan items.
	 * @return true if if does, false otherwise
	 */
	public boolean isLoyaltyThroughItems();

	/**
	 * Returns whether this clan attains conquest loyalty
	 * through mobs worshiping the same deity as their leader.
	 * @return true if if does, false otherwise
	 */
	public boolean isWorshipConquest();

	/**
	 * Returns the id constant representing the government type
	 * of this
	 * @see Clan
	 * @see Clan#getGovernmentID()
	 * @see Clan#getGovernment()
	 * @param type the type id constant
	 */
	public void setGovernmentID(int type);

	/**
	 * Returns the roleid constant representing the highest rank
	 * in this clan type that can perform the given function
	 * or null.
	 * @see ClanPosition
	 * @see Function
	 * @param func the function (or null) they must be able to do
	 * @param mob the mob to check for a top rank
	 * @return the roleid constant
	 */
	public int getTopQualifiedRoleID(Function func, MOB mob);

	/**
	 * Returns the roleid(s) constant representing the highest rank
	 * roleid(s) in this clan type that can perform the given function.
	 * @see ClanPosition
	 * @see Function
	 * @param func the function to perform, or null just to return privileged rank
	 * @return the top roleid
	 */
	public List<Integer> getTopRankedRoles(Function func);

	/**
	 * Returns the number of roles (max roleid)
	 * @return number of roles
	 */
	public int getNumberRoles();

	/**
	 * Del a member from this clan
	 * @param M the member to remove
	 */
	public void delMember(MOB M);

	/**
	 * Adds a new member to this clan
	 * @param M the member to add
	 * @param role the coded role number
	 */
	public void addMember(MOB M, int role);

	/**
	 * Orders the system to update this clan in the database.
	 */
	public void update();

	/**
	 * Orders the system to destroy this clan in the database.
	 */
	public void destroyClan();

	/**
	 * Orders the system to create this clan in the database.
	 */
	public void create();

	/**
	 * Gets external items belonging to this clan, which should be destroyed with the
	 * clan, but can still be transient.  These are items like artifacts, or ships, 
	 * vehicles, etc.
	 *
	 * @see com.planet_ink.coffee_mud.core.interfaces.ItemCollection
	 * @see com.planet_ink.coffee_mud.Items.interfaces.Item
	 *
	 * @return an item collection
	 */
	public ItemCollection getExtItems();
	
	/**
	 * Returns whether the given user can be assigned the given role
	 * @param mob the mob to check
	 * @param role the role to check for
	 * @return true if they can, false otherwise
	 */
	public boolean canBeAssigned(MOB mob, int role);

	/**
	 * Represents an individual clan vote
	 * @author Bo Zimmerman
	 *
	 */
	public static class ClanVote
	{
		/**
		 * Text to describe this vote to the voter.
		 */
		public String voteStarter="";
		/**
		 * One of the VSTAT_* constants representing
		 * the status of this vote.
		 * @see Clan
		 */
		public int voteStatus=0;
		/**
		 * Date/time stamp for when the vote was started.
		 */
		public long voteStarted=0;
		/**
		 * The command to execute if the vote passes.
		 */
		public String matter="";
		/**
		 * The FUNC_* constant representing this vote.
		 * @see Clan
		 */
		public int function=0;
		/**
		 * A 2 dimentional vector of the votes, where each
		 * row is a vote, with dimension 1 being the member name
		 * and dimension 2 being a Boolean representing their choice.
		 */
		public PairVector<String,Boolean> votes=null;
	}

	/**
	 * A internal membership record, as returned by the database
	 * @author Bo Zimmerman
	 */
	public class MemberRecord
	{
		public String name;
		public int role;
		public int mobpvps = 0;
		public int playerpvps=0;
		public MemberRecord(String name, int role, int mobpvps, int playerpvps)
		{
			this.name = name;
			this.role = role;
			this.mobpvps = mobpvps;
			this.playerpvps = playerpvps;
		}

		@Override
		public String toString()
		{
			return name;
		}
	}
	/**
	 * A internal membership record, as returned by the database,
	 * plus extra fields from cmchar
	 * @author Bo Zimmerman
	 */
	public class FullMemberRecord extends MemberRecord
	{
		public int level;
		public long timestamp;
		public boolean isAdmin;
		public FullMemberRecord(String name, int level, int role, long timestamp, int mobpvps, int playerpvps, boolean isAdmin)
		{
			super(name,role,mobpvps,playerpvps); 
			this.level=level; 
			this.timestamp=timestamp;
			this.isAdmin=isAdmin;
		}
	}

	/** Vote just started constant for the ClanVote.voteStatus member. @see ClanVote#voteStatus */
	public final static int VSTAT_STARTED=0;
	/** Vote has failed constant for the ClanVote.voteStatus member. @see ClanVote#voteStatus */
	public final static int VSTAT_FAILED=1;
	/** Vote has passed constant for the ClanVote.voteStatus member. @see ClanVote#voteStatus */
	public final static int VSTAT_PASSED=2;
	/** Descriptors for the values of ClanVote.voteStatus member. @see ClanVote#voteStatus */
	public final static String[] VSTAT_DESCS=CMLib.lang().sessionTranslation(new String[]{
		"In Progress",
		"Failed",
		"Passed"
	});

	/** Stat variables associated with clan objects. */
	public final static String[] CLAN_STATS={
		"ACCEPTANCE", // 0
		"DETAIL", // 1
		"DONATEROOM", // 2
		"EXP", // 3
		"GOVT", // 4
		"MORGUE", // 5
		"POLITICS", // 6
		"PREMISE", // 7
		"RECALL", // 8
		"SIZE", // 9
		"STATUS", // 10
		"TAXES", // 11
		"TROPHIES", // 12
		"TYPE", // 13
		"AREAS", // 14
		"MEMBERLIST", // 15
		"TOPMEMBER", // 16
		"CLANLEVEL", // 17
		"CATEGORY", // 18
		"RIVALROUS",//19
		"MINMEMBERS", //20
		"CLANCHARCLASS", // 21
		"NAME" // 22
	};

	/** constant for the getStatus() method, denoting normal status. @see Clan#getStatus() .*/
	public static final int CLANSTATUS_ACTIVE=0;
	/** constant for the getStatus() method, denoting unapproved status. @see Clan#getStatus() .*/
	public static final int CLANSTATUS_PENDING=1;
	/** constant for the getStatus() method, denoting fading status. @see Clan#getStatus() .*/
	public static final int CLANSTATUS_FADING=2;
	/** constant descriptor strings for the CLANSTATUS_* constants, ordered by their value. @see Clan .*/
	public static final String[] CLANSTATUS_DESC={
		"ACTIVE",
		"PENDING",
		"FADING"
	};

	/** constant for getClanRelations method, denoting neutral status towards. @see Clan#getClanRelations(String) */
	public static final int REL_NEUTRAL=0;
	/** constant for getClanRelations method, denoting at war with. @see Clan#getClanRelations(String) */
	public static final int REL_WAR=1;
	/** constant for getClanRelations method, denoting hostile to. @see Clan#getClanRelations(String) */
	public static final int REL_HOSTILE=2;
	/** constant for getClanRelations method, denoting friendly to. @see Clan#getClanRelations(String) */
	public static final int REL_FRIENDLY=3;
	/** constant for getClanRelations method, denoting ally with. @see Clan#getClanRelations(String) */
	public static final int REL_ALLY=4;
	/** constant used to determine something about clan relations */
	public static final int[] REL_NEUTRALITYGAUGE={/*REL_NEUTRAL*/0,/*REL_WAR*/4, /*REL_HOSTILE*/1,/*REL_FRIENDLY*/1,/*REL_ALLY*/4};
	/** table used to determine how two clans are related when their declared relations differ.  both axis are REL_* constants. @see Clan */
	public static final int[][] RELATIONSHIP_VECTOR={
	{REL_NEUTRAL,	REL_WAR,		REL_HOSTILE,	REL_FRIENDLY,	REL_FRIENDLY},
	{REL_WAR,		REL_WAR,		REL_WAR,		REL_WAR,		REL_WAR},
	{REL_HOSTILE,	REL_WAR,		REL_HOSTILE,	REL_HOSTILE,	REL_HOSTILE},
	{REL_FRIENDLY,	REL_WAR,		REL_HOSTILE,	REL_FRIENDLY,	REL_FRIENDLY},
	{REL_FRIENDLY,	REL_WAR,		REL_HOSTILE,	REL_FRIENDLY,	REL_ALLY},
	};
	/** descriptor strings for the REL_* constants, ordered by their value.  @see Clan */
	public static final String[] REL_DESCS={
		"NEUTRAL","WAR","HOSTILE","FRIENDLY","ALLY"
	};

	/** long descriptor strings for the REL_* constants, ordered by their value.  @see Clan */
	public static final String[] REL_STATES={
		"NEUTRALITY TOWARDS",
		"WAR WITH",
		"HOSTILITIES WITH",
		"FRIENDSHIP WITH",
		"AN ALLIANCE WITH"
	};

	/**
	 * enum for the getTrophies @see Clan#getTrophies() method.
	 * @author Bo Zimmerman
	 */
	public static enum Trophy
	{
		Points("Most control points","Control Points"),
		Experience("Most clan experience","Experience"),
		Areas("Most controlled areas","Areas Controlled"),
		PlayerKills("Most rival player-kills","PlayerKills"),
		Members("Most members","Most Members"),
		MemberLevel("Highest median level","Highest Levels")
		;
		public final String description;
		public final String codeString;
		private Trophy(String desc, String codeName)
		{
			this.description=desc;
			this.codeString=codeName;
		}

		public int flagNum(){return (int)Math.round(Math.pow(2.0, ordinal())); }
	}

	/**
	 * An enumeration of relationships between a clan position and a
	 * particular power of clans.
	 * @author Bo Zimmermanimmerman
	 */
	public static enum Authority
	{
		CAN_NOT_DO,
		CAN_DO,
		MUST_VOTE_ON
	}

	/**
	 * An enumeration of ways auto-promotion can work in a clan
	 * @author Bo Zimmermanimmerman
	 */
	public static enum AutoPromoteFlag
	{
		NONE,
		LEVEL,
		RANK,
		LEVEL_OVERWRITE,
		RANK_OVERWRITE
	}

	/**
	 * An enumation of all the major clan functions gated
	 * by internal security
	 * @author Bo Zimmermanimmerman
	 */
	public static enum Function
	{
		/** constant for the clan function of accepting new members. @see Clan#getAuthority(int,Function) */
		ACCEPT,
		/** constant for the clan function of promoting or demoting members. @see Clan#getAuthority(int,Function) */
		ASSIGN,
		/** constant for the clan function of exihiling members. @see Clan#getAuthority(int,Function) */
		EXILE,
		/** constant for the clan function of setting a new clan home. @see Clan#getAuthority(int,Function) */
		SET_HOME,
		/** constant for the clan function of setting a new donation room. @see Clan#getAuthority(int,Function) */
		SET_DONATE,
		/** constant for the clan function of rejecting an applicant. @see Clan#getAuthority(int,Function) */
		REJECT,
		/** constant for the clan function of writing a new clan premise. @see Clan#getAuthority(int,Function) */
		PREMISE,
		/** constant for the clan function of acting as owner of clan property. @see Clan#getAuthority(int,Function) */
		PROPERTY_OWNER,
		/** constant for the clan function of withdrawing from clan bank accounts. @see Clan#getAuthority(int,Function) */
		WITHDRAW,
		/** constant for the clan function of depositing into clan bank accounts. @see Clan#getAuthority(int,Function) */
		DEPOSIT,
		/** constant for the clan function of ordering lower ranked clan members. @see Clan#getAuthority(int,Function) */
		ORDER_UNDERLINGS,
		/** constant for the clan function of ordering mobs in clan conquered areas. @see Clan#getAuthority(int,Function) */
		ORDER_CONQUERED,
		/** constant for the clan function of voting on promotions. @see Clan#getAuthority(int,Function) */
		VOTE_ASSIGN,
		/** constant for the clan function of voting on non-promotion questions . @see Clan#getAuthority(int,Function) */
		VOTE_OTHER,
		/** constant for the clan function of depositing and listing clan bank accounts. @see Clan#getAuthority(int,Function) */
		DEPOSIT_LIST,
		/** constant for the clan function of declaring war and peace . @see Clan#getAuthority(int,Function) */
		DECLARE,
		/** constant for the clan function of changing the clans tax rate. @see Clan#getAuthority(int,Function) */
		TAX,
		/** constant for the clan function of clanenchanting items. @see Clan#getAuthority(int,Function) */
		ENCHANT,
		/** constant for the clan function of channeling with  @see Clan#getAuthority(int,Function) */
		CHANNEL,
		/** constant for the clan function of using the morgue. @see Clan#getAuthority(int,Function) */
		MORGUE,
		/** constant for the clan function of seeing private members. @see Clan#getAuthority(int,Function) */
		LIST_MEMBERS,
		/** constant for the clan function of enjoying clan homes. @see Clan#getAuthority(int,Function) */
		HOME_PRIVS,
		/** constant for the clan function of enjoying clan homes. @see Clan#getAuthority(int,Function) */
		CLAN_BENEFITS,
		/** constant for the clan function of enjoying clan homes. @see Clan#getAuthority(int,Function) */
		CLAN_TITLES,
	}
}
