package com.planet_ink.coffee_mud.Behaviors.interfaces;
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
 * A LegalBehavior is a Behavior that provides functionality related to law and
 * order within a given geographic sphere, which is usually an Area with a
 * LegalBehavior behavior.  A LegalBehavior keeps track of Warrants against
 * players and mobs which persist only in memory.  It also controls the behavior
 * of arresting officers and judges, and dispenses justice by taking mobs and
 * players to jail, putting them on parole, or issuing warnings or other
 * punishments.
 * @see com.planet_ink.coffee_mud.Behaviors.interfaces.Behavior
 * @see com.planet_ink.coffee_mud.Common.interfaces.LegalWarrant
 * @see com.planet_ink.coffee_mud.Common.interfaces.Law
 */
public interface LegalBehavior extends Behavior
{
	/** constant for the number of miliseconds in a real-life day */
	public static final long ONE_REAL_DAY=(long)1000*60*60*24;
	/** constant for the number of miliseconds before a warrant expires */
	public static final long EXPIRATION_MILLIS=ONE_REAL_DAY*7; // 7 real days
	/** constant for the number of miliseconds before an area is under legal control */
	public static final long CONTROLTIME=ONE_REAL_DAY*3;

	/**
	 * Returns whether or not the given legal warrant is still a valid, timely
	 * warrant that can be acted upon by law enforcement.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.LegalWarrant
	 * @param W the legal warrant to inspect
	 * @param debugging whether debug information should be sent to the log
	 * @return whether the given warrant is still a valid, timely crime.
	 */
	public boolean isStillACrime(LegalWarrant W, boolean debugging);

	/**
	 * Inspects the circumstances of, and if necessary, assigns a warrant to
	 * be handled by local law enforcement and judges.  Call this method
	 * if you want to add a warrant for a custom crime with your own qualifying
	 * flags.  For recognized crimes with crime keys, however, use the accuse
	 * command.
	 * @see com.planet_ink.coffee_mud.Behaviors.interfaces.LegalBehavior#accuse(Area, MOB, MOB, String[])
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#PUNISHMENTMASK_DESCS
	 * @param mob the accused character
	 * @param laws the system of laws to use as a basis
	 * @param myArea the geographical domain that the laws apply to
	 * @param target the victim of a crime, if any
	 * @param crimeLocs string of location flags, e.g. !indoors !home keyword !keyword
	 * @param crimeFlags string of crime situation flags, e.g. !recently !combat
	 * @param crime string description of the crime, e.g. robbing T-NAME
	 * @param sentence string sentence action, e.g. warn, parole1, jail1, death
	 * @param warnMsg string the officer will say to explain the seriousness of the crime
	 * @return whether or not the warrant was successfully issued
	 */
	public boolean fillOutWarrant(MOB mob,
								  Law laws,
								  Area myArea,
								  Environmental target,
								  String crimeLocs,
								  String crimeFlags,
								  String crime,
								  String sentence,
								  String warnMsg);

	/**
	 * A method that transfers a warrant out on the accused to a different
	 * framed individual
	 * @param myArea the geographical legal area
	 * @param accused the mob with actual warrants out on him/her
	 * @param framed the person to transfer the first warrant to.
	 * @return whether warrants were actually transferred from the accused to the framed
	 */
	public boolean frame(Area myArea, MOB accused, MOB framed);

	/**
	 * Assigns an officer and begins the automated arresting procedure. The
	 * target criminal must have a warrant out for this to end well for the
	 * state.
	 * @param myArea the geographic legal area
	 * @param officer the mob to assign as the arresting officer
	 * @param mob the mob to arrest
	 * @return whether the arrest began successfully.
	 */
	public boolean arrest(Area myArea, MOB officer, MOB mob);

	/**
	 * Returns the set of laws governing the given geographic legal area,
	 * assuming that this LegalBehavior is the behavior governing the same.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law
	 * @param myArea  the geographic legal area
	 * @return the Law object that governs the area and behavior
	 */
	public Law legalInfo(Area myArea);

	/**
	 * Returns whether the given mob is both an officer of the law, and not
	 * otherwise engaged in an arrest, and so is available to make one
	 * @param myArea the geographic legal area
	 * @param mob the pc/npc to test
	 * @return whether the mob is an officer of the law
	 */
	public boolean isElligibleOfficer(Area myArea, MOB mob);

	/**
	 * Returns whether the given mob has a valid warrant out for his/her arrest.
	 * @param myArea the geographic legal area
	 * @param accused the mob to test
	 * @return whether a warrant is available for the accused
	 */
	public boolean hasWarrant(Area myArea, MOB accused);

	/**
	 * Returns whether the given mob qualifies as an arresting officer of any
	 * sort in the given legal area.
	 * @param myArea the geographic legal area
	 * @param mob the mob to test for officerhood
	 * @return true if the mob is an officer, false otherwise
	 */
	public boolean isAnyOfficer(Area myArea, MOB mob);

	/**
	 * Returns whether the given mob qualifies as the judge in the given legal
	 * area.
	 * @param myArea the geographic legal area
	 * @param mob the mob to test for judgehood
	 * @return true if the mob is a judge, false otherwise
	 */
	public boolean isJudge(Area myArea, MOB mob);

	/**
	 * A method to change the amount of base currency currently
	 * listed as fines owed by the given mob.  A value of 0 erases.
	 * @param d the amount of base currency the mob owes
	 * @param mob the mob who owes money to the state
	 */
	public void modifyAssessedFines(double d, MOB mob);

	/**
	 * Returns the amount of base currency owed by the given mob, if any.
	 * @param mob the mob who might owe money
	 * @return the amount owed, or 0 if none.
	 */
	public double finesOwed(MOB mob);

	/**
	 * This method notifies the legal behavior that its laws have changed
	 * and need to be updated.  Call this method whenever the behaviors/
	 * areas laws have changed.
	 * @param myArea the geographic legal area
	 * @return Whether the update was necessary due to the legal parameters
	 */
	public boolean updateLaw(Area myArea);

	/**
	 * Get the name of the clan that currently rules this area, if applicable.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Clan
	 * @return Empty string if the area is unruled, or ruled by the system. ClanID otherwise.
	 */
	public String rulingOrganization();

	/**
	 * If the legal behavior and area are conquerable by clans or foreign organizations,
	 * this method will return the name of the current controlling clan, and some information
	 * about the state of the conquest, such as control points achieved.
	 * @param myArea the geographic legal area
	 * @return information about the conquest of this area, in readable form.
	 */
	public String conquestInfo(Area myArea);

	/**
	 * Returns whether this legalbehavior governs an area that is presently
	 * legally stable.  Unconquerable areas are always stable, and areas conquered
	 * and controlled for a sufficient amount of time are also stable.
	 * @return Whether order has been restored.
	 */
	public boolean isFullyControlled();

	/**
	 * Returns the number of control points necessary to conquer the area governed
	 * by this legal behavior.  Not applicable if the legal behavior doesn't permit
	 * government changes or conquest.
	 * @see com.planet_ink.coffee_mud.Behaviors.interfaces.LegalBehavior#setControlPoints(String, int)
	 * @see com.planet_ink.coffee_mud.Behaviors.interfaces.LegalBehavior#getControlPoints(String)
	 * @return the number of control points needed to control this legal behavior
	 */
	public int controlPoints();

	/**
	 * The present chance (percent) that the area may collapse into revolt and
	 * remove itself from control.  Not applicable if the legal behavior doesn't permit
	 * government changes or conquest.
	 * @return the percent chance of revolt
	 */
	public int revoltChance();

	/**
	 * Modify the number of control points earned by the given clanID. Not
	 * applicable if the legal behavior doesn't permit government changes or conquest.
	 * @see com.planet_ink.coffee_mud.Behaviors.interfaces.LegalBehavior#controlPoints()
	 * @see com.planet_ink.coffee_mud.Behaviors.interfaces.LegalBehavior#getControlPoints(String)
	 * @param clanID the clan to assign the control points to
	 * @param newControlPoints the number of points to assign
	 */
	public void setControlPoints(String clanID, int newControlPoints);

	/**
	 * Returns the number of control points earned by the given clanID. Not
	 * applicable if the legal behavior doesn't permit government changes or conquest.
	 * @see com.planet_ink.coffee_mud.Behaviors.interfaces.LegalBehavior#controlPoints()
	 * @see com.planet_ink.coffee_mud.Behaviors.interfaces.LegalBehavior#setControlPoints(String, int)
	 * @param clanID the clan to assign the control points to
	 * @return The number of control points earned by this clan/organization
	 */
	public int getControlPoints(String clanID);

	/**
	 * Searches the list of warrants, returning those criminal mobs whose names
	 * match the search string, and still have legal warrants available for them.
	 * Use a search name of NULL to return all criminals.
	 * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB
	 * @param myArea the geographic legal area
	 * @param searchStr the name/search string to use
	 * @return a list of MOB objects
	 */
	public List<MOB> getCriminals(Area myArea, String searchStr);

	/**
	 * Returns a list of all active legal warrants available on the given
	 * mob.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.LegalWarrant
	 * @param myArea the geographic legal area
	 * @param accused the mob to look for warrants for
	 * @return a list of LegalWarrant objects
	 */
	public List<LegalWarrant> getWarrantsOf(Area myArea, MOB accused);

	/**
	 * Puts a warrant on the official docket so that officers can act
	 * on them.  This method is called by other methods to finish off
	 * their work.  Any WARRANTS channels are also notified.
	 * @see com.planet_ink.coffee_mud.Behaviors.interfaces.LegalBehavior#fillOutWarrant(MOB, Law, Area, Environmental, String, String, String, String, String)
	 * @see com.planet_ink.coffee_mud.Behaviors.interfaces.LegalBehavior#addWarrant(Area, MOB, MOB, String, String, String, String, String)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.LegalWarrant
	 * @param myArea the geographic legal area
	 * @param W the LegalWarrant to put on the docket
	 * @return whether the warrant was successfully added
	 */
	public boolean addWarrant(Area myArea, LegalWarrant W);

	/**
	 * Fills out and, if possible, issues a warrant for arrest using the given
	 * crime data.  Calls fillOutWarrant to do its work.
	 * @see com.planet_ink.coffee_mud.Behaviors.interfaces.LegalBehavior#fillOutWarrant(MOB, Law, Area, Environmental, String, String, String, String, String)
	 * @param myArea the geographic legal area
	 * @param accused the accused character
	 * @param victim the victim of a crime, if any
	 * @param crimeLocs string of location flags, e.g. !indoors !home keyword !keyword
	 * @param crimeFlags string of crime situation flags, e.g. !recently !combat
	 * @param crime string description of the crime, e.g. robbing T-NAME
	 * @param sentence string sentence action, e.g. warn, parole1, jail1, death
	 * @param warnMsg string the officer will say to explain the seriousness of the crime
	 * @return whether or not the warrant was successfully issued
	 */
	public boolean addWarrant(Area myArea, MOB accused, MOB victim, String crimeLocs, String crimeFlags, String crime, String sentence, String warnMsg);

	/**
	 * Removes the given warrants from the list of issued warrants.  Does not update
	 * the old-warrants (prior convictions) record, but erases the warrant completely.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.LegalWarrant
	 * @param myArea the geographic legal area
	 * @param W the legal warrant to remove
	 * @return true if the warrant was found to remove, false otherwise
	 */
	public boolean deleteWarrant(Area myArea, LegalWarrant W);

	/**
	 * Removes the first warrant for the given accused criminal, for any one of
	 * the given list of official crime KEYS.  Crime KEYS are the key names of
	 * crimed, such as TAXEVASION.
	 * @param myArea the geographic legal area
	 * @param accused the mob possible accused of one of the crimes
	 * @param acquittableLaws the list of crime keys.
	 * @return whether an acquittable crime was found, and removed
	 */
	public boolean aquit(Area myArea, MOB accused, String[] acquittableLaws);

	/**
	 * Returns whether any of the given Room objects in the jails Vector
	 * is indeed an official Jail room as defined by this legal behavior.
	 * @see com.planet_ink.coffee_mud.Locales.interfaces.Room
	 * @param myArea the geographic legal area
	 * @param jails a list of Room objects to inspect
	 * @return whether any one of the room objects is, in fact, a jail
	 */
	public boolean isJailRoom(Area myArea, List<Room> jails);

	/**
	 * Issues a LegalWarrant against the accused on behalf of the given
	 * victim, for a crime listed in the list of crime keys.
	 * Calls fillOutWarrant after retreiving the remaining information about
	 * the crime key described by the parameter accusableLaws.
	 * @see com.planet_ink.coffee_mud.Behaviors.interfaces.LegalBehavior#fillOutWarrant(MOB, Law, Area, Environmental, String, String, String, String, String)
	 * @param myArea the geographic legal area
	 * @param accused the accused mob
	 * @param victim the victim of the crime
	 * @param accusableLaws a crime key, such as TAXEVASION
	 * @return whether one of the laws was found, and a warrant successfully filled out
	 */
	public boolean accuse(Area myArea, MOB accused, MOB victim, String[] accusableLaws);
	
	/**
	 * If the warrant reflects someone in prison, it releases them.
	 * If the warrant reflects someone being arrested, it releases
	 * the arrest and temporarily ignores the warrant.
	 * 
	 * @param myArea the legal area
	 * @param warrant the warrant to excuse
	 */
	public void release(Area myArea, LegalWarrant warrant);
}
