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
public interface Clan extends Cloneable
{

	public static final int POS_APPLICANT=0;
	public static final int POS_STAFF=1;
	public static final int POS_MEMBER=2;
	public static final int POS_TREASURER=4;
	public static final int POS_LEADER=8;
	public static final int POS_BOSS=16;

	public static final int TYPE_CLAN=1;

	public static final int SETTING_RESTRICT_LEVEL=1;
	public static final int SETTING_RESTRICT_RACE=2;
	public static final int SETTING_RESTRICT_CLASS=4;
	public static final int SETTING_RESTRICT_ALIGNMENT=8;
	public static final int SETTING_RESTRICT_GENDER=16;

	public int getAlign();
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

	public String getAcceptanceSettings();
	public void setAcceptanceSettings(String newSettings);

	public String getPolitics();
	public void setPolitics(String politics);

	public String getRecall();
	public void setRecall(String newRecall);

	public String getDonation();
	public void setDonation(String newDonation);
  
	public Vector getMemberList();
	public Vector getMemberList(int PosFilter);

	public void update();
	
	/** return a new instance of the object*/
	public Clan newInstance();
	public Clan copyOf();
}