package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class PuddleMaker extends StdBehavior
{
	public String ID(){return "PuddleMaker";}
	protected int canImproveCode(){return Behavior.CAN_ROOMS|Behavior.CAN_AREAS;}
	public Behavior newInstance()
	{
		return new PuddleMaker();
	}

	public boolean coldWetWeather(int weather)
	{
		switch(weather)
		{
		case Area.WEATHER_BLIZZARD:
		case Area.WEATHER_SLEET:
		case Area.WEATHER_SNOW:
		case Area.WEATHER_HAIL:
			return true;
		}
		return false;
	}
	public boolean dryWeather(int weather)
	{
		switch(weather)
		{
		case Area.WEATHER_DROUGHT:
		case Area.WEATHER_DUSTSTORM:
		case Area.WEATHER_HEAT_WAVE:
			return true;
		}
		return false;
	}
	public boolean justWetWeather(int weather)
	{
		switch(weather)
		{
		case Area.WEATHER_RAIN:
		case Area.WEATHER_THUNDERSTORM:
			return true;
		}
		return false;
	}
	public boolean anyWetWeather(int weather)
	{
		return coldWetWeather(weather)||justWetWeather(weather);
	}
	public int pct()
	{
		int pct=50;
		if(getParms().length()>0)
			pct=Util.s_int(getParms());
		return pct;
	}

	public void makePuddle(Room R, int oldWeather, int newWeather)
	{
		for(int i=0;i<R.numItems();i++)
		{
			Item I=R.fetchItem(i);
			if((I instanceof Drink)
			   &&(!I.isGettable())
			   &&((I.name().toLowerCase().indexOf("puddle")>=0)
				  ||(I.name().toLowerCase().indexOf("snow")>=0)))
					return;
		}
		Item I=CMClass.getItem("GenLiquidResource");
		I.setGettable(false);
		((Drink)I).setLiquidHeld(100);
		((Drink)I).setLiquidRemaining(100);
		((Drink)I).setLiquidType(EnvResource.RESOURCE_FRESHWATER);
		I.setMaterial(EnvResource.RESOURCE_FRESHWATER);
		if(coldWetWeather(oldWeather))
		{
			I.setName("some snow");
			I.setDisplayText("some snow rests on the ground here.");
			I.setDescription("the snow is white and still quite cold!");
		}
		else
		{
			I.setName("a puddle of water");
			I.setDisplayText("a puddle of water has formed here.");
			I.setDescription("It looks drinkable.");
		}
		R.addItemRefuse(I,Item.REFUSE_MONSTER_EQ);
		R.recoverRoomStats();
	}



	int lastWeather=-1;
	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);

		if(anyWetWeather(lastWeather))
		{
			if(ticking instanceof Room)
			{
				Room R=(Room)ticking;
				Area A=R.getArea();
				if((!anyWetWeather(A.weatherType(R)))
				&&(!dryWeather(A.weatherType(R)))
				&&(Dice.rollPercentage()<pct()))
					makePuddle(R,lastWeather,A.weatherType(R));
			}
			else
			if(ticking instanceof Area)
			{
				Area A=(Area)ticking;
				if((!anyWetWeather(A.weatherType(null)))
				&&(!dryWeather(A.weatherType(null))))
					for(Enumeration e=A.getMap();e.hasMoreElements();)
					{
						Room R=(Room)e.nextElement();
						if(((R.domainType()&Room.INDOORS)==0)
						&&(R.domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER)
						&&(R.domainType()!=Room.DOMAIN_OUTDOORS_WATERSURFACE)
						&&(Dice.rollPercentage()<pct()))
							makePuddle(R,lastWeather,A.weatherType(null));
					}
			}
		}

		if(ticking instanceof Room)
			lastWeather=((Room)ticking).getArea().weatherType((Room)ticking);
		else
		if(ticking instanceof Area)
			lastWeather=((Area)ticking).weatherType(null);
		return true;
	}
}
