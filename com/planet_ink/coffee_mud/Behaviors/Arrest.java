package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

public class Arrest extends StdBehavior
{
	public Vector warrants=new Vector();
	private static final int ACTION_WARN=0;
	private static final int ACTION_THREATEN=1;
	private static final int ACTION_EXECUTE=2;
	private static final int ACTION_JAIL=3;
	
	private static final int STATE_SEEKING=0;
	private static final int STATE_ARRESTING=1;
	private static final int STATE_MOVING=2;
	private static final int STATE_REPORTING=3;
	private static final int STATE_WAITING=4;
	private static final int STATE_JAILING=5;
	private static final int STATE_EXECUTING=6;
	
	private class ArrestWarrant
	{
		public String name="";
		public String crime="";
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
