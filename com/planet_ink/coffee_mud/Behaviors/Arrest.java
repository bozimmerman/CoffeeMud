package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

public class Arrest extends StdBehavior
{
	public Vector warrants=new Vector();
	
	private class ArrestWarrant
	{
		public String name="";
		public int actionCode=-1;
		public int state=-1;
		public MOB officer=null;
	}
									  
	public Arrest()
	{
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	}
	public Behavior newInstance()
	{
		return new Arrest();
	}

	public void tick(Environmental ticking, int tickID)
	{
		super.tick(ticking,tickID);

		if(tickID!=Host.AREA_TICK) return;
	}
}
