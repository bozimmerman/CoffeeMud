package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Song_Death extends Song
{
	public String ID() { return "Song_Death"; }
	public String name(){ return "Death";}
	public int quality(){ return MALICIOUS;}
	public Environmental newInstance(){	return new Song_Death();}
	protected boolean mindAttack(){return true;}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		MOB mob=(MOB)affected;
		if(mob==null) return false;
		if(mob==invoker) return true;
		if(invoker==null) return false;

		int hpLoss=(int)Math.round(Math.floor(mob.curState().getHitPoints()*.1));
		ExternalPlay.postDamage(invoker,mob,this,hpLoss,Affect.MASK_GENERAL|Affect.TYP_CAST_SPELL,Weapon.TYPE_BURSTING,"^SThe painful song <DAMAGE> <T-NAME>!^?");
		return true;
	}

}