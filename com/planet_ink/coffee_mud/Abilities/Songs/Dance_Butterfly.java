package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Dance_Butterfly extends Dance
{
	public String ID() { return "Dance_Butterfly"; }
	public String name(){ return "Butterfly";}
	public int quality(){ return BENEFICIAL_OTHERS;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(invoker==null) return;

		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_FLYING);
	}
}