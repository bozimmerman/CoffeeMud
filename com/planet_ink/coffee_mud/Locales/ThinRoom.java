package com.planet_ink.coffee_mud.Locales;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Move;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Properties.Property;
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
   Copyright 2006-2020 Bo Zimmerman

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
public class ThinRoom implements Room
{
	@Override
	public String ID()
	{
		return "ThinRoom";
	}

	@Override
	public String name()
	{
		return "A Thin Room";
	}

	@Override
	public String Name()
	{
		return name();
	}

	@Override
	public String description()
	{
		return "";
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	public String displayText(final MOB mob)
	{
		return "";
	}

	@Override
	public String description(final MOB mob)
	{
		return "";
	}

	@Override
	public int maxRange()
	{
		return 5;
	}

	@Override
	public int minRange()
	{
		return 0;
	}

	@Override
	public boolean isSavable()
	{
		return false;
	}

	@Override
	public void setSavable(final boolean truefalse)
	{
	}

	@Override
	public int getTickStatus()
	{
		return Tickable.STATUS_NOT;
	}

	protected String						roomID	= "";
	protected Area							myArea	= null;
	protected static final Vector<Integer>	empty	= new ReadOnlyVector<Integer>(1);
	protected static final Exit[]			exits	= new Exit[Directions.NUM_DIRECTIONS()];
	protected static final Room[]			rooms	= new Room[Directions.NUM_DIRECTIONS()];

	@Override
	public String roomID()
	{
		return roomID;
	}

	@Override
	public void setRoomID(final String newRoomID)
	{
		roomID = newRoomID;
	}

	@Override
	public void initializeClass()
	{
	}

	@Override
	public String getContextName(final Environmental E)
	{
		return E == null ? "nothing" : E.name();
	}

	@Override
	public int getAtmosphereCode()
	{
		return ATMOSPHERE_INHERIT;
	}

	@Override
	public int getAtmosphere()
	{
		return (myArea != null) ? myArea.getAtmosphere() : RawMaterial.RESOURCE_AIR;
	}

	@Override
	public void setAtmosphere(final int resourceCode)
	{
	}

	@Override
	public int domainType()
	{
		return Room.DOMAIN_OUTDOORS_CITY;
	}

	@Override
	public int getClimateTypeCode()
	{
		return CLIMASK_INHERIT;
	}

	@Override
	public int getClimateType()
	{
		return (myArea == null) ? CLIMASK_NORMAL : myArea.getClimateType();
	}

	@Override
	public void setClimateType(final int climate)
	{
	}

	@Override
	public int myResource()
	{
		return -1;
	}

	@Override
	public void setResource(final int resourceCode)
	{
	}

	@Override
	public List<Integer> resourceChoices()
	{
		return empty;
	}

	@Override
	public void toggleMobility(final boolean onoff)
	{
	}

	@Override
	public boolean getMobility()
	{
		return true;
	}

	protected volatile boolean	recurse	= false;

	@Override
	public boolean isHere(final Environmental E)
	{
		return false;
	}

	@Override
	public void setRawExit(final int direction, final Exit E)
	{
		if(E != null)
			exits[direction]=E;
		else
			exits[direction]=null;
	}

	@Override
	public Room prepareRoomInDir(final Room R, final int direction)
	{
		if(R==null)
			return null;
		if((roomID==null)||(roomID.length()==0)||(recurse))
			return null;
		try
		{
			recurse=true;
			Room myR=null;
			synchronized(("SYNC"+roomID).intern())
			{
				myR=CMLib.map().getRoom(roomID);
				if(myR==null)
				{
					myR=CMLib.database().DBReadRoom(roomID,false);
					if(myR!=null)
					{
						CMLib.database().DBReadRoomExits(roomID,myR,false);
						CMLib.database().DBReadContent(roomID,myR,true);
						myR.getArea().fillInAreaRoom(R);
						if(CMath.bset(myR.getArea().flags(),Area.FLAG_THIN))
							myR.setExpirationDate(System.currentTimeMillis()+WorldMap.ROOM_EXPIRATION_MILLIS);
					}
				}
			}
			if((myR!=null)
			&&(direction>=0)
			&&(direction<Directions.NUM_DIRECTIONS())
			&&(R.rawDoors()[direction]==this))
			{
				R.rawDoors()[direction]=myR;
				final MOB mob=CMClass.getFactoryMOB("the wind",1,R);
				try
				{
					R.executeMsg(mob, CMClass.getMsg(mob, R, CMMsg.MSG_NEWROOM, null));
				}
				finally
				{
					mob.destroy();
				}
			}
			if(myR!=null)
			{
				if(myR instanceof ThinRoom) // the purpose of a thin room is to be expanded when requested.
					return null;
				return myR.prepareRoomInDir(R,direction);
			}
		}
		finally
		{
			recurse=false;
		}
		return null;
	}

	@Override
	public void startItemRejuv()
	{
	}

	@Override
	public void recoverRoomStats()
	{
	}

	@Override
	public long expirationDate()
	{
		return 0;
	}

	@Override
	public void setExpirationDate(final long time)
	{
	}

	@Override
	public void clearSky()
	{
	}

	@Override
	public void giveASky(final int depth)
	{
	}

	@Override
	public List<Room> getSky()
	{
		final List<Room> skys = new Vector<Room>(0);
		return skys;
	}

	@Override
	public Area getArea()
	{
		return myArea;
	}

	@Override
	public void setArea(final Area newArea)
	{
		myArea = newArea;
	}

	@Override
	public void setGridParent(final GridLocale room)
	{
	}

	@Override
	public GridLocale getGridParent()
	{
		return null;
	}

	@Override
	public Room[] rawDoors()
	{
		return rooms;
	}

	@Override
	public Exit getRawExit(final int dir)
	{
		if(dir<exits.length)
			return exits[dir];
		return null;
	}

	@Override
	public int getReverseDir(final int direction)
	{
		if((direction<0)||(direction>=Directions.NUM_DIRECTIONS()))
			return -1;
		final Room opRoom=getRoomInDir(direction);
		if(opRoom!=null)
		{
			if(direction == Directions.GATE)
				return Directions.GATE;
			final int formalOpDir=Directions.getOpDirectionCode(direction);
			if(opRoom.rawDoors()[formalOpDir]==this)
				return formalOpDir;
			if(opRoom.getRoomInDir(formalOpDir)==this)
				return formalOpDir;
			for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
			{
				if(opRoom.rawDoors()[d]==this)
					return d;
			}
			return formalOpDir;
		}
		return -1;
	}

	@Override
	public Exit getReverseExit(final int direction)
	{
		final int opDir=getReverseDir(direction);
		if((opDir<0)||(opDir>=Directions.NUM_DIRECTIONS()))
			return null;
		final Room opRoom=getRoomInDir(direction);
		if(opRoom!=null)
			return opRoom.getExitInDir(opDir);
		return null;
	}

	@Override
	public Exit getPairedExit(final int direction)
	{
		final Exit opExit=getReverseExit(direction);
		final Exit myExit=getExitInDir(direction);
		if((myExit==null)||(opExit==null))
			return null;
		if(myExit.hasADoor()!=opExit.hasADoor())
			return null;
		return opExit;
	}

	@Override
	public Room getRoomInDir(final int direction)
	{
		return null;
	}

	@Override
	public Exit getExitInDir(final int direction)
	{
		return null;
	}

	@Override
	public int pointsPerMove()
	{
		return 0;
	}

	@Override
	public int thirstPerRound()
	{
		return 0;
	}

	@Override
	public void send(final MOB source, final CMMsg msg)
	{
	}

	@Override
	public void sendOthers(final MOB source, final CMMsg msg)
	{
	}

	@Override
	public void showHappens(final int allCode, final String allMessage)
	{
	}

	@Override
	public void showHappens(final int allCode, final Environmental like, final String allMessage)
	{
	}

	@Override
	public boolean show(final MOB source,
						final Environmental target,
						final int allCode,
						final String allMessage)
	{
		return true;
	}

	@Override
	public boolean show(final MOB source,
						final Environmental target,
						final Environmental tool,
						final int allCode,
						final String allMessage)
	{
		return true;
	}

	@Override
	public boolean show(final MOB source,
						final Environmental target,
						final Environmental tool,
						final int srcCode,
						final int tarCode,
						final int othCode,
						final String allMessage)
	{
		return true;
	}

	@Override
	public boolean show(final MOB source,
						final Environmental target,
						final Environmental tool,
						final int srcCode,
						final String srcMessage,
						final int tarCode,
						final String tarMessage,
						final int othCode,
						final String othMessage)
	{
		return true;
	}

	@Override
	public boolean show(final MOB source,
						final Environmental target,
						final Environmental tool,
						final int allCode,
						final String srcMessage,
						final String tarMessage,
						final String othMessage)
	{
		return true;
	}

	@Override
	public boolean showOthers(final MOB source,
							  final Environmental target,
							  final int allCode,
							  final String allMessage)
	{
		return true;
	}

	@Override
	public boolean showSource(final MOB source,
							  final Environmental target,
							  final int allCode,
							  final String allMessage)
	{
		return true;
	}

	@Override
	public boolean showOthers(final MOB source,
							  final Environmental target,
							  final Environmental tool,
							  final int allCode,
							  final String allMessage)
	{
		return true;
	}

	@Override
	public boolean showSource(final MOB source,
							  final Environmental target,
							  final Environmental tool,
							  final int allCode,
							  final String allMessage)
	{
		return true;
	}

	@Override
	public void eachInhabitant(final EachApplicable<MOB> applier)
	{
	}

	@Override
	public MOB fetchInhabitant(final String inhabitantID)
	{
		return null;
	}

	@Override
	public MOB fetchInhabitantExact(final String inhabitantID)
	{
		return null;
	}

	@Override
	public MOB fetchRandomInhabitant()
	{
		return null;
	}

	@Override
	public List<MOB> fetchInhabitants(final String inhabitantID)
	{
		return new Vector<MOB>(1);
	}

	@Override
	public void addInhabitant(final MOB mob)
	{
	}

	@Override
	public void delInhabitant(final MOB mob)
	{
	}

	@Override
	public void delAllInhabitants(final boolean destroy)
	{
	}

	@Override
	public int numInhabitants()
	{
		return 0;
	}

	private static final Enumeration<MOB> emptyMobs = new EmptyEnumeration<MOB>();

	@Override
	public Enumeration<MOB> inhabitants()
	{
		return emptyMobs;
	}

	@Override
	public boolean isInhabitant(final MOB mob)
	{
		return false;
	}

	@Override
	public MOB fetchInhabitant(final int i)
	{
		return null;
	}

	@Override
	public int numPCInhabitants()
	{
		return 0;
	}

	public MOB fetchPCInhabitant(final int i)
	{
		return null;
	}

	@Override
	public void bringMobHere(final MOB mob, final boolean andFollowers)
	{
	}

	@Override
	public void setName(final String newName)
	{
	}

	@Override
	public void setDescription(final String newDescription)
	{
	}

	@Override
	public void setDisplayText(final String newDisplayText)
	{
	}

	@Override
	public String image()
	{
		return "";
	}

	@Override
	public String rawImage()
	{
		return "";
	}

	@Override
	public void setImage(final String newImage)
	{
	}

	@Override
	public String name(final MOB viewerMob)
	{
		return name();
	}

	@Override
	public void addItem(final Item item)
	{
	}

	@Override
	public void addItem(final Item item, final Expire expire)
	{
	}

	@Override
	public void delItem(final Item item)
	{
	}

	@Override
	public void delAllItems(final boolean destroy)
	{
	}

	@Override
	public int numItems()
	{
		return 0;
	}

	@Override
	public boolean isContent(final Item item)
	{
		return false;
	}

	@Override
	public Item findItem(final Item goodLocation, final String itemID)
	{
		return null;
	}

	@Override
	public Item getItem(final int i)
	{
		return null;
	}

	@Override
	public void eachItem(final EachApplicable<Item> applier)
	{
	}

	@Override
	public Item getRandomItem()
	{
		return null;
	}

	private static final Enumeration<Item> emptyItems = new EmptyEnumeration<Item>();

	@Override
	public Enumeration<Item> items()
	{
		return emptyItems;
	}

	@Override
	public Enumeration<Item> itemsRecursive()
	{
		return emptyItems;
	}

	public Item getItem(final String s)
	{
		return null;
	}

	public Item getItem(final Item goodLocation, final String s)
	{
		return null;
	}

	@Override
	public Item findItem(final String itemID)
	{
		return null;
	}

	@Override
	public void moveItemTo(final Item item, final Expire expire, final Move... moveFlags)
	{
	}

	@Override
	public void moveItemTo(final Item container)
	{
	}

	@Override
	public List<Item> findItems(final String itemID)
	{
		return new Vector<Item>(1);
	}

	@Override
	public List<Item> findItems(final Item goodLocation, final String itemID)
	{
		return new Vector<Item>(1);
	}

	@Override
	public Exit fetchExit(final String thingName)
	{
		return null;
	}

	@Override
	public PhysicalAgent fetchFromRoomFavorExits(final String thingName)
	{
		return null;
	}

	@Override
	public PhysicalAgent fetchFromRoomFavorItems(final Item goodLocation, final String thingName)
	{
		return null;
	}

	@Override
	public PhysicalAgent fetchFromMOBRoomItemExit(final MOB mob, final Item goodLocation, final String thingName, final Filterer<Environmental> filter)
	{
		return null;
	}

	@Override
	public PhysicalAgent fetchFromRoomFavorMOBs(final Item goodLocation, final String thingName)
	{
		return null;
	}

	@Override
	public PhysicalAgent fetchFromMOBRoomFavorsItems(final MOB mob, final Item goodLocation, final String thingName, final Filterer<Environmental> filter)
	{
		return null;
	}

	@Override
	public PhysicalAgent fetchFromMOBRoomFavorsMOBs(final MOB mob, final Item goodLocation, final String thingName, final Filterer<Environmental> filter)
	{
		return null;
	}

	@Override
	public void addEffect(final Ability to)
	{
	}

	@Override
	public void addNonUninvokableEffect(final Ability to)
	{
	}

	@Override
	public void delEffect(final Ability to)
	{
	}

	@Override
	public void delAllEffects(final boolean unInvoke)
	{
	}

	@Override
	public void eachEffect(final EachApplicable<Ability> applier)
	{
	}

	@Override
	public int numEffects()
	{
		return 0;
	}

	private static final Enumeration<Ability> emptyEffects = new EmptyEnumeration<Ability>();

	@Override
	public Enumeration<Ability> effects()
	{
		return emptyEffects;
	}

	@Override
	public Ability fetchEffect(final int index)
	{
		return null;
	}

	@Override
	public Ability fetchEffect(final String ID)
	{
		return null;
	}

	@Override
	public void addBehavior(final Behavior to)
	{
	}

	@Override
	public void delBehavior(final Behavior to)
	{
	}

	@Override
	public void delAllBehaviors()
	{
	}

	@Override
	public int numBehaviors()
	{
		return 0;
	}

	private static final Enumeration<Behavior> emptyBehaviors = new EmptyEnumeration<Behavior>();

	@Override
	public Enumeration<Behavior> behaviors()
	{
		return emptyBehaviors;
	}

	@Override
	public Behavior fetchBehavior(final int index)
	{
		return null;
	}

	@Override
	public void eachBehavior(final EachApplicable<Behavior> applier)
	{
	}

	@Override
	public Behavior fetchBehavior(final String ID)
	{
		return null;
	}

	@Override
	public void addScript(final ScriptingEngine S)
	{
	}

	@Override
	public void delScript(final ScriptingEngine S)
	{
	}

	@Override
	public void delAllScripts()
	{
	}

	@Override
	public int numScripts()
	{
		return 0;
	}

	private static final Enumeration<ScriptingEngine> emptyScripts = new EmptyEnumeration<ScriptingEngine>();

	@Override
	public Enumeration<ScriptingEngine> scripts()
	{
		return emptyScripts;
	}

	@Override
	public ScriptingEngine fetchScript(final int x)
	{
		return null;
	}

	@Override
	public void eachScript(final EachApplicable<ScriptingEngine> applier)
	{
	}

	@Override
	public boolean isGeneric()
	{
		return false;
	}

	@Override
	public int getSaveStatIndex()
	{
		return getStatCodes().length;
	}

	private static final String[]	CODES	= { "CLASS" };

	@Override
	public String[] getStatCodes()
	{
		return CODES;
	}

	@Override
	public String L(final String str, final String... xs)
	{
		return CMLib.lang().fullSessionTranslation(str, xs);
	}

	@Override
	public boolean isStat(final String code)
	{
		return CMParms.indexOf(getStatCodes(), code.toUpperCase().trim()) >= 0;
	}

	protected int getCodeNum(final String code)
	{
		for(int i=0;i<CODES.length;i++)
		{
			if(code.equalsIgnoreCase(CODES[i]))
				return i;
		}
		return -1;
	}

	@Override
	public String getStat(final String code)
	{
		switch(getCodeNum(code))
		{
		case 0:
			return ID();
		}
		return "";
	}

	@Override
	public void setStat(final String code, final String val)
	{
		switch(getCodeNum(code))
		{
		case 0:
			return;
		}
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		return E == this;
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			final ThinRoom E=(ThinRoom)this.clone();
			//CMClass.bumpCounter(E,CMClass.CMObjectType.LOCALE);//removed for mem & perf
			return E;

		}
		catch(final CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}

	private boolean	amDestroyed	= false;

	@Override
	public void destroy()
	{
		amDestroyed = true;
	}

	@Override
	public boolean amDestroyed()
	{
		return amDestroyed;
	}

	protected static final PhyStats	phyStats	= (PhyStats) CMClass.getCommon("DefaultPhyStats");

	@Override
	public PhyStats phyStats()
	{
		return phyStats;
	}

	@Override
	public PhyStats basePhyStats()
	{
		return phyStats;
	}

	/*
	protected void finalize()
	{
		CMClass.unbumpCounter(this, CMClass.CMObjectType.ABILITY);
	}// removed for mem & perf
	*/

	@Override
	public void recoverPhyStats()
	{
	}

	@Override
	public void setBasePhyStats(final PhyStats newStats)
	{
	}

	@Override
	public CMObject newInstance()
	{
		try
		{
			return this.getClass().newInstance();
		}
		catch (final Exception e)
		{
			Log.errOut(ID(), e);
		}
		return new ThinRoom();
	}

	@Override
	public int compareTo(final CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	@Override
	public void setMiscText(final String newMiscText)
	{
	}

	@Override
	public String text()
	{
		return "";
	}

	@Override
	public String miscTextFormat()
	{
		return CMParms.FORMAT_UNDEFINED;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
	}

	@Override
	public void affectCharStats(final MOB affectedMob, final CharStats affectableStats)
	{
	}

	@Override
	public void affectCharState(final MOB affectedMob, final CharState affectableMaxState)
	{
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		return;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		return true;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		return true;
	}

	@Override
	public int getCombatTurnMobIndex()
	{
		return 0;
	}

	@Override
	public void setCombatTurnMobIndex(final int index)
	{
	}

}
