package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Dance_Musette extends Dance
{
	public String ID() { return "Dance_Musette"; }
	public String name(){ return "Musette";}
	public int quality(){ return MALICIOUS;}
	public Environmental newInstance(){	return new Dance_Musette();}
	protected String danceOf(){return name()+" Dance";}
	
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(invoker==null) return;
		if(invoker==affected) return;

		affectableStats.setSpeed(Util.div(affectableStats.speed(),2.0));
	}
}
