package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Dance_Macabre extends Dance
{
	public String ID() { return "Dance_Macabre"; }
	public String name(){ return "Macabre";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected String danceOf(){return name()+" Dance";}

	private boolean activated=false;

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(activated)
		{
			affectableStats.setDamage(affectableStats.damage()+10);
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+75);
		}
		else
		if((affected instanceof MOB)
		&&(((MOB)affected).isInCombat())
		&&(((MOB)affected).getVictim().isInCombat())
		&&(((MOB)affected).getVictim()!=affected))
		{
			affectableStats.setDamage(affectableStats.damage()+5);
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+50);
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(Sense.isHidden(affected))
		{
			if(!activated)
			{
				activated=true;
				affected.recoverEnvStats();
			}
		}
		else
		if(activated)
		{
			activated=false;
			affected.recoverEnvStats();
		}
		return super.tick(ticking,tickID);
	}


}