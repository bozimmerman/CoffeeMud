package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Song_Silence extends Song
{
	public String ID() { return "Song_Silence"; }
	public String name(){ return "Silencing";}
	public int quality(){ return MALICIOUS;}
	protected boolean skipStandardSongTick(){return true;}
	public Environmental newInstance(){	return new Song_Silence();}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		MOB mob=(MOB)affected;
		if(mob==null)
			return false;

		if((invoker==null)
		||(referenceSong==null)
		||(referenceSong.affecting()==null)
		||(referenceSong.invoker()==null)
		||(invoker.location()!=mob.location()))
		{
			unsing(mob,null,this);
			return false;
		}
		return true;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(invoker==null) return;
		if(affected==invoker) return;

		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_HEAR);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_SPEAK);
	}
}
