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
import java.util.Map;

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
*/
/**
 * The Law interface defines an object containing the various
 * infractions that are recognized for a given LegalObject, the
 * officials that enforce the infractions, some guidelines on
 * their behavior, and information their processes of enforcement.
 *
 * @see com.planet_ink.coffee_mud.Behaviors.interfaces.LegalBehavior
 * @see com.planet_ink.coffee_mud.Common.interfaces.LegalWarrant
 */
public interface Law extends CMCommon
{
	/**
	 * Initializes a new Law object with information from a given
	 * Properties file, along with the LegalBehavior which will
	 * enforce the laws, and flags denoting how maleable these
	 * laws are.  Principally calls resetLaw.
	 *
	 * @see com.planet_ink.coffee_mud.Behaviors.interfaces.LegalBehavior
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#resetLaw()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#hasModifiableNames()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#hasModifiableLaws()
	 *
	 * @param details the behavior governing this law
	 * @param laws the properties file containing all the legal definitions
	 * @param modifiableNames whether officials (judges/officers) are modifiable
	 * @param modifiableLaws whether the laws themselves are modifiable
	 */
	public void initialize(LegalBehavior details,
						   Properties laws,
						   boolean modifiableNames,
						   boolean modifiableLaws);

	/**
	 * Changes the action state of the given warrant
	 * (and all dependent warrants)
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.LegalWarrant
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#PUNISHMENT_DESCS
	 *
	 * @param W the warrant to change the state of
	 * @param state the new action state
	 */
	public void changeStates(LegalWarrant W, int state);

	/**
	 * Forces the legal definitions to be re-read from their primary
	 * storage, usually an INI file, or the database, depending on
	 * how the legalbehavior is defined.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#initialize(LegalBehavior, Properties, boolean, boolean)
	 */
	public void resetLaw();

	/**
	 * Returns whether this legal system allows mobs to be arrested
	 * (as opposed to just players)
	 *
	 * @return true if mobs can be arrested, false otherwise
	 */
	public boolean arrestMobs();

	/**
	 * Whether the officials can be changed (officers judges)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#initialize(LegalBehavior, Properties, boolean, boolean)
	 *
	 * @return true if the officials can be changed, false otherwise
	 */
	public boolean hasModifiableNames();

	/**
	 * Whether the legal definitions can be changed
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#initialize(LegalBehavior, Properties, boolean, boolean)
	 *
	 * @return true if the legal definitions can be changed, false otherwise
	 */
	public boolean hasModifiableLaws();

	/**
	 * Returns one of the raw property entries used to construct
	 * the legal definitions.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#initialize(LegalBehavior, Properties, boolean, boolean)
	 *
	 * @param msg the name of the raw property to return
	 * @return the value of the raw legal property
	 */
	public String getInternalStr(String msg);

	/**
	 * Returns whether or not the given property name is one of the
	 * raw property entries used to construct the legal definitions.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#initialize(LegalBehavior, Properties, boolean, boolean)
	 *
	 * @param msg the name of the raw property to look for
	 * @return true if the property is found, false otherwise
	 */
	public boolean isInternalStr(String msg);

	/**
	 * Sets one of the raw property entries used to construct
	 * the legal definitions.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#initialize(LegalBehavior, Properties, boolean, boolean)
	 *
	 * @param tag the name of the raw property to set
	 * @param value the new value of the property
	 */
	public void setInternalStr(String tag, String value);

	/**
	 * Returns the entire raw legal definition as a ~ delimited string.
	 * @return the entire raw legal definition as a ~ delimited string.
	 */
	public String rawLawString();

	/**
	 * Whether the legal system on the legal behavior is active.
	 * @return true if the law is active, false otherwise
	 */
	public boolean lawIsActivated();

	/**
	 * Returns a warrant if the given mob or player mob object
	 * represents someone accused of killing an officer.
	 *
	 * @see com.planet_ink.coffee_mud.Behaviors.interfaces.LegalBehavior
	 *
	 * @param A the Legal Area governed by the Legal Behavior
	 * @param behav the legal behavior governing the law
	 * @param mob the mob or player to inspect
	 * @return a legal warrant if the mob is a copkiller, null otherwise
	 */
	public LegalWarrant getCopkiller(Area A, LegalBehavior behav, MOB mob);

	/**
	 * Returns a warrant if the given mob or player mob object
	 * represents someone accused of resisting arrest.
	 *
	 * @see com.planet_ink.coffee_mud.Behaviors.interfaces.LegalBehavior
	 *
	 * @param A the Legal Area governed by the Legal Behavior
	 * @param behav the legal behavior governing the law
	 * @param mob the mob or player to inspect
	 * @return a legal warrant if the mob is a law resister, null otherwise
	 */
	public LegalWarrant getLawResister(Area A, LegalBehavior behav, MOB mob);

	/**
	 * Called by an Area periodically to update its records on property
	 * taxes owed, to withdraw money from accounts to pay said taxes if
	 * applicable, and issue a warrant if necessary.
	 *
	 * @param A the Legal Area governed by this law for property taxes
	 * @param debugging whether internal debugging msgs should be generated
	 */
	public void propertyTaxTick(Area A, boolean debugging);

	/**
	 * If defined and found, this method returns an Environmental
	 * array with two elements.  The first element (0) is a Room
	 * object denoting the place where taxes are stored, and the
	 * second element (1) is a container in that room for the
	 * taxes.  If the second element is not found or defined,
	 * the taxes are dropped on the floor.
	 *
	 * @param A the legal Area to look for a treasury in.
	 * @return the two dimensional array of objects (or nulls)
	 */
	public TreasurySet getTreasuryNSafe(Area A);

	/**
	 * Combined with otherBits, this method returns the
	 * definition of "miscellaneous" crimes involving emotes
	 * and similar random phenomenon.  This method in particular
	 * returns a list of String words and phrases which, when encountered
	 * in a player or mobs activity, denote the commission of an
	 * "other" crime.  This Vectors entries match one for one with
	 * the list returned by otherBits()
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#otherBits()
	 *
	 * @return a list of words and phrases
	 */
	public List<List<String>> otherCrimes();

	/**
	 * Combined with otherCrimes, this method returns the
	 * definition of "miscellaneous" crimes involving emotes
	 * and similar random phenomenon.  This method in particular
	 * returns a list of String[] array objects definitioning
	 * the various limitations, flags, and consequences of committing
	 * each "other" crime.  This Vectors entries match one for one with
	 * the list returned by otherBits()
	 *
	 * The entries in each String[] array are indexed by the
	 * constants BIT_*
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#BIT_CRIMENAME
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#otherBits()
	 *
	 * @return a list of String[] array bits of other crime info
	 */
	public List<String[]> otherBits();

	/**
	 * Combined with bannedBits, this method returns the
	 * definition of "illegal substance carrying" crimes.
	 * This method in particular returns a list of raw
	 * resource names or item names which, when encountered
	 * in a player or mobs activity, denote the commission of an
	 * "substance" crime.  This Vectors entries match one for one with
	 * the list returned by bannedBits()
	 *
	 * @see com.planet_ink.coffee_mud.Items.interfaces.RawMaterial
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#bannedBits()
	 *
	 * @return a list of item or resource names
	 */
	public List<List<String>> bannedSubstances();

	/**
	 * Combined with bannedSubstances, this method returns the
	 * definition of "substance" crimes involving manipulating
	 * an illegal substance in public.  This method in particular
	 * returns a list of String[] array objects definitioning
	 * the various limitations, flags, and consequences of committing
	 * each "substance" crime.  This Vectors entries match one for one with
	 * the list returned by bannedSubstances()
	 *
	 * The entries in each String[] array are indexed by the
	 * constants BIT_*
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#BIT_CRIMENAME
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#bannedSubstances()
	 *
	 * @return a list of String[] array bits of substance crime info
	 */
	public List<String[]> bannedBits();

	/**
	 * Method for accessing the crimes, flags, and consequences
	 * involving the use of spells, chants, skills, etc.  The
	 * returned map is indexed by the Ability ID of the
	 * potentially banned skill.  The associated hashed element
	 * is a String[] array of various flags and information about
	 * the consequences of the act.
	 *
	 * The entries in each String[] array are indexed by the
	 * constants BIT_*
	 *
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#BIT_CRIMENAME
	 *
	 * @return a map of String[] array bits of ability crime info
	 */
	public java.util.Map<String,String[]> abilityCrimes();

	/**
	 * Method for accessing the crimes, flags, and consequences
	 * defined as the most basic law.  The returned map is
	 * indexed by the basic crimes ID, and includes NUDITY, ARMED,
	 * TRESPASSING, MURDER, ASSAULT, RESISTINGARREST, and PROPERTYROB.
	 * The associated hashed element is a String[] array of various
	 * flags and information about the consequences of the act.
	 *
	 * The entries in each String[] array are indexed by the
	 * constants BIT_*
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#BIT_CRIMENAME
	 *
	 * @return a map of String[] array bits of basic crime info
	 */
	public Map<String,String[]> basicCrimes();

	/**
	 * Returns a map of various catch-all properties and variables
	 * associated with the tax laws.  The map keys are all string
	 * IDs denoting the property, while the associated element is an object
	 * whose type differs by key.
	 * TAXEVASION - a String[] array index by the constant BIT_*
	 * PROPERTYTAX - a String representing the property tax rate
	 * CITTAX - a String representing the citizen tax rate
	 * SALESTAX - a String representing the sales tax rate
	 * TREASURY - a String of semicolon delimited info about treasury room/
	 *  		- safe room.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#BIT_CRIMENAME
	 *
	 * @return a map of tax law related property information
	 */
	public Map<String, Object> taxLaws();

	/**
	 * A list of strings denoting random things an officer will
	 * say while taking an arrested criminal to the judge.
	 *
	 * @return a list of cute sayings.
	 */
	public List<String> chitChat();

	/**
	 * A list of strings denoting random things an officer will
	 * say while taking an arrested criminal to the jail.
	 *
	 * @return a list of cute sayings.
	 */
	public List<String> chitChat2();

	/**
	 * A list of strings denoting random things an officer will
	 * say while taking an arrested criminal to the detention center.
	 *
	 * @return a list of cute sayings.
	 */
	public List<String> chitChat3();

	/**
	 * A list of strings denoting which rooms are considered jails.
	 * They better have a locked door somewhere!
	 *
	 * @return a list of strings denoting jail rooms
	 */
	public List<String> jailRooms();

	/**
	 * A list of strings denoting which rooms are considered release
	 * rooms for after a prisoner has served jail time.
	 *
	 * @return a list of strings denoting release rooms
	 */
	public List<String> releaseRooms();

	/**
	 * A list a strings denoting which mobs are considered officers
	 * of the law in the legal area.
	 *
	 * @return a list of strings denoting the names of officers
	 */
	public List<String> officerNames();

	/**
	 * A list a strings denoting which mobs are considered judges
	 * of the law in the legal area.
	 *
	 * @return a list of strings denoting the names of judges
	 */
	public List<String> judgeNames();

	/**
	 * Returns a list of all old LegalWarrant objects for all
	 * criminals and crimes since the last MUD reboot.  These are
	 * kept track of so the punishments can be escalated properly.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.LegalWarrant
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#getOldWarrant(MOB, String, boolean)
	 *
	 * @return a list of old warrant objects
	 */
	public List<LegalWarrant> oldWarrants();

	/**
	 * Returns an old warrant object matching the given criteria.
	 * Old warrants are kept track of so the punishments can be
	 * escalated.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.LegalWarrant
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#oldWarrants()
	 *
	 * @param criminal the old criminal
	 * @param crime the old crime ID (from taxlaw, basiclaw, or other)
	 * @param pull true to remove the old warrant from the list, false no
	 * @return the old legal warrant, or null
	 */
	public LegalWarrant getOldWarrant(MOB criminal, String crime, boolean pull);

	/**
	 * Returns a list of all current LegalWarrant objects still considered
	 * to be active and relevant.  Officers can act on these.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.LegalWarrant
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#getWarrant(MOB, int)
	 *
	 * @return a list of legal warrants
	 */
	public List<LegalWarrant> warrants();

	/**
	 * Returns a iterated LegalWarrant object for the given mob.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.LegalWarrant
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#warrants()
	 *
	 * @param mob the mob to get a warrant for
	 * @param which the iteration number (from 0)
	 * @return the LegalWarrant object to use, or NULL if last was returned
	 */
	public LegalWarrant getWarrant(MOB mob, int which);

	/**
	 * A String array of various messages given by officers and or
	 * judges during various stages of the legal adjudication
	 * process.  These are indexed by the MSG_* constants.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#getMessage(int)
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#MSG_COPKILLER
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#MSG_TOTAL
	 *
	 * @return a string array of important things said by the officers
	 */
	public String[] messages();

	/**
	 * Returns a string of one of the messages given by officers and or
	 * judges during various stages of the legal adjudication
	 * process.  These are indexed by the MSG_* constants.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#messages()
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#MSG_COPKILLER
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#MSG_TOTAL
	 *
	 * @param which a number, as indexed by the MSG_* constants
	 * @return a string of an important thing said by the officers
	 */
	public String getMessage(int which);

	/**
	 * Returns a 4-dimensional String[] array for each of the
	 * 4 levels of Parole.  Each string is a message given by
	 * the judge detailing the punishment.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#PUNISHMENT_PAROLE1
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#PUNISHMENT_PAROLE2
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#PUNISHMENT_PAROLE3
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#PUNISHMENT_PAROLE4
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#paroleMessages(int)
	 *
	 * @return a 4-dimensional String[] array for each of the 4 levels of Parole
	 */
	public String[] paroleMessages();

	/**
	 * Returns one of the 4 messages given by the judge for each
	 * of the four parole punishments.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#paroleMessages()
	 *
	 * @param which which of the 4 messages to return (0-3)
	 * @return the message given by the judge
	 */
	public String paroleMessages(int which);

	/**
	 * A parole time is a number of ticks for each of the four levels
	 * of parole punishments.
	 * This method returns an Integer array of all four times.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#PUNISHMENT_PAROLE1
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#PUNISHMENT_PAROLE2
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#PUNISHMENT_PAROLE3
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#PUNISHMENT_PAROLE4
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#paroleTimes(int)
	 *
	 * @return the Integer array of the four parole punishment times.
	 */
	public Integer[] paroleTimes();

	/**
	 * A parole time is a number of ticks for each of the four levels
	 * of parole punishments.
	 * This method returns the appropriate parol time for the given
	 * number.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#paroleTimes()
	 *
	 * @param which which of the four to return (0-3)
	 * @return the number of ticks the punishment perscribes
	 */
	public int paroleTimes(int which);

	/**
	 * Returns a 4-dimensional String[] array for each of the
	 * 4 levels of jail.  Each string is a message given by
	 * the judge detailing the punishment.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#PUNISHMENT_JAIL1
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#PUNISHMENT_JAIL2
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#PUNISHMENT_JAIL3
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#PUNISHMENT_JAIL4
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#jailMessages(int)
	 *
	 * @return a 4-dimensional String[] array for each of the 4 levels of jail
	 */
	public String[] jailMessages();

	/**
	 * Returns one of the 4 messages given by the judge for each
	 * of the four jail punishments.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#jailMessages()
	 *
	 * @param which which of the 4 messages to return (0-3)
	 * @return the message given by the judge
	 */
	public String jailMessages(int which);

	/**
	 * A parole time is a number of ticks for each of the four levels
	 * of jail punishments.
	 * This method returns an Integer array of all four times.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#PUNISHMENT_JAIL1
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#PUNISHMENT_JAIL2
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#PUNISHMENT_JAIL3
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#PUNISHMENT_JAIL4
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#jailTimes(int)
	 *
	 * @return the Integer array of the four parole punishment times.
	 */
	public Integer[] jailTimes();

	/**
	 * A parole time is a number of ticks for each of the four levels
	 * of jail punishments.
	 * This method returns the appropriate jail time for the given
	 * number.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Law#jailTimes()
	 *
	 * @param which which of the four to return (0-3)
	 * @return the number of ticks the punishment perscribes
	 */
	public int jailTimes(int which);

	/**
	 * For the getTreasuryNSafe, this class stores
	 * the location of the treasury, for taxing purposes.
	 * @author Bo Zimmermanimmerman
	 *
	 */
	public class TreasurySet
	{
		public Room room;
		public Container container;
		public TreasurySet(Room R, Container C){ room=R; container=C;}
	}

	/** A base punishment code meaning the officer warns the criminal */
	public static final int PUNISHMENT_WARN=0;
	/** A base punishment code meaning the officer threatens the criminal */
	public static final int PUNISHMENT_THREATEN=1;
	/** A base punishment code meaning the judge paroles the criminal */
	public static final int PUNISHMENT_PAROLE1=2;
	/** A base punishment code meaning the judge paroles the criminal */
	public static final int PUNISHMENT_PAROLE2=3;
	/** A base punishment code meaning the judge paroles the criminal */
	public static final int PUNISHMENT_PAROLE3=4;
	/** A base punishment code meaning the judge paroles the criminal */
	public static final int PUNISHMENT_PAROLE4=5;
	/** A base punishment code meaning the judge jails the criminal */
	public static final int PUNISHMENT_JAIL1=6;
	/** A base punishment code meaning the judge jails the criminal */
	public static final int PUNISHMENT_JAIL2=7;
	/** A base punishment code meaning the judge jails the criminal */
	public static final int PUNISHMENT_JAIL3=8;
	/** A base punishment code meaning the judge jails the criminal */
	public static final int PUNISHMENT_JAIL4=9;
	/** A base punishment code meaning the judge executes the criminal */
	public static final int PUNISHMENT_EXECUTE=10;
	/** A helper punishment code denoting the highest code */
	public static final int PUNISHMENT_HIGHEST=10;
	/** A mask denoting which bits in an are reserved for the base punishment */
	public static final int PUNISHMENT_MASK=255;

	/** An array of code words for each of the base punishment types */
	public static final String[] PUNISHMENT_DESCS={
		"WARNING",
		"THREAT",
		"PAROLE1",
		"PAROLE2",
		"PAROLE3",
		"PAROLE4",
		"JAIL1",
		"JAIL2",
		"JAIL3",
		"JAIL4",
		"DEATH",
	};

	/** a bitmask, added to a base punishment, denoting this crime be separated from others */
	public static final int PUNISHMENTMASK_SEPARATE=256;
	/** a bitmask, added to a base punishment type, denoting that the officer enforces */
	public static final int PUNISHMENTMASK_SKIPTRIAL=512;
	/** a bitmask, added to a base punishment type, denoting automatic detension */
	public static final int PUNISHMENTMASK_DETAIN=1024;
	/** a bitmask, added to a base punishment type, denoting a fine */
	public static final int PUNISHMENTMASK_FINE=2048;
	/** a bitmask, added to a base punishment type, denoting criminal not walked to releaseroom */
	public static final int PUNISHMENTMASK_NORELEASE=4096;
	/** a bitmask, added to a base punishment type, denoting maximum adjusted punishment */
	public static final int PUNISHMENTMASK_PUNISHCAP=8192;
	/** a string array for each of the punishment bitmasks */
	public static final String[] PUNISHMENTMASK_DESCS={
		"SEPARATE",
		"SKIPTRIAL",
		"DETAIN=",
		"FINE=",
		"NORELEASE",
		"PUNISHCAP="
	};
	/** an array of the various bitmask values added to punishment codes */
	public static final int[] PUNISHMENTMASK_CODES={
		PUNISHMENTMASK_SEPARATE,
		PUNISHMENTMASK_SKIPTRIAL,
		PUNISHMENTMASK_DETAIN,
		PUNISHMENTMASK_FINE,
		PUNISHMENTMASK_NORELEASE,
		PUNISHMENTMASK_PUNISHCAP
	};

	/** a state of adjudication meaning officer has not yet found the criminal */
	public static final int STATE_SEEKING=0;
	/** a state of adjudication meaning officer is arresting the criminal */
	public static final int STATE_ARRESTING=1;
	/** a state of adjudication meaning officer is subdueing the criminal */
	public static final int STATE_SUBDUEING=2;
	/** a state of adjudication meaning officer is taking the criminal to trial */
	public static final int STATE_MOVING=3;
	/** a state of adjudication meaning officer is reporting charges to the judge */
	public static final int STATE_REPORTING=4;
	/** a state of adjudication meaning officer is waiting for a judgement from judge */
	public static final int STATE_WAITING=5;
	/** a state of adjudication meaning judge is parolling the criminal */
	public static final int STATE_PAROLING=6;
	/** a state of adjudication meaning judge is sentencing the criminal to jail */
	public static final int STATE_JAILING=7;
	/** a state of adjudication meaning judge is executing the criminal */
	public static final int STATE_EXECUTING=8;
	/** a state of adjudication meaning officer is taking the criminal to jail */
	public static final int STATE_MOVING2=9;
	/** a state of adjudication meaning officer is to release the criminal from jail */
	public static final int STATE_RELEASE=10;
	/** a state of adjudication meaning officer is releasing the criminal from jail */
	public static final int STATE_MOVING3=11;
	/** a state of adjudication meaning officer is taking the criminal to detension */
	public static final int STATE_DETAINING=12;

	/** an index to crime-definition flags denoting types of places the law applies to */
	public static final int BIT_CRIMELOCS=0;
	/** an index to crime-definition flags denoting circumstances the law applies to */
	public static final int BIT_CRIMEFLAGS=1;
	/** an index to crime-definition flags denoting name of the crime */
	public static final int BIT_CRIMENAME=2;
	/** an index to crime-definition flags denoting the punishment type and flags */
	public static final int BIT_SENTENCE=3;
	/** an index to crime-definition flags denoting the warning message for breaking the law */
	public static final int BIT_WARNMSG=4;
	/** an index to crime-definition flags denoting the number of parts of a crime definition */
	public static final int BIT_NUMBITS=5;

	/** an index to messages said to criminals by officers, denotes a previous offence */
	public final static int MSG_PREVOFF=0;
	/** an index to messages said to criminals by officers, denotes a warning is forthcoming */
	public final static int MSG_WARNING=1;
	/** an index to messages said to criminals by officers, denotes a threat is forthcoming */
	public final static int MSG_THREAT=2;
	/** an index to messages said to criminals by officers, denotes an execution is forthcoming */
	public final static int MSG_EXECUTE=3;
	/** an index to messages said to criminals by officers, denotes the criminal is a protected citizen */
	public final static int MSG_PROTECTEDMASK=4;
	/** an index to messages said to criminals by officers, denotes the criminal is a trespassor */
	public final static int MSG_TRESPASSERMASK=5;
	/** an index to messages said to criminals by officers, denotes resisting arrest */
	public final static int MSG_RESISTFIGHT=6;
	/** an index to messages said to criminals by officers, denotes an order not to resist */
	public final static int MSG_NORESIST=7;
	/** an index to messages said to criminals by officers, denotes a warning not to resist */
	public final static int MSG_RESISTWARN=8;
	/** an index to messages said to criminals by officers, denotes a parole sentence is ready */
	public final static int MSG_PAROLEDISMISS=9;
	/** an index to messages said to criminals by officers, denotes a jail sentence is done */
	public final static int MSG_LAWFREE=10;
	/** an index to messages said to criminals by officers, denotes a  previous law resister */
	public final static int MSG_RESIST=11;
	/** an index to messages said to criminals by officers, denotes a previous cop killer */
	public final static int MSG_COPKILLER=12;
	/** the number of messages said to criminals by officers*/
	public final static int MSG_TOTAL=13;

	/** a default law definition, suitable for reading into a Properties object */
	public static final String defaultLaw=
		"OFFICERS=@\n"+
		"JUDGE=@\n"+
		"JAIL=@\n"+
		"RELEASEROOM=@\n"+
		"WARNINGMSG="+CMLib.lang().L("Your behavior is unacceptable.  Do not repeat this offense.  You may go.")+"\n"+
		"THREATMSG="+CMLib.lang().L("That behavior is NOT tolerated here.  Keep your nose clean, or next time I may not be so lenient.  You may go.")+"\n"+
		"JAIL1MSG="+CMLib.lang().L("You are hereby sentenced to minimum jail time.  Take away the prisoner!")+"\n"+
		"JAIL2MSG="+CMLib.lang().L("You are hereby sentenced to jail time.  Take away the prisoner!")+"\n"+
		"JAIL3MSG="+CMLib.lang().L("You are hereby sentenced to hard jail time.  Take away the prisoner!")+"\n"+
		"JAIL4MSG="+CMLib.lang().L("You are hereby sentenced to rot in jail.  Take away the prisoner!")+"\n"+
		"PAROLE1MSG="+CMLib.lang().L("You are hereby sentenced to a short period under the prisoner's geas. Perhaps that will make you think!")+"\n"+
		"PAROLE2MSG="+CMLib.lang().L("You are hereby sentenced to a period under the prisoner's geas. That will teach you, I think.")+"\n"+
		"PAROLE3MSG="+CMLib.lang().L("You are hereby sentenced to hard time under the prisoner's geas! That will teach you!")+"\n"+
		"PAROLE4MSG="+CMLib.lang().L("You are hereby sentenced to rot under the prisoner's geas!  Don't let me see you again!")+"\n"+
		"PAROLEDISMISS="+CMLib.lang().L("Now, get out of my sight!")+"\n"+
		"PREVOFFMSG="+CMLib.lang().L("You have been warned about this behavior before.")+"\n"+
		"EXECUTEMSG="+CMLib.lang().L("You are hereby sentenced to a brutal death.  Sentence to be carried out IMMEDIATELY!")+"\n"+
		"LAWFREE="+CMLib.lang().L("You are free to go.")+"\n"+
		"CHITCHAT="+CMLib.lang().L("\"You didn't really think you could get away with it did you?\" \"You are REALLY in for it!\" \"Convicts like you are a dime a dozen.\" \"MAKE WAY! DEAD MAN WALKING!\" \"You are gonna GET it.\" \"I love my job.\"")+"\n"+
		"CHITCHAT2="+CMLib.lang().L("\"You didn't really think you would get away with it did you?\" \"I hope you aren't claustrophobic!\" \"Remember not to drop your soap in there.\" \"MAKE WAY! DEAD MAN WALKING!\" \"I recommend you hold your breath while you're in there -- I always do.  It stinks!\" \"Putting away scum like you makes it all worthwhile\"")+"\n"+
		"CHITCHAT3="+CMLib.lang().L("\"This is for your own good, so please don't resist.\" \"You understand that your detention is mandatory I hope.\" \"Just doing my job.\" \"Been a nice day, don't ya think?\" \"We're almost there, don't worry.\" \"Not much farther now.\"")+"\n"+
		"RESISTWARNMSG="+CMLib.lang().L("I said SIT DOWN! NOW!")+"\n"+
		"NORESISTMSG="+CMLib.lang().L("Good.  Now hold still.")+"\n"+
		"ACTIVATED=FALSE\n"+
		"RESISTFIGHTMSG="+CMLib.lang().L("Resisting arrest?! How DARE you!")+"\n"+
		"COPKILLERMSG="+CMLib.lang().L("COP-KILLER!!!!! AAARRRGGGHHHH!!!!!")+"\n"+
		"RESISTMSG="+CMLib.lang().L("Resisting arrest eh?  Well, have it your way.")+"\n"+
		"PROTECTED=+ADJINT 3\n"+
		"ARRESTMOBS=true\n"+
		"TRESPASSERS=-Race +Undead\n"+
		"PAROLE1TIME=40\n"+
		"PAROLE2TIME=80\n"+
		"PAROLE3TIME=160\n"+
		"PAROLE4TIME=320\n"+
		"JAIL1TIME=20\n"+
		"JAIL2TIME=40\n"+
		"JAIL3TIME=80\n"+
		"JAIL4TIME=160\n"+
		"RESISTINGARREST=;;"+CMLib.lang().L("resisting arrest")+";jail1;"+CMLib.lang().L("Resisting arrest by a lawful officer is a serious crime.")+"\n"+
		"TRESPASSING=!home !indoors;!recently;"+CMLib.lang().L("trespassing")+";jail3;"+CMLib.lang().L("Your kind are not allowed here.")+"\n"+
		"NUDITY=!home !indoors;witness !recently;"+CMLib.lang().L("indecent exposure")+";warning punishcap=parole4;"+CMLib.lang().L("Nudity below the waist violates our high moral code.  Use the 'outfit' command if you need clothes!")+"\n"+
		"ARMED=\n"+
		"ASSAULT=;;"+CMLib.lang().L("assaulting <T-NAME>")+";jail4;"+CMLib.lang().L("Assault is a hideous offense.")+"\n"+
		"MURDER=;;"+CMLib.lang().L("murdering <T-NAME>")+";death;"+CMLib.lang().L("Murder is a barbarous offense.")+"\n"+
		"PROPERTYROB=;;"+CMLib.lang().L("robbing the property of <T-NAME>")+";jail3;"+CMLib.lang().L("Robbery violates our high moral code.")+"\n"+
		"TAXEVASION=;;"+CMLib.lang().L("evading taxes")+";jail1;"+CMLib.lang().L("Paying taxes and dieing are our solemn duties to the state.")+"\n"+
		"TREASURY=\n"+
		"PROPERTYTAX=0\n"+
		"SALESTAX=0\n"+
		"CITTAX=10\n"+
		"THIEF_SWIPE=;;"+CMLib.lang().L("robbing <T-NAME>")+";jail2;"+CMLib.lang().L("Swiping violates our high moral code.")+"\n"+
		"THIEF_STEAL=;;"+CMLib.lang().L("robbing <T-NAME>")+";jail3;"+CMLib.lang().L("Stealing violates our high moral code.")+"\n"+
		"THIEF_TRAP=!home;;"+CMLib.lang().L("setting traps in city limits")+";jail3;"+CMLib.lang().L("Trapping puts us all in mortal danger.")+"\n"+
		"THIEF_BRIBE=;;"+CMLib.lang().L("bribing <T-NAME>")+";jail2;"+CMLib.lang().L("Bribing is a violation of our moral code.")+"\n"+
		"THIEF_CON=;;"+CMLib.lang().L("conning <T-NAME>")+";jail2;"+CMLib.lang().L("Conning and deception is a violation of our moral code.")+"\n"+
		"THIEF_EMBEZZLE=;;"+CMLib.lang().L("embezzling <T-NAME>")+";jail4;"+CMLib.lang().L("Embezzling money is a form of vicious theft!")+"\n"+
		"THIEF_CONTRACTHIT=;;"+CMLib.lang().L("taking out contract on <T-NAME>'s life")+";death;"+CMLib.lang().L("Murder by contract is a barbarous offense.")+"\n"+
		"THIEF_DEATHTRAP=;;"+CMLib.lang().L("setting a death trap")+";death;"+CMLib.lang().L("Murder by trapping is a barbarous offense.")+"\n"+
		"THIEF_FORGERY=;;"+CMLib.lang().L("forgery")+";jail2;"+CMLib.lang().L("Forgery is deceptive and quite illegal.")+"\n"+
		"THIEF_RACKETEER=;;"+CMLib.lang().L("racketeering <T-NAME>")+";jail3;"+CMLib.lang().L("Racketeering is a form of vicious theft.")+"\n"+
		"THIEF_ROBBERY=;;"+CMLib.lang().L("robbing <T-NAME>")+";jail3;"+CMLib.lang().L("Robbery violates our high moral code.")+"\n"+
		"INEBRIATION=!home !pub !tavern !inn !bar;!recently;"+CMLib.lang().L("public intoxication")+";parole1 punishcap=jail4;"+CMLib.lang().L("Drunkenness is a demeaning and intolerable state.")+"\n"+
		"POISON_ALCOHOL=!home !pub !tavern !inn !bar;!recently;"+CMLib.lang().L("public intoxication")+";parole1 punishcap=jail4;"+CMLib.lang().L("Drunkenness is a demeaning and intolerable state.")+"\n"+
		"POISON_FIREBREATHER=!home !pub !tavern !inn !bar;!recently;"+CMLib.lang().L("public intoxication")+";parole1 punishcap=jail4;"+CMLib.lang().L("Drunkenness is a demeaning and intolerable state.")+"\n"+
		"POISON_LIQUOR=!home !pub !tavern !inn !bar;!recently;"+CMLib.lang().L("public intoxication")+";parole1 punishcap=jail4;"+CMLib.lang().L("Drunkenness is a demeaning and intolerable state.")+"\n";
}
