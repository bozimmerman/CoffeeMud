package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Song_Death extends Song
{
	public Song_Death()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Death";
		displayText="(Song of Death)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;

		mindAttack=true;
		quality=Ability.MALICIOUS;

		baseEnvStats().setLevel(24);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Song_Death();
	}

	public boolean tick(int tickID)
	{
		if(!super.tick(tickID))
			return false;

		MOB mob=(MOB)affected;
		if(mob==null) return false;
		if(mob==invoker) return true;
		if(invoker==null) return false;

		int hpLoss=(int)Math.round(Math.floor(mob.curState().getHitPoints()*.1));
		mob.location().show(mob,null,Affect.MSG_OK_ACTION,"The painful song "+ExternalPlay.hitWord(-1,hpLoss)+" <S-NAME>!");
		ExternalPlay.postDamage(invoker,mob,this,hpLoss);
		return true;
	}

}