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
		switch(mob.location().getArea().getClimateObj().weatherType(mob.location()))
		{
		case Climate.WEATHER_WINDY:
		case Climate.WEATHER_THUNDERSTORM:
		case Climate.WEATHER_BLIZZARD:
		case Climate.WEATHER_DUSTSTORM:
		case Climate.WEATHER_HAIL:
		case Climate.WEATHER_SLEET:
		case Climate.WEATHER_SNOW:
		case Climate.WEATHER_RAIN:
			break;
		default:
			mob.tell("The weather just doesn't get much calmer than this.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int size=mob.location().getArea().numberOfIDedRooms();
		size=size/mob.envStats().level();
		if(size<0) size=0;
		boolean success=profficiencyCheck(mob,-size,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"^SThe swirling sky changes color!^?":"^S<S-NAME> chant(s) into the swirling sky!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				switch(mob.location().getArea().getClimateObj().weatherType(mob.location()))
				{
				case Climate.WEATHER_WINDY:
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_CLEAR);
					break;
				case Climate.WEATHER_THUNDERSTORM:
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_RAIN);
					break;
				case Climate.WEATHER_BLIZZARD:
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_SNOW);
					break;
				case Climate.WEATHER_DUSTSTORM:
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_CLEAR);
					break;
				case Climate.WEATHER_HAIL:
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_CLOUDY);
					break;
				case Climate.WEATHER_SLEET:
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_CLOUDY);
					break;
				case Climate.WEATHER_SNOW:
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_CLOUDY);
					break;
				case Climate.WEATHER_RAIN:
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_CLOUDY);
					break;
				default:
					break;
				}
				mob.location().getArea().getClimateObj().forceWeatherTick(mob.location().getArea());
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> chant(s) into the sky, but the magic fizzles.");

		return success;
	}
}