package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Dance_Butoh extends Dance
{
	public String ID() { return "Dance_Butoh"; }
	public String name(){ return "Butoh";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	public Environmental newInstance(){	return new Dance_Butoh();}
	protected String danceOf(){return name()+" Dance";}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		MOB mob=(MOB)affected;
		if(mob==null) return true;
		if(invoker==null) return true;
		mob.curState().adjMana(prancerLevel()*4,mob.maxState());
		return true;
	}
}