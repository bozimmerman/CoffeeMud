package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Song_Valor extends Song
{
	public String ID() { return "Song_Valor"; }
	public String name(){ return "Valor";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(invoker!=null)
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+invoker().charStats().getStat(CharStats.CHARISMA)+(invoker.envStats().level()/3));
	}
}
