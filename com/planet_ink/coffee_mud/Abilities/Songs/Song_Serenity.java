package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Song_Serenity extends Song
{

	public Song_Serenity()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Serenity";
		displayText="(Song of Serenity)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;

		quality=Ability.MALICIOUS;
		mindAttack=true;

		baseEnvStats().setLevel(9);

		addQualifyingClass("Bard",9);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Song_Serenity();
	}

	public boolean okAffect(Affect msg)
	{
		if(affected==null) return true;
		if(!(affected instanceof MOB)) return true;
		if(!Sense.canBeHeardBy(invoker,affected)) return true;

		if((Util.bset(msg.targetCode(),Affect.MASK_MALICIOUS))&&(msg.amISource((MOB)affected)))
		{
			msg.source().makePeace();
			msg.source().tell("You feel too peaceful to fight.");
			return false;
		}
		return true;
	}
}
