package com.planet_ink.coffee_mud.Locales;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.Exits.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.*;

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

		if(affect.amITarget(this)&&(affect.targetType()==Affect.MOVE)&&(affect.source().fetchAffect(new Falling().ID())==null)&&(!Sense.isFlying(affect.source())))
		{
			MOB mob=affect.source();
			if(mob.fetchAffect(new Skill_Climb().ID())!=null)
			{
				String direction=null;
				if(mob.location()==doors[Directions.UP])
					direction="down";
				else
				if(mob.location()==doors[Directions.DOWN])
					direction="up";
				if(direction!=null)
					mob.location().show(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> attempt(s) to climb "+direction+".");
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
		if((affect.target() instanceof Item)&&(!Sense.isFlying(affect.target())&&(affect.targetCode()==Affect.HANDS_DROP)))
			Falling.startFalling(affect.target(),this);
		else
		if(affect.amITarget(this)&&(affect.targetType()==Affect.MOVE)&&(affect.source().fetchAffect(new Falling().ID())==null))
		{
			MOB mob=affect.source();
			if(this.isInhabitant(mob))
			{
				Skill_Climb c=(Skill_Climb)mob.fetchAffect(new Skill_Climb().ID());
				if((!Sense.isFlying(mob))
				&&((c==null)||((c!=null)&&(!c.successful)))
				&&(getRoom(Directions.DOWN)!=null)
				&&(getExit(Directions.DOWN)!=null)
				&&(getExit(Directions.DOWN).isOpen()))
					Falling.startFalling(mob,null);
			}
		}
	}
}
