package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.Directions;
import com.planet_ink.coffee_mud.utils.Sense;

import java.util.*;

public class InTheAir extends StdRoom
{
	public InTheAir()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_AIR;
		domainCondition=Room.CONDITION_NORMAL;
	}
	public Environmental newInstance()
	{
		return new InTheAir();
	}

	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;

		if((affect.targetMinor()==affect.TYP_ENTER)
		&&(affect.target()==this))
		{
			MOB mob=affect.source();
			if(mob.location()!=this.doors()[Directions.UP])
				if((!Sense.isFlying(mob))
				&&((mob.riding()==null)||(!Sense.isFlying(mob.riding()))))
				{
					mob.tell("You can't fly.");
					return false;
				}
		}
		return true;
	}

	public void affect(Affect affect)
	{
		super.affect(affect);

		if((affect.target() instanceof Item)
		   &&(!Sense.isFlying(affect.target()))
		   &&(affect.targetMinor()==Affect.TYP_DROP))
		{
			Ability falling=CMClass.getAbility("Falling");
			falling.setAffectedOne(this);
			falling.invoke(null,null,affect.target(),true);
		}
		else
		if(this.isInhabitant(affect.source()))
		{
			MOB mob=affect.source();
			if((!Sense.isFlying(mob))
			&&((mob.riding()==null)||(!Sense.isFlying(mob.riding())))
			&&(doors()[Directions.DOWN]!=null)
			&&(exits()[Directions.DOWN]!=null)
			&&(exits()[Directions.DOWN].isOpen()))
			{
				Ability falling=CMClass.getAbility("Falling");
				falling.setAffectedOne(null);
				falling.invoke(null,null,mob,true);
			}
		}
	}
}
