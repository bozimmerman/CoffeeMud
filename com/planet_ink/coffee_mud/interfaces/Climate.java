package com.planet_ink.coffee_mud.interfaces;

public interface Climate extends Tickable
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
	
	public final static String[] WEATHER_DESCS=
	{ "CLEAR","CLOUDY","WINDY","RAIN","THUNDERSTORM","SNOW","HAIL","HEAT","SLEET","BLIZZARD","DUST","DROUGHT","COLD"};
	
	public int weatherType(Room room);
	public int nextWeatherType(Room room);
	public String weatherDescription(Room room);
	public String nextWeatherDescription(Room room);
	public void setNextWeatherType(int weatherCode);
	public void setCurrentWeatherType(int weatherCode);
	public boolean canSeeTheMoon(Room room);
	public boolean canSeeTheSun(Room room);

	public String getWeatherDescription(Area A);
	public String getNextWeatherDescription(Area A);
	
	public void forceWeatherTick(Area A);
	public int adjustWaterConsumption(int base, MOB mob, Room room);
	public int adjustMovement(int base, MOB mob, Room room);
}
