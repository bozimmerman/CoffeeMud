package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import java.util.*;


public class Play_Accompaniment extends Play
{
	public String ID() { return "Play_Accompaniment"; }
	public String name(){ return "Accompaniment";}
	public int quality(){ return BENEFICIAL_OTHERS;}

	public void affectEnvStats(Environmental E, EnvStats stats)
	{
		super.affectEnvStats(E,stats);
		if((E instanceof MOB)&&(E!=invoker())&&(((MOB)E).charStats().getCurrentClass().baseClass().equals("Bard")))
		{
			int lvl=invokerLevel()/10;
			if(lvl<1) lvl=1;
			stats.setLevel(stats.level()+lvl);
		}
	}
}