package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Prayer_Weather extends Prayer
{
	public String ID() { return "Prayer_Weather"; }
	public String name(){ return "Weather";}
	protected int canAffectCode(){return 0;}
	public int holyQuality(){ return HOLY_NEUTRAL;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Prayer_Weather();}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int size=mob.location().getArea().mapSize();
		size=size-(mob.envStats().level()*20);
		if(size<0) size=0;
		boolean success=profficiencyCheck(-size,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"The sky changes color!":"^S<S-NAME> "+prayWord(mob)+" for a change in weather!^?");
			if(mob.location().okAffect(msg))
			{
				int switcher=Dice.roll(1,3,0);
				mob.location().send(mob,msg);
				switch(mob.location().getArea().weatherType(mob.location()))
				{
				case Area.WEATHER_BLIZZARD:
					if(switcher==1)
						mob.location().getArea().setNextWeatherType(Area.WEATHER_BLIZZARD);
					else if(switcher==2)
						mob.location().getArea().setNextWeatherType(Area.WEATHER_BLIZZARD);
					else
					mob.location().getArea().setNextWeatherType(Area.WEATHER_SNOW);
					break;
				case Area.WEATHER_CLEAR:
					if(switcher==1)
						mob.location().getArea().setNextWeatherType(Area.WEATHER_WINDY);
					else if(switcher==2)
						mob.location().getArea().setNextWeatherType(Area.WEATHER_RAIN);
					else
					mob.location().getArea().setNextWeatherType(Area.WEATHER_CLOUDY);
					break;
				case Area.WEATHER_CLOUDY:
					if(switcher==1)
						mob.location().getArea().setNextWeatherType(Area.WEATHER_WINDY);
					else if(switcher==2)
						mob.location().getArea().setNextWeatherType(Area.WEATHER_RAIN);
					else
					mob.location().getArea().setNextWeatherType(Area.WEATHER_CLEAR);
					break;
				case Area.WEATHER_DROUGHT:
					if(switcher==1)
						mob.location().getArea().setNextWeatherType(Area.WEATHER_DUSTSTORM);
					else if(switcher==2)
						mob.location().getArea().setNextWeatherType(Area.WEATHER_HEAT_WAVE);
					else
					mob.location().getArea().setNextWeatherType(Area.WEATHER_CLEAR);
					break;
				case Area.WEATHER_DUSTSTORM:
					if(switcher==1)
						mob.location().getArea().setNextWeatherType(Area.WEATHER_DUSTSTORM);
					else if(switcher==2)
						mob.location().getArea().setNextWeatherType(Area.WEATHER_CLOUDY);
					else
					mob.location().getArea().setNextWeatherType(Area.WEATHER_CLEAR);
					break;
				case Area.WEATHER_HAIL:
					if(switcher==1)
						mob.location().getArea().setNextWeatherType(Area.WEATHER_HAIL);
					else if(switcher==2)
						mob.location().getArea().setNextWeatherType(Area.WEATHER_SLEET);
					else
					mob.location().getArea().setNextWeatherType(Area.WEATHER_CLOUDY);
					break;
				case Area.WEATHER_HEAT_WAVE:
					if(switcher==1)
						mob.location().getArea().setNextWeatherType(Area.WEATHER_DUSTSTORM);
					else if(switcher==2)
						mob.location().getArea().setNextWeatherType(Area.WEATHER_RAIN);
					else
					mob.location().getArea().setNextWeatherType(Area.WEATHER_CLEAR);
					break;
				case Area.WEATHER_RAIN:
					if(switcher==1)
						mob.location().getArea().setNextWeatherType(Area.WEATHER_THUNDERSTORM);
					else if(switcher==2)
						mob.location().getArea().setNextWeatherType(Area.WEATHER_THUNDERSTORM);
					else
					mob.location().getArea().setNextWeatherType(Area.WEATHER_CLOUDY);
					break;
				case Area.WEATHER_SLEET:
					if(switcher==1)
						mob.location().getArea().setNextWeatherType(Area.WEATHER_SLEET);
					else if(switcher==2)
						mob.location().getArea().setNextWeatherType(Area.WEATHER_SLEET);
					else
					mob.location().getArea().setNextWeatherType(Area.WEATHER_CLOUDY);
					break;
				case Area.WEATHER_SNOW:
					if(switcher==1)
						mob.location().getArea().setNextWeatherType(Area.WEATHER_BLIZZARD);
					else if(switcher==2)
						mob.location().getArea().setNextWeatherType(Area.WEATHER_SLEET);
					else
					mob.location().getArea().setNextWeatherType(Area.WEATHER_CLOUDY);
					break;
				case Area.WEATHER_THUNDERSTORM:
					if(switcher==1)
						mob.location().getArea().setNextWeatherType(Area.WEATHER_THUNDERSTORM);
					else if(switcher==2)
						mob.location().getArea().setNextWeatherType(Area.WEATHER_THUNDERSTORM);
					else
					mob.location().getArea().setNextWeatherType(Area.WEATHER_RAIN);
					break;
				case Area.WEATHER_WINDY:
					if(switcher==1)
						mob.location().getArea().setNextWeatherType(Area.WEATHER_WINDY);
					else if(switcher==2)
						mob.location().getArea().setNextWeatherType(Area.WEATHER_THUNDERSTORM);
					else
					mob.location().getArea().setNextWeatherType(Area.WEATHER_CLEAR);
					break;
				case Area.WEATHER_WINTER_COLD:
					if(switcher==1)
						mob.location().getArea().setNextWeatherType(Area.WEATHER_WINDY);
					else if(switcher==2)
						mob.location().getArea().setNextWeatherType(Area.WEATHER_SNOW);
					else
					mob.location().getArea().setNextWeatherType(Area.WEATHER_CLEAR);
					break;
				default:
					break;
				}
				mob.location().getArea().forceWeatherTick();
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> "+prayWord(mob)+", but nothing happens.");

		return success;
	}
}