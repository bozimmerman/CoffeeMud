package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
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
		malicious=true;

		baseEnvStats().setLevel(24);

		addQualifyingClass(new Bard().ID(),24);

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

		int hpLoss=(int)Math.round(Math.floor(mob.curState().getHitPoints()*.1));
		mob.location().show(mob,null,Affect.VISUAL_WNOISE,"The painful song "+TheFight.hitWord(-1,hpLoss)+" <S-NAME>!");
		TheFight.doDamage(mob,hpLoss);
		return true;
	}

}