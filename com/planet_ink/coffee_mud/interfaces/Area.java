package com.planet_ink.coffee_mud.interfaces;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public interface Area extends Environmental
{
	public final static int TECH_LOW=0;
	public final static int TECH_MIXED=1;
	public final static int TECH_HIGH=2;
	public final static String[] TECH_DESCS={"Low Tech","Mixed Tech","High Tech"};
	
	public final static int CLIMASK_NORMAL=0;
	public final static int CLIMASK_WET=1;
	public final static int CLIMASK_COLD=2;
	public final static int CLIMATE_WINDY=4;
	public final static int CLIMASK_HOT=8;
	public final static int CLIMASK_DRY=16;
	public final static String[] CLIMATE_DESCS={"NORMAL","WET","COLD","WINDY","HOT","DRY"};
	public final static int NUM_CLIMATES=6;
	public final static int ALL_CLIMATE_MASK=31;
	
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

	public void fillInAreaRooms();
	public void fillInAreaRoom(Room R);
	public Enumeration getMetroMap();
	public Enumeration getProperMap();
	public int metroSize();
	public int properSize();
	public boolean inMetroArea(Area A);
	public int numberOfProperIDedRooms();
	public Room getRandomMetroRoom();
	public Room getRandomProperRoom();
	public void clearMaps();
	
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
	public final static int AREASTAT_NUMBER=9;
	
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
    public boolean isParent(Area named);
    public boolean isParent(String named);
    public void addParent(Area Adopted);
    public void removeParent(Area Disowned);
    public void removeParent(int Disowned);
    public boolean canParent(Area newParent);
}
