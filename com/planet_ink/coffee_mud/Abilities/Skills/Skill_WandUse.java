package com.planet_ink.coffee_mud.Abilities.Skills;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Skill_WandUse extends StdAbility
{

	boolean activated=false;

	public Skill_WandUse()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Wands";
		displayText="";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;
		quality=Ability.BENEFICIAL_SELF;

		canTargetCode=0;
		canAffectCode=Ability.CAN_MOBS;
		
		baseEnvStats().setLevel(1);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Skill_WandUse();
	}
}
