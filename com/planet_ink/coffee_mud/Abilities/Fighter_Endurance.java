package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
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

		baseEnvStats().setLevel(18);

		addQualifyingClass(new Fighter().ID(),18);
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

		if(profficiencyCheck(0))
		{
			mob.curState().adjState(mob,mob.maxState());
			helpProfficiency(mob);
		}
		return super.tick(tickID);
	}
}
