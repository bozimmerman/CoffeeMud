package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Chant_CalmWind extends Chant
{
	public Chant_CalmWind()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Calm Wind";
		displayText="(Calm Wind)";
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
		return new Chant_CalmWind();
	}
	
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
			break;
		case Area.WEATHER_HAIL:
		case Area.WEATHER_SLEET:
		case Area.WEATHER_SNOW:
		case Area.WEATHER_RAIN:
			mob.tell("The weather is nasty, but not especially windy any more.");
			return false;
		default:
			mob.tell("If doesn't seem especially windy right now.");
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
			FullMsg msg=new FullMsg(mob,null,this,affectType,auto?"^SThe swirling sky changes color!^?":"^S<S-NAME> chant(s) into the swirling sky!^?");
			if(mob.location().okAffect(msg))
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