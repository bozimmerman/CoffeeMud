package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GetsAllEquipped extends ActiveTicker
{
	private boolean DoneEquipping=false;

	public GetsAllEquipped()
	{
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		maxTicks=5;minTicks=10;chance=100;
		tickReset();
		canImproveCode=Behavior.CAN_MOBS;
	}

	public Behavior newInstance()
	{
		return new GetsAllEquipped();
	}

	public void tick(Environmental ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if((canAct(ticking,tickID))&&(ticking instanceof MOB))
		{
			if(DoneEquipping)
				return;

			MOB mob=(MOB)ticking;
			Room thisRoom=mob.location();
			if(thisRoom.numItems()==0) return;

			DoneEquipping=true;
			Vector V=new Vector();
			V.addElement(new String("GET"));
			V.addElement(new String("ALL"));
			Vector V1=new Vector();
			V1.addElement(new String("WEAR"));
			V1.addElement(new String("ALL"));
			try
			{
				ExternalPlay.doCommand(mob,V);
				ExternalPlay.doCommand(mob,V1);
			}
			catch(Exception e)
			{

			}

		}
	}
}
