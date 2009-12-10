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
 * The EnvStats interface is a state object that holds some basic information about
 * just about every Environmental object in the game.  Not all stats are relevant for
 * ALL Environmentals, but most, especially the big ones.
 * 
 * Environmentals always keep two instances of this object, a base one, representing
 * his base unmodified state, and current one, representing his state after spells
 * and other affects have had their say.
 * @see com.planet_ink.coffee_mud.core.interfaces.CMModifiable
 * @see com.planet_ink.coffee_mud.core.interfaces.Environmental#baseEnvStats()
 * @see com.planet_ink.coffee_mud.core.interfaces.Environmental#envStats()
 */
public interface EnvStats extends CMCommon, CMModifiable
{
    /**
     * Returns a bitmask for sense related flags of mobs (CAN_ constants), or 
     * miscellaneous runtime flags for items and other Environmentals (SENSE_ constants). 
     * @see com.planet_ink.coffee_mud.Common.interfaces.EnvStats#SENSE_ITEMNEVERSAVED
     * @see com.planet_ink.coffee_mud.Common.interfaces.EnvStats#CAN_NOT_SEE
     * @see com.planet_ink.coffee_mud.Common.interfaces.EnvStats#setSensesMask(int)
     * @return a bitmask made up of SENSE_ constants or CAN_ constants
     */
    public int sensesMask(); // mobs, run-time items
    
    
    /**
     * Sets a bitmask for sense related flags of mobs (CAN_ constants), or 
     * miscellaneous runtime flags for items and other Environmentals (SENSE_ constants). 
     * @see com.planet_ink.coffee_mud.Common.interfaces.EnvStats#SENSE_ITEMNEVERSAVED
     * @see com.planet_ink.coffee_mud.Common.interfaces.EnvStats#CAN_NOT_SEE
     * @see com.planet_ink.coffee_mud.Common.interfaces.EnvStats#setSensesMask(int)
     * @param newMask a bitmask made up of SENSE_ constants or CAN_ constants
     */
    public void setSensesMask(int newMask);
    
    /**
     * Returns a bitmask for disposition related flags of Environmentals (IS_ constants).
     * @see com.planet_ink.coffee_mud.Common.interfaces.EnvStats#IS_BONUS
     * @see com.planet_ink.coffee_mud.Common.interfaces.EnvStats#setDisposition(int)
     * @return a bitmask of disposition related flags
     */
    public int disposition(); // items, mobs
    
    /**
     * Sets a bitmask for disposition related flags of Environmentals (IS_ constants).
     * @see com.planet_ink.coffee_mud.Common.interfaces.EnvStats#IS_BONUS
     * @see com.planet_ink.coffee_mud.Common.interfaces.EnvStats#disposition()
     * @param newDisposition a bitmask of disposition flags
     */
    public void setDisposition(int newDisposition);
    
    /**
     * Returns the experience level of the mob, item, exit, Environmental.
     * @see com.planet_ink.coffee_mud.Common.interfaces.EnvStats#setLevel(int)
     * @return the experience level
     */
    public int level(); // items, exits, mobs
    
    /**
     * Sets the experience level of the mob, item, exit, Environmental.
     * @see com.planet_ink.coffee_mud.Common.interfaces.EnvStats#level()
     * @param newLevel the new experience level
     */
    public void setLevel(int newLevel);

    /**
     * Returns the ability level (a secondary level, e.g. if magical, how much?)
     * Also acts as a random flag for various purposes.  For instance, on mobs,
     * it designates a hit point multiplier.
     * @see com.planet_ink.coffee_mud.Common.interfaces.EnvStats#setAbility(int)
     * @return the ability level (or misc integer)
     */
    public int ability(); // items, mobs
    
    /**
     * Sets the ability level (a secondary level, e.g. if magical, how much?)
     * Also acts as a random flag for various purposes. For instance, on mobs,
     * it designates a hit point multiplier.
     * @see com.planet_ink.coffee_mud.Common.interfaces.EnvStats#ability()
     * @param newAdjustment the new ability level (or misc integer)
     */
    public void setAbility(int newAdjustment);
    
    /**
     * Returns the number of ticks before an Environmental removed from the game
     * (due to death, destruction, or just removal from home) is restored.
     * @see com.planet_ink.coffee_mud.Common.interfaces.EnvStats#setRejuv(int)
     * @return the number of ticks before rejuv (0==never)
     */
    public int rejuv(); // items, mobs
    
    /**
     * Sets the number of ticks before an Environmental removed from the game
     * (due to death, destruction, or just removal from home) is restored.
     * @see com.planet_ink.coffee_mud.Common.interfaces.EnvStats#rejuv()
     * @param newRejuv the new number of ticks before rejuv (0==never)
     */
    public void setRejuv(int newRejuv);

    /**
     * Returns the weight of this Environmental, in pounds.
     * @see com.planet_ink.coffee_mud.Common.interfaces.EnvStats#setWeight(int)
     * @return the weight of this Environmental
     */
    public int weight(); // items, mobs
    
    /**
     * Sets the weight of this Environmental, in pounds.
     * @see com.planet_ink.coffee_mud.Common.interfaces.EnvStats#weight()
     * @param newWeight the new weight of this Environmental
     */
    public void setWeight(int newWeight);

    /**
     * Returns the height of this Environmental, in inches
     * @see com.planet_ink.coffee_mud.Common.interfaces.EnvStats#setHeight(int)
     * @return the height of this Environmental, in inches (0=indeterminate)
     */
    public int height(); // items, mobs
    
    /**
     * Sets the height of this Environmental, in inches
     * @see com.planet_ink.coffee_mud.Common.interfaces.EnvStats#height()
     * @param newHeight the new height of this Environmental, in inches (0=indeterminate)
     */
    public void setHeight(int newHeight);

    /**
     * Returns the defensive capability number of this Environmental.
     * @see com.planet_ink.coffee_mud.Common.interfaces.EnvStats#setArmor(int)
     * @return the raw defensive capability of this Environmental
     */
    public int armor(); // armor items, mobs
    
    /**
     * Sets the defensive capability number of this Environmental.
     * @see com.planet_ink.coffee_mud.Common.interfaces.EnvStats#armor()
     * @param newArmor the defensive capability number of this Environmental
     */
    public void setArmor(int newArmor);

    /**
     * Returns the maximum damaging ability of this Environmental 
     * @see com.planet_ink.coffee_mud.Common.interfaces.EnvStats#setDamage(int)
     * @return the maxiumu damaging ability of this Environmental
     */
    public int damage(); // weapon items, mobs
    
    /**
     * Sets the maximum damaging ability of this Environmental 
     * @see com.planet_ink.coffee_mud.Common.interfaces.EnvStats#damage()
     * @param newDamage the new maximum damaging ability of this Environmental
     */
    public void setDamage(int newDamage);

    /**
     * Returns the number of actions this mob can do per tick.
     * @see com.planet_ink.coffee_mud.Common.interfaces.EnvStats#setSpeed(double)
     * @return the number of actions per tick.
     */
    public double speed(); // mobs
    
    /**
     * Sets the number of actions this mob can do per tick 
     * @see com.planet_ink.coffee_mud.Common.interfaces.EnvStats#speed()
     * @param newSpeed the new number of actions this mob can do per tick
     */
    public void setSpeed(double newSpeed);

    /**
     * Returns the rawcombat attack prowess of this Environmental
     * Usually mobs or weapons
     * @see com.planet_ink.coffee_mud.Common.interfaces.EnvStats#setAttackAdjustment(int)
     * @return the raw combat attack prowess of this Environmental
     */
    public int attackAdjustment(); // weapon items, mobs
    
    /**
     * Sets the rawcombat attack prowess of this Environmental
     * Usually mobs or weapons
     * @see com.planet_ink.coffee_mud.Common.interfaces.EnvStats#attackAdjustment()
     * @param newAdjustment the new raw combat attack prowess of this Environmental
     */
    public void setAttackAdjustment(int newAdjustment);

    /**
     * Returns a modified name for this Environmental, usually null for no change.
     * @see com.planet_ink.coffee_mud.Common.interfaces.EnvStats#setName(String)
     * @return the modified name, or null
     */
    public String newName(); // items, mobs

    /**
     * Sets a modified name for this Environmental, usually null for no change.
     * @see com.planet_ink.coffee_mud.Common.interfaces.EnvStats#newName()
     * @param newName the modified name, or null
     */
    public void setName(String newName);

    /**
     * Returns a list of ambiances (extra words, visible fields) that are tacked
     * onto the display text of this Environmental. 
     * @see com.planet_ink.coffee_mud.Common.interfaces.EnvStats#addAmbiance(String)
     * @see com.planet_ink.coffee_mud.Common.interfaces.EnvStats#delAmbiance(String)
     * @return a list of ambiances
     */
    public String[] ambiances(); // everything
    
    /**
     * Adds an ambiance (extra word, visible field) to the list that are tacked
     * onto the display text of this Environmental. 
     * @see com.planet_ink.coffee_mud.Common.interfaces.EnvStats#ambiances()
     * @see com.planet_ink.coffee_mud.Common.interfaces.EnvStats#delAmbiance(String)
     * @param ambiance a new ambiance string
     */
    public void addAmbiance(String ambiance);
    
    /**
     * Removes an ambiance (extra word, visible field) from the list that are tacked
     * onto the display text of this Environmental. 
     * @param ambiance the old ambiance string
     */
    public void delAmbiance(String ambiance);
    
    /**
     * Returns a single string summary of several important stats from this object.
     * @return a single string summary of several important stats from this object.
     */
    public String getCombatStats();
    
    /**
     * Returns whether the given object is substantially the same as this one
     * @param E the EnvStats to compare to
     * @return whether or not they are the same
     */
    public boolean sameAs(EnvStats E);
    
    /**
     * Copies the internal data of this object into another of kind.
     * @param intoStats another EnvStats object.
     */
    public void copyInto(EnvStats intoStats);

    /**
     * Sets all the stats in this object to the given value
     * @param def a value to set all the stats to
     */
    public void setAllValues(int def);

    /** an index into the internal stats integer array for level */
    public final static int STAT_LEVEL=0;
    /** an index into the internal stats integer array for senses */
    public final static int STAT_SENSES=1;         // see Senses class
    /** an index into the internal stats integer array for armor */
    public final static int STAT_ARMOR=2;            // should be positive
    /** an index into the internal stats integer array for damage */
    public final static int STAT_DAMAGE=3;             // should be positive
    /** an index into the internal stats integer array for attack */
    public final static int STAT_ATTACK=4;   // should be negative
    /** an index into the internal stats integer array for disposition */
    public final static int STAT_DISPOSITION=5;        // see Senses class
    /** an index into the internal stats integer array for rejuv */
    public final static int STAT_REJUV=6;
    /** an index into the internal stats integer array for weight */
    public final static int STAT_WEIGHT=7;
    /** an index into the internal stats integer array for ability */
    public final static int STAT_ABILITY=8;            // object dependant
    /** an index into the internal stats integer array for height */
    public final static int STAT_HEIGHT=9;
    /** the size of the internal stats integer array */
    public final static int NUM_STATS=10;
     
	// sensemask stuff
    /** a bit setting, as from sensesMask(), flagging this mob as unable to see */
	public final static int CAN_NOT_SEE=1;
    /** a bit setting, as from sensesMask(), flagging this mob as able to see hidden */
	public final static int CAN_SEE_HIDDEN=2;
    /** a bit setting, as from sensesMask(), flagging this mob as able to see invisible */
	public final static int CAN_SEE_INVISIBLE=4;
    /** a bit setting, as from sensesMask(), flagging this mob as able to see evil */
	public final static int CAN_SEE_EVIL=8;
    /** a bit setting, as from sensesMask(), flagging this mob as able to see good */
	public final static int CAN_SEE_GOOD=16;
    /** a bit setting, as from sensesMask(), flagging this mob as able to see sneakers */
	public final static int CAN_SEE_SNEAKERS=32;
    /** a bit setting, as from sensesMask(), flagging this mob as able to see bonus */
	public final static int CAN_SEE_BONUS=64;
    /** a bit setting, as from sensesMask(), flagging this mob as able to see dark */
	public final static int CAN_SEE_DARK=128;
    /** a bit setting, as from sensesMask(), flagging this mob as able to see infrared */
	public final static int CAN_SEE_INFRARED=256;
    /** a bit setting, as from sensesMask(), flagging this mob as unable to hear */
	public final static int CAN_NOT_HEAR=512;
    /** a bit setting, as from sensesMask(), flagging this mob as unable to move */
	public final static int CAN_NOT_MOVE=1024;
    /** a bit setting, as from sensesMask(), flagging this mob as unable to smell */
	public final static int CAN_NOT_SMELL=2048;
    /** a bit setting, as from sensesMask(), flagging this mob as unable to taste */
	public final static int CAN_NOT_TASTE=4096;
    /** a bit setting, as from sensesMask(), flagging this mob as unable to speak */
	public final static int CAN_NOT_SPEAK=8192;
    /** a bit setting, as from sensesMask(), flagging this mob as unable to breathe */
	public final static int CAN_NOT_BREATHE=16384;
    /** a bit setting, as from sensesMask(), flagging this mob as able to see their victims */
	public final static int CAN_SEE_VICTIM=32768;
    /** a bit setting, as from sensesMask(), flagging this mob as able to see metals*/
	public final static int CAN_SEE_METAL=65536;
	
	// sensemask stuff not applicable to mobs
    /** a bit setting, as from sensesMask(), flagging this item/room as being unlocatable */
	public final static int SENSE_UNLOCATABLE=1;
    /** a bit setting, as from sensesMask(), flagging this item/room */
	public final static int SENSE_ITEMNOMINRANGE=2;
    /** a bit setting, as from sensesMask(), flagging this item/room  */
	public final static int SENSE_ITEMNOMAXRANGE=4;
    /** a bit setting, as from sensesMask(), flagging this item/room as readable */
	public final static int SENSE_ITEMREADABLE=8;
    /** a bit setting, as from sensesMask(), flagging this item/room as ungettable */
	public final static int SENSE_ITEMNOTGET=16;
    /** a bit setting, as from sensesMask(), flagging this item/room as undroppable */
	public final static int SENSE_ITEMNODROP=32;
    /** a bit setting, as from sensesMask(), flagging this item/room as unremovable */
	public final static int SENSE_ITEMNOREMOVE=64;
    /** a bit setting, as from sensesMask(), flagging this item/room as having unseeable contents */
	public final static int SENSE_CONTENTSUNSEEN=128;
    /** a bit setting, as from sensesMask(), flagging this item/room as never being savable */
	public final static int SENSE_ITEMNEVERSAVED=256;
    /** a bit setting, as from sensesMask(), flagging this item/room as being unexplorable */
	public final static int SENSE_ROOMUNEXPLORABLE=512;
    /** a bit setting, as from sensesMask(), flagging this item/room as not allowing movement */
	public final static int SENSE_ROOMNOMOVEMENT=1024;
    /** a bit setting, as from sensesMask(), flagging this item/room as being unmappable */
	public final static int SENSE_ROOMUNMAPPABLE=2048;
    /** a bit setting, as from sensesMask(), flagging this item/room as being flagged for synchronization */
	public final static int SENSE_ROOMGRIDSYNC=4096;
    /** a bit setting, as from sensesMask(), flagging this item/room as being unruinable */
	public final static int SENSE_ITEMNORUIN=8192;
    /** a bit setting, as from sensesMask(), flagging this item/room  */
	public final static int SENSE_UNUSEDMASK15=16384;
    /** a bit setting, as from sensesMask(), flagging this item/room  */
	public final static int SENSE_UNUSEDMASK16=32768;
    /** a bit setting, as from sensesMask(), flagging this item/room  */
	public final static int SENSE_UNUSEDMASK17=65536;
	
    /** STAT codes list, indexed by the 2nd root of the various sensesMask() SENSE_ bitmasks */ 
	public static final String[] SENSE_CODES={
		"UNLOCATABLE",
		"ITEMNOMINRANGE",
		"ITEMNOMAXRANGE",
		"ITEMREADABLE",
		"ITEMNOTGET",
		"ITEMNODROP",
		"ITEMNOREMOVE",
		"CONTENTSUNSEEN",
		"ITEMNEVERSAVED",
		"ROOMUNEXPLORABLE",
		"ROOMNOMOVEMENT",
		"ROOMUNMAPPABLE",
		"ROOMGRIDSYNC",
		"ITEMNORUIN",
		"UNUSEDMASK15",
		"UNUSEDMASK16",
		"UNUSEDMASK17",
	};

		/** All bits in a Integer, cast into a long */
	public final static long ALLMASK=Integer.MAX_VALUE;
	
	// dispositions
    /** a bit setting, as from disposition(), flagging this object as not being seen */
	public final static int IS_NOT_SEEN=1;
    /** a bit setting, as from disposition(), flagging this object as being hidden */
	public final static int IS_HIDDEN=2;
    /** a bit setting, as from disposition(), flagging this object as being invisible */
	public final static int IS_INVISIBLE=4;
    /** a bit setting, as from disposition(), flagging this object as being evil */
	public final static int IS_EVIL=8;
    /** a bit setting, as from disposition(), flagging this object as being good */
	public final static int IS_GOOD=16;
    /** a bit setting, as from disposition(), flagging this object as sneaking */
	public final static int IS_SNEAKING=32;
    /** a bit setting, as from disposition(), flagging this object as being magical */
	public final static int IS_BONUS=64;
    /** a bit setting, as from disposition(), flagging this object as being in the dark */
	public final static int IS_DARK=128;
    /** a bit setting, as from disposition(), flagging this object as being a golem */
	public final static int IS_GOLEM=256;
    /** a bit setting, as from disposition(), flagging this object as being asleep */
	public final static int IS_SLEEPING=512;
    /** a bit setting, as from disposition(), flagging this object as sitting/crawling */
	public final static int IS_SITTING=1024;
    /** a bit setting, as from disposition(), flagging this object as flying */
	public final static int IS_FLYING=2048;
    /** a bit setting, as from disposition(), flagging this object as swimming */
	public final static int IS_SWIMMING=4096;
    /** a bit setting, as from disposition(), flagging this object as glowing */
	public final static int IS_GLOWING=8192;
    /** a bit setting, as from disposition(), flagging this object as climbing */
	public final static int IS_CLIMBING=16384;
    /** a bit setting, as from disposition(), flagging this object as falling */
	public final static int IS_FALLING=32768;
    /** a bit setting, as from disposition(), flagging this object as being a lightsource */
	public final static int IS_LIGHTSOURCE=65536;
    /** a bit setting, as from disposition(), flagging this object as being bound */
	public final static int IS_BOUND=131072;
    /** a bit setting, as from disposition(), flagging this object as being cloaked */
	public final static int IS_CLOAKED=262144;
    /** a bit setting, as from disposition(), flagging this object as being unsavable */
    public final static int IS_UNSAVABLE=524288;
    /** a bit setting, as from disposition(), flagging this object as being cataloged */
    public final static int IS_CATALOGED=1048576;

    /** STAT codes list, indexed by the 2nd root of the various sensesMask() CAN_SEE bitmasks */ 
	public static final String[] CAN_SEE_CODES={"CANNOTSEE",
											  "CANSEEHIDDEN",
											  "CANSEEINVISIBLE",
											  "CANSEEEVIL",
											  "CANSEEGOOD",
											  "CANSEESNEAKERS",
											  "CANSEEBONUS",
											  "CANSEEDARK",
											  "CANSEEINFRARED",
											  "CANNOTHEAR",
											  "CANNOTMOVE",
											  "CANNOTSMELL",
											  "CANNOTTASTE",
											  "CANNOTSPEAK",
											  "CANNOTBREATHE",
											  "CANSEEVICTIM",
											  "CANSEEMETAL"};
	
    /** Descriptions, indexed by the 2nd root of the various CAN_SEE sensesMask() bitmasks */ 
	public static final String[] CAN_SEE_DESCS={"Is Blind",
											 "Can see hidden",
											 "Can see invisible",
											 "Can see evil",
											 "Can see good",
											 "Can detect sneakers",
											 "Can see magic",
											 "Can see in the dark",
											 "Has infravision",
											 "Is Deaf",
											 "Is Paralyzed",
											 "Can not smell",
											 "Can not eat",
											 "Is Mute",
											 "Can not breathe",
											 "Can detect victims",
											 "Can detect metal"};
	
    /** Descriptive verbs, indexed by the 2nd root of the various CAN_SEE sensesMask() bitmasks */ 
	public static final String[] CAN_SEE_VERBS={"Causes Blindness",
											 "Allows see hidden",
											 "Allows see invisible",
											 "Allows see evil",
											 "Allows see good",
											 "Allows detect sneakers",
											 "Allows see magic",
											 "Allows darkvision",
											 "Allows infravision",
											 "Causes Deafness",
											 "Causes Paralyzation",
											 "Deadens smell",
											 "Disallows eating",
											 "Causes Mutemess",
											 "Causes choking",
											 "Allows detect victims",
											 "Allows detect metal"};
	
    /** STAT codes, indexed by the 2nd root of the various IS_ disposition() bitmasks */ 
	public static final String[] IS_CODES={"ISSEEN",
											"ISHIDDEN",
											"ISINVISIBLE",
											"ISEVIL",
											"ISGOOD",
											"ISSNEAKING",
											"ISBONUS",
											"ISDARK",
											"ISGOLEM",
											"ISSLEEPING",
											"ISSITTING",
											"ISFLYING",
											"ISSWIMMING",
											"ISGLOWING",
											"ISCLIMBING",
											"ISFALLING",
											"ISLIGHT",
											"ISBOUND",
											"ISCLOAKED",
                                            "ISUNSAVABLE",
                                            "ISCATALOGED"};
	
    /** Descriptions, indexed by the 2nd root of the various IS_ disposition() bitmasks */ 
	public static final String[] IS_DESCS= {"Is never seen",
											"Is hidden",
											"Is invisible",
											"Evil aura",
											"Good aura",
											"Is sneaking",
											"Is magical",
											"Is dark",
											"Is golem",
											"Is sleeping",
											"Is sitting",
											"Is flying",
											"Is swimming",
											"Is glowing",
											"Is climbing",
											"Is falling",
											"Is a light source",
											"Is binding",
											"Is Cloaked",
                                            "Is never saved",
                                            "Is cataloged"};
	
    /** Descriptive verbs, indexed by the 2nd root of the various IS_ disposition() bitmasks */ 
	public static final String[] IS_VERBS= {"Causes Nondetectability",
											"Causes hide",
											"Causes invisibility",
											"Creates Evil aura",
											"Creates Good aura",
											"Causes sneaking",
											"Creates magical aura",
											"Creates dark aura",
											"Creates golem aura",
											"Causes sleeping",
											"Causes sitting",
											"Allows flying",
											"Causes swimming",
											"Causes glowing aura",
											"Allows climbing",
											"Causes falling",
											"Causes a light source",
											"Causes binding",
											"Causes cloaking",
                                            "Causes disappearance",
                                            "Causes unsavability",
                                            "Created from a template"};
}
