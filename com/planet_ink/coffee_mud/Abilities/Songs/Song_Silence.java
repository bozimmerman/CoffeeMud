package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Song_Silence extends Song
{

	public Song_Silence()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Silence";
		displayText="(Song of Silence)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;
		skipStandardSongTick=true;

		quality=Ability.MALICIOUS;

		baseEnvStats().setLevel(13);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Song_Silence();
	}

	public boolean tick(int tickID)
	{
		if(!super.tick(tickID))
			return false;

		MOB mob=(MOB)affected;
		if(mob==null)
			return false;

		if((invoker==null)
		||(referenceSong==null)
		||(referenceSong.affecting()==null)
		||(referenceSong.invoker()==null)
		||(invoker.location()!=mob.location()))
			return false;
		return true;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(invoker==null) return;
		if(affected==invoker) return;

		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_HEAR);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SPEAK);
	}
}
