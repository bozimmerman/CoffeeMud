package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Song_Knowledge extends Song
{

	public Song_Knowledge()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Knowledge";
		displayText="(Song of Knowledge)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(12);
		quality=Ability.OK_OTHERS;

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Song_Knowledge();
	}
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(invoker==null) return;
		affectableStats.setStat(CharStats.WISDOM,(int)Math.round(affectableStats.getStat(CharStats.WISDOM)+2));
		affectableStats.setStat(CharStats.INTELLIGENCE,(int)Math.round(affectableStats.getStat(CharStats.INTELLIGENCE)+2));
	}
}
