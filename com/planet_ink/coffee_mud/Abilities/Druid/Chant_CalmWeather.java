package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Chant_CalmWeather extends Chant
{
	public String ID() { return "Chant_CalmWeather"; }
	public String name(){ return "Calm Weather";}
	public int quality(){return Ability.INDIFFERENT;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Chant_CalmWeather();}
	public long flags(){return Ability.FLAG_WEATHERAFFECTING;}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(((mob.location().domainType()&Room.INDOORS)>0)&&(!auto))
		{
			mob.tell("You must be outdoors for this chant to work.");
			return false;
		}
		switch(mob.location().getArea().weatherType(mob.location()))
		{
		case Area.WEATHER_WINDY:
		case Area.WEATHER_THUNDERSTORM:
		case Area.WEATHER_BLIZZARD:
		case Area.WEATHER_DUSTSTORM:
		case Area.WEATHER_HAIL:
		case Area.WEATHER_SLEET:
		case Area.WEATHER_SNOW:
		case Area.WEATHER_RAIN:
			break;
		default:
			mob.tell("The weather just doesn't get much calmer than this.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int size=mob.location().getArea().mapSize();
		size=size/mob.envStats().level();
		if(size<0) size=0;
		boolean success=profficiencyCheck(-size,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"^SThe swirling sky changes color!^?":"^S<S-NAME> chant(s) into the swirling sky!^?");
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				switch(mob.location().getArea().weatherType(mob.location()))
				{
				case Area.WEATHER_WINDY:
					mob.location().getArea().setNextWeatherType(Area.WEATHER_CLEAR);
					break;
				case Area.WEATHER_THUNDERSTORM:
					mob.location().getArea().setNextWeatherType(Area.WEATHER_RAIN);
					break;
				case Area.WEATHER_BLIZZARD:
					mob.location().getArea().setNextWeatherType(Area.WEATHER_SNOW);
					break;
				case Area.WEATHER_DUSTSTORM:
					mob.location().getArea().setNextWeatherType(Area.WEATHER_CLEAR);
					break;
				case Area.WEATHER_HAIL:
					mob.location().getArea().setNextWeatherType(Area.WEATHER_CLOUDY);
					break;
				case Area.WEATHER_SLEET:
					mob.location().getArea().setNextWeatherType(Area.WEATHER_CLOUDY);
					break;
				case Area.WEATHER_SNOW:
					mob.location().getArea().setNextWeatherType(Area.WEATHER_CLOUDY);
					break;
				case Area.WEATHER_RAIN:
					mob.location().getArea().setNextWeatherType(Area.WEATHER_CLOUDY);
					break;
				default:
					break;
				}
				mob.location().getArea().forceWeatherTick();
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> chant(s) into the sky, but the magic fizzles.");

		return success;
	}
}