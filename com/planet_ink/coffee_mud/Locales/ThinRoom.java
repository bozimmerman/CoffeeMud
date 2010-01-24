package com.planet_ink.coffee_mud.Locales;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
@SuppressWarnings("unchecked")
public class ThinRoom implements Room {

	public String ID(){return "ThinRoom";}
	public String name(){ return "A Thin Room";}
	public String Name(){return name();}
	public String description(){return "";}
	public String displayText(){return "";}
	public int maxRange(){return 5;}
	public int minRange(){return 0;}
	public boolean savable(){ return false;}
	public long getTickStatus(){return Tickable.STATUS_NOT;}
	protected String roomID="";
	protected Area myArea=null;
	protected static final Vector empty=new Vector();
	protected static final Exit[] exits=new Exit[Directions.NUM_DIRECTIONS()];
	protected static final Room[] rooms=new Room[Directions.NUM_DIRECTIONS()];
	public String roomID(){return roomID;}
	public void setRoomID(String newRoomID){roomID=newRoomID;}
    public void initializeClass(){}
    public String getContextName(Environmental E){return E==null?"nothing":E.name();}

	public int domainType(){return Room.DOMAIN_OUTDOORS_CITY;}
	public int domainConditions(){return Room.CONDITION_NORMAL;}
	public int myResource(){return -1;}
	public void setResource(int resourceCode){}
    public void resetVectors(){}
	public Vector resourceChoices(){empty.clear(); return empty;}
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
		if((roomID.length()==0)||(recurse)) return null;
		recurse=true;
		Room myR=null;
		synchronized(("SYNC"+roomID).intern())
		{
			myR=CMLib.map().getRoom(roomID);
			if(myR==null)
			{
				Vector V=CMLib.database().DBReadRoomData(roomID,false);
				if(V.size()>0)
				{
					myR=(Room)V.firstElement();
					while(V.size()>1) V.removeElementAt(1);
					CMLib.database().DBReadRoomExits(roomID,V,false);
					CMLib.database().DBReadContent(myR,V);
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

	public String roomTitle(MOB mob){return "";}
	public String roomDescription(MOB mob){return "";}

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

	public MOB fetchInhabitant(String inhabitantID){return null;}
	public Vector fetchInhabitants(String inhabitantID){return new Vector(1);}
	public void addInhabitant(MOB mob){}
	public void delInhabitant(MOB mob){}
	public int numInhabitants(){return 0;}
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

	public void addItem(Item item){}
	public void addItemRefuse(Item item, int expireMins){}
	public void delItem(Item item){}
	public int numItems(){return 0;}
	public boolean isContent(Item item){return false;}
	public Item fetchItem(Item goodLocation, String itemID){return null;}
	public Item fetchItem(int i){return null;}
	public Item fetchAnyItem(String itemID){return null;}
	public void bringItemHere(Item item, int expireMins, boolean andRiders){}
	public Vector fetchAnyItems(String itemID) { return new Vector(1);}
	public Vector fetchItems(Item goodLocation, String itemID) { return new Vector(1);}

	public Environmental fetchFromRoomFavorItems(Item goodLocation, String thingName,int wornFilter){return null;}
	public Environmental fetchFromMOBRoomItemExit(MOB mob, Item goodLocation, String thingName, int wornFilter){return null;}
	public Environmental fetchFromRoomFavorMOBs(Item goodLocation, String thingName, int wornFilter){return null;}
	public Environmental fetchFromMOBRoomFavorsItems(MOB mob, Item goodLocation, String thingName, int wornFilter){return null;}
	public Environmental fetchFromMOBRoomFavorsMOBs(MOB mob, Item goodLocation, String thingName, int wornFilter){return null;}

	public void addEffect(Ability to){}
	public void addNonUninvokableEffect(Ability to){}
	public void delEffect(Ability to){}
	public int numEffects(){ return 0;}
	public Ability fetchEffect(int index){return null;}
	public Ability fetchEffect(String ID){return null;}
	public void addBehavior(Behavior to){}
	public void delBehavior(Behavior to){}
	public int numBehaviors(){return 0;}
	public Behavior fetchBehavior(int index){return null;}
	public Behavior fetchBehavior(String ID){return null;}
    public void addScript(ScriptingEngine S){}
    public void delScript(ScriptingEngine S) {}
    public int numScripts(){return 0;}
    public ScriptingEngine fetchScript(int x){ return null;}
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
            CMClass.bumpCounter(E,CMClass.OBJECT_LOCALE);
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

	protected static final EnvStats envStats=(EnvStats)CMClass.getCommon("DefaultEnvStats");
	public EnvStats envStats(){return envStats;}
	public EnvStats baseEnvStats(){return envStats;}
    protected void finalize(){ CMClass.unbumpCounter(this,CMClass.OBJECT_ABILITY); }

	public void recoverEnvStats(){}
	public void setBaseEnvStats(EnvStats newBaseEnvStats){}
	public CMObject newInstance()
	{
		try
        {
			return (CMObject)this.getClass().newInstance();
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

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{}
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{}
	public void affectCharState(MOB affectedMob, CharState affectableMaxState)
	{}
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		return;
	}
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		return true;
	}
	public boolean tick(Tickable ticking, int tickID)
	{ return true;	}
}
