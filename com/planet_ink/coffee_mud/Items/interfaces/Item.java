package com.planet_ink.coffee_mud.Items.interfaces;
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
   Copyright 2000-2008 Bo Zimmerman

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
 * The interface for all common items, and as a base for RawMaterial, armor, weapons, etc.
 *
 * @author Bo Zimmerman
 *
 */
/**
 * @author Owner
 *
 */
/**
 * @author Owner
 *
 */
public interface Item extends Environmental, Rider
{
    /** a constant used in the Locale item search classes to filter on only items being worn */
	public static int WORNREQ_WORNONLY=0;
    /** a constant used in the Locale item search classes to filter on only items NOT being worn */
	public static int WORNREQ_UNWORNONLY=1;
    /** a constant used in the Locale item search classes to filter on only items being worn OR not being worn */
	public static int WORNREQ_ANY=2;

    /** 
     * Where the item is located.  Either null for
     * plain site (or contained on person), or will
     * point to the container object 
     * @return The item in which  it is contained, or null.
     */
	public Item container();
    /** 
     * Change  the container where the item is located.  Either null for
     * plain site (or contained on person), or will
     * point to the Container item. 
     * @see Container
     * @param newLocation Container item in which this item is contained.
     */
	public void setContainer(Item newLocation);
    /**
     * If an item is in a container, which is in a container, etc, this will
     * return the "highest" or ultimate container in which this item is located.
     * If an item is in a container which is in plain view, it will simply 
     * return container().  As in container(), null means the item is in plain view.
     * @see Container
     * @return the highest level container in which  this item is found.
     */
	public Item ultimateContainer();
    /**
     * This method basically calls setContainer(null), and then removes this item
     * from its owner().  It effectively removes the item from the map.  This is 
     * generally assumed to be a temporary condition.  To really destroy the item
     * permanently, the destroy() method is used.  The unWear() method is also called.
     * @see Container
     */
    public void removeFromOwnerContainer();
    
    /**
     * How many items this Item object represents.  When an item is Packaged, this
     * method will return a number greater than 1, otherwise it always returns 1.
     * @return the number of items represented by this object.
     */
    public int numberOfItems();

    /**
     * This method returns the calculated and expanded description of the properties
     * of the item as would be discovered through the Identify spell.  It starts with
     * its rawSecretIdentity() and adds to it any strings which the Ability objects
     * contained in the Items effects list would generate.  An empty string means
     * the item has no secret properties per se.
     * @return a displayable string describing the secret properties of the item.
     */
	public String secretIdentity();
    /**
     * This method returns those secret properties of the item which are entered directly
     * by the builder when the item is designed.  It is the string saved to the database, 
     * and is used by the secretIdentity() method to construct a full secret description
     * of the Item. 
     * @return the string entered by the builder as the item secret properties or name.
     */
	public String rawSecretIdentity();
    /**
     * This method is used to change the string returned by rawSecretIdentity.  This string
     * is saved to the database as the items secret properties desctiption.  The secretIdentity
     * method uses this string to construct its full description.
     * @param newIdentity the secret properties of this item.  Empty string means it has none.
     */
	public void setSecretIdentity(String newIdentity);

    /**
     * Whether the usesRemaining() number above is used to determine the percentage health of
     * the item.  If this method returns false, then health or condition is irrelevant to this
     * Item. If true is returned, then usesRemaining is a number from 100 to 0,  where 100 means
     * perfect condition, and 0 means imminent disintegration.
     * @return whether this item has a valid condition
     */
	public boolean subjectToWearAndTear();
	/** 
     * Uses remaining is a general use numeric value whose meaning differs for different Item
     * types.  For instance, Wands use it to represent charges, Weapons and Armor use it to
     * represent Condition, Ammunition uses it to represent quantity, etc.
     * @return the general numeric value of this field. 
     */
	public int usesRemaining();
    /**
     * Sets the uses remaining field, which is a general numeric value whose meaning differs
     * for different Item types.  See usesRemaining() method above for more information.
     * @param newUses a new  general numeric value for this field.
     */
	public void setUsesRemaining(int newUses);
	
    /**
     * If this Item is current Ticking due to its having Behaviors or other properties which
     * might grant it the ability to Tick, this method will cause that ticking to cease and
     * desist.  This means that it will lose its periodic thread calls to its tick() method.
     */
    public void stopTicking();
    /** 
     * The default value of the item, represented in the base CoffeeMud currency.  This
     * method starts with baseGoldValue, which is a user-entered value, and adjusts
     * according to magical enhancements and the condition of the  item.
     * @return the adjusted value of the item in the base currency.
     */
	public int value();
    /**
     * The user/builder-entered value of the item, represented in base CoffeeMud currency.
     * It is used as a basis for the value returned by the value() method.
     * @return the raw user-entered value of item.
     */    
	public int baseGoldValue();
    /**
     * Changes the base value of the item, represented in base CoffeeMud currency.
     * The value is saved to the database, and is used by the value() method as a basis.
     * @param newValue the new raw value of the item
     */
	public void setBaseValue(int newValue);
	
    /**
     * The resource code representing the material out of which this item is principally made.
     * The resource codes are composed of an integer where the highest order bits represent
     * the basic material type, and the lower order bits represent the specific material type.
     * These codes are defined in RawMaterial.
     * @see RawMaterial
     * @return the RawMaterial code describing what this item is made of.
     */
	public int material();
    /**
     * Sets the resource code representing the material out of which this item is principally made.
     * The resource codes are composed of an integer where the highest order bits represent
     * the basic material type, and the lower order bits represent the specific material type.
     * These codes are defined in RawMaterial interface.
     * @see RawMaterial
     */
	public void setMaterial(int newValue);

    /**
     * If the IS_READABLE flag is set for the envStats().sensesMask() flag on this item, then this
     * method will be consulted as representing any writing on the Item.  Typically accessed with
     * the READ command.  This flag is defined in the EnvStats interface
     * @see com.planet_ink.coffee_mud.Common.interfaces.EnvStats
     * @return the readable text on the item.
     */
	public String readableText();
    /**
     * Changes the text that is written on the item.  This method does NOT set the IS_READABLE flag
     * on the envStats().sensesMask flag.  This flag is defined in the EnvStats interface
     * @see com.planet_ink.coffee_mud.Common.interfaces.EnvStats
     * @param text what is written on the item.  Empty string means nothing.
     */
	public void setReadableText(String text);
	
    /** 
     * Can test where, if anywhere, an item is being worn.  The value may be 0 to see if the item
     * is not being worn (since 0 means inventory) or a combination of 1 or more of the worn codes
     * listed in the Item interface.
     * @see Item
     * @param wornCode either 0, or one or more worn codes 
     * @return whether this item is being worn on the wornCode location
     */
    public boolean amWearingAt(long wornCode);
    /**
     * Whether this item is designated as being wearable on the given worn codes defined in the
     * Item interface.  This method does not care about the practicality of wearing for a given
     * mob (in other words, whether an item is already being worn on the location is not checked).
     * @see Item
     * @param wornCode the worn code to check, as defined in the Item interface
     * @return whether this item is allowed to be worn on that location
     */
    public boolean fitsOn(long wornCode);
    /**
     * Returns a bitmap of all the locations where the given mob is practically unable to wear
     * an item.  The method compares items the mob is currently wearing to all possible wear
     * locations, including locations where the mob can't wear things, and returns a bitmap
     * representing all of those unwearable locations.  The bitmap is constructed using the
     * bitmap constants defined in the Item interface.
     * @see Item
     * @param mob the player or mob to evaluate for unwearable locations
     * @return a bitmap made up of unwearable location bitmaps, from constants in Item interface
     */
    public long whereCantWear(MOB mob);
    /**
     * Returns whether it is practical for the given mob to wear this Item on the given worn
     * location as given by the wornCode.  The method checks for existing limbs and wear
     * locations, and for items already being worn by the mob, and finally determines if the
     * worn location given by the wornCode is among the available slots.
     * @see Item
     * @param mob the player or mob being evaluated
     * @param wornCode the worn location to check and see if this Item can be worn there.
     * @return whether this item can be worn by the given player at the given wornCode
     */
    public boolean canWear(MOB mob, long wornCode);
    /**
     * Using the canWear method, this method will put the item into a state of being worn
     * only if it is practical for the given mob or player to wear this Item. The mob or
     * player must have the item in his or her inventory first.
     * @param mob the player or mob to put this item  on.
     */
    public void wearIfPossible(MOB mob);
    /**
     * Puts this item into a state of being worn regardless of whether it is practical for
     * the given mob to wear it -- for instance, even if an item is already being worn where
     * this item wants to be worn, or if the player has no such limbs to wear this item.  None
     * of that matter to this method.  The item must be in the mobs inventory first.
     * @param mob the player or mob 
     */
    public void wearEvenIfImpossible(MOB mob);
    /**
     * This method is similar to the wearEvenIfImpossible method method above, except that 
     * it does not inspect this item for allowed wearable locations, but always puts the
     * item on the wear location represented by the given wornCode bitmap.  This bitmap
     * is made up of constants from the Item interface.
     * @see Item
     * @see Item#wearEvenIfImpossible(MOB)
     * @param wornCode the bitmap from Item interface constants used
     */
    public void wearAt(long wornCode);
    /**
     * Removes this item from a state of being worn, and puts it back into the mob or
     * player inventory.  setContainer(null) may still need be called to make the
     * item visible to the players inventory.
     */
    public void unWear();
    /**
     * Returns the bitmap representing where this item is presently being worn by
     * the mob or player who has the item in his or inventory.  The value returned
     * by this method is set by the wear* methods defined above.  The bitmap is
     * made up of a combination of the wear location constants from the Item
     * interface.
     * @see Item
     * @see Item#wearAt(long)
     * @see Item#wearIfPossible(MOB)
     * @see Item#wearEvenIfImpossible(MOB)
     * @return the current worn code for this item
     */
    public long rawWornCode();
    /**
     * Sets the bitmap representing where this item is presently being worn by
     * the mob or player who has the item in his or inventory.  The value set
     * by this method is set by the wear* methods defined above.  The bitmap is
     * made up of a combination of the wear location constants from the Item
     * interface.
     * @see Item
     * @see Item#wearAt(long)
     * @see Item#wearIfPossible(MOB)
     * @see Item#wearEvenIfImpossible(MOB)
     * @param newValue the current worn code for this item
     */
    public void setRawWornCode(long newValue);
    /**
     * Returns the builder-defined bitmap made from a logical combination of the worn
     * location constants from the Item interface.  When combined with the rawLogicalAnd()
     * method, it says whether this item is wearable on all of the locations defined by this
     * bitmap at the same time or whether this item is wearable on any of the locations defined
     * by this bitmap.
     * @see Item
     * @return the worn location bitmap defined by the Item interface.
     */
    public long rawProperLocationBitmap();
    /**
     * Sets the builder-defined bitmap made from a logical combination of the worn
     * location constants from the Item interface.  When combined with the rawLogicalAnd()
     * method, it says whether this item is wearable on all of the locations defined by this
     * bitmap at the same time or whether this item is wearable on any of the locations defined
     * by this bitmap.
     * @see Item
     * @param newValue the worn location bitmap defined by the Item interface.
     */
    public void setRawProperLocationBitmap(long newValue);
    /**
     * Flag which determines whether the rawProperLocationBitmap represents the fact that
     * it is worn on ALL locations (value of true) or worn on any of the locations.
     * @see Item#rawProperLocationBitmap()  
     * @return whether this item is worn on all locations or any of the locations
     */
    public boolean rawLogicalAnd();
    /**
     * Sets flag which determines whether the rawProperLocationBitmap represents the fact that
     * it is worn on ALL locations (value of true) or worn on any of the locations.
     * @see Item#rawProperLocationBitmap()  
     * @param newAnd whether this item is worn on all locations or any of the locations
     */
    public void setRawLogicalAnd(boolean newAnd);
    /**
     * compares whether this item is allowed to be worn on the same locations as the
     * given item.
     * @param toThis the item to compare this items allowed worn locations to
     * @return whether this item is allowed to be worn on the same place as the param
     */
    public boolean compareProperLocations(Item toThis);
    /**
     * For a normal item, this method returns the same as envStats().weight().  For
     * a Container, it returns the weight of the container plus the recursive weight
     * of all items in the container.
     * @see Container
     * @return the total weight of the item and any possible contents.
     */
    public int recursiveWeight();
    /**
     * The Room or MOB representing where this item is located.  Containers are handled
     * by another pointer, container(), so those two methods be used together to determine
     * where a given item is.
     * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB
     * @see com.planet_ink.coffee_mud.Items.interfaces.Item
     * @return the mob or room where the item is located
     */
    public Environmental owner();
    /**
     * Sets the Room or MOB representing where this item is located.  Containers are handled
     * by another pointer, container(), so those two methods be used together to determine
     * where a given item is.  This method is called by the addInventory method on mobs
     * and the addItem interface on Rooms.  Alone, this method is insufficient to properly
     * determine an items location, so one of the two above should be called instead.
     * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB#addInventory(Item)
     * @see com.planet_ink.coffee_mud.Locales.interfaces.Room#addItem(Item)
     * @param E the mob or room where the item is located
     */
    public void setOwner(Environmental E);
    
	/**
	 * Sets the internal database id for mapping back to tables.
	 * Used principally by code that needs to do an UPDATE on a 
	 * particular mob, so it can be traced back to its row in 
	 * the appropriate table.
	 * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB#databaseID()
	 * @param id the database id
	 */
	public void setDatabaseID(String id);
	
	/**
	 * Gets the internal database id for mapping back to tables.
	 * Used principally by code that needs to do an UPDATE on a 
	 * particular mob, so it can be traced back to its row in 
	 * the appropriate table.
	 * @see com.planet_ink.coffee_mud.MOBS.interfaces.MOB#setDatabaseID(String)
	 * @return the database id
	 */
	public String databaseID();
    
	/** worn code constant, representing  being unworn altogether */
	public static final long IN_INVENTORY=0;
    /** worn code constant, worn on the head */
	public static final long WORN_HEAD=1;
    /** worn code constant, worn on the neck*/
	public static final long WORN_NECK=2;
    /** worn code constant, worn on the torso*/
	public static final long WORN_TORSO=4;
    /** worn code constant, worn on both arms*/
	public static final long WORN_ARMS=8;
    /** worn code constant, worn on the left wrist*/
	public static final long WORN_LEFT_WRIST=16;
    /** worn code constant, worn on the right wrist*/
	public static final long WORN_RIGHT_WRIST=32;
    /** worn code constant, worn on the left finger*/
	public static final long WORN_LEFT_FINGER=64;
    /** worn code constant, worn on the right finger*/
	public static final long WORN_RIGHT_FINGER=128;
    /** worn code constant, worn on the feet*/
	public static final long WORN_FEET=256;
    /** worn code constant, worn on the held position*/
	public static final long WORN_HELD=512;
    /** worn code constant, worn on the wield position*/
	public static final long WORN_WIELD=1024;
    /** worn code constant, worn on both hands*/
	public static final long WORN_HANDS=2048;
    /** worn code constant, floats nearby*/
	public static final long WORN_FLOATING_NEARBY=4096;
    /** worn code constant, worn on the waist*/
	public static final long WORN_WAIST=8192;
    /** worn code constant, worn on the legs*/
	public static final long WORN_LEGS=16384;
    /** worn code constant, worn on the eye area of the face*/
	public static final long WORN_EYES=32768;
    /** worn code constant, worn on or around the ears*/
	public static final long WORN_EARS=65536;
    /** worn code constant, worn about the shoulders and the whole body*/
	public static final long WORN_ABOUT_BODY=131072;
    /** worn code constant, worn on or in the mouth*/
	public static final long WORN_MOUTH=262144;
    /** worn code constant, worn on the back*/
	public static final long WORN_BACK=524288;
	
    /**
     * An array representing all of the  worn location bitmaps, except INVENTORY.  The
     * array has worn location constants in the order in which they are presented to
     * the user.
     */
	public static final long[] WORN_ORDER={
		WORN_HEAD,
		WORN_EYES,
		WORN_EARS,
		WORN_MOUTH,
		WORN_NECK,
		WORN_BACK,
		WORN_ABOUT_BODY,
		WORN_TORSO,
		WORN_ARMS,
		WORN_LEFT_WRIST,
		WORN_RIGHT_WRIST,
		WORN_HANDS,
		WORN_LEFT_FINGER,
		WORN_RIGHT_FINGER,
		WORN_WAIST,
		WORN_LEGS,
		WORN_FEET,
        WORN_FLOATING_NEARBY,
        WORN_WIELD,
        WORN_HELD,
	};
	
    /**
     * An array representing the armor protective strength of the worn location bitmaps, in the same order as their
     * numeric value. 
     */
	public static final double[] WORN_WEIGHTS={
		0.0, //inventory
		1.0, //ON_HEAD
		0.5, //ON_NECK
		3.0, //ON_TORSO
		1.0, //ON_ARMS
		0.5, //ON_LEFT_WRIST
		0.5, //ON_RIGHT_WRIST
		0.25, //ON_LEFT_FINGER
		0.25, //ON_RIGHT_FINGER
		0.5, //ON_FEET
		1.5, //HELD
		0.5, //WIELD
		0.5, //ON_HANDS
		0.01, //FLOATING_NEARBY
		1.0, //ON_WAIST
		2.0, //ON_LEGS
		0.25, //ON_EYES
		0.25, //ON_EARS
		1.5, //ABOUT_BODY
		0.01, //ON_MOUTH
		1.0  //ON_BACK
	};
	
    /**
     * An array representing the relative weight of items made for each of the several worn locations, in
     * the same order as their numeric value. These weights are broken, in turn, into values for cloth,  
     * leather, and metal armors respectively.
     */
	public static final double[][] WORN_WEIGHT_POINTS={ // cloth, leather, metal
		{0.0,	0.0,	0.0}, //inventory
		{1.0,	2.0,	3.0}, //ON_HEAD
		{0.5,	1.0,	1.5}, //ON_NECK
		{3.0,	6.0,	9.0}, //ON_TORSO
		{1.5,	3.0,	4.5}, //ON_ARMS
		{0.5,	1.0,	1.5}, //ON_LEFT_WRIST
		{0.5,	1.0,	1.5}, //ON_RIGHT_WRIST
		{0.1,	0.2,	0.25},//ON_LEFT_FINGER
		{0.1,	0.2,	0.25},//ON_RIGHT_FINGER
		{1.0,	2.0,	3.0}, //ON_FEET
		{1.5,	3.0,	4.5}, //HELD
		{1.5,	3.0,	4.5}, //WIELD
		{0.5,	1.0,	1.5}, //ON_HANDS
		{0.0,	0.0,	0.0}, //FLOATING_NEARBY
		{0.5,	1.0,	1.5}, //ON_WAIST
		{1.5,	3.0,	4.5}, //ON_LEGS
		{0.1,	0.2,	0.25},//ON_EYES
		{0.1,	0.2,	0.25},//ON_EARS
		{1.0,	2.0,	3.0}, //ABOUT_BODY
		{0.1,	0.2,	0.25},//ON_MOUTH
		{1.0,	2.0,	3.0}  //ON_BACK
	};
    
    /**
     * An array naming each of the worn location constants, in the order of their numeric value.
     */
	public static final String[] WORN_DESCS={
		"inventory",
		"head",
		"neck",
		"torso",
		"arms",
		"left wrist",
		"right wrist",
		"left finger",
		"right finger",
		"feet",
		"held",
		"wield",
		"hands",
		"floating nearby",
		"waist",
		"legs",
		"eyes",
		"ears",
		"body",
		"mouth",
		"back"
    };
	
    /** An array containing all of the worn codes,in the order of their numeric value. */
	public static final long[] WORN_CODES={
	    IN_INVENTORY,
		WORN_HEAD,
        WORN_NECK,
        WORN_TORSO,
        WORN_ARMS,
        WORN_LEFT_WRIST,
        WORN_RIGHT_WRIST,
        WORN_LEFT_FINGER,
        WORN_RIGHT_FINGER,
        WORN_FEET,
        WORN_HELD,
        WORN_WIELD,
        WORN_HANDS,
        WORN_FLOATING_NEARBY,
		WORN_WAIST,
        WORN_LEGS,
        WORN_EYES,
        WORN_EARS,
        WORN_ABOUT_BODY,
        WORN_MOUTH,
        WORN_BACK,
	};
	
	public final static long[] WORN_DEPENDENCYGRID={
	    -1, // inventory
		WORN_EYES|WORN_EARS|WORN_MOUTH, // head
        WORN_HEAD|WORN_EYES|WORN_EARS|WORN_MOUTH, // neck
        WORN_HEAD|WORN_NECK|WORN_ARMS|WORN_LEFT_WRIST|WORN_RIGHT_WRIST|WORN_LEFT_FINGER|WORN_RIGHT_FINGER|WORN_FEET|WORN_HELD|WORN_WIELD|WORN_HANDS|WORN_WAIST|WORN_LEGS|WORN_EYES|WORN_EARS|WORN_MOUTH|WORN_BACK,// torso
        WORN_LEFT_WRIST|WORN_RIGHT_WRIST|WORN_LEFT_FINGER|WORN_RIGHT_FINGER|WORN_HELD|WORN_WIELD|WORN_HANDS, // arms
        WORN_LEFT_FINGER|WORN_HANDS|WORN_HELD|WORN_WIELD,
        WORN_RIGHT_FINGER|WORN_HANDS|WORN_HELD|WORN_WIELD,
        -1,
        -1,
        -1,
        -1,
        -1,
        WORN_HELD|WORN_WIELD,
        -1,
		WORN_WAIST,
		WORN_FEET,
        -1,
        -1,
        -1,
        -1,
        -1,
	};
	
	//WORN_HEAD|WORN_NECK|WORN_TORSO|WORN_ARMS|WORN_LEFT_WRIST|WORN_RIGHT_WRIST|WORN_LEFT_FINGER|WORN_RIGHT_FINGER|WORN_FEET|WORN_HELD|WORN_WIELD|WORN_HANDS|WORN_WAIST|WORN_LEGS|WORN_EYES|WORN_EARS|WORN_MOUTH|WORN_BACK
}
