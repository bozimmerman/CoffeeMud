package com.planet_ink.coffee_mud.interfaces;
import java.util.Vector;

public interface Area extends Environmental
{
	public int weatherType(Room room);
	public int nextWeatherType(Room room);
	public String weatherDescription(Room room);
	public String nextWeatherDescription(Room room);
	public int climateType();
	
	public void setClimateType(int newClimateType);
	public String getWeatherDescription();
	public String getNextWeatherDescription();
	
	public int getMonth();
	public void setMonth(int m);
	public int getMoonPhase();
	
	public int getDayOfMonth();
	public void setDayOfMonth(int d);
	public int getTimeOfDay();
	public void setTimeOfDay(int t);
	
	public void tickControl(boolean start);
	public StringBuffer getAreaStats();
	public Vector getMyMap();
	
	public void addSubOp(String username);
	public void delSubOp(String username);
	public boolean amISubOp(String username);
	public String getSubOpList();
	public void setSubOpList(String list);
	
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
	
	public final static int CLIMASK_NORMAL=0;
	public final static int CLIMASK_WET=1;
	public final static int CLIMASK_COLD=2;
	public final static int CLIMATE_WINDY=4;
	public final static int CLIMASK_HOT=8;
	public final static int CLIMASK_DRY=16;
}
