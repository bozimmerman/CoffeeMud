package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Scavenger extends ActiveTicker
{
	public Scavenger()
	{
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		minTicks=10; maxTicks=30; chance=25;
		tickReset();
	}

	public Behavior newInstance()
	{
		return new Scavenger();
	}

	public void tick(Environmental ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if((canAct(ticking,tickID))&&(ticking instanceof MOB))
		{
			MOB mob=(MOB)ticking;
			Room thisRoom=mob.location();
			if(thisRoom.numItems()==0) return;
			for(int i=0;i<thisRoom.numItems();i++)
				if(thisRoom.fetchItem(i) instanceof DeadBody)
					return;

			Vector V=new Vector();
			V.addElement(new String("GET"));
			V.addElement(new String("ALL"));
			try
			{
				ExternalPlay.doCommand(mob,V);
			}
			catch(Exception e)
			{

			}

		}
	}
}