package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

import java.util.*;
import com.planet_ink.coffee_mud.utils.Sense;
import com.planet_ink.coffee_mud.utils.Util;
public class UnderWater extends StdRoom
{


	public UnderWater()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		baseEnvStats().setSensesMask(baseEnvStats().sensesMask()|EnvStats.CAN_BREATHE);
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_SWIMMING);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_UNDERWATER;
		domainCondition=Room.CONDITION_WET;
	}

	public Environmental newInstance()
	{
		return new UnderWater();
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SWIMMING);
	}

	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;
		if((affect.targetMinor()==affect.TYP_FIRE)
		||(affect.targetMinor()==affect.TYP_GAS))
		{
			affect.source().tell("That won't work underwater.");
			return false;
		}

		if(affect.amITarget(this)
		   &&(Util.bset(affect.sourceMajor(),Affect.AFF_MOVEDON))
		   &&(!Sense.isSwimming(affect.source()))
		   &&((affect.source().riding()==null)||(!Sense.isSwimming(affect.source().riding()))))
		{
			MOB mob=affect.source();
			if(!Sense.isSwimming(mob))
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
		return true;
	}
}
