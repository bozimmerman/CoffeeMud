package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

public class Arrest extends StdBehavior
{
	private MOB target=null;
	public Arrest()
	{
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	}
	public Behavior newInstance()
	{
		return new Arrest();
	}

	public void lookForCriminals(MOB observer)
	{
		if(!canFreelyBehaveNormal(observer)) return;
	}

	public void makeTheArrest(MOB observer)
	{
		if(!canFreelyBehaveNormal(observer)) return;
	}

	public void tick(Environmental ticking, int tickID)
	{
		super.tick(ticking,tickID);

		if(tickID!=Host.MOB_TICK) return;
		if(!canFreelyBehaveNormal(ticking)) return;
		MOB mob=(MOB)ticking;
		if(target!=null)
			makeTheArrest(mob);
		else
			lookForCriminals(mob);
	}
}
