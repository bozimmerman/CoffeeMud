package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Song_Disgust extends Song
{

	public Song_Disgust()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Disgust";
		displayText="(Song of Disgust)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;

		quality=Ability.MALICIOUS;
		mindAttack=true;

		baseEnvStats().setLevel(7);

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Song_Disgust();
	}
	
	public boolean tick(int tickID)
	{
		if(!super.tick(tickID))
			return false;

		MOB mob=(MOB)affected;
		if(mob==null) return true;
		if(mob==invoker) return true;
		if(invoker==null) return true;
		Room room=invoker.location();
		if((!mob.isInCombat())&&(room!=null))
		{
			MOB newMOB=room.fetchInhabitant(Dice.roll(1,room.numInhabitants(),-1));
			if(newMOB!=mob)
			{
				room.show(mob,newMOB,Affect.MSG_OK_ACTION,"<S-NAME> appear(s) disgusted with <T-NAMESELF>.");
				ExternalPlay.postAttack(mob,newMOB,mob.fetchWieldedItem());
			}
		}
		return true;
	}

}