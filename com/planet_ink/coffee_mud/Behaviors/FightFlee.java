package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class FightFlee extends ActiveTicker
{
	public FightFlee()
	{
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		minTicks=1;maxTicks=1;chance=33;
		tickReset();
	}

	public Behavior newInstance()
	{
		return new FightFlee();
	}
	
	public void tick(Environmental ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if((canAct(ticking,tickID))&&(ticking instanceof MOB))
		{
			MOB mob=(MOB)ticking;
			if(mob.isInCombat()
			   &&(mob.getVictim()!=null)
			   &&(mob.getVictim().getVictim()==mob))
				ExternalPlay.flee(mob,"");
		}
	}
}