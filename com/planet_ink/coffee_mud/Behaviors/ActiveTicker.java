package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class ActiveTicker extends StdBehavior
{
	protected int tickDown=0;
	protected int minTicks=10;
	protected int maxTicks=30;
	protected int chance=100;

	public ActiveTicker()
	{
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		tickReset();
		canImproveCode=Behavior.CAN_ITEMS|Behavior.CAN_MOBS|Behavior.CAN_ROOMS|Behavior.CAN_EXITS|Behavior.CAN_AREAS;
	}

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
		minTicks=getVal(parms,"min",minTicks);
		maxTicks=getVal(parms,"max",maxTicks);
		chance=getVal(parms,"chance",chance);
		tickReset();
	}

	public static int getVal(String text, String key, int defaultValue)
	{
		text=text.toUpperCase();
		key=key.toUpperCase();
		int x=text.indexOf(key);
		while(x>=0)
		{
			if((x==0)||(!Character.isLetter(text.charAt(x-1))))
			{
				while((x<text.length())&&(text.charAt(x)!='=')&&(!Character.isDigit(text.charAt(x))))
					x++;
				if((x<text.length())&&(text.charAt(x)=='='))
				{
					while((x<text.length())&&(!Character.isDigit(text.charAt(x))))
						x++;
					if(x<text.length())
					{
						text=text.substring(x);
						x=0;
						while((x<text.length())&&(Character.isDigit(text.charAt(x))))
							x++;
						return Util.s_int(text.substring(0,x));
					}
				}
				x=-1;
			}
			else
				x=text.toUpperCase().indexOf(key.toUpperCase(),x+1);
		}
		return defaultValue;
	}

	protected boolean canAct(Environmental ticking, int tickID)
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
