package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class WimpyAggressive extends Aggressive
{
	public String ID(){return "WimpyAggressive";}
	protected int tickWait=0;
	protected int tickDown=0;
	public Behavior newInstance()
	{
		return new WimpyAggressive();
	}
	
	public boolean grantsAggressivenessTo(MOB M)
	{
		return Sense.isSleeping(M)&&
			ExternalPlay.zapperCheck(getParms(),M);
	}
	public void setParms(String newParms)
	{
		super.setParms(newParms);
		tickWait=getParmVal(newParms,"delay",0);
		tickDown=tickWait;
	}

	public static void pickAWimpyFight(MOB observer)
	{
		if(!canFreelyBehaveNormal(observer)) return;
		for(int i=0;i<observer.location().numInhabitants();i++)
		{
			MOB mob=observer.location().fetchInhabitant(i);
			if((mob!=null)&&(mob!=observer)&&(Sense.isSleeping(mob)))
			{
				startFight(observer,mob,false);
				if(observer.isInCombat()) break;
			}
		}
	}

	public static void tickWimpyAggressively(Environmental ticking, int tickID)
	{
		if(tickID!=Host.MOB_TICK) return;
		if(ticking==null) return;
		if(!(ticking instanceof MOB)) return;

		pickAWimpyFight((MOB)ticking);
	}
	public void tick(Environmental ticking, int tickID)
	{
		if(tickID!=Host.MOB_TICK) return;
		if((--tickDown)<0)
		{
			tickDown=tickWait;
			tickWimpyAggressively(ticking,tickID);
		}
	}
}
