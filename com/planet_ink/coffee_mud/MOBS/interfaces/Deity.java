package com.planet_ink.coffee_mud.MOBS.interfaces;
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
 * A Deity is a type of mob that provides benefits (and punishments) for
 * players that worship them, especially Clerics.  They also provide 
 * for mini-mini quests (called rituals or sins) for obtaining the benefits
 * or punishments, as well as mini-mini quests for clerics to do services.
 * 
 * This interface defines the language for specifying the rituals, 
 * as well as the methods for accessing them and their benefits and
 * detriments.
 * @author Bo Zimmerman
 *
 */
public interface Deity extends MOB
{
	/**
	 * The definition of the key words in the ritual definitions.
	 * Most of these require a parameter of one sort or another,
	 * depending on the deity class.  The command phrases
	 * are separated by &amp; (for and) or | for or.
	 * @author Bo Zimmerman
	 *
	 */
	public enum RitualTrigger
	{
		SAY(CMMsg.TYP_SPEAK),
		TIME(),
		PUTTHING(CMMsg.TYP_PUT,"PUT"),
		BURNTHING(CMMsg.TYP_FIRE),
		EAT(CMMsg.TYP_EAT),
		DRINK(CMMsg.TYP_DRINK),
		INROOM(CMMsg.TYP_LOOK),
		RIDING(),
		CAST(CMMsg.TYP_CAST_SPELL),
		EMOTE(CMMsg.TYP_EMOTE),
		PUTVALUE(CMMsg.TYP_PUT),
		PUTMATERIAL(CMMsg.TYP_PUT),
		BURNMATERIAL(CMMsg.TYP_FIRE,"BURN"),
		BURNVALUE(CMMsg.TYP_FIRE),
		SITTING("SIT"),
		STANDING("STAND"),
		SLEEPING("SLEEP"),
		READING(CMMsg.TYP_READ,"READ"),
		RANDOM(),
		CHECK(),
		WAIT(),
		YOUSAY(),
		OTHERSAY(),
		ALLSAY(),
		;
		private final int msgCode;
		private final String shortName;
		
		private RitualTrigger(int msgCode, String shortName)
		{
			this.msgCode = msgCode;
			this.shortName = shortName;
		}

		private RitualTrigger(int msgCode)
		{
			this.msgCode = msgCode;
			this.shortName = name();
		}

		private RitualTrigger(String shortName)
		{
			this.msgCode = -999;
			this.shortName = name();
		}

		private RitualTrigger()
		{
			this.msgCode = -999;
			this.shortName = name();
		}
		
		public int getCMMsgCode()
		{
			return msgCode;
		}
		
		public String getShortName()
		{
			return shortName;
		}
	}
	
	/**
	 * Separator enum constants for ritual definitions.
	 * @author Bo Zimmerman
	 *
	 */
	public enum RitualConnector
	{
		AND,
		OR
	}

	/**
	 * Gets the Zapper Mask string that defines the requirements
	 * to be a Cleric of this Deity.  See help on ZAPPERMASK
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
	 * @see Deity#setClericRequirements(String)
	 * @see Deity#getClericRequirementsDesc()
	 * @return the Zapper Mask string that defines the requirements
	 */
	public String getClericRequirements();
	
	/**
	 * Sets the Zapper Mask string that defines the requirements
	 * to be a Cleric of this Deity.  See help on ZAPPERMASK
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
	 * @see Deity#getClericRequirements()
	 * @see Deity#getClericRequirementsDesc()
	 * @param reqs the Zapper Mask string that defines the requirements
	 */
	public void setClericRequirements(String reqs);
	
	/**
	 * Returns a friendly readable description of the requirements
	 * to be a Cleric of this Deity.
	 * @see Deity#setClericRequirements(String)
	 * @see Deity#getClericRequirements()
	 * @return  a friendly readable description of the requirements
	 */
	public String getClericRequirementsDesc();

	/**
	 * Gets the Zapper Mask string that defines the requirements
	 * to be a Worshipper of this Deity.  See help on ZAPPERMASK
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
	 * @see Deity#setWorshipRequirements(String)
	 * @see Deity#getWorshipRequirementsDesc()
	 * @return the Zapper Mask string that defines the requirements
	 */
	public String getWorshipRequirements();
	
	/**
	 * Sets the Zapper Mask string that defines the requirements
	 * to be a Worshipper of this Deity.  See help on ZAPPERMASK
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary
	 * @see Deity#getWorshipRequirements()
	 * @see Deity#getWorshipRequirementsDesc()
	 * @param reqs the Zapper Mask string that defines the requirements
	 */
	public void setWorshipRequirements(String reqs);
	
	/**
	 * Returns a friendly readable description of the requirements
	 * to be a Worshipper of this Deity.
	 * @see Deity#setWorshipRequirements(String)
	 * @see Deity#getWorshipRequirements()
	 * @return  a friendly readable description of the requirements
	 */
	public String getWorshipRequirementsDesc();

	/* Manipulation of blessing objects, which includes spells, traits, skills, etc.*/
	
	/**
	 * Adds a new blessing, which worshippers/clerics can get from performming
	 * the appropriate deity ritual.  The blessing can also be designated as
	 * only for clerics, or for both clerics and worshippers.
	 * @see Deity#getClericRitual()
	 * @see Deity#getWorshipRitual()
	 * @see Deity#delBlessing(Ability)
	 * @see Deity#numBlessings()
	 * @see Deity#fetchBlessing(String)
	 * @see Deity#fetchBlessingCleric(int)
	 * @see Deity#fetchBlessing(int)
	 * @param to the blessing ability object
	 * @param clericOnly true if its only for clerics, false if for everyone
	 */
	public void addBlessing(Ability to, boolean clericOnly);
	
	/**
	 * Deletes the given blessing, which worshippers/clerics can get from performming
	 * the appropriate deity ritual.
	 * @see Deity#getClericRitual()
	 * @see Deity#getWorshipRitual()
	 * @see Deity#addBlessing(Ability, boolean)
	 * @see Deity#numBlessings()
	 * @see Deity#fetchBlessing(String)
	 * @see Deity#fetchBlessingCleric(int)
	 * @see Deity#fetchBlessing(int)
	 * @param to the blessing ability object
	 */
	public void delBlessing(Ability to);
	
	/**
	 * Returns the total number of blessings, both cleric and worshipper.
	 * Blessings are spells cast on worshippers or clerics by the deity for 
	 * performing an appropriate ritual.
	 * @see Deity#getClericRitual()
	 * @see Deity#getWorshipRitual()
	 * @see Deity#addBlessing(Ability, boolean)
	 * @see Deity#delBlessing(Ability)
	 * @see Deity#fetchBlessing(String)
	 * @see Deity#fetchBlessingCleric(int)
	 * @see Deity#fetchBlessing(int)
	 * @return the total number of blessings, both cleric and worshipper.
	 */
	public int numBlessings();
	
	/**
	 * Gets the blessing at the given index.  Blessings are spells
	 * cast on worshippers or clerics by the deity for performing
	 * an appropriate ritual.
	 * @see Deity#getClericRitual()
	 * @see Deity#getWorshipRitual()
	 * @see Deity#addBlessing(Ability, boolean)
	 * @see Deity#delBlessing(Ability)
	 * @see Deity#fetchBlessing(String)
	 * @see Deity#fetchBlessingCleric(int)
	 * @see Deity#numBlessings()
	 * @param index the index of the blessing to return info about
	 * @return the blessing ability object at the given index
	 */
	public Ability fetchBlessing(int index);
	
	/**
	 * Returns the blessing the given Ability ID.  Blessings are spells
	 * cast on worshippers or clerics by the deity for performing
	 * an appropriate ritual.
	 * @see Deity#getClericRitual()
	 * @see Deity#getWorshipRitual()
	 * @see Deity#addBlessing(Ability, boolean)
	 * @see Deity#delBlessing(Ability)
	 * @see Deity#fetchBlessing(int)
	 * @see Deity#fetchBlessingCleric(int)
	 * @see Deity#numBlessings()
	 * @param ID the Ability ID of the blessing to return info about
	 * @return the blessing ability object with the given ID
	 */
	public Ability fetchBlessing(String ID);
	
	/**
	 * Returns whether the blessing at the given index is only for
	 * clerics.  Blessings are spells cast on worshippers or clerics 
	 * by the deity for performing an appropriate ritual.
	 * @see Deity#getClericRitual()
	 * @see Deity#getWorshipRitual()
	 * @see Deity#addBlessing(Ability, boolean)
	 * @see Deity#delBlessing(Ability)
	 * @see Deity#fetchBlessing(String)
	 * @see Deity#fetchBlessing(int)
	 * @see Deity#fetchBlessingCleric(String)
	 * @see Deity#numBlessings()
	 * @param index the index of the blessing to return info about
	 * @return true if the blessing at this index is only for clerics
	 */
	public boolean fetchBlessingCleric(int index);
	
	/**
	 * Returns whether the blessing with the given Ability ID is only for
	 * clerics.  Blessings are spells cast on worshippers or clerics 
	 * by the deity for performing an appropriate ritual.
	 * @see Deity#getClericRitual()
	 * @see Deity#getWorshipRitual()
	 * @see Deity#addBlessing(Ability, boolean)
	 * @see Deity#delBlessing(Ability)
	 * @see Deity#fetchBlessing(String)
	 * @see Deity#fetchBlessing(int)
	 * @see Deity#fetchBlessingCleric(int)
	 * @see Deity#numBlessings()
	 * @param ID the ability id of the blessing to return info about
	 * @return true if the blessing with that ID is only for clerics
	 */
	public boolean fetchBlessingCleric(String ID);

	/**
	 * Gets the raw ritual command string that defines what a
	 * cleric must do to receive the Blessings of this deity.
	 * These are coded strings with one line commands from the
	 * RitualTrigger list, along with one or more parameters.
	 * The command phrases are separated by &amp; or | to denote
	 * AND or OR.
	 * @see Deity
	 * @see Deity#fetchBlessing(String)
	 * @see Deity#setClericRitual(String)
	 * @see Deity#getClericTriggerDesc()
	 * @see Deity.RitualTrigger
	 * @see Deity.RitualConnector
	 * @return the coded ritual command string
	 */
	public String getClericRitual();

	/**
	 * Sets the raw ritual command string that defines what a
	 * cleric must do to receive the Blessings of this deity.
	 * These are coded strings with one line commands from the
	 * RitualTrigger list, along with one or more parameters.
	 * The command phrases are separated by &amp; or | to denote
	 * AND or OR.
	 * @see Deity
	 * @see Deity#fetchBlessing(String)
	 * @see Deity#getClericRitual()
	 * @see Deity#getClericTriggerDesc()
	 * @see Deity.RitualTrigger
	 * @see Deity.RitualConnector
	 * @param ritual the coded ritual command string
	 */
	public void setClericRitual(String ritual);
	
	/** 
	 * Returns the friendly description of the ritual that
	 * cleric must do to receive the Blessings of this deity.
	 * @see Deity
	 * @see Deity#fetchBlessing(String)
	 * @see Deity#getClericRitual()
	 * @see Deity#setClericRitual(String)
	 * @see Deity.RitualTrigger
	 * @see Deity.RitualConnector
	 * @return a description of the blessing ritual for clerics
	 */
	public String getClericTriggerDesc();

	/**
	 * Gets the raw ritual command string that defines what a
	 * cleric must do to complete a Service of this deity.
	 * These are coded strings with one line commands from the
	 * RitualTrigger list, along with one or more parameters.
	 * The command phrases are separated by &amp;.
	 * @see Deity
	 * @see Deity#fetchBlessing(String)
	 * @see Deity#setServiceRitual(String)
	 * @see Deity#getServiceTriggerDesc()
	 * @see Deity.RitualTrigger
	 * @see Deity.RitualConnector
	 * @return the coded ritual command string
	 */
	public String getServiceRitual();

	/**
	 * Sets the raw ritual command string that defines what a
	 * cleric must do to complete a Service of this deity.
	 * These are coded strings with one line commands from the
	 * RitualTrigger list, along with one or more parameters.
	 * The command phrases are separated by &amp;.
	 * @see Deity
	 * @see Deity#fetchBlessing(String)
	 * @see Deity#getServiceRitual()
	 * @see Deity#getServiceTriggerDesc()
	 * @see Deity.RitualTrigger
	 * @see Deity.RitualConnector
	 * @param ritual the coded ritual command string
	 */
	public void setServiceRitual(String ritual);
	
	/** 
	 * Returns the friendly description of the ritual that
	 * cleric must do to perform a service for this deity.
	 * @see Deity
	 * @see Deity#fetchBlessing(String)
	 * @see Deity#getServiceRitual()
	 * @see Deity#setServiceRitual(String)
	 * @see Deity.RitualTrigger
	 * @see Deity.RitualConnector
	 * @return a description of the service ritual for clerics
	 */
	public String getServiceTriggerDesc();

	/**
	 * Gets the raw ritual command string that defines what a
	 * worshipper must do to receive the Blessings of this deity.
	 * These are coded strings with one line commands from the
	 * RitualTrigger list, along with one or more parameters.
	 * The command phrases are separated by &amp; or | to denote
	 * AND or OR.
	 * @see Deity
	 * @see Deity#fetchBlessing(String)
	 * @see Deity#setWorshipRitual(String)
	 * @see Deity#getWorshipTriggerDesc()
	 * @see Deity.RitualTrigger
	 * @see Deity.RitualConnector
	 * @return the coded ritual command string
	 */
	public String getWorshipRitual();

	/**
	 * Sets the raw ritual command string that defines what a
	 * worshipper must do to receive the Blessings of this deity.
	 * These are coded strings with one line commands from the
	 * RitualTrigger list, along with one or more parameters.
	 * The command phrases are separated by &amp; or | to denote
	 * AND or OR.
	 * @see Deity
	 * @see Deity#fetchBlessing(String)
	 * @see Deity#getWorshipRitual()
	 * @see Deity#getWorshipTriggerDesc()
	 * @see Deity.RitualTrigger
	 * @see Deity.RitualConnector
	 * @param ritual the coded ritual command string
	 */
	public void setWorshipRitual(String ritual);
	
	/** 
	 * Returns the friendly description of the ritual that
	 * worshipper must do to receive the Blessings of this deity.
	 * @see Deity
	 * @see Deity#fetchBlessing(String)
	 * @see Deity#getWorshipRitual()
	 * @see Deity#setWorshipRitual(String)
	 * @see Deity.RitualTrigger
	 * @see Deity.RitualConnector
	 * @return a description of the blessing ritual for worshippers
	 */
	public String getWorshipTriggerDesc();

	/* Manipulation of curse objects, which includes spells, traits, skills, etc.*/
	
	/**
	 * Adds a new curse, which worshippers/clerics can get from performming
	 * the appropriate deity "Sin".  The curse can also be designated as
	 * only for clerics, or for both clerics and worshippers.
	 * @see Deity#getClericSin()
	 * @see Deity#getWorshipSin()
	 * @see Deity#delCurse(Ability)
	 * @see Deity#numCurses()
	 * @see Deity#fetchCurse(String)
	 * @see Deity#fetchCurseCleric(int)
	 * @see Deity#fetchCurse(int)
	 * @param to the curse ability object
	 * @param clericOnly true if its only for clerics, false if for everyone
	 */
	public void addCurse(Ability to, boolean clericOnly);
	
	/**
	 * Deletes the given curse, which worshippers/clerics can get from performming
	 * the appropriate deity "Sin".
	 * @see Deity#getClericSin()
	 * @see Deity#getWorshipSin()
	 * @see Deity#addCurse(Ability, boolean)
	 * @see Deity#numCurses()
	 * @see Deity#fetchCurse(String)
	 * @see Deity#fetchCurseCleric(int)
	 * @see Deity#fetchCurse(int)
	 * @param to the curse ability object
	 */
	public void delCurse(Ability to);
	
	/**
	 * Returns the total number of curses, both cleric and worshipper.
	 * Curses are spells cast on worshippers or clerics by the deity for 
	 * performing an appropriate "Sin".
	 * @see Deity#getClericSin()
	 * @see Deity#getWorshipSin()
	 * @see Deity#addCurse(Ability, boolean)
	 * @see Deity#delCurse(Ability)
	 * @see Deity#fetchCurse(String)
	 * @see Deity#fetchCurseCleric(int)
	 * @see Deity#fetchCurse(int)
	 * @return the total number of curses, both cleric and worshipper.
	 */
	public int numCurses();
	
	/**
	 * Gets the curse at the given index.  Curses are spells
	 * cast on worshippers or clerics by the deity for performing
	 * an appropriate "Sin".
	 * @see Deity#getClericSin()
	 * @see Deity#getWorshipSin()
	 * @see Deity#addCurse(Ability, boolean)
	 * @see Deity#delCurse(Ability)
	 * @see Deity#fetchCurse(String)
	 * @see Deity#fetchCurseCleric(int)
	 * @see Deity#numCurses()
	 * @param index the index of the curse to return info about
	 * @return the curse ability object at the given index
	 */
	public Ability fetchCurse(int index);
	
	/**
	 * Returns the curse the given Ability ID.  Curses are spells
	 * cast on worshippers or clerics by the deity for performing
	 * an appropriate "Sin".
	 * @see Deity#getClericSin()
	 * @see Deity#getWorshipSin()
	 * @see Deity#addCurse(Ability, boolean)
	 * @see Deity#delCurse(Ability)
	 * @see Deity#fetchCurse(int)
	 * @see Deity#fetchCurseCleric(int)
	 * @see Deity#numCurses()
	 * @param ID the Ability ID of the curse to return info about
	 * @return the curse ability object with the given ID
	 */
	public Ability fetchCurse(String ID);
	
	/**
	 * Returns whether the curse at the given index is only for
	 * clerics.  Curses are spells cast on worshippers or clerics 
	 * by the deity for performing an appropriate "Sin".
	 * @see Deity#getClericSin()
	 * @see Deity#getWorshipSin()
	 * @see Deity#addCurse(Ability, boolean)
	 * @see Deity#delCurse(Ability)
	 * @see Deity#fetchCurse(String)
	 * @see Deity#fetchCurse(int)
	 * @see Deity#fetchCurseCleric(String)
	 * @see Deity#numCurses()
	 * @param index the index of the curse to return info about
	 * @return true if the curse at this index is only for clerics
	 */
	public boolean fetchCurseCleric(int index);
	
	/**
	 * Returns whether the curse with the given Ability ID is only for
	 * clerics.  Curses are spells cast on worshippers or clerics 
	 * by the deity for performing an appropriate "Sin".
	 * @see Deity#getClericSin()
	 * @see Deity#getWorshipSin()
	 * @see Deity#addCurse(Ability, boolean)
	 * @see Deity#delCurse(Ability)
	 * @see Deity#fetchCurse(String)
	 * @see Deity#fetchCurse(int)
	 * @see Deity#fetchCurseCleric(int)
	 * @see Deity#numCurses()
	 * @param ID the ability id of the curse to return info about
	 * @return true if the curse with that ID is only for clerics
	 */
	public boolean fetchCurseCleric(String ID);
	
	/**
	 * Gets the raw ritual command string that defines what a
	 * cleric must do to receive the Curses of this deity.
	 * These are coded strings with one line commands from the
	 * RitualTrigger list, along with one or more parameters.
	 * The command phrases are separated by &amp; or | to denote
	 * AND or OR.
	 * @see Deity
	 * @see Deity#fetchCurse(String)
	 * @see Deity#setClericSin(String)
	 * @see Deity#getClericSinDesc()
	 * @see Deity.RitualTrigger
	 * @see Deity.RitualConnector
	 * @return the coded ritual command string
	 */
	public String getClericSin();
	
	/**
	 * Sets the raw ritual command string that defines what a
	 * cleric must do to receive the Curses of this deity.
	 * These are coded strings with one line commands from the
	 * RitualTrigger list, along with one or more parameters.
	 * The command phrases are separated by &amp; or | to denote
	 * AND or OR.
	 * @see Deity
	 * @see Deity#fetchCurse(String)
	 * @see Deity#getClericSin()
	 * @see Deity#getClericSinDesc()
	 * @see Deity.RitualTrigger
	 * @see Deity.RitualConnector
	 * @param ritual the coded ritual command string
	 */
	public void setClericSin(String ritual);
	
	/** 
	 * Returns the friendly description of the ritual that
	 * cleric must do to receive the Curses of this deity.
	 * @see Deity
	 * @see Deity#fetchCurse(String)
	 * @see Deity#getClericSin()
	 * @see Deity#setClericSin(String)
	 * @see Deity.RitualTrigger
	 * @see Deity.RitualConnector
	 * @return a description of the cursing ritual for clerics
	 */
	public String getClericSinDesc();

	/**
	 * Gets the raw ritual command string that defines what a
	 * worshipper must do to receive the Curses of this deity.
	 * These are coded strings with one line commands from the
	 * RitualTrigger list, along with one or more parameters.
	 * The command phrases are separated by &amp; or | to denote
	 * AND or OR.
	 * @see Deity
	 * @see Deity#fetchCurse(String)
	 * @see Deity#setWorshipSin(String)
	 * @see Deity#getWorshipSinDesc()
	 * @see Deity.RitualTrigger
	 * @see Deity.RitualConnector
	 * @return the coded ritual command string
	 */
	public String getWorshipSin();
	
	/**
	 * Sets the raw ritual command string that defines what a
	 * worshipper must do to receive the Curses of this deity.
	 * These are coded strings with one line commands from the
	 * RitualTrigger list, along with one or more parameters.
	 * The command phrases are separated by &amp; or | to denote
	 * AND or OR.
	 * @see Deity
	 * @see Deity#fetchCurse(String)
	 * @see Deity#getWorshipSin()
	 * @see Deity#getWorshipSinDesc()
	 * @see Deity.RitualTrigger
	 * @see Deity.RitualConnector
	 * @param ritual the coded ritual command string
	 */
	public void setWorshipSin(String ritual);
	
	/** 
	 * Returns the friendly description of the ritual that
	 * worshipper must do to receive the Curses of this deity.
	 * @see Deity
	 * @see Deity#fetchCurse(String)
	 * @see Deity#getWorshipSin()
	 * @see Deity#setWorshipSin(String)
	 * @see Deity.RitualTrigger
	 * @see Deity.RitualConnector
	 * @return a description of the cursing ritual for worshippers
	 */
	public String getWorshipSinDesc();

	/* Manipulation of granted clerical powers, which includes spells, traits, skills, etc.*/
	/* Make sure that none of these can really be qualified for by the cleric!*/
	
	/**
	 * Adds a new power up ability, which clerics can get from performming
	 * the appropriate powerup ritual.
	 * @see Deity#getClericPowerup()
	 * @see Deity#delPower(Ability)
	 * @see Deity#numPowers()
	 * @see Deity#fetchPower(String)
	 * @see Deity#fetchPower(int)
	 * @param to the power up ability object
	 */
	public void addPower(Ability to);
	
	/**
	 * Removes the power up ability, which clerics can get from performming
	 * the appropriate powerup ritual.
	 * @see Deity#getClericPowerup()
	 * @see Deity#addPower(Ability)
	 * @see Deity#numPowers()
	 * @see Deity#fetchPower(String)
	 * @see Deity#fetchPower(int)
	 * @param to the power up ability object to remove
	 */
	public void delPower(Ability to);
	
	/**
	 * Returns the number of power up abilities, which clerics get from
	 * performing the appropriate powerup ritual.
	 * @see Deity#getClericPowerup()
	 * @see Deity#addPower(Ability)
	 * @see Deity#delPower(Ability)
	 * @see Deity#fetchPower(String)
	 * @see Deity#fetchPower(int)
	 * @return the number of power up abilities
	 */
	public int numPowers();
	
	/**
	 * Returns the power up abilities at the given index.
	 * Power up abilities are Abilities which clerics get from
	 * performing the appropriate powerup ritual.
	 * @see Deity#getClericPowerup()
	 * @see Deity#addPower(Ability)
	 * @see Deity#delPower(Ability)
	 * @see Deity#fetchPower(String)
	 * @see Deity#numPowers()
	 * @param index the index of the Ability to return
	 * @return the power up Ability object at that index
	 */
	public Ability fetchPower(int index);
	
	/**
	 * Returns the power up abilities with the given Ability ID.
	 * Power up abilities are Abilities which clerics get from
	 * performing the appropriate powerup ritual.
	 * @see Deity#getClericPowerup()
	 * @see Deity#addPower(Ability)
	 * @see Deity#delPower(Ability)
	 * @see Deity#fetchPower(String)
	 * @see Deity#numPowers()
	 * @param ID the Ability ID of the Ability to return
	 * @return the power up Ability object with the Ability ID
	 */
	public Ability fetchPower(String ID);

	/**
	 * Gets the raw ritual command string that defines what a
	 * cleric must do to receive the Power Up Abilities of this deity.
	 * These are coded strings with one line commands from the
	 * RitualTrigger list, along with one or more parameters.
	 * The command phrases are separated by &amp; or | to denote
	 * AND or OR.
	 * @see Deity
	 * @see Deity#fetchPower(String)
	 * @see Deity#setClericPowerup(String)
	 * @see Deity#getClericPowerupDesc()
	 * @see Deity.RitualTrigger
	 * @see Deity.RitualConnector
	 * @return the coded ritual command string
	 */
	public String getClericPowerup();

	/**
	 * Sets the raw ritual command string that defines what a
	 * cleric must do to receive the Power Up Abilities of this deity.
	 * These are coded strings with one line commands from the
	 * RitualTrigger list, along with one or more parameters.
	 * The command phrases are separated by &amp; or | to denote
	 * AND or OR.
	 * @see Deity
	 * @see Deity#fetchPower(String)
	 * @see Deity#getClericPowerup()
	 * @see Deity#getClericPowerupDesc()
	 * @see Deity.RitualTrigger
	 * @see Deity.RitualConnector
	 * @param ritual the coded ritual command string
	 */
	public void setClericPowerup(String ritual);
	
	/** 
	 * Returns the friendly description of the ritual that
	 * cleric must do to receive the Power Up Abilities of this deity.
	 * @see Deity
	 * @see Deity#fetchPower(String)
	 * @see Deity#getClericPowerup()
	 * @see Deity#setClericPowerup(String)
	 * @see Deity.RitualTrigger
	 * @see Deity.RitualConnector
	 * @return a description of the power up ritual for clerics
	 */
	public String getClericPowerupDesc();

}
