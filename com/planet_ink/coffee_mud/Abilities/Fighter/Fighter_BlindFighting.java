package com.planet_ink.coffee_mud.Abilities.Fighter;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_BlindFighting extends StdAbility
{

	public Fighter_BlindFighting()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Blind Fighting";
		displayText="";
		miscText="";

		canBeUninvoked=false;
		isAutoinvoked=true;

		baseEnvStats().setLevel(10);
		quality=Ability.BENEFICIAL_SELF;

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Fighter_BlindFighting();
	}

	public int classificationCode()
	{
		return Ability.SKILL;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		if(!(affected instanceof MOB)) return;
		MOB mob=(MOB)affected;
		if(!mob.isInCombat()) return;
		if((!Sense.canBeSeenBy(mob.getVictim(),mob))
		&&(profficiencyCheck(0,false)))
		{
			affectableStats.setSensesMask(affectableStats.sensesMask()|Sense.CAN_SEE_VICTIM);
			helpProfficiency(mob);
		}
	}
}