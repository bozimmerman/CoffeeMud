package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Dance_Flamenco extends Dance
{
	public String ID() { return "Dance_Flamenco"; }
	public String name(){ return "Flamenco";}
	public int quality(){ return MALICIOUS;}
	public Environmental newInstance(){	return new Dance_Flamenco();}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		MOB mob=(MOB)affected;
		if(mob==null) return false;
		if(mob==invoker) return true;
		if(invoker==null) return false;

		int hpLoss=Dice.roll(prancerLevel(),8,0)+Dice.roll((invoker().getGroupMembers(new Hashtable()).size())-1,20,0);
		ExternalPlay.postDamage(invoker,mob,this,hpLoss,CMMsg.MASK_GENERAL|CMMsg.TYP_CAST_SPELL,Weapon.TYPE_BURSTING,"^SThe flamenco dance <DAMAGE> <T-NAME>!^?");
		return true;
	}

}