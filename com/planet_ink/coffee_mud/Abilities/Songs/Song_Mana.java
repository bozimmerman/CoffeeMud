package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Song_Mana extends Song
{

	public Song_Mana()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Mana";
		displayText="(Song of Mana)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;

		quality=Ability.OK_OTHERS;
		mindAttack=false;

		baseEnvStats().setLevel(16);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Song_Mana();
	}
	
	public boolean tick(int tickID)
	{
		if(!super.tick(tickID))
			return false;

		MOB mob=(MOB)affected;
		if(mob==null) return true;
		if(invoker==null) return true;
		//int level=invoker.envStats().level();
		//int mana=(int)Math.round(new Integer(level).doubleValue()/2.0);
		mob.curState().adjMana(5,mob.maxState());
		return true;
	}
}
