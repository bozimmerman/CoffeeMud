package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

public class Arrest extends StdBehavior
{
	public Vector warrants=new Vector();
	public Properties laws=null;
	
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
	
	private class ArrestWarrant implements Cloneable
	{
		public String name="";
		public String crime="";
		public int actionCode=-1;
		public int state=-1;
		public MOB officer=null;
	}

	private Properties getLaws()
	{
		if(laws==null)
		{
			String lawName=getParms();
			if(lawName.length()==0)
				lawName="law.ini";
			laws=new Properties();
			try{laws.load(new FileInputStream("resources"+File.separatorChar+lawName));}catch(IOException e){Log.errOut("Arrest",e);}
		}
		return laws;
	}
	
	public Arrest()
	{
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	}
	public Behavior newInstance()
	{
		return new Arrest();
	}

	public void affect(Affect affect)
	{
		super.affect(affect);
		switch(affect.targetMinor())
		{
		case Affect.TYP_JUSTICE:
			break;
		case Affect.TYP_DEATH:
			break;
		case Affect.TYP_CAST_SPELL:
			break;
		case Affect.TYP_SPEAK:
			break;
		}
		
	}
	
	
	public void tick(Environmental ticking, int tickID)
	{
		super.tick(ticking,tickID);

		if(tickID!=Host.AREA_TICK) return;
		
		
	}
}
