package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Song_Charm extends Song
{

	public Song_Charm()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Charm";
		displayText="(Song of Charm)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(4);

		addQualifyingClass("Bard",4);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Song_Charm();
	}
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{
		super.affectCharStats(affectedMob,affectableStats);
		if(invoker==null) return;

		if(invoker.charStats().getCharisma()<affectableStats.getCharisma()+4)
			affectableStats.setCharisma(affectableStats.getCharisma()+4);
		else
			affectableStats.setCharisma(invoker.charStats().getCharisma()+2);
	}
}
