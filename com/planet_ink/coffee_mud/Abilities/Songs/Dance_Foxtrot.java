package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Dance_Foxtrot extends Dance
{
	public String ID() { return "Dance_Foxtrot"; }
	public String name(){ return "Foxtrot";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	public Environmental newInstance(){	return new Dance_Foxtrot();}
	private int ticks=1;
	private int increment=1;
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		MOB mob=(MOB)affected;
		if(mob==null)
			return false;
		
		mob.curState().adjMovement((invokerManaCost/15)+increment,mob.maxState());
		if((++ticks)>15)
		{
			increment++;
			ticks=1;
		}
		return true;
	}

}
