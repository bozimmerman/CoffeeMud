package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_Endurance extends StdAbility
{
	public Fighter_Endurance()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Endurance";
		displayText="";
		miscText="";

		canBeUninvoked=false;
		isAutoinvoked=true;
		quality=Ability.OK_SELF;

		baseEnvStats().setLevel(19);

		addQualifyingClass("Fighter",19);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Fighter_Endurance();
	}


	public int classificationCode()
	{
		return Ability.SKILL;
	}

	public boolean tick(int tickID)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return false;

		MOB mob=(MOB)affected;

		if((profficiencyCheck(0,false))
		&&(!mob.isInCombat())
		&&((mob.curState().getHitPoints()<mob.maxState().getHitPoints())
		||(mob.curState().getMana()<mob.maxState().getMana())))
		{
			mob.curState().adjState(mob,mob.maxState());
			helpProfficiency(mob);
		}
		return super.tick(tickID);
	}
}
