package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Dance_CanCan extends Dance
{
	public String ID() { return "Dance_CanCan"; }
	public String name(){ return "Can-Can";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	public Environmental newInstance(){	return new Dance_CanCan();}
	public static Ability kick=null;
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		MOB mob=(MOB)affected;
		if(mob==null)
			return false;
		
		if(mob.isInCombat())
		{
			if(kick==null)
			{
				kick=CMClass.getAbility("Fighter_Kick");
				kick.setProfficiency(100);
			}
			kick.invoke(mob,mob.getVictim(),false);
		}
		return true;
	}

}
