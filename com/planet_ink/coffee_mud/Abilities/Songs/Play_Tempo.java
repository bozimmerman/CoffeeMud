package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Play_Tempo extends Play
{
	public String ID() { return "Play_Tempo"; }
	public String name(){ return "Tempo";}
	public int quality(){ return BENEFICIAL_OTHERS;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			for(int i=0;i<mob.numAllEffects();i++)
			{
				Ability A=mob.fetchEffect(i);
				if((A!=null)
				&&((A.classificationCode()&Ability.ALL_CODES)==Ability.COMMON_SKILL))
					A.tick(mob,MudHost.TICK_MOB);
			}
		}
		return true;
	}
}
