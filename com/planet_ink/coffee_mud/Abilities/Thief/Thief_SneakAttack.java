package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_SneakAttack extends ThiefSkill
{
	public String ID() { return "Thief_SneakAttack"; }
	public String name(){ return "Sneak Attack";}
	public int quality(){return Ability.MALICIOUS;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public Environmental newInstance(){	return new Thief_SneakAttack();}
	private boolean activated=false;
	private boolean oncePerRound=false;

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(activated)
		{
			affectableStats.setDamage(affectableStats.damage()+5);
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+50);
		}
	}
	
	public boolean okAffect(Environmental myHost, Affect msg)
	{
		if(!super.okAffect(myHost,msg)) return false;
		if((affected==null)||((!(affected instanceof MOB)))) return true;
		if(activated
		   &&(!oncePerRound)
		   &&msg.amISource((MOB)affected)
		   &&(Util.bset(msg.targetCode(),Affect.MASK_HURT)))
		{
			oncePerRound=true;
			helpProfficiency((MOB)affected);
		}
		return true;
	}
	
	public boolean tick(Environmental ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
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
		if(oncePerRound) oncePerRound=false;
		return true;
	}
	
}
