package com.planet_ink.coffee_mud.interfaces;

import java.util.*;

/**
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
  * @author=Jeremy Vyska
  */
public interface Clan extends Cloneable, Tickable
{

	public static final int POS_APPLICANT=0;
	public static final int POS_MEMBER=1;
	public static final int POS_STAFF=2;
	public static final int POS_TREASURER=4;
	public static final int POS_LEADER=8;
	public static final int POS_BOSS=16;

	public static final int CLANSTATUS_ACTIVE=0;
	public static final int CLANSTATUS_PENDING=1;
	public static final int CLANSTATUS_FADING=2;
	
	public static final int REL_NEUTRAL=0;
	public static final int REL_WAR=1;
	public static final int REL_HOSTILE=2;
	public static final int REL_FRIENDLY=3;
	public static final int REL_ALLY=4;

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
	
	public static final int GVT_DICTATORSHIP=0;
	public static final int GVT_OLIGARCHY=1;
	public static final int GVT_REPUBLIC=2;
	public static final int GVT_DEMOCRACY=3;
	public static final String[] FVT_DESCS={
		"DICTATORSHIP",
		"OLIGARCHY",
		"REPUBLIC",
		"DEMOCRACY"
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
	
	public int allowedToDoThis(MOB mob, int function);
	
	public int getSize();

	public String getName();
	public String ID();
	public void setName(String newName);
	public int getType();
	public String typeName();

	/** Retrieves this Clan's basic story. 
	  * This is to make the Clan's more RP based and so we can
	  * provide up-to-date information on Clans on the web server.
	  */
	public String getPremise();
	/** Sets this Clan's basic story.  See {@link getPremise} for more info. */
	public void setPremise(String newPremise);

	/** Creates the string for the 'clandetail' command */
	public String getDetail(MOB mob);

	public String getAcceptanceSettings();
	public void setAcceptanceSettings(String newSettings);

	public String getPolitics();
	public void setPolitics(String politics);

	public int getStatus();
	public void setStatus(int newStatus);

	public String getRecall();
	public void setRecall(String newRecall);

	public String getDonation();
	public void setDonation(String newDonation);
  
	public Vector getMemberList();
	public Vector getMemberList(int PosFilter);

	public int getClanRelations(String id);
	public void setClanRelations(String id, int rel);
	
	public int getGovernment();
	public void setGovernment(int type);
		
	public int getTopRank();

	public void update();
	
	/** return a new instance of the object*/
	public Clan newInstance();
	public Clan copyOf();
}