package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Dance_Capoeira extends Dance
{
	public String ID() { return "Dance_Capoeira"; }
	public String name(){ return "Capoeira";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	public Environmental newInstance(){	return new Dance_Capoeira();}
	protected String danceOf(){return name()+" Dance";}
	
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		if((affected instanceof MOB)&&(((MOB)affected).fetchWieldedItem()==null))
		{
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(prancerLevel()));
			affectableStats.setDamage(affectableStats.damage()+(prancerLevel()/3));
		}
	}
}
