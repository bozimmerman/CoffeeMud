package com.planet_ink.coffee_mud.Abilities.Fighter;

import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighter_RapidShot extends StdAbility
{
	public Fighter_RapidShot()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Rapid Shot";
		displayText="";
		miscText="";

		canAffectCode=Ability.CAN_MOBS;
		canTargetCode=0;
		
		canBeUninvoked=false;
		isAutoinvoked=true;
		quality=Ability.BENEFICIAL_SELF;

		baseEnvStats().setLevel(20);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Fighter_RapidShot();
	}

	public int classificationCode()
	{
		return Ability.SKILL;
	}

	public boolean tick(int tickID)
	{
		if(!super.tick(tickID)) return false;
		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((mob.isInCombat())&&(mob.rangeToTarget()>0))
		{
			Item w=mob.fetchWieldedItem();
			if((w!=null)
			&&(w instanceof Weapon)
			&&(((Weapon)w).weaponClassification()==Weapon.CLASS_RANGED)
			&&(profficiencyCheck(0,false))
			&&(((Weapon)w).ammunitionType().length()>0))
			{
				helpProfficiency(mob);
				for(int i=0;i<(adjustedLevel(mob)/7);i++)
					ExternalPlay.postAttack(mob,mob.getVictim(),w);
			}
		}
		return true;
	}
}
