package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.Util;
import com.planet_ink.coffee_mud.utils.Sense;
import com.planet_ink.coffee_mud.utils.Directions;
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
		&&(affect.source().fetchAffect("Falling")==null)&&(!Sense.isFlying(affect.source())))
		{
			MOB mob=affect.source();
			if(mob.fetchAffect("Skill_Climb")!=null)
			{
				String direction=null;
				if(mob.location()==doors[Directions.UP])
					direction="down";
				else
				if(mob.location()==doors[Directions.DOWN])
					direction="up";
				if(direction!=null)
					mob.location().show(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> attempt(s) to climb "+direction+".");
			}
			else
			{
				mob.tell("You need to climb that way, if you know how.");
				return false;
			}
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
		    &&(affect.source().fetchAffect("Falling")==null))
		{
			MOB mob=affect.source();
			if(this.isInhabitant(mob))
			{
				Ability c=mob.fetchAffect("Skill_Climb");
				if((!Sense.isFlying(mob))
				&&(c==null)
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
}
