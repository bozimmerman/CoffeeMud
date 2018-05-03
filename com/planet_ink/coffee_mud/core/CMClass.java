package com.planet_ink.coffee_mud.core;
import com.planet_ink.coffee_mud.WebMacros.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.Function;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Modifier;
import java.net.URL;

import org.mozilla.javascript.*;
import org.mozilla.javascript.optimizer.*;

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
 * The core class loader, but more importantly, the core object template manager
 * for the whole mud.  Classes are grouped by their core interfaces, allowing them
 * to have short "ID" names as referents.  Classes are loaded and initialized from the
 * class loader and then kept as template objects, with newInstances created on demand (or
 * simply returned as the template, in cases where the objects are shared).
 * @author Bo Zimmerman
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class CMClass extends ClassLoader
{
	protected static boolean						debugging		= false;
	protected static volatile long					lastUpdateTime	= System.currentTimeMillis();
	protected static final Map<String, Class<?>>	classes			= new Hashtable<String, Class<?>>();

	private static CMClass[] clss=new CMClass[256];
	/**
	 * Creates a new instance of the class loader, updating the thread-group ref if necessary.
	 */
	public CMClass()
	{
		super();
		final char c=Thread.currentThread().getThreadGroup().getName().charAt(0);
		if(clss==null)
			clss=new CMClass[256];
		if(clss[c]==null)
			clss[c]=this;
	}
	
	/**
	 * Creates and returns a new CMClass object for the current calling thread
	 * @return a new CMClass object for the current calling thread
	 */
	public static final CMClass initialize()
	{ 
		return new CMClass(); 
	}

	/**
	 * Returns the CMClass instance tied to this particular thread group, or null if not yet created.
	 * @return the CMClass instance tied to this particular thread group, or null if not yet created.
	 */
	private static CMClass c()
	{ 
		return clss[Thread.currentThread().getThreadGroup().getName().charAt(0)];
	}
	
	/**
	 * Returns the CMClass instance tied to the given thread group, or null if not yet created.
	 * @param c the code for the thread group to return (0-255)
	 * @return the CMClass instance tied to the given thread group, or null if not yet created.
	 */
	public static CMClass c(byte c)
	{
		return clss[c];
	}
	
	/**
	 * Returns the CMClass instance tied to this particular thread group, or null if not yet created.
	 * @return the CMClass instance tied to this particular thread group, or null if not yet created.
	 */
	public static CMClass instance()
	{
		return c();
	}

	private static boolean[] classLoaderSync={false};

	public static enum CMObjectType
	{
	/** stat constant for race type objects */
	RACE("com.planet_ink.coffee_mud.Races.interfaces.Race"),
	/** stat constant for char class type objects */
	CHARCLASS("com.planet_ink.coffee_mud.CharClasses.interfaces.CharClass"),
	/** stat constant for mob type objects */
	MOB("com.planet_ink.coffee_mud.MOBS.interfaces.MOB"),
	/** stat constant for ability type objects */
	ABILITY("com.planet_ink.coffee_mud.Abilities.interfaces.Ability"),
	/** stat constant for locale/room type objects */
	LOCALE("com.planet_ink.coffee_mud.Locales.interfaces.Room"),
	/** stat constant for exit type objects */
	EXIT("com.planet_ink.coffee_mud.Exits.interfaces.Exit"),
	/** stat constant for item type objects */
	ITEM("com.planet_ink.coffee_mud.Items.interfaces.Item"),
	/** stat constant for behavior type objects */
	BEHAVIOR("com.planet_ink.coffee_mud.Behaviors.interfaces.Behavior"),
	/** stat constant for clan type objects */
	CLAN("com.planet_ink.coffee_mud.core.interfaces.Clan"),
	/** stat constant for weapon type objects */
	WEAPON("com.planet_ink.coffee_mud.Items.interfaces.Weapon"),
	/** stat constant for armor type objects */
	ARMOR("com.planet_ink.coffee_mud.Items.interfaces.Armor"),
	/** stat constant for misc magic type objects */
	MISCMAGIC("com.planet_ink.coffee_mud.Items.interfaces.MiscMagic"),
	/** stat constant for area type objects */
	AREA("com.planet_ink.coffee_mud.Areas.interfaces.Area"),
	/** stat constant for command type objects */
	COMMAND("com.planet_ink.coffee_mud.Commands.interfaces.Command"),
	/** stat constant for clan items type objects */
	CLANITEM("com.planet_ink.coffee_mud.Items.interfaces.ClanItem"),
	/** stat constant for misc tech type objects */
	TECH("com.planet_ink.coffee_mud.Items.interfaces.Technical"),
	/** stat constant for misc tech type objects */
	COMPTECH("com.planet_ink.coffee_mud.Items.interfaces.TechComponent"),
	/** stat constant for misc tech type objects */
	SOFTWARE("com.planet_ink.coffee_mud.Items.interfaces.Software"),
	/** stat constant for webmacros type objects */
	WEBMACRO("com.planet_ink.coffee_mud.WebMacros.interfaces.WebMacro"),
	/** stat constant for common type objects */
	COMMON("com.planet_ink.coffee_mud.Common.interfaces.CMCommon"),
	/** stat constant for library type objects */
	LIBRARY("com.planet_ink.coffee_mud.Libraries.interfaces.CMLibrary");

		public final String ancestorName; // in meters
		CMObjectType(String ancestorName)
		{
			this.ancestorName = ancestorName;
		}
	}

	/** collection of all object types that are classified as "items" of one sort or another */
	public static final CMObjectType[] OBJECTS_ITEMTYPES = new CMObjectType[]
	{
		CMObjectType.MISCMAGIC,
		CMObjectType.ITEM,
		CMObjectType.ARMOR,
		CMObjectType.CLANITEM,
		CMObjectType.MISCMAGIC,
		CMObjectType.TECH,
		CMObjectType.COMPTECH,
		CMObjectType.SOFTWARE,
		CMObjectType.WEAPON
	};

	/** static int for the web macro object with the longest name, used for web optimization */
	public static int longestWebMacro=-1;

	protected Hashtable<String, CMCommon>	common			= new Hashtable<String, CMCommon>();
	protected XVector<Race>					races			= new XVector<Race>();
	protected XVector<CharClass>			charClasses		= new XVector<CharClass>();
	protected XVector<MOB>					MOBs			= new XVector<MOB>();
	protected XVector<Ability>				abilities		= new XVector<Ability>();
	protected XVector<Room>					locales			= new XVector<Room>();
	protected XVector<Exit>					exits			= new XVector<Exit>();
	protected XVector<Item>					items			= new XVector<Item>();
	protected XVector<Behavior>				behaviors		= new XVector<Behavior>();
	protected XVector<Weapon>				weapons			= new XVector<Weapon>();
	protected XVector<Armor>				armor			= new XVector<Armor>();
	protected XVector<MiscMagic>			miscMagic		= new XVector<MiscMagic>();
	protected XVector<Technical>			tech			= new XVector<Technical>();
	protected XVector<ClanItem>				clanItems		= new XVector<ClanItem>();
	protected XVector<Area>					areaTypes		= new XVector<Area>();
	protected XVector<Command>				commands		= new XVector<Command>();
	protected XVector<CMLibrary>			libraries		= new XVector<CMLibrary>();
	protected Hashtable<String, WebMacro>	webMacros		= new Hashtable<String, WebMacro>();
	protected Hashtable<String, Command>	commandWords	= new Hashtable<String, Command>();

	protected static final LinkedList<CMMsg>MSGS_CACHE		= new LinkedList<CMMsg>();
	protected static final LinkedList<MOB>	MOB_CACHE		= new LinkedList<MOB>();
	protected static final int				MAX_MSGS		= 10000 + ((Runtime.getRuntime().maxMemory() == Integer.MAX_VALUE) ? 10000 : (int) (Runtime.getRuntime().maxMemory() / 10000));
	protected static final int				MAX_MOBS		= 50 + (MAX_MSGS / 200);

	/*
	 * removed to save memory and processing time -- but left for future use
	protected static final long[] OBJECT_CREATIONS=new long[OBJECT_TOTAL];
	protected static final long[] OBJECT_DESTRUCTIONS=new long[OBJECT_TOTAL];
	protected static final Map<CMObject,Object>[] OBJECT_CACHE=new WeakHashMap[OBJECT_TOTAL];
	protected static final boolean KEEP_OBJECT_CACHE=false;

	static
	{
		if(KEEP_OBJECT_CACHE)
			for(int i=0;i<OBJECT_TOTAL;i++)
				OBJECT_CACHE[i]=new WeakHashMap<CMObject,Object>();
	}

	public final static void bumpCounter(final CMObject O, final int which)
	{
		if(KEEP_OBJECT_CACHE)
		{
			if(OBJECT_CACHE[which].containsKey(O))
			{
				Log.errOut("Duplicate!",new Exception("Duplicate Found!"));
				return;
			}
			OBJECT_CACHE[which].put(O,OBJECT_CACHE);
		}
		OBJECT_CREATIONS[which]++;
	}

	public final static void unbumpCounter(final CMObject O, final int which)
	{
		if(KEEP_OBJECT_CACHE)
		{
			if(OBJECT_CACHE[which].containsKey(O)) // yes, if its in there, its bad
			{
				OBJECT_CACHE[which].remove(O);
				Log.errOut("bumped!",O.getClass().getName());
				return;
			}
		}
		OBJECT_DESTRUCTIONS[which]++;
	}

	public static final String getCounterReport()
	{
		StringBuffer str=new StringBuffer("");
		for(int i=0;i<OBJECT_TOTAL;i++)
		{
			if(OBJECT_CREATIONS[i]>0)
				str.append(L("@x1: Created: @x2, Destroyed: @x3, Remaining: @x4\n\r",CMStrings.padRight(OBJECT_DESCS[i],12),OBJECT_CREATIONS[i],OBJECT_DESTRUCTIONS[i],(OBJECT_CREATIONS[i]-OBJECT_DESTRUCTIONS[i])));
		}
		return str.toString();
	}

	public static final long numRemainingObjectCounts(final int type)
	{
		return OBJECT_CREATIONS[type] - OBJECT_DESTRUCTIONS[type];
	}
	*/

	/**
	 * Returns whether the given class exists in the vm,
	 * not necessarily any given classloader.
	 * Requires a fully qualified java class name.
	 * @param className a fully qualified java class name.
	 * @return whether the given class exists in the vm
	 */
	public final static boolean exists(String className)
	{
		try
		{
			Class.forName (className);
			return true;
		}
		catch (final ClassNotFoundException exception)
		{
			return false;
		}
	}

	/**
	 * Checks the given object against the given object type
	 * @see com.planet_ink.coffee_mud.core.CMClass.CMObjectType
	 * @param O the object to inspect
	 * @param type the type to compare against
	 * @return true if theres a match, and false otherwise
	 */
	public final static boolean isType(final Object O, final CMObjectType type)
	{
		switch(type)
		{
			case RACE: 
				return O instanceof Race;
			case CHARCLASS: 
				return O instanceof CharClass;
			case MOB: 
				return O instanceof MOB;
			case ABILITY: 
				return O instanceof Ability;
			case LOCALE: 
				return O instanceof Room;
			case EXIT: 
				return O instanceof Exit;
			case ITEM: 
				return O instanceof Item;
			case BEHAVIOR: 
				return O instanceof Behavior;
			case CLAN: 
				return O instanceof Clan;
			case WEAPON: 
				return O instanceof Weapon;
			case ARMOR: 
				return O instanceof Armor;
			case MISCMAGIC: 
				return O instanceof MiscMagic;
			case AREA: 
				return O instanceof Area;
			case COMMAND: 
				return O instanceof Command;
			case CLANITEM: 
				return O instanceof ClanItem;
			case TECH: 
				return O instanceof Electronics;
			case WEBMACRO: 
				return O instanceof WebMacro;
			case COMMON: 
				return O instanceof CMCommon;
			case LIBRARY: 
				return O instanceof CMLibrary;
			case SOFTWARE: 
				return O instanceof Software;
			case COMPTECH: 
				return O instanceof TechComponent;
		}
		return false;
	}

	/**
	 * Returns a newInstance of an object of the given type and ID. NULL if not found.
	 * @see com.planet_ink.coffee_mud.core.CMClass.CMObjectType
	 * @param ID the ID of the object to look for
	 * @param type the type of object to check
	 * @return a newInstance of an object of the given type and ID.
	 */
	public final static CMObject getByType(final String ID, final CMObjectType type)
	{
		switch(type)
		{
			case RACE: 
				return CMClass.getRace(ID);
			case CHARCLASS: 
				return CMClass.getCharClass(ID);
			case MOB: 
				return CMClass.getMOB(ID);
			case ABILITY: 
				return CMClass.getAbility(ID);
			case LOCALE: 
				return CMClass.getLocale(ID);
			case EXIT: 
				return CMClass.getExit(ID);
			case ITEM: 
				return CMClass.getBasicItem(ID);
			case BEHAVIOR: 
				return CMClass.getBehavior(ID);
			case CLAN: 
				return CMClass.getCommon(ID);
			case WEAPON: 
				return CMClass.getWeapon(ID);
			case ARMOR: 
				return CMClass.getAreaType(ID);
			case MISCMAGIC: 
				return CMClass.getMiscMagic(ID);
			case AREA: 
				return CMClass.getAreaType(ID);
			case COMMAND: 
				return CMClass.getCommand(ID);
			case CLANITEM: 
				return CMClass.getClanItem(ID);
			case TECH: 
				return CMClass.getTech(ID);
			case WEBMACRO: 
				return CMClass.getWebMacro(ID);
			case COMMON: 
				return CMClass.getCommon(ID);
			case LIBRARY: 
				return CMClass.getLibrary(ID);
			case COMPTECH: 
				return CMClass.getTech(ID);
			case SOFTWARE: 
				return CMClass.getTech(ID);
		}
		return null;
	}

	/**
	 * Returns the object type of the given object
	 * @see com.planet_ink.coffee_mud.core.CMClass.CMObjectType
	 * @param O the object to inspect
	 * @return the cmobjectype type
	 */
	public final static CMObjectType getType(final Object O)
	{
		if(O instanceof Race)
			return CMObjectType.RACE;
		if(O instanceof CharClass)
			return CMObjectType.CHARCLASS;
		if(O instanceof Ability)
			return CMObjectType.ABILITY;
		if(O instanceof Room)
			return CMObjectType.LOCALE;
		if(O instanceof MOB)
			return CMObjectType.MOB;
		if(O instanceof Exit)
			return CMObjectType.EXIT;
		if(O instanceof Behavior)
			return CMObjectType.BEHAVIOR;
		if(O instanceof WebMacro)
			return CMObjectType.WEBMACRO;
		if(O instanceof Area)
			return CMObjectType.AREA;
		if(O instanceof CMLibrary)
			return CMObjectType.LIBRARY;
		if(O instanceof CMCommon)
			return CMObjectType.COMMON;
		if(O instanceof Command)
			return CMObjectType.COMMAND;
		if(O instanceof Clan)
			return CMObjectType.CLAN;
		if(O instanceof ClanItem)
			return CMObjectType.CLANITEM;
		if(O instanceof MiscMagic)
			return CMObjectType.MISCMAGIC;
		if(O instanceof Armor)
			return CMObjectType.ARMOR;
		if(O instanceof Weapon)
			return CMObjectType.WEAPON;
		if(O instanceof Item)
			return CMObjectType.ITEM;
		if(O instanceof Software)
			return CMObjectType.SOFTWARE;
		if(O instanceof TechComponent)
			return CMObjectType.COMPTECH;
		if(O instanceof Electronics)
			return CMObjectType.TECH;
		return null;
	}

	/**
	 * Given a string, Integer, or some other stringable object, this will return the
	 * cmobjecttype based on its name or ordinal relationship.
	 * @see com.planet_ink.coffee_mud.core.CMClass.CMObjectType
	 * @param nameOrOrdinal the string, integer, or whatever object
	 * @return the cmobjecttype it refers to
	 */
	public static CMObjectType getTypeByNameOrOrdinal(final Object nameOrOrdinal)
	{
		if(nameOrOrdinal==null)
			return null;
		if(nameOrOrdinal instanceof Integer)
		{
			final int itemtypeord = ((Integer)nameOrOrdinal).intValue();
			if((itemtypeord>=0)&&(itemtypeord<CMObjectType.values().length))
				return CMClass.CMObjectType.values()[itemtypeord];
		}
		if(nameOrOrdinal instanceof Long)
		{
			final int itemtypeord = ((Long)nameOrOrdinal).intValue();
			if((itemtypeord>=0)&&(itemtypeord<CMObjectType.values().length))
				return CMClass.CMObjectType.values()[itemtypeord];
		}
		final String s=nameOrOrdinal.toString();
		if(s.length()==0)
			return null;
		if(CMath.isInteger(s))
		{
			final int itemtypeord=CMath.s_int(s);
			if((itemtypeord>=0)&&(itemtypeord<CMObjectType.values().length))
				return CMClass.CMObjectType.values()[itemtypeord];
		}
		try
		{
			return CMClass.CMObjectType.valueOf(s);
		}
		catch(final Exception e)
		{
			return (CMClass.CMObjectType)CMath.s_valueOf(CMClass.CMObjectType.values(), s.toUpperCase().trim());
		}
	}

	protected static final Object getClassSet(final String type) 
	{ 
		return getClassSet(findObjectType(type));
	}
	
	protected static final Object getClassSet(final CMObjectType code)
	{
		switch(code)
		{
		case RACE: 
			return c().races;
		case CHARCLASS: 
			return c().charClasses;
		case MOB: 
			return c().MOBs;
		case ABILITY: 
			return c().abilities;
		case LOCALE: 
			return c().locales;
		case EXIT: 
			return c().exits;
		case ITEM: 
			return c().items;
		case BEHAVIOR: 
			return c().behaviors;
		case CLAN: 
			return null;
		case WEAPON: 
			return c().weapons;
		case ARMOR: 
			return c().armor;
		case MISCMAGIC: 
			return c().miscMagic;
		case AREA: 
			return c().areaTypes;
		case COMMAND: 
			return c().commands;
		case CLANITEM: 
			return c().clanItems;
		case TECH: 
			return c().tech;
		case WEBMACRO: 
			return c().webMacros;
		case COMMON: 
			return c().common;
		case LIBRARY: 
			return c().libraries;
		case COMPTECH: 
			return c().tech;
		case SOFTWARE: 
			return c().tech;
		}
		return null;
	}

	/**
	 * Returns the total number of template/prototypes of the given type stored by
	 * this CMClass instance.
	 * @see com.planet_ink.coffee_mud.core.CMClass.CMObjectType
	 * @param type the type of object to count
	 * @return the number stored
	 */
	public static final int numPrototypes(final CMObjectType type)
	{
		final Object o = getClassSet(type);
		if(o instanceof Set)
			return ((Set)o).size();
		if(o instanceof List)
			return ((List)o).size();
		if(o instanceof Collection)
			return ((Collection)o).size();
		if(o instanceof HashSet)
			return ((HashSet)o).size();
		if(o instanceof Hashtable)
			return ((Hashtable)o).size();
		if(o instanceof Vector)
			return ((Vector)o).size();
		return 0;
	}

	/**
	 * An enumeration of all the stored races in this classloader for this thread
	 * @return an enumeration of all the stored races in this classloader for this thread
	 */
	public static final Enumeration<Race> races()
	{
		return c().races.elements();
	}

	/**
	 * An enumeration of all the stored common Objects in this classloader for
	 * this thread
	 * 
	 * @return an enumeration of all the stored common Objects in this
	 *         classloader for this thread
	 */
	public static final Enumeration<CMCommon> commonObjects()
	{
		return c().common.elements();
	}

	/**
	 * An enumeration of all the stored char Classes in this classloader for
	 * this thread
	 * 
	 * @return an enumeration of all the stored char Classes in this classloader
	 *         for this thread
	 */
	public static final Enumeration<CharClass> charClasses()
	{
		return c().charClasses.elements();
	}

	/**
	 * An enumeration of all the stored mob Types in this classloader for this
	 * thread
	 * 
	 * @return an enumeration of all the stored mob Types in this classloader
	 *         for this thread
	 */
	public static final Enumeration<MOB> mobTypes()
	{
		return c().MOBs.elements();
	}

	/**
	 * An enumeration of all the stored races in this classloader for this
	 * thread
	 * 
	 * @return an enumeration of all the stored races in this classloader for
	 *         this thread
	 */
	public static final Enumeration<CMLibrary> libraries()
	{
		return c().libraries.elements();
	}

	/**
	 * An enumeration of all the stored locales in this classloader for this
	 * thread
	 * 
	 * @return an enumeration of all the stored locales in this classloader for
	 *         this thread
	 */
	public static final Enumeration<Room> locales()
	{
		return c().locales.elements();
	}

	/**
	 * An enumeration of all the stored exits in this classloader for this
	 * thread
	 * 
	 * @return an enumeration of all the stored exits in this classloader for
	 *         this thread
	 */
	public static final Enumeration<Exit> exits()
	{
		return c().exits.elements();
	}

	/**
	 * An enumeration of all the stored behaviors in this classloader for this
	 * thread
	 * 
	 * @return an enumeration of all the stored behaviors in this classloader
	 *         for this thread
	 */
	public static final Enumeration<Behavior> behaviors()
	{
		return c().behaviors.elements();
	}

	/**
	 * An enumeration of all the stored basic Items in this classloader for this
	 * thread
	 * 
	 * @return an enumeration of all the stored basic Items in this classloader
	 *         for this thread
	 */
	public static final Enumeration<Item> basicItems()
	{
		return c().items.elements();
	}

	/**
	 * An enumeration of all the stored weapons in this classloader for this
	 * thread
	 * 
	 * @return an enumeration of all the stored weapons in this classloader for
	 *         this thread
	 */
	public static final Enumeration<Weapon> weapons()
	{
		return c().weapons.elements();
	}

	/**
	 * An enumeration of all the stored armor in this classloader for this
	 * thread
	 * 
	 * @return an enumeration of all the stored armor in this classloader for
	 *         this thread
	 */
	public static final Enumeration<Armor> armor()
	{
		return c().armor.elements();
	}

	/**
	 * An enumeration of all the stored misc Magic in this classloader for this
	 * thread
	 * 
	 * @return an enumeration of all the stored misc Magic in this classloader
	 *         for this thread
	 */
	public static final Enumeration<MiscMagic> miscMagic()
	{
		return c().miscMagic.elements();
	}

	/**
	 * An enumeration of all the stored misc Magic in this classloader for this
	 * thread
	 * 
	 * @param f the filterer to help select which ones you want
	 * @return an enumeration of all the stored misc Magic in this classloader
	 *         for this thread
	 */
	public static final Enumeration<MiscMagic> miscMagic(Filterer<MiscMagic> f)
	{
		return new FilteredEnumeration<MiscMagic>(c().miscMagic.elements(), f);
	}

	/**
	 * An enumeration of all the stored misc Tech in this classloader for this
	 * thread
	 * 
	 * @return an enumeration of all the stored misc Tech in this classloader
	 *         for this thread
	 */
	public static final Enumeration<Technical> tech()
	{
		return c().tech.elements();
	}

	/**
	 * An enumeration of all the stored misc Tech in this classloader for this
	 * thread
	 * 
	 * @param f the filterer to help select which ones you want
	 * @return an enumeration of all the stored misc Tech in this classloader
	 *         for this thread
	 */
	public static final Enumeration<Technical> tech(Filterer<Technical> f)
	{
		return new FilteredEnumeration<Technical>(c().tech.elements(), f);
	}

	/**
	 * An enumeration of all the stored clan Items in this classloader for this
	 * thread
	 * 
	 * @return an enumeration of all the stored clan Items in this classloader
	 *         for this thread
	 */
	public static final Enumeration<ClanItem> clanItems()
	{
		return c().clanItems.elements();
	}

	/**
	 * An enumeration of all the stored area Types in this classloader for this
	 * thread
	 * 
	 * @return an enumeration of all the stored area Types in this classloader
	 *         for this thread
	 */
	public static final Enumeration<Area> areaTypes()
	{
		return c().areaTypes.elements();
	}

	/**
	 * An enumeration of all the stored commands in this classloader for this
	 * thread
	 * 
	 * @return an enumeration of all the stored commands in this classloader for
	 *         this thread
	 */
	public static final Enumeration<Command> commands()
	{
		return c().commands.elements();
	}

	/**
	 * An enumeration of all the stored abilities in this classloader for this
	 * thread
	 * 
	 * @return an enumeration of all the stored abilities in this classloader
	 *         for this thread
	 */
	public static final Enumeration<Ability> abilities()
	{
		return c().abilities.elements();
	}

	/**
	 * An enumeration of all the stored abilities in this classloader for this
	 * thread
	 * 
	 * @param f the filterer to help select which ones you want
	 * @return an enumeration of all the stored abilities in this classloader
	 *         for this thread
	 */
	public static final Enumeration<Ability> abilities(Filterer<Ability> f)
	{
		return new FilteredEnumeration<Ability>(c().abilities.elements(), f);
	}

	/**
	 * An enumeration of all the stored webmacros in this classloader for this
	 * thread
	 * 
	 * @return an enumeration of all the stored webmacros in this classloader
	 *         for this thread
	 */
	public static final Enumeration<WebMacro> webmacros()
	{
		return c().webMacros.elements();
	}

	/**
	 * Returns a random available race prototype from your classloader
	 * 
	 * @return a random available race prototype
	 */
	public static final Race randomRace()
	{
		return c().races.elementAt((int) Math.round(Math.floor(Math.random() * (c().races.size()))));
	}

	/**
	 * Returns a random available char class prototype from your classloader
	 * 
	 * @return a random available char class prototype
	 */
	public static final CharClass randomCharClass()
	{
		return c().charClasses.elementAt((int) Math.round(Math.floor(Math.random() * (c().charClasses.size()))));
	}

	/**
	 * Returns a random available ability prototype from your classloader
	 * 
	 * @return a random available ability prototype
	 */
	public static final Ability randomAbility()
	{
		return c().abilities.elementAt((int) Math.round(Math.floor(Math.random() * (c().abilities.size()))));
	}

	/**
	 * Returns a random available area prototype from your classloader
	 * 
	 * @return a random available area prototype
	 */
	public static final Area randomArea()
	{
		return c().areaTypes.elementAt((int) Math.round(Math.floor(Math.random() * (c().areaTypes.size()))));
	}

	/**
	 * Returns a new instance of a locale object of the given ID from your
	 * classloader
	 * 
	 * @param calledThis the ID() of the object to return
	 * @return a new instance of a locale object of the given ID
	 */
	public static final Room getLocale(final String calledThis)
	{
		return (Room) getNewGlobal(c().locales, calledThis);
	}

	/**
	 * Returns the prototype instance of a locale object of the given ID from your
	 * classloader
	 * 
	 * @param calledThis the ID() of the object to return
	 * @return the prototype instance of a locale object of the given ID
	 */
	public static final Room getLocalePrototype(final String calledThis)
	{
		return (Room) getGlobal(c().locales, calledThis);
	}

	/**
	 * Returns a reference to the prototype for the library of the given ID from
	 * your classloader
	 * 
	 * @param calledThis the ID() of the object to return
	 * @return a reference to the prototype for the library of the given ID
	 */
	public static final CMLibrary getLibrary(final String calledThis)
	{
		return (CMLibrary) getGlobal(c().libraries, calledThis);
	}

	/**
	 * Returns a new instance of a area object of the given ID from your
	 * classloader
	 * 
	 * @param calledThis the ID() of the object to return
	 * @return a new instance of a area object of the given ID
	 */
	public static final Area getAreaType(final String calledThis)
	{
		return (Area) getNewGlobal(c().areaTypes, calledThis);
	}

	/**
	 * Returns a new instance of a exit object of the given ID from your
	 * classloader
	 * 
	 * @param calledThis the ID() of the object to return
	 * @return a new instance of a exit object of the given ID
	 */
	public static final Exit getExit(final String calledThis)
	{
		return (Exit) getNewGlobal(c().exits, calledThis);
	}

	/**
	 * Returns a new instance of a MOB object of the given ID from your
	 * classloader
	 * 
	 * @param calledThis the ID() of the object to return
	 * @return a new instance of a MOB object of the given ID
	 */
	public static final MOB getMOB(final String calledThis)
	{
		return (MOB) getNewGlobal(c().MOBs, calledThis);
	}

	/**
	 * Returns a new instance of a weapon object of the given ID from your
	 * classloader
	 * 
	 * @param calledThis the ID() of the object to return
	 * @return a new instance of a weapon object of the given ID
	 */
	public static final Weapon getWeapon(final String calledThis)
	{
		return (Weapon) getNewGlobal(c().weapons, calledThis);
	}

	/**
	 * Returns a new instance of a clan item object of the given ID from your
	 * classloader
	 * 
	 * @param calledThis the ID() of the object to return
	 * @return a new instance of a clan item object of the given ID
	 */
	public static final ClanItem getClanItem(final String calledThis)
	{
		return (ClanItem) getNewGlobal(c().clanItems, calledThis);
	}

	/**
	 * Returns a new instance of a misc magic object of the given ID from your
	 * classloader
	 * 
	 * @param calledThis the ID() of the object to return
	 * @return a new instance of a misc magic object of the given ID
	 */
	public static final Item getMiscMagic(final String calledThis)
	{
		return (Item) getNewGlobal(c().miscMagic, calledThis);
	}

	/**
	 * Returns a new instance of a misc tech object of the given ID from your
	 * classloader
	 * 
	 * @param calledThis the ID() of the object to return
	 * @return a new instance of a misc tech object of the given ID
	 */
	public static final Item getTech(final String calledThis)
	{
		return (Item) getNewGlobal(c().tech, calledThis);
	}

	/**
	 * Returns a new instance of a armor object of the given ID from your
	 * classloader
	 * 
	 * @param calledThis the ID() of the object to return
	 * @return a new instance of a armor object of the given ID
	 */
	public static final Armor getArmor(final String calledThis)
	{
		return (Armor) getNewGlobal(c().armor, calledThis);
	}

	/**
	 * Returns a new instance of a basic item object of the given ID from your
	 * classloader
	 * 
	 * @param calledThis the ID() of the object to return
	 * @return a new instance of a basic item object of the given ID
	 */
	public static final Item getBasicItem(final String calledThis)
	{
		return (Item) getNewGlobal(c().items, calledThis);
	}

	/**
	 * Returns a new instance of a behavior object of the given ID from your
	 * classloader
	 * 
	 * @param calledThis the ID() of the object to return
	 * @return a new instance of a behavior object of the given ID
	 */
	public static final Behavior getBehavior(final String calledThis)
	{
		return (Behavior) getNewGlobal(c().behaviors, calledThis);
	}

	/**
	 * Returns a new instance of a ability object of the given ID from your
	 * classloader
	 * 
	 * @param calledThis the ID() of the object to return
	 * @return a new instance of a ability object of the given ID
	 */
	public static final Ability getAbility(final String calledThis)
	{
		return (Ability) getNewGlobal(c().abilities, calledThis);
	}

	/**
	 * Returns the prototype instance of the ability object of the given ID from
	 * your classloader
	 * 
	 * @param calledThis the ID() of the object to return
	 * @return the prototype instance of a ability object of the given ID
	 */
	public static final Ability getAbilityPrototype(final String calledThis)
	{
		return (Ability) getGlobal(c().abilities, calledThis);
	}

	/**
	 * Returns a reference to the prototype for the char class of the given ID
	 * from your classloader
	 * 
	 * @param calledThis the ID() of the object to return
	 * @return a reference to the prototype for the char class of the given ID
	 */
	public static final CharClass getCharClass(final String calledThis)
	{
		return (CharClass) getGlobal(c().charClasses, calledThis);
	}

	/**
	 * Returns a new instance of a common object of the given ID from your
	 * classloader
	 * 
	 * @param calledThis the ID() of the object to return
	 * @return a new instance of a common object of the given ID
	 */
	public static final CMCommon getCommon(final String calledThis)
	{
		return (CMCommon) getNewGlobal(c().common, calledThis);
	}

	/**
	 * Returns the prototype instance of a common object of the given ID from
	 * your classloader
	 * 
	 * @param calledThis the ID() of the object to return
	 * @return the prototype instance of a common object of the given ID
	 */
	public static final CMCommon getCommonPrototype(final String calledThis)
	{
		return (CMCommon) getGlobal(c().common, calledThis);
	}

	/**
	 * Returns a reference to the prototype for the command of the given ID from
	 * your classloader
	 * 
	 * @param word the ID() of the object to return
	 * @return a reference to the prototype for the command of the given ID
	 */
	public static final Command getCommand(final String word)
	{
		return (Command) getGlobal(c().commands, word);
	}

	/**
	 * Returns a reference to the prototype for the web macro of the given ID
	 * from your classloader
	 * 
	 * @param macroName the ID() of the object to return
	 * @return a reference to the prototype for the web macro of the given ID
	 */
	public static final WebMacro getWebMacro(final String macroName)
	{
		return c().webMacros.get(macroName);
	}

	/**
	 * Returns a reference to the prototype for the race of the given ID from
	 * your classloader
	 * 
	 * @param calledThis the ID() of the object to return
	 * @return a reference to the prototype for the race of the given ID
	 */
	public static final Race getRace(final String calledThis)
	{
		return (Race) getGlobal(c().races, calledThis);
	}

	/**
	 * Returns the number of prototypes in the classloader of the given set of types
	 * @param types the types to count
	 * @return the number of prototypes in the classloader of the given set of types
	 */
	public static final int numPrototypes(final CMObjectType[] types)
	{
		int total=0;
		for (final CMObjectType type : types)
			total+=numPrototypes(type);
		return total;
	}

	/**
	 * Fills the given list with the IDs of the various Item types, subject to the given filters
	 * @param namesList the list to populate with IDs
	 * @param NonArchon true to not include Archon items
	 * @param NonGeneric true to not include Gen items
	 * @param NonStandard true to not include Standard items
	 * @param themeCode the theme mask to respect, sortof
	 */
	public static final void addAllItemClassNames(final List<String> namesList, final boolean NonArchon,
												  final boolean NonGeneric, final boolean NonStandard,
												  final int themeCode)
	{
		namesList.addAll(getAllItemClassNames(basicItems(),NonArchon,NonGeneric,NonStandard));
		namesList.addAll(getAllItemClassNames(weapons(),NonArchon,NonGeneric,NonStandard));
		if(CMath.bset(themeCode,Area.THEME_FANTASY))
		{
			namesList.addAll(getAllItemClassNames(armor(),NonArchon,NonGeneric,NonStandard));
			namesList.addAll(getAllItemClassNames(miscMagic(),NonArchon,NonGeneric,NonStandard));
		}
		if(CMath.bset(themeCode,Area.THEME_TECHNOLOGY))
			namesList.addAll(getAllItemClassNames(tech(),NonArchon,NonGeneric,NonStandard));
		namesList.addAll(getAllItemClassNames(clanItems(),NonArchon,NonGeneric,NonStandard));
	}

	private static List<String> getAllItemClassNames(final Enumeration<? extends Item> i,
													 final boolean NonArchon, final boolean NonGeneric, final boolean NonStandard)
	{
		final Vector<String> V=new Vector<String>();
		for(;i.hasMoreElements();)
		{
			final Item I=i.nextElement();
			if(((!NonArchon)||(!(I instanceof ArchonOnly)))
			&&((!NonStandard)||(I.isGeneric()))
			&&((!NonGeneric)||(!I.isGeneric())))
				V.addElement(CMClass.classID(I));
		}
		return V;
	}

	/**
	 * Returns a new instance of an item object of the given ID from your classloader
	 * Will search basic, armor, weapons, misc magic, clan items, and misc tech respectively
	 * @param calledThis the ID() of the object to return
	 * @return a new instance of an item object of the given ID
	 */
	public static Item getItem(final String calledThis)
	{
		Item thisItem=(Item)getNewGlobal(c().items,calledThis);
		if(thisItem==null)
			thisItem=(Item)getNewGlobal(c().armor,calledThis);
		if(thisItem==null)
			thisItem=(Item)getNewGlobal(c().weapons,calledThis);
		if(thisItem==null)
			thisItem=(Item)getNewGlobal(c().miscMagic,calledThis);
		if(thisItem==null)
			thisItem=(Item)getNewGlobal(c().clanItems,calledThis);
		if(thisItem==null)
			thisItem=(Item)getNewGlobal(c().tech,calledThis);
		return thisItem;
	}

	protected Item sampleItem=null;
	/**
	 * Returns the saved copy of the first basic item prototype
	 * @return the saved copy of the first basic item prototype
	 */
	public static final Item sampleItem()
	{
		final CMClass myC=c();
		if((myC.sampleItem==null)&&(myC.items.size()>0))
			myC.sampleItem= (Item)myC.items.firstElement().copyOf();
		return myC.sampleItem;
	}

	/**
	 * Returns a reference to the prototype of an item object of the given ID from your classloader
	 * Will search basic, armor, weapons, misc magic, clan items, and misc tech respectively
	 * @param itemID the ID() of the object to return
	 * @return a reference to the prototype of an item object of the given ID
	 */
	public static final Item getItemPrototype(final String itemID)
	{
		Item thisItem=(Item)getGlobal(c().items,itemID);
		if(thisItem==null)
			thisItem=(Item)getGlobal(c().armor,itemID);
		if(thisItem==null)
			thisItem=(Item)getGlobal(c().weapons,itemID);
		if(thisItem==null)
			thisItem=(Item)getGlobal(c().miscMagic,itemID);
		if(thisItem==null)
			thisItem=(Item)getGlobal(c().clanItems,itemID);
		if(thisItem==null)
			thisItem=(Item)getGlobal(c().tech,itemID);
		return thisItem;
	}

	/**
	 * Returns a reference to the prototype of a mob object of the given ID from your classloader
	 * @param mobID the ID() of the object to return
	 * @return a reference to the prototype of an mob object of the given ID
	 */
	public static final MOB getMOBPrototype(final String mobID)
	{
		return (MOB)CMClass.getGlobal(c().MOBs,mobID);
	}

	protected MOB sampleMOB=null;
	/**
	 * Returns the saved copy of the first mob prototype
	 * @return the saved copy of the first mob prototype
	 */
	public static final MOB sampleMOB()
	{
		final CMClass myC=c();
		if((myC.sampleMOB==null)&&(myC.MOBs.size()>0))
		{
			myC.sampleMOB=(MOB)myC.MOBs.firstElement().copyOf();
			myC.sampleMOB.basePhyStats().setDisposition(PhyStats.IS_NOT_SEEN);
			myC.sampleMOB.phyStats().setDisposition(PhyStats.IS_NOT_SEEN);
		}
		if(myC.sampleMOB.location()==null)
			myC.sampleMOB.setLocation(CMLib.map().getRandomRoom());
		return myC.sampleMOB;
	}

	protected MOB samplePlayer=null;
	/**
	 * Returns the saved copy of the first mob prototype as a player
	 * @return the saved copy of the first mob prototype as a player
	 */
	public static final MOB samplePlayer()
	{
		final CMClass myC=c();
		if((myC.samplePlayer==null)&&(myC.MOBs.size()>0))
		{
			myC.samplePlayer=(MOB)myC.MOBs.firstElement().copyOf();
			myC.samplePlayer.basePhyStats().setDisposition(PhyStats.IS_NOT_SEEN);
			myC.samplePlayer.phyStats().setDisposition(PhyStats.IS_NOT_SEEN);
			final PlayerStats playerStats = (PlayerStats)getCommon("DefaultPlayerStats");
			if(playerStats != null)
			{
				if(CMProps.isUsingAccountSystem())
				{
					final PlayerAccount account = (PlayerAccount)getCommon("DefaultPlayerAccount");
					if(account != null)
						playerStats.setAccount(account);
				}
				myC.samplePlayer.setPlayerStats(playerStats);
			}
		}
		if(myC.samplePlayer.location()==null)
			myC.samplePlayer.setLocation(CMLib.map().getRandomRoom());
		return myC.samplePlayer;
	}

	/**
	 * Searches the command prototypes for a trigger word match and returns the command.
	 * @param word the command word to search for
	 * @param exactOnly true for a whole word match, false for a startsWith match
	 * @return the command prototypes for a trigger word match and returns the command.
	 */
	public static final Command findCommandByTrigger(final String word, final boolean exactOnly)
	{
		final CMClass myC=c();
		final Command C=myC.commandWords.get(word.trim().toUpperCase());
		if((exactOnly)||(C!=null))
			return C;
		final String upword=word.toUpperCase();
		String key;
		for(final Enumeration<String> e=myC.commandWords.keys();e.hasMoreElements();)
		{
			key=e.nextElement();
			if(key.toUpperCase().startsWith(upword))
				return myC.commandWords.get(key);
		}
		return null;
	}

	protected final int totalLocalClasses()
	{
		return races.size()+charClasses.size()+MOBs.size()+abilities.size()+locales.size()+exits.size()
			  +items.size()+behaviors.size()+weapons.size()+armor.size()+miscMagic.size()+clanItems.size()
			  +tech.size()+areaTypes.size()+common.size()+libraries.size()+commands.size()
			  +webMacros.size();
	}

	/**
	 * Returns the total number of prototypes of all classes in your classloader
	 * @return the total number of prototypes of all classes in your classloader
	 */
	public static final int totalClasses()
	{
		return c().totalLocalClasses();
	}

	/**
	 * Deletes the class of the given object type from your classloader
	 * @param type the type of object that the given object belongs to
	 * @param O the specific prototype class to remove
	 * @return true
	 */
	public static final boolean delClass(final CMObjectType type, final CMObject O)
	{
		if(O==null)
			return false;
		if(classes.containsKey(O.getClass().getName()))
			classes.remove(O.getClass().getName());
		final Object set=getClassSet(type);
		if(set==null)
			return false;
		CMClass.lastUpdateTime=System.currentTimeMillis();
		if(set instanceof List)
		{
			((List)set).remove(O);
			if(set instanceof XVector)
				((XVector)set).sort();
		}
		else
		if(set instanceof Hashtable)
			((Hashtable)set).remove(O.ID().trim());
		else
		if(set instanceof HashSet)
			((HashSet)set).remove(O);
		else
			return false;
		if(set==c().commands)
			reloadCommandWords();
		//if(set==libraries) CMLib.registerLibraries(libraries.elements());
		return true;
	}

	/**
	 * Adds a new prototype of the given object type from your classloader
	 * @param type the type of object that the given object belongs to
	 * @param O the specific prototype class to add
	 * @return true
	 */
	public static final boolean addClass(final CMObjectType type, final CMObject O)
	{
		final Object set=getClassSet(type);
		if(set==null)
			return false;
		CMClass.lastUpdateTime=System.currentTimeMillis();
		if(set instanceof List)
		{
			((List)set).add(O);
			if(set instanceof XVector)
				((XVector)set).sort();
		}
		else
		if(set instanceof Hashtable)
			((Hashtable)set).put(O.ID().trim().toUpperCase(), O);
		else
		if(set instanceof HashSet)
			((HashSet)set).add(O);
		else
			return false;
		if(set==c().commands)
			reloadCommandWords();
		if(set==c().libraries)
			CMLib.registerLibraries(c().libraries.elements());
		return true;
	}

	/**
	 * Searches for a match to the given object type name,
	 * preferring exact, but accepting prefixes.
	 * @param name the object type name to search for
	 * @return the matching object type or NULL
	 */
	public final static CMObjectType findObjectType(final String name)
	{
		for(final CMObjectType o : CMObjectType.values())
		{
			if(o.toString().equalsIgnoreCase(name))
				return o;
		}
		final String upperName=name.toUpperCase();
		for(final CMObjectType o : CMObjectType.values())
		{
			if(o.toString().toUpperCase().startsWith(upperName))
				return o;
		}
		for(final CMObjectType o : CMObjectType.values())
		{
			if(upperName.startsWith(o.toString().toUpperCase()))
				return o;
		}
		return null;
	}

	/**
	 * Searches for a match to the given object type name,
	 * preferring exact, but accepting prefixes. Returns
	 * the ancestor java class type
	 * @param code the object type name to search for
	 * @return the matching object type interface/ancestor or NULL
	 */
	public final static String findTypeAncestor(final String code)
	{
		final CMObjectType typ=findObjectType(code);
		if(typ!=null)
			return typ.ancestorName;
		return "";
	}

	/**
	 * Returns the internal object type to which the given object example
	 * belongs by checking its interface implementations/ancestry
	 * @param O the object to find the type of
	 * @return the type of object this is, or NULL
	 */
	public final static CMObjectType getObjectType(final Object O)
	{
		for(final CMObjectType o : CMObjectType.values())
		{
			try
			{
				final Class<?> ancestorCl = instance().loadClass(o.ancestorName);
				if(CMClass.checkAncestry(O.getClass(),ancestorCl))
					return o;
			}
			catch (final Exception e)
			{
			}
		}
		return null;
	}

	/**
	 * Loads the class with the given coffeemud or java path to your classloader.
	 * @param classType the type of object to load
	 * @param path the file or java path of the class to load
	 * @param quiet true to not report errors to the log, false otherwise
	 * @return true if the prototype was loaded
	 */
	public static final boolean loadClass(final CMObjectType classType, final String path, final boolean quiet)
	{
		debugging=CMSecurity.isDebugging(CMSecurity.DbgFlag.CLASSLOADER);
		final Object set=getClassSet(classType);
		if(set==null)
			return false;
		CMClass.lastUpdateTime=System.currentTimeMillis();

		if(!loadListToObj(set,path,classType.ancestorName,quiet))
			return false;

		if(set instanceof List)
		{
			if(set instanceof XVector)
				((XVector)set).sort();
			if(set==c().commands)
				reloadCommandWords();
			if(set==c().libraries)
				CMLib.registerLibraries(c().libraries.elements());
		}
		return true;
	}

	protected static String makeDotClassPath(final String path)
	{
		String pathLess=path;
		final String upperPathLess=pathLess.toUpperCase();
		if(upperPathLess.endsWith(".CLASS"))
			pathLess=pathLess.substring(0,pathLess.length()-6);
		else
		if(upperPathLess.endsWith(".JAVA"))
			pathLess=pathLess.substring(0,pathLess.length()-5);
		else
		if(upperPathLess.endsWith(".JS"))
			pathLess=pathLess.substring(0,pathLess.length()-3);
		pathLess=pathLess.replace('/','.');
		pathLess=pathLess.replace('\\','.');
		return pathLess;
	}

	protected static String makeFilePath(final String path)
	{
		final String upperPath=path.toUpperCase();
		if((!upperPath.endsWith(".CLASS"))
		&&(!upperPath.endsWith(".JAVA"))
		&&(!upperPath.endsWith(".JS")))
			return path.replace('.','/')+".class";
		return path;
	}

	/**
	 * If the given class exists in the classloader, a new instance will be returned.
	 * If it does not, it will be loaded, and then a new instance of it will be returned.
	 * @param classType the type of class as a filter
	 * @param path the path of some sort to get a new instance of
	 * @param quiet true to not post errors to the log, false otherwise
	 * @return a new instance of the given class
	 */
	public static final Object getLoadNewClassInstance(final CMObjectType classType, final String path, final boolean quiet)
	{
		if((path==null)||(path.length()==0))
			return null;
		try
		{
			final String pathLess=makeDotClassPath(path);
			if(classes.containsKey(pathLess))
				return (classes.get(pathLess)).newInstance();
		}
		catch(final Exception e)
		{
		}
		final Vector<Object> V=new Vector<Object>(1);
		if(!loadListToObj(V,makeFilePath(path),classType.ancestorName,quiet))
			return null;
		if(V.size()==0)
			return null;
		final Object o = V.firstElement();
		try
		{
			return o.getClass().newInstance();
		}
		catch(final Exception e)
		{
			return o;
		}
	}

	/**
	 * Returns true if the given class has been loaded into the classloader, or if it is loadable
	 * through the cm class loading system.
	 * @param classType the type of class to check for (for ancestry confirmation)
	 * @param path the path of the class to check for
	 * @return true if it is loaded or loadable, false otherwise
	 */
	public final static boolean checkForCMClass(final CMObjectType classType, final String path)
	{
		if((path==null)||(path.length()==0))
			return false;
		try
		{
			final String pathLess=makeDotClassPath(path);
			if(classes.containsKey(pathLess))
				return true;
		}
		catch(final Exception e)
		{
		}
		final Vector<Object> V=new Vector<Object>(1);
		if(!loadListToObj(V,makeFilePath(path),classType.ancestorName,true))
			return false;
		if(V.size()==0)
			return false;
		return true;
	}

	/**
	 * Returns the base prototype of the given type, by id
	 * @param type the cmobjecttype to return
	 * @param calledThis the ID of the cmobjecttype
	 * @return the base prototype of the given type, by id
	 */
	public static final CMObject getPrototypeByID(final CMObjectType type, final String calledThis)
	{
		final Object set=getClassSet(type);
		if(set==null)
			return null;
		CMObject thisItem;
		if(set instanceof List)
			thisItem=getGlobal((List)set,calledThis);
		else
		if(set instanceof Map)
			thisItem=getGlobal((Map)set,calledThis);
		else
			return null;
		return thisItem;
	}

	/**
	 * Returns either a new instance of the class of the given full java name,
	 * or the coffeemud prototype of the class with the given id.  Checks all
	 * cmobjecttypes.
	 * @param calledThis the ID or the given full java name.
	 * @return a new instance of the class, or the prototype
	 */
	public static final Object getObjectOrPrototype(final String calledThis)
	{
		String shortThis=calledThis;
		final int x=shortThis.lastIndexOf('.');
		if(x>0)
		{
			shortThis=shortThis.substring(x+1);
			try
			{
				return classes.get(calledThis).newInstance();
			}
			catch (final Exception e)
			{
			}
		}
		for(final CMObjectType o : CMObjectType.values())
		{
			final Object thisItem=getPrototypeByID(o,shortThis);
			if(thisItem!=null)
				return thisItem;
		}
		return null;
	}

	/**
	 * Returns a new instance of a Environmental of the given id, prefers items,
	 * but also checks mobs and abilities as well.
	 * @param calledThis the id of the cmobject
	 * @return a new instance of a Environmental
	 */
	public static final Environmental getUnknown(final String calledThis)
	{
		Environmental thisItem=(Environmental)getNewGlobal(c().items,calledThis);
		if(thisItem==null)
			thisItem=(Environmental)getNewGlobal(c().armor,calledThis);
		if(thisItem==null)
			thisItem=(Environmental)getNewGlobal(c().weapons,calledThis);
		if(thisItem==null)
			thisItem=(Environmental)getNewGlobal(c().miscMagic,calledThis);
		if(thisItem==null)
			thisItem=(Environmental)getNewGlobal(c().tech,calledThis);
		if(thisItem==null)
			thisItem=(Environmental)getNewGlobal(c().MOBs,calledThis);
		if(thisItem==null)
			thisItem=(Environmental)getNewGlobal(c().abilities,calledThis);
		if(thisItem==null)
			thisItem=(Environmental)getNewGlobal(c().clanItems,calledThis);
		if((thisItem==null)&&(c().charClasses.size()>0)&&(calledThis.length()>0))
			Log.sysOut("CMClass","Unknown Unknown '"+calledThis+"'.");
		return thisItem;
	}

	/**
	 * Does a search for a race of the given name, first checking
	 * for identical matches, then case insensitive name matches.
	 * @param calledThis the name or id
	 * @return the race object
	 */
	public static final Race findRace(final String calledThis)
	{
		final Race thisItem=getRace(calledThis);
		if(thisItem!=null)
			return thisItem;
		Race R;
		final CMClass c=c();
		for(int i=0;i<c.races.size();i++)
		{
			R=c.races.elementAt(i);
			if(R.name().equalsIgnoreCase(calledThis))
				return R;
		}
		return null;
	}

	/**
	 * Does a search for a Char Class of the given name, first checking
	 * for identical matches, then case insensitive name matches.
	 * @param calledThis the name or id
	 * @return the Char Class object
	 */
	public static final CharClass findCharClass(final String calledThis)
	{
		final CharClass thisItem=getCharClass(calledThis);
		if(thisItem!=null)
			return thisItem;
		CharClass C;
		final CMClass c=c();
		for(int i=0;i<c.charClasses.size();i++)
		{
			C=c.charClasses.elementAt(i);
			for(int n=0;n<C.nameSet().length;n++)
			{
				if(C.nameSet()[n].equalsIgnoreCase(calledThis))
					return C;
			}
		}
		return null;
	}

	/**
	 * Returns a new instance of the cmobject of the given id from the given list
	 * @param list the list to search, must be alphabetized
	 * @param ID the perfect cmobject ID of the object
	 * @return a new instance of the cmobject of the given id from the given list
	 */
	public static final CMObject getNewGlobal(final List<? extends CMObject> list, final String ID)
	{
		final CMObject O=getGlobal(list,ID);
		if(O!=null)
			return O.newInstance();
		return null;
	}

	/**
	 * Returns the prototype of the cmobject of the given id from the given list
	 * @param list the list to search, must be alphabetized
	 * @param ID the perfect cmobject ID of the object
	 * @return the prototype of the cmobject of the given id from the given list
	 */
	public static final CMObject getGlobal(final List<? extends CMObject> list, final String ID)
	{
		if(list.size()==0)
			return null;
		int start=0;
		int end=list.size()-1;
		while(start<=end)
		{
			final int mid=(end+start)/2;
			final int comp=classID(list.get(mid)).compareToIgnoreCase(ID);
			if(comp==0)
				return list.get(mid);
			else
			if(comp>0)
				end=mid-1;
			else
				start=mid+1;
		}
		return null;
	}

	/**
	 * Searches for an Ability object using the given search term.
	 * This "finder" matches the ID, and searches the name and display text.
	 * @param calledThis the search term to use
	 * @return the first ability found matching the search term
	 */
	public static final Ability findAbility(final String calledThis)
	{
		return findAbility(calledThis,-1,-1,false);
	}

	/**
	 * Searches for an Ability object using the given search term and filters.
	 * This "finder" matches the ID, and searches the name and display text.
	 * @param calledThis the search term to use
	 * @param ofClassDomain a class/domain filter, or -1 to skip
	 * @param ofFlags an ability flag filter, or -1 to skip
	 * @param exactOnly true to match only case-insensitive whole strings, false otherwise
	 * @return the first ability found matching the search term
	 */
	public static final Ability findAbility(final String calledThis, final int ofClassDomain, final long ofFlags, final boolean exactOnly)
	{
		final Vector<Ability> ableV;
		Ability A;
		if((ofClassDomain>=0)||(ofFlags>=0))
		{
			ableV = new Vector<Ability>();
			for(final Enumeration<Ability> e=c().abilities.elements();e.hasMoreElements();)
			{
				A=e.nextElement();
				if((ofClassDomain<0)
				||((A.classificationCode() & Ability.ALL_ACODES)==ofClassDomain)
				||((A.classificationCode() & Ability.ALL_DOMAINS)==ofClassDomain))
				{
					if((ofFlags<0)
					||(CMath.bset(A.flags(),ofFlags)))
						ableV.addElement(A);
				}
			}
		}
		else
			ableV = c().abilities;

		A=(Ability)getGlobal(ableV,calledThis);
		if(A==null)
			A=(Ability)CMLib.english().fetchEnvironmental(ableV,calledThis,true);
		if((A==null)&&(!exactOnly))
			A=(Ability)CMLib.english().fetchEnvironmental(ableV,calledThis,false);
		if(A!=null)
			A=(Ability)A.newInstance();
		return A;
	}

	/**
	 * Searches for a Behavior object using the given search term.
	 * This "finder" matches the ID, and searches the name.
	 * @param calledThis the search term to use
	 * @return the first behavior found matching the search term
	 */
	public static final Behavior findBehavior(final String calledThis)
	{
		Behavior B=(Behavior)getGlobal(c().behaviors,calledThis);
		if(B==null)
			B=getBehaviorByName(calledThis,true);
		if(B==null)
			B=getBehaviorByName(calledThis,false);
		if(B!=null)
			B=(Behavior)B.copyOf();
		return B;
	}

	/**
	 * Searches for a Behavior object using the given search term and filters.
	 * This "finder" matches the name only, no ID.
	 * @param calledThis the search term to use
	 * @param exact true for whole string match, false otherwise
	 * @return the first behavior found matching the search term
	 */
	public static final Behavior getBehaviorByName(final String calledThis, final boolean exact)
	{
		if(calledThis==null)
			return null;
		Behavior B=null;
		for(final Enumeration<Behavior> e=behaviors();e.hasMoreElements();)
		{
			B=e.nextElement();
			if(B.name().equalsIgnoreCase(calledThis))
				return (Behavior)B.copyOf();
		}
		if(exact)
			return null;
		for(final Enumeration<Behavior> e=behaviors();e.hasMoreElements();)
		{
			B=e.nextElement();
			if(CMLib.english().containsString(B.name(),calledThis))
				return (Behavior)B.copyOf();
		}
		return null;
	}

	/**
	 * Searches for an Ability object using the given search term.
	 * This "finder" matches the name only, no ID
	 * @param calledThis the search term to use
	 * @param exact true for whole string match, false otherwise
	 * @return the first ability found matching the search term
	 */
	public static final Ability getAbilityByName(final String calledThis, final boolean exact)
	{
		if(calledThis==null)
			return null;
		Ability A=null;
		for(final Enumeration<Ability> e=abilities();e.hasMoreElements();)
		{
			A=e.nextElement();
			if(A.name().equalsIgnoreCase(calledThis))
				return A;
		}
		if(exact)
			return null;
		for(final Enumeration<Ability> e=abilities();e.hasMoreElements();)
		{
			A=e.nextElement();
			if(CMLib.english().containsString(A.name(),calledThis))
				return A;
		}
		return null;
	}

	/**
	 * Searches for an Ability object using the given search term and filters.
	 * This "finder" searches the name and display text, and finally the ID.
	 * The filter here is to allow you to filter only abilities that a given
	 * mob qualifies for by sending their charstats as a "character class" set.
	 * @param calledThis the search term to use
	 * @param charStats only the abilities qualified for by the classes herein
	 * @return the first ability found matching the search term
	 */
	public static final Ability findAbility(final String calledThis, final CharStats charStats)
	{
		Ability A=null;
		final List<Ability> As=new LinkedList<Ability>();
		for(final Enumeration<Ability> e=abilities();e.hasMoreElements();)
		{
			A=e.nextElement();
			for(int c=0;c<charStats.numClasses();c++)
			{
				final CharClass C=charStats.getMyClass(c);
				if(CMLib.ableMapper().getQualifyingLevel(C.ID(),true,A.ID())>=0)
				{
					As.add(A);
					break;
				}
			}
		}
		A=(Ability)CMLib.english().fetchEnvironmental(As,calledThis,true);
		if(A==null)
			A=(Ability)CMLib.english().fetchEnvironmental(As,calledThis,false);
		if(A==null)
			A=(Ability)getGlobal(c().abilities,calledThis);
		if(A!=null)
			A=(Ability)A.newInstance();
		return A;
	}

	/**
	 * Searches for an Ability object using the given search term and filters.
	 * This "finder" searches the name and display text, and finally the ID.
	 * The filter here is to allow you to filter only abilities that a given
	 * mob actually has.
	 * @param calledThis the search term to use
	 * @param mob the dude to search
	 * @return the first ability found matching the search term
	 */
	public static final Ability findAbility(final String calledThis, final MOB mob)
	{
		final List<Ability> As=new LinkedList<Ability>();
		Ability A=null;
		for(final Enumeration<Ability> a=mob.allAbilities();a.hasMoreElements();)
		{
			A=a.nextElement();
			if(A!=null) 
				As.add(A);
		}
		A=(Ability)CMLib.english().fetchEnvironmental(As,calledThis,true);
		if(A==null)
			A=(Ability)CMLib.english().fetchEnvironmental(As,calledThis,false);
		if(A==null)
			A=(Ability)getGlobal(c().abilities,calledThis);
		if(A!=null)
			A=(Ability)A.newInstance();
		return A;
	}

	/**
	 * Given a map of CMObjects with ID()s defined, this will return the one matched by the given ID.
	 * If the ID is not found in the map, it will iterate and look for a case-insensitive match before
	 * giving up.  It returns a brand new object.
	 * @param list the map of IDs to objects
	 * @param ID the ID to search for
	 * @return the CMObject that the ID belongs to, after newInstance is called.
	 */
	public static final CMObject getNewGlobal(final Map<String,? extends CMObject> list, final String ID)
	{
		final CMObject O=getGlobal(list,ID);
		if(O!=null)
			return O.newInstance();
		return null;
	}

	/**
	 * Given a map of CMObjects with ID()s defined, this will return the one matched by the given class name.
	 * If the name is not found in the map, it will iterate and look for a case-insensitive match before
	 * giving up.   If returns the actual map reference.
	 * @param list the map of IDs to objects
	 * @param ID the ID to search for
	 * @return the CMObject that the ID belongs to, straight from the map.
	 */
	public static final CMObject getGlobal(final Map<String,? extends CMObject> list, final String ID)
	{
		CMObject o=list.get(ID);
		if(o==null)
		{
			for(final String s : list.keySet())
			{
				o=list.get(s);
				if(classID(o).equalsIgnoreCase(ID))
					return o;
			}
			return null;
		}
		return o;
	}

	/**
	 * Adds a new Race to the class sets.
	 * @param GR the race to add
	 */
	public static final void addRace(final Race GR)
	{
		Race R;
		for(int i=0;i<c().races.size();i++)
		{
			R=c().races.elementAt(i);
			if(R.ID().compareToIgnoreCase(GR.ID())>=0)
			{
				if(R.ID().compareToIgnoreCase(GR.ID())==0)
					c().races.setElementAt(GR,i);
				else
					c().races.insertElementAt(GR,i);
				return;
			}
		}
		c().races.addElement(GR);
	}

	/**
	 * Adds a new character class to the set
	 * @param CR the character class to add
	 */
	public static final void addCharClass(final CharClass CR)
	{
		for(int i=0;i<c().charClasses.size();i++)
		{
			final CharClass C=c().charClasses.elementAt(i);
			if(C.ID().compareToIgnoreCase(CR.ID())>=0)
			{
				if(C.ID().compareToIgnoreCase(CR.ID())==0)
					c().charClasses.setElementAt(CR,i);
				else
					c().charClasses.insertElementAt(CR,i);
				return;
			}
		}
		c().charClasses.addElement(CR);
	}

	/**
	 * Removes the given characterclass from this set
	 * @param C the character class to remove
	 */
	public static final void delCharClass(final CharClass C)
	{
		c().charClasses.removeElement(C);
	}

	/**
	 * Removes the given race from this set
	 * @param R the race to remove
	 */
	public static final void delRace(final Race R)
	{
		c().races.removeElement(R);
	}

	/**
	 * Given a list of CMObjects, this will sort them, by {@link CMObject#ID()}
	 * @param V the list of objects to sort.
	 */
	public static final void sortCMObjectsByID(final List<CMObject> V)
	{
		Collections.sort(V,new Comparator<CMObject>()
		{
			@Override
			public int compare(CMObject o1, CMObject o2)
			{
				if(o1 == null)
				{
					if (o2 == null)
						return 0;
					return -1;
				}
				else
				if(o2 == null)
					return 1;
				return o1.ID().compareTo(o2.ID());
			}
		});
	}

	/**
	 * Given a list of environmentals, this will sort them by {@link Environmental#ID()}
	 * @param V the list of environmentals
	 */
	public static final void sortEnvironmentalsByName(final List<? extends Environmental> V)
	{
		Collections.sort(V,new Comparator<Environmental>()
		{
			@Override
			public int compare(Environmental o1, Environmental o2)
			{
				if(o1 == null)
				{
					if (o2 == null)
						return 0;
					return -1;
				}
				else
				if(o2 == null)
					return 1;
				return o1.name().compareToIgnoreCase(o2.name());
			}
		});
	}

	/**
	 * Calls "initializeclass" on all the given CMObjects.
	 * @param V the list of CMObjects to initialize.
	 */
	private final void initializeClassGroup(final List<? extends CMObject> V)
	{
		for(int v=0;v<V.size();v++)
			((CMObject)V.get(v)).initializeClass();
	}

	/**
	 * Calls "initializeclass" on all the given CMObjects.
	 * @param H the set of CMObjects to initialize.
	 */
	private final void initializeClassGroup(final Map<String,? extends CMObject> H)
	{
		for(final Object o : H.keySet())
			((CMObject)H.get(o)).initializeClass();
	}

	/**
	 * Initializes ALL the internal classes in these sets.  All of them.  All types.
	 */
	public final void intializeClasses()
	{
		final char tCode=Thread.currentThread().getThreadGroup().getName().charAt(0);
		for(final CMObjectType o : CMObjectType.values())
		{
			if((tCode==MudHost.MAIN_HOST)||(CMProps.isPrivateToMe(o.toString())))
			{
				final Object set = CMClass.getClassSet(o);
				if(set instanceof List)
					initializeClassGroup((List)set);
				else
				if(set instanceof Hashtable)
					initializeClassGroup((Map)set);
			}
		}
	}

	/**
	 * Given the "stock" default path to a set of coffeemud classes, and a potential set of requested paths, this
	 * method will follow requested paths (observing the default if default is listed in the request paths), and
	 * load all the coffeemud classes therein, making sure they respect the given interface/ancestor name.
	 * @param defaultPath the path to use when default is a requested path
	 * @param requestedPathList the ; separated list of paths to look for classes in
	 * @param ancestor the full class name of an acester/interface
	 * @return a hashtable mapping the IDs of the classes with a prototype instance of the classes
	 */
	public static Hashtable loadHashListToObj(final String defaultPath, String requestedPathList, final String ancestor)
	{
		final Hashtable<String,Object> h=new Hashtable<String,Object>();
		int x=requestedPathList.indexOf(';');
		String path;
		while(x>=0)
		{
			path=requestedPathList.substring(0,x).trim();
			requestedPathList=requestedPathList.substring(x+1).trim();
			loadObjectListToObj(h,defaultPath,path,ancestor);
			x=requestedPathList.indexOf(';');
		}
		loadObjectListToObj(h,defaultPath,requestedPathList,ancestor);
		return h;
	}

	/**
	 * Given the "stock" default path to a set of coffeemud classes, and a potential set of requested paths, this
	 * method will follow requested paths (observing the default if default is listed in the request paths), and
	 * load all the coffeemud classes therein, making sure they respect the given interface/ancestor name.
	 * @param defaultPath the path to use when default is a requested path
	 * @param requestedPathList the ; separated list of paths to look for classes in
	 * @param ancestor the full class name of an acester/interface
	 * @return a vector of all the  prototype instance of the classes
	 */
	public static final XVector loadVectorListToObj(final String defaultPath, String requestedPathList, final String ancestor)
	{
		final Vector<Object> v=new Vector<Object>();
		int x=requestedPathList.indexOf(';');
		String path;
		while(x>=0)
		{
			path=requestedPathList.substring(0,x).trim();
			requestedPathList=requestedPathList.substring(x+1).trim();
			loadObjectListToObj(v,defaultPath,path,ancestor);
			x=requestedPathList.indexOf(';');
		}
		loadObjectListToObj(v,defaultPath,requestedPathList,ancestor);
		return new XVector<Object>(new TreeSet<Object>(v));
	}

	/**
	 * Given the "stock" default path to a set of coffeemud classes, and a potential set of requested paths, this
	 * method will follow requested paths (observing the default if default is listed in the request paths), and
	 * load all the coffeemud classes therein, making sure they respect the given interface/ancestor class.
	 * @param defaultPath the path to use when default is a requested path
	 * @param requestedPathList the ; separated list of paths to look for classes in
	 * @param ancestorC1 the full class of an acester/interface
	 * @param subDir if given, this will be appended to all requested paths except default
	 * @param quiet true to not report errors to the log, false otherwise
	 * @return a vector of all the  prototype instance of the classes
	 */
	public static final Vector<Object> loadClassList(final String defaultPath, String requestedPathList, final String subDir, final Class<?> ancestorC1, final boolean quiet)
	{
		final Vector<Object> v=new Vector<Object>();
		int x=requestedPathList.indexOf(';');
		while(x>=0)
		{
			String path=requestedPathList.substring(0,x).trim();
			requestedPathList=requestedPathList.substring(x+1).trim();
			if(path.equalsIgnoreCase("%default%"))
				loadListToObj(v,defaultPath, ancestorC1, quiet);
			else
			{
				if((subDir!=null)&&(subDir.length()>0))
					path+=subDir;
				loadListToObj(v,path,ancestorC1, quiet);
			}
			x=requestedPathList.indexOf(';');
		}
		if(requestedPathList.equalsIgnoreCase("%default%"))
			loadListToObj(v,defaultPath, ancestorC1, quiet);
		else
		{
			if((subDir!=null)&&(subDir.length()>0))
				requestedPathList+=subDir;
			loadListToObj(v,requestedPathList,ancestorC1, quiet);
		}
		return v;
	}

	/**
	 * Given a java collection type of some sort (hashtable, vector, etc), a default path, a requested path,
	 * and the name of an interface/ancestor that classes must implement, this method will load all classes
	 * in the appropriate path into the given collection.
	 * @param collection the collection type to use (map, list, set, etc, etc)
	 * @param defaultPath the path to use if the given path requests the default path
	 * @param path the requested path to use
	 * @param ancestor the full java class name of an interface ancestor to force classes to respect
	 * @return true if classes were loaded without errors, false otherwise
	 */
	public static final boolean loadObjectListToObj(final Object collection, final String defaultPath, final String path, final String ancestor)
	{
		if(path.length()>0)
		{
			final boolean success;
			if(path.equalsIgnoreCase("%default%"))
				success=loadListToObj(collection,defaultPath, ancestor, false);
			else
				success=loadListToObj(collection,path,ancestor, false);
			return success;
		}
		return false;
	}

	/**
	 * Given a java collection type of some sort (hashtable, vector, etc), a file path,
	 * and the name of an interface/ancestor that classes must implement, this method will load all classes
	 * in the appropriate path into the given collection.
	 * @param collection the collection type to use (map, list, set, etc, etc)
	 * @param filePath the path to look for classes in
	 * @param ancestor the full java class name of an interface ancestor to force classes to respect
	 * @param quiet true to not report errors, false otherwise
	 * @return true if classes were loaded successfully, false otherwise
	 */
	public static final boolean loadListToObj(final Object collection, final String filePath, final String ancestor, final boolean quiet)
	{
		final CMClass loader=new CMClass();
		Class<?> ancestorCl=null;
		if (ancestor != null && ancestor.length() != 0)
		{
			try
			{
				ancestorCl = loader.loadClass(ancestor);
			}
			catch (final ClassNotFoundException e)
			{
				if(!quiet)
					Log.sysOut("CMClass","WARNING: Couldn't load ancestor class: "+ancestor);
			}
		}
		return loadListToObj(collection, filePath, ancestorCl, quiet);
	}

	/**
	 * Given a java collection type of some sort (hashtable, vector, etc), a file path,
	 * and the class of an interface/ancestor that classes must implement, this method will load all classes
	 * in the appropriate path into the given collection.
	 * @param collection the collection type to use (map, list, set, etc, etc)
	 * @param filePath the path to look for classes in
	 * @param ancestorCl the full java class of an interface ancestor to force classes to respect
	 * @param quiet true to not report errors, false otherwise
	 * @return true if classes were loaded successfully, false otherwise
	 */
	public static final boolean loadListToObj(final Object collection, final String filePath, final Class<?> ancestorCl, final boolean quiet)
	{
		final CMClass loader=new CMClass();
		final CMFile file=new CMFile(filePath,null,CMFile.FLAG_LOGERRORS);
		final Vector<String> fileList=new Vector<String>();
		if(file.canRead())
		{
			if(file.isDirectory())
			{
				final CMFile[] list=file.listFiles();
				for (final CMFile element : list)
				{
					if((element.getName().indexOf('$')<0)&&(element.getName().toUpperCase().endsWith(".CLASS")))
						fileList.addElement(element.getVFSPathAndName());
				}
				for (final CMFile element : list)
				{
					if(element.getName().toUpperCase().endsWith(".JS"))
						fileList.addElement(element.getVFSPathAndName());
				}
			}
			else
			{
				fileList.addElement(file.getVFSPathAndName());
			}
		}
		else
		{
			if(!quiet)
				Log.errOut("CMClass","Unable to access path "+file.getVFSPathAndName());
			return false;
		}
		String item;
		for(int l=0;l<fileList.size();l++)
		{
			item=fileList.elementAt(l);
			if(item.startsWith("/"))
				item=item.substring(1);
			try
			{
				Object O=null;
				String packageName=item.replace('/','.');
				if(packageName.toUpperCase().endsWith(".CLASS"))
					packageName=packageName.substring(0,packageName.length()-6);
				final Class<?> C=loader.loadClass(packageName,true);
				if(C!=null)
				{
					if(!checkAncestry(C,ancestorCl))
					{
						if(!quiet)
							Log.sysOut("CMClass","WARNING: class failed ancestral check: "+packageName);
					}
					else
						O=C.newInstance();
				}
				if(O==null)
				{
					if(!quiet)
						Log.sysOut("CMClass","Unable to create class '"+packageName+"'");
				}
				else
				{
					String itemName=O.getClass().getName();
					final int x=itemName.lastIndexOf('.');
					if(x>=0)
						itemName=itemName.substring(x+1);
					if(collection instanceof Map)
					{
						final Map H=(Map)collection;
						if(H.containsKey(itemName.trim().toUpperCase()))
							H.remove(itemName.trim().toUpperCase());
						H.put(itemName.trim().toUpperCase(),O);
					}
					else
					if(collection instanceof List)
					{
						final List V=(List)collection;
						boolean doNotAdd=false;
						for(int v=0;v<V.size();v++)
						{
							if(getSimpleClassName(V.get(v)).equals(itemName))
							{
								V.set(v,O);
								doNotAdd=true;
								break;
							}
						}
						if(!doNotAdd)
							V.add(O);
					}
					else
					if(collection instanceof Collection)
					{
						final Collection V=(Collection)collection;
						for(final Object o : V)
						{
							if(getSimpleClassName(o).equals(itemName))
							{
								V.remove(o);
								break;
							}
						}
						V.add(O);
					}
				}
			}
			catch(final Exception e)
			{
				if(!quiet)
					Log.errOut("CMClass",e);
				return false;
			}
		}
		return true;
	}

	/**
	 * This strange method returns an environmentals name,
	 * plus a string of instance hex digits, which I guess make
	 * the name more unique.
	 * @param E the environmenal to make a unique name for
	 * @return the unique name
	 */
	public static final String getObjInstanceStr(Environmental E)
	{
		if(E==null)
			return "NULL";
		final int x=E.toString().indexOf('@');
		if(x<0)
			return E.Name()+E.toString();
		return E.Name()+E.toString().substring(x);
	}

	/**
	 * Returns the simple class name of an object -- basically the name that comes
	 * after the final "." in a classpath.
	 * @param O the object to get the name for
	 * @return the simple name
	 */
	public static final String getSimpleClassName(final Object O)
	{
		if(O==null)
			return "";
		return getSimpleClassName(O.getClass());
	}

	/**
	 * Returns the simple class name of a class -- basically the name that comes
	 * after the final "." in a classpath.
	 * @param C the class to get the name for
	 * @return the simple name
	 */
	public static final String getSimpleClassName(final Class<?> C)
	{
		if(C==null)
			return "";
		final String name=C.getName();
		final int lastDot=name.lastIndexOf('.');
		if(lastDot>=0)
			return name.substring(lastDot+1);
		return name;
	}

	/**
	 * Given a class, this method will return a CMFile object for the directory containing
	 * that class.
	 * @param C the class to get a directory for
	 * @return the CMFile containing that class
	 */
	public static final CMFile getClassDir(final Class<?> C)
	{
		final URL location = C.getProtectionDomain().getCodeSource().getLocation();
		String loc;
		if(location == null)
		{
			return null;
		}

		loc=location.getPath();
		loc=loc.replace('/',File.separatorChar);
		String floc=new java.io.File(".").getAbsolutePath();
		if(floc.endsWith("."))
			floc=floc.substring(0,floc.length()-1);
		if(floc.endsWith(File.separator))
			floc=floc.substring(0,floc.length()-File.separator.length());
		int x=floc.indexOf(File.separator);
		if(x>=0)
			floc=floc.substring(File.separator.length());
		x=loc.indexOf(floc);
		loc=loc.substring(x+floc.length());
		loc=loc.replace(File.separatorChar,'/');
		return new CMFile("/"+loc,null);
	}

	/**
	 * Returns true if the given class implements the given ancestor/interface
	 * @param cl the class to check
	 * @param ancestorCl the ancestor/interface
	 * @return true if one comes from the second
	 */
	public static final boolean checkAncestry(final Class<?> cl, final Class<?> ancestorCl)
	{
		if (cl == null)
			return false;
		if (cl.isPrimitive() || cl.isInterface())
			return false;
		if (Modifier.isAbstract(cl.getModifiers()) || !Modifier.isPublic(cl.getModifiers()))
			return false;
		if (ancestorCl == null)
			return true;
		return (ancestorCl.isAssignableFrom(cl));
	}

	/**
	 * Returns the address part of an instance objects "default string name", which
	 * looks like com.planet_ink.coffee_mud.blah.ClassName{@literal @}ab476d87e
	 * where the part after the at sign is the address
	 * @param e the object to get an address for
	 * @return the address
	 */
	public static final String classPtrStr(final Object e)
	{
		final String ptr=""+e;
		final int x=ptr.lastIndexOf('@');
		if(x>0)
			return ptr.substring(x+1);
		return ptr;
	}

	/// *// *
	 // * This is a simple version for external clients since they
	 // * will always want the class resolved before it is returned
	 // * to them.
	 // */

	/**
	 * Returns the ID() if the object is a CMObject, and otherwise
	 * the simple class name, which is the class name after the final
	 * dot in a class path
	 * @param e the object to get a simple class name for.
	 * @return the simple class name, or ID
	 */
	public static final String classID(final Object e)
	{
		if(e!=null)
		{
			if(e instanceof CMObject)
				return ((CMObject)e).ID();
			else
			if(e instanceof Command)
				return getSimpleClassName(e);
			else
				return getSimpleClassName(e);
		}
		return "";
	}

	/**
	 * Attempts to load the given class, by fully qualified name
	 * @param className the class name
	 * @return the class loaded
	 * @throws ClassNotFoundException something went wrong
	 */
	@Override
	public final Class<?> loadClass(final String className) throws ClassNotFoundException
	{
		return (loadClass(className, true));
	}

	/**
	 * Finishes loading the class into the underlying classloader by handing the byte data to
	 * the classloader, after building a proper full class name.
	 * @param className the class name
	 * @param classData the byte data of the class to load
	 * @param overPackage the package the class belongs to
	 * @param resolveIt true to link the class, false if this is a drill
	 * @return the class defined
	 * @throws ClassFormatError  something went wrong
	 */
	public final Class<?> finishDefineClass(String className, final byte[] classData, final String overPackage, final boolean resolveIt)
		throws ClassFormatError
	{
		Class<?> result=null;
		if(overPackage!=null)
		{
			final int x=className.lastIndexOf('.');
			if(x>=0)
				className=overPackage+className.substring(x);
			else
				className=overPackage+"."+className;
		}
		try{result=defineClass(className, classData, 0, classData.length);}
		catch(final NoClassDefFoundError e)
		{
			if(e.getMessage().toLowerCase().indexOf("(wrong name:")>=0)
			{
				final int x=className.lastIndexOf('.');
				if(x>=0)
				{
					final String notherName=className.substring(x+1);
					result=defineClass(notherName, classData, 0, classData.length);
				}
				else
					throw e;
			}
			else
				throw e;
		}
		if (result==null)
		{
			throw new ClassFormatError();
		}
		if (resolveIt)
		{
			resolveClass(result);
		}
		
		if(debugging)
			Log.debugOut("CMClass","Loaded: "+result.getName());
		
		classes.put(className, result);
		return result;
	}

	/**
	 * Attempts to load the given class, by fully qualified name. This is fun
	 * because it will also load javascript classes, if the className ends with
	 * .js instead of .class
	 * This is the required version of loadClass&lt;?&gt; which is called
	 * both from loadClass&lt;?&gt; above and from the internal function
	 * FindClassFromClass.
	 * @param className the class name
	 * @param resolveIt true to link the class, false if this is a drill
	 * @return the class loaded
	 * @throws ClassNotFoundException something went wrong
	 */
	@Override
	public synchronized final Class<?> loadClass(String className, final boolean resolveIt)
		throws ClassNotFoundException
	{
		String pathName=null;
		if(className.endsWith(".class")) 
			className=className.substring(0,className.length()-6);
		if(className.toUpperCase().endsWith(".JS"))
		{
			pathName=className.substring(0,className.length()-3).replace('.','/')+className.substring(className.length()-3);
			className=className.substring(0,className.length()-3);
		}
		else
			pathName=className.replace('.','/')+".class";
		Class<?> result = classes.get(className);
		if (result!=null)
		{
			if(debugging)
				Log.debugOut("CMClass","Loaded: "+result.getName());
			return result;
		}
		if((super.findLoadedClass(className)!=null)
		||(className.indexOf("com.planet_ink.coffee_mud.")<0)
		||(className.startsWith("com.planet_ink.coffee_mud.core."))
		||(className.startsWith("com.planet_ink.coffee_mud.application."))
		||(className.indexOf(".interfaces.")>=0))
		{
			try
			{
				result=super.findSystemClass(className);
				if(result!=null)
				{
					if(debugging)
						Log.debugOut("CMClass","Loaded: "+result.getName());
					return result;
				}
			}
			catch (final Exception t)
			{
			}
		}
		/* Try to load it from our repository */
		final CMFile CF=new CMFile(pathName,null);
		final byte[] classData=CF.raw();
		if((classData==null)||(classData.length==0))
		{
			throw new ClassNotFoundException("File "+pathName+" not readable!");
		}
		if(CF.getName().toUpperCase().endsWith(".JS"))
		{
			final String name=CF.getName().substring(0,CF.getName().length()-3);
			final StringBuffer str=CF.textVersion(classData);
			if((str==null)||(str.length()==0))
				throw new ClassNotFoundException("JavaScript file "+pathName+" not readable!");
			final List<String> V=Resources.getFileLineVector(str);
			Class<?> extendsClass=null;
			final Vector<Class<?>> implementsClasses=new Vector<Class<?>>();
			String overPackage=null;
			for(int v=0;v<V.size();v++)
			{
				if((extendsClass==null)&&V.get(v).trim().toUpperCase().startsWith("//EXTENDS "))
				{
					final String extendName=V.get(v).trim().substring(10).trim();
					try
					{
						extendsClass=loadClass(extendName);
					}
					catch(final ClassNotFoundException e)
					{
						Log.errOut("CMClass","Could not load "+CF.getName()+" from "+className+" because "+extendName+" is an invalid extension.");
						throw e;
					}
				}
				if((overPackage==null)&&V.get(v).trim().toUpperCase().startsWith("//PACKAGE "))
					overPackage=V.get(v).trim().substring(10).trim();
				if(V.get(v).toUpperCase().startsWith("//IMPLEMENTS "))
				{
					final String extendName=V.get(v).substring(13).trim();
					Class<?> C=null;
					try
					{
						C=loadClass(extendName);
					}
					catch(final ClassNotFoundException e)
					{
						continue;
					}
					implementsClasses.addElement(C);
				}
			}
			final Context X=Context.enter();
			final JScriptLib jlib=new JScriptLib();
			X.initStandardObjects(jlib);
			jlib.defineFunctionProperties(JScriptLib.functions, JScriptLib.class, ScriptableObject.DONTENUM);
			final CompilerEnvirons ce = new CompilerEnvirons();
			ce.initFromContext(X);
			final ClassCompiler cc = new ClassCompiler(ce);
			if(extendsClass==null)
				Log.errOut("CMClass","Warning: "+CF.getVFSPathAndName()+" does not extend any class!");
			else
				cc.setTargetExtends(extendsClass);
			Class<?> mainClass=null;
			if(implementsClasses.size()>0)
			{
				final Class[] CS=new Class[implementsClasses.size()];
				for(int i=0;i<implementsClasses.size();i++) 
					CS[i]=implementsClasses.elementAt(i);
				cc.setTargetImplements(CS);
			}
			final Object[] objs = cc.compileToClassFiles(str.toString(), "script", 1, name);
			for (int i=0;i<objs.length;i+=2)
			{
				final Class<?> C=finishDefineClass((String)objs[i],(byte[])objs[i+1],overPackage,resolveIt);
				if(mainClass==null) 
					mainClass=C;
			}
			Context.exit();
			if((debugging)&&(mainClass!=null))
				Log.debugOut("CMClass","Loaded: "+mainClass.getName());
			return mainClass;
		}
		result=finishDefineClass(className,classData,null,resolveIt);
		return result;
	}

	/**
	 * Causes the map of command words associated with command objects
	 * to be re-mapped, so that users can use them.
	 */
	protected static final void reloadCommandWords()
	{
		c().commandWords.clear();
		Command C;
		String[] wordList;
		for(int c=0;c<c().commands.size();c++)
		{
			C=c().commands.elementAt(c);
			wordList=C.getAccessWords();
			if(wordList!=null)
			{
				for (final String element : wordList)
					c().commandWords.put(element.trim().toUpperCase(),C);
			}
		}
	}

	/**
	 * Making good use of the class path directories from the INI file, this will load
	 * all the damn classes in coffeemud, being nice enough to report them to the log
	 * as it does so
	 * @param page the coffeemud.ini file
	 * @return true if success happened, and false otherwise
	 */
	public static final boolean loadAllCoffeeMudClasses(final CMProps page)
	{
		CMClass c=c();
		if(c==null)
			c=new CMClass();
		final CMClass baseC=clss[MudHost.MAIN_HOST];
		final char tCode=Thread.currentThread().getThreadGroup().getName().charAt(0);
		// wait for baseC
		while((tCode!=MudHost.MAIN_HOST)&&(!classLoaderSync[0]))
		{
			try
			{
				Thread.sleep(500);
			}
			catch(final Exception e)
			{ 
				break;
			}
		}
		try
		{
			final String prefix="com/planet_ink/coffee_mud/";
			debugging=CMSecurity.isDebugging(CMSecurity.DbgFlag.CLASSLOADER);

			c.libraries=loadVectorListToObj(prefix+"Libraries/",page.getStr("LIBRARY"),CMObjectType.LIBRARY.ancestorName);
			if(c.libraries.size()==0) 
				return false;
			CMLib.registerLibraries(c.libraries.elements());
			if(CMLib.unregistered().length()>0)
			{
				Log.errOut("CMClass","Fatal Error: libraries are unregistered: "+CMLib.unregistered().substring(0,CMLib.unregistered().length()-2));
				return false;
			}
			CMLib.propertiesLoaded(); // cause props loaded on libraries, necc for some stuff

			if((tCode!=MudHost.MAIN_HOST)&&(!CMProps.isPrivateToMe("COMMON")))
				c.common=baseC.common;
			else
				c.common=loadHashListToObj(prefix+"Common/",page.getStr("COMMON"),CMObjectType.COMMON.ancestorName);
			if(c.common.size()==0)
				return false;

			if((tCode!=MudHost.MAIN_HOST)&&(!CMProps.isPrivateToMe("WEBMACROS")))
				c.webMacros=baseC.webMacros;
			else
			{
				c.webMacros=CMClass.loadHashListToObj(prefix+"WebMacros/", "%DEFAULT%",CMObjectType.WEBMACRO.ancestorName);
				Log.sysOut(Thread.currentThread().getName(),"WebMacros loaded  : "+c.webMacros.size());
				for(final Enumeration e=c.webMacros.keys();e.hasMoreElements();)
				{
					final String key=(String)e.nextElement();
					if(key.length()>longestWebMacro)
						longestWebMacro=key.length();
				}
			}

			if((tCode!=MudHost.MAIN_HOST)&&(!CMProps.isPrivateToMe("RACE")))
				c.races=baseC.races;
			else
			{
				c.races=loadVectorListToObj(prefix+"Races/",page.getStr("RACES"),CMObjectType.RACE.ancestorName);
				//Log.sysOut(Thread.currentThread().getName(),"Races loaded      : "+c.races.size());
			}
			if(c.races.size()==0) 
				return false;

			if((tCode!=MudHost.MAIN_HOST)&&(!CMProps.isPrivateToMe("CHARCLASS")))
				c.charClasses=baseC.charClasses;
			else
			{
				c.charClasses=loadVectorListToObj(prefix+"CharClasses/",page.getStr("CHARCLASSES"),CMObjectType.CHARCLASS.ancestorName);
				//Log.sysOut(Thread.currentThread().getName(),"Classes loaded    : "+c.charClasses.size());
			}
			if(c.charClasses.size()==0) 
				return false;

			if((tCode!=MudHost.MAIN_HOST)&&(!CMProps.isPrivateToMe("MOB")))
				c.MOBs=baseC.MOBs;
			else
			{
				c.MOBs=loadVectorListToObj(prefix+"MOBS/",page.getStr("MOBS"),CMObjectType.MOB.ancestorName);
				Log.sysOut(Thread.currentThread().getName(),"MOB Types loaded  : "+c.MOBs.size());
			}
			if(c.MOBs.size()==0) 
				return false;

			if((tCode!=MudHost.MAIN_HOST)&&(!CMProps.isPrivateToMe("EXIT")))
				c.exits=baseC.exits;
			else
			{
				c.exits=loadVectorListToObj(prefix+"Exits/",page.getStr("EXITS"),CMObjectType.EXIT.ancestorName);
				Log.sysOut(Thread.currentThread().getName(),"Exit Types loaded : "+c.exits.size());
			}
			if(c.exits.size()==0)
				return false;

			if((tCode!=MudHost.MAIN_HOST)&&(!CMProps.isPrivateToMe("AREA")))
				c.areaTypes=baseC.areaTypes;
			else
			{
				c.areaTypes=loadVectorListToObj(prefix+"Areas/",page.getStr("AREAS"),CMObjectType.AREA.ancestorName);
				Log.sysOut(Thread.currentThread().getName(),"Area Types loaded : "+c.areaTypes.size());
			}
			if(c.areaTypes.size()==0) 
				return false;

			if((tCode!=MudHost.MAIN_HOST)&&(!CMProps.isPrivateToMe("LOCALE")))
				c.locales=baseC.locales;
			else
			{
				c.locales=loadVectorListToObj(prefix+"Locales/",page.getStr("LOCALES"),CMObjectType.LOCALE.ancestorName);
				Log.sysOut(Thread.currentThread().getName(),"Locales loaded    : "+c.locales.size());
			}
			if(c.locales.size()==0) 
				return false;

			if((tCode!=MudHost.MAIN_HOST)&&(!CMProps.isPrivateToMe("ABILITY")))
				c.abilities=baseC.abilities;
			else
			{
				c.abilities=loadVectorListToObj(prefix+"Abilities/",page.getStr("ABILITIES"),CMObjectType.ABILITY.ancestorName);
				if(c.abilities.size()==0) 
					return false;
				if((page.getStr("ABILITIES")!=null)
				&&(page.getStr("ABILITIES").toUpperCase().indexOf("%DEFAULT%")>=0))
				{
					Vector<Ability> tempV;
					int size=0;
					tempV=loadVectorListToObj(prefix+"Abilities/Fighter/","%DEFAULT%",CMObjectType.ABILITY.ancestorName);
					size=tempV.size();
					c.abilities.addAll(tempV);

					tempV=loadVectorListToObj(prefix+"Abilities/Ranger/","%DEFAULT%",CMObjectType.ABILITY.ancestorName);
					size+=tempV.size();
					c.abilities.addAll(tempV);

					tempV=loadVectorListToObj(prefix+"Abilities/Paladin/","%DEFAULT%",CMObjectType.ABILITY.ancestorName);
					size+=tempV.size();
					c.abilities.addAll(tempV);

					if(size>0)
						Log.sysOut(Thread.currentThread().getName(),"Fighter Skills    : "+size);

					tempV=loadVectorListToObj(prefix+"Abilities/Druid/","%DEFAULT%",CMObjectType.ABILITY.ancestorName);
					if(tempV.size()>0)
						Log.sysOut(Thread.currentThread().getName(),"Chants loaded     : "+tempV.size());
					c.abilities.addAll(tempV);

					tempV=loadVectorListToObj(prefix+"Abilities/Languages/","%DEFAULT%",CMObjectType.ABILITY.ancestorName);
					if(tempV.size()>0)
						Log.sysOut(Thread.currentThread().getName(),"Languages loaded  : "+tempV.size());
					c.abilities.addAll(tempV);

					tempV=loadVectorListToObj(prefix+"Abilities/Properties/","%DEFAULT%",CMObjectType.ABILITY.ancestorName);
					size=tempV.size();
					c.abilities.addAll(tempV);

					tempV=loadVectorListToObj(prefix+"Abilities/Diseases/","%DEFAULT%",CMObjectType.ABILITY.ancestorName);
					size+=tempV.size();
					c.abilities.addAll(tempV);

					tempV=loadVectorListToObj(prefix+"Abilities/Poisons/","%DEFAULT%",CMObjectType.ABILITY.ancestorName);
					size+=tempV.size();
					c.abilities.addAll(tempV);

					tempV=loadVectorListToObj(prefix+"Abilities/Misc/","%DEFAULT%",CMObjectType.ABILITY.ancestorName);
					size+=tempV.size();
					Log.sysOut(Thread.currentThread().getName(),"Properties loaded : "+size);
					c.abilities.addAll(tempV);

					tempV=loadVectorListToObj(prefix+"Abilities/Prayers/","%DEFAULT%",CMObjectType.ABILITY.ancestorName);
					Log.sysOut(Thread.currentThread().getName(),"Prayers loaded    : "+tempV.size());
					c.abilities.addAll(tempV);

					tempV=loadVectorListToObj(prefix+"Abilities/Thief/","%DEFAULT%",CMObjectType.ABILITY.ancestorName);
					//size+=tempV.size();
					c.abilities.addAll(tempV);
					if(tempV.size()>0)
						Log.sysOut(Thread.currentThread().getName(),"Thief Skills      : "+tempV.size());

					tempV=loadVectorListToObj(prefix+"Abilities/Archon/","%DEFAULT%",CMObjectType.ABILITY.ancestorName);
					size+=tempV.size();
					c.abilities.addAll(tempV);

					tempV=loadVectorListToObj(prefix+"Abilities/Skills/","%DEFAULT%",CMObjectType.ABILITY.ancestorName);
					size=tempV.size();
					c.abilities.addAll(tempV);

					tempV=loadVectorListToObj(prefix+"Abilities/Common/","%DEFAULT%",CMObjectType.ABILITY.ancestorName);
					size+=tempV.size();
					c.abilities.addAll(tempV);

					tempV=loadVectorListToObj(prefix+"Abilities/Specializations/","%DEFAULT%",CMObjectType.ABILITY.ancestorName);
					size+=tempV.size();
					c.abilities.addAll(tempV);
					if(size>0)
						Log.sysOut(Thread.currentThread().getName(),"Skills loaded     : "+size);

					tempV=loadVectorListToObj(prefix+"Abilities/Songs/","%DEFAULT%",CMObjectType.ABILITY.ancestorName);
					if(tempV.size()>0)
						Log.sysOut(Thread.currentThread().getName(),"Songs loaded      : "+tempV.size());
					c.abilities.addAll(tempV);

					tempV=loadVectorListToObj(prefix+"Abilities/Spells/","%DEFAULT%",CMObjectType.ABILITY.ancestorName);
					if(tempV.size()>0)
						Log.sysOut(Thread.currentThread().getName(),"Spells loaded     : "+tempV.size());
					c.abilities.addAll(tempV);

					tempV=loadVectorListToObj(prefix+"Abilities/SuperPowers/","%DEFAULT%",CMObjectType.ABILITY.ancestorName);
					size=tempV.size();
					c.abilities.addAll(tempV);
					if(size>0)
						Log.sysOut(Thread.currentThread().getName(),"Heroics loaded    : "+size);

					tempV=loadVectorListToObj(prefix+"Abilities/Tech/","%DEFAULT%",CMObjectType.ABILITY.ancestorName);
					size=tempV.size();
					c.abilities.addAll(tempV);
					if(size>0)
						Log.sysOut(Thread.currentThread().getName(),"Tech Skills loaded: "+size);

					tempV=loadVectorListToObj(prefix+"Abilities/Traps/","%DEFAULT%",CMObjectType.ABILITY.ancestorName);
					if(tempV.size()>0)
						Log.sysOut(Thread.currentThread().getName(),"Traps loaded      : "+tempV.size());
					c.abilities.addAll(tempV);

					c.abilities.sort();

					CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: reading generic abilities");
					final List<DatabaseEngine.AckRecord> genAbilities=CMLib.database().DBReadAbilities();
					if(genAbilities.size()>0)
					{
						int loaded=0;
						for(final DatabaseEngine.AckRecord rec : genAbilities)
						{
							String type=rec.typeClass();
							if((type==null)||(type.trim().length()==0))
								type="GenAbility";
							final Ability A=(Ability)(CMClass.getAbility(type).copyOf());
							A.setStat("ALLXML",rec.data());
							if((!A.ID().equals("GenAbility"))&&(!A.ID().equals(type)))
							{
								c.abilities.addElement(A);
								loaded++;
							}
						}
						if(loaded>0)
						{
							Log.sysOut(Thread.currentThread().getName(),"GenAbles loaded   : "+loaded);
							c.abilities.sort();
						}
					}
				}
			}

			if((tCode!=MudHost.MAIN_HOST)&&(!CMProps.isPrivateToMe("ITEM")))
				c.items=baseC.items;
			else
			{
				c.items=loadVectorListToObj(prefix+"Items/Basic/",page.getStr("ITEMS"),CMObjectType.ITEM.ancestorName);
				if(c.items.size()>0)
					Log.sysOut(Thread.currentThread().getName(),"Basic Items loaded: "+c.items.size());
			}

			if((tCode!=MudHost.MAIN_HOST)&&(!CMProps.isPrivateToMe("WEAPON")))
				c.weapons=baseC.weapons;
			else
			{
				c.weapons=loadVectorListToObj(prefix+"Items/Weapons/",page.getStr("WEAPONS"),CMObjectType.WEAPON.ancestorName);
				if(c.weapons.size()>0)
					Log.sysOut(Thread.currentThread().getName(),"Weapons loaded    : "+c.weapons.size());
			}

			if((tCode!=MudHost.MAIN_HOST)&&(!CMProps.isPrivateToMe("ARMOR")))
				c.armor=baseC.armor;
			else
			{
				c.armor=loadVectorListToObj(prefix+"Items/Armor/",page.getStr("ARMOR"),CMObjectType.ARMOR.ancestorName);
				if(c.armor.size()>0)
					Log.sysOut(Thread.currentThread().getName(),"Armor loaded      : "+c.armor.size());
			}

			if((tCode!=MudHost.MAIN_HOST)&&(!CMProps.isPrivateToMe("MISCMAGIC")))
				c.miscMagic=baseC.miscMagic;
			else
			{
				c.miscMagic=loadVectorListToObj(prefix+"Items/MiscMagic/",page.getStr("MISCMAGIC"),CMObjectType.MISCMAGIC.ancestorName);
				if(c.miscMagic.size()>0)
					Log.sysOut(Thread.currentThread().getName(),"Magic Items loaded: "+c.miscMagic.size());
			}

			if((tCode!=MudHost.MAIN_HOST)&&(!CMProps.isPrivateToMe("CLANITEMS")))
				c.clanItems=baseC.clanItems;
			else
			{
				c.clanItems=loadVectorListToObj(prefix+"Items/ClanItems/",page.getStr("CLANITEMS"),CMObjectType.CLANITEM.ancestorName);
				if(c.clanItems.size()>0)
					Log.sysOut(Thread.currentThread().getName(),"Clan Items loaded : "+c.clanItems.size());
			}

			if((tCode!=MudHost.MAIN_HOST)&&(!CMProps.isPrivateToMe("TECH")))
				c.tech=baseC.tech;
			else
			{
				Vector<Electronics> tempV;
				c.tech=loadVectorListToObj(prefix+"Items/BasicTech/",page.getStr("TECH"),CMObjectType.TECH.ancestorName);

				tempV=loadVectorListToObj(prefix+"Items/CompTech/",page.getStr("COMPTECH"),CMObjectType.COMPTECH.ancestorName);
				if(tempV.size()>0)
					c.tech.addAll(tempV);
				tempV=loadVectorListToObj(prefix+"Items/Software/",page.getStr("SOFTWARE"),CMObjectType.SOFTWARE.ancestorName);
				if(tempV.size()>0)
					c.tech.addAll(tempV);
				if(c.tech.size()>0)
					Log.sysOut(Thread.currentThread().getName(),"Electronics loaded: "+c.tech.size());

				c.tech.sort();
			}

			if((c.items.size()+c.weapons.size()+c.armor.size()+c.tech.size()+c.miscMagic.size()+c.clanItems.size())==0)
				return false;

			if((tCode!=MudHost.MAIN_HOST)&&(!CMProps.isPrivateToMe("BEHAVIOR")))
				c.behaviors=baseC.behaviors;
			else
			{
				c.behaviors=loadVectorListToObj(prefix+"Behaviors/",page.getStr("BEHAVIORS"),CMObjectType.BEHAVIOR.ancestorName);
				Log.sysOut(Thread.currentThread().getName(),"Behaviors loaded  : "+c.behaviors.size());
			}
			if(c.behaviors.size()==0) 
				return false;

			if((tCode!=MudHost.MAIN_HOST)&&(!CMProps.isPrivateToMe("COMMAND")))
			{
				c.commands=baseC.commands;
				c.commandWords=baseC.commandWords;
			}
			else
			{
				c.commands=loadVectorListToObj(prefix+"Commands/",page.getStr("COMMANDS"),CMObjectType.COMMAND.ancestorName);
				Log.sysOut(Thread.currentThread().getName(),"Commands loaded   : "+c.commands.size());
			}
			if(c.commands.size()==0) 
				return false;
		}
		catch(final Exception t)
		{
			t.printStackTrace();
			return false;
		}

		reloadCommandWords();

		// misc startup stuff
		if((tCode==MudHost.MAIN_HOST)||(CMProps.isPrivateToMe("CHARCLASS")))
		{
			for(int i=0;i<c.charClasses.size();i++)
			{
				final CharClass C=c.charClasses.elementAt(i);
				C.copyOf();
			}
		}
		if((tCode==MudHost.MAIN_HOST)||(CMProps.isPrivateToMe("RACE")))
		{
			int numRaces=c.races.size();
			for(int r=0;r<c.races.size();r++)
			{
				final Race R=c.races.elementAt(r);
				R.copyOf();
			}
			CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: reading genRaces");
			final List<DatabaseEngine.AckRecord> genRaces=CMLib.database().DBReadRaces();
			if(genRaces.size()>0)
			{
				for(int r=0;r<genRaces.size();r++)
				{
					final Race GR=(Race)getRace("GenRace").copyOf();
					GR.setRacialParms(genRaces.get(r).data());
					if(!GR.ID().equals("GenRace"))
					{
						addRace(GR);
						numRaces++;
					}
				}
			}
			Log.sysOut(Thread.currentThread().getName(),"Races loaded      : "+numRaces);
		}
		if((tCode==MudHost.MAIN_HOST)||(CMProps.isPrivateToMe("CHARCLASS")))
		{
			int numCharClasses=c.charClasses.size();
			CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: reading genClasses");
			final List<DatabaseEngine.AckRecord> genClasses=CMLib.database().DBReadClasses();
			if(genClasses.size()>0)
			{
				for(int r=0;r<genClasses.size();r++)
				{
					final CharClass CR=(CharClass)(CMClass.getCharClass("GenCharClass").copyOf());
					CR.setClassParms(genClasses.get(r).data());
					if(!CR.ID().equals("GenCharClass"))
					{
						addCharClass(CR);
						numCharClasses++;
					}
				}
			}
			Log.sysOut(Thread.currentThread().getName(),"Classes loaded    : "+numCharClasses);
		}
		CMProps.setUpLowVar(CMProps.Str.MUDSTATUS,"Booting: initializing classes");
		c.intializeClasses();
		if((tCode==MudHost.MAIN_HOST)||(CMProps.isPrivateToMe("EXPERTISES")))
		{
			CMLib.expertises().recompileExpertises();
			Log.sysOut(Thread.currentThread().getName(),"Expertises defined: "+CMLib.expertises().numExpertises());
		}
		if(tCode==MudHost.MAIN_HOST)
			classLoaderSync[0]=true;
		CMClass.lastUpdateTime=System.currentTimeMillis();
		return true;
	}

	/**
	 * Returns a timestamp of the last time there was a change in the full set of classes.
	 * @return the last time there was a change
	 */
	public static long getLastClassUpdatedTime()
	{
		return lastUpdateTime;
	}

	/**
	 * The helper class for full blown JavaScript objects.
	 * @author Bo Zimmerman
	 *
	 */
	protected static final class JScriptLib extends ScriptableObject
	{
		@Override
		public String getClassName()
		{
			return "JScriptLib";
		}

		static final long		serialVersionUID	= 47;
		public static String[]	functions			= { "toJavaString" };

		public String toJavaString(Object O)
		{
			return Context.toString(O);
		}
	}

	/**
	 * CMMsg objects are normally re-used, and this method is the recycle bin.
	 * If the msg were to have been garbage collected, it would get returned here
	 * anyway, but this is the nice way to get it done.
	 * @param msg the CMMsg we are done using
	 * @return true if it was returned to the bin, and false if it was allowed to die
	 */
	public static final boolean returnMsg(final CMMsg msg)
	{
		if(MSGS_CACHE.size()<MAX_MSGS)
		{
			synchronized(CMClass.MSGS_CACHE)
			{
				if(MSGS_CACHE.size()<MAX_MSGS)
				{
					MSGS_CACHE.addLast(msg);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns either a CMMsg object from the cache, if one is available, or makes
	 * a new one.
	 * @return a CMMsg object, ready to use.
	 */
	public final static CMMsg getMsg()
	{
		try
		{
			synchronized(MSGS_CACHE)
			{
				return MSGS_CACHE.removeFirst();
			}
		}
		catch(final Exception e)
		{
			return (CMMsg)getCommon("DefaultMessage");
		}
	}

	/**
	 * Creates and configures a CMMsg object for use in the game
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#source()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#sourceCode()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#sourceMessage()
	 * @param source the agent source of the action 
	 * @param newAllCode the source, target, and others code to use
	 * @param allMessage the source, target, and others string msg to send
	 * @return the CMMsg Object
	 */
	public static final CMMsg getMsg(final MOB source, final int newAllCode, final String allMessage)
	{
		final CMMsg M = getMsg();
		M.modify(source, newAllCode, allMessage);
		return M;
	}
	
	/**
	 * Creates and configures a CMMsg object for use in the game
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#source()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#sourceCode()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#sourceMessage()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#value()
	 * @param source the agent source of the action 
	 * @param newAllCode the source, target, and others code to use
	 * @param allMessage the source, target, and others string msg to send
	 * @param newValue the value to set on the message
	 * @return the CMMsg Object
	 */
	public static final CMMsg getMsg(final MOB source, final int newAllCode, final String allMessage, final int newValue)
	{
		final CMMsg M = getMsg();
		M.modify(source, newAllCode, allMessage, newValue);
		return M;
	}

	/**
	 * Creates and configures a CMMsg object for use in the game
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#source()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#target()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#sourceCode()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#sourceMessage()
	 * @param source the agent source of the action 
	 * @param target the target of the action 
	 * @param newAllCode the source, target, and others code to use
	 * @param allMessage the source, target, and others string msg to send
	 * @return the CMMsg Object
	 */
	public static final CMMsg getMsg(final MOB source, final Environmental target, final int newAllCode, final String allMessage)
	{
		final CMMsg M = getMsg();
		M.modify(source, target, newAllCode, allMessage);
		return M;
	}

	/**
	 * Creates and configures a CMMsg object for use in the game
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#source()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#target()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#tool()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#sourceCode()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#sourceMessage()
	 * @param source the agent source of the action 
	 * @param target the target of the action 
	 * @param tool the tool used by the source to do the action
	 * @param newAllCode the source, target, and others code to use
	 * @param allMessage the source, target, and others string msg to send
	 * @return the CMMsg Object
	 */
	public static final CMMsg getMsg(final MOB source, final Environmental target, final Environmental tool, final int newAllCode, final String allMessage)
	{
		final CMMsg M = getMsg();
		M.modify(source, target, tool, newAllCode, allMessage);
		return M;
	}

	/**
	 * Creates and configures a CMMsg object for use in the game
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#source()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#target()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#tool()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#sourceCode()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#targetCode()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#othersCode()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#sourceMessage()
	 * @param source the agent source of the action 
	 * @param target the target of the action 
	 * @param tool the tool used by the source to do the action
	 * @param newSourceCode the source code for this action
	 * @param newTargetCode the target code for this action
	 * @param newOthersCode the others/observed code for this action
	 * @param allMessage the source, target, and others string msg to send
	 * @return the CMMsg Object
	 */
	public static final CMMsg getMsg(final MOB source, final Environmental target, final Environmental tool, final int newSourceCode, final int newTargetCode,
									 final int newOthersCode, final String allMessage)
	{
		final CMMsg M = getMsg();
		M.modify(source, target, tool, newSourceCode, newTargetCode, newOthersCode, allMessage);
		return M;
	}

	/**
	 * Creates and configures a CMMsg object for use in the game
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#source()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#target()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#tool()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#sourceCode()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#sourceMessage()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#targetMessage()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#othersMessage()
	 * @param source the agent source of the action 
	 * @param target the target of the action 
	 * @param tool the tool used by the source to do the action
	 * @param newAllCode the source, target, and others code to use
	 * @param sourceMessage the action/message as seen by the source
	 * @param targetMessage the action/message as seen by the target
	 * @param othersMessage  the action/message as seen by everyone else
	 * @return the CMMsg Object
	 */
	public static final CMMsg getMsg(final MOB source, final Environmental target, final Environmental tool, final int newAllCode, final String sourceMessage,
									 final String targetMessage, final String othersMessage)
	{
		final CMMsg M = getMsg();
		M.modify(source, target, tool, newAllCode, sourceMessage, newAllCode, targetMessage, newAllCode, othersMessage);
		return M;
	}

	/**
	 * Creates and configures a CMMsg object for use in the game
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#source()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#target()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#tool()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#sourceCode()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#targetCode()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#othersCode()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#sourceMessage()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#targetMessage()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#othersMessage()
	 * @param source the agent source of the action 
	 * @param target the target of the action 
	 * @param tool the tool used by the source to do the action
	 * @param newSourceCode the source code for this action
	 * @param sourceMessage the action/message as seen by the source
	 * @param newTargetCode the target code for this action
	 * @param targetMessage the action/message as seen by the target
	 * @param newOthersCode the others/observed code for this action
	 * @param othersMessage  the action/message as seen by everyone else
	 * @return the CMMsg Object
	 */
	public static final CMMsg getMsg(final MOB source, final Environmental target, final Environmental tool, final int newSourceCode, final String sourceMessage,
									 final int newTargetCode, final String targetMessage, final int newOthersCode, final String othersMessage)
	{
		final CMMsg M = getMsg();
		M.modify(source, target, tool, newSourceCode, sourceMessage, newTargetCode, targetMessage, newOthersCode, othersMessage);
		return M;
	}

	/**
	 * Factory mob objects are normally re-used, and this method is the recycle bin.
	 * If the mob were to have been garbage collected, it would get returned here
	 * anyway, but this is the nice way to get it done.
	 * @param mob the mob we are done using
	 * @return true if it was returned to the bin, and false if it was allowed to die
	 */
	public static final boolean returnMob(final MOB mob)
	{
		if(MOB_CACHE.size()<MAX_MOBS)
		{
			synchronized(CMClass.MOB_CACHE)
			{
				MOB_CACHE.addLast(mob);
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns either a StdFactoryMOB object from the cache, if one is available, or makes
	 * a new one.
	 * @return a StdFactoryMOB object, ready to use.
	 */
	public final static MOB getFactoryMOB()
	{
		try
		{
			synchronized(MOB_CACHE)
			{
				return MOB_CACHE.removeFirst();
			}
		}
		catch(final Exception e)
		{
			return getMOB("StdFactoryMOB");
		}
	}

	/**
	 * Returns either a StdFactoryMOB object from the cache, if one is available, or makes
	 * a new one, giving him the name, level, and room location given.
	 * @param name the name to give the mob
	 * @param level the level to give the mob
	 * @param room the room to set the mobs location at
	 * @return a StdFactoryMOB object, ready to use.
	 */
	public final static MOB getFactoryMOB(final String name, final int level, final Room room)
	{
		final MOB mob2=CMClass.getFactoryMOB();
		mob2.setName(name);
		mob2.basePhyStats().setLevel(level);
		mob2.phyStats().setLevel(level);
		mob2.setLocation(room);
		return mob2;
	}

	/**
	 * Unloads all the classes in this system.
	 * Why, I do not know.
	 */
	public static final void shutdown()
	{
		for (final CMClass cls : clss)
		{
			if(cls!=null)
				cls.unload();
		}
		classLoaderSync[0]=false;
	}

	/**
	 * Clears all the class sets in this loader.
	 * I don't know why.
	 */
	public final void unload()
	{
		common.clear();
		races.clear();
		charClasses.clear();
		MOBs.clear();
		abilities.clear();
		locales.clear();
		exits.clear();
		items.clear();
		behaviors.clear();
		weapons.clear();
		armor.clear();
		miscMagic.clear();
		tech.clear();
		areaTypes.clear();
		clanItems.clear();
		commands.clear();
		webMacros.clear();
		commandWords.clear();
	}
}
