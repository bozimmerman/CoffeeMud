package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_MountedCombat extends StdAbility
{
	public String ID() { return "Skill_MountedCombat"; }
	public String name(){ return "Mounted Combat";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.BENEFICIAL_SELF;}
	public int classificationCode(){return Ability.SKILL;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public Environmental newInstance(){	return new Skill_MountedCombat();}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			if((mob.isInCombat())&&(mob.rangeToTarget()==0)&&(mob.riding()!=null)&&(mob.riding().amRiding(mob)))
			{
				affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+mob.baseEnvStats().attackAdjustment());
				affectableStats.setDamage(affectableStats.damage()+mob.baseEnvStats().damage());
			}
		}
	}
}
