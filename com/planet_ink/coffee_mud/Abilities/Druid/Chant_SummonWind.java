package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Chant_SummonWind extends Chant
{
	public Chant_SummonWind()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Summon Wind";
		displayText="(Summon Wind)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;

		canAffectCode=0;
		canTargetCode=0;
		
		baseEnvStats().setLevel(7);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Chant_SummonWind();
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((mob.location().domainType()&Room.INDOORS)>0)
		{
			mob.tell("You must be outdoors for this chant to work.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int size=mob.location().getArea().getMyMap().size();
		size=size-(mob.envStats().level()*20);
		if(size<0) size=0;
		boolean success=profficiencyCheck(-size,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType,auto?"The sky changes color!":"<S-NAME> chant(s) into the sky for wind!");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				switch(mob.location().getArea().weatherType(mob.location()))
				{
				case Area.WEATHER_BLIZZARD:
					mob.location().getArea().setNextWeatherType(Area.WEATHER_BLIZZARD);
					break;
				case Area.WEATHER_CLEAR:
					mob.location().getArea().setNextWeatherType(Area.WEATHER_WINDY);
					break;
				case Area.WEATHER_CLOUDY:
					mob.location().getArea().setNextWeatherType(Area.WEATHER_WINDY);
					break;
				case Area.WEATHER_DROUGHT:
					mob.location().getArea().setNextWeatherType(Area.WEATHER_DUSTSTORM);
					break;
				case Area.WEATHER_DUSTSTORM:
					mob.location().getArea().setNextWeatherType(Area.WEATHER_DUSTSTORM);
					break;
				case Area.WEATHER_HAIL:
					mob.location().getArea().setNextWeatherType(Area.WEATHER_HAIL);
					break;
				case Area.WEATHER_HEAT_WAVE:
					mob.location().getArea().setNextWeatherType(Area.WEATHER_DUSTSTORM);
					break;
				case Area.WEATHER_RAIN:
					mob.location().getArea().setNextWeatherType(Area.WEATHER_THUNDERSTORM);
					break;
				case Area.WEATHER_SLEET:
					mob.location().getArea().setNextWeatherType(Area.WEATHER_SLEET);
					break;
				case Area.WEATHER_SNOW:
					mob.location().getArea().setNextWeatherType(Area.WEATHER_BLIZZARD);
					break;
				case Area.WEATHER_THUNDERSTORM:
					mob.location().getArea().setNextWeatherType(Area.WEATHER_THUNDERSTORM);
					break;
				case Area.WEATHER_WINDY:
					mob.location().getArea().setNextWeatherType(Area.WEATHER_WINDY);
					break;
				case Area.WEATHER_WINTER_COLD:
					mob.location().getArea().setNextWeatherType(Area.WEATHER_WINDY);
					break;
				default:
					break;
				}
				mob.location().getArea().forceWeatherTick();
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> chant(s) into the sky for wind, but the magic fizzles.");

		return success;
	}
}