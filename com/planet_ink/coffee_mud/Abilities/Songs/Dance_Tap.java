package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Dance_Tap extends Dance
{
	public String ID() { return "Dance_Tap"; }
	public String name(){ return "Tap";}
	public int quality(){ return MALICIOUS;}
	public Environmental newInstance(){	return new Dance_Tap();}
	protected String danceOf(){return name()+" Dance";}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		MOB mob=(MOB)affected;
		if(mob==null)
			return false;
		
		if(mob!=invoker())
		{
			mob.curState().adjMovement(-(prancerLevel()/3),mob.maxState());
			mob.curState().adjMana(-prancerLevel(),mob.maxState());
		}
		return true;
	}
}
