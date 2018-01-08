package com.planet_ink.coffee_mud.Common.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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

/*
   Copyright 2004-2018 Bo Zimmerman

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
 * A climate represents the entire weather pattern of an area
 * It does most of the mundane things related to weather
 * changes based on season, area, and other factors.
 */
public interface Climate extends Tickable, CMCommon
{
	/**
	 * Returns the specific weather situation for
	 * the given room.  Returns one of the Climate.WEATHER_*
	 * constants.  It always returns WEATHER_CLEAR for
	 * indoor locales.
	 * @see Climate
	 * @see Climate#setCurrentWeatherType(int)
	 * @param room the room to evaluate.
	 * @return the Climate.WEATHER_* constant
	 */
	public int weatherType(Room room);
	/**
	 * Returns the upcoming specific weather situation for
	 * the given room.  Returns one of the Climate.WEATHER_*
	 * constants.  It always returns WEATHER_CLEAR for
	 * indoor locales.  Climate objects always calculate
	 * weather 1 step ahead for predictive purposes.
	 * @see Climate
	 * @see Climate#setNextWeatherType(int)
	 * @param room the room to evaluate.
	 * @return the Climate.WEATHER_* constant
	 */
	public int nextWeatherType(Room room);
	/**
	 * Sets the upcoming specific weather situation for
	 * this area.  Takes one of the Climate.WEATHER_*
	 * constants. Climate objects always calculate
	 * weather 1 step ahead for predictive purposes.
	 * @see Climate
	 * @see Climate#nextWeatherType(Room)
	 * @param weatherCode the Climate.WEATHER_* constant
	 */
	public void setNextWeatherType(int weatherCode);
	/**
	 * Sets the current specific weather situation for
	 * this area.  Takes one of the Climate.WEATHER_*
	 * constants. Climate objects always calculate
	 * weather 1 step ahead for predictive purposes.
	 * @see Climate
	 * @see Climate#weatherType(Room)
	 * @param weatherCode the Climate.WEATHER_* constant
	 */
	public void setCurrentWeatherType(int weatherCode);
	/**
	 * Returns a readable description of the weather
	 * for the given room.
	 * @param room the room to evaluate
	 * @return a readable string.
	 */
	public String weatherDescription(Room room);
	/**
	 * Returns a readable description of the weather
	 * that will be upcoming for the given room.
	 * @param room the room to evaluate
	 * @return a readable string.
	 */
	public String nextWeatherDescription(Room room);
	/**
	 * Returns whether a player can see the stars from
	 * the current room.
	 * @param room the room vantage point
	 * @return whether the stars can be seen.
	 */
	public boolean canSeeTheStars(Room room);
	/**
	 * Returns whether a player can see the moon from
	 * the current room.
	 * @param room the room vantage point
	 * @param butNotA an ability to exempt from moon flags
	 * @return whether the moon can be seen.
	 */
	public boolean canSeeTheMoon(Room room, Ability butNotA);
	/**
	 * Returns whether a player can see the sun from
	 * the current room.
	 * @param room the room vantage point
	 * @return whether the sun can be seen.
	 */
	public boolean canSeeTheSun(Room room);
	/**
	 * Returns a readable string describing the
	 * weather conditions in the given area, assuming
	 * this climate is the correct one.
	 * @param A the area to evaluate
	 * @return a description of the weather.
	 */
	public String getWeatherDescription(Area A);
	/**
	 * Returns a readable string describing the upcoming
	 * weather conditions in the given area, assuming
	 * this climate is the correct one.
	 * @param A the area to evaluate
	 * @return a description of the weather coming up.
	 */
	public String getNextWeatherDescription(Area A);
	/**
	 * This method forces the weather to change to what
	 * is designated by the nextWeatherType().  The Area
	 * is required to calculate a new nextWeatherType.
	 * @param A the area to calculate next weather for.
	 */
	public void forceWeatherTick(Area A);
	/**
	 * If applicable, thies method will return the base water
	 * thirstiness of the given mob, modified up or down based
	 * on the room given.
	 * @param base the water thirst gain to start with
	 * @param room the room the mob is in
	 * @return the modified base thirst, after gain or loss
	 */
	public int adjustWaterConsumption(int base, Room room);
	/**
	 * If applicable, thies method will return the base movement
	 * cost of the given mob, modified up or down based
	 * on the room given.
	 * @param base the movement  loss to start with
	 * @param room the room the mob is moving through
	 * @return the modified movement, after gain or loss
	 */
	public int adjustMovement(int base, Room room);
	/** constant describing clear or no weather. @see Climate#weatherType(Room) */
	public final static int WEATHER_CLEAR=0;
	/** constant describing  cloudy weather. @see Climate#weatherType(Room) */
	public final static int WEATHER_CLOUDY=1;
	/** constant describing windy weather. @see Climate#weatherType(Room) */
	public final static int WEATHER_WINDY=2;
	/** constant describing rainy weather. @see Climate#weatherType(Room) */
	public final static int WEATHER_RAIN=3;
	/** constant describing thunderstorm weather. @see Climate#weatherType(Room) */
	public final static int WEATHER_THUNDERSTORM=4;
	/** constant describing snowy weather. @see Climate#weatherType(Room) */
	public final static int WEATHER_SNOW=5;
	/** constant describing haily weather. @see Climate#weatherType(Room) */
	public final static int WEATHER_HAIL=6;
	/** constant describing heat wave weather. @see Climate#weatherType(Room) */
	public final static int WEATHER_HEAT_WAVE=7;
	/** constant describing sleety weather. @see Climate#weatherType(Room) */
	public final static int WEATHER_SLEET=8;
	/** constant describing blizzardous weather. @see Climate#weatherType(Room) */
	public final static int WEATHER_BLIZZARD=9;
	/** constant describing duststorming weather. @see Climate#weatherType(Room) */
	public final static int WEATHER_DUSTSTORM=10;
	/** constant describing drought condition weather. @see Climate#weatherType(Room) */
	public final static int WEATHER_DROUGHT=11;
	/** constant describing harsh cold weather. @see Climate#weatherType(Room) */
	public final static int WEATHER_WINTER_COLD=12;
	/** constant describing the number of weather types. @see Climate#weatherType(Room) */
	public final static int NUM_WEATHER=13;
	/** constant defining how often weather changes in the climates */
	public static final int WEATHER_TICK_DOWN=150; // 150 = 10 minutes * 60 seconds / 4
	/**  descriptive strings for the Climate.WEATHER_* constants, ordered by their value. @see Climate */
	public final static String[] WEATHER_DESCS=
	{ 
		"CLEAR","CLOUDY","WINDY","RAIN","THUNDERSTORM","SNOW","HAIL","HEAT","SLEET","BLIZZARD","DUST","DROUGHT","COLD"
	};

}
