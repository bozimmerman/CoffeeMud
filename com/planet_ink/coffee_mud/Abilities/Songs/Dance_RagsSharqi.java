package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Dance_RagsSharqi extends Dance
{
	public String ID() { return "Dance_RagsSharqi"; }
	public String name(){ return "Rags Sharqi";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected String danceOf(){return name()+" Dance";}

	public void affectCharState(MOB affectedMOB, CharState affectedState)
	{
		if(invoker!=null)
			affectedState.setHitPoints(affectedState.getHitPoints()+((prancerLevel()+10)*5));
	}
	public void affectCharStats(MOB affectedMOB, CharStats affectedStats)
	{
		super.affectCharStats(affectedMOB,affectedStats);
		affectedStats.setStat(CharStats.SAVE_POISON,affectedStats.getStat(CharStats.SAVE_POISON)+10);
	}
}