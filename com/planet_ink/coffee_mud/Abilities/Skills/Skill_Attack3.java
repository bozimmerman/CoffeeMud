package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Skill_Attack3 extends StdAbility
{
	private boolean active=true;

	public Skill_Attack3()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Third Attack";
		displayText="";
		miscText="";

		quality=Ability.BENEFICIAL_SELF;
		canBeUninvoked=false;
		isAutoinvoked=true;

		canTargetCode=0;
		canAffectCode=Ability.CAN_MOBS;
		
		baseEnvStats().setLevel(18);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Skill_Attack3();
	}

	public int classificationCode()
	{
		return Ability.SKILL;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		affectableStats.setSpeed(affectableStats.speed()+(1.0*(new Integer(profficiency()).doubleValue()/100.0)));
	}
	
	public void affect(Affect affect)
	{
		super.affect(affect);

		if((affected==null)||(!(affected instanceof MOB)))
			return;

		MOB mob=(MOB)affected;

		if((affect.amISource(mob))
		&&(affect.sourceMinor()==Affect.TYP_WEAPONATTACK)
		&&(Dice.rollPercentage()>97)
		&&(mob.isInCombat())
		&&(!mob.amDead())
		&&(affect.target() instanceof MOB)
		&&(mob.envStats().level()>=envStats().level()))
			helpProfficiency(mob);
	}
}
