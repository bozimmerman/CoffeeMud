package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Play_Ditty extends Play
{
	public String ID() { return "Play_Ditty"; }
	public String name(){ return "Ditty";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	public Environmental newInstance(){	return new Play_Ditty();}
	protected String songOf(){return "a "+name();}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((affected!=null)&&(affected instanceof MOB)&&(invoker()!=null))
		{
			MOB mob=(MOB)affected;
			mob.curState().adjHitPoints(invoker().charStats().getStat(CharStats.CHARISMA)/4,mob.maxState());
		}
		return true;
	}
}
