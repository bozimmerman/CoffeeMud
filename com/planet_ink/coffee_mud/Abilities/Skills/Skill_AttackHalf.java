package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_AttackHalf extends StdAbility
{
	public String ID() { return "Skill_AttackHalf"; }
	public String name(){ return "Half Attack";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.BENEFICIAL_SELF;}
	public int classificationCode(){return Ability.SKILL;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	public Environmental newInstance(){	return new Skill_AttackHalf();}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		affectableStats.setSpeed(affectableStats.speed()+(0.5*(new Integer(profficiency()).doubleValue()/100.0)));
	}
	public void affect(Affect affect)
	{
		super.affect(affect);

		if((affected==null)||(!(affected instanceof MOB)))
			return;

		MOB mob=(MOB)affected;

		if((affect.amISource(mob))
		&&(affect.sourceMinor()==Affect.TYP_WEAPONATTACK)
		&&(Dice.rollPercentage()>95)
		&&(mob.isInCombat())
		&&(!mob.amDead())
		&&(affect.target() instanceof MOB)
		&&(mob.envStats().level()>=envStats().level()))
			helpProfficiency(mob);
	}
}
