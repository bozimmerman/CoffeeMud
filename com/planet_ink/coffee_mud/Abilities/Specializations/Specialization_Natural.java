package com.planet_ink.coffee_mud.Abilities.Specializations;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Specialization_Natural extends Specialization_Weapon
{
	public Specialization_Natural()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Hand-to-hand combat";
		displayText="";
		miscText="";

		canBeUninvoked=false;
		isAutoinvoked=true;

		baseEnvStats().setLevel(1);
		weaponType=Weapon.CLASS_NATURAL;

		addQualifyingClass("Fighter",1);
		addQualifyingClass("Ranger",1);
		addQualifyingClass("Paladin",1);
		addQualifyingClass("Bard",1);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Specialization_Natural();
	}

	public void affect(Affect affect)
	{
		if((activated)
		&&(Dice.rollPercentage()<25)
		&&(affected instanceof MOB)
		&&(affect.amISource((MOB)affected))
		&&(affect.targetMinor()==Affect.TYP_WEAPONATTACK)
		&&((affect.tool()==null)))
			helpProfficiency((MOB)affected);
	}


	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		activated=false;
		super.affectEnvStats(affected,affectableStats);
		if((affected instanceof MOB)&&(((MOB)affected).fetchWieldedItem()==null))
		{
			activated=true;
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(int)Math.round(15.0*(Util.div(profficiency(),100.0))));
		}
	}
}
