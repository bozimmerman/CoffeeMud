package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class CorpseEater extends ActiveTicker
{
	public String ID(){return "CorpseEater";}
	protected int canImproveCode(){return Behavior.CAN_MOBS;}
	public CorpseEater()
	{
		minTicks=5; maxTicks=20; chance=75;
		tickReset();
	}
	public Behavior newInstance()
	{
		return new CorpseEater();
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if((canAct(ticking,tickID))&&(ticking instanceof MOB))
		{
			MOB mob=(MOB)ticking;
			Room thisRoom=mob.location();
			if(thisRoom.numItems()==0) return true;
			for(int i=0;i<thisRoom.numItems();i++)
			{
				Item I=thisRoom.fetchItem(i);
				if((I!=null)&&(I instanceof DeadBody)&&(Sense.canBeSeenBy(I,mob)||Sense.canSmell(mob)))
				{
					if(I instanceof Container)
						((Container)I).emptyPlease();
					thisRoom.show(mob,null,I,Affect.MSG_NOISYMOVEMENT,"<S-NAME> eat(s) <O-NAME>.");
					I.destroy();
					return true;
				}
			}
		}
		return true;
	}
}