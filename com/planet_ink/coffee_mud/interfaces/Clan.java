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
public interface Clan 
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

  /** Check if MOB is a member or not */
  public boolean isMember(MOB mob);
  /** Adds MOB.  True to be returned if success */
  public boolean addMember(MOB newMember);
  /** Adds MOB with a starting position.  True is success */
  public boolean addMember(MOB newMember, int newPos);
  /** Validate MOB based on Clan settings */
  public int okMember(MOB newMember);
  /** Removes MOB.  True to be returned if success */
  public boolean removeMember(MOB dropMember);
  public MOB fetchMember(int x);
  /** Replaces PlaceHolder MOB with actual.
    * The problem was, to have almost anything work correctly
    * while players are offline, we have to have MOBs that just
    * take up space with the same name.  This function swaps out
    * the fake mob with the player's mob.
    */
  public void loginMember(MOB login);

  public void setPosition(MOB member, int newPos);
  public void setPosition(MOB member, int newPos, boolean quiet);
  public int getPosition(MOB member);
  public void stripPositions(MOB member);
  /** Calculates average alignment of this Clan */
  public int getAlign();
  public int getSize();

  public String getName();
  public String ID();
  public void setName(String newName);

  /** Retrieves this Clan's basic story. 
    * This is to make the Clan's more RP based and so we can
    * provide up-to-date information on Clans on the web server.
    */
  public String getPremise();
  /** Sets this Clan's basic story.  See {@link getPremise} for more info. */
  public void setPremise(String newPremise);
  public int getTypeBitmap();
  public void setTypeBitmap(int newVal);

  /**
    * Retrieves this Clan's settings bitmap.  This will hold options that
    * Clan Leaders can configure for their Clan.
    */
  public int getSettingsBitmap();
  public void setSettingsBitmap(int newVal);

  /** Retrieves this Clans's Recall room.
    * This is so we can let people teleport to their clan home.
    */
  public Room fetchRecall();
  public void setRecall(Room newRecall);

  /**
    * Retrieves this Clans's Recall room.
    * This is so we can let Clanned users donate gear to their clan
    */
  public Room fetchDonationRoom();
  public void setDonationRoom(Room newDonation);
  
  public void update();
}