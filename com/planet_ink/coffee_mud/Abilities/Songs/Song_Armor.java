package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Song_Armor extends Song
{
	public String ID() { return "Song_Armor"; }
	public String name(){ return "Armor";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	public Environmental newInstance(){	return new Song_Armor();}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(invoker==null) return;

		affectableStats.setArmor((affectableStats.armor()-invoker().charStats().getStat(CharStats.CHARISMA))-(invoker.envStats().level()));
	}
}
