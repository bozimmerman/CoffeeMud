package com.planet_ink.coffee_mud.interfaces;
import java.util.*;

public interface Area extends Environmental
{
	public final static int WEATHER_CLEAR=0;
	public final static int WEATHER_CLOUDY=1;
	public final static int WEATHER_WINDY=2;
	public final static int WEATHER_RAIN=3;
	public final static int WEATHER_THUNDERSTORM=4;
	public final static int WEATHER_SNOW=5;
	public final static int WEATHER_HAIL=6;
	public final static int WEATHER_HEAT_WAVE=7;
	public final static int WEATHER_SLEET=8;
	public final static int WEATHER_BLIZZARD=9;
	public final static int WEATHER_DUSTSTORM=10;
	public final static int WEATHER_DROUGHT=11;
	public final static int WEATHER_WINTER_COLD=12;
	public final static int NUM_WEATHER=13;
	public final static int BOTHER_WEATHER_TICKS=86; // 360 secs
	
	public final static int TECH_LOW=0;
	public final static int TECH_MIXED=1;
	public final static int TECH_HIGH=2;
	public final static String[] TECH_DESCS={"Low Tech","Mixed Tech","High Tech"};
	
	public final static String[] WEATHER_DESCS=
	{ "CLEAR","CLOUDY","WINDY","RAIN","THUNDERSTORM","SNOW","HAIL","HEAT","SLEET","BLIZZARD","DUST","DROUGHT","COLD"};
	
	public final static String[] MOON_PHASES={
		"There is a new moon in the sky.",
		"The moon is in the waxing crescent phase.",
		"The moon is in its first quarter.",
		"The moon is in the waxing gibbous phase (almost full).",
		"There is a full moon in the sky.",
		"The moon is in the waning gibbous phase (no longer full).",
		"The moon is in its last quarter.",
		"The moon is in the waning crescent phase.",
		"There is a BLUE MOON! Oh my GOD! Run away!!!!!"
	};
	public final static int PHASE_NEW=0;
	public final static int PHASE_WAXCRESCENT=1;
	public final static int PHASE_WAXQUARTER=2;
	public final static int PHASE_WAXGIBBOUS=3;
	public final static int PHASE_FULL=4;
	public final static int PHASE_WANEGIBBOUS=5;
	public final static int PHASE_WANDEQUARTER=6;
	public final static int PHASE_WANECRESCENT=7;
	public final static int PHASE_BLUE=8;
	
	public final static String[] TOD_DESC={
		"It is dawn ","It is daytime ","It is dusk ","It is nighttime "
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
	
	public final static int A_FULL_DAY=16; // groups of 10 minutes, so 2 hours, 40 minutes=24 hours.
	public final static int DAYS_IN_MONTH=30; // number of days in a month
	public final static int MONTHS_IN_YEAR=12; // number of months in a full year
									   
	public final static int TIME_DAWN=0;
	public final static int TIME_DAY=1;
	public final static int TIME_DUSK=2;
	public final static int TIME_NIGHT=3;
	
	public final static int SEASON_SPRING=0;
	public final static int SEASON_SUMMER=1;
	public final static int SEASON_FALL=2;
	public final static int SEASON_WINTER=3;
	public final static String[] SEASON_DESCS={"SPRING","SUMMER","FALL","WINTER"};
	
	public int weatherType(Room room);
	public int nextWeatherType(Room room);
	public String weatherDescription(Room room);
	public String nextWeatherDescription(Room room);
	public int climateType();
	public int getSeasonCode();
	public void setNextWeatherType(int weatherCode);
	public void setCurrentWeatherType(int weatherCode);

	public int getTechLevel();
	public void setTechLevel(int level);
	public String getArchivePath();
	public void setArchivePath(String pathFile);

	public int adjustWaterConsumption(int base, MOB mob, Room room);
	public int adjustMovement(int base, MOB mob, Room room);
	
	public void setClimateType(int newClimateType);
	public String getWeatherDescription();
	public String getNextWeatherDescription();
	
	public String timeDescription(MOB mob, Room room);
	public int getYear();
	public void setYear(int y);
	
	public int getMonth();
	public void setMonth(int m);
	public int getMoonPhase();
	public boolean canSeeTheMoon(Room room);
	public boolean canSeeTheSun(Room room);
	
	public int getDayOfMonth();
	public void setDayOfMonth(int d);
	public int getTimeOfDay();
	public boolean setTimeOfDay(int t);
	public int getTODCode();
	
	public void forceWeatherTick();
	public void tickControl(boolean start);
	public void tickTock(int howManyHours);
	
	public void fillInAreaRooms();
	public void fillInAreaRoom(Room R);
	public Enumeration getMap();
	public int mapSize();
	public int numberOfIDedRooms();
	public Room getRandomRoom();
	public void clearMap();
	
	public void toggleMobility(boolean onoff);
	public boolean getMobility();
	
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
