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
   Copyright 2000-2014 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
public class ThinRoom implements Room {

	public String ID(){return "ThinRoom";}
	public String name(){ return "A Thin Room";}
	public String Name(){return name();}
	public String description(){return "";}
	public String displayText(){return "";}
	public String displayText(MOB mob){return "";}
	public String description(MOB mob){return "";}
	public int maxRange(){return 5;}
	public int minRange(){return 0;}
	public boolean isSavable(){ return false;}
	public void setSavable(boolean truefalse){}
	public long getTickStatus(){return Tickable.STATUS_NOT;}
	protected String roomID="";
	protected Area myArea=null;
	protected static final Vector empty=new ReadOnlyVector(1);
	protected static final Exit[] exits=new Exit[Directions.NUM_DIRECTIONS()];
	protected static final Room[] rooms=new Room[Directions.NUM_DIRECTIONS()];
	public String roomID(){return roomID;}
	public void setRoomID(String newRoomID){roomID=newRoomID;}
	public void initializeClass(){}
	public String getContextName(Environmental E){return E==null?"nothing":E.name();}

	public int getAtmosphereCode() { return ATMOSPHERE_INHERIT; }
	public int getAtmosphere() { return (myArea!=null)?myArea.getAtmosphere():RawMaterial.RESOURCE_AIR; }
	public void setAtmosphere(int resourceCode) { }
	public int domainType(){return Room.DOMAIN_OUTDOORS_CITY;}
	public int getClimateTypeCode(){return CLIMASK_INHERIT; }
	public int getClimateType() { return (myArea==null)?CLIMASK_NORMAL:myArea.getClimateType(); }
	public void setClimateType(int climate){ }
	public int myResource(){return -1;}
	public void setResource(int resourceCode){}
	public List<Integer> resourceChoices(){return empty;}
	public void toggleMobility(boolean onoff){}
	public boolean getMobility(){return true;}
	private boolean recurse=false;
	public boolean isHere(Environmental E){return false;}
	public void setRawExit(int direction, Environmental E){
		if(E instanceof Room)
			exits[direction]=((Room)E).getRawExit(direction);
		else
		if(E instanceof Exit)
			exits[direction]=(Exit)E;
		else
			exits[direction]=null;
	}
	
	public Room prepareRoomInDir(Room R, int direction)
	{
		if(R==null) return null;
		if((roomID==null)||(roomID.length()==0)||(recurse)) return null;
		recurse=true;
		Room myR=null;
		synchronized(("SYNC"+roomID).intern())
		{
			myR=CMLib.map().getRoom(roomID);
			if(myR==null)
			{
				Map<String,Room> V=CMLib.database().DBReadRoomData(roomID,false);
				if(V.size()>0)
				{
					Iterator<String> i=V.keySet().iterator();
					myR=V.get(i.next());
					while(i.hasNext()) V.remove(i.next());
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
			R.rawDoors()[direction]=myR;
		recurse=false;
		if(myR instanceof ThinRoom) return myR;
		if(myR!=null) return myR.prepareRoomInDir(R,direction);
		return null;
	}

	public void startItemRejuv(){}
	public void recoverRoomStats(){}
	public long expirationDate(){return 0;}
	public void setExpirationDate(long time){}

	public void clearSky(){}
	public void giveASky(int zero){}
	public boolean isSameRoom(Object O){return this==O;}

	public Area getArea(){return myArea;}
	public void setArea(Area newArea){myArea=newArea;}
	public void setGridParent(GridLocale room){}
	public GridLocale getGridParent(){return null;}
	public Room[] rawDoors(){return rooms;}
	public Exit getRawExit(int dir)
	{
		if(dir<exits.length)
			return exits[dir];
		return null;
	}
	
	public Exit getReverseExit(int direction){return null;}
	public Exit getPairedExit(int direction){return null;}
	public Room getRoomInDir(int direction){return null;}
	public Exit getExitInDir(int direction){return null;}

	public int pointsPerMove(MOB mob){return 0;}
	public int thirstPerRound(MOB mob){return 0;}

	public void send(MOB source, CMMsg msg){}
	public void sendOthers(MOB source, CMMsg msg){}
	public void showHappens(int allCode, String allMessage){}
	public void showHappens(int allCode, Environmental like, String allMessage){}
	public boolean show(MOB source,
						Environmental target,
						int allCode,
						String allMessage){return true;}
	public boolean show(MOB source,
						Environmental target,
						Environmental tool,
						int allCode,
						String allMessage){return true;}
	public boolean show(MOB source,
						Environmental target,
						Environmental tool,
						int srcCode,
						int tarCode,
						int othCode,
						String allMessage){return true;}
	public boolean show(MOB source,
						Environmental target,
						Environmental tool,
						int srcCode,
						String srcMessage,
						int tarCode,
						String tarMessage,
						int othCode,
						String othMessage){return true;}
	public boolean show(MOB source,
						Environmental target,
						Environmental tool,
						int allCode,
						String srcMessage,
						String tarMessage,
						String othMessage){return true;}
	public boolean showOthers(MOB source,
							  Environmental target,
							  int allCode,
							  String allMessage){return true;}
	public boolean showSource(MOB source,
							  Environmental target,
							  int allCode,
							  String allMessage){return true;}
	public boolean showOthers(MOB source,
							  Environmental target,
							  Environmental tool,
							  int allCode,
							  String allMessage){return true;}
	public boolean showSource(MOB source,
							  Environmental target,
							  Environmental tool,
							  int allCode,
							  String allMessage){return true;}

	public void eachInhabitant(final EachApplicable<MOB> applier){}
	public MOB fetchInhabitant(String inhabitantID){return null;}
	public MOB fetchRandomInhabitant() { return null; }
	public List<MOB> fetchInhabitants(String inhabitantID){return new Vector(1);}
	public void addInhabitant(MOB mob){}
	public void delInhabitant(MOB mob){}
	public void delAllInhabitants(boolean destroy){}
	public int numInhabitants(){return 0;}
	public Enumeration<MOB> inhabitants(){return EmptyEnumeration.INSTANCE;}
	public boolean isInhabitant(MOB mob){return false;}
	public MOB fetchInhabitant(int i){return null;}
	public int numPCInhabitants(){return 0;}
	public MOB fetchPCInhabitant(int i){return null;}
	public void bringMobHere(MOB mob, boolean andFollowers){}
	public void setName(String newName){}
	public void setDescription(String newDescription){}
	public void setDisplayText(String newDisplayText){}
	public String image(){return "";}
	public String rawImage(){return "";}
	public void setImage(String newImage){}
	public String name(MOB viewerMob) { return name(); }

	public void addItem(Item item){}
	public void addItem(Item item, Expire expire){}
	public void delItem(Item item){}
	public void delAllItems(boolean destroy){}
	public int numItems(){return 0;}
	public boolean isContent(Item item){return false;}
	public Item findItem(Item goodLocation, String itemID){return null;}
	public Item getItem(int i){return null;}
	public void eachItem(final EachApplicable<Item> applier){}
	public Item getRandomItem(){return null;}
	public Enumeration<Item> items(){ return EmptyEnumeration.INSTANCE;}
	public Item getItem(String s){return null;}
	public Item getItem(Item goodLocation, String s){return null;}
	public Item findItem(String itemID){return null;}
	public void moveItemTo(Item item, Expire expire, Move... moveFlags){}
	public void moveItemTo(Item container) {}
	public List<Item> findItems(String itemID) { return new Vector(1);}
	public List<Item> findItems(Item goodLocation, String itemID) { return new Vector(1);}

	public PhysicalAgent fetchFromRoomFavorItems(Item goodLocation, String thingName){return null;}
	public PhysicalAgent fetchFromMOBRoomItemExit(MOB mob, Item goodLocation, String thingName, Filterer<Environmental> filter){return null;}
	public PhysicalAgent fetchFromRoomFavorMOBs(Item goodLocation, String thingName){return null;}
	public PhysicalAgent fetchFromMOBRoomFavorsItems(MOB mob, Item goodLocation, String thingName, Filterer<Environmental> filter){return null;}
	public PhysicalAgent fetchFromMOBRoomFavorsMOBs(MOB mob, Item goodLocation, String thingName, Filterer<Environmental> filter){return null;}

	public void addEffect(Ability to){}
	public void addNonUninvokableEffect(Ability to){}
	public void delEffect(Ability to){}
	public void delAllEffects(boolean unInvoke){}
	public void eachEffect(final EachApplicable<Ability> applier){}
	public int numEffects(){ return 0;}
	public Enumeration<Ability> effects(){return EmptyEnumeration.INSTANCE;}
	public Ability fetchEffect(int index){return null;}
	public Ability fetchEffect(String ID){return null;}
	public void addBehavior(Behavior to){}
	public void delBehavior(Behavior to){}
	public void delAllBehaviors(){}
	public int numBehaviors(){return 0;}
	public Enumeration<Behavior> behaviors() { return EmptyEnumeration.INSTANCE;}
	public Behavior fetchBehavior(int index){return null;}
	public void eachBehavior(final EachApplicable<Behavior> applier){}
	public Behavior fetchBehavior(String ID){return null;}
	public void addScript(ScriptingEngine S){}
	public void delScript(ScriptingEngine S) {}
	public void delAllScripts(){}
	public int numScripts(){return 0;}
	public Enumeration<ScriptingEngine> scripts() { return EmptyEnumeration.INSTANCE;}
	public ScriptingEngine fetchScript(int x){ return null;}
	public void eachScript(final EachApplicable<ScriptingEngine> applier){}
	public boolean isGeneric(){return false;}
	public int getSaveStatIndex(){return getStatCodes().length;}
	private static final String[] CODES={"CLASS"};
	public String[] getStatCodes(){return CODES;}
	public boolean isStat(String code){ return CMParms.indexOf(getStatCodes(),code.toUpperCase().trim())>=0;}
	protected int getCodeNum(String code){
		for(int i=0;i<CODES.length;i++)
			if(code.equalsIgnoreCase(CODES[i])) return i;
		return -1;
	}
	public String getStat(String code){
		switch(getCodeNum(code))
		{
		case 0: return ID();
		}
		return "";
	}
	public void setStat(String code, String val)
	{
		switch(getCodeNum(code))
		{
		case 0: return;
		}
	}
	public boolean sameAs(Environmental E){return E==this;}
	public CMObject copyOf()
	{
		try
		{
			ThinRoom E=(ThinRoom)this.clone();
			//CMClass.bumpCounter(E,CMClass.CMObjectType.LOCALE);//removed for mem & perf
			return E;

		}
		catch(CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}

	private boolean amDestroyed=false;
	public void destroy(){amDestroyed=true;}
	public boolean amDestroyed(){return amDestroyed;}

	protected static final PhyStats phyStats=(PhyStats)CMClass.getCommon("DefaultPhyStats");
	public PhyStats phyStats(){return phyStats;}
	public PhyStats basePhyStats(){return phyStats;}
	//protected void finalize(){ CMClass.unbumpCounter(this,CMClass.CMObjectType.ABILITY); }//removed for mem & perf

	public void recoverPhyStats(){}
	public void setBasePhyStats(PhyStats newStats){}
	public CMObject newInstance()
	{
		try
		{
			return this.getClass().newInstance();
		}
		catch(Exception e)
		{
			Log.errOut(ID(),e);
		}
		return new ThinRoom();
	}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	public void setMiscText(String newMiscText){}
	public String text(){return "";}
	public String miscTextFormat(){return CMParms.FORMAT_UNDEFINED;}

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{}
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{}
	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
	{}
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		return;
	}
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		return true;
	}
	public boolean tick(Tickable ticking, int tickID)
	{ return true;    }
}
