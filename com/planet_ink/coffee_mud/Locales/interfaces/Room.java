package com.planet_ink.coffee_mud.Locales.interfaces;
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
@SuppressWarnings("unchecked")
public interface Room extends Environmental, Affectable, Behavable
{
	public String roomID();
	public void setRoomID(String newRoomID);

	public final static int INDOORS=128;

	public final static int DOMAIN_OUTDOORS_CITY=0;
	public final static int DOMAIN_OUTDOORS_WOODS=1;
	public final static int DOMAIN_OUTDOORS_ROCKS=2;
	public final static int DOMAIN_OUTDOORS_PLAINS=3;
	public final static int DOMAIN_OUTDOORS_UNDERWATER=4;
	public final static int DOMAIN_OUTDOORS_AIR=5;
	public final static int DOMAIN_OUTDOORS_WATERSURFACE=6;
	public final static int DOMAIN_OUTDOORS_JUNGLE=7;
	public final static int DOMAIN_OUTDOORS_SWAMP=8;
	public final static int DOMAIN_OUTDOORS_DESERT=9;
	public final static int DOMAIN_OUTDOORS_HILLS=10;
	public final static int DOMAIN_OUTDOORS_MOUNTAINS=11;
	public final static int DOMAIN_OUTDOORS_SPACEPORT=12;
	public final static String[] outdoorDomainDescs={
		"CITY",
		"WOODS",
		"ROCKY",
		"PLAINS",
		"UNDERWATER",
		"AIR",
		"WATERSURFACE",
		"JUNGLE",
		"SWAMP",
		"DESERT",
		"HILLS",
		"MOUNTAINS",
		"SPACEPORT"};

	public final static int DOMAIN_INDOORS_STONE=INDOORS+0;
	public final static int DOMAIN_INDOORS_WOOD=INDOORS+1;
	public final static int DOMAIN_INDOORS_CAVE=INDOORS+2;
	public final static int DOMAIN_INDOORS_MAGIC=INDOORS+3;
	public final static int DOMAIN_INDOORS_UNDERWATER=INDOORS+4;
	public final static int DOMAIN_INDOORS_AIR=INDOORS+5;
	public final static int DOMAIN_INDOORS_WATERSURFACE=INDOORS+6;
	public final static int DOMAIN_INDOORS_METAL=INDOORS+7;
	public final static String[] indoorDomainDescs={
		"STONE",
		"WOODEN",
		"CAVE",
		"MAGIC",
		"UNDERWATER",
		"AIR",
		"WATERSURFACE",
		"METAL"};

	public final static int CONDITION_NORMAL=0;
	public final static int CONDITION_WET=1;
	public final static int CONDITION_HOT=2;
	public final static int CONDITION_COLD=3;

	public int domainType();
	public int domainConditions();
	public int myResource();
	public void setResource(int resourceCode);
	public Vector resourceChoices();
	public void toggleMobility(boolean onoff);
	public boolean getMobility();
	public void resetVectors();

	public final static String[][] VARIATION_CODES={
		{"SUMMER","S"+TimeClock.SEASON_SUMMER},
		{"SPRING","S"+TimeClock.SEASON_SPRING},
		{"WINTER","S"+TimeClock.SEASON_WINTER},
		{"FALL","S"+TimeClock.SEASON_FALL},
		{"DAY","C"+TimeClock.TIME_DAY},
		{"DAYTIME","C"+TimeClock.TIME_DAY},
		{"NIGHT","C"+TimeClock.TIME_NIGHT},
		{"NIGHTTIME","C"+TimeClock.TIME_NIGHT},
		{"DAWN","C"+TimeClock.TIME_DAWN},
		{"DUSK","C"+TimeClock.TIME_DUSK},
		{"RAIN","W"+Climate.WEATHER_RAIN},
		{"SLEET","W"+Climate.WEATHER_SLEET},
		{"SNOW","W"+Climate.WEATHER_SNOW},
		{"CLEAR","W"+Climate.WEATHER_CLEAR},
		{"HEATWAVE","W"+Climate.WEATHER_HEAT_WAVE},
		{"THUNDERSTORM","W"+Climate.WEATHER_THUNDERSTORM},
		{"BLIZZARD","W"+Climate.WEATHER_BLIZZARD},
		{"WINDY","W"+Climate.WEATHER_WINDY},
		{"DROUGHT","W"+Climate.WEATHER_DROUGHT},
		{"DUSTSTORM","W"+Climate.WEATHER_DUSTSTORM},
		{"COLD","W"+Climate.WEATHER_WINTER_COLD},
		{"HAIL","W"+Climate.WEATHER_HAIL},
		{"CLOUDY","W"+Climate.WEATHER_CLOUDY},
		{"SWIMMING","M"+EnvStats.IS_SWIMMING},
		{"FLYING","M"+EnvStats.IS_FLYING},
		{"CRAWLING","M"+EnvStats.IS_SITTING},
		{"SITTING","M"+EnvStats.IS_SITTING},
		{"FALLING","M"+EnvStats.IS_FALLING},
		{"CLIMBING","M"+EnvStats.IS_CLIMBING},
		{"INVISIBLE","M"+EnvStats.IS_INVISIBLE},
		{"HIDDEN","M"+EnvStats.IS_HIDDEN},
		{"ELSE","\n"},
		{"VARIES","\r"}
	};
	public static final Hashtable VARIATION_CODES_HASHED=CMParms.makeHashtable(VARIATION_CODES);
	

	public void startItemRejuv();
	public void recoverRoomStats();

	public void clearSky();
	public void giveASky(int zero);
	public boolean isSameRoom(Object O);

	public Area getArea();
	public void setArea(Area newArea);
	public void setGridParent(GridLocale room);
	public GridLocale getGridParent();

	public Room[] rawDoors();
	public void setRawExit(int direction, Environmental E);
    public Exit getRawExit(int direction);
	public Exit getReverseExit(int direction);
	public Exit getPairedExit(int direction);
	public Room getRoomInDir(int direction);
	public Exit getExitInDir(int direction);
	public Room prepareRoomInDir(Room fromRoom, int direction);
	

	public int pointsPerMove(MOB mob);
	public int thirstPerRound(MOB mob);

	public String roomTitle(MOB mob);
	public String roomDescription(MOB mob);

	public void send(MOB source, CMMsg msg);
	public void sendOthers(MOB source, CMMsg msg);
	public void showHappens(int allCode, String allMessage);
	public void showHappens(int allCode, Environmental like, String allMessage);
	public boolean show(MOB source,
						Environmental target,
						int allCode,
						String allMessage);
	public boolean show(MOB source,
						Environmental target,
						Environmental tool,
						int allCode,
						String allMessage);
    public boolean show(MOB source,
                        Environmental target,
                        Environmental tool,
                        int srcCode,
                        int tarCode,
                        int othCode,
                        String allMessage);
    public boolean show(MOB source,
                        Environmental target,
                        Environmental tool,
                        int srcCode,
                        String srcMessage,
                        int tarCode,
                        String tarMessage,
                        int othCode,
                        String othMessage);
    public boolean show(MOB source,
                        Environmental target,
                        Environmental tool,
                        int allCode,
                        String srcMessage,
                        String tarMessage,
                        String othMessage);
	public boolean showOthers(MOB source,
						      Environmental target,
						      int allCode,
						      String allMessage);
	public boolean showSource(MOB source,
						      Environmental target,
						      int allCode,
						      String allMessage);
	public boolean showOthers(MOB source,
							  Environmental target,
							  Environmental tool,
							  int allCode,
							  String allMessage);
	public boolean showSource(MOB source,
							  Environmental target,
							  Environmental tool,
							  int allCode,
							  String allMessage);
	public boolean isHere(Environmental E);

	public MOB fetchInhabitant(String inhabitantID);
	public Vector fetchInhabitants(String inhabitantID);
	public void addInhabitant(MOB mob);
	public void delInhabitant(MOB mob);
	public int numInhabitants();
	public boolean isInhabitant(MOB mob);
	public MOB fetchInhabitant(int i);
	public int numPCInhabitants();
	public MOB fetchPCInhabitant(int i);
	public void bringMobHere(MOB mob, boolean andFollowers);

	public String getContextName(Environmental E);
	
	public void addItem(Item item);
	public void addItemRefuse(Item item, int expireMins);
	public void delItem(Item item);
	public int numItems();
	public boolean isContent(Item item);
	public Item fetchItem(Item goodLocation, String itemID);
	public Item fetchItem(int i);
	public Item fetchAnyItem(String itemID);
	public Vector fetchItems(Item goodLocation, String itemID);
	public Vector fetchAnyItems(String itemID);
	public void bringItemHere(Item item, int expireMins, boolean andRiders);

	public Environmental fetchFromRoomFavorItems(Item goodLocation, String thingName,int wornFilter);
	public Environmental fetchFromMOBRoomItemExit(MOB mob, Item goodLocation, String thingName, int wornFilter);
	public Environmental fetchFromRoomFavorMOBs(Item goodLocation, String thingName, int wornFilter);
	public Environmental fetchFromMOBRoomFavorsItems(MOB mob, Item goodLocation, String thingName, int wornFilter);
	public Environmental fetchFromMOBRoomFavorsMOBs(MOB mob, Item goodLocation, String thingName, int wornFilter);
}