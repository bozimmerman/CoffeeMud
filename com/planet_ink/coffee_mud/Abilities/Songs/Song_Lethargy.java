package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Song_Lethargy extends Song
{
	public String ID() { return "Song_Lethargy"; }
	public String name(){ return "Lethargy";}
	public String displayText(){ return "(Song of Lethargy)";}
	public int quality(){ return MALICIOUS;}
	public Environmental newInstance(){	return new Song_Lethargy();}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(invoker==null) return;
		if(invoker==affected) return;

		affectableStats.setSpeed(Util.div(affectableStats.speed(),2.0));
	}
}
