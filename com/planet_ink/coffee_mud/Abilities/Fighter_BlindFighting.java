package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.Items.Weapons.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
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

		baseEnvStats().setLevel(8);

		addQualifyingClass(new Fighter().ID(),8);
		addQualifyingClass(new Paladin().ID(),baseEnvStats().level()+2);
		addQualifyingClass(new Ranger().ID(),baseEnvStats().level()+2);
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

	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(affected==null) return;
		if(!(affected instanceof MOB)) return;
		MOB mob=(MOB)affected;
		if(!mob.isInCombat()) return;
		if((!Sense.canBeSeenBy(mob.getVictim(),mob))
		&&(profficiencyCheck(0)))
		{
			int fullAdjustment=(int)TheFight.adjustedAttackBonus(mob);
			int successAdjustment=affectableStats.attackAdjustment()+(int)(Math.round(Util.div(fullAdjustment,2.0)));
			int successDamage=affectableStats.damage()*2;

			mob.envStats().setAttackAdjustment(successAdjustment);
			mob.envStats().setDamage(successDamage);
			helpProfficiency(mob);
		}
	}
}