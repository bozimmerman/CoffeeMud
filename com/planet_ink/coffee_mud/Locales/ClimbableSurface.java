package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class ClimbableSurface extends StdRoom
{
	public ClimbableSurface()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		recoverEnvStats();
		domainType=Room.DOMAIN_OUTDOORS_ROCKS;
		domainCondition=Room.CONDITION_NORMAL;
	}
	public Environmental newInstance()
	{
		return new ClimbableSurface();
	}

	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;

		if(affect.amITarget(this)
		&&(Util.bset(affect.targetCode(),Affect.AFF_MOVEDON))
		&&(!Sense.isFalling(affect.source()))
		&&(!Sense.isClimbing(affect.source()))
		&&(!Sense.isFlying(affect.source())))
		{
			affect.source().tell("You need to climb that way, if you know how.");
			return false;
		}
		return true;
	}

	public void affect(Affect affect)
	{
		super.affect(affect);

		if((affect.target() instanceof Item)
		   &&(!Sense.isFlying(affect.target())
			  &&(affect.targetMinor()==Affect.TYP_DROP)))
		{
			Ability falling=CMClass.getAbility("Falling");
			falling.setAffectedOne(this);
			falling.invoke(null,null,affect.target(),true);
		}
		else
		if(affect.amITarget(this)
			&&(Util.bset(affect.targetCode(),Affect.AFF_MOVEDON))
			&&(!Sense.isFalling(affect.source())))
		{
			MOB mob=affect.source();
			if(this.isInhabitant(mob))
			{
				if((!Sense.isFlying(mob))
				&&(!Sense.isClimbing(mob))
				&&(getRoomInDir(Directions.DOWN)!=null)
				&&(getExitInDir(Directions.DOWN)!=null)
				&&(getExitInDir(Directions.DOWN).isOpen()))
				{
					Ability falling=CMClass.getAbility("Falling");
					falling.setAffectedOne(null);
					falling.invoke(null,null,mob,true);
				}
			}
		}
	}
}
