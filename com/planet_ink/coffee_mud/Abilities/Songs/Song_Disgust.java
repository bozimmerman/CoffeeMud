package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Song_Disgust extends Song
{
	public String ID() { return "Song_Disgust"; }
	public String name(){ return "Disgust";}
	public int quality(){ return MALICIOUS;}
	public Environmental newInstance(){	return new Song_Disgust();}
	protected boolean mindAttack(){return true;}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
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