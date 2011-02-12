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
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZapperMask;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;


/*
   Copyright 2000-2011 Jeremy Vyska, Bo Zimmerman

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
	 * @param mob the mob whose priviledges to check.
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
	 * series of Clan.ClanVote objects
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan.ClanVote
	 * @return the set of clan votes
	 */
	public Enumeration<ClanVote> votes();
	/**
	 * Orders the system to save any changes to existing
	 * ongoing votes for this clan.
	 */
	public void updateVotes();
	/**
	 * Adds a new ClanVote object to the list of ongoing
	 * votes for this clan.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan.ClanVote
	 * @param CV the clanvote object to add
	 */
	public void addVote(ClanVote CV);
	/**
	 * Removes a new ClanVote object from the list of ongoing
	 * votes for this clan.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan.ClanVote
	 * @param CV the clanvote object to remove
	 */
	public void delVote(ClanVote CV);

	/**
	 * Returns the number of members allowed to vote on the
	 * given function.  The function is one of the Clan.FUNC_*
	 * constants.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan
	 * @param function the FUNC_* constant
	 * @return the number of members of this clan who can vote on it
	 */
	public int getNumVoters(Function function);
	/**
	 * Returns the number of members of this clan.
	 * @return the membership count.
	 */
	public int getSize();

	/**
	 * Returns the name of the clan, which is almost always the
	 * same as the ID, which is why clans can't change their names.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#setName(String)
	 * @return the name of the clan.
	 */
	public String getName();
	/**
	 * Returns the unique identifying ID of the clan for reference
	 * elsewhere.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#getName()
	 * @return the unique identifier of the clan.
	 */
	public String clanID();
	/**
	 * Sets a new name for this clan.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#getName()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#clanID()
	 * @param newName the new name of this clan
	 */
	public void setName(String newName);
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
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#getPremise()
	 */
	public void setPremise(String newPremise);

	/**
	 * Sets this Clan's enforced character class.
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
	 * to join a particular clan.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#setAcceptanceSettings(String)
	 * @return the mask used to identify prospects
	 */
	public String getAcceptanceSettings();
	/**
	 * Sets a mask used to identify whether a player is allowed
	 * to join a particular clan.
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
	 * Returns whether membership is automatically approved to this clan.
	 * And if so, what the default position for members are.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#setAutoPosition(int)
	 * @return xml document describing the politics of this clan
	 */
	public int getAutoPosition();
	/**
	 * Sets whether membership is automatically approved to this clan.
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
	 * @return a Clan.CLANSTAT_* constant
	 */
	public int getStatus();
	/**
	 * Sets one of the CLANSTAT_* constants describing the pending
	 * status of this clan for acceptable, or whether its one its way out.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#getStatus()
	 * @param newStatus a Clan.CLANSTAT_* constant
	 */
	public void setStatus(int newStatus);

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
	 * Returns a bitmap representing the trophies won by this clan.
	 * The bitmap is made up of Clan.TROPHY_* constants.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#setTrophies(int)
	 * @return a bitmap of the trophies
	 */
	public int getTrophies();
	/**
	 * Sets a bitmap representing the trophies won by this clan.
	 * The bitmap is made up of Clan.TROPHY_* constants.
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
	 * Gets the amount of experience earned by this clan.
	 * @return the experience earned
	 */
	public long getExp();
	/**
	 * Sets the amount of experience earned by this clan.
	 * @param exp the experience earned
	 */
	public void setExp(long exp);
	/**
	 * Adjusts the amount of experience earned by this clan.
	 * @param howMuch the experience adjustment, + or -
	 */
	public void adjExp(int howMuch);
	/**
	 * Adjusts the amount of experience earned by a player based
	 * on the tax rate.  Will automatically adjust the exp of
	 * the clan and save it.
	 * @param exp the old experience
	 */
	public int applyExpMods(int exp);
	/**
	 * Called when a member of this clan kills a member of another clan.
	 * Will update the clan in the database.
	 */
    public void recordClanKill();
    /**
     * Returns the number of other-clan kills this clan has recorded.
     * @return the number of kills.
     */
    public int getCurrentClanKills();
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
	 * Returns all the areas on the map controlled by this clan.
	 * @see com.planet_ink.coffee_mud.Behaviors.Conquerable
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area
	 * @return a vector of area objects
	 */
	public List<Area> getControlledAreas();

	/**
	 * Sets the tax rate for this clan.
	 * @see Clan#getTaxes()
	 * @param rate the tax rate 0-100.0
	 */
	public void setTaxes(double rate);
	/**
	 * Gets the tax rate for this clan.
	 * @see Clan#setTaxes(double)
	 * @return rate the tax rate 0-100.0
	 */
	public double getTaxes();
	
	/**
	 * Returns the maximum number of players who can hold the given 
	 * role in this clan.  For assignment purposes.
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
     * @param government the clan governmnet
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
	 * @see Clan.MemberRecord
	 * @return the membership
	 */
	public List<MemberRecord> getMemberList();
	/**
	 * Returns the set of members, where
	 * each row represents a MemberRecord.
	 * Will filter by the given Clan.POS_* constant.
	 * @see Clan
	 * @see Clan.MemberRecord
	 * @param PosFilter
	 * @return the membership
	 */
	public List<MemberRecord> getMemberList(int PosFilter);

	/**
	 * Returns the highest ranking member of this clan.
	 * @return the mob object for the highest ranking member.
	 */
	public MOB getResponsibleMember();

	/**
	 * Returns a Clan.REL_* constant denoting the relationship
	 * between this clan and the clan of the given name.
	 * @see Clan#setClanRelations(String, int, long)
	 * @see Clan#getLastRelationChange(String)
	 * @param id the name of another clan.
	 * @return the Clan.REL_* constant
	 */
	public int getClanRelations(String id);
	/**
	 * Returns the time/date stamp when this clan last changed
	 * its relations with the given clan.
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
	 * @param rel the Clan.REL_* constant for the new relationship
	 * @param time a current date/time stamp for this change
	 */
	public void setClanRelations(String id, int rel, long time);
	
	/**
	 * Returns the id representing the government type
	 * of this clan.
	 * @see Clan
	 * @see Clan#setGovernmentID(int)
	 * @see Clan#getGovernment()
	 * @return the if constant
	 */
	public int getGovernmentID();
	
	/**
	 * Returns the object representing the government
	 * of this clan.
	 * @see Clan.Government
	 * @see Clan#getGovernmentID()
	 * @see Clan#setGovernmentID(int)
	 * @return the if constant
	 */
    public Government getGovernment();
    
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
	public boolean isLoyaltyThroughWorship();
	
	/**
	 * Returns the id constant representing the government type
	 * of this clan.
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
	 * @see Clan.Position
	 * @see Clan.Function
     * @param func the function (or null) they must be able to do
     * @param mob the mob to check for a top rank
	 * @return the roleid constant
	 */
	public int getTopQualifiedRoleID(Function func, MOB mob);
	
	/**
	 * Returns the roleid(s) constant representing the highest rank
	 * roleid(s) in this clan type that can perform the given function.
	 * @see Clan.Position
	 * @see Clan.Function
	 * @parm func the function to perform, or null just to return privileged rank
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
	/*
	 * Returns whether the given user can be assigned the given role
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
         * One of the Clan.VSTAT_* constants representing
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
         * The Clan.FUNC_* constant representing this vote.
         * @see Clan
         */
        public int function=0;
        /**
         * A 2 dimentional vector of the votes, where each
         * row is a vote, with dimension 1 being the member name
         * and dimension 2 being a Boolean representing their choice.
         */
        public DVector votes=null;
    }
    
    /**
     * A internal membership record, as returned by the database 
     * @author Bo Zimmerman
     */
    public class MemberRecord
    {
    	public String name;
    	public int role;
    	public long timestamp;
    	public MemberRecord(String name, int role, long timestamp) {
    		this.name=name; this.role=role; this.timestamp=timestamp;
    	}
    	public String toString() { return name;}
    }
    
    /** Vote just started constant for the Clan.ClanVote.voteStatus member. @see Clan.ClanVote#voteStatus */
	public final static int VSTAT_STARTED=0;
    /** Vote has failed constant for the Clan.ClanVote.voteStatus member. @see Clan.ClanVote#voteStatus */
	public final static int VSTAT_FAILED=1;
    /** Vote has passed constant for the Clan.ClanVote.voteStatus member. @see Clan.ClanVote#voteStatus */
	public final static int VSTAT_PASSED=2;
    /** Descriptors for the values of Clan.ClanVote.voteStatus member. @see Clan.ClanVote#voteStatus */
	public final static String[] VSTAT_DESCS={
		"In Progress",
		"Failed",
		"Passed"
	};

	
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
        "TOPMEMBER" // 16
    };

	/** constant for the Clan.getStatus() method, denoting normal status. @see Clan#getStatus() .*/
	public static final int CLANSTATUS_ACTIVE=0;
	/** constant for the Clan.getStatus() method, denoting unapproved status. @see Clan#getStatus() .*/
	public static final int CLANSTATUS_PENDING=1;
	/** constant for the Clan.getStatus() method, denoting fading status. @see Clan#getStatus() .*/
	public static final int CLANSTATUS_FADING=2;
	/** constant descriptor strings for the Clan.CLANSTATUS_* constants, ordered by their value. @see Clan .*/
	public static final String[] CLANSTATUS_DESC={
		"ACTIVE",
		"PENDING",
		"FADING"
	};
	
	/** constant for Clan.getClanRelations method, denoting neutral status towards. @see Clan#getClanRelations(String) */
	public static final int REL_NEUTRAL=0;
	/** constant for Clan.getClanRelations method, denoting at war with. @see Clan#getClanRelations(String) */
	public static final int REL_WAR=1;
	/** constant for Clan.getClanRelations method, denoting hostile to. @see Clan#getClanRelations(String) */
	public static final int REL_HOSTILE=2;
	/** constant for Clan.getClanRelations method, denoting friendly to. @see Clan#getClanRelations(String) */
	public static final int REL_FRIENDLY=3;
	/** constant for Clan.getClanRelations method, denoting ally with. @see Clan#getClanRelations(String) */
	public static final int REL_ALLY=4;
    /** constant used to determine something about clan relations */
    public static final int[] REL_NEUTRALITYGAUGE={/*REL_NEUTRAL*/0,/*REL_WAR*/4, /*REL_HOSTILE*/1,/*REL_FRIENDLY*/1,/*REL_ALLY*/4};
    /** table used to determine how two clans are related when their declared relations differ.  both axis are Clan.REL_* constants. @see Clan */
	public static final int[][] RELATIONSHIP_VECTOR={
	{REL_NEUTRAL,	REL_WAR,		REL_HOSTILE,	REL_FRIENDLY,	REL_FRIENDLY},
	{REL_WAR,		REL_WAR,		REL_WAR,		REL_WAR,		REL_WAR},
	{REL_HOSTILE,	REL_WAR,		REL_HOSTILE,	REL_HOSTILE,	REL_HOSTILE},
	{REL_FRIENDLY,	REL_WAR,		REL_HOSTILE,	REL_FRIENDLY,	REL_FRIENDLY},
	{REL_FRIENDLY,	REL_WAR,		REL_HOSTILE,	REL_FRIENDLY,	REL_ALLY},
	};
	/** descriptor strings for the Clan.REL_* constants, ordered by their value.  @see Clan */
	public static final String[] REL_DESCS={
		"NEUTRAL","WAR","HOSTILE","FRIENDLY","ALLY"
	};
	/** long descriptor strings for the Clan.REL_* constants, ordered by their value.  @see Clan */
	public static final String[] REL_STATES={
		"NEUTRALITY TOWARDS",
		"WAR WITH",
		"HOSTILITIES WITH",
		"FRIENDSHIP WITH",
		"AN ALLIANCE WITH"
	};

	/** constant for the Clan.getTrophies @see Clan#getTrophies() method. Denotes control points. */
	public static final int TROPHY_CONTROL=1;
	/** constant for the Clan.getTrophies @see Clan#getTrophies() method. Denotes exp. */
	public static final int TROPHY_EXP=2;
	/** constant for the Clan.getTrophies @see Clan#getTrophies() method. Denotes most areas. */
	public static final int TROPHY_AREA=4;
	/** constant for the Clan.getTrophies @see Clan#getTrophies() method. Denotes most pkills. */
    public static final int TROPHY_PK=8;
	/** descriptor strings for the Clan.TROPHY_* constants, ordered by their value.  @see Clan */
	public static final String TROPHY_DESCS_SHORT[]={"","CP","EXP","","AREA","","","","PK"};
	/** long descriptor strings for the Clan.TROPHY_* constants, ordered by their value.  @see Clan */
	public static final String TROPHY_DESCS[]={"","Most control points","Most clan experience","","Most controlled areas","","","","Most rival player-kills"};

	/**
	 * An enumeration of relationships between a clan position and a
	 * particular power of clans.
	 * @author bzimmerman
	 */
	public static enum Authority
	{
		CAN_NOT_DO,
		CAN_DO,
		MUST_VOTE_ON
	}
	
	/**
	 * An enumeration of ways auto-promotion can work in a clan
	 * @author bzimmerman
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
	 * @author bzimmerman
	 */
	public static enum Function
	{
		/** constant for the clan function of accepting new members. @see Clan#getAuthority(int,Clan.Function) */
		ACCEPT,
		/** constant for the clan function of promoting or demoting members. @see Clan#getAuthority(int,Clan.Function) */
		ASSIGN,
		/** constant for the clan function of exihiling members. @see Clan#getAuthority(int,Clan.Function) */
		EXILE,
		/** constant for the clan function of setting a new clan home. @see Clan#getAuthority(int,Clan.Function) */
		HOMESET,
		/** constant for the clan function of setting a new donation room. @see Clan#getAuthority(int,Clan.Function) */
		DONATESET,
		/** constant for the clan function of rejecting an applicant. @see Clan#getAuthority(int,Clan.Function) */
		REJECT,
		/** constant for the clan function of writing a new clan premise. @see Clan#getAuthority(int,Clan.Function) */
		PREMISE,
		/** constant for the clan function of acting as owner of clan property. @see Clan#getAuthority(int,Clan.Function) */
		PROPERTYOWNER,
		/** constant for the clan function of withdrawing from clan bank accounts. @see Clan#getAuthority(int,Clan.Function) */
		WITHDRAW,
		/** constant for the clan function of ordering lower ranked clan members. @see Clan#getAuthority(int,Clan.Function) */
		CANORDERUNDERLINGS,
		/** constant for the clan function of ordering mobs in clan conquered areas. @see Clan#getAuthority(int,Clan.Function) */
		CANORDERCONQUERED,
		/** constant for the clan function of voting on promotions. @see Clan#getAuthority(int,Clan.Function) */
		VOTEASSIGN,
		/** constant for the clan function of voting on non-promotion questions . @see Clan#getAuthority(int,Clan.Function) */
		VOTEOTHER,
		/** constant for the clan function of depositing and listing clan bank accounts. @see Clan#getAuthority(int,Clan.Function) */
		DEPOSITLIST,
		/** constant for the clan function of declaring war and peace . @see Clan#getAuthority(int,Clan.Function) */
		DECLARE,
		/** constant for the clan function of changing the clans tax rate. @see Clan#getAuthority(int,Clan.Function) */
		TAX,
		/** constant for the clan function of clanenchanting items. @see Clan#getAuthority(int,Clan.Function) */
		ENCHANT,
		/** constant for the clan function of channeling with clan. @see Clan#getAuthority(int,Clan.Function) */
		CHANNEL,
		/** constant for the clan function of using the morgue. @see Clan#getAuthority(int,Clan.Function) */
		MORGUE,
		/** constant for the clan function of seeing private members. @see Clan#getAuthority(int,Clan.Function) */
		LISTMEMBERS,
		/** constant for the clan function of enjoying clan homes. @see Clan#getAuthority(int,Clan.Function) */
		HOMEPRIVILEGES,
		/** constant for the clan function of enjoying clan homes. @see Clan#getAuthority(int,Clan.Function) */
		CLANSPELLS,
		/** constant for the clan function of enjoying clan homes. @see Clan#getAuthority(int,Clan.Function) */
		CLANTITLES,
	}
	
	/**
	 * A class for the characteristics of a position within a clan.
	 * @author bzimmerman
	 */
	public static class	Position
	{
		/** the named ID of the position */
		public String 	ID;
		/** the named ID of the position */
		public int 		roleID;
		/** the ordered rank of the position */
		public int 		rank;
		/** the name of the position within this government */
		public String	name;
		/** the plural name of the position within this government */
		public String	pluralName;
		/** the maximum number of members that can hold this position */
		public int		max;
		/** the internal zapper mask for internal requirements to this position */
		public String	innerMaskStr;
		/** the internal zapper mask for internal requirements to this position */
		public boolean 	isPublic;
		/** the zapper mask for internal requirements to this position */
		public CompiledZapperMask	internalMask;
		/** a chart of whether this position can perform the indexed function in this government */
		public Authority[] functionChart;
		
		/**
		 * Initialize a new clan position
		 * @param ID the coded name of the rank
		 * @param roleID the numeric ID of the rank
		 * @param rank the ordered rank number (0+)
		 * @param name the name of this position
		 * @param plural the plural name of this position
		 * @param max the maximum number of members that can hold this position
		 * @param innermask basic internal requirements for this position
		 * @param funcChart an array of ClanPositionPower objects for each function that can be performed.
		 */
		public Position(String ID, int roleID, int rank, String name, String plural, int max, String innerMask, Authority[] funcChart, boolean isPublic)
		{
			this.ID=ID; this.roleID=roleID; this.pluralName=plural; this.innerMaskStr=innerMask;
			this.rank=rank; this.name=name; this.max=max;this.functionChart=funcChart;
			this.isPublic=isPublic;
			internalMask=CMLib.masking().maskCompile(innerMask);
		}
	}

	/**
	 * A class defining the characteristics of a clan government,
	 * and its membership.
	 * @author bzimmerman
	 */
	public static class Government
	{
		/** If this is a default government type, this is its ID, otherwise -1 */
		public int		ID;
		/** The name of this government type, which is its identifier when ID above is -1 */
		public String	name;
		/** The role automatically assigned to those who apply successfully */
		public int		autoRole;
		/** The role automatically assigned to those who are accepted */
		public int		acceptPos;
		/** A short description of this government type for players */
		public String	shortDesc;
		/** Zapper mask for requirements to even apply */
		public String	requiredMaskStr;
		/**  Whether this clan type is shown on the list  */
		public boolean	isPublic;
		/**  Whether mambers must all be in the same family */
		public boolean	isFamilyOnly;
		/**  The number of minimum members for the clan to survive -- overrides coffeemud.ini */
		public Integer	overrideMinMembers;
		/** Whether conquest is enabled for this clan */
		public boolean	conquestEnabled;
		/** Whether clan items increase loyalty in conquered areas for this clan type */
		public boolean	conquestItemLoyalty;
		/** Whether loyalty and conquest are determined by what deity the mobs are */
		public boolean	conquestDeityBasis;
		/** maximum number of mud days a vote will go on for */
		public int		maxVoteDays;
		/** minimum % of voters who must have voted for a vote to be valid if time expires*/
		public int		voteQuorumPct;
		/**  Whether this is the default government  */
		public boolean	isDefault = false;
		/** The list of ClanPosition objects for each holdable position in this government */
		public Position[] positions;
		/**  Whether an unfilled topRole is automatically filled by those who meet its innermask  */
		public AutoPromoteFlag 		autoPromoteBy;
		/** Zapper mask for requirements to even apply */
		public CompiledZapperMask   requiredMask;
		
		/**
		 * Initialize a new Clan Government
		 * @param ID
		 * @param name
		 * @param pos
		 * @param highPos
		 * @param autoPos
		 * @param acceptPos
		 * @param requiredMask
		 * @param autoPromoteBy
		 * @param isPublic
		 * @param isFamilyOnly
		 * @param overrideMinMembers
		 * @param conquestEnabled
		 * @param conquestItemLoyalty
		 * @param conquestDeityBasis
		 * @param shortDesc
		 * @param maxVoteDays
		 * @param voteQuorumPct
		 */
		public Government(int ID, String name, Position[] pos, int autoPos, int acceptPos,
						  String requiredMask, AutoPromoteFlag autoPromoteBy, boolean isPublic, boolean isFamilyOnly,
						  Integer overrideMinMembers,
						  boolean conquestEnabled, boolean conquestItemLoyalty, boolean conquestDeityBasis,
						  String shortDesc, int maxVoteDays, int voteQuorumPct)
		{
			this.ID=ID; this.name=name; this.positions=pos; this.autoRole=autoPos; this.acceptPos=acceptPos;
			this.requiredMaskStr=requiredMask; this.autoPromoteBy=autoPromoteBy; this.isPublic=isPublic;
			this.isFamilyOnly=isFamilyOnly; this.overrideMinMembers=overrideMinMembers; 
			this.conquestEnabled=conquestEnabled; this.conquestItemLoyalty=conquestItemLoyalty;
			this.conquestDeityBasis=conquestDeityBasis; this.shortDesc=shortDesc;
			this.maxVoteDays=maxVoteDays;this.voteQuorumPct=voteQuorumPct;
			this.requiredMask=CMLib.masking().maskCompile(requiredMaskStr);
		}
	}
}
