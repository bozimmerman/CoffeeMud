package com.planet_ink.coffee_mud.Common.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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

import java.util.*;


/* 
   Copyright 2000-2006 Jeremy Vyska, Bo Zimmerman

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
public interface Clan extends Cloneable, Tickable
{

	/**
	 * Returns whether the given mob is allowed to perform the
	 * given function.  The return value can designate that they
	 * may do it now, that they may not do it, or that it requires
	 * a vote.  A return value 1 means they can do it now, 0 means
	 * it requires a vote, and -1 means they can never do it. The
	 * functions are defined in the Clan interface as FUNC_* 
	 * constants. 
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan
	 * @param mob the mob whose priviledges to check.
	 * @param function the Clan.FUNC_* constant to use
	 * @return either 1, 0, or -1 saying if they are allowed.
	 */
	public int allowedToDoThis(MOB mob, int function);
	
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
	public Enumeration votes();
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
	public void addVote(Object CV);
	/**
	 * Removes a new ClanVote object from the list of ongoing
	 * votes for this clan.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan.ClanVote
	 * @param CV the clanvote object to remove
	 */
	public void delVote(Object CV);
	
	/**
	 * Returns the number of members allowed to vote on the 
	 * given function.  The function is one of the Clan.FUNC_*
	 * constants.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan
	 * @param function the FUNC_* constant 
	 * @return the number of members of this clan who can vote on it
	 */
	public int getNumVoters(int function);
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
	 * Returns the clan type, a meaningless variable.
	 * @return always returns TYPE_CLAN
	 */
	public int getType();
	/**
	 * Returns Clan, Republic, or another one of the 
	 * clan government types defined by Clan.GVT_DESC
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan
	 * @return the clans government types
	 */
	public String typeName();

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
	 * Sets this Clan's basic story.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan#getPremise()
	 */
	public void setPremise(String newPremise);

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

	public int getTrophies();
	public void setTrophies(int trophyFlag);

	public String getDonation();
	public void setDonation(String newDonation);
  
	public long getExp();
	public void setExp(long exp);
	public void adjExp(int howMuch);
    
    public void recordClanKill();
    public int getCurrentClanKills();
	
	public long calculateMapPoints(Vector controlledAreas);
	public long calculateMapPoints();
	public Vector getControlledAreas();
	
	public int applyExpMods(int exp);
	public void setTaxes(double rate);
	public double getTaxes();
	
	public DVector getMemberList();
	public DVector getMemberList(int PosFilter);
	
	public MOB getResponsibleMember();

	public int getClanRelations(String id);
	public long getLastRelationChange(String id);
	public void setClanRelations(String id, int rel, long time);
	
	public int getGovernment();
	public void setGovernment(int type);
		
	public int getTopRank();

	public void update();
	public void destroyClan();
	public void create();
    
    public static class ClanVote
    {
        public String voteStarter="";
        public int voteStatus=0;
        public long voteStarted=0;
        public String matter="";
        public int function=0;
        public DVector votes=null;
    }
    
	public final static int VSTAT_STARTED=0;
	public final static int VSTAT_FAILED=1;
	public final static int VSTAT_PASSED=2;
	
	public final static String[] VSTAT_DESCS={
		"In Progress",
		"Failed",
		"Passed"
	};
	
	public static final int POS_APPLICANT=0;
	public static final int POS_MEMBER=1;
	public static final int POS_STAFF=2;
	public static final int POS_ENCHANTER=4;
	public static final int POS_TREASURER=8;
	public static final int POS_LEADER=16;
	public static final int POS_BOSS=32;
	public static final int[] POSORDER={POS_APPLICANT,
										POS_MEMBER,
										POS_STAFF,
										POS_ENCHANTER,
										POS_TREASURER,
										POS_LEADER,
										POS_BOSS};

	public static final int CLANSTATUS_ACTIVE=0;
	public static final int CLANSTATUS_PENDING=1;
	public static final int CLANSTATUS_FADING=2;
	public static final String[] CLANSTATUS_DESC={
		"ACTIVE",
		"PENDING",
		"FADING"
	};
	
	public static final int REL_NEUTRAL=0;
	public static final int REL_WAR=1;
	public static final int REL_HOSTILE=2;
	public static final int REL_FRIENDLY=3;
	public static final int REL_ALLY=4;
    
    public static final int[] REL_NEUTRALITYGAUGE={/*REL_NEUTRAL*/0,/*REL_WAR*/4, /*REL_HOSTILE*/1,/*REL_FRIENDLY*/1,/*REL_ALLY*/4};

	public static final int[][] RELATIONSHIP_VECTOR={
	{REL_NEUTRAL,	REL_WAR,		REL_HOSTILE,	REL_FRIENDLY,	REL_FRIENDLY},
	{REL_WAR,		REL_WAR,		REL_WAR,		REL_WAR,		REL_WAR},
	{REL_HOSTILE,	REL_WAR,		REL_HOSTILE,	REL_HOSTILE,	REL_HOSTILE},
	{REL_FRIENDLY,	REL_WAR,		REL_HOSTILE,	REL_FRIENDLY,	REL_FRIENDLY},
	{REL_FRIENDLY,	REL_WAR,		REL_HOSTILE,	REL_FRIENDLY,	REL_ALLY},
	};
	
	public static final String[] REL_DESCS={
		"NEUTRAL","WAR","HOSTILE","FRIENDLY","ALLY"
	};
	public static final String[] REL_STATES={
		"NEUTRALITY TOWARDS",
		"WAR WITH",
		"HOSTILITIES WITH",
		"FRIENDSHIP WITH",
		"AN ALLIANCE WITH"
	};
	
	public static final int TROPHY_CONTROL=1;
	public static final int TROPHY_EXP=2;
	public static final int TROPHY_AREA=4;
    public static final int TROPHY_PK=8;
	public static final String TROPHY_DESCS_SHORT[]={"","CP","EXP","","AREA","","","","PK"};
	public static final String TROPHY_DESCS[]={"","Most control points","Most clan experience","","Most controlled areas","","","","Most rival player-kills"};
	
	public static final int GVT_DICTATORSHIP=0;
	public static final int GVT_OLIGARCHY=1;
	public static final int GVT_REPUBLIC=2;
	public static final int GVT_DEMOCRACY=3;
	public static final String[] GVT_DESCS={
		"CLAN",
		"GUILD",
		"UNION",
		"FELLOWSHIP"
	};
	
	public static final int[] topRanks={
		POS_BOSS,
		POS_BOSS,
		POS_BOSS,
		POS_LEADER
	};
	
	
	public static final String[][] ROL_DESCS={
		{"APPLICANT","MEMBER","STAFF","ENCHANTER","TREASURER","LEADER","BOSS"},
		{"APPLICANT","MEMBER","CHIEF","ENCHANTER","TREASURER","SECRETARY","GUILDMASTER"},
		{"APPLICANT","CITIZEN","SHERIFF","ENCHANTER","TREASURER","SECRETARY","SENATOR"},
		{"APPLICANT","CITIZEN","SOLDIER","ENCHANTER","TREASURER","MANAGER","FIRST CITIZEN"}
	};
	public static final int maxint=Integer.MAX_VALUE;
	public static final int[][] ROL_MAX={
		{maxint,maxint,maxint,1,1,maxint,1},
		{maxint,maxint,maxint,1,1,maxint,5},
		{maxint,maxint,maxint,1,1,1,5},
		{maxint,maxint,maxint,maxint,maxint,maxint,1}
	};
	
	public static final int TYPE_CLAN=1;

	public static final int FUNC_CLANACCEPT=0;
	public static final int FUNC_CLANASSIGN=1;
	public static final int FUNC_CLANEXILE=2;
	public static final int FUNC_CLANHOMESET=3;
	public static final int FUNC_CLANDONATESET=4;
	public static final int FUNC_CLANREJECT=5;
	public static final int FUNC_CLANPREMISE=6;
	public static final int FUNC_CLANPROPERTYOWNER=7;
	public static final int FUNC_CLANWITHDRAW=8;
	public static final int FUNC_CLANCANORDERUNDERLINGS=9;
	public static final int FUNC_CLANCANORDERCONQUERED=10;
	public static final int FUNC_CLANVOTEASSIGN=11;
	public static final int FUNC_CLANVOTEOTHER=12;
	public static final int FUNC_CLANDEPOSITLIST=13;
	public static final int FUNC_CLANDECLARE=14;
	public static final int FUNC_CLANTAX=15;
	public static final int FUNC_CLANENCHANT=16;
	
}