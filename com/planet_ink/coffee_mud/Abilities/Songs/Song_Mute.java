package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Song_Mute extends Song
{
	public String ID() { return "Song_Mute"; }
	public String name(){ return "Mute";}
	public String displayText(){ return "(Song of Mute)";}
	public int quality(){ return MALICIOUS;}
	public Environmental newInstance(){	return new Song_Mute();	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(invoker==null) return;
		if(affected==invoker) return;

		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_SPEAK);
	}
}
