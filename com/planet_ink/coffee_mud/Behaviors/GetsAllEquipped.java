package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GetsAllEquipped extends ActiveTicker
{
	public String ID(){return "GetsAllEquipped";}
	protected int canImproveCode(){return Behavior.CAN_MOBS;}
	public GetsAllEquipped()
	{
		maxTicks=5;minTicks=10;chance=100;
		tickReset();
	}
	
	private boolean DoneEquipping=false;

	public Behavior newInstance()
	{
		return new GetsAllEquipped();
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if((canAct(ticking,tickID))&&(ticking instanceof MOB))
		{
			if(DoneEquipping)
				return true;

			MOB mob=(MOB)ticking;
			Room thisRoom=mob.location();
			if(thisRoom.numItems()==0) return true;

			DoneEquipping=true;
			Vector V=new Vector();
			V.addElement("GET");
			V.addElement("ALL");
			Vector V1=new Vector();
			V1.addElement("WEAR");
			V1.addElement("ALL");
			mob.doCommand(V);
			mob.doCommand(V1);
		}
		return true;
	}
}
