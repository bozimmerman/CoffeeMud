package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Vagrant extends StdBehavior
{
	private int sleepForTicks=0;
	private int wakeForTicks=0;
	public Vagrant()
	{
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	}
	public Behavior newInstance()
	{
		return new Vagrant();
	}
	
	public boolean okAffect(Environmental oking, Affect msg)
	{
		if((oking==null)||(!(oking instanceof MOB)))
		   return super.okAffect(oking,msg);
		MOB mob=(MOB)oking;
		if(msg.amITarget(mob)
		   &&(((msg.sourceCode()&Affect.ACT_MOVE)>0)||((msg.sourceCode()&Affect.ACT_HANDS)>0)))
		{
			if(!msg.amISource(mob))
				sleepForTicks=0;
			else
			if(sleepForTicks>0)
			{
				mob.envStats().setDisposition(mob.envStats().disposition()|EnvStats.IS_SLEEPING);
				return false;
			}
		}
		return true;
	}
	
	public void tick(Environmental ticking, int tickID)
	{
		super.tick(ticking,tickID);

		if(tickID!=Host.MOB_TICK) return;
		MOB mob=(MOB)ticking;
		if((wakeForTicks<=0)&&(sleepForTicks<=0))
		{
			if((Dice.rollPercentage()>50)||(mob.isInCombat()))
			{
				ExternalPlay.standIfNecessary(mob);
				wakeForTicks=Dice.roll(1,30,0);
			}
			else
			{
				if(Sense.aliveAwakeMobile(mob,true))
					mob.location().show(mob,mob.location(),Affect.MSG_SLEEP,"<S-NAME> curl(s) on the ground and go(es) to sleep.");
				if(Sense.isSleeping(mob))
					sleepForTicks=Dice.roll(1,10,0);
			}
		}
		else
		if(wakeForTicks>0)
			wakeForTicks--;
		else
		if(sleepForTicks>0)
			sleepForTicks--;
	}
}