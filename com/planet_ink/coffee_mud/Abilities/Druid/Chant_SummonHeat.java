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
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(((mob.location().domainType()&Room.INDOORS)>0)&&(!auto))
		{
			mob.tell("You must be outdoors for this chant to work.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int size=mob.location().getArea().mapSize();
		size=size-(mob.envStats().level()*20);
		if(size<0) size=0;
		boolean success=profficiencyCheck(-size,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"The sky changes color!":"^S<S-NAME> chant(s) into the sky for heat!^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				switch(mob.location().getArea().weatherType(mob.location()))
				{
				case Area.WEATHER_BLIZZARD:
					mob.location().getArea().setNextWeatherType(Area.WEATHER_THUNDERSTORM);
					break;
				case Area.WEATHER_CLEAR:
					mob.location().getArea().setNextWeatherType(Area.WEATHER_HEAT_WAVE);
					break;
				case Area.WEATHER_CLOUDY:
					mob.location().getArea().setNextWeatherType(Area.WEATHER_CLEAR);
					break;
				case Area.WEATHER_DROUGHT:
					mob.location().getArea().setNextWeatherType(Area.WEATHER_DUSTSTORM);
					break;
				case Area.WEATHER_DUSTSTORM:
					mob.location().getArea().setNextWeatherType(Area.WEATHER_DROUGHT);
					break;
				case Area.WEATHER_HAIL:
					mob.location().getArea().setNextWeatherType(Area.WEATHER_RAIN);
					break;
				case Area.WEATHER_HEAT_WAVE:
					mob.location().getArea().setNextWeatherType(Area.WEATHER_DROUGHT);
					break;
				case Area.WEATHER_RAIN:
					mob.location().getArea().setNextWeatherType(Area.WEATHER_CLEAR);
					break;
				case Area.WEATHER_SLEET:
					mob.location().getArea().setNextWeatherType(Area.WEATHER_RAIN);
					break;
				case Area.WEATHER_SNOW:
					mob.location().getArea().setNextWeatherType(Area.WEATHER_RAIN);
					break;
				case Area.WEATHER_THUNDERSTORM:
					mob.location().getArea().setNextWeatherType(Area.WEATHER_RAIN);
					break;
				case Area.WEATHER_WINDY:
					mob.location().getArea().setNextWeatherType(Area.WEATHER_DUSTSTORM);
					break;
				case Area.WEATHER_WINTER_COLD:
					mob.location().getArea().setNextWeatherType(Area.WEATHER_CLEAR);
					break;
				default:
					break;
				}
				mob.location().getArea().forceWeatherTick();
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> chant(s) into the sky for heat, but the magic fizzles.");

		return success;
	}
}