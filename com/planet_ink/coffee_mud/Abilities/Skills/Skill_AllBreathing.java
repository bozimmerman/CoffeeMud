package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Skill_AllBreathing extends StdAbility
{

	public Skill_AllBreathing()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="All Breathing";
		displayText="";
		miscText="";

		canBeUninvoked=false;
		isAutoinvoked=true;
		quality=Ability.BENEFICIAL_SELF;

		canTargetCode=0;
		canAffectCode=Ability.CAN_MOBS;
		
		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Skill_AllBreathing();
	}

	public int classificationCode()
	{
		return Ability.SKILL;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if(!Sense.canBreathe(mob))
			affectableStats.setSensesMask(affectableStats.sensesMask()-EnvStats.CAN_BREATHE);
	}
}
