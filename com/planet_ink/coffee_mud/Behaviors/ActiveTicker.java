package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class ActiveTicker extends StdBehavior
{
	public String ID(){return "ActiveTicker";}
	protected int canImproveCode(){return Behavior.CAN_ITEMS|Behavior.CAN_MOBS|Behavior.CAN_ROOMS|Behavior.CAN_EXITS|Behavior.CAN_AREAS;}
	
	protected int minTicks=10;
	protected int maxTicks=30;
	protected int chance=100;
	protected int tickDown=(int)Math.round(Math.random()*(maxTicks-minTicks))+minTicks;

	protected void tickReset()
	{
		tickDown=(int)Math.round(Math.random()*(maxTicks-minTicks))+minTicks;
	}

	public Behavior newInstance()
	{
		return new ActiveTicker();
	}

	public void setParms(String newParms)
	{
		parms=newParms;
		minTicks=getParmVal(parms,"min",minTicks);
		maxTicks=getParmVal(parms,"max",maxTicks);
		chance=getParmVal(parms,"chance",chance);
		tickReset();
	}

	public String getParmsNoTicks()
	{
		String parms=getParms();
		char c=';';
		int x=parms.indexOf(c);
		if(x<0){ c='/'; x=parms.indexOf(c);}
		if(x>0)
		{
			if((x+1)>parms.length())
				return "";
			parms=parms.substring(x+1);
		}
		else
		{
			return "";
		}
		return parms;
	}
	
	protected boolean canAct(Tickable ticking, int tickID)
	{
		if((tickID==Host.MOB_TICK)
		||(tickID==Host.ITEM_BEHAVIOR_TICK)
		||(tickID==Host.ROOM_BEHAVIOR_TICK)
		||((tickID==Host.AREA_TICK)&&(ticking instanceof Area)))
		{
			int a=Dice.rollPercentage();
			if((--tickDown)<1)
			{
				tickReset();
				if((ticking instanceof MOB)&&(!canActAtAll(ticking)))
					return false;
				if(a>chance)
					return false;
				return true;
			}
		}
		return false;
	}
}
