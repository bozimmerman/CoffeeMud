package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Song_Health extends Song
{

	public Song_Health()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Health";
		displayText="(Song of Health)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;

		quality=Ability.BENEFICIAL_OTHERS;

		baseEnvStats().setLevel(12);

		addQualifyingClass("Bard",12);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Song_Health();
	}

	public void affectCharState(MOB affectedMOB, CharState affectedState)
	{
		if(invoker!=null)
			affectedState.setHitPoints(affectedState.getHitPoints()+(invoker.envStats().level()*5));
	}
}
