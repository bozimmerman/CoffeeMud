package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_SneakAttack extends ThiefSkill
{
	public String ID() { return "Thief_SneakAttack"; }
	public String name(){ return "Sneak Attack";}
	public String displayText(){return "";}
	public int quality(){return Ability.MALICIOUS;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
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

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg)) return false;
		if((affected==null)||((!(affected instanceof MOB)))) return true;
		if(activated
		   &&(!oncePerRound)
		   &&msg.amISource((MOB)affected)
		   &&(msg.targetMinor()==CMMsg.TYP_DAMAGE))
		{
			oncePerRound=true;
			helpProfficiency((MOB)affected);
		}
		return true;
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
		if(oncePerRound) oncePerRound=false;
		return super.tick(ticking,tickID);
	}

}
