package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.Items.Weapons.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;


public class Specialization_Sword extends Specialization_Weapon
{
	boolean activated=false;
	
	public Specialization_Sword()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Sword Specialization";
		displayText="";
		miscText="";

		canBeUninvoked=false;
		isAutoinvoked=true;

		baseEnvStats().setLevel(1);

		addQualifyingClass(new Fighter().ID(),1);
		addQualifyingClass(new Ranger().ID(),1);
		addQualifyingClass(new Paladin().ID(),1);
		addQualifyingClass(new Thief().ID(),1);
		addQualifyingClass(new Bard().ID(),1);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Specialization_Sword();
	}

	public void affect(Affect affect)
	{
		if((activated)&&(affected instanceof MOB))
		{
			if(affect.amISource((MOB)affected))
			{
				if(affect.targetCode()==Affect.STRIKE_HANDS)
					helpProfficiency((MOB)affected);
			}
		}
	}
	
	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		activated=false;
		if(affected instanceof MOB)
		{
			Item myWeapon=((MOB)affected).fetchWieldedItem();
			if((myWeapon!=null)
			&&(myWeapon instanceof Weapon)
			&&(((Weapon)myWeapon).weaponClassification==Weapon.CLASS_SWORD))
			{
				activated=true;
				affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(int)Math.round(15.0*(Util.div(profficiency(),100.0))));
			}
		}
	}

}
