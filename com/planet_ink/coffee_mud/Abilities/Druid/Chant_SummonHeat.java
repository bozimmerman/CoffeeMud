package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Chant_SummonHeat extends Chant
{
	public String ID() { return "Chant_SummonHeat"; }
	public String name(){ return "Summon Heat";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	public Environmental newInstance(){	return new Chant_SummonHeat();}
	public long flags(){return Ability.FLAG_WEATHERAFFECTING;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(((mob.location().domainType()&Room.INDOORS)>0)&&(!auto))
		{
			mob.tell("You must be outdoors for this chant to work.");
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
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"The sky changes color!":"^S<S-NAME> chant(s) into the sky for heat!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				switch(mob.location().getArea().getClimateObj().weatherType(mob.location()))
				{
				case Climate.WEATHER_BLIZZARD:
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_THUNDERSTORM);
					break;
				case Climate.WEATHER_CLEAR:
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_HEAT_WAVE);
					break;
				case Climate.WEATHER_CLOUDY:
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_CLEAR);
					break;
				case Climate.WEATHER_DROUGHT:
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_DUSTSTORM);
					break;
				case Climate.WEATHER_DUSTSTORM:
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_DROUGHT);
					break;
				case Climate.WEATHER_HAIL:
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_RAIN);
					break;
				case Climate.WEATHER_HEAT_WAVE:
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_DROUGHT);
					break;
				case Climate.WEATHER_RAIN:
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_CLEAR);
					break;
				case Climate.WEATHER_SLEET:
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_RAIN);
					break;
				case Climate.WEATHER_SNOW:
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_RAIN);
					break;
				case Climate.WEATHER_THUNDERSTORM:
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_RAIN);
					break;
				case Climate.WEATHER_WINDY:
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_DUSTSTORM);
					break;
				case Climate.WEATHER_WINTER_COLD:
					mob.location().getArea().getClimateObj().setNextWeatherType(Climate.WEATHER_CLEAR);
					break;
				default:
					break;
				}
				mob.location().getArea().getClimateObj().forceWeatherTick(mob.location().getArea());
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> chant(s) into the sky for heat, but the magic fizzles.");

		return success;
	}
}