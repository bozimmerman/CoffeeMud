package com.planet_ink.coffee_mud.Races.interfaces;
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
   Copyright 2001-2018 Bo Zimmerman

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
 * A, well, Race
 * @author Bo Zimmerman
 */
public interface Race extends Tickable, StatsAffecting, MsgListener, CMObject, Modifiable
{
	/**
	 * Return a nice, displayable name for this race
	 * @return the races name
	 */
	@Override
	public String name();

	/**
	 * Which racial category this race falls in.
	 * @return racial category
	 */
	public String racialCategory();

	/**
	 * Returns one or a combination of the Area.THEME_*
	 * constants from the Area interface.  This bitmap
	 * then describes the types of areas, skills, and
	 * classes which can interact.
	 * This bitmap is also used to to tell whether
	 * the race is available for selection by users
	 * at char creation time, whether they can
	 * change to this race via spells, or whether
	 * the race is utterly unavailable to them.
	 * @see com.planet_ink.coffee_mud.Areas.interfaces.Area
	 * @return the availability/theme of this race
	 */
	public int availabilityCode();

	/**
	 * After a mob is set or changed to a new race, this method
	 * should be called to finalize or initialize any settings
	 * from this race.
	 * The verify flag is almost always true, unless the mob
	 * is a new player being created, in which case false is sent.
	 * @param mob the mob or player being set to this race
	 * @param verifyOnly true flag unless this is a new player character
	 */
	public void startRacing(MOB mob, boolean verifyOnly);

	/**
	 * Typically called when a mob gains a level with this base-race, to allow the race to
	 * assign any new skills.  Can also be called just to populate a mob with race skills,
	 * so it should also confirm any lower level skills also.
	 * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB#addAbility(Ability)
	 * @param mob the mob to give abilities to.
	 * @param isBorrowedRace whether the skills are savable (false) or temporary (true)
	 */
	public void grantAbilities(MOB mob, boolean isBorrowedRace);

	/**
	 * Will initialize a player or mobs height and weight based
	 * on this races parameters.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.PhyStats
	 * @param stats the PhyStats object to change
	 * @param gender the mobs gender 'M' or 'F'
	 */
	public void setHeightWeight(PhyStats stats, char gender);

	/**
	 * The minimum height of males of this race.
	 * @return minimum height of males in inches
	 */
	public int shortestMale();

	/**
	 * The minimum height of females of this race.
	 * @return minimum height of females in inches
	 */
	public int shortestFemale();

	/**
	 * The amount from 0-this to add to the minimum height
	 * to achieve a random height.
	 * @return a range of inches to add to the mimiumum height
	 */
	public int heightVariance();

	/**
	 * The lightest weight for a member of this race
	 * @return the lightest weight for something of this race
	 */
	public int lightestWeight();

	/**
	 * The amount from 0-this to add to the minumum weight
	 * to achieve a random weight.
	 * @return a range of pounds to add to the minimum weight
	 */
	public int weightVariance();

	/**
	 * Returns an integer array equal in size and index to the
	 * Race.AGE_* constants in the Race interface.  Each value
	 * in the index represents the first mudyear age of that
	 * age category.
	 * @see Race
	 * @return an integer array mapping ages to age categories
	 */
	public int[] getAgingChart();

	/**
	 * A bitmap showing which on locations a member of this
	 * race can not wear clothing, even if the members have one
	 * or more of the required limbs.  The bitmap is made from
	 * Wearable.WORN_* constant values.
	 * @see com.planet_ink.coffee_mud.Items.interfaces.Item
	 * @return the illegal wear location bitmap
	 */
	public long forbiddenWornBits();

	/**
	 * Returns an array indexed by body part codes as defined by
	 * the BODY_* constants in the Race interface.  Each value is
	 * either -1 to show that the body part does not apply, 0 to
	 * show that the body part is not found on this race, and 1 or
	 * more to show how many of that part this race normally has.
	 * @return an array of body parts
	 */
	public int[] bodyMask();

	/**
	 * Converts this race to a generic race (if it isn't already)
	 * and returns it.  Returns itself if its already generic.
	 * @return the generic race.
	 */
	public Race makeGenRace();

	/**
	 * Converts this race to a generic race (if it isn't already)
	 * and mixes its attributes with the race passed it.  A new
	 * race ID and a new race name must also be provided.
	 * @param race the race to use as a baseline
	 * @param newRaceID the id of the new race
	 * @param newRaceName the name of the new race
	 * @return the generic race.
	 */
	public Race mixRace(Race race, String newRaceID, String newRaceName);

	/**
	 * Returns a vector of Item objects representing the standard
	 * clothing, weapons, or other objects commonly given to players
	 * of this race just starting out.
	 * @param myChar one who will receive the objects
	 * @return a vector of Item objects
	 */
	public List<Item> outfit(MOB myChar);

	/**
	 * Returns a description of the given mobs description, by
	 * consulting the mobs curState().getHitPoints method.
	 * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB#curState()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CharState#getHitPoints()
	 * @param viewer the mob observing the health of this one
	 * @param mob the mob whose health to check
	 * @return a string describing his health
	 */
	public String healthText(MOB viewer, MOB mob);

	/**
	 * Returns the list of ability IDs of skills that this
	 * race is flatly and quietly immune to, whether
	 * malicious or not -- it just won't happen.
	 * @return the list of Ability IDs.
	 */
	public String[] abilityImmunities();

	/**
	 * Sends back a generic mob name appropriate to a mob of this
	 * race, at the given gender and age-group.
	 *
	 * @param gender the gender of the mob
	 * @param age the age category
	 * @return a good name
	 */
	public String makeMobName(char gender, int age);

	/**
	 * Returns a Weapon object representing what a member of this
	 * race fights with when unarmed.  This method may change what it
	 * returns on every call to mix things up a bit.
	 * @see com.planet_ink.coffee_mud.Items.interfaces.Weapon
	 * @return a Weapon object representing claws or teeth, etc..
	 */
	public Weapon myNaturalWeapon();

	/**
	 * Returns resource codes of what this race can breathe as
	 * an atmosphere.  The list is guaranteed sorted.  If the list
	 * is empty, the race can breathe anything at all.
	 * @see com.planet_ink.coffee_mud.Items.interfaces.RawMaterial
	 * @return a list of resource codes that this race can breathe
	 */
	public int[] getBreathables();

	/**
	 * Returns a list of RawMaterial objects (usually GenFoodResource, GenLiquidResource,
	 * or GenResource items) representing what is left over of a member of this race
	 * after they've been butchered and cut up.
	 * @see com.planet_ink.coffee_mud.Items.interfaces.RawMaterial
	 * @return a list of rawmaterial objects
	 */
	public List<RawMaterial> myResources();

	/**
	 * Returns the corpse of a member of this race, populates it with the equipment of
	 * the given mob, and places it in the given room.  If the destroyBodyAfterUse returns
	 * true, it will also populate the body with the contents of the myResources method.
	 * @see #myResources()
	 * @param mob the mob to use as a template for the body
	 * @param room the room to place the corpse in
	 * @return the corpse generated and placed in the room
	 */
	public DeadBody getCorpseContainer(MOB mob, Room room);

	/**
	 * Whether this race object represents a Generic Race, or one which is modifiable by
	 * builders at run-time.
	 * @return whether this race is modifiable at run-time.
	 */
	public boolean isGeneric();

	/**
	 * Whether this race is usually rideable; helps determine the appropriate java class to use.
	 * @return whether this race is generally rideable
	 */
	public boolean useRideClass();

	/**
	 * If this race is modifiable at run time, this method will return an xml document
	 * describing the several attributes of this race.
	 * @see #isGeneric()
	 * @see #setRacialParms(String)
	 * @return an xml document describing this race
	 */
	public String racialParms();

	/**
	 * If this race is modifiable at run time, this method will use the given xml document
	 * describing the several attributes of this race to populate this races fields and attributes.
	 * @see #isGeneric()
	 * @see #racialParms()
	 * @param parms an xml document describing this race
	 */
	public void setRacialParms(String parms);

	/**
	 * Returns the string describing what folks see when a member of this race enters a room.
	 * Should give an idea of the gate or walking style of this race.
	 * @return what people see what this race enters a room
	 */
	public String arriveStr();

	/**
	 * Returns the string describing what folks see when a member of this race leaves a room.
	 * Should give an idea of the gate or walking style of this race.
	 * @return what people see what this race leaves a room
	 */
	public String leaveStr();

	/**
	 * This method is called whenever a player gains a level while a member of this race.  If
	 * there are any special things which need to be done to a player who gains a level, they
	 * can be done in this method.  By default, it doesn't do anything.
	 * @param mob the mob to level up
	 * @param gainedAbilityIDs the set of abilities/skill IDs gained during this leveling process
	 */
	public void level(MOB mob, List<String> gainedAbilityIDs);

	/**
	 * Whenever a player or mob of this race gains experience, this method gets a chance
	 * to modify the amount before the gain actually occurs.
	 * @param host the player or mob whose race object this is
	 * @param mob the player or mob gaining experience
	 * @param victim if applicable, the mob or player who died to give the exp
	 * @param amount the amount of exp on track for gaining
	 * @return the adjusted amount of experience to gain
	 */
	public int adjustExperienceGain(MOB host, MOB mob, MOB victim, int amount);

	/**
	 * Returns true if the given race is actually the same as the
	 * current race.  Usually just ID().equals(ID()), or if either
	 * is human.  Passing the race to itself in this method is a good
	 * way to check for general fertility.
	 * @param R the race to check
	 * @return true if its the same as this one, false otherwise
	 */
	public boolean canBreedWith(Race R);

	/**
	 * Whether this race can be associated with a character class.
	 * @see com.planet_ink.coffee_mud.CharClasses.interfaces.CharClass
	 * @return whether this race can have a class
	 */
	public boolean classless();

	/**
	 * Whether players of this race can be associated with an experience level.
	 * @return whether players of this race can have a level
	 */
	public boolean leveless();

	/**
	 * Whether players of this race can gain or lose experience points.
	 * @return whether players of this race can gain or lose experience points
	 */
	public boolean expless();

	/**
	 * Return a vector of skills, spells, and other ability ids granted to the given
	 * mob when they are created as this race.  The entries are the ability id,
	 * the default proficiency, the level, and whether it is auto-gained.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability
	 * @return a quadvector of the Ability IDs, profs, levels, auto-gained 
	 */
	public QuadVector<String,Integer,Integer,Boolean> culturalAbilities();

	/**
	 * Return a vector of skills, spells, and other abilities granted to the given
	 * mob of the given mobs level.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability
	 * @param mob the mob to grant the abilities to
	 * @return a vector of the Ability objects
	 */
	public SearchIDList<Ability> racialAbilities(MOB mob);

	/**
	 * Return a vector of skills, spells, and other abilities granted to the given
	 * mob of the given mobs level.  This method is not functionally used because
	 * it doesn't quite work correctly yet.
	 * @see com.planet_ink.coffee_mud.Abilities.interfaces.Ability
	 * @see com.planet_ink.coffee_mud.Races.interfaces.Race#numRacialEffects(MOB)
	 * @param mob the mob to grant the abilities to
	 * @return a vector of the Ability objects
	 */
	public ChameleonList<Ability> racialEffects(MOB mob);

	/**
	 * Returns the number of racial effects elligible to the given lob. Must
	 * faster and more efficient than getting the whole list and checking its
	 * size.
	 * @param mob the mob to grant the abilities to
	 * @return number of entries in the ability object vector
	 */
	public int numRacialEffects(MOB mob);

	/**
	 * Apply any affects of the given mob at the given age to the given base and/or
	 * current char stats.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CharState
	 * @param mob the mob to apply changes to
	 * @param baseStats permanent charstats changes
	 * @param charStats temporary charstats changes
	 */
	public void agingAffects(MOB mob, CharStats baseStats, CharStats charStats);

	/**
	 * Returns the amount, as a positive or negative % to adjust all experience gains.
	 * 
	 * @return xp adjustment 0-100, or -1 - -100 to adjust experience gains by.
	 */
	public int getXPAdjustment();

	/**
	 * Returns a list of the stat adjustments made by this race
	 * @return a list of the stat adjustments made by this race
	 */
	public String getStatAdjDesc();

	/**
	 * Returns the adjustment to practices made by this race (or nothing)
	 * @return the adjustment to practices made by this race (or nothing)
	 */
	public String getPracAdjDesc();

	/**
	 * Returns the adjustment to trains made by this race (or nothing)
	 * @return the adjustment to trains made by this race (or nothing)
	 */
	public String getTrainAdjDesc();

	/**
	 * Returns the list of modifications to senses done by this race
	 * or nothing.
	 * @return the list of modifications to senses done by this race
	 */
	public String getSensesChgDesc();

	/**
	 * Returns the list of modifications to disposition done by this race
	 * or nothing.
	 * @return the list of modifications to disposition done by this race
	 */
	public String getDispositionChgDesc();

	/**
	 * Returns the list of racial abilities granted to those of this race
	 * or nothing.
	 * @return the list of racial abilities granted to those of this race
	 */
	public String getAbilitiesDesc();

	/**
	 * Returns the list of racial languages granted to those of this race
	 * or nothing.
	 * @return the list of racial languages granted to those of this race
	 */
	public String getLanguagesDesc();
	
	/**
	 * Returns the number of registered usages of this race as of the 
	 * moment of the call.  It includes mobs loaded at boot-time.
	 * @param alter TODO
	 * @return the usage count.
	 */
	public int usageCount(int alter);

	/** Age constant for an infant */
	public final static int AGE_INFANT=0;
	/** Age constant for a toddler */
	public final static int AGE_TODDLER=1;
	/** Age constant for a child */
	public final static int AGE_CHILD=2;
	/** Age constant for a yound adultt */
	public final static int AGE_YOUNGADULT=3;
	/** Age constant for the mature adult */
	public final static int AGE_MATURE=4;
	/** Age constant for the middle aged adult*/
	public final static int AGE_MIDDLEAGED=5;
	/** Age constant for the old*/
	public final static int AGE_OLD=6;
	/** Age constant for the very old*/
	public final static int AGE_VENERABLE=7;
	/** Age constant for the very very old*/
	public final static int AGE_ANCIENT=8;
	/** Constant string list for the names of the age constants, in their order of value */
	public final static String[] AGE_DESCS=CMLib.lang().sessionTranslation(new String[]
	{
			"Infant","Toddler","Child","Young adult","Adult", "Mature", "Old", "Venerable", "Ancient"
	});

	/** Age in Years constant for an immortal thing */
	public final static int YEARS_AGE_LIVES_FOREVER=Integer.MAX_VALUE;

	/** body part constant representing antenea*/
	public final static int BODY_ANTENEA=0;
	/** body part constant representing eyes */
	public final static int BODY_EYE=1;
	/** body part constant representing ears*/
	public final static int BODY_EAR=2;
	/** body part constant representing head*/
	public final static int BODY_HEAD=3;
	/** body part constant representing neck*/
	public final static int BODY_NECK=4;
	/** body part constant representing arm*/
	public final static int BODY_ARM=5;
	/** body part constant representing hand*/
	public final static int BODY_HAND=6;
	/** body part constant representing torso*/
	public final static int BODY_TORSO=7;
	/** body part constant representing legs*/
	public final static int BODY_LEG=8;
	/** body part constant representing feet*/
	public final static int BODY_FOOT=9;
	/** body part constant representing noses*/
	public final static int BODY_NOSE=10;
	/** body part constant representing gills*/
	public final static int BODY_GILL=11;
	/** body part constant representing mouth*/
	public final static int BODY_MOUTH=12;
	/** body part constant representing waists*/
	public final static int BODY_WAIST=13;
	/** body part constant representing tails*/
	public final static int BODY_TAIL=14;
	/** body part constant representing wings*/
	public final static int BODY_WING=15;
	/** the number of body part constants*/
	public final static int BODY_PARTS=16;
	/** constant string list naming each of the BODY_* constants in the order of their value*/
	public final static String[] BODYPARTSTR=
	{
		"ANTENEA","EYE","EAR","HEAD","NECK","ARM","HAND","TORSO","LEG","FOOT",
		"NOSE","GILL","MOUTH","WAIST","TAIL","WING"
	};
	/** constant hash of BODYPARTSTR */
	public final static Map<Object,Integer> BODYPARTHASH=CMStrings.makeNumericHash(BODYPARTSTR);
	
	/** constant used to set and check the classless flag on generic races */
	public final static int GENFLAG_NOCLASS=1;
	/** constant used to set and check the levelless flag on generic races */
	public final static int GENFLAG_NOLEVELS=2;
	/** constant used to set and check the expless flag on generic races */
	public final static int GENFLAG_NOEXP=4;
	/** constant used to set and check the charming flag on generic races */
	public final static int GENFLAG_NOCHARM=8;
	/** constant used to set and check the fertility flag on generic races */
	public final static int GENFLAG_NOFERTILE=16;
	/** constant string list naming each of the GENFLAG_* constants in the order of their value */
	public final static String[] GENFLAG_DESCS={"CLASSLESS","LEVELLESS","EXPLESS","CHARMLESS","CHILDLESS"};

	public final static Map<String,Integer> BODYPARTHASH_RL_LOWER=new SHashtable<String,Integer>(new Enumeration<Pair<String,Integer>>()
	{
		private int									index	= 0;
		private final Stack<Pair<String, Integer>>	others	= new Stack<Pair<String, Integer>>();

		@Override
		public boolean hasMoreElements()
		{
			return (others.size() > 0) || (index < BODYPARTSTR.length);
		}

		@Override
		public Pair<String, Integer> nextElement()
		{
			if (!hasMoreElements())
				throw new NoSuchElementException();
			if (others.size() > 0)
				return others.pop();
			others.push(new Pair<String, Integer>(BODYPARTSTR[index].toLowerCase(), Integer.valueOf(index)));
			others.push(new Pair<String, Integer>("left " + BODYPARTSTR[index].toLowerCase(), Integer.valueOf(index)));
			others.push(new Pair<String, Integer>("right " + BODYPARTSTR[index].toLowerCase(), Integer.valueOf(index)));
			index++;
			return others.pop();
		}
	});

	/** array mapping worn locations to body parts, indexed by body parts. */
	public final static long[] BODY_WEARVECTOR=
	{
		Wearable.WORN_HEAD, // ANTENEA, having any of these removes that pos
		Wearable.WORN_EYES, // EYES, having any of these adds this position
		Wearable.WORN_EARS, // EARS, gains a wear position here for every 2
		Wearable.WORN_HEAD, // HEAD, gains a wear position here for every 1
		Wearable.WORN_NECK, // NECK, gains a wear position here for every 1
		Wearable.WORN_ARMS, // ARMS, gains a wear position here for every 2
		Wearable.WORN_HANDS, // HANDS, gains a wear position here for every 1
		Wearable.WORN_TORSO, // TORSO, gains a wear position here for every 1
		Wearable.WORN_LEGS, // LEGS, gains a wear position here for every 2
		Wearable.WORN_FEET, // FEET, gains a wear position here for every 2
		Wearable.WORN_HEAD, // NOSE, No applicable wear position for this body part
		Wearable.WORN_HEAD, // GILLS, No applicable wear position for this body part
		Wearable.WORN_MOUTH, // MOUTH, gains a wear position here for every 1
		Wearable.WORN_WAIST, // WAIST, gains a wear position here for every 1
		Wearable.WORN_BACK, // TAIL, having any of these removes that pos
		Wearable.WORN_BACK, // WINGS, having any of these removes that pos
	};

	/** 2 dimentional array, indexed first by body_ part constant, with each row
	 * having two values: the first being the Wearable.WORN_ location which is affected
	 * by having or losing this body part, and then the number of such body parts
	 * necessary to gain or lose one such wear location.  A value of -1 means N/A
	 */
	public final static long[][] BODY_WEARGRID=
	{
		{Wearable.WORN_HEAD,-1}, // ANTENEA, having any of these removes that pos
		{Wearable.WORN_EYES,2}, // EYES, having any of these adds this position
		{Wearable.WORN_EARS,2}, // EARS, gains a wear position here for every 2
		{Wearable.WORN_HEAD,1}, // HEAD, gains a wear position here for every 1
		{Wearable.WORN_NECK,1}, // NECK, gains a wear position here for every 1
		{Wearable.WORN_ARMS,2}, // ARMS, gains a wear position here for every 2
		{Wearable.WORN_WIELD|Wearable.WORN_HELD|Wearable.WORN_HANDS
		 |Wearable.WORN_LEFT_FINGER|Wearable.WORN_LEFT_WRIST
		 |Wearable.WORN_RIGHT_FINGER|Wearable.WORN_RIGHT_WRIST,1}, // HANDS, gains a wear position here for every 1
			// lots of exceptions apply to the above
		{Wearable.WORN_TORSO|Wearable.WORN_BACK,1}, // TORSO, gains a wear position here for every 1
		{Wearable.WORN_LEGS,2}, // LEGS, gains a wear position here for every 2
		{Wearable.WORN_FEET,2}, // FEET, gains a wear position here for every 2
		{-1,-1}, // NOSE, No applicable wear position for this body part
		{-1,-1}, // GILLS, No applicable wear position for this body part
		{Wearable.WORN_MOUTH,1}, // MOUTH, gains a wear position here for every 1
		{Wearable.WORN_WAIST,1}, // WAIST, gains a wear position here for every 1
		{-1,-1}, // TAIL, having any of these removes that pos
		{Wearable.WORN_BACK,-1}, // WINGS, having any of these removes that pos
	};
}
