package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.Sense;
import com.planet_ink.coffee_mud.utils.Util;
import java.util.*;

public class IndoorWaterSurface extends StdRoom implements Drink
{
	public String ID(){return "IndoorWaterSurface";}
	public IndoorWaterSurface()
	{
		super();
		name="the water";
		recoverEnvStats();
		domainType=Room.DOMAIN_INDOORS_WATERSURFACE;
		domainCondition=Room.CONDITION_WET;
	}
	public Environmental newInstance()
	{
		return new IndoorWaterSurface();
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		switch(WaterSurface.isOkWaterSurfaceAffect(this,affect))
		{
		case -1: return false;
		case 1: return true;
		}
		return super.okAffect(myHost,affect);
	}

	public void affect(Environmental myHost, Affect affect)
	{
		super.affect(myHost,affect);
		UnderWater.sinkAffects(this,affect);
	}
	public int thirstQuenched(){return 1000;}
	public int liquidHeld(){return Integer.MAX_VALUE-1000;}
	public int liquidRemaining(){return Integer.MAX_VALUE-1000;}
	public int liquidType(){return EnvResource.RESOURCE_FRESHWATER;}
	public void setLiquidType(int newLiquidType){}
	public void setThirstQuenched(int amount){}
	public void setLiquidHeld(int amount){}
	public void setLiquidRemaining(int amount){}
	public boolean containsDrink(){return true;}
	public Vector resourceChoices(){return UnderWater.roomResources;}
}