package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class ShallowWater extends StdRoom implements Drink
{
	public String ID(){return "ShallowWater";}
	public ShallowWater()
	{
		super();
		name="the water";
		baseEnvStats.setWeight(2);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_ROCKS;
		domainCondition=Room.CONDITION_WET;
		baseThirst=0;
	}
	public Environmental newInstance()
	{
		return new ShallowWater();
	}
	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(affect.amITarget(this)&&(affect.targetMinor()==Affect.TYP_DRINK))
		{
			if(liquidType()==EnvResource.RESOURCE_SALTWATER)
			{
				affect.source().tell("You don't want to be drinking saltwater.");
				return false;
			}
			return true;
		}
		return super.okAffect(myHost,affect);
	}
	public void affect(Environmental myHost, Affect affect)
	{
		super.affect(myHost,affect);
		if(affect.amITarget(this)&&(affect.targetMinor()==Affect.TYP_DRINK))
		{
			MOB mob=affect.source();
			boolean thirsty=mob.curState().getThirst()<=0;
			boolean full=!mob.curState().adjThirst(thirstQuenched(),mob.maxState());
			if(thirsty)
				mob.tell("You are no longer thirsty.");
			else
			if(full)
				mob.tell("You have drunk all you can.");
		}
	}
	
	public int thirstQuenched(){return 500;}
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
