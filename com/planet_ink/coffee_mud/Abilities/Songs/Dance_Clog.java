package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Dance_Clog extends Dance
{
	public String ID() { return "Dance_Clog"; }
	public String name(){ return "Clog Dance";}
	public int quality(){ return MALICIOUS;}
	protected String danceOf(){return name();}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(invoker==null) return;
		if(affected==invoker) return;

		affectableStats.setAttackAdjustment((affectableStats.attackAdjustment()-invoker().charStats().getStat(CharStats.CHARISMA))-((int)Math.round(invoker.envStats().level())*2));
	}
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(invoker==null) return;
		if(affected==invoker) return;

		affectableStats.setStat(CharStats.DEXTERITY,(int)Math.round(Util.div(affectableStats.getStat(CharStats.DEXTERITY),2.0)));
	}
}
