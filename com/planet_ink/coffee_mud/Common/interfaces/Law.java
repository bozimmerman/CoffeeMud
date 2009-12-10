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
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
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
    public Environmental[] getTreasuryNSafe(Area A);
    
    /**
     * Combined with otherBits, this method returns the
     * definition of "miscellaneous" crimes involving emotes
     * and similar random phenomenon.  This method in particular
     * returns a Vector of String words and phrases which, when encountered
     * in a player or mobs activity, denote the commission of an
     * "other" crime.  This Vectors entries match one for one with
     * the Vector returned by otherBits()
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Law#otherBits()
     * 
     * @return a Vector of words and phrases
     */
    public Vector otherCrimes();
    
    /**
     * Combined with otherCrimes, this method returns the
     * definition of "miscellaneous" crimes involving emotes
     * and similar random phenomenon.  This method in particular
     * returns a Vector of String[] array objects definitioning
     * the various limitations, flags, and consequences of committing
     * each "other" crime.  This Vectors entries match one for one with
     * the Vector returned by otherBits()
     * 
     * The entries in each String[] array are indexed by the 
     * constants BIT_*
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Law#BIT_CRIMENAME
     * @see com.planet_ink.coffee_mud.Common.interfaces.Law#otherBits()
     * 
     * @return a Vector of String[] array bits of other crime info
     */
    public Vector otherBits();
    
    /**
     * Combined with bannedBits, this method returns the
     * definition of "illegal substance carrying" crimes.  
     * This method in particular returns a Vector of raw
     * resource names or item names which, when encountered
     * in a player or mobs activity, denote the commission of an
     * "substance" crime.  This Vectors entries match one for one with
     * the Vector returned by bannedBits()
     * 
     * @see com.planet_ink.coffee_mud.Items.interfaces.RawMaterial#RESOURCE_DESCS
     * @see com.planet_ink.coffee_mud.Common.interfaces.Law#bannedBits()
     * 
     * @return a Vector of item or resource names
     */
    public Vector bannedSubstances();
    
    /**
     * Combined with bannedSubstances, this method returns the
     * definition of "substance" crimes involving manipulating
     * an illegal substance in public.  This method in particular
     * returns a Vector of String[] array objects definitioning
     * the various limitations, flags, and consequences of committing
     * each "substance" crime.  This Vectors entries match one for one with
     * the Vector returned by bannedSubstances()
     * 
     * The entries in each String[] array are indexed by the 
     * constants BIT_*
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.Law#BIT_CRIMENAME
     * @see com.planet_ink.coffee_mud.Common.interfaces.Law#bannedSubstances()
     * 
     * @return a Vector of String[] array bits of substance crime info
     */
    public Vector bannedBits();
    
    /**
     * Method for accessing the crimes, flags, and consequences 
     * involving the use of spells, chants, skills, etc.  The 
     * returned Hashtable is indexed by the Ability ID of the 
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
     * @return a Hashtable of String[] array bits of ability crime info
     */
    public Hashtable abilityCrimes();
    
    /**
     * Method for accessing the crimes, flags, and consequences
     * defined as the most basic law.  The returned Hashtable is
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
     * @return a Hashtable of String[] array bits of basic crime info
     */
    public Hashtable basicCrimes();
    
    /**
     * Returns a Hashtable of various catch-all properties and variables
     * associated with the tax laws.  The Hashtable keys are all string
     * IDs denoting the property, while the associated element is an object
     * whose type differs by key.  
     * TAXEVASION - a String[] array index by the constant BIT_*
     * PROPERTYTAX - a String representing the property tax rate
     * CITTAX - a String representing the citizen tax rate
     * SALESTAX - a String representing the sales tax rate
     * TREASURY - a String of semicolon delimited info about treasury room/
     *          - safe room.
     * @see com.planet_ink.coffee_mud.Common.interfaces.Law#BIT_CRIMENAME
     * 
     * @return a Hashtable of tax law related property information
     */
    public Hashtable taxLaws();
    
    /**
     * A Vector of strings denoting random things an officer will
     * say while taking an arrested criminal to the judge.
     * 
     * @return a vector of cute sayings.
     */
    public Vector chitChat();
    
    /**
     * A Vector of strings denoting random things an officer will
     * say while taking an arrested criminal to the jail.
     * 
     * @return a vector of cute sayings.
     */
    public Vector chitChat2();
    
    /**
     * A Vector of strings denoting random things an officer will
     * say while taking an arrested criminal to the detention center.
     * 
     * @return a vector of cute sayings.
     */
    public Vector chitChat3();
    
    /**
     * A Vector of strings denoting which rooms are considered jails.
     * They better have a locked door somewhere!
     * 
     * @return a Vector of strings denoting jail rooms
     */
    public Vector jailRooms();
    
    /**
     * A Vector of strings denoting which rooms are considered release 
     * rooms for after a prisoner has served jail time.
     * 
     * @return a Vector of strings denoting release rooms
     */
    public Vector releaseRooms();
    
    /**
     * A Vector a strings denoting which mobs are considered officers
     * of the law in the legal area.
     * 
     * @return a vector of strings denoting the names of officers
     */
    public Vector officerNames();
    
    /**
     * A Vector a strings denoting which mobs are considered judges
     * of the law in the legal area.
     * 
     * @return a vector of strings denoting the names of judges
     */
    public Vector judgeNames();
    
    /**
     * Returns a vector of all old LegalWarrant objects for all
     * criminals and crimes since the last MUD reboot.  These are
     * kept track of so the punishments can be escalated properly.
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.LegalWarrant
     * @see com.planet_ink.coffee_mud.Common.interfaces.Law#getOldWarrant(MOB, String, boolean)
     * 
     * @return a vector of old warrant objects
     */
    public Vector oldWarrants();
    
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
     * Returns a Vector of all current LegalWarrant objects still considered
     * to be active and relevant.  Officers can act on these.
     * 
     * @see com.planet_ink.coffee_mud.Common.interfaces.LegalWarrant
     * @see com.planet_ink.coffee_mud.Common.interfaces.Law#getWarrant(MOB, int)
     * 
     * @return a vector of legal warrants
     */
    public Vector warrants();
    
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
    /** a string array for each of the punishment bitmasks */
    public static final String[] PUNISHMENTMASK_DESCS={
        "SEPARATE",
        "SKIPTRIAL",
        "DETAIN=",
        "FINE=",
        "NORELEASE"
    };
    /** an array of the various bitmask values added to punishment codes */ 
    public static final int[] PUNISHMENTMASK_CODES={
        PUNISHMENTMASK_SEPARATE,
        PUNISHMENTMASK_SKIPTRIAL,
        PUNISHMENTMASK_DETAIN,
        PUNISHMENTMASK_FINE,
        PUNISHMENTMASK_NORELEASE
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
		"WARNINGMSG=Your behavior is unacceptable.  Do not repeat this offense.  You may go.\n"+
		"THREATMSG=That behavior is NOT tolerated here.  Keep your nose clean, or next time I may not be so lenient.  You may go.\n"+
		"JAIL1MSG=You are hereby sentenced to minimum jail time.  Take away the prisoner!\n"+
		"JAIL2MSG=You are hereby sentenced to jail time.  Take away the prisoner!\n"+
		"JAIL3MSG=You are hereby sentenced to hard jail time.  Take away the prisoner!\n"+
		"JAIL4MSG=You are hereby sentenced to rot in jail.  Take away the prisoner!\n"+
		"PAROLE1MSG=You are hereby sentenced to a short period under the prisoner's geas. Perhaps that will make you think!\n"+
		"PAROLE2MSG=You are hereby sentenced to a period under the prisoner's geas. That will teach you, I think.\n"+
		"PAROLE3MSG=You are hereby sentenced to hard time under the prisoner's geas! That will teach you!\n"+
		"PAROLE4MSG=You are hereby sentenced to rot under the prisoner's geas!  Don't let me see you again!\n"+
		"PAROLEDISMISS=Now, get out of my sight!\n"+
		"PREVOFFMSG=You have been warned about this behavior before.\n"+
		"EXECUTEMSG=You are hereby sentenced to a brutal death.  Sentence to be carried out IMMEDIATELY!\n"+
		"LAWFREE=You are free to go.\n"+
		"CHITCHAT=\"You didn't really think you could get away with it did you?\" \"You are REALLY in for it!\" \"Convicts like you are a dime a dozen.\" \"MAKE WAY! DEAD MAN WALKING!\" \"You are gonna GET it.\" \"I love my job.\"\n"+
		"CHITCHAT2=\"You didn't really think you would get away with it did you?\" \"I hope you aren't claustrophobic!\" \"Remember not to drop your soap in there.\" \"MAKE WAY! DEAD MAN WALKING!\" \"I recommend you hold your breathe while you're in there -- I always do.  It stinks!\" \"Putting away scum like you makes it all worthwhile\"\n"+
        "CHITCHAT3=\"This is for your own good, so please don't resist.\" \"You understand that your detention is mandatory I hope.\" \"Just doing my job.\" \"Been a nice day, don't ya think?\" \"We're almost there, don't worry.\" \"Not much farther now.\"\n"+
		"RESISTWARNMSG=I said SIT DOWN! NOW!\n"+
		"NORESISTMSG=Good.  Now hold still.\n"+
		"ACTIVATED=FALSE\n"+
		"RESISTFIGHTMSG=Resisting arrest?! How DARE you!\n"+
		"COPKILLERMSG=COP-KILLER!!!!! AAARRRGGGHHHH!!!!!\n"+
		"RESISTMSG=Resisting arrest eh?  Well, have it your way.\n"+
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
		"RESISTINGARREST=;;resisting arrest;jail1;Resisting arrest by a lawful officer is a serious crime.\n"+
		"TRESPASSING=!home !indoors;!recently;trespassing;jail3;Your kind are not allowed here.\n"+
		"NUDITY=!home !indoors;witness !recently;indecent exposure;warning;Nudity below the waist violates our high moral code.  Use the 'outfit' command if you need clothes!\n"+
		"ARMED=\n"+
		"ASSAULT=;;assaulting <T-NAME>;jail4;Assault is a hideous offense.\n"+
		"MURDER=;;murdering <T-NAME>;death;Murder is a barbarous offense.\n"+
		"PROPERTYROB=;;robbing the property of <T-NAME>;jail3;Robbery violates our high moral code.\n"+
		"TAXEVASION=;;evading taxes;jail1;Paying taxes and dieing are our solemn duties to the state.\n"+
		"TREASURY=\n"+
		"PROPERTYTAX=0\n"+
		"SALESTAX=0\n"+
		"CITTAX=10\n"+
		"THIEF_SWIPE=;;robbing <T-NAME>;jail2;Swiping violates our high moral code.\n"+
		"THIEF_STEAL=;;robbing <T-NAME>;jail3;Stealing violates our high moral code.\n"+
		"THIEF_TRAP=!home;;setting traps in city limits;jail3;Trapping puts us all in mortal danger.\n"+
		"THIEF_BRIBE=;;bribing <T-NAME>;jail2;Bribing is a violation of our moral code.\n"+
		"THIEF_CON=;;conning <T-NAME>;jail2;Conning and deception is a violation of our moral code.\n"+
		"THIEF_EMBEZZLE=;;embezzling <T-NAME>;jail4;Embezzling money is a form of vicious theft!\n"+
		"THIEF_CONTRACTHIT=;;taking out contract on <T-NAME>'s life;death;Murder by contract is a barbarous offense.\n"+
		"THIEF_DEATHTRAP=;;setting a death trap;death;Murder by trapping is a barbarous offense.\n"+
		"THIEF_FORGERY=;;forgery;jail2;Forgery is deceptive and quite illegal.\n"+
		"THIEF_RACKETEER=;;racketeering <T-NAME>;jail3;Racketeering is a form of vicious theft.\n"+
		"THIEF_ROBBERY=;;robbing <T-NAME>;jail3;Robbery violates our high moral code.\n"+
		"INEBRIATION=!home !pub !tavern !inn !bar;!recently;public intoxication;parole1;Drunkenness is a demeaning and intolerable state.\n"+
		"POISON_ALCOHOL=!home !pub !tavern !inn !bar;!recently;public intoxication;parole1;Drunkenness is a demeaning and intolerable state.\n"+
		"POISON_FIREBREATHER=!home !pub !tavern !inn !bar;!recently;public intoxication;parole1;Drunkenness is a demeaning and intolerable state.\n"+
		"POISON_LIQUOR=!home !pub !tavern !inn !bar;!recently;public intoxication;parole1;Drunkenness is a demeaning and intolerable state.\n";
}
