package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Song_Valor extends Song
{

	public Song_Valor()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Valor";
		displayText="(Song of Valor)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(3);
		quality=Ability.BENEFICIAL_OTHERS;
		addQualifyingClass("Bard",3);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Song_Valor();
	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(invoker==null) return;

		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+5+((int)Math.round(Util.div(invoker.envStats().level(),10.0))*5));
	}
}
