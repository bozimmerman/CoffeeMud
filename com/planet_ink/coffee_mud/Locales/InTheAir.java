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
		
		if((affect.targetCode()==affect.MOVE_ENTER)
		&&(affect.target()==this))
		{
			MOB mob=affect.source();
			if(mob.location()!=this.getRoom(Directions.UP))
				if(!Sense.isFlying(mob))
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
		if((affect.target() instanceof Item)&&(!Sense.isFlying(affect.target())&&(affect.targetCode()==Affect.HANDS_DROP)))
			Falling.startFalling(affect.target(),this);
		else
		if(this.isInhabitant(affect.source()))
		{
			MOB mob=affect.source();
			if((!Sense.isFlying(mob))
			&&(getRoom(Directions.DOWN)!=null)
			&&(getExit(Directions.DOWN)!=null)
			&&(getExit(Directions.DOWN).isOpen()))
			{
				Falling.startFalling(mob,null);
			}
		}
	}
}
