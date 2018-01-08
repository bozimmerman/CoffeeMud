package com.planet_ink.coffee_mud.Items.interfaces;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

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
import com.planet_ink.coffee_mud.Items.interfaces.RawMaterial.CODES;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

/*
   Copyright 2010-2018 Bo Zimmerman

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

public interface Wearable extends Environmental
{
	/** a constant used in the Locale item search classes to filter on only items being worn */
	public static final Filterer<Environmental> FILTER_WORNONLY=new Filterer<Environmental>()
	{
		@Override
		public boolean passesFilter(Environmental obj)
		{
			if(obj instanceof Item)
				return !((Item)obj).amWearingAt(IN_INVENTORY);
			return false;
		}
	};
	/** a constant used in the Locale item search classes to filter on only items NOT being worn */
	public static final Filterer<Environmental> FILTER_UNWORNONLY=new Filterer<Environmental>()
	{
		@Override
		public boolean passesFilter(Environmental obj)
		{
			if(obj instanceof Item)
				return ((Item)obj).amWearingAt(IN_INVENTORY);
			return false;
		}
	};
	/** a constant used in the Locale item search classes to filter on only items being worn OR not being worn */
	@SuppressWarnings("unchecked")
	public static final Filterer<Environmental> FILTER_ANY=Filterer.ANYTHING;

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
	 * @return true if the item was wearable
	 */
	public boolean wearIfPossible(MOB mob);
	/**
	 * Using the canWear method, this method will put the item into a state of being worn
	 * on the given location only if it is practical for the given mob or player to wear
	 * this Item at the given location. The mob or player must have the item in his or her
	 * inventory first.
	 * @param mob the player or mob to put this item  on.
	 * @param wearCode the bitmap wear code for the location to attempt
	 * @return true if the item was wearable at the location
	 */
	public boolean wearIfPossible(MOB mob, long wearCode);
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
	 * @see Wearable#wearEvenIfImpossible(MOB)
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
	 * @see Wearable#wearAt(long)
	 * @see Wearable#wearIfPossible(MOB)
	 * @see Wearable#wearEvenIfImpossible(MOB)
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
	 * @see Wearable#wearAt(long)
	 * @see Wearable#wearIfPossible(MOB)
	 * @see Wearable#wearEvenIfImpossible(MOB)
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
	 * @see Wearable#rawProperLocationBitmap()
	 * @return whether this item is worn on all locations or any of the locations
	 */
	public boolean rawLogicalAnd();
	/**
	 * Sets flag which determines whether the rawProperLocationBitmap represents the fact that
	 * it is worn on ALL locations (value of true) or worn on any of the locations.
	 * @see Wearable#rawProperLocationBitmap()
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
	/** highest possible worn code value*/
	public static final long HIGHEST_WORN_CODE=1152921504606846976L;

	/**
	 * An array representing all of the  worn location bitmaps, except INVENTORY.  The
	 * array has worn location constants in the order in which they are presented to
	 * the user.
	 */
	public static final long[] DEFAULT_WORN_ORDER={
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
	public static final double[] DEFAULT_WORN_WEIGHTS={
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
	public static final double[][] DEFAULT_WORN_WEIGHT_POINTS={ // cloth, leather, metal
		{0.0,    0.0,    0.0}, //inventory
		{1.0,    2.0,    3.0}, //ON_HEAD
		{0.5,    1.0,    1.5}, //ON_NECK
		{3.0,    6.0,    9.0}, //ON_TORSO
		{1.5,    3.0,    4.5}, //ON_ARMS
		{0.5,    1.0,    1.5}, //ON_LEFT_WRIST
		{0.5,    1.0,    1.5}, //ON_RIGHT_WRIST
		{0.1,    0.2,    0.25},//ON_LEFT_FINGER
		{0.1,    0.2,    0.25},//ON_RIGHT_FINGER
		{1.0,    2.0,    3.0}, //ON_FEET
		{1.5,    3.0,    4.5}, //HELD
		{1.5,    3.0,    4.5}, //WIELD
		{0.5,    1.0,    1.5}, //ON_HANDS
		{0.0,    0.0,    0.0}, //FLOATING_NEARBY
		{0.5,    1.0,    1.5}, //ON_WAIST
		{1.5,    3.0,    4.5}, //ON_LEGS
		{0.1,    0.2,    0.25},//ON_EYES
		{0.1,    0.2,    0.25},//ON_EARS
		{1.0,    2.0,    3.0}, //ABOUT_BODY
		{0.1,    0.2,    0.25},//ON_MOUTH
		{1.0,    2.0,    3.0}  //ON_BACK
	};

	/**
	 * An array naming each of the worn location constants, in the order of their numeric value.
	 */
	public static final String[] DEFAULT_WORN_DESCS={
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
	public static final long[] DEFAULT_WORN_CODES={
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

	/**
	 * A Chart, indexed by WORN_CODE, showing the other parts dependent on that one.
	 */
	public final static long[] DEFAULT_WORN_DEPENDENCYGRID={
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
	/**
	 * Global location stat code data collector
	 * @author Bo Zimmermanimmerman
	 */
	public class CODES
	{
		public CODES()
		{
			super();
			final char c=Thread.currentThread().getThreadGroup().getName().charAt(0);
			if(insts==null)
				insts=new CODES[256];
			if(insts[c]==null)
				insts[c]=this;
			synchronized(this)
			{
				final String[][] addExtra = CMProps.instance().getStrsStarting("ADDWEARLOC_");
				final String[][] repExtra = CMProps.instance().getStrsStarting("REPLACEWEARLOC_");
				for(int i=0;i<Wearable.DEFAULT_WORN_CODES.length;i++)
					add(DEFAULT_WORN_DESCS[i], DEFAULT_WORN_DEPENDENCYGRID[i],
						DEFAULT_WORN_WEIGHTS[i], CMParms.indexOf(DEFAULT_WORN_ORDER,DEFAULT_WORN_CODES[i]),
						DEFAULT_WORN_WEIGHT_POINTS[i][0], DEFAULT_WORN_WEIGHT_POINTS[i][1], DEFAULT_WORN_WEIGHT_POINTS[i][2]);
				// now, stupid as it is, I have to fix the worn orders
				allCodesInOrder=Arrays.copyOf(Wearable.DEFAULT_WORN_ORDER, Wearable.DEFAULT_WORN_ORDER.length);

				for(int i=0;i<addExtra.length+repExtra.length;i++)
				{
					final String[] array = (i>=addExtra.length)?repExtra[i-addExtra.length]:addExtra[i];
					final boolean replace = i>=addExtra.length;
					final String stat = array[0].toLowerCase().trim().replace('_',' ');
					final String p=array[1];
					final List<String> V=CMParms.parseCommas(p, false);
					if(V.size()!=6)
					{
						Log.errOut("Wearable","Bad coffeemud.ini wear loc row (requires 6 elements, separated by ,): "+p);
						continue;
					}
					String type="ADD";
					int oldLocationCodeIndex=-1;
					if(replace)
					{
						final int idx=CMParms.indexOf(DEFAULT_WORN_DESCS, stat);
						if(idx>=0)
						{
							oldLocationCodeIndex=idx;
							type="REPLACE";
						}
						else
						{
							Log.errOut("Wearable","Bad replace worn loc in coffeemud.ini file: "+stat);
							continue;
						}
					}
					final String dependencyMaskStr=(V.get(0)).toLowerCase();
					long dependencyMask=0;
					final List<String> subLocs = CMParms.parseAny(dependencyMaskStr, '|', true);
					for(int s=0;s<subLocs.size();s++)
					{
						final int idx=CMParms.indexOf(DEFAULT_WORN_DESCS, subLocs.get(s).toLowerCase());
						if(idx>=0)
							dependencyMask|=DEFAULT_WORN_CODES[idx];
						else
							Log.errOut("Wearable","Bad dependency mask in coffeemud.ini file: "+subLocs.get(s).toLowerCase());
					}
					final double armorStrength=CMath.s_double(V.get(1));
					final int wornOrder=CMath.s_int(V.get(2));
					final double clothWeight=CMath.s_double(V.get(3));
					final double leatherWeight=CMath.s_double(V.get(4));
					final double metalWeight=CMath.s_double(V.get(5));
					if(type.equalsIgnoreCase("ADD"))
						add(stat, dependencyMask, armorStrength, wornOrder, clothWeight, leatherWeight, metalWeight);
					else
					if(type.equalsIgnoreCase("REPLACE")&&(oldLocationCodeIndex>=0))
						replace(oldLocationCodeIndex, stat, dependencyMask, armorStrength, wornOrder, clothWeight, leatherWeight, metalWeight);
				}
			}
		}

		private static CODES c(){ return insts[Thread.currentThread().getThreadGroup().getName().charAt(0)];}
		public static CODES c(byte c){return insts[c];}
		public static CODES instance()
		{
			CODES c=insts[Thread.currentThread().getThreadGroup().getName().charAt(0)];
			if(c==null)
				c=new CODES();
			return c;
		}

		public static void reset() 
		{
			insts[Thread.currentThread().getThreadGroup().getName().charAt(0)]=null;
			instance();
		}

		private static CODES[] insts=new CODES[256];

		private long[] allCodes = new long[0];
		private long[] allCodesInOrder=new long[0];
		private long[] dependencyMasks = new long[0];
		private double[][] wornWeightPoints = new double[0][0];
		private double[] armorWeights =  new double[0];
		private String[] descs = new String[0];
		private String[] updescs = new String[0];
		/**
		 * Returns total number of codes 0 - this-1
		 * @return total number of codes 0 - this-1
		 */
		public static int TOTAL() { return c().allCodes.length;}
		/**
		 * Returns total number of codes 0 - this-1
		 * @return total number of codes 0 - this-1
		 */
		public int total() { return allCodes.length;}

		/**
		 * Returns an array of the numeric codes for all locations
		 * @return an array of the numeric codes for all locations
		 */
		public static long[] ALL_ORDERED() { return c().allCodesInOrder;}
		/**
		 * Returns an array of the numeric codes for all locations
		 * @return an array of the numeric codes for all locations
		 */
		public long[] all_ordered() { return allCodesInOrder;}
		/**
		 * Returns an array of the numeric codes for all locations
		 * @return an array of the numeric codes for all locations
		 */
		public static long[] ALL() { return c().allCodes;}
		/**
		 * Returns an array of the numeric codes for all locations
		 * @return an array of the numeric codes for all locations
		 */
		public long[] all() { return allCodes;}
		/**
		 * Returns an the numeric codes of the indexes locations code
		 * @param x the indexed locations code
		 * @return an the numeric codes of the indexes locations code
		 */
		public static long GET(int x) { return c().allCodes[x];}
		/**
		 * Returns an the numeric codes of the indexes locations code
		 * @param x the indexed locations code
		 * @return an the numeric codes of the indexes locations code
		 */
		public long get(int x) { return allCodes[x];}
		/**
		 * Returns the index of the names locations, or -1
		 * @param rsc the resource name
		 * @return the index of the names locations, or -1
		 */
		public static int FINDDEX_ignoreCase(String rsc) { return c().findDex_ignoreCase(rsc);}

		/**
		 * Returns the index of the names locations, or -1
		 * @param rsc the resource name
		 * @return the index of the names locations, or -1
		 */
		public int findDex_ignoreCase(String rsc)
		{
			if(rsc==null)
				return -1;
			final int x=CMParms.indexOfIgnoreCase(descs, rsc.toLowerCase());
			if(x>=0)
				return x;
			return -1;
		}
		/**
		 * Returns the code of the names , or -1
		 * @param rsc the resource name
		 * @return the code of the names , or -1
		 */
		public static long FIND_ignoreCase(String rsc) { return c().find_ignoreCase(rsc);}

		/**
		 * Returns the code of the names , or -1
		 * @param rsc the resource name
		 * @return the code of the names , or -1
		 */
		public long find_ignoreCase(String rsc)
		{
			if(rsc==null)
				return -1;
			final int x=CMParms.indexOfIgnoreCase(descs, rsc.toLowerCase());
			if(x>=0)
				return allCodes[x];
			return -1;
		}
		/**
		 * Returns the index of the names locations, or -1
		 * @param rsc the resource name
		 * @return the index of the names locations, or -1
		 */
		public static int FINDDEX_endsWith(String rsc) 
		{ 
			return c().findDex_endsWith(rsc);
		}

		/**
		 * Returns the index of the names locations, or -1
		 * @param rsc the resource name
		 * @return the index of the names locations, or -1
		 */
		public int findDex_endsWith(String rsc)
		{
			if(rsc==null)
				return -1;
			final int x=CMParms.endsWith(descs, rsc.toLowerCase());
			if(x>=0)
				return x;
			return -1;
		}
		/**
		 * Returns the index of the names locations, or -1
		 * @param rsc the resource name
		 * @return the index of the names locations, or -1
		 */
		public static long FIND_endsWith(String rsc) { return c().find_endsWith(rsc);}

		/**
		 * Returns the index of the names locations, or -1
		 * @param rsc the resource name
		 * @return the index of the names locations, or -1
		 */
		public long find_endsWith(String rsc)
		{
			if(rsc==null)
				return -1;
			final int x=CMParms.endsWith(descs, rsc.toLowerCase());
			if(x>=0)
				return allCodes[x];
			return -1;
		}
		/**
		 * Returns a comma-delimited list of location names
		 * represented by the given worn code.
		 * @param wornCode the worn code
		 * @return the list of names
		 */
		public static String LISTED_CODES(long wornCode) 
		{ 
			return c().listedCodes(wornCode);
		}
		
		/**
		 * Returns a comma-delimited list of location names
		 * represented by the given worn code.
		 * @param wornCode the worn code
		 * @return the list of names
		 */
		public String listedCodes(long wornCode)
		{
			final StringBuffer buf=new StringBuffer("");
			for(int wornNum=1;wornNum<total();wornNum++)
			{
				if(CMath.bset(wornCode,allCodes[wornNum]))
					buf.append(descs[wornNum]+", ");
			}
			String buff=buf.toString();
			if(buff.endsWith(", "))
				buff=buff.substring(0,buff.length()-2).trim();
			return buff;
		}

		/**
		 * Returns whether the code is valid
		 * @param code the resource code
		 * @return whether the code is valid
		 */
		public static boolean IS_VALID(int code)
		{
			return (code>=0) && (CMParms.indexOf(c().allCodes, code)>=0);
		}
		/**
		 * Returns the names of the various locations
		 * @return the names of the various locations
		 */
		public static String[] NAMES() { return c().descs;}
		/**
		 * Returns the names of the various locations
		 * @return the names of the various locations
		 */
		public static String[] NAMESUP() { return c().updescs;}
		/**
		 * Returns the names of the various locations
		 * @return the names of the various locations
		 */
		public String[] names() { return descs;}
		/**
		 * Returns the names of the various locations
		 * @return the names of the various locations
		 */
		public String[] namesup() { return updescs;}
		/**
		 * Returns the name of the locations
		 * @param code the code
		 * @return the name of the locations
		 */
		public static String NAME(int code) { return c().descs[code];}
		/**
		 * Returns the name of the locations
		 * @param code the code
		 * @return the name of the locations
		 */
		public static String NAMEUP(int code) { return c().updescs[code];}
		/**
		 * Returns the name of the code
		 * @param code the code
		 * @return the name of the code
		 */
		public String name(int code) { return CMLib.lang().L(descs[code]); }
		/**
		 * Returns the name of the code
		 * @param code the code
		 * @return the name of the code
		 */
		public String nameup(int code) { return updescs[code]; }
		/**
		 * Returns the name of the locations
		 * @param code the code
		 * @return the name of the locations
		 */
		public static String NAME(long code) { return c().name(code);}
		/**
		 * Returns the name of the locations
		 * @param code the code
		 * @return the name of the locations
		 */
		public static String NAMEUP(long code) { return c().nameup(code);}
		/**
		 * Returns the name of the code
		 * @param code the code
		 * @return the name of the code
		 */
		public String name(long code) 
		{
			final int x=CMParms.indexOf(allCodes, code);
			if(x>=0)
				return descs[x];
			return "";
		}
		/**
		 * Returns the name of the code
		 * @param code the code
		 * @return the name of the code
		 */
		public String nameup(long code) 
		{
			final int x=CMParms.indexOf(allCodes, code);
			if(x>=0)
				return updescs[x];
			return "";
		}
		/**
		 * Returns the location dependency mask (or -1) of the various locations
		 * @return the location dependency mask (or -1) of the various locations
		 */
		public static long[] DEPENDENCY_MASKS() { return c().dependencyMasks;}
		/**
		 * Returns the location dependency mask (or -1) of the various locations
		 * @return the location dependency mask (or -1) of the various locations
		 */
		public long[] dependency_masks() { return dependencyMasks;}
		/**
		 * Returns an array representing the relative weight of items made for
		 * each of the several worn locations, in the same order as their numeric
		 * value. These weights are broken, in turn, into values for cloth,
		 * leather, and metal armors respectively.
		 * @return the doule double array
		 */
		public static double[][] MATERIAL_WEIGHT_POINTS() { return c().wornWeightPoints;}
		/**
		 * Returns an array representing the relative weight of items made for
		 * each of the several worn locations, in the same order as their numeric
		 * value. These weights are broken, in turn, into values for cloth,
		 * leather, and metal armors respectively.
		 * @return the doule double array
		 */
		public double[][] material_weight_points() { return wornWeightPoints;}
		/**
		 * Returns an array of the protective strength of each location
		 * @return an array of the protective strength of each location
		 */
		public static double[] LOCATION_STRENGTH_POINTS() { return c().armorWeights;}
		/**
		 * Returns an array of the protective strength of each location
		 * @return an array of the protective strength of each location
		 */
		public double[] location_strength_points() { return armorWeights;}

		/**
		 * Adds a new wear location.  I suspect this stuff works.  I also suspect
		 * it is not used, in favor of the hard coded stuff above.  
		 * TODO: this isn't implemented fully!
		 * @param desc the wear location description
		 * @param dependencyMask the dependency locations
		 * @param armorStrength armor strength factor
		 * @param wornOrder the worn order
		 * @param clothWeight the cloth weight factor
		 * @param leatherWeight the leather weight factor
		 * @param metalWeight the metal weight factor
		 */
		public synchronized void add(String desc, long dependencyMask, double armorStrength, int wornOrder,
									 double clothWeight, double leatherWeight, double metalWeight)
		{
			if(allCodes.length>61)
				return;
			long newCode = 0;
			if(allCodes.length>0)
				newCode = (long)1<<(allCodes.length-1);
			allCodes=Arrays.copyOf(allCodes, allCodes.length+1);
			allCodes[allCodes.length-1]=newCode;
			descs=Arrays.copyOf(descs, descs.length+1);
			descs[descs.length-1]=desc;
			updescs=Arrays.copyOf(updescs, updescs.length+1);
			updescs[updescs.length-1]=desc.toUpperCase();
			dependencyMasks=Arrays.copyOf(dependencyMasks, dependencyMasks.length+1);
			dependencyMasks[dependencyMasks.length-1]=dependencyMask;
			armorWeights=Arrays.copyOf(armorWeights, armorWeights.length+1);
			armorWeights[armorWeights.length-1]=armorStrength;
			wornWeightPoints=Arrays.copyOf(wornWeightPoints, wornWeightPoints.length+1);
			final double[] newRow={clothWeight,leatherWeight,metalWeight};
			wornWeightPoints[wornWeightPoints.length-1]=newRow;
			insertInOrder(newCode,wornOrder);
		}

		/**
		 * Insert the new code
		 * @param newCode the worn code
		 * @param wornOrder the worn orderr
		 */
		private void insertInOrder(long newCode, int wornOrder)
		{
			if(wornOrder<0)
				return;
			final Vector<Long> V= new Vector<Long>();
			for (final long element : allCodesInOrder)
				V.add(Long.valueOf(element));
			V.remove(Long.valueOf(newCode));
			if(wornOrder>=V.size())
				V.add(Long.valueOf(newCode));
			else
				V.insertElementAt(Long.valueOf(newCode), wornOrder);
			final long[] newCodesInOrder = new long[V.size()];
			for(int l=0;l<V.size();l++)
				newCodesInOrder[l]=V.elementAt(l).longValue();
			allCodesInOrder=newCodesInOrder;
		}

		/**
		 * replaces an existing wear location.  I suspect this stuff works.  I also suspect
		 * it is not used, in favor of the hard coded stuff above.  
		 * TODO: this isn't implemented fully!
		 * @param codeIndex the index of the location to replace
		 * @param desc the wear location description
		 * @param dependencyMask the dependency locations
		 * @param armorStrength armor strength factor
		 * @param wornOrder the worn order
		 * @param clothWeight the cloth weight factor
		 * @param leatherWeight the leather weight factor
		 * @param metalWeight the metal weight factor
		 */
		public synchronized void replace(int codeIndex, String desc, long dependencyMask, double armorStrength, int wornOrder,
										  double clothWeight, double leatherWeight, double metalWeight)
		{
			if(codeIndex<=0)
				return;
			descs[codeIndex]=desc;
			updescs[codeIndex]=desc.toUpperCase();
			dependencyMasks[codeIndex]=dependencyMask;
			armorWeights[codeIndex]=armorStrength;
			final double[] newRow={clothWeight,leatherWeight,metalWeight};
			wornWeightPoints[codeIndex]=newRow;
			insertInOrder(allCodes[codeIndex],wornOrder);
		}
	}
}
