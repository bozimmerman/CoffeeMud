package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.Sense;
import com.planet_ink.coffee_mud.utils.Util;
import java.util.*;

public class WaterSurface extends StdRoom implements Drink
{
	public WaterSurface()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="the water";
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_WATERSURFACE;
		domainCondition=Room.CONDITION_WET;
	}
	public Environmental newInstance()
	{
		return new WaterSurface();
	}

	public boolean okAffect(Affect affect)
	{
		if(Sense.isSleeping(this)) return super.okAffect(affect);
		
		if(((affect.targetMinor()==Affect.TYP_LEAVE)
			||(affect.targetMinor()==Affect.TYP_ENTER)
			||(affect.targetMinor()==Affect.TYP_FLEE))
		   &&(affect.amITarget(this))
		   &&(!Sense.isSwimming(affect.source()))
		   &&((affect.source().riding()==null)||(!Sense.isSwimming(affect.source().riding()))))
		{
			MOB mob=affect.source();
			boolean hasBoat=false;
			for(int i=0;i<mob.inventorySize();i++)
			{
				Item I=mob.fetchInventory(i);
				if((I!=null)&&(I instanceof Rideable)&&(((Rideable)I).rideBasis()==Rideable.RIDEABLE_WATER))
				{	hasBoat=true; break;}
			}
			if((!Sense.isSwimming(mob))&&(!hasBoat)&&(!Sense.isInFlight(mob)))
			{
				mob.tell("You need to swim or ride a boat that way.");
				return false;
			}
			else
			if(Sense.isSwimming(mob))
				if(mob.envStats().weight()>Math.round(Util.mul(mob.maxCarry(),0.50)))
				{
					mob.tell("You are too encumbered to swim.");
					return false;
				}
		}
		else
		if(((affect.sourceMinor()==Affect.TYP_SIT)||(affect.sourceMinor()==Affect.TYP_SLEEP))
		&&((affect.source().riding()==null)||(!Sense.isSwimming(affect.source().riding()))))
		{
			affect.source().tell("You cannot rest here.");
			return false;
		}
		else
		if(affect.amITarget(this)&&(affect.targetMinor()==Affect.TYP_DRINK))
		{
			if(liquidType()==EnvResource.RESOURCE_SALTWATER)
			{
				affect.source().tell("You don't want to be drinking saltwater.");
				return false;
			}
			return true;
		}
		return super.okAffect(affect);
	}

	public void affect(Affect affect)
	{
		super.affect(affect);
		if((affect.target() instanceof Item)
		   &&(affect.targetMinor()==Affect.TYP_DROP)
		   &&(!Sense.isSleeping(this)))
			((Item)affect.target()).destroyThis();
		else
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
	public int thirstQuenched(){return 1000;}
	public int liquidHeld(){return Integer.MAX_VALUE-1000;}
	public int liquidRemaining(){return Integer.MAX_VALUE-1000;}
	public int liquidType(){return EnvResource.RESOURCE_FRESHWATER;}
	public void setThirstQuenched(int amount){}
	public void setLiquidHeld(int amount){}
	public void setLiquidRemaining(int amount){}
	public boolean containsDrink(){return true;}
}