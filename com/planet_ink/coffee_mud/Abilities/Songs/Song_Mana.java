package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Song_Mana extends Song
{
	public String ID() { return "Song_Mana"; }
	public String name(){ return "Mana";}
	public int quality(){ return OK_OTHERS;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		MOB mob=(MOB)affected;
		if(mob==null) return true;
		if(invoker==null) return true;
		//int level=invoker.envStats().level();
		//int mana=(int)Math.round(new Integer(level).doubleValue()/2.0);
		mob.curState().adjMana(100,mob.maxState());
		return true;
	}
}
