package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Dance_War extends Dance
{
	public String ID() { return "Dance_War"; }
	public String name(){ return "War";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected String danceOf(){return name()+" Dance";}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		MOB mob=(MOB)affected;
		if(mob==null)
			return false;
		mob.curState().setMana(0);
		return true;
	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(prancerLevel()));
		affectableStats.setArmor(affectableStats.armor()-(prancerLevel()));
		affectableStats.setDamage(affectableStats.damage()+(prancerLevel()/3));
	}
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(invoker==null) return;
		affectableStats.setStat(CharStats.CONSTITUTION,(int)Math.round(affectableStats.getStat(CharStats.CONSTITUTION)+2));
		affectableStats.setStat(CharStats.DEXTERITY,(int)Math.round(affectableStats.getStat(CharStats.DEXTERITY)+2));
		affectableStats.setStat(CharStats.INTELLIGENCE,(int)Math.round(affectableStats.getStat(CharStats.INTELLIGENCE)+2));
		affectableStats.setStat(CharStats.WISDOM,(int)Math.round(affectableStats.getStat(CharStats.WISDOM)+2));
		affectableStats.setStat(CharStats.STRENGTH,(int)Math.round(affectableStats.getStat(CharStats.STRENGTH)+2));
		affectableStats.setStat(CharStats.CHARISMA,(int)Math.round(affectableStats.getStat(CharStats.CHARISMA)+2));
	}
}