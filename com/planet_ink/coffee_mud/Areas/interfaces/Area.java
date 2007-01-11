package com.planet_ink.coffee_mud.Areas.interfaces;
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
   Copyright 2000-2007 Bo Zimmerman

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
public interface Area extends Environmental, Economics
{
	public final static int THEME_FANTASY=1;
	public final static int THEME_TECHNOLOGY=2;
	public final static int THEME_HEROIC=4;
	public final static int THEME_SKILLONLYMASK=8;
	public final static String[] THEME_DESCS={"Unknown",             // 0
	    									  "Fantasy",             // 1
											  "Technical",           // 2
											  "Fantasy & Technical", // 3
											  "Heroic",              // 4
											  "Heroic & Fantasy",    // 5
											  "Heroic & Technical",  // 6
											  "All Allowed"          // 7
	};
	public final static String[] THEME_DESCS_EXT={"Unavailable",         // 0
												  "Fantasy",             // 1
												  "Technical",           // 2
												  "Fantasy & Technical", // 3
												  "Heroic",              // 4
												  "Heroic & Fantasy",    // 5
												  "Heroic & Technical",  // 6
												  "All Allowed",         // 7
		    									  "Unavail. Skill only", // 8
		    									  "Fantasy Skills Only", // 9
												  "Tech Skill Only",     // 10
												  "Fant&Tech Skill Only",// 11
												  "Powers only",         // 12
												  "Powers & Spells only",// 13
												  "Hero&Tech Skill only",// 14
												  "Any skill only"       // 15
											   
	};	
	public final static int CLIMASK_NORMAL=0;
	public final static int CLIMASK_WET=1;
	public final static int CLIMASK_COLD=2;
	public final static int CLIMATE_WINDY=4;
	public final static int CLIMASK_HOT=8;
	public final static int CLIMASK_DRY=16;
	public final static String[] CLIMATE_DESCS={"NORMAL","WET","COLD","WINDY","HOT","DRY"};
	public final static int NUM_CLIMATES=6;
	public final static int ALL_CLIMATE_MASK=31;
	
	public final static int FLAG_THIN=1;
	
	public long flags();
	public int getTechLevel();
	public void setTechLevel(int level);
	public String getArchivePath();
	public void setArchivePath(String pathFile);
	public Climate getClimateObj();
	public void setClimateObj(Climate obj);
	public TimeClock getTimeObj();
	public void setTimeObj(TimeClock obj);
	public int climateType();
	public void setClimateType(int newClimateType);
	public void setAuthorID(String authorID);
	public String getAuthorID();
	public String getCurrency();
	public void setCurrency(String currency);
    public int numBlurbFlags();
    public int numAllBlurbFlags();
    public String getBlurbFlag(String flag);
    public String getBlurbFlag(int which);
    public void addBlurbFlag(String flagPlusDesc);
    public void delBlurbFlag(String flagOnly);

	public void fillInAreaRooms();
	public void fillInAreaRoom(Room R);
    public void addProperRoom(Room R);
    public void delProperRoom(Room R);
    public Room getRoom(String roomID);
    public boolean isRoom(Room R);
	public Room getRandomProperRoom();
	public Enumeration getProperMap();
    public void addProperRoomnumber(String roomID);
    public void delProperRoomnumber(String roomID);
	public Enumeration getCompleteMap();
	public RoomnumberSet getProperRoomnumbers();
	public void setProperRoomnumbers(RoomnumberSet set);
	public RoomnumberSet getCachedRoomnumbers();
	public int numberOfProperIDedRooms();
	public int properSize();
    
	public Enumeration getMetroMap();
    public void addMetroRoom(Room R);
    public void delMetroRoom(Room R);
    public void addMetroRoomnumber(String roomID);
    public void delMetroRoomnumber(String roomID);
	public int metroSize();
	public boolean inMyMetroArea(Area A);
	public Room getRandomMetroRoom();
	public Vector getMetroCollection();
	
	public String getNewRoomID(Room startRoom, int direction);
	
	public void toggleMobility(boolean onoff);
	public boolean getMobility();
	public void tickControl(boolean start);
	
	public void addSubOp(String username);
	public void delSubOp(String username);
	public boolean amISubOp(String username);
	public String getSubOpList();
	public void setSubOpList(String list);
	public Vector getSubOpVectorList();
	
	public StringBuffer getAreaStats();
	public int[] getAreaIStats();
	public final static int AREASTAT_POPULATION=0;
	public final static int AREASTAT_MINLEVEL=1;
	public final static int AREASTAT_MAXLEVEL=2;
	public final static int AREASTAT_AVGLEVEL=3;
	public final static int AREASTAT_MEDLEVEL=4;
	public final static int AREASTAT_AVGALIGN=5;
	public final static int AREASTAT_MEDALIGN=6;
	public final static int AREASTAT_TOTLEVEL=7;
	public final static int AREASTAT_INTLEVEL=8;
    public final static int AREASTAT_VISITABLEROOMS=9;
	public final static int AREASTAT_NUMBER=10;
	
    // Partition Necessary
    public void addChildToLoad(String str);
    public void addParentToLoad(String str);
    public Enumeration getChildren();
    public String getChildrenList();
    public int getNumChildren();
    public Area getChild(int num);
    public Area getChild(String named);
    public boolean isChild(Area named);
    public boolean isChild(String named);
    public void addChild(Area Adopted);
    public void removeChild(Area Disowned);
    public void removeChild(int Disowned);
    public boolean canChild(Area newChild);
    public Enumeration getParents();
    public String getParentsList();
    public int getNumParents();
    public Area getParent(int num);
    public Area getParent(String named);
    public Vector getParentsRecurse();
    public boolean isParent(Area named);
    public boolean isParent(String named);
    public void addParent(Area Adopted);
    public void removeParent(Area Disowned);
    public void removeParent(int Disowned);
    public boolean canParent(Area newParent);
    
    public class CompleteRoomEnumerator implements Enumeration
    {
    	Enumeration roomEnumerator=null;
    	Area area=null;
    	public CompleteRoomEnumerator(Area myArea){
    		area=myArea;
    		roomEnumerator=area.getProperRoomnumbers().getRoomIDs();
    	}
    	public boolean hasMoreElements(){return roomEnumerator.hasMoreElements();}
    	public Object nextElement()
    	{
    		String roomID=(String)roomEnumerator.nextElement();
    		if(roomID==null) return null;
			Room R=area.getRoom(roomID);
			if(R==null) return nextElement();
			if(R.expirationDate()!=0)
				R.setExpirationDate(R.expirationDate()+(1000*60*10));
			return CMLib.map().getRoom(R);
    	}
    }
}
