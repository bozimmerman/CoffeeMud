package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.Sense;
import com.planet_ink.coffee_mud.utils.Util;
import java.util.*;

public class WaterSurface extends StdRoom
{
	public WaterSurface()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
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
		if(!super.okAffect(affect))
			return false;

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
			if((!Sense.isSwimming(mob))&&(!hasBoat)&&(!Sense.isFlying(mob)))
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
		return true;
	}

	public void affect(Affect affect)
	{
		super.affect(affect);
		if((affect.target() instanceof Item)&&(affect.targetMinor()==Affect.TYP_DROP))
			((Item)affect.target()).destroyThis();
	}
}