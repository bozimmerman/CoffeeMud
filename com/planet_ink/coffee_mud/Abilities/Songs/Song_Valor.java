package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Song_Valor extends Song
{
	public String ID() { return "Song_Valor"; }
	public String name(){ return "Valor";}
	public String displayText(){ return "(Song of Valor)";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	public Environmental newInstance(){	return new Song_Valor();}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(invoker==null) return;

		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+5+((int)Math.round(Util.div(invoker.envStats().level(),10.0))*5));
	}
}
