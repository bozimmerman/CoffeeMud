package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Song_Clumsiness extends Song
{
	public String ID() { return "Song_Clumsiness"; }
	public String name(){ return "Clumsiness";}
	public int quality(){ return MALICIOUS;}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(invoker==null) return;
		if(affected==invoker) return;

		affectableStats.setAttackAdjustment((affectableStats.attackAdjustment()-invoker().charStats().getStat(CharStats.CHARISMA))-(invoker.envStats().level()*2));
	}
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(invoker==null) return;
		if(affected==invoker) return;

		affectableStats.setStat(CharStats.DEXTERITY,(int)Math.round(Util.div(affectableStats.getStat(CharStats.DEXTERITY),2.0)));
	}
}
